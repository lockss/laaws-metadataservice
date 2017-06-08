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
package org.lockss.laaws.mdq.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.lockss.laaws.mdq.api.factories.UrlsApiServiceFactory;
import org.lockss.laaws.mdq.model.UrlInfo;
import org.lockss.rs.auth.Roles;

@Path("/urls")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@Api(value = "/urls")
public class UrlsApi  {
  private final UrlsApiService delegate = UrlsApiServiceFactory.getUrlsApi();

  /**
   * Provides the URL for a DOI given the DOI.
   * 
   * @param doi
   *          A String with the DOI for which the URL is requested.
   * @param securityContext
   *          A SecurityContext providing access to security related
   *          information.
   * @return a Response with any data that needs to be returned to the runtime.
   * @throws ApiException
   *           if there is a problem obtaining the URL.
   */
  @GET
  @Path("/doi/{doi}")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  @ApiOperation(value = "Gets the URL for a DOI", notes =
  "Provides the URL for a DOI given the DOI",
  response = UrlInfo.class,
  authorizations = {@Authorization(value = "basicAuth")}, tags={ "urls", })
  @ApiResponses(value = { 
      @ApiResponse(code = 200, message = "The URL for the specified DOI",
	  response = UrlInfo.class) })
  @RolesAllowed(Roles.ROLE_ANY) // Allow any authenticated user.
  public Response getUrlsDoi(
      @ApiParam(value = "The DOI for which the URL is requested.",
      required = true) @PathParam("doi")
      String doi, @Context SecurityContext securityContext)
	  throws ApiException {
    return delegate.getUrlsDoi(doi, securityContext);
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
  @GET
  @Path("/openurl")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  @ApiOperation(value = "Performs an OpenURL query", notes =
  "Provides the URL that results from performing an OpenURL query. With query parameters inline",
  response = UrlInfo.class,
  authorizations = {@Authorization(value = "basicAuth")}, tags={ "urls", })
  @ApiResponses(value = { 
      @ApiResponse(code = 200,
	  message = "The data related to the performed OpenURL query",
	  response = UrlInfo.class) })
  @RolesAllowed(Roles.ROLE_ANY) // Allow any authenticated user.
  public Response getUrlsOpenUrl(
      @ApiParam(value = "The OpenURL query parameters.", required = true)
      @QueryParam("params") List<String> params,
      @Context SecurityContext securityContext) throws ApiException {
    return delegate.getUrlsOpenUrl(params, securityContext);
  }
}
