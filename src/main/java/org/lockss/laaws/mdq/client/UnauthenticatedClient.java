/*

 Copyright (c) 2017-2019 Board of Trustees of Leland Stanford Jr. University,
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
package org.lockss.laaws.mdq.client;

import java.net.URI;
import java.util.Collections;
import org.lockss.laaws.mdq.model.AuMetadataPageInfo;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Client for an unauthenticated user.
 */
public class UnauthenticatedClient {

  /**
   * Entry point.
   * 
   * @param args A String[] with the command line arguments.
   * @throws Exception if there are errors.
   */
  public static void main(String[] args) throws Exception {
    for (int i = 0; i < args.length; i++) {
      System.out.println("args[" + i + "] = " + args[i]);
    }

    if (args.length < 1) {
      System.err.println("ERROR: Missing command line argument with the "
	  + "identifier of the Archival Unit for which its metadata is "
	  + "requested.");
    }

    String template = BaseClient.baseUri + "/metadata/aus/{auid}";

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template)
	.build().expand(Collections.singletonMap("auid", args[0]));

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    System.out.println("uri = " + uri);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<AuMetadataPageInfo> response =
	BaseClient.getRestTemplate().exchange(uri, HttpMethod.GET,
	    new HttpEntity<String>(null, headers), AuMetadataPageInfo.class);

    int status = response.getStatusCodeValue();
    System.out.println("status = " + status);
    AuMetadataPageInfo result = response.getBody();
    System.out.println("result = " + result);
  }
}
