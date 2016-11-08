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
import javax.annotation.security.PermitAll;
//import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.lockss.laaws.mdq.api.factories.AuApiServiceFactory;
//import org.lockss.rs.auth.Roles;

/**
 * Provider of access to the metadata of an AU.
 */
@Path("/au")
@Api(value = "/au")
public class AuApi  {
  private final AuApiService delegate = AuApiServiceFactory.getAuApi();

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
  // TODO: Fix authentication and authorization.
  //@RolesAllowed(Roles.ROLE_ANY) // Allow any authenticated user.
  @PermitAll
  public Response getAuAuid(
      @ApiParam(value =
      "The identifier of the AU for which the metadata is requested",
      required=true) @PathParam("auid") String auid,
      @ApiParam(value = "The identifier of the page of metadata to be returned",
      defaultValue="1") @DefaultValue("1") @QueryParam("page") Integer page,
      @ApiParam(value = "The number of items per page", defaultValue="50")
      @DefaultValue("50") @QueryParam("limit") Integer limit,
      @Context SecurityContext securityContext)
	  throws NotFoundException, ApiException {
    return delegate.getAuAuid(auid,page,limit,securityContext);
  }
}
