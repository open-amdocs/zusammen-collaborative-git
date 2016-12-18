/*
 * Copyright © 2016 Amdocs Software Systems Limited
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

package org.amdocs.tsuzammen.plugin.collaborationstore.git.utils;


import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.commons.datatypes.item.Content;
import org.amdocs.tsuzammen.commons.datatypes.item.Entity;
import org.amdocs.tsuzammen.commons.datatypes.item.Format;
import org.amdocs.tsuzammen.commons.datatypes.item.ItemVersion;
import org.amdocs.tsuzammen.utils.common.CommonMethods;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SourceControlFileStore {

  private SessionContext context;
  private String path;
  private List<File> filesToAdd;


  public SourceControlFileStore(SessionContext context, String path) {
    this.path = path;
    this.context = context;
  }


  public File[] getFilesToAdd() {
    return CommonMethods.toArray(this.filesToAdd, File.class);
  }

  public void store(ItemVersion itemVersion) {
    if (itemVersion.getInfo() != null) {

      filesToAdd
          .add(FileUtils.writeFile(path, PluginConstants.INFO_FILE_NAME, itemVersion.getInfo()));
    }

    if (itemVersion.getRelations() != null) {
      filesToAdd.add(
          FileUtils
              .writeFile(path, PluginConstants.RELATIONS_FILE_NAME, itemVersion.getRelations()));
      storeContentListToDisc(context, path, itemVersion.getContents());
    }
  }

  private void storeContentListToDisc(SessionContext context, String path, Map<String, Content>
      contents) {
    for (Map.Entry<String, Content> content : contents.entrySet()) {
      path += path + File.separator + content.getKey();
      storeContentToDisc(context, path, content.getValue());
    }
  }

  private void storeContentToDisc(SessionContext context, String path, Content content) {


    for (Entity entity : content.getEntities()) {
      path += path + File.separator + entity.getId();
      if (!SourceControlUtil.isEmpty(entity)) {
        storeEntityToDisc(context, path, entity.getId(), entity, content.getDataFormat()
        );
      }
    }

  }



  private void storeEntityToDisc(SessionContext context, String path, String entityId, Entity
      entity, Format dataFormat) {

    filesToAdd.add(FileUtils.writeFile(path, entityId, entity.getData()));
    if (entity.getInfo() != null) {
      filesToAdd.add(FileUtils.writeFile(path, PluginConstants.INFO_FILE_NAME, entity.getInfo()));
    }
    if (entity.getVisualization() != null) {
      filesToAdd.add(FileUtils
          .writeFile(path, PluginConstants.VISUALIZATION_FILE_NAME, entity.getVisualization()));
    }

    if (entity.getRelations() != null) {
      filesToAdd.add(
          FileUtils.writeFile(path, PluginConstants.RELATIONS_FILE_NAME, entity.getRelations()));
    }
    storeContentListToDisc(context, path, entity.getContents());

  }


}
