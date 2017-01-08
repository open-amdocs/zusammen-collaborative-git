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

import com.google.gson.reflect.TypeToken;
import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.datatypes.item.Info;
import org.amdocs.tsuzammen.datatypes.item.Relation;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.SourceControlDaoFactory;
import org.amdocs.tsuzammen.sdk.types.ElementData;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;
import org.amdocs.tsuzammen.utils.fileutils.json.JsonUtil;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants.ITEM_VERSION_INFO_FILE_NAME;

public class ElementDataUtil {

  public static ElementDataUtil init(){
    return new ElementDataUtil();
  }

  public ElementData uploadElementData(SessionContext context, Git git, String elementPath){
    ElementData elementData = new ElementData();
    Optional<InputStream> fileContent;
    fileContent = getFileContent(context, git,elementPath, PluginConstants.IMPL_FILE_NAME);
    try {
      elementData.setElementImplClass(Class.forName(new String(FileUtils.toByteArray(fileContent.get())
      )));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    fileContent = getFileContent(context, git,elementPath, PluginConstants.RELATIONS_FILE_NAME);
    if (fileContent.isPresent()) {
      elementData.setRelations(JsonUtil.json2Object(fileContent.get(), new TypeToken<ArrayList<Relation>>() {}.getType()));
    }

    fileContent = getFileContent(context, git,elementPath, PluginConstants.VISUALIZATION_FILE_NAME);
    if (fileContent.isPresent()) {
      elementData.setVisualization(fileContent.get());
    }

    fileContent = getFileContent(context, git,elementPath, PluginConstants.DATA_FILE_NAME);
    if (fileContent.isPresent()) {
      elementData.setData(fileContent.get());
    }

    fileContent = getFileContent(context, git,elementPath, PluginConstants.SEARCH_DATA_FILE_NAME);
    if (fileContent.isPresent()) {
      elementData.setSearchData(fileContent.get());
    }

    fileContent = getFileContent(context, git,elementPath, PluginConstants.INFO_FILE_NAME);
    if (fileContent.isPresent()) {
      elementData.setInfo(JsonUtil.json2Object(fileContent.get(), Info.class));
    }

    return elementData;
  }



  public void updateElementData(SessionContext context, Git git, String elementPath,
                                     ElementData
      elementData) {
    addFileContent(context, git,
        elementPath, PluginConstants.IMPL_FILE_NAME, elementData.getElementImplClass().getName());
    if (elementData.getRelations() != null) {
      addFileContent(context, git,
          elementPath, PluginConstants.RELATIONS_FILE_NAME, elementData.getRelations());
    }
    if (elementData.getVisualization() != null) {
      addFileContent(context, git,
          elementPath, PluginConstants.VISUALIZATION_FILE_NAME, elementData.getVisualization());
    }

    if (elementData.getData() != null) {
      addFileContent(context, git,
          elementPath, PluginConstants.DATA_FILE_NAME, elementData.getData());
    }

    if (elementData.getData() != null) {
      addFileContent(context, git,
          elementPath, PluginConstants.SEARCH_DATA_FILE_NAME, elementData.getSearchData());
    }

    Info info = elementData.getInfo();
    if (info != null && info.getProperties() != null && !info.getProperties().isEmpty()) {
      addFileContent(context, git,
          elementPath, PluginConstants.INFO_FILE_NAME, info);
    }
  }


  public void addFileContent(SessionContext context, Git git, String path, String fileName,
                              Object
                                  fileContent) {

    if (fileContent instanceof InputStream) {
      FileUtils
          .writeFileFromInputStream(path, ITEM_VERSION_INFO_FILE_NAME, (InputStream) fileContent);
    } else {
      FileUtils.writeFile(path, ITEM_VERSION_INFO_FILE_NAME, fileContent);
    }
    getSourceControlDao(context).add(context, git, path + File.separator + fileName);
  }

  private Optional<InputStream> getFileContent(SessionContext context, Git git, String
      path, String
      fileName) {

     return  FileUtils.readFile(path , fileName);
  }

  public GitSourceControlDao getSourceControlDao(SessionContext context) {
    return SourceControlDaoFactory.getInstance().createInterface(context);
  }

}
