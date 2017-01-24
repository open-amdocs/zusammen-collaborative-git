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

package org.amdocs.zusammen.plugin.collaborationstore.git.dao.util;

import com.google.gson.reflect.TypeToken;
import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.Namespace;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.item.Info;
import org.amdocs.zusammen.datatypes.item.Relation;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.SourceControlDaoFactory;
import org.amdocs.zusammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.zusammen.sdk.types.ElementData;
import org.amdocs.zusammen.utils.fileutils.FileUtils;
import org.amdocs.zusammen.utils.fileutils.json.JsonUtil;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class ElementDataUtil {

  public ElementData uploadElementData(Git git, String elementPath, String elementId) {
    ElementData elementData;
    Namespace namespace = getNamespaceFromElementPath(elementPath, elementId);
    try {
      elementData = new ElementData(new Id((new File(getRepositoryPath(git))).getName()), new Id(git
          .getRepository().getBranch()), namespace, new Id(elementId));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    elementData.setParentId(getParentId(namespace));
    populateElementContent(elementData, getRepositoryPath(git) + elementPath);
    return elementData;
  }

  private void populateElementContent(ElementData elementData, String elementPath) {
    getFileContent(elementPath, PluginConstants.INFO_FILE_NAME)
        .map(fileContent -> JsonUtil.json2Object(fileContent, Info.class))
        .ifPresent(elementData::setInfo);
    getFileContent(elementPath, PluginConstants.RELATIONS_FILE_NAME)
        .map(fileContent ->
            (ArrayList<Relation>) JsonUtil
                .json2Object(fileContent, new TypeToken<ArrayList<Relation>>() {
                }.getType()))
        .ifPresent(elementData::setRelations);
    getFileContent(elementPath, PluginConstants.VISUALIZATION_FILE_NAME)
        .ifPresent(elementData::setVisualization);
    getFileContent(elementPath, PluginConstants.DATA_FILE_NAME)
        .ifPresent(elementData::setData);
    getFileContent(elementPath, PluginConstants.SEARCH_DATA_FILE_NAME)
        .ifPresent(elementData::setSearchableData);
    elementData.setSubElements(
        getSubElementIds(elementPath).stream().map(Id::new).collect(Collectors.toSet()));
  }

  public Info uploadItemVersionInfo(Git git) {
    return getFileContent(getRepositoryPath(git), PluginConstants.ITEM_VERSION_INFO_FILE_NAME)
        .map(fileContent -> JsonUtil.json2Object(fileContent, Info.class))
        .orElse(null);
  }

  private String getRepositoryPath(Git git) {
    return git.getRepository().getWorkTree().getPath();
  }

  private Namespace getNamespaceFromElementPath(String elementPath, String elementId) {
    Namespace namespace = new Namespace();
    namespace.setValue(elementPath.replace(File.separator + elementId, "")
        .replace(File.separator, Namespace.NAMESPACE_DELIMITER));
    return namespace;
  }

  private Id getParentId(Namespace namespace) {

   if(Namespace.ROOT_NAMESPACE.equals(namespace)) return null;

   int fromIndex = namespace.getValue().contains(Namespace.NAMESPACE_DELIMITER)?namespace
       .getValue().lastIndexOf(Namespace.NAMESPACE_DELIMITER):0;
   int toIndex = namespace.getValue().length();

   return new Id(namespace.getValue()
       .substring(fromIndex,toIndex));
  }

  public void updateElementData(SessionContext context, Git git, String basePath, String
      relativePath, ElementData elementData) {

    if (elementData.getId().getValue().equals(Id.ZERO.getValue())) {
      updateItemVersionDataFromElementData(context, git, basePath, elementData);
    }

    if (elementData.getVisualization() != null) {
      addFileContent(context, git, getRepositoryPath(git), relativePath,
          PluginConstants.VISUALIZATION_FILE_NAME, elementData.getVisualization());
    }

    if (elementData.getData() != null) {
      addFileContent(context, git,
          basePath, relativePath, PluginConstants.DATA_FILE_NAME, elementData.getData());
    }

    if (elementData.getSearchableData() != null) {
      addFileContent(context, git,
          basePath, relativePath, PluginConstants.SEARCH_DATA_FILE_NAME,
          elementData.getSearchableData());
    }

    Info info = elementData.getInfo();
    if (info != null) {
      addFileContent(context, git,
          basePath, relativePath, PluginConstants.INFO_FILE_NAME, info);
    }
  }

  private void updateItemVersionDataFromElementData(SessionContext context, Git git,
                                                    String basePath, ElementData elementData) {
    Info info = elementData.getInfo();
    if (info != null) {
      addFileContent(context, git,
          basePath, null, PluginConstants.ITEM_VERSION_INFO_FILE_NAME, info);
    }
  }

  public void addFileContent(SessionContext context, Git git, String basePath, String
      relativePath, String fileName,
                             Object fileContent) {
    relativePath = relativePath == null ? "" : relativePath;
    if (fileContent == null) {
      return;
    }
    if (fileContent instanceof InputStream) {
      FileUtils
          .writeFileFromInputStream(basePath + File.separator + relativePath, fileName,
              (InputStream)
                  fileContent);
    } else {
      FileUtils.writeFile(basePath + File.separator + relativePath, fileName, fileContent);
    }
    String fileToAdd = getFileRelativePath(relativePath, fileName);
    getSourceControlDao(context).add(context, git, fileToAdd);
  }

  private String getFileRelativePath(String relativePath, String fileName) {
    if (relativePath == null || "".equals(relativePath)) {
      return fileName;
    } else {
      relativePath = relativePath.startsWith(File.separator) ? relativePath.substring(1)
          : relativePath;
      return relativePath + File.separator + fileName;
    }
  }

  protected Optional<InputStream> getFileContent(String path, String fileName) {
    return FileUtils.readFile(path, fileName);
  }

  protected List<String> getSubElementIds(String path) {
    List<String> elementIds = new ArrayList<>();
    File file = new File(path);
    File[] files = file.listFiles();
    if (files == null || files.length == 0) {
      return elementIds;
    }
    for (File subfile : files) {
      if (subfile.isDirectory()) {
        elementIds.add(subfile.getName());
      }
    }
    return elementIds;
  }

  public GitSourceControlDao getSourceControlDao(SessionContext context) {
    return SourceControlDaoFactory.getInstance().createInterface(context);
  }
}
