/*

 Copyright (c) 2016-2017 Board of Trustees of Leland Stanford Jr. University,
 all rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Except as contained in this notice, the name of Stanford University shall not
 be used in advertising or otherwise to promote the sale, use or other dealings
 in this Software without prior written authorization from Stanford University.

 */
package org.lockss.laaws.mdq.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.lockss.app.LockssDaemon;
import org.lockss.daemon.OpenUrlResolver;
import org.lockss.daemon.OpenUrlResolver.OpenUrlInfo;
import org.lockss.laaws.mdq.api.ApiException;
import org.lockss.laaws.mdq.api.UrlsApiService;
import org.lockss.laaws.mdq.model.UrlInfo;
import org.apache.log4j.Logger;

/**
 * Implementation of the base provider of access to URLs.
 */
public class UrlsApiServiceImpl extends UrlsApiService {
  private static Logger log = Logger.getLogger(UrlsApiServiceImpl.class);

  /**
   * Provides the access URL for a DOI given the DOI.
   * 
   * @param doi
   *          A String with the DOI for which the access URL is requested.
   * @param securityContext
   *          A SecurityContext providing access to security related
   *          information.
   * @return a Response with any data that needs to be returned to the runtime.
   * @throws ApiException
   *           if there is a problem obtaining the URL.
   */
  @Override
  public Response getUrlsDoi(String doi, SecurityContext securityContext)
      throws ApiException {
    if (log.isDebugEnabled()) log.debug("doi = " + doi);

    try {
      // Build an OpenURL query.
      Map<String, String> params = new HashMap<String,String>();
      params.put("rft_id", "info:doi/" + doi);

      return Response.ok().entity(resolveOpenUrl(params)).build();
    } catch (Exception e) {
      String message = "Cannot getUrlsDoi() for doi = '" + doi + "'";
      log.error(message, e);
      return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, message);
    }
  }

  /**
   * Provides the URL that results from performing an OpenURL query
   * 
   * @param params
   *          A List<String> with the OpenURL query parameters.
   * @param securityContext
   *          A SecurityContext providing access to security related
   *          information.
   * @return a Response with any data that needs to be returned to the runtime.
   * @throws ApiException
   *           if there is a problem obtaining the URL.
   */
  @Override
  public Response getUrlsOpenUrl(List<String> params,
      SecurityContext securityContext) throws ApiException {
    if (log.isDebugEnabled()) log.debug("params = " + params);

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

      return Response.ok().entity(resolveOpenUrl(openUrlParams)).build();
    } catch (Exception e) {
      String message = "Cannot getUrlsOpenUrl() for params = '" + params + "'";
      log.error(message, e);
      return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, message);
    }
  }

  /**
   * Provides the results of an OpenUrl query.
   * 
   * @param params
   *          A Map<String, String> with the OpenURL query parameters.
   * @return a UrlInfo with the results.
   */
  private UrlInfo resolveOpenUrl(Map<String, String> params) {
    if (log.isDebugEnabled()) log.debug("params = " + params);

    // The unique URLs that result from performing the query.
    Set<String> urls = new HashSet<String>();

    // Make the query.
    OpenUrlInfo openUrlInfo =
	new OpenUrlResolver(LockssDaemon.getLockssDaemon())
	.resolveOpenUrl(params);
    if (log.isDebugEnabled()) log.debug("openUrlInfo = " + openUrlInfo);

    // Loop through all the results.
    Iterator<OpenUrlInfo> iterator = openUrlInfo.iterator();

    while (iterator.hasNext()) {
      OpenUrlInfo next = iterator.next();
      if (log.isDebugEnabled()) log.debug("next = " + next);

      String url = next.getResolvedUrl();
      if (log.isDebugEnabled()) log.debug("url = " + url);

      if (url != null && !"null".equalsIgnoreCase(url)) {
	// Accumulate the resulting unique URLs.
	urls.add(url);
      }
    }

    if (log.isDebugEnabled()) log.debug("urls = " + urls);

    UrlInfo result = new UrlInfo(params, new ArrayList<String>(urls));
    if (log.isDebugEnabled()) log.debug("result = " + result);

    return result;
  }

  /**
   * Provides the appropriate response in case of an error.
   * 
   * @param statusCode
   *          A Response.Status with the error status code.
   * @param message
   *          A String with the error message.
   * @return a Response with the error response.
   */
  private Response getErrorResponse(Response.Status status, String message) {
    return Response.status(status).entity(toJsonMessage(message)).build();
  }

  /**
   * Formats to JSON any message to be returned.
   * 
   * @param message
   *          A String with the message to be formatted.
   * @return a String with the JSON-formatted message.
   */
  private String toJsonMessage(String message) {
    return "{\"message\":\"" + message + "\"}"; 
  }
}
