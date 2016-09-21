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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.apache.log4j.Logger;
import org.lockss.app.LockssDaemon;
import org.lockss.laaws.mdq.api.*;
import org.lockss.laaws.mdq.api.NotFoundException;
import org.lockss.laaws.mdq.model.AuMetadata;
import org.lockss.metadata.AuMetadataDetail;

/**
 * Implementation of the base provider of access to the metadata of an AU.
 */
public class AuApiServiceImpl extends AuApiService {
  private static Logger log = Logger.getLogger(AuApiServiceImpl.class);

  /**
   * Provides the metadata stored for an AU given the AU identifier.
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
  public Response getAuAuid(String auid, SecurityContext securityContext)
      throws NotFoundException {
    if (log.isDebugEnabled()) log.debug("auid = " + auid);

    try {
      AuMetadataDetail auMetadataDetail = LockssDaemon.getLockssDaemon().
	  getMetadataManager().getAuMetadataDetail(auid);
      if (log.isDebugEnabled())
	log.debug("auMetadataDetail = " + auMetadataDetail);

      AuMetadata result = new AuMetadata(auMetadataDetail);
      if (log.isDebugEnabled()) log.debug("result = " + result);

      return Response.ok().entity(result).build();
    } catch (IllegalArgumentException iae) {
      String message = "No Archival Unit found for auid = '" + auid + "'";
      log.error(message);
      throw new NotFoundException(1, message);
    } catch (Exception e) {
      String message = "Cannot getAuAuid() for auid = '" + auid + "'";
      log.error(message, e);
      throw new NotFoundException(1, message + ": " + e.getMessage());
    }
  }
}
