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
import java.util.List;
import org.lockss.laaws.mdq.model.UrlInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Provider of access to URLs.
 */
@Api(value = "urls")
public interface UrlsApi {

  /**
   * Provides the URL for a DOI given the DOI.
   * 
   * @param doi
   *          A String with the DOI for which the URL is requested.
   * @return a ResponseEntity<UrlInfo> with the URL information.
   */
  @ApiOperation(value = "Gets the URL for a DOI",
      notes = "Provides the URL for a DOI given the DOI",
      response = UrlInfo.class,
      authorizations = {@Authorization(value = "basicAuth")}, tags={ "urls", })
  @ApiResponses(value = { 
      @ApiResponse(code = 200, message = "The URL for the specified DOI",
	  response = UrlInfo.class),
      @ApiResponse(code = 401, message = "Unauthorized request",
      response = UrlInfo.class),
      @ApiResponse(code = 500, message = "Internal server error",
      response = UrlInfo.class),
      @ApiResponse(code = 503,
      message = "Some or all of the system is not available",
      response = UrlInfo.class) })
  @RequestMapping(value = "/urls/doi/{doi}",
  produces = { "application/json" }, consumes = { "application/json" },
  method = RequestMethod.GET)
  default ResponseEntity<UrlInfo> getUrlsDoi(
      @ApiParam(value = "The DOI for which the URL is requested",
      required=true ) @PathVariable("doi") String doi) {
    return new ResponseEntity<UrlInfo>(HttpStatus.NOT_IMPLEMENTED);
  }

  /**
   * Provides the URL that results from performing an OpenURL query
   * 
   * @param params
   *          A List<String> with the OpenURL query parameters.
   * @return a ResponseEntity<UrlInfo> with the URL information.
   */
  @ApiOperation(value = "Performs an OpenURL query",
      notes = "Provides the URL that results from performing an OpenURL query. With query parameters inline",
      response = UrlInfo.class,
      authorizations = {@Authorization(value = "basicAuth")}, tags={ "urls", })
  @ApiResponses(value = { 
      @ApiResponse(code = 200,
	  message = "The data related to the performed OpenURL query",
	  response = UrlInfo.class),
      @ApiResponse(code = 401, message = "Unauthorized request",
      response = UrlInfo.class),
      @ApiResponse(code = 500, message = "Internal server error",
      response = UrlInfo.class),
      @ApiResponse(code = 503,
      message = "Some or all of the system is not available",
      response = UrlInfo.class) })
  @RequestMapping(value = "/urls/openurl",
  produces = { "application/json" }, consumes = { "application/json" },
  method = RequestMethod.GET)
  default ResponseEntity<UrlInfo> getUrlsOpenUrl(
      @ApiParam(value = "The OpenURL parameters", required = true)
      @RequestParam("params") List<String> params) {
    return new ResponseEntity<UrlInfo>(HttpStatus.NOT_IMPLEMENTED);
  }
}
