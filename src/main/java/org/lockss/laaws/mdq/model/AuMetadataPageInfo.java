/*

 Copyright (c) 2016-2018 Board of Trustees of Leland Stanford Jr. University,
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.lockss.metadata.ItemMetadata;

/**
 * The display page of AU metadata.
 */
@ApiModel(description = "The display page of AU metadata")
public class AuMetadataPageInfo   {
  private List<ItemMetadata> items = new ArrayList<ItemMetadata>();
  private PageInfo pageInfo = null;

  /**
   * The metadata for the AU items in the page.
   * 
   * @return items
   **/
  @ApiModelProperty(required = true,
      value = "The metadata for the AU items in the page")
  public List<ItemMetadata> getItems() {
    return items;
  }

  public void setItems(List<ItemMetadata> items) {
    this.items = items;
  }

  /**
   * Get pageInfo
   * 
   * @return pageInfo
   **/
  @ApiModelProperty(required = true, value = "")
  public PageInfo getPageInfo() {
    return pageInfo;
  }

  public void setPageInfo(PageInfo pageInfo) {
    this.pageInfo = pageInfo;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuMetadataPageInfo auMetadataPageInfo = (AuMetadataPageInfo) o;
    return Objects.equals(this.items, auMetadataPageInfo.items) &&
        Objects.equals(this.pageInfo, auMetadataPageInfo.pageInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, pageInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuMetadataPageInfo {\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("    pageInfo: ").append(toIndentedString(pageInfo)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
