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

package com.amdocs.zusammen.plugin.collaborationstore.dao.util;

import com.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants;
import com.google.gson.reflect.TypeToken;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.Namespace;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.Relation;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import com.amdocs.zusammen.utils.fileutils.json.JsonUtil;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class ElementUtil {

  private static final String EMPTY_FILE = "";


  public CollaborationElement initCollaborationElement(ElementContext elementContext,
                                                       String elementPath,
                                                       Id elementId) {
    Namespace namespace = getNamespaceFromElementPath(elementPath, elementId.getValue());

    CollaborationElement element = createCollaborationElement(elementContext, namespace,
        elementId);
    element.setParentId(namespace.getParentElementId());
    return element;
  }

  protected CollaborationElement createCollaborationElement(ElementContext elementContext, Namespace
      namespace,
                                                            Id elementId) {

    return new CollaborationElement(elementContext.getItemId(), elementContext.getVersionId(),
        namespace,
        elementId);


  }

  public CollaborationElement uploadCollaborationElement(ElementContext elementContext, String
      repositoryPath, String elementPath,
                                                         Id elementId) {
    CollaborationElement element = initCollaborationElement(elementContext, elementPath,
        elementId);
    populateElementContent(element, repositoryPath + File.separator + elementPath);
    return element;
  }

  private void populateElementContent(CollaborationElement element, String elementPath) {
    if(!element.getId().getValue().equals(Id.ZERO)) { // if not root
      getFileContentAsInputStream(elementPath, PluginConstants.INFO_FILE_NAME).map(fileContent ->
          JsonUtil.json2Object(fileContent, Info.class))
          .ifPresent(element::setInfo);

      getFileContentAsInputStream(elementPath, PluginConstants.RELATIONS_FILE_NAME)
          .map(fileContent ->
              (ArrayList<Relation>) JsonUtil
                  .json2Object(fileContent, new TypeToken<ArrayList<Relation>>() {
                  }.getType()))
          .ifPresent(element::setRelations);


      consumeFileContentAsInputStream(elementPath, PluginConstants.VISUALIZATION_FILE_NAME,
          element::setVisualization);
      consumeFileContentAsInputStream(elementPath, PluginConstants.DATA_FILE_NAME,
          element::setData);
      consumeFileContentAsInputStream(elementPath, PluginConstants.SEARCH_DATA_FILE_NAME,
          element::setSearchableData);
    }
    element.setSubElements(
        getSubElementIds(elementPath).stream().map(Id::new).collect(Collectors.toSet()));
  }

  public ItemVersion uploadItemVersionData(Id itemId, Id versionId, String repositoryPath) {

    return uploadItemVersionData(repositoryPath);
  }

  public ItemVersion uploadItemVersionData(String repositoryPath) {
    ItemVersion itemVersion = new ItemVersion();

    ItemVersionData itemVersionData = new ItemVersionData();
    itemVersionData.setInfo(getFileContentAsInputStream(repositoryPath,
        PluginConstants.ITEM_VERSION_INFO_FILE_NAME)
        .map(fileContent -> JsonUtil.json2Object(fileContent, Info.class))
        .orElse(null));

    itemVersionData.setRelations(getFileContentAsInputStream(repositoryPath,
        PluginConstants.RELATIONS_FILE_NAME)
        .map(fileContent -> (ArrayList<Relation>) JsonUtil
            .json2Object(fileContent, new TypeToken<ArrayList<Relation>>() {
            }.getType())).orElse(null));

    Map<String, String> itemVersionInformation =
        getFileContentAsInputStream(repositoryPath,
            PluginConstants.ZUSAMMEN_TAGGING_FILE_NAME)
            .map(inputStream -> JsonUtil.json2Object(inputStream, Map.class
            )).orElse(new HashMap<String, String>());
    itemVersion.setBaseId(new Id(itemVersionInformation.get(PluginConstants.ITEM_VERSION_BASE_ID)));
    itemVersion.setId(new Id(itemVersionInformation.get(PluginConstants.ITEM_VERSION_ID)));
    itemVersion.setData(itemVersionData);
    return itemVersion;
  }

  protected String getRepositoryPath(Git git) {
    return git.getRepository().getWorkTree().getPath();
  }

  private Namespace getNamespaceFromElementPath(String elementPath, String elementId) {
    String namespaceValue = elementPath
        .replace(elementId, "")
        .replace(File.separator, Namespace.NAMESPACE_DELIMITER);
    if (namespaceValue.startsWith(Namespace.NAMESPACE_DELIMITER)) {
      namespaceValue = namespaceValue.substring(1);
    }
    if (namespaceValue.endsWith(Namespace.NAMESPACE_DELIMITER)) {
      namespaceValue = namespaceValue.substring(0, namespaceValue.length() - 1);
    }

    Namespace namespace = new Namespace();
    namespace.setValue(namespaceValue);
    return namespace;
  }


  public Collection<String> updateCollaborationElement(String basePath, String relativePath,
                                                       CollaborationElement element,
                                                       Action action) {
    ArrayList<String> files = new ArrayList<>();
    if (action.equals(Action.CREATE)) {
      if (addFileContent(basePath, relativePath,
          PluginConstants.ZUSAMMEN_TAGGING_FILE_NAME, EMPTY_FILE)) {
        files.add(relativePath + File.separator + PluginConstants.ZUSAMMEN_TAGGING_FILE_NAME);
      }
    }

    if (element.getId().getValue().equals(Id.ZERO.getValue())) {
      updateItemVersionDataFromCollaborationElement(basePath, element);
    }

    if (addFileContent(basePath, relativePath, PluginConstants.INFO_FILE_NAME, element.getInfo())) {
      files.add(relativePath + File.separator + PluginConstants.INFO_FILE_NAME);
    }
    if (element.getRelations() != null && element.getRelations().size() > 0) {
      if (addFileContent(basePath, relativePath, PluginConstants.RELATIONS_FILE_NAME,
          element.getRelations())) {
        files.add(relativePath + File.separator + PluginConstants.RELATIONS_FILE_NAME);
      }
    }
    if (addFileContent(basePath, relativePath, PluginConstants.VISUALIZATION_FILE_NAME,
        element.getVisualization())) {
      files.add(relativePath + File.separator + PluginConstants.VISUALIZATION_FILE_NAME);
    }
    if (addFileContent(basePath, relativePath, PluginConstants.DATA_FILE_NAME, element.getData())) {
      files.add(relativePath + File.separator + PluginConstants.DATA_FILE_NAME);
    }
    if (addFileContent(basePath, relativePath, PluginConstants.SEARCH_DATA_FILE_NAME,
        element.getSearchableData())) {
      files.add(relativePath + File.separator + PluginConstants.SEARCH_DATA_FILE_NAME);
    }
    return files;
  }


  private Collection<String> updateItemVersionDataFromCollaborationElement(String basePath,
                                                             CollaborationElement element) {
    List<String> files = new ArrayList<>();
    Info info = element.getInfo();
    if (info != null) {
      if(addFileContent(basePath, null, PluginConstants.ITEM_VERSION_INFO_FILE_NAME, info))
        files.add(PluginConstants.ITEM_VERSION_INFO_FILE_NAME);
    }
    return files;
  }

  public boolean addFileContent(String basePath, String relativePath, String fileName,
                                Object fileContent) {
    if (fileContent == null) {
      return false;
    }
    relativePath = relativePath == null ? "" : relativePath;
    if (fileContent instanceof InputStream) {
      FileUtils.writeFileFromInputStream(basePath + File.separator + relativePath, fileName,
          (InputStream) fileContent);
    } else {
      FileUtils.writeFile(basePath + File.separator + relativePath, fileName, fileContent);
    }
    return true;
  }

  private void consumeFileContentAsInputStream(String filePath, String fileName,
                                               Consumer<InputStream> inputStreamConsumer) {
    getFileContentAsInputStream(filePath, fileName).ifPresent(inputStream -> {
      inputStreamConsumer.accept(inputStream);
      try {
        inputStream.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  Optional<byte[]> getFileContentAsByteArray(String filePath, String fileName) {
    return getFileContentAsInputStream(filePath, fileName).map(inputStream -> {
      byte[] bytes = FileUtils.toByteArray(inputStream);
      try {
        inputStream.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return bytes;
    });
  }

  private Optional<InputStream> getFileContentAsInputStream(String path, String fileName) {
    return FileUtils.readFile(path, fileName);
  }

  List<String> getSubElementIds(String path) {
    List<String> elementIds = new ArrayList<>();
    File file = new File(path);
    File[] files = file.listFiles();
    if (files == null || files.length == 0) {
      return elementIds;
    }
    for (File subfile : files) {
      if (subfile.isDirectory() && !subfile.isHidden()) {
        elementIds.add(subfile.getName());
      }
    }
    return elementIds;
  }

  public CollaborationElement uploadElement(String itemId, String versionId, String elementId,
                                            String
                                                rootPath, String elementPath) {
    CollaborationElement element = initCollaborationElement(itemId, versionId,
        elementPath, elementId);
    populateElementContent(element, rootPath + File.separator + elementPath);
    return element;
  }

  private CollaborationElement initCollaborationElement(String itemId, String versionId,
                                                        String elementPath,
                                                        String elementId) {
    return new CollaborationElement(new Id(itemId), new Id(versionId), getNamespaceFromElementPath
        (elementPath, elementId), new Id(elementId));

  }
}
