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

package org.amdocs.zusammen.plugin.collaborationstore.dao.util;


import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.Namespace;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.item.Action;
import org.amdocs.zusammen.datatypes.item.ElementContext;
import org.amdocs.zusammen.datatypes.item.Info;
import org.amdocs.zusammen.datatypes.item.ItemVersion;
import org.amdocs.zusammen.datatypes.item.ItemVersionChange;
import org.amdocs.zusammen.datatypes.item.ItemVersionData;
import org.amdocs.zusammen.datatypes.item.ItemVersionDataConflict;
import org.amdocs.zusammen.datatypes.item.Relation;
import org.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDaoFactory;
import org.amdocs.zusammen.plugin.collaborationstore.dao.impl.git.GitConflictFileSplitter;
import org.amdocs.zusammen.plugin.collaborationstore.types.LocalRemoteDataConflict;
import org.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants;
import org.amdocs.zusammen.plugin.collaborationstore.types.CollaborationConflictResult;
import org.amdocs.zusammen.plugin.collaborationstore.types.CollaborationDiffResult;
import org.amdocs.zusammen.plugin.collaborationstore.types.ElementRawData;
import org.amdocs.zusammen.plugin.collaborationstore.types.FileInfoDiff;
import org.amdocs.zusammen.plugin.collaborationstore.types.ItemVersionConflictFiles;
import org.amdocs.zusammen.plugin.collaborationstore.types.ItemVersionRawData;
import org.amdocs.zusammen.plugin.collaborationstore.types.Repository;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationElementChange;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationElementConflict;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import org.amdocs.zusammen.utils.fileutils.FileUtils;
import org.amdocs.zusammen.utils.fileutils.json.JsonUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class SourceControlUtil {

  private final ElementUtil elementUtil = new ElementUtil();

  public String getPrivateRepositoryPath(SessionContext context, String path, Id itemId) {
    String tenant = context.getTenant() != null ? context.getTenant() : "";
    return (path + File.separator + "users" + File.separator + context
        .getUser().getUserName() + File.separator + itemId.getValue()).replace(PluginConstants
        .TENANT, tenant);
  }

  public String getPublicRepositoryPath(SessionContext context, String
      path, Id itemId) {
    String tenant = context.getTenant() != null ? context.getTenant() : "";
    return (path + File.separator + itemId.getValue()).replace(PluginConstants.TENANT, tenant);
  }

  public String getElementRelativePath(Namespace namespace, Id elementId) {
    return namespace.getValue().replace(Namespace.NAMESPACE_DELIMITER, File.separator)
        + File.separator + elementId.toString();
  }

 /* public CollaborationMergeConflict handleSyncResponse(SessionContext context, Git git, PullResult
      pullResult) {

    if (pullResult != null && !pullResult.isSuccessful()) {
      return handleMergeResponse(context, git, pullResult.getMergeResult());
    }
    return new CollaborationMergeConflict();
  }*/

  /*public CollaborationMergeConflict handleMergeResponse(SessionContext context, Git git, MergeResult
      mergeResult) {

    CollaborationMergeConflict result = new CollaborationMergeConflict();
    String elementId;
    Map<String, String> elementPathMap = new HashMap<>();
    CollaborationElement element;
    if (!isMergeSuccesses(mergeResult)) {
      for (String file : mergeResult.getConflicts().keySet()) {
        elementId = extractIdFromFilePath(file);
        if (!elementPathMap.containsKey(elementId)) {
          result.addElementConflict(handleElementConflict(git,
              extractElementPathFromFilePath(file), elementId));
        }
      }
    }
    return result;
  }
*/

  protected boolean isMergeSuccesses(MergeResult mergeResult) {
    return !(mergeResult != null && mergeResult.getConflicts() != null && mergeResult.getConflicts
        ().size() > 0);
  }

  /*protected CollaborationElementConflict handleElementConflict(Git git, String elementPath, String
      elementId) {
    CollaborationElement element =
        getCollaborationElementUtil().initCollaborationElement(git, elementPath, elementId);
    CollaborationElementConflict elementConflict = new CollaborationElementConflict();
    elementConflict.setLocalElement(new CollaborationElement(element.getItemId(), element
        .getVersionId(), element.getNamespace(), element.getId()));
    elementConflict.setRemoteElement(new CollaborationElement(element.getItemId(), element
        .getVersionId(), element.getNamespace(), element.getId()));

    String fullPath = getRepositoryFullPath(git, elementPath);


    getCollaborationElementUtil()
        .getFileContentAsByteArray(fullPath, PluginConstants.DATA_FILE_NAME)
        .ifPresent(fileContent -> consumeConflictContentAsInputStream(fileContent,
            elementConflict.getLocalElement()::setData,
            elementConflict.getRemoteElement()::setData));

    getCollaborationElementUtil()
        .getFileContentAsByteArray(fullPath, PluginConstants.SEARCH_DATA_FILE_NAME)
        .ifPresent(fileContent -> consumeConflictContentAsInputStream(fileContent,
            elementConflict.getLocalElement()::setSearchableData,
            elementConflict.getRemoteElement()::setSearchableData));

    getCollaborationElementUtil()
        .getFileContentAsByteArray(fullPath, PluginConstants.VISUALIZATION_FILE_NAME)
        .ifPresent(fileContent -> consumeConflictContentAsInputStream(fileContent,
            elementConflict.getLocalElement()::setVisualization,
            elementConflict.getRemoteElement()::setVisualization));

    getCollaborationElementUtil()
        .getFileContentAsByteArray(fullPath, PluginConstants.INFO_FILE_NAME)
        .ifPresent(fileContent -> consumeConflictContentAsObjects(fileContent, Info.class,
            elementConflict.getLocalElement()::setInfo,
            elementConflict.getRemoteElement()::setInfo));

    getCollaborationElementUtil()
        .getFileContentAsByteArray(fullPath, PluginConstants.RELATIONS_FILE_NAME)
        .ifPresent(fileContent -> consumeConflictContentAsObjects(fileContent,
            new TypeToken<ArrayList<Relation>>() {
            }.getType(),
            elementConflict.getLocalElement()::setRelations,
            elementConflict.getRemoteElement()::setRelations));

    return elementConflict;
  }*/

  protected String getRepositoryFullPath(Git git, String elementPath) {
    return git.getRepository().getWorkTree().getPath() + File.separator + elementPath;
  }

  protected <T> void consumeConflictContentAsObjects(byte[] conflictContent,
                                                     Type classOfContent,
                                                     Consumer<T> conflictLocalContentSetter,
                                                     Consumer<T> conflictRemoteContentSetter) {
    LocalRemoteDataConflict conflict = GitConflictFileSplitter.splitMergedFile(conflictContent);
    conflictLocalContentSetter.accept(
        JsonUtil.json2Object(new ByteArrayInputStream(conflict.getLocal()), classOfContent));
    conflictRemoteContentSetter.accept(
        JsonUtil.json2Object(new ByteArrayInputStream(conflict.getRemote()), classOfContent));
  }

  protected void consumeConflictContentAsInputStream(byte[] conflictContent,
                                                     Consumer<InputStream> conflictLocalContentSetter,
                                                     Consumer<InputStream> conflictRemoteContentSetter) {
    LocalRemoteDataConflict conflict = GitConflictFileSplitter.splitMergedFile(conflictContent);
    conflictLocalContentSetter.accept(new ByteArrayInputStream(conflict.getLocal()));
    conflictRemoteContentSetter.accept(new ByteArrayInputStream(conflict.getRemote()));
  }

  /*public CollaborationMergeChange handleMergeFileDiff(SessionContext context,
                                                      GitSourceControlDao dao, Git git,
                                                      ObjectId from, ObjectId to) {
    to = to != null ? to : dao.getHead(context, git);
    return calculateItemVersionChangedData(context, dao, git, from, to, null);
  }

  public CollaborationMergeChange calculateItemVersionChangedData(SessionContext context,
                                                                  GitSourceControlDao dao,
                                                                  Git git,
                                                                  ObjectId from,
                                                                  ObjectId to,
                                                                  TreeFilter treeFilter) {
    CollaborationMergeChange collaborationMergeChange = new CollaborationMergeChange();

    Collection<DiffEntry> diffs = dao.revisionDiff(context, git, from, to, treeFilter);
    if (diffs != null) {
      Map<String, CollaborationElementChange> elementMap = new HashMap<>();
      String elementId;
      ItemVersion itemVersion = null;
      for (DiffEntry diff : diffs) {
        elementId = extractIdFromFilePath(diff.getNewPath());
        uploadChangedData(git, collaborationMergeChange, elementMap, elementId, itemVersion,
            diff);

        if (diff.getNewPath().contains(PluginConstants.ZUSAMMEN_TAGGING_FILE_NAME)) {
          if (elementId == null) {
            collaborationMergeChange.getChangedVersion().setAction(getAction(diff
                .getChangeType()));
          } else {
            elementMap.get(elementId).setAction(getAction(diff.getChangeType()));
          }
        }
      }
      collaborationMergeChange.setChangedElements(elementMap.values());
    }
    return collaborationMergeChange;
  }*/


  public String extractIdFromFilePath(String path) {
    File file = new File(path);
    if (file.getParentFile() == null) {
      return null;
    }
    return file.getParentFile().getName();
  }

  public String extractElementPathFromFilePath(String path) {
    File file = new File(path);
    return file.getParentFile() != null ? file.getParentFile().getPath() : "";
  }

 /* public CollaborationPublishResult handleMergeFileDiff(SessionContext context,
                                                        GitSourceControlDao dao, Git git,
                                                        Collection<PushResult> pushResult) {
    CollaborationPublishResult collaborationPublishResult;

    ObjectId from = getOldRevisionId(pushResult);
    ObjectId to = getNewRevisionId(pushResult);

    if (from != null) {
      collaborationPublishResult = getChangeData(context, dao, git, from, to);
    } else {
      collaborationPublishResult = getRepoData(context, dao, git);
    }
    return collaborationPublishResult;
  }*/

  /*protected CollaborationPublishResult getChangeData(SessionContext context,
                                                     GitSourceControlDao dao, Git git,
                                                     ObjectId from, ObjectId to) {

    CollaborationPublishResult result = new CollaborationPublishResult();
    CollaborationMergeChange mergeChange =
        calculateItemVersionChangedData(context, dao, git,
            from, to, null);
    result.setChange(mergeChange);
    return result;
  }*/


  /*public CollaborationPublishResult getRepoData(SessionContext context,
                                                GitSourceControlDao dao, Git git) {
    CollaborationPublishResult collaborationPublishResult = new CollaborationPublishResult();
    CollaborationMergeChange mergeChange = new CollaborationMergeChange();

    Collection<CollaborationElementChange> changedElementInfoCollection = new ArrayList<>();
    CollaborationElementChange elementChange;
    CollaborationElement element;
    String elementId;
    String elementPath;
    Set<String> elementSet = new HashSet<>();
    Collection<String> files = dao.getBranchFileList(context, git);

    for (String file : files) {

      elementId = extractIdFromFilePath(file);
      elementPath = extractElementPathFromFilePath(file);
      if (elementId == null) {
        ItemVersion itemVersion = getCollaborationElementUtil().uploadItemVersionData(git);
        ItemVersionChange itemVersionChange = new ItemVersionChange();
        itemVersionChange.setItemVersion(itemVersion);
        mergeChange.setChangedVersion(itemVersionChange);
        mergeChange.getChangedVersion().setAction(Action.CREATE);

      } else if (!elementSet.contains(elementId)) {
        elementSet.add(elementId);
        elementChange = new CollaborationElementChange();
        elementChange.setAction(Action.CREATE);
        element =
            getCollaborationElementUtil().uploadCollaborationElement(git, elementPath, elementId);
        elementChange.setElement(element);
        changedElementInfoCollection.add(elementChange);
      }
    }
    mergeChange.setChangedElements(changedElementInfoCollection);

    collaborationPublishResult.setChange(mergeChange);
    return collaborationPublishResult;


  }
*/
  protected ObjectId getNewRevisionId(Collection<PushResult> pushResults) {

    PushResult pushResult;
    if (pushResults.iterator().hasNext()) {
      pushResult = pushResults.iterator().next();
    } else {
      return null;
    }
    Collection<RemoteRefUpdate> remoteUpdates =
        pushResult.getRemoteUpdates();
    if (remoteUpdates.iterator().hasNext()) {
      ObjectId id = remoteUpdates.iterator().next().getNewObjectId();
      if (id == null || id.equals(ObjectId.zeroId())) {
        return null;
      }

      return id;
    } else {
      return null;
    }
  }

  protected ObjectId getOldRevisionId(Collection<PushResult> pushResults) {
    PushResult pushResult;
    if (pushResults.iterator().hasNext()) {
      pushResult = pushResults.iterator().next();
    } else {
      return null;
    }
    Collection<RemoteRefUpdate> remoteUpdates =
        pushResult.getRemoteUpdates();
    if (remoteUpdates.iterator().hasNext()) {
      ObjectId id = remoteUpdates.iterator().next().getExpectedOldObjectId();
      if (id == null || id.equals(ObjectId.zeroId())) {
        return null;
      }
      return id;
    } else {
      return null;
    }
  }

  protected Action getAction(DiffEntry.ChangeType changeType) {
    switch (changeType) {
      case ADD:
        return Action.CREATE;
      case DELETE:
        return Action.DELETE;
      case MODIFY:
        return Action.UPDATE;
    }
    throw new RuntimeException("Action[" + changeType + "] not supported");
  }

  protected Action getResetAction(DiffEntry.ChangeType changeType) {
    switch (changeType) {
      case ADD:
        return Action.DELETE;
      case DELETE:
        return Action.CREATE;
      case MODIFY:
        return Action.UPDATE;
    }
    throw new RuntimeException("Action[" + changeType + "] not supported");
  }

  protected ElementUtil getCollaborationElementUtil() {
    return this.elementUtil;
  }

  public void calculateItemVersionChangedDataAction(
      CollaborationMergeChange changes) {

    Collection<CollaborationElementChange> elements = changes.getChangedElements();
    for (CollaborationElementChange element : elements) {
      if (Action.CREATE == element.getAction()) {
        element.setAction(Action.DELETE);
      } else if (Action.DELETE == element.getAction()) {
        element.setAction(Action.CREATE);
      }
    }
  }

  public CollaborationMergeChange handleResetResponse(SessionContext context, Id itemId, Id
      versionId, Collection<DiffEntry> resetResult) {
    CollaborationMergeChange collaborationMergeChange = new CollaborationMergeChange();
    if (resetResult != null && resetResult.size() > 0) {
      Map<String, CollaborationElementChange> elementMap = new HashMap<>();
      String elementId;
      ItemVersion itemVersion = null;

      for (DiffEntry diff : resetResult) {
        elementId = extractIdFromFilePath(diff.getNewPath());
        uploadChangedData(context, itemId, versionId, collaborationMergeChange, elementMap,
            elementId,
            itemVersion,
            diff);

        if (diff.getNewPath().contains(PluginConstants.ZUSAMMEN_TAGGING_FILE_NAME)) {
          if (elementId == null) {
            collaborationMergeChange.getChangedVersion().setAction(getResetAction(diff
                .getChangeType()));
          } else {
            elementMap.get(elementId).setAction(getResetAction(diff.getChangeType()));
          }
        }
      }
      collaborationMergeChange.setChangedElements(elementMap.values());
    }


    return null;
  }

  private void uploadChangedData(SessionContext context, Id itemId, Id versionId,
                                 CollaborationMergeChange
                                     collaborationMergeChange,
                                 Map<String, CollaborationElementChange> elementMap,
                                 String elementId, ItemVersion itemVersion, DiffEntry diff) {
    ItemVersionChange itemVersionChange;
    CollaborationElement element;
    CollaborationElementChange elementChange;
    if (elementId == null && itemVersion == null) {
      itemVersion = getCollaborationElementUtil().uploadItemVersionData(itemId, versionId,
          getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH, itemId));
      itemVersionChange = new ItemVersionChange();
      itemVersionChange.setItemVersion(itemVersion);
      itemVersionChange.setAction(Action.UPDATE);
      collaborationMergeChange.setChangedVersion(itemVersionChange);
    } else if (elementId != null && !elementMap.containsKey(elementId)) {
      String elementPath = extractElementPathFromFilePath(diff.getNewPath());
      element =
          getCollaborationElementUtil().uploadCollaborationElement(new ElementContext(itemId,
                  versionId),
              getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH, itemId), elementPath,
              new Id(elementId));
      elementChange = new CollaborationElementChange();
      elementChange.setAction(Action.UPDATE);
      elementChange.setElement(element);
      elementMap.put(elementId, elementChange);
    }

  }

  public CollaborationMergeChange loadFileDiffElements(SessionContext context,
                                                       String repositoryLocation, Id itemId,
                                                       Id versionId,
                                                       CollaborationDiffResult collaborationDiffResult) {
    CollaborationMergeChange change = new CollaborationMergeChange();
    Collection<FileInfoDiff> diffs =
        collaborationDiffResult.getFileInfoDiffs();
    Map<String, CollaborationElementChange> elementMap = new HashMap<>();
    String elementPath;
    String elementId;
    CollaborationElementChange element;

    for (FileInfoDiff diff : diffs) {
      elementPath = extractElementPathFromFilePath(diff.getFilePath());
      elementId = extractIdFromFilePath(diff.getFilePath());
      if (elementId != null && !elementId.equals("")) {
        if (!elementMap.containsKey(elementId)) {
          element = uploadElementData(itemId.getValue(), versionId.getValue(), elementId,
              repositoryLocation,
              elementPath, Action.UPDATE);
          change.getChangedElements().add(element);
          elementMap.put(elementId, element);
        }
      } else {
        change.setChangedVersion(uploadItemVersion(repositoryLocation));
      }

      if (diff.getFilePath().contains(PluginConstants.ZUSAMMEN_TAGGING_FILE_NAME)) {
        if (elementId == null) {
          change.getChangedVersion().setAction(diff.getAction());
        } else {
          elementMap.get(elementId).setAction(diff.getAction());
        }
      }
    }


    return change;
  }

  private ItemVersionChange uploadItemVersion(String itemVersionPath) {
    ItemVersion itemVersion = getCollaborationElementUtil().uploadItemVersionData(itemVersionPath);
    ItemVersionChange itemVersionChange = new ItemVersionChange();
    itemVersionChange.setItemVersion(itemVersion);
    itemVersionChange.setAction(Action.UPDATE);

    return itemVersionChange;
  }

  private CollaborationElementChange uploadElementData(
      String itemId, String versionId, String elementId, String rootPath, String elementPath,
      Action action) {
    CollaborationElementChange elementChange = new CollaborationElementChange();
    elementChange.setElement(elementUtil.uploadElement(itemId, versionId, elementId, rootPath,
        elementPath));
    elementChange.setAction(action);
    return elementChange;
  }


  public ElementRawData uploadRawElementData(SessionContext context, String repositoryPath,
                                             String elementPathFromFilePath) {
    ElementRawData elementRawData = new ElementRawData();
    String elementFullPath = repositoryPath + File.separator + elementPathFromFilePath;
    Optional<byte[]> fileContentOptional =
        loadFileContent(elementFullPath, PluginConstants.DATA_FILE_NAME);
    fileContentOptional.ifPresent(elementRawData::setData);

    fileContentOptional = loadFileContent(elementFullPath, PluginConstants.INFO_FILE_NAME);
    fileContentOptional.ifPresent(elementRawData::setInfo);

    fileContentOptional = loadFileContent(elementFullPath, PluginConstants.RELATIONS_FILE_NAME);
    fileContentOptional.ifPresent(elementRawData::setRelations);

    fileContentOptional = loadFileContent(elementFullPath, PluginConstants.VISUALIZATION_FILE_NAME);
    fileContentOptional.ifPresent(elementRawData::setVisualization);

    fileContentOptional = loadFileContent(elementFullPath, PluginConstants.SEARCH_DATA_FILE_NAME);
    fileContentOptional.ifPresent(elementRawData::setSearchableData);

    return elementRawData;
  }

  public ItemVersionRawData uploadRawItemVersionData(SessionContext context, String repositoryPath
  ) {
    ItemVersionRawData itemVersionRawData = new ItemVersionRawData();

    Optional<byte[]> fileContentOptional =
        loadFileContent(repositoryPath, PluginConstants.ITEM_VERSION_INFO_FILE_NAME);
    fileContentOptional.ifPresent(itemVersionRawData::setInfo);


    fileContentOptional = loadFileContent(repositoryPath, PluginConstants.RELATIONS_FILE_NAME);
    fileContentOptional.ifPresent(itemVersionRawData::setRelations);

    return itemVersionRawData;
  }


  private Optional<byte[]> loadFileContent(String path, String name) {
    Optional<InputStream> fileContent = FileUtils.readFile(path, name);
    if (fileContent.isPresent()) {
      return Optional.of(FileUtils.toByteArray(fileContent.get()));
    } else {
      return Optional.empty();
    }


  }

  public Collection<CollaborationElementConflict>
  resolveSyncElementConflicts(SessionContext context,
                              Id itemId,
                              Id versionId,
                              Map<String, ElementRawData> elementRawDataMap,
                              CollaborationConflictResult collaborationConflictResult) {

    Collection<CollaborationElementConflict> collaborationElementConflictCollection = new
        ArrayList<>();
    elementRawDataMap.entrySet().stream().forEach(entry -> collaborationElementConflictCollection
        .add(resolveElementConflict(context, itemId, versionId, entry.getKey(), entry.getValue(),
            collaborationConflictResult
                .getConflictingFilesByElementId(entry.getKey()))));

    return collaborationElementConflictCollection;
  }

  public ItemVersionDataConflict
  resolveSyncItemVersionConflicts(SessionContext context,
                                  ItemVersionRawData itemVersionRawData,
                                  ItemVersionConflictFiles itemVersionConflictFiles) {


    ItemVersionDataConflict itemVersionDataConflict = new ItemVersionDataConflict();
    Pair<byte[], byte[]> remoteLocalFileContent;
    ItemVersionData localData = new ItemVersionData();
    ItemVersionData remoteData = new ItemVersionData();
    remoteLocalFileContent =
        getFileResolvedContent(context, itemVersionRawData.getInfo()
            , itemVersionConflictFiles.getItemVersionInfo() != null);
    localData.setInfo(JsonUtil.json2Object(FileUtils.toInputStream(remoteLocalFileContent
        .getValue()), Info.class));
    remoteData.setInfo(JsonUtil.json2Object(FileUtils.toInputStream(remoteLocalFileContent
        .getKey()), Info.class));

    remoteLocalFileContent =
        getFileResolvedContent(context, itemVersionRawData.getRelations()
            , itemVersionConflictFiles.getItemVersionInfo() != null);
    localData.setRelations(JsonUtil.json2Object(FileUtils.toInputStream(remoteLocalFileContent
        .getValue()), new TypeToken<ArrayList<Relation>>() {
    }.getType()));
    remoteData.setRelations(JsonUtil.json2Object(FileUtils.toInputStream(remoteLocalFileContent
        .getKey()), new TypeToken<ArrayList<Relation>>() {
    }.getType()));

    itemVersionDataConflict.setLocalData(localData);
    itemVersionDataConflict.setRemoteData(remoteData);

    return itemVersionDataConflict;
  }


  private CollaborationElementConflict resolveElementConflict(SessionContext context,
                                                              Id itemId,
                                                              Id versionId,
                                                              String elementId,
                                                              ElementRawData elementRawData,
                                                              List<String> conflictingFiles) {
    Pair<byte[], byte[]> remoteLocalFileContent;
    String elementPath = extractElementPathFromFilePath(conflictingFiles.get(0));
    CollaborationElementConflict collaborationElementConflict = new CollaborationElementConflict();

    CollaborationElement remoteElement = new CollaborationElement(itemId, versionId,
        getNamespaceFromElementPath(elementPath, elementId), new Id(elementId));

    CollaborationElement localElement = new CollaborationElement(itemId, versionId,
        getNamespaceFromElementPath(elementPath, elementId), new Id(elementId));

    remoteLocalFileContent = getFileResolvedContent(context, elementRawData
            .getData(),
        conflictingFiles.contains(elementPath + File.separator + PluginConstants.DATA_FILE_NAME));

    localElement.setData(FileUtils.toInputStream(remoteLocalFileContent.getValue()));
    if (remoteLocalFileContent.getKey() != null) {
      remoteElement.setData(FileUtils.toInputStream(remoteLocalFileContent.getKey()));
    }

    remoteLocalFileContent = getFileResolvedContent(context, elementRawData
            .getVisualization(),
        conflictingFiles.contains(elementPath + File.separator + PluginConstants
            .VISUALIZATION_FILE_NAME));
    localElement.setVisualization(FileUtils.toInputStream(remoteLocalFileContent.getValue()));
    if (remoteLocalFileContent.getKey() != null) {
      remoteElement.setVisualization(FileUtils.toInputStream(remoteLocalFileContent.getKey()));
    }


    remoteLocalFileContent = getFileResolvedContent(context, elementRawData
            .getSearchableData(),
        conflictingFiles.contains(elementPath + File.separator + PluginConstants
            .SEARCH_DATA_FILE_NAME));
    localElement.setSearchableData(FileUtils.toInputStream(remoteLocalFileContent.getValue()));
    remoteElement.setSearchableData(FileUtils.toInputStream(remoteLocalFileContent.getKey()));
    if (remoteLocalFileContent.getKey() != null) {
      remoteElement.setSearchableData(FileUtils.toInputStream(remoteLocalFileContent.getKey()));
    }


    remoteLocalFileContent = getFileResolvedContent(context, elementRawData
            .getSearchableData(),
        conflictingFiles.contains(elementPath + File.separator + PluginConstants
            .INFO_FILE_NAME));
    localElement.setInfo(JsonUtil.json2Object(FileUtils.toInputStream(remoteLocalFileContent
        .getValue()), Info.class));
    if (remoteLocalFileContent.getKey() != null) {
      remoteElement.setInfo(JsonUtil.json2Object(FileUtils.toInputStream(remoteLocalFileContent
          .getKey()), Info.class));
    }


    remoteLocalFileContent = getFileResolvedContent(context, elementRawData
        .getRelations(), conflictingFiles.contains(elementPath + File.separator + PluginConstants
        .RELATIONS_FILE_NAME));
    localElement.setRelations(JsonUtil.json2Object(FileUtils.toInputStream(remoteLocalFileContent
        .getValue()), new TypeToken<ArrayList<Relation>>() {
    }.getType()));
    if (remoteLocalFileContent.getKey() != null) {
      remoteElement.setRelations(JsonUtil.json2Object(FileUtils.toInputStream(remoteLocalFileContent
          .getKey()), new TypeToken<ArrayList<Relation>>() {
      }.getType()));
    }

    collaborationElementConflict.setLocalElement(localElement);
    collaborationElementConflict.setRemoteElement(remoteElement);
    return collaborationElementConflict;
  }

  private Pair<byte[], byte[]> getFileResolvedContent(SessionContext context, byte[] data,
                                                      boolean iSConflicting) {
    if (iSConflicting) {
      LocalRemoteDataConflict localRemoteDataConflict =
          SourceControlDaoFactory.getInstance().createInterface(context)
              .splitFileContentConflict(context, data);

      return new Pair<>(localRemoteDataConflict.getRemote(), localRemoteDataConflict.getLocal());
    } else {
      return new Pair<>(null, data);
    }

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

  public CollaborationDiffResult getRepoFiles(SessionContext context, Repository repository) {
    CollaborationDiffResult collaborationDiffResult = new CollaborationDiffResult(
        CollaborationDiffResult.MODE.NEW);
    Collection<String> files =
        SourceControlDaoFactory.getInstance().createInterface(context).getBranchFileList
            (context,
                repository);

    files.stream()
        .forEach(file -> collaborationDiffResult.add(new FileInfoDiff(file, Action.CREATE)));
    return collaborationDiffResult;
  }
}
