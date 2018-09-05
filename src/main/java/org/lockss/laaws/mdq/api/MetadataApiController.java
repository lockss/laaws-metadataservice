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

import io.swagger.annotations.ApiParam;
import java.security.AccessControlException;
import java.util.ConcurrentModificationException;
import javax.servlet.http.HttpServletRequest;
import org.lockss.app.LockssApp;
import org.lockss.laaws.mdq.model.AuMetadataPageInfo;
import org.lockss.laaws.mdq.model.PageInfo;
import org.lockss.laaws.status.model.ApiStatus;
import org.lockss.metadata.ItemMetadata;
import org.lockss.metadata.ItemMetadataContinuationToken;
import org.lockss.metadata.ItemMetadataPage;
import org.lockss.metadata.extractor.MetadataExtractorManager;
import org.lockss.spring.auth.Roles;
import org.lockss.spring.auth.SpringAuthenticationFilter;
import org.lockss.spring.status.SpringLockssBaseApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for access to the metadata of an AU.
 */
@RestController
public class MetadataApiController extends SpringLockssBaseApiController
    implements MetadataApi {
  private static final Logger logger =
      LoggerFactory.getLogger(MetadataApiController.class);

  @Autowired
  private HttpServletRequest request;

  /**
   * Deletes from the database an Archival Unit given its identifier.
   * 
   * @param auid
   *          A String with the AU identifier.
   * @return a {@code ResponseEntity<Integer>} with the count of metadata items
   *         deleted.
   */
  @Override
  @RequestMapping(value = "/metadata/aus/{auid}",
  produces = { "application/json" },
  method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteMetadataAusAuid(
      @PathVariable("auid") String auid) {
    if (logger.isDebugEnabled()) logger.debug("auid = " + auid);

    // Check authorization.
    try {
      SpringAuthenticationFilter.checkAuthorization(Roles.ROLE_CONTENT_ADMIN);
    } catch (AccessControlException ace) {
      logger.warn(ace.getMessage());
      return new ResponseEntity<String>(ace.getMessage(), HttpStatus.FORBIDDEN);
    }

    try {
      Integer count = getMetadataExtractorManager().deleteAu(auid);
      if (logger.isDebugEnabled()) logger.debug("count = " + count);

      return new ResponseEntity<Integer>(count, HttpStatus.OK);
    } catch (IllegalArgumentException iae) {
      String message = "No Archival Unit found for auid '" + auid + "'";
      logger.warn(message, iae);
      return new ResponseEntity<String>(message, HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      String message = "Cannot deleteMetadataAusAuid() for auid '" + auid + "'";
      logger.warn(message, e);
      return new ResponseEntity<String>(message,
	  HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Provides the full metadata stored for an AU given the AU identifier or a
   * pageful of the metadata defined by the continuation token and size.
   * 
   * @param auid
   *          A String with the AU identifier.
   * @param limit
   *          An Integer with the maximum number of AU metadata items to be
   *          returned.
   * @param continuationToken
   *          A String with the continuation token of the next page of metadata
   *          to be returned.
   * @return a {@code ResponseEntity<AuMetadataPageInfo>} with the metadata.
   */
  @Override
  @RequestMapping(value = "/metadata/aus/{auid}",
  produces = { "application/json" },
  method = RequestMethod.GET)
  public ResponseEntity<?> getMetadataAusAuid(@PathVariable("auid") String auid,
      @RequestParam(value = "limit", required = false, defaultValue="50")
      Integer limit,
      @RequestParam(value = "continuationToken", required = false)
      String continuationToken) {
    if (logger.isDebugEnabled()) {
      logger.debug("auid = " + auid);
      logger.debug("limit = " + limit);
      logger.debug("continuationToken = " + continuationToken);
    }

    // Validation of requested page size.
    if (limit == null || limit.intValue() < 0) {
      String message =
	  "Limit of requested items must be a non-negative integer; it was '"
	      + limit + "'";
	logger.warn(message);
	return new ResponseEntity<String>(message, HttpStatus.BAD_REQUEST);
    }

    // Parse the request continuation token.
    ItemMetadataContinuationToken imct = null;

    try {
      imct = new ItemMetadataContinuationToken(continuationToken);
    } catch (IllegalArgumentException iae) {
      String message =
	  "Invalid continuation token '" + continuationToken + "'";
      logger.warn(message, iae);
      return new ResponseEntity<String>(message, HttpStatus.BAD_REQUEST);
    }

    try {
      // Get the pageful of results.
      ItemMetadataPage itemsPage = getMetadataExtractorManager()
	  .getAuMetadataDetail(auid, limit, imct);
      if (logger.isDebugEnabled()) logger.debug("itemsPage = " + itemsPage);

      AuMetadataPageInfo result = new AuMetadataPageInfo();
      PageInfo pi = new PageInfo();
      result.setPageInfo(pi);

      String curLink = request.getRequestURL().toString() + "?limit=" + limit
	  + "&continuationToken=" + continuationToken;
      if (logger.isDebugEnabled()) logger.debug("curLink = " + curLink);

      pi.setCurLink(curLink);
      pi.setResultsPerPage(itemsPage.getItems().size());

      // Check whether there is a response continuation token.
      if (itemsPage.getContinuationToken() != null) {
	// Yes.
	pi.setContinuationToken(itemsPage.getContinuationToken()
	    .toWebResponseContinuationToken());

	String nextLink = request.getRequestURL().toString() + "?limit=" + limit
	    + "&continuationToken=" + pi.getContinuationToken();
	if (logger.isDebugEnabled()) logger.debug("nextLink = " + nextLink);

	pi.setNextLink(nextLink);
      }

      result.setItems(itemsPage.getItems());
      if (logger.isDebugEnabled()) logger.debug("result = " + result);

      return new ResponseEntity<AuMetadataPageInfo>(result, HttpStatus.OK);
    } catch (ConcurrentModificationException cme) {
      String message =
	  "Pagination conflict for auid '" + auid + "': " + cme.getMessage();
      logger.warn(message, cme);
      return new ResponseEntity<String>(message, HttpStatus.CONFLICT);
    } catch (IllegalArgumentException iae) {
      String message = "No Archival Unit found for auid '" + auid + "'";
      logger.warn(message, iae);
      return new ResponseEntity<String>(message, HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      String message = "Cannot getMetadataAusAuid() for auid '" + auid + "'";
      logger.warn(message, e);
      return new ResponseEntity<String>(message,
	  HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Stores the metadata for an item belonging to an AU.
   * 
   * @param item
   *          An ItemMetadata with the AU item metadata.
   * @return a {@code ResponseEntity<Long>} with the identifier of the stored
   *         metadata.
   */
  @Override
  @RequestMapping(value = "/metadata/aus",
  produces = { "application/json" }, consumes = { "application/json" },
  method = RequestMethod.POST)
  public ResponseEntity<?> postMetadataAusItem(
      @ApiParam(required=true) @RequestBody ItemMetadata item) {
    if (logger.isDebugEnabled()) logger.debug("item = " + item);

    // Check authorization.
    try {
      SpringAuthenticationFilter.checkAuthorization(Roles.ROLE_CONTENT_ADMIN);
    } catch (AccessControlException ace) {
      logger.warn(ace.getMessage());
      return new ResponseEntity<String>(ace.getMessage(), HttpStatus.FORBIDDEN);
    }

    try {
      Long mdItemSeq = getMetadataExtractorManager().storeAuItemMetadata(item);
      if (logger.isDebugEnabled()) logger.debug("mdItemSeq = " + mdItemSeq);

      return new ResponseEntity<Long>(mdItemSeq, HttpStatus.OK);
    } catch (Exception e) {
      String message = "Cannot postMetadataAusItem() for item '" + item + "'";
      logger.warn(message, e);
      return new ResponseEntity<String>(message,
	  HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private static final String API_VERSION = "1.0.0";

  /**
   * Provides the status object.
   * 
   * @return an ApiStatus with the status.
   */
  @Override
  public ApiStatus getApiStatus() {
    return new ApiStatus()
      .setVersion(API_VERSION)
      .setReady(LockssApp.getLockssApp().isAppRunning());
  }

  /**
   * Provides the metadata extractor manager.
   * 
   * @return a MetadataExtractorManager with the metadata extractor manager.
   */
  private MetadataExtractorManager getMetadataExtractorManager() {
    return LockssApp.getManagerByTypeStatic(MetadataExtractorManager.class);
  }
}
