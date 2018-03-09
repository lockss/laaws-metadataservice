/*

Copyright (c) 2000-2018 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.lockss.laaws.mdq.api;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.MalformedParametersException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lockss.app.LockssDaemon;
import org.lockss.daemon.OpenUrlResolver;
import org.lockss.daemon.OpenUrlResolver.OpenUrlInfo;
import org.lockss.laaws.mdq.model.UrlInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

/**
 * Controller for access to URLs.
 */
@RestController
public class UrlsApiController implements UrlsApi {
  private static final Logger logger =
      LoggerFactory.getLogger(UrlsApiController.class);

  /**
   * Provides the URL for a DOI given the DOI.
   * 
   * @param doi
   *          A String with the DOI for which the URL is requested.
   * @return a {@code ResponseEntity<UrlInfo>} with the URL information.
   */
  @Override
  @RequestMapping(value = "/urls/doi/{doi}",
  produces = { "application/json" },
  method = RequestMethod.GET)
  public ResponseEntity<UrlInfo> getUrlsDoi(@PathVariable("doi") String doi) {
    if (logger.isDebugEnabled()) logger.debug("doi = " + doi);

    String decodedDoi = null;

    try {
      decodedDoi = UriUtils.decode(doi, "UTF-8");
      if (logger.isDebugEnabled()) logger.debug("decodedDoi = " + decodedDoi);

      // Build an OpenURL query.
      Map<String, String> params = new HashMap<String,String>();
      params.put("rft_id", "info:doi/" + decodedDoi);

      return new ResponseEntity<UrlInfo>(resolveOpenUrl(params), HttpStatus.OK);
    } catch (UnsupportedEncodingException uee) {
      String message = "Cannot decode doi = '" + doi + "'";
      logger.error(message, uee);
      throw new MalformedParametersException(message);
    } catch (Exception e) {
      String message = "Cannot getUrlsDoi() for doi = '" + decodedDoi + "'";
      logger.error(message, e);
      throw new RuntimeException(message);
    }
  }

  /**
   * Provides the URL that results from performing an OpenURL query
   * 
   * @param params
   *          A {@code List<String>} with the OpenURL query parameters.
   * @return a {@code ResponseEntity<UrlInfo>} with the URL information.
   */
  @Override
  @RequestMapping(value = "/urls/openurl",
  produces = { "application/json" },
  method = RequestMethod.GET)
  public ResponseEntity<UrlInfo> getUrlsOpenUrl(
      @RequestParam("params") List<String> params) {
    if (logger.isDebugEnabled()) logger.debug("params = " + params);

    try {
      // Build the OpenURL query.
      Map<String, String> openUrlParams = new HashMap<String,String>();

      for (String param : params) {
	int sepLoc = param.trim().indexOf("=");

	if (sepLoc > 0 && sepLoc < param.length() - 1) {
	  openUrlParams.put(param.substring(0, sepLoc),
	      param.substring(sepLoc + 1));
	}
      }

      if (logger.isDebugEnabled())
	logger.debug("openUrlParams = " + openUrlParams);

      return new ResponseEntity<UrlInfo>(resolveOpenUrl(openUrlParams),
	  HttpStatus.OK);
    } catch (Exception e) {
      String message = "Cannot getUrlsOpenUrl() for params = '" + params + "'";
      logger.error(message, e);
      throw new RuntimeException(message);
    }
  }

  /**
   * Provides the results of an OpenUrl query.
   * 
   * @param params
   *          A {@code Map<String, String>} with the OpenURL query parameters.
   * @return a UrlInfo with the results.
   */
  private UrlInfo resolveOpenUrl(Map<String, String> params) {
    if (logger.isDebugEnabled()) logger.debug("params = " + params);

    // The unique URLs that result from performing the query.
    Set<String> urls = new HashSet<String>();

    // Make the query.
    OpenUrlInfo openUrlInfo =
	new OpenUrlResolver(LockssDaemon.getLockssDaemon())
	.resolveOpenUrl(params);
    if (logger.isDebugEnabled()) logger.debug("openUrlInfo = " + openUrlInfo);

    // Loop through all the results.
    Iterator<OpenUrlInfo> iterator = openUrlInfo.iterator();

    while (iterator.hasNext()) {
      OpenUrlInfo next = iterator.next();
      if (logger.isDebugEnabled()) logger.debug("next = " + next);

      String url = next.getResolvedUrl();
      if (logger.isDebugEnabled()) logger.debug("url = " + url);

      if (url != null && !"null".equalsIgnoreCase(url)) {
	// Accumulate the resulting unique URLs.
	urls.add(url);
      }
    }

    if (logger.isDebugEnabled()) logger.debug("urls = " + urls);

    UrlInfo result = new UrlInfo(params, new ArrayList<String>(urls));
    if (logger.isDebugEnabled()) logger.debug("result = " + result);

    return result;
  }

  @ExceptionHandler(MalformedParametersException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse badRequestExceptionHandler(
      MalformedParametersException e) {
    return new ErrorResponse(e.getMessage()); 	
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse internalExceptionHandler(RuntimeException e) {
    return new ErrorResponse(e.getMessage()); 	
  }
}
