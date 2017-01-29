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
import org.amdocs.zusammen.sdk.types.CollaborationMergeChange;
import org.amdocs.zusammen.sdk.types.CollaborationMergeConflict;
import org.amdocs.zusammen.sdk.types.CollaborationPublishResult;
import org.amdocs.zusammen.sdk.types.ElementData;
import org.amdocs.zusammen.sdk.types.ElementDataChange;
import org.amdocs.zusammen.sdk.types.ElementDataConflict;
import org.amdocs.zusammen.utils.fileutils.FileUtils;
import org.amdocs.zusammen.utils.fileutils.json.JsonUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SourceControlUtil {


  private final ElementDataUtil elementDataUtil = new ElementDataUtil();

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
    //Map<String, ElementData> elementDataMap = new HashMap<>();
    Map<String, String> elementPathMap = new HashMap<>();
    ElementData elementData;
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


  private boolean isMergeSuccesses(MergeResult mergeResult) {
    return !(mergeResult != null && mergeResult.getConflicts() != null && mergeResult.getConflicts
        ().size() > 0);
  }

  private ElementDataConflict handleElementConflict(Git git, String elementPath, String elementId) {

    ElementData elementData = elementDataUtil.initElementData(git, elementPath, elementId);
    String fullPath = git.getRepository().getWorkTree().getPath() + File.separator + elementPath;
    elementDataUtil.getFileContent(fullPath, PluginConstants.VISUALIZATION_FILE_NAME)
        .ifPresent(elementData::setVisualization);
    elementDataUtil.getFileContent(fullPath, PluginConstants.DATA_FILE_NAME)
        .ifPresent(elementData::setData);
    elementDataUtil.getFileContent(fullPath, PluginConstants.SEARCH_DATA_FILE_NAME)
        .ifPresent(elementData::setSearchableData);


    ElementDataConflict elementConflicts = new ElementDataConflict();
    elementConflicts.setLocalElement(new ElementData(elementData.getItemId(), elementData
        .getVersionId(), elementData.getNamespace(), elementData.getId()));
    elementConflicts.setRemoteElement(new ElementData(elementData.getItemId(), elementData
        .getVersionId(), elementData.getNamespace(), elementData.getId()));

    //data
    LocalRemoteDataConflict localRemoteDataConflict = splitMergedFile(elementData.getData());
    elementConflicts.getLocalElement().setData(new ByteArrayInputStream
        (localRemoteDataConflict.getLocal()));
    elementConflicts.getRemoteElement().setData(new ByteArrayInputStream
        (localRemoteDataConflict.getRemote()));

    //search data
    localRemoteDataConflict = splitMergedFile(elementData.getSearchableData());
    elementConflicts.getLocalElement().setSearchableData(new ByteArrayInputStream
        (localRemoteDataConflict.getLocal()));
    elementConflicts.getRemoteElement().setSearchableData(new ByteArrayInputStream
        (localRemoteDataConflict.getRemote()));

    //visualisation data
    localRemoteDataConflict = splitMergedFile(elementData.getVisualization());
    elementConflicts.getLocalElement().setSearchableData(new ByteArrayInputStream
        (localRemoteDataConflict.getLocal()));
    elementConflicts.getRemoteElement().setSearchableData(new ByteArrayInputStream
        (localRemoteDataConflict.getRemote()));


    byte[] content;
    Optional<InputStream> contentIS;
    //info
    contentIS = elementDataUtil.getFileContent(fullPath, PluginConstants
        .INFO_FILE_NAME);
    if (contentIS.isPresent()) {
      content = FileUtils.toByteArray(contentIS.get());
      localRemoteDataConflict = GitConflictFileSplitter.splitMergedFile(content);
      elementConflicts.getLocalElement().setInfo(JsonUtil.json2Object(new ByteArrayInputStream
          (localRemoteDataConflict.getLocal()), Info.class));
      elementConflicts.getLocalElement().setInfo(JsonUtil.json2Object(new ByteArrayInputStream
          (localRemoteDataConflict.getRemote()), Info.class));
    }

    //relations
    contentIS = elementDataUtil.getFileContent(fullPath, PluginConstants
        .RELATIONS_FILE_NAME);
    if (contentIS.isPresent()) {
      content = FileUtils.toByteArray(contentIS.get());
      localRemoteDataConflict = GitConflictFileSplitter.splitMergedFile(content);
      elementConflicts.getLocalElement().setRelations(JsonUtil.json2Object(new ByteArrayInputStream
          (localRemoteDataConflict.getLocal()), new TypeToken<ArrayList<Relation>>() {
      }.getType()));
      elementConflicts.getLocalElement().setRelations(JsonUtil.json2Object(new ByteArrayInputStream
          (localRemoteDataConflict.getRemote()), new TypeToken<ArrayList<Relation>>() {
      }.getType()));
    }


    return elementConflicts;
  }

  public LocalRemoteDataConflict splitMergedFile(InputStream is) {
    byte[] mergedFile = FileUtils.toByteArray(is);
    return GitConflictFileSplitter.splitMergedFile(mergedFile);
  }

  public CollaborationMergeChange handlePublishFileDiff(SessionContext
                                                            context,
                                                        GitSourceControlDao dao,
                                                        Git git,
                                                        ObjectId from,
                                                        ObjectId to) {
    to = to != null ? to : dao.getHead(context, git);


    CollaborationMergeChange collaborationPublishResult =
        calculateItemVersionChangedData(context, dao, git,
            from, to, null);
    return collaborationPublishResult;
  }

  private CollaborationMergeChange calculateItemVersionChangedData(SessionContext context,
                                                                   GitSourceControlDao dao,
                                                                   Git git,
                                                                   ObjectId from,
                                                                   ObjectId to,
                                                                   TreeFilter treeFilter) {

    CollaborationMergeChange collaborationMergeChange = new CollaborationMergeChange();

    Collection<DiffEntry> diffs = dao.revisionDiff(context, git, from, to, treeFilter);


    if (diffs != null) {
      Map<String, ElementDataChange> elementDataMap = new HashMap();
      String elementId;
      ElementData elementData;
      ElementDataChange changedElementData;
      ItemVersion itemVersion = null;
      ItemVersionChange itemVersionChange;
      for (DiffEntry diff : diffs) {
        elementId = extractElementIdFromFilePath(diff.getNewPath());
        if (elementId == null && itemVersion == null) {
          itemVersion = elementDataUtil.uploadItemVersionData(git);
          itemVersionChange = new ItemVersionChange();
          itemVersionChange.setItemVersion(itemVersion);
          itemVersionChange.setAction(Action.UPDATE);
          collaborationMergeChange.setChangedVersion(itemVersionChange);
        } else if (elementId != null && !elementDataMap.containsKey(elementId)) {
          String elementPath = extractElementPathFromFilePath(diff.getNewPath());
          elementData = elementDataUtil.uploadElementData(git, elementPath,
              elementId);
          changedElementData = new ElementDataChange();
          changedElementData.setAction(Action.UPDATE);
          changedElementData.setElementData(elementData);
          elementDataMap.put(elementId, changedElementData);
        }

        if (diff.getNewPath().contains(PluginConstants.ZUSAMMEN_TAGGING_FILE_NAME)) {
          if (elementId == null) {
            collaborationMergeChange.getChangedVersion().setAction(getAction(diff
                .getChangeType()));
          } else {
            elementDataMap.get(elementId).setAction(getAction(diff.getChangeType()));
          }
        }
      }
      collaborationMergeChange.setChangedElements(elementDataMap.values());
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

  public CollaborationPublishResult handlePublishFileDiff(SessionContext context,
                                                          GitSourceControlDao dao,
                                                          Git git,
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

  private CollaborationPublishResult getChangeData(SessionContext context,
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

    Collection<ElementDataChange> changedElementInfoCollection = new ArrayList();
    ElementDataChange changedElementData;
    ElementData elementData;
    String elementId;
    String elementPath;
    try {
      RevWalk walk = new RevWalk(git.getRepository());
      Ref head = git.getRepository().getRef("HEAD");
      RevCommit commit = walk.parseCommit(head.getObjectId());
      RevTree tree = commit.getTree();

      TreeWalk treeWalk = new TreeWalk(git.getRepository());

      treeWalk.addTree(tree);
      treeWalk.setRecursive(true);
      Set<String> elementDataSet = new HashSet<>();
      while (treeWalk.next()) {

        elementId = extractElementIdFromFilePath(treeWalk.getPathString());
        elementPath = extractElementPathFromFilePath(treeWalk.getPathString());
        if (elementId == null) {
          ItemVersion itemVersion = elementDataUtil.uploadItemVersionData(git);
          ItemVersionChange itemVersionChange = new ItemVersionChange();
          itemVersionChange.setItemVersion(itemVersion);
          mergeChange.setChangedVersion(itemVersionChange);
          mergeChange.getChangedVersion().setAction(Action.CREATE);

        } else if (!elementDataSet.contains(elementId)) {
          elementDataSet.add(elementId);
          changedElementData = new ElementDataChange();
          changedElementData.setAction(Action.CREATE);
          elementData = elementDataUtil.uploadElementData(git, elementPath, elementId);
          changedElementData.setElementData(elementData);
          changedElementInfoCollection.add(changedElementData);
        }
      }
      mergeChange.setChangedElements(changedElementInfoCollection);

      collaborationPublishResult.setChange(mergeChange);
      return collaborationPublishResult;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }



  private ObjectId getNewRevisionId(Collection<PushResult> pushResults) {

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

  private ObjectId getOldRevisionId(Collection<PushResult> pushResults) {
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

  private Action getAction(DiffEntry.ChangeType changeType) {
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


}
