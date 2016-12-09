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
package org.lockss.laaws.mdq.api.impl;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.apache.log4j.Logger;
import org.lockss.app.LockssApp;
//import org.lockss.app.LockssDaemon;
import org.lockss.laaws.mdq.api.AusApiService;
import org.lockss.laaws.mdq.api.ApiException;
import org.lockss.laaws.mdq.api.NotFoundException;
import org.lockss.laaws.mdq.model.ItemMetadata;
import org.lockss.laaws.mdq.model.AuMetadataPageInfo;
import org.lockss.laaws.mdq.model.PageInfo;
import org.lockss.metadata.ItemMetadataDetail;
import org.lockss.metadata.MetadataManager;

/**
 * Implementation of the base provider of access to the metadata of an AU.
 */
public class AusApiServiceImpl extends AusApiService {
  private static Logger log = Logger.getLogger(AusApiServiceImpl.class);

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
  public Response getAuAuid(String auid, Integer page, Integer limit,
      HttpServletRequest request, SecurityContext securityContext)
	  throws NotFoundException, ApiException {
    if (log.isDebugEnabled()) {
      log.debug("auid = " + auid);
      log.debug("page = " + page);
      log.debug("limit = " + limit);
    }

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

    try {
//      List<ItemMetadataDetail> itemDetails = LockssDaemon.getLockssDaemon()
//	  .getMetadataManager().getAuMetadataDetail(auid, page, limit);
      List<ItemMetadataDetail> itemDetails = ((MetadataManager)LockssApp
	  .getManager(MetadataManager.getManagerKey()))
	  .getAuMetadataDetail(auid, page, limit);
      if (log.isDebugEnabled()) {
	log.debug("itemDetails.size() = " + itemDetails.size());
	log.debug("itemDetails = " + itemDetails);
      }

      List<ItemMetadata> items = new ArrayList<ItemMetadata>();

      for (ItemMetadataDetail itemDetail : itemDetails) {
	items.add(new ItemMetadata(itemDetail));
      }

      if (log.isDebugEnabled()) log.debug("items = " + items);
      result.setItems(items);
    } catch (IllegalArgumentException iae) {
      String message = "No Archival Unit found for auid = '" + auid + "'";
      log.error(message);
      throw new NotFoundException(1, message);
    } catch (Exception e) {
      String message = "Cannot getAuAuid() for auid = '" + auid + "'";
      log.error(message, e);
      throw new NotFoundException(1, message + ": " + e.getMessage());
    }

    if (log.isDebugEnabled()) log.debug("result = " + result);

    return Response.ok().entity(result).build();
  }
}