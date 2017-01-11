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

package org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.util;

import com.google.gson.reflect.TypeToken;
import org.amdocs.tsuzammen.datatypes.Id;
import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.datatypes.item.ElementInfo;
import org.amdocs.tsuzammen.datatypes.item.Info;
import org.amdocs.tsuzammen.datatypes.item.Relation;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.SourceControlDaoFactory;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.tsuzammen.sdk.types.ElementData;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;
import org.amdocs.tsuzammen.utils.fileutils.json.JsonUtil;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants.ITEM_VERSION_INFO_FILE_NAME;

public class ElementDataUtil {



  public ElementData uploadElementData(SessionContext context, Git git, String elementPath){
    ElementInfo elementInfo = uploadElementInfo(context,git,elementPath);
    Optional<InputStream> fileContent;
    fileContent = getFileContent(context, git,elementPath, PluginConstants.IMPL_FILE_NAME);
    ElementData elementData = null;
    try {
        elementData = new ElementData(new Id(git.getRepository().getDirectory().getName()),new Id(git
            .getRepository().getBranch()),null,Class.forName(new String(FileUtils.toByteArray
            (fileContent.get()))));
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    elementData.setInfo(elementInfo.getInfo());
    elementData.setRelations(elementInfo.getRelations());

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

    List<String> subElementIds = getSubElementIds(context,git,elementPath);
    String type;
    for (String subElementId:subElementIds) {
      type = new String(FileUtils.toByteArray(getFileContent(context, git, elementPath + File
          .separator + subElementId, PluginConstants.IMPL_FILE_NAME).get()));
      try {
        elementData.putSubElement(new Id(subElementId), Class.forName(type));
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return elementData;
  }


  public ElementInfo uploadElementInfo(SessionContext context, Git git, String elementPath) {
    ElementInfo elementInfo = new ElementInfo(new Id(extractElementIdFromElementPath(elementPath)));
    Optional<InputStream> fileContent;

    fileContent = getFileContent(context, git,elementPath, PluginConstants.RELATIONS_FILE_NAME);
    if (fileContent.isPresent()) {
      elementInfo.setRelations(JsonUtil.json2Object(fileContent.get(), new
          TypeToken<ArrayList<Relation>>() {}.getType()));
    }

    fileContent = getFileContent(context, git,elementPath, PluginConstants.INFO_FILE_NAME);
    if (fileContent.isPresent()) {
      elementInfo.setInfo(JsonUtil.json2Object(fileContent.get(), Info.class));
    }
    ElementInfo subElementInfo;
    List<String> elementIds = getSubElementIds(context,git,elementPath);
    for(String subElementId:elementIds){
      subElementInfo = uploadElementInfo(context,git,elementPath+File.separator+subElementId);
      elementInfo.addSubelement(subElementInfo);
    }
    return elementInfo;
  }

  private String extractElementIdFromElementPath(String elementPath) {

    return (new File(elementPath)).getName();
    /*String[] splitPath = elementPath.split(File.separator);
    return splitPath[splitPath.length-1];*/
  }


  public void updateElementData(SessionContext context, Git git, String elementPath,
                                     ElementData
      elementData) {

    if (elementData.getVisualization() != null) {
      addFileContent(context, git,
          elementPath, PluginConstants.VISUALIZATION_FILE_NAME, elementData.getVisualization());
    }

    if (elementData.getData() != null) {
      addFileContent(context, git,
          elementPath, PluginConstants.DATA_FILE_NAME, elementData.getData());
    }

    if (elementData.getSearchData() != null) {
      addFileContent(context, git,
          elementPath, PluginConstants.SEARCH_DATA_FILE_NAME, elementData.getSearchData());
    }

    Info info = elementData.getInfo();
    if (info != null ) {
      addFileContent(context, git,
          elementPath, PluginConstants.INFO_FILE_NAME, info);
    }
  }



  public void addFileContent(SessionContext context, Git git, String path, String fileName,
                              Object fileContent) {

    if (fileContent instanceof InputStream) {
      FileUtils
          .writeFileFromInputStream(path, ITEM_VERSION_INFO_FILE_NAME, (InputStream) fileContent);
    } else {
      FileUtils.writeFile(path, ITEM_VERSION_INFO_FILE_NAME, fileContent);
    }
    getSourceControlDao(context).add(context, git, path + File.separator + fileName);
  }

  protected Optional<InputStream> getFileContent(SessionContext context, Git git, String
      path, String fileName) {

     return  FileUtils.readFile(path , fileName);
  }

  protected List<String> getSubElementIds(SessionContext context,Git git,String path){

    List<String> elementIds = new ArrayList<>();
    File file = new File(path);
    File[] files = file.listFiles();
    for(File subfile:files){
      if(subfile.isDirectory()){
        elementIds.add(subfile.getName());
      }
    }
    return elementIds;
  }


  public GitSourceControlDao getSourceControlDao(SessionContext context) {
    return SourceControlDaoFactory.getInstance().createInterface(context);
  }

}
