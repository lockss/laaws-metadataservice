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
package org.lockss.laaws.mdq.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The information related to a resulting list of URLs.
 **/
@ApiModel(description = "The information related to a resulting list of URLs.")
public class UrlInfo {
  private Map<String, String> params = new HashMap<String, String>();
  private List<String> urls = new ArrayList<String>();

  /**
   * Default constructor.
   */
  public UrlInfo() {

  }

  /**
   * Full constructor.
   * 
   * @param params
   *          A Map<String, String> with the query parameters that define the
   *          resulting URLs.
   * @param urls
   *          A List<String> with the URLs.
   */
  public UrlInfo(Map<String, String> params, List<String> urls) {
    this.params = params;
    this.urls = urls;
  }

  /**
   * The parameters that define the resulting URLs.
   **/
  @ApiModelProperty(required = true,
      value = "The parameters that define the resulting URLs.")
  public Map<String, String> getParams() {
    return params;
  }
  public void setParams(Map<String, String> params) {
    this.params = params;
  }

  /**
   * The URLs.
   **/
  @ApiModelProperty(required = true, value = "The URLs.")
  public List<String> getUrls() {
    return urls;
  }

  public void setUrls(List<String> urls) {
    this.urls = urls;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UrlInfo urlInfo = (UrlInfo) o;
    return Objects.equals(params, urlInfo.params) &&
	Objects.equals(this.urls, urlInfo.urls);
  }

  @Override
  public int hashCode() {
    return Objects.hash(params, urls);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UrlInfo {\n");
    
    sb.append("    params: ").append(toIndentedString(params)).append("\n");
    sb.append("    urls: ").append(toIndentedString(urls)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
