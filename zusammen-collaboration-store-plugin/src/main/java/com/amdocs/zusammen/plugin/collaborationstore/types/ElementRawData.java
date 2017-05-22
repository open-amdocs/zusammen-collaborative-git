/*
 * Copyright © 2016-2017 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amdocs.zusammen.plugin.collaborationstore.types;

public class ElementRawData {
  private byte[] info;
  private byte[] relations;
  private byte[] data;
  private byte[] visualization;
  private byte[] searchableData;

  public byte[] getInfo() {
    return info;
  }

  public void setInfo(byte[] info) {
    this.info = info;
  }

  public byte[] getRelations() {
    return relations;
  }

  public void setRelations(byte[] relations) {
    this.relations = relations;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public byte[] getVisualization() {
    return visualization;
  }

  public void setVisualization(byte[] visualization) {
    this.visualization = visualization;
  }

  public byte[] getSearchableData() {
    return searchableData;
  }

  public void setSearchableData(byte[] searchableData) {
    this.searchableData = searchableData;
  }
}
