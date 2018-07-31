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
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.lockss.app.LockssApp;
import org.lockss.laaws.mdq.model.AuMetadataPageInfo;
import org.lockss.laaws.mdq.model.ItemMetadata;
import org.lockss.laaws.mdq.model.PageInfo;
import org.lockss.metadata.extractor.MetadataExtractorManager;
import org.lockss.spring.auth.Roles;
import org.lockss.spring.auth.SpringAuthenticationFilter;
import org.lockss.laaws.status.model.ApiStatus;
import org.lockss.spring.status.SpringLockssBaseApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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
   * Deletes the metadata stored for an AU given the AU identifier.
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
    SpringAuthenticationFilter.checkAuthorization(Roles.ROLE_CONTENT_ADMIN);

    try {
      Integer count = getMetadataExtractorManager().deleteAuMetadataItems(auid);
      if (logger.isDebugEnabled()) logger.debug("count = " + count);

      return new ResponseEntity<Integer>(count, HttpStatus.OK);
    } catch (IllegalArgumentException iae) {
      String message = "No Archival Unit found for auid '" + auid + "'";
      logger.error(message, iae);
      throw new IllegalArgumentException(message);
    } catch (Exception e) {
      String message = "Cannot deleteMetadataAusAuid() for auid '" + auid + "'";
      logger.error(message, e);
      throw new RuntimeException(message);
    }
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
  @Override
  @RequestMapping(value = "/metadata/aus/{auid}",
  produces = { "application/json" },
  method = RequestMethod.GET)
  public ResponseEntity<?> getMetadataAusAuid(@PathVariable("auid") String auid,
      @RequestParam(value = "page", required = false, defaultValue="1")
      Integer page,
      @RequestParam(value = "limit", required = false, defaultValue="50")
      Integer limit) {
    if (logger.isDebugEnabled()) {
      logger.debug("auid = " + auid);
      logger.debug("page = " + page);
      logger.debug("limit = " + limit);
    }

    try {
      PageInfo pi = new PageInfo();

      String curLink = request.getRequestURL().toString();
      String nextLink = curLink;

      if (page != null) {
	curLink = curLink + "?page=" + page;
	nextLink = nextLink + "?page=" + (page + 1);

	if (limit != null) {
	  curLink = curLink + "&limit=" + limit;
	  nextLink = nextLink + "&limit=" + limit;
	}
      } else if (limit != null) {
	curLink = curLink + "?limit=" + limit;
	nextLink = nextLink + "?limit=" + limit;
      }

      if (logger.isDebugEnabled()) {
	logger.debug("curLink = " + curLink);
	logger.debug("nextLink = " + nextLink);
      }

      pi.setCurLink(curLink);
      pi.setNextLink(nextLink);
      pi.setCurrentPage(page);
      pi.setResultsPerPage(limit);

      AuMetadataPageInfo result = new AuMetadataPageInfo();
      result.setPageInfo(pi);

      List<ItemMetadata> items =
	  getMetadataExtractorManager().getAuMetadataDetail(auid, page, limit);
      if (logger.isDebugEnabled()) logger.debug("items = " + items);

      result.setItems(items);
      if (logger.isDebugEnabled()) logger.debug("result = " + result);

      return new ResponseEntity<AuMetadataPageInfo>(result, HttpStatus.OK);
    } catch (IllegalArgumentException iae) {
      String message = "No Archival Unit found for auid '" + auid + "'";
      logger.error(message, iae);
      throw new IllegalArgumentException(message);
    } catch (Exception e) {
      String message = "Cannot getMetadataAusAuid() for auid '" + auid + "'";
      logger.error(message, e);
      throw new RuntimeException(message);
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
    SpringAuthenticationFilter.checkAuthorization(Roles.ROLE_CONTENT_ADMIN);

    try {
      Long mdItemSeq = getMetadataExtractorManager().storeAuItemMetadata(item);
      if (logger.isDebugEnabled()) logger.debug("mdItemSeq = " + mdItemSeq);

      return new ResponseEntity<Long>(mdItemSeq, HttpStatus.OK);
    } catch (Exception e) {
      String message = "Cannot postMetadataAusItem() for item '" + item + "'";
      logger.error(message, e);
      throw new RuntimeException(message);
    }
  }

  @ExceptionHandler(AccessControlException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ErrorResponse authorizationExceptionHandler(AccessControlException e) {
    return new ErrorResponse(e.getMessage()); 	
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse notFoundExceptionHandler(IllegalArgumentException e) {
    return new ErrorResponse(e.getMessage()); 	
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse internalExceptionHandler(RuntimeException e) {
    return new ErrorResponse(e.getMessage()); 	
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
