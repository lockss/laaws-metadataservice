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
   * @return a {@code ResponseEntity<UrlInfo>} with the URL information.
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
  produces = { "application/json" },
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
   *          A {@code List<String>} with the OpenURL query parameters.
   * @return a {@code ResponseEntity<UrlInfo>} with the URL information.
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
  produces = { "application/json" },
  method = RequestMethod.GET)
  default ResponseEntity<UrlInfo> getUrlsOpenUrl(
      @ApiParam(value = "The OpenURL parameters", required = true)
      @RequestParam("params") List<String> params) {
    return new ResponseEntity<UrlInfo>(HttpStatus.NOT_IMPLEMENTED);
  }
}
