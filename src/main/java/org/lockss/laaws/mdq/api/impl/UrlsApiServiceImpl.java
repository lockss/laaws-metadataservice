/*

 Copyright (c) 2016 Board of Trustees of Leland Stanford Jr. University,
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

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.lockss.app.LockssDaemon;
import org.lockss.daemon.OpenUrlResolver;
import org.lockss.laaws.mdq.api.ApiException;
import org.lockss.laaws.mdq.api.UrlsApiService;
import org.lockss.laaws.mdq.model.OpenUrlParams;
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
  public Response getUrlDoi(String doi, SecurityContext securityContext)
      throws ApiException {
    if (log.isDebugEnabled()) log.debug("doi = " + doi);

    try {
      // Build an OpenURL query.
      Map<String, String> params = new HashMap<String,String>();
      params.put("rft_id", "info:doi/" + doi);

      return Response.ok().entity(resolveOpenUrl(params)).build();
    } catch (Exception e) {
      String message = "Cannot getUrlDoi() for doi = '" + doi + "'";
      log.error(message, e);
      throw new ApiException(1, message + ": " + e.getMessage());
    }
  }

  /**
   * Provides the URL that results from performing an OpenURL query
   * 
   * @param params
   *          An OpenUrlParams with the OpenURL query parameters.
   * @param securityContext
   *          A SecurityContext providing access to security related
   *          information.
   * @return a Response with any data that needs to be returned to the runtime.
   * @throws ApiException
   *           if there is a problem obtaining the URL.
   */
  @Override
  public Response postOpenUrl(OpenUrlParams params,
      SecurityContext securityContext) throws ApiException {
    if (log.isDebugEnabled()) log.debug("params = " + params);

    try {
      return Response.ok().entity(resolveOpenUrl(params)).build();
    } catch (Exception e) {
      String message = "Cannot postOpenUrl() for params = '" + params + "'";
      log.error(message, e);
      throw new ApiException(1, message + ": " + e.getMessage());
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
    String url = new OpenUrlResolver(LockssDaemon.getLockssDaemon())
	  .resolveOpenUrl(params).getResolvedUrl();
    if (log.isDebugEnabled()) log.debug("url = " + url);

    UrlInfo result = new UrlInfo(params, url);
    if (log.isDebugEnabled()) log.debug("result = " + result);

    return result;
  }
}
