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
package org.lockss.laaws.mdq.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.PathSegment;
import org.lockss.rs.auth.AccessControlFilter;
//import org.lockss.servlet.LockssServlet;

/**
 * Finer-grained role authorization filter for this service.
 */
public class Authorizer extends AccessControlFilter {

  /**
   * Provides the names of the roles permissible for the user to be able to
   * execute operations of this web service.
   * 
   * @return a Set<String> with the permissible roles.
   */
  @Override
  protected Set<String> getPermissibleRoles(String method,
      List<PathSegment> pathSegments) {

    Set<String> permissibleRoles = new HashSet<String>();

//    if ("GET".equals(method)) {
//      if (pathSegments.size() == 2
//	  && "au".equals(pathSegments.get(0).getPath().toLowerCase())) {
//	permissibleRoles.add(LockssServlet.ROLE_CONTENT_ACCESS);
//      } else if (pathSegments.size() == 3
//	  && "url".equals(pathSegments.get(0).getPath().toLowerCase())
//	  && "doi".equals(pathSegments.get(1).getPath().toLowerCase())) {
//	permissibleRoles.add(LockssServlet.ROLE_CONTENT_ACCESS);
//      } else if (pathSegments.size() == 2
//	  && "url".equals(pathSegments.get(0).getPath().toLowerCase())
//	  && "openurl".equals(pathSegments.get(1).getPath().toLowerCase())) {
//	permissibleRoles.add(LockssServlet.ROLE_CONTENT_ACCESS);
//      }
//    }

    return permissibleRoles;
  }
}
