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

import io.swagger.annotations.ApiParam;
import java.security.AccessControlException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.lockss.app.LockssDaemon;
import org.lockss.laaws.mdq.model.AuMetadataPageInfo;
import org.lockss.laaws.mdq.model.ItemMetadata;
import org.lockss.laaws.mdq.model.PageInfo;
import org.lockss.laaws.mdq.security.SpringAuthenticationFilter;
import org.lockss.metadata.MetadataManager;
import org.lockss.rs.auth.Roles;
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
public class MetadataApiController implements MetadataApi {
  private static final Logger logger =
      LoggerFactory.getLogger(MetadataApiController.class);

  @Autowired
  private HttpServletRequest request;

  /**
   * Deletes the metadata stored for an AU given the AU identifier.
   * 
   * @param auid
   *          A String with the AU identifier.
   * @return a ResponseEntity<Integer> with the count of metadata items deleted.
   */
  @Override
  @RequestMapping(value = "/metadata/aus/{auid}",
  produces = { "application/json" }, consumes = { "application/json" },
  method = RequestMethod.DELETE)
  public ResponseEntity<Integer> deleteMetadataAusAuid(
      @PathVariable("auid") String auid) {
    if (logger.isDebugEnabled()) logger.debug("auid = " + auid);

    SpringAuthenticationFilter.checkAuthorization(Roles.ROLE_CONTENT_ADMIN);

    try {
      Integer count = getMetadataManager().deleteAuMetadataItems(auid);
      if (logger.isDebugEnabled()) logger.debug("count = " + count);

      return new ResponseEntity<Integer>(count, HttpStatus.OK);
    } catch (IllegalArgumentException iae) {
      String message = "No Archival Unit found for auid '" + auid + "'";
      logger.error(message);
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
   * @return a ResponseEntity<AuMetadataPageInfo> with the metadata.
   */
  @Override
  @RequestMapping(value = "/metadata/aus/{auid}",
  produces = { "application/json" }, consumes = { "application/json" },
  method = RequestMethod.GET)
  public ResponseEntity<AuMetadataPageInfo> getMetadataAusAuid(
      @PathVariable("auid") String auid,
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
	  getMetadataManager().getAuMetadataDetail(auid, page, limit);
      if (logger.isDebugEnabled()) logger.debug("items = " + items);

      result.setItems(items);
      if (logger.isDebugEnabled()) logger.debug("result = " + result);

      return new ResponseEntity<AuMetadataPageInfo>(result, HttpStatus.OK);
    } catch (IllegalArgumentException iae) {
      String message = "No Archival Unit found for auid '" + auid + "'";
      logger.error(message);
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
   * @return a ResponseEntity<Long> with the identifier of the stored metadata.
   */
  @Override
  @RequestMapping(value = "/metadata/aus",
  produces = { "application/json" }, consumes = { "application/json" },
  method = RequestMethod.POST)
  public ResponseEntity<Long> postMetadataAusItem(
      @ApiParam(required=true) @RequestBody ItemMetadata item) {
    if (logger.isDebugEnabled()) logger.debug("item = " + item);

    SpringAuthenticationFilter.checkAuthorization(Roles.ROLE_CONTENT_ADMIN);

    try {
      Long mdItemSeq = getMetadataManager().storeAuItemMetadata(item);
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

  /**
   * Provides the metadata manager.
   * 
   * @return a MetadataManager with the metadata manager.
   */
  private MetadataManager getMetadataManager() {
    return LockssDaemon.getLockssDaemon().getMetadataManager();
  }
}
