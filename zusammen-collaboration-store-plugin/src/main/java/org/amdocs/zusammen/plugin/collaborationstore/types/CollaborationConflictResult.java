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

package org.amdocs.zusammen.plugin.collaborationstore.types;

import org.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollaborationConflictResult {

  private Map<String, List<String>> conflictingEntityFilesMap = new HashMap<>();
  private ItemVersionConflictFiles itemVersionConflictFiles;


  public void addElementFile(String elementId, String elementPath) {


    List<String> files = conflictingEntityFilesMap.get(elementId);
    if (files == null) {
      files = new ArrayList<>();
      conflictingEntityFilesMap.put(elementId, files);
    }
    files.add(elementPath);
  }


  public List<String> getConflictingFilesByElementId(String elementId) {
    return conflictingEntityFilesMap.get(elementId);
  }

  public Set<String> getConflictingElementList() {
    return conflictingEntityFilesMap.keySet();
  }

  public void addItemVersionFile(String file) {
    if (itemVersionConflictFiles == null) {
      itemVersionConflictFiles = new ItemVersionConflictFiles();
    }
    if (PluginConstants.ITEM_VERSION_INFO_FILE_NAME.equals(file)) {
      this.itemVersionConflictFiles.setItemVersionInfo(file);
    } else if (PluginConstants.RELATIONS_FILE_NAME.equals(file)) {
      this.itemVersionConflictFiles.setItemVersionRelations(file);
    }
  }

  public ItemVersionConflictFiles getItemVersionConflictFiles() {
    return itemVersionConflictFiles;
  }
}
