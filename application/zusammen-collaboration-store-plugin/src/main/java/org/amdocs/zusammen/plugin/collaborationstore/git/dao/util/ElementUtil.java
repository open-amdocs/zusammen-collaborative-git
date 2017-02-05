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
import org.amdocs.zusammen.datatypes.item.Action;
import org.amdocs.zusammen.datatypes.item.Info;
import org.amdocs.zusammen.datatypes.item.ItemVersion;
import org.amdocs.zusammen.datatypes.item.ItemVersionData;
import org.amdocs.zusammen.datatypes.item.Relation;
import org.amdocs.zusammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.amdocs.zusammen.utils.fileutils.FileUtils;
import org.amdocs.zusammen.utils.fileutils.json.JsonUtil;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class ElementUtil {

  private static final String EMPTY_FILE = "";


  public CollaborationElement initCollaborationElement(Git git, String elementPath,
                                                       String elementId) {
    Namespace namespace = getNamespaceFromElementPath(elementPath, elementId);

    CollaborationElement element = createCollaborationElement(git, namespace, elementId);
    element.setParentId(namespace.getParentElementId());
    return element;
  }

  protected CollaborationElement createCollaborationElement(Git git, Namespace namespace,
                                                            String elementId) {
    try {
      return new CollaborationElement(new Id((new File(getRepositoryPath(git)))
          .getName()), new Id(git
          .getRepository().getBranch()), namespace, new Id(elementId));

    } catch (IOException e) {
      throw new RuntimeException(e);

    }
  }

  public CollaborationElement uploadCollaborationElement(Git git, String elementPath,
                                                         String elementId) {
    CollaborationElement element = initCollaborationElement(git, elementPath, elementId);
    populateElementContent(element, getRepositoryPath(git) + File.separator + elementPath);
    return element;
  }

  private void populateElementContent(CollaborationElement element, String elementPath) {
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

    element.setSubElements(
        getSubElementIds(elementPath).stream().map(Id::new).collect(Collectors.toSet()));
  }

  public ItemVersion uploadItemVersionData(Git git) {
    ItemVersion itemVersion = new ItemVersion();

    ItemVersionData itemVersionData = new ItemVersionData();
    itemVersionData.setInfo(getFileContentAsInputStream(getRepositoryPath(git),
        PluginConstants.ITEM_VERSION_INFO_FILE_NAME)
        .map(fileContent -> JsonUtil.json2Object(fileContent, Info.class))
        .orElse(null));

    itemVersionData.setRelations(getFileContentAsInputStream(getRepositoryPath(git),
        PluginConstants.RELATIONS_FILE_NAME)
        .map(fileContent -> (ArrayList<Relation>) JsonUtil
            .json2Object(fileContent, new TypeToken<ArrayList<Relation>>() {
            }.getType())).orElse(null));

    Map<String, String> itemVersionInformation =
        getFileContentAsInputStream(getRepositoryPath(git),
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

  public void updateCollaborationElement(Git git, String basePath, String relativePath,
                                         CollaborationElement element, Action action) {
    if (action.equals(Action.CREATE)) {
      addFileContent(getRepositoryPath(git), relativePath,
          PluginConstants.ZUSAMMEN_TAGGING_FILE_NAME, EMPTY_FILE);
    }

    if (element.getId().getValue().equals(Id.ZERO.getValue())) {
      updateItemVersionDataFromCollaborationElement(basePath, element);
    }

    addFileContent(basePath, relativePath, PluginConstants.INFO_FILE_NAME, element.getInfo());
    if (element.getRelations() != null && element.getRelations().size() > 0) {
      addFileContent(basePath, relativePath, PluginConstants.RELATIONS_FILE_NAME,
          element.getRelations());
    }
    addFileContent(getRepositoryPath(git), relativePath, PluginConstants.VISUALIZATION_FILE_NAME,
        element.getVisualization());
    addFileContent(basePath, relativePath, PluginConstants.DATA_FILE_NAME, element.getData());
    addFileContent(basePath, relativePath, PluginConstants.SEARCH_DATA_FILE_NAME,
        element.getSearchableData());
  }

  private void updateItemVersionDataFromCollaborationElement(String basePath,
                                                             CollaborationElement element) {
    Info info = element.getInfo();
    if (info != null) {
      addFileContent(basePath, null, PluginConstants.ITEM_VERSION_INFO_FILE_NAME, info);
    }
  }

  public void addFileContent(String basePath, String relativePath, String fileName,
                             Object fileContent) {
    if (fileContent == null) {
      return;
    }
    relativePath = relativePath == null ? "" : relativePath;
    if (fileContent instanceof InputStream) {
      FileUtils.writeFileFromInputStream(basePath + File.separator + relativePath, fileName,
          (InputStream) fileContent);
    } else {
      FileUtils.writeFile(basePath + File.separator + relativePath, fileName, fileContent);
    }
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
}
