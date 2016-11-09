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
package org.lockss.laaws.mdq.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.lockss.laaws.mdq.api.factories.UrlApiServiceFactory;
import org.lockss.laaws.mdq.model.OpenUrlParams;
import org.lockss.rs.auth.Roles;

@Path("/url")
@Api(value = "/url")
public class UrlApi  {
  private final UrlApiService delegate = UrlApiServiceFactory.getUrlApi();

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
  @GET
  @Path("/doi/{doi}")
  @Produces({"application/json"})
  @RolesAllowed(Roles.ROLE_ANY) // Allow any authenticated user.
  public Response getUrlDoi(
      @ApiParam(value = "The DOI for which the access URL is requested.",
      required = true) @PathParam("doi")
      String doi, @Context SecurityContext securityContext)
	  throws ApiException {
    return delegate.getUrlDoi(doi, securityContext);
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
  @POST
  @Path("/openurl")
  @Produces({ "application/json" })
  @RolesAllowed(Roles.ROLE_ANY) // Allow any authenticated user.
  public Response postOpenUrl(
      @ApiParam(value = "The OpenURL query parameters.", required = true)
      OpenUrlParams params, @Context SecurityContext securityContext)
	  throws ApiException {
    return delegate.postOpenUrl(params, securityContext);
  }
}
