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

import io.swagger.annotations.*;
import org.lockss.laaws.mdq.model.AuMetadataPageInfo;
import org.lockss.laaws.mdq.model.ItemMetadata;
import org.lockss.rs.status.SpringLockssBaseApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Provider of access to the metadata of an AU.
 */
@Api(value = "metadata")
public interface MetadataApi extends SpringLockssBaseApi {

  /**
   * Deletes the metadata stored for an AU given the AU identifier.
   * 
   * @param auid
   *          A String with the AU identifier.
   * @return a {@code ResponseEntity<Integer>} with the count of metadata items
   *         deleted.
   */
  @ApiOperation(value = "Delete the metadata stored for an AU",
      notes = "Delete the metadata stored for an AU given the AU identifier",
      response = Integer.class,
      authorizations = {@Authorization(value = "basicAuth")},
      tags={ "metadata", })
  @ApiResponses(value = { 
      @ApiResponse(code = 200,
	  message = "The number of deleted AU metadata items",
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
  @RequestMapping(value = "/metadata/aus/{auid}",
  produces = { "application/json" }, consumes = { "application/json" },
  method = RequestMethod.DELETE)
  default ResponseEntity<Integer> deleteMetadataAusAuid(
      @ApiParam(value =
      "The identifier of the AU for which the metadata is to be deleted",
      required=true ) @PathVariable("auid") String auid) {
    return new ResponseEntity<Integer>(HttpStatus.NOT_IMPLEMENTED);
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
   * @return a {@code ResponseEntity<AuMetadataPageInfo>} with the metadata.
   */
  @ApiOperation(value = "Get the metadata stored for an AU", notes =
      "Get the full metadata stored for an AU given the AU identifier or a pageful of the metadata defined by the page index and size",
      response = AuMetadataPageInfo.class,
      authorizations = {@Authorization(value = "basicAuth")},
      tags={ "metadata", })
  @ApiResponses(value = { 
      @ApiResponse(code = 200, message = "The metadata of the specified AU",
	  response = AuMetadataPageInfo.class),
      @ApiResponse(code = 401, message = "Unauthorized request",
      response = AuMetadataPageInfo.class),
      @ApiResponse(code = 404, message = "AU not found",
      response = AuMetadataPageInfo.class),
      @ApiResponse(code = 500, message = "Internal server error",
      response = AuMetadataPageInfo.class),
      @ApiResponse(code = 503,
      message = "Some or all of the system is not available",
      response = AuMetadataPageInfo.class) })
  @RequestMapping(value = "/metadata/aus/{auid}",
  produces = { "application/json" }, consumes = { "application/json" },
  method = RequestMethod.GET)
  default ResponseEntity<AuMetadataPageInfo> getMetadataAusAuid(
      @ApiParam(value =
      "The identifier of the AU for which the metadata is requested",
      required=true ) @PathVariable("auid") String auid,
      @ApiParam(value = "The identifier of the page of metadata to be returned",
      defaultValue = "1") @RequestParam(value = "page", required = false,
      defaultValue="1") Integer page,
      @ApiParam(value = "The number of items per page", defaultValue = "50")
      @RequestParam(value = "limit", required = false, defaultValue="50")
      Integer limit) {
    return new ResponseEntity<AuMetadataPageInfo>(HttpStatus.NOT_IMPLEMENTED);
  }

  /**
   * Stores the metadata for an item belonging to an AU.
   * 
   * @param item
   *          An ItemMetadata with the AU item metadata.
   * @return a {@code ResponseEntity<Long>} with the identifier of the stored
   *         metadata.
   */
  @ApiOperation(value = "Store the metadata for an AU item",
      notes = "Store the metadata for an item belonging to an AU",
      response = Long.class,
      authorizations = {@Authorization(value = "basicAuth")},
      tags={ "metadata", })
  @ApiResponses(value = { 
      @ApiResponse(code = 200, message =
	  "The key under which the metadata of the AU item has been stored",
	  response = Long.class),
      @ApiResponse(code = 401, message = "Unauthorized request",
      response = Long.class),
      @ApiResponse(code = 403, message = "Forbidden request",
      response = Long.class),
      @ApiResponse(code = 500, message = "Internal server error",
      response = Long.class),
      @ApiResponse(code = 503,
      message = "Some or all of the system is not available",
      response = Long.class) })
  @RequestMapping(value = "/metadata/aus",
  produces = { "application/json" }, consumes = { "application/json" },
  method = RequestMethod.POST)
  default ResponseEntity<Long> postMetadataAusItem(
      @ApiParam(value = "The metadata of the AU item to be stored",
      required=true ) @RequestBody ItemMetadata item) {
    return new ResponseEntity<Long>(HttpStatus.NOT_IMPLEMENTED);
  }
}
