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
import org.amdocs.zusammen.datatypes.item.Action;
import org.amdocs.zusammen.datatypes.item.Info;
import org.amdocs.zusammen.datatypes.item.ItemVersion;
import org.amdocs.zusammen.datatypes.item.ItemVersionChange;
import org.amdocs.zusammen.datatypes.item.Relation;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.types.LocalRemoteDataConflict;
import org.amdocs.zusammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationElementChange;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationElementConflict;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeConflict;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationPublishResult;
import org.amdocs.zusammen.utils.fileutils.json.JsonUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class SourceControlUtil {


  private final ElementUtil elementUtil = new ElementUtil();

  public String getPrivateRepositoryPath(SessionContext context, String path, Id itemId) {
    StringBuffer sb = new StringBuffer();
    sb.append(path).append(File.separator).append("users").append(File.separator).append(context
        .getUser()
        .getUserName())
        .append(File
            .separator).append(itemId.toString());

    return sb.toString();
  }

  public String getPublicRepositoryPath(SessionContext context, String
      path, Id itemId) {
    StringBuffer sb = new StringBuffer();
    sb.append(path).append(File.separator).append(itemId.toString());

    return sb.toString();
  }

  public String getElementRelativePath(Namespace namespace, Id elementId) {
    return namespace.getValue().replace(Namespace.NAMESPACE_DELIMITER, File.separator)
        + File.separator + elementId.toString();
  }

  public CollaborationMergeConflict handleSyncResponse(SessionContext context, Git git, PullResult
      pullResult) {

    if (pullResult != null && !pullResult.isSuccessful()) {
      return handleMergeResponse(context, git, pullResult.getMergeResult());
    }
    return new CollaborationMergeConflict();
  }

  public CollaborationMergeConflict handleMergeResponse(SessionContext context, Git git, MergeResult
      mergeResult) {

    CollaborationMergeConflict result = new CollaborationMergeConflict();
    String elementId;
    //Map<String, CollaborationElement> elementMap = new HashMap<>();
    Map<String, String> elementPathMap = new HashMap<>();
    CollaborationElement element;
    if (!isMergeSuccesses(mergeResult)) {
      for (String file : mergeResult.getConflicts().keySet()) {
        elementId = extractElementIdFromFilePath(file);
        if (!elementPathMap.containsKey(elementId)) {
          result.addElementConflict(handleElementConflict(git,
              extractElementPathFromFilePath(file), elementId));
        }
      }
    }
    return result;
  }


  protected boolean isMergeSuccesses(MergeResult mergeResult) {
    return !(mergeResult != null && mergeResult.getConflicts() != null && mergeResult.getConflicts
        ().size() > 0);
  }

  protected CollaborationElementConflict handleElementConflict(Git git, String elementPath, String
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
  }

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

  public CollaborationMergeChange handleMergeFileDiff(SessionContext context,
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
        elementId = extractElementIdFromFilePath(diff.getNewPath());
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
  }


  protected String extractElementIdFromFilePath(String path) {
    File file = new File(path);
    if (file.getParentFile() == null) {
      return null;
    }
    return file.getParentFile().getName();
  }

  protected String extractElementPathFromFilePath(String path) {
    File file = new File(path);
    return file.getParentFile() != null ? file.getParentFile().getPath() : "";
  }

  public CollaborationPublishResult handleMergeFileDiff(SessionContext context,
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
  }

  protected CollaborationPublishResult getChangeData(SessionContext context,
                                                     GitSourceControlDao dao, Git git,
                                                     ObjectId from, ObjectId to) {

    CollaborationPublishResult result = new CollaborationPublishResult();
    CollaborationMergeChange mergeChange =
        calculateItemVersionChangedData(context, dao, git,
            from, to, null);
    result.setChange(mergeChange);
    return result;
  }


  public CollaborationPublishResult getRepoData(SessionContext context,
                                                GitSourceControlDao dao, Git git) {
    CollaborationPublishResult collaborationPublishResult = new CollaborationPublishResult();
    CollaborationMergeChange mergeChange = new CollaborationMergeChange();

    Collection<CollaborationElementChange> changedElementInfoCollection = new ArrayList<>();
    CollaborationElementChange elementChange;
    CollaborationElement element;
    String elementId;
    String elementPath;
    Set<String> elementSet = new HashSet<>();
    Collection<String> files = dao.getBranchFileList(context, git, null);

    for (String file : files) {

      elementId = extractElementIdFromFilePath(file);
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

  public CollaborationMergeChange handleResetResponse(SessionContext context, Git git,
                                                      Collection<DiffEntry> resetResult) {
    CollaborationMergeChange collaborationMergeChange = new CollaborationMergeChange();
    if (resetResult != null && resetResult.size() > 0) {
      Map<String, CollaborationElementChange> elementMap = new HashMap<>();
      String elementId;
      ItemVersion itemVersion = null;

      for (DiffEntry diff : resetResult) {
        elementId = extractElementIdFromFilePath(diff.getNewPath());
        uploadChangedData(git, collaborationMergeChange, elementMap, elementId, itemVersion,
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

  private void uploadChangedData(Git git, CollaborationMergeChange collaborationMergeChange,
                                 Map<String, CollaborationElementChange> elementMap,
                                 String elementId, ItemVersion itemVersion, DiffEntry diff) {
    ItemVersionChange itemVersionChange;
    CollaborationElement element;
    CollaborationElementChange elementChange;
    if (elementId == null && itemVersion == null) {
      itemVersion = getCollaborationElementUtil().uploadItemVersionData(git);
      itemVersionChange = new ItemVersionChange();
      itemVersionChange.setItemVersion(itemVersion);
      itemVersionChange.setAction(Action.UPDATE);
      collaborationMergeChange.setChangedVersion(itemVersionChange);
    } else if (elementId != null && !elementMap.containsKey(elementId)) {
      String elementPath = extractElementPathFromFilePath(diff.getNewPath());
      element =
          getCollaborationElementUtil().uploadCollaborationElement(git, elementPath, elementId);
      elementChange = new CollaborationElementChange();
      elementChange.setAction(Action.UPDATE);
      elementChange.setElement(element);
      elementMap.put(elementId, elementChange);
    }

  }
}
