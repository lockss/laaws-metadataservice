/*

 Copyright (c) 2017 Board of Trustees of Leland Stanford Jr. University,
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

import io.swagger.annotations.*;
import org.lockss.laaws.mdq.model.AuMetadataPageInfo;
import org.lockss.laaws.mdq.model.ItemMetadata;
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
public interface MetadataApi {

  /**
   * Deletes the metadata stored for an AU given the AU identifier.
   * 
   * @param auid
   *          A String with the AU identifier.
   * @return a ResponseEntity<Integer> with the count of metadata items deleted.
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
   * @return a ResponseEntity<AuMetadataPageInfo> with the metadata.
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
   * @return a ResponseEntity<Long> with the identifier of the stored metadata.
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
