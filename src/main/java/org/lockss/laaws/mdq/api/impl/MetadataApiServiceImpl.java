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
package org.lockss.laaws.mdq.api.impl;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.apache.log4j.Logger;
import org.lockss.app.LockssDaemon;
import org.lockss.laaws.mdq.api.ApiException;
import org.lockss.laaws.mdq.api.MetadataApiService;
import org.lockss.laaws.mdq.api.NotFoundException;
import org.lockss.laaws.mdq.model.ItemMetadata;
import org.lockss.laaws.mdq.model.AuMetadataPageInfo;
import org.lockss.laaws.mdq.model.PageInfo;
import org.lockss.metadata.MetadataManager;

/**
 * Implementation of the base provider of access to the metadata of an AU.
 */
public class MetadataApiServiceImpl extends MetadataApiService {
  private static Logger log = Logger.getLogger(MetadataApiServiceImpl.class);

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
  @Override
  public Response deleteMetadataAusAuid(String auid,
      SecurityContext securityContext) throws NotFoundException {
    if (log.isDebugEnabled()) log.debug("auid = " + auid);

    try {
      Integer count = getMetadataManager().deleteAuMetadataItems(auid);
      if (log.isDebugEnabled()) log.debug("count = " + count);

      return Response.ok().entity(count).build();
    } catch (IllegalArgumentException iae) {
      String message = "No Archival Unit found for auid = '" + auid + "'";
      log.error(message);
      return getErrorResponse(Response.Status.NOT_FOUND, message);
    } catch (Exception e) {
      String message =
	  "Cannot deleteMetadataAusAuid() for auid = '" + auid + "'";
      log.error(message, e);
      return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, message);
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
  @Override
  public Response getMetadataAusAuid(String auid, Integer page, Integer limit,
      HttpServletRequest request, SecurityContext securityContext)
	  throws NotFoundException, ApiException {
    if (log.isDebugEnabled()) {
      log.debug("auid = " + auid);
      log.debug("page = " + page);
      log.debug("limit = " + limit);
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

      if (log.isDebugEnabled()) {
	log.debug("curLink = " + curLink);
	log.debug("nextLink = " + nextLink);
      }

      pi.setCurLink(curLink);
      pi.setNextLink(nextLink);
      pi.setCurrentPage(page);
      pi.setResultsPerPage(limit);

      AuMetadataPageInfo result = new AuMetadataPageInfo();
      result.setPageInfo(pi);

      List<ItemMetadata> items =
	  getMetadataManager().getAuMetadataDetail(auid, page, limit);
      if (log.isDebugEnabled()) log.debug("items = " + items);

      result.setItems(items);
      if (log.isDebugEnabled()) log.debug("result = " + result);

      return Response.ok().entity(result).build();
    } catch (IllegalArgumentException iae) {
      String message = "No Archival Unit found for auid = '" + auid + "'";
      log.error(message);
      return getErrorResponse(Response.Status.NOT_FOUND, message);
    } catch (Exception e) {
      String message = "Cannot getMetadataAusAuid() for auid = '" + auid + "'";
      log.error(message, e);
      return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, message);
    }
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
  @Override
  public Response postMetadataAusItem(ItemMetadata item,
      SecurityContext securityContext) throws ApiException {
    if (log.isDebugEnabled()) log.debug("item = " + item);

    try {
      Long mdItemSeq = getMetadataManager().storeAuItemMetadata(item);
      if (log.isDebugEnabled()) log.debug("mdItemSeq = " + mdItemSeq);

      return Response.ok().entity(mdItemSeq).build();
    } catch (Exception e) {
      String message = "Cannot postMetadataAusItem() for item = '" + item + "'";
      log.error(message, e);
      return getErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, message);
    }
  }

  /**
   * Provides the metadata manager.
   * 
   * @return a MetadataManager with the metadata manager.
   */
  private MetadataManager getMetadataManager() {
    return LockssDaemon.getLockssDaemon().getMetadataManager();
  }

  /**
   * Provides the appropriate response in case of an error.
   * 
   * @param statusCode
   *          A Response.Status with the error status code.
   * @param message
   *          A String with the error message.
   * @return a Response with the error response.
   */
  private Response getErrorResponse(Response.Status status, String message) {
    return Response.status(status).entity(toJsonMessage(message)).build();
  }

  /**
   * Formats to JSON any message to be returned.
   * 
   * @param message
   *          A String with the message to be formatted.
   * @return a String with the JSON-formatted message.
   */
  private String toJsonMessage(String message) {
    return "{\"message\":\"" + message + "\"}"; 
  }
}
