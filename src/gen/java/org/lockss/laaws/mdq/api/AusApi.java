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
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.lockss.laaws.mdq.api.factories.AusApiServiceFactory;
import org.lockss.laaws.mdq.model.AuMetadataPageInfo;
import org.lockss.laaws.mdq.model.ItemMetadata;
import org.lockss.rs.auth.Roles;

/**
 * Provider of access to the metadata of an AU.
 */
@Path("/aus")
@Produces({ "application/json" })
@Api(value = "/aus")
public class AusApi  {
  private final AusApiService delegate = AusApiServiceFactory.getAusApi();

  /**
   * Deletes the metadata stored for an AU given the AU identifier.
   * 
   * @param auid
   *          A String with the AU identifier.
   * @param securityContext
   *          A SecurityContext providing access to security related
   *          information.
   * @return a Response with any data that needs to be returned to the runtime.
   * @throws NotFoundException
   *           if the AU with the given identifier does not exist.
   */
  @DELETE
  @Path("/{auid}")
  @Produces({"application/json"})
  @ApiOperation(value = "Delete the metadata stored for an AU",
  notes = "Delete the metadata stored for an AU given the AU identifier",
  response = Integer.class,
  authorizations = {@Authorization(value = "basicAuth")}, tags={ "aus", })
  @ApiResponses(value = { 
      @ApiResponse(code = 200, message = "The number of metadata items deleted",
	  response = Integer.class),
      @ApiResponse(code = 401, message = "Unauthorized request",
      response = Integer.class),
      @ApiResponse(code = 403, message = "Forbidden request",
      response = Integer.class),
      @ApiResponse(code = 404, message = "AU not found",
      response = Integer.class),
      @ApiResponse(code = 500, message = "Internal server error",
      response = Integer.class),
      @ApiResponse(code = 503,
      message = "Some or all of the system is not available",
      response = Integer.class) })
  @RolesAllowed(Roles.ROLE_CONTENT_ADMIN) // Allow this role.
  public Response deleteAuAuid(
      @ApiParam(value =
      "The identifier of the AU for which the metadata is to be deleted",
      required=true) @PathParam("auid") String auid,
      @Context SecurityContext securityContext) throws NotFoundException {
    return delegate.deleteAuAuid(auid,securityContext);
  }

  /**
   * Provides the full metadata stored for an AU given the AU identifier or a
   * pageful of the metadata defined by the page index and size.
   * 
   * @param auid
   *          A String with the AU identifier.
   * @param page
   *          An Integer with the index of the page to be returned.
   * @param limit
   *          An Integer with the maximum number of AU metadata items to be
   *          returned.
   * @param request
   *          An HttpServletRequest providing access to the incoming request.
   * @param securityContext
   *          A SecurityContext providing access to security related
   *          information.
   * @return a Response with any data that needs to be returned to the runtime.
   * @throws NotFoundException
   *           if the AU with the given identifier does not exist.
   * @throws ApiException
   *           if there are other problems.
   */
  @GET
  @Path("/{auid}")
  @Produces({ "application/json" })
  @ApiOperation(value = "Get the metadata stored for an AU", notes =
  "Get the full metadata stored for an AU given the AU identifier or a pageful of the metadata defined by the page index and size",
  response = AuMetadataPageInfo.class,
  authorizations = {@Authorization(value = "basicAuth")}, tags={ "aus", })
  @ApiResponses(value = { 
      @ApiResponse(code = 200, message = "The metadata of the specified AU",
	  response = AuMetadataPageInfo.class),
      @ApiResponse(code = 404, message = "AU not found",
      response = AuMetadataPageInfo.class),
      @ApiResponse(code = 500, message = "Internal server error",
      response = AuMetadataPageInfo.class),
      @ApiResponse(code = 503,
      message = "Some or all of the system is not available",
      response = AuMetadataPageInfo.class) })
  @RolesAllowed(Roles.ROLE_ANY) // Allow any authenticated user.
  public Response getAuAuid(
      @ApiParam(value =
      "The identifier of the AU for which the metadata is requested",
      required=true) @PathParam("auid") String auid,
      @ApiParam(value = "The identifier of the page of metadata to be returned",
      defaultValue="1") @DefaultValue("1") @QueryParam("page") Integer page,
      @ApiParam(value = "The number of items per page", defaultValue="50")
      @DefaultValue("50") @QueryParam("limit") Integer limit,
      @Context HttpServletRequest request,
      @Context SecurityContext securityContext)
	  throws NotFoundException, ApiException {
    return delegate.getAuAuid(auid,page,limit,request,securityContext);
  }

  /**
   * Stores the metadata for an item belonging to an AU.
   * 
   * @param item
   *          An ItemMetadata with the AU item metadata.
   * @return a Response with any data that needs to be returned to the runtime.
   * @throws ApiException
   *           if there are problems.
   */
  @POST
  @Produces({ "application/json" })
  @ApiOperation(value = "Store the metadata for an AU item", notes =
  "Store the metadata for an item belonging to an AU",
  authorizations = {@Authorization(value = "basicAuth")}, tags={ "aus", })
  @ApiResponses(value = { 
      @ApiResponse(code = 200,
	  message = "The key under which the metadata of the AU item has been stored",
	  response = Long.class),
      @ApiResponse(code = 500, message = "Internal server error",
	  response = Long.class),
      @ApiResponse(code = 503,
	  message = "Some or all of the system is not available",
	  response = Long.class) })
  @RolesAllowed(Roles.ROLE_CONTENT_ADMIN) // Allow any authenticated user.
  public Response postAuItem(
      @ApiParam(value = "The metadata of the AU item to be stored",
      required=true) ItemMetadata item,
      @Context SecurityContext securityContext) throws ApiException {
    return delegate.postAuItem(item,securityContext);
  }
}
