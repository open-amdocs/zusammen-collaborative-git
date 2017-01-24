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


import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.Namespace;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.collaboration.ChangeType;
import org.amdocs.zusammen.datatypes.item.Info;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.types.LocalRemoteDataConflict;
import org.amdocs.zusammen.sdk.types.ChangedElementData;
import org.amdocs.zusammen.sdk.types.ElementData;
import org.amdocs.zusammen.sdk.types.ElementDataConflict;
import org.amdocs.zusammen.sdk.types.ItemVersionChangedData;
import org.amdocs.zusammen.sdk.types.ItemVersionMergeConflict;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SourceControlUtil {

  private final String HEADER_END = "<<<<<<<";
  private final String TRAILER_START = ">>>>>>>";
  private final String SWITCH_FILE = "=======";

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

  public ItemVersionMergeConflict handleSyncResponse(SessionContext context, Git git, PullResult
      pullResult) {

    if (pullResult != null && !pullResult.isSuccessful()) {
      return handleMergeResponse(context, git, pullResult.getMergeResult());
    }
    return new ItemVersionMergeConflict();
  }

  public ItemVersionMergeConflict handleMergeResponse(SessionContext context, Git git, MergeResult
      mergeResult) {

    ItemVersionMergeConflict result = new ItemVersionMergeConflict();
    String elementId;
    Map<String, ElementData> elementDataMap = new HashMap<>();
    ElementData elementData;
    if (!isMergeSuccesses(mergeResult)) {
      for (String file : mergeResult.getConflicts().keySet()) {
        elementId = extractElementIdFromFilePath(file);
        if (!elementDataMap.containsKey(elementId)) {
          elementData =
              elementDataUtil.uploadElementData(git, extractElementPathFromFilePath
                  (file), elementId);
          elementDataMap.put(elementId, elementData);
        }
      }
    }

    for (Map.Entry<String, ElementData> entry : elementDataMap.entrySet()) {
      result.addElementConflict(handleElementConflict(entry.getValue()));
    }


    return result;

  }

  private boolean isMergeSuccesses(MergeResult mergeResult) {
    return (mergeResult != null &&
        !mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.ALREADY_UP_TO_DATE) &&
        !mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.MERGED) &&
        !mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.MERGED_NOT_COMMITTED) &&
        !mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.FAST_FORWARD)
        || (mergeResult == null ||
        mergeResult.getConflicts() == null ||
        mergeResult.getConflicts().size() == 0));
  }


  private ElementDataConflict handleElementConflict(ElementData elementData) {
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

    //info
    localRemoteDataConflict = splitMergedFile(JsonUtil.object2Json(elementData.getInfo()));
    elementConflicts.getLocalElement().setInfo(JsonUtil.json2Object(new
        String(localRemoteDataConflict.getLocal()), Info.class));
    elementConflicts.getRemoteElement().setInfo(JsonUtil.json2Object(new
        String(localRemoteDataConflict.getRemote()), Info.class));

    //search data
    localRemoteDataConflict = splitMergedFile(elementData.getSearchableData());
    elementConflicts.getLocalElement().setSearchableData(new ByteArrayInputStream
        (localRemoteDataConflict.getLocal()));
    elementConflicts.getRemoteElement().setSearchableData(new ByteArrayInputStream
        (localRemoteDataConflict.getRemote()));

    return elementConflicts;

  }

  public LocalRemoteDataConflict splitMergedFile(InputStream is) {
    String mergedFile = new String(FileUtils.toByteArray(is));
    return splitMergedFile(mergedFile);
  }

  public LocalRemoteDataConflict splitMergedFile(String mergedFile) {
    LocalRemoteDataConflict localRemoteDataConflict = new LocalRemoteDataConflict();
    String[] lines = mergedFile.split("\\r\\n|\\r|\\n");

    boolean headerEnd = false; //until <<<<<<<
    boolean trailerStart = false; // from >>>>>>>
    boolean switchFile = false; // from =======
    for (String line : lines) {
      if (line.startsWith(HEADER_END)) {
        headerEnd = true;
        continue;
      }
      if (line.startsWith(TRAILER_START)) {
        trailerStart = true;
        continue;
      }
      if (line.startsWith(SWITCH_FILE)) {
        switchFile = true;
        continue;
      }
      if (!switchFile || trailerStart) {
        localRemoteDataConflict.appendL(line);
      }
      if (switchFile || trailerStart || !headerEnd) {
        localRemoteDataConflict.appendR(line);
      }
    }
    return localRemoteDataConflict;
  }

  public ItemVersionChangedData handlePublishResponse(SessionContext context,
                                                      GitSourceControlDao dao,
                                                      Git git,
                                                      Collection<PushResult> pushResult) {

    ObjectId from =
        pushResult.iterator().next().getRemoteUpdates().iterator().next().getExpectedOldObjectId();
    ObjectId to =
        pushResult.iterator().next().getRemoteUpdates().iterator().next().getNewObjectId();
    return handleSyncFileDiff(context, dao, git,
        from, to);

  }


  public ItemVersionChangedData handleSyncFileDiff(SessionContext
                                                       context,
                                                   GitSourceControlDao dao,
                                                   Git git,
                                                   ObjectId from,
                                                   ObjectId to) {
    to = to != null ? to : dao.getHead(context, git);


    ItemVersionChangedData itemVersionChangedData = new ItemVersionChangedData();

    Collection<DiffEntry> diffs = dao.revisionDiff(context, git, from, to);
    Collection<ChangedElementData> changedElementInfoCollection = new ArrayList<>();

    if (diffs != null) {
      Set<String> elementDataSet = new HashSet<>();
      String elementId;
      ElementData elementData;
      ChangedElementData changedElementData;
      Info changedInfo;
      for (DiffEntry diff : diffs) {
        elementId = extractElementIdFromFilePath(diff.getNewPath());
        if (elementId == null) {
          changedInfo = elementDataUtil.uploadItemVersionInfo(git);
          itemVersionChangedData.setItemVersionInfo(changedInfo);
        }else if (!elementDataSet.contains(elementId)) {
          String elementPath = extractElementPathFromFilePath(diff.getNewPath());
          elementData = elementDataUtil.uploadElementData(git, elementPath,
              elementId);
          elementDataSet.add(elementId);
          changedElementData = new ChangedElementData();
          changedElementData.setChangeType(ChangeType.valueOf(diff.getChangeType().name()));
          changedElementData.setElementData(elementData);
          changedElementInfoCollection.add(changedElementData);
        }
      }
    }

    itemVersionChangedData.setChangedElements(changedElementInfoCollection);
    return itemVersionChangedData;
  }

  protected String extractElementIdFromFilePath(String path) {


    File file = new File(path);
    if(file.getParentFile() == null) return null;
    return file.getParentFile().getName();
  }

  protected String extractElementPathFromFilePath(String path) {
    File file = new File(path);
    return file.getParentFile() != null ? file.getParentFile().getPath() : "";
  }

  public ItemVersionChangedData handleSyncFileDiff(SessionContext context, GitSourceControlDao dao,
                                                   Git git,
                                                   Collection<PushResult> pushResult) {
    ItemVersionChangedData itemVersionChangedData;
    Collection<ChangedElementData> changedElementInfoCollection ;

    ObjectId from = getOldRevisionId(pushResult);
    ObjectId to = getNewRevisionId(pushResult);

    if(from!= null){
      itemVersionChangedData = getChangeDataAfterPush(context,dao,git,from,to);
    }else{
      itemVersionChangedData = getRepoData(context,dao,git);
    }


    return itemVersionChangedData;
  }

  public ItemVersionChangedData getRepoData(SessionContext context,
                                                     GitSourceControlDao dao, Git git) {
    ItemVersionChangedData itemVersionChangedData = new ItemVersionChangedData();
    Collection<ChangedElementData> changedElementInfoCollection = new ArrayList<>();
    ChangedElementData changedElementData;
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
        if(elementId==null){
          Info itemVersionInfo = elementDataUtil.uploadItemVersionInfo(git);
          itemVersionChangedData.setItemVersionInfo(itemVersionInfo);

        }else if (!elementDataSet.contains(elementId)){
          elementDataSet.add(elementId);
          changedElementData = new ChangedElementData();
          changedElementData.setChangeType(ChangeType.ADD);
          elementData = elementDataUtil.uploadElementData(git, elementPath, elementId);
          changedElementData.setElementData(elementData);
          changedElementInfoCollection.add(changedElementData);
        }


      }
      itemVersionChangedData.setChangedElements(changedElementInfoCollection);
      return itemVersionChangedData;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private ItemVersionChangedData getChangeDataAfterPush(SessionContext context,
                                                                 GitSourceControlDao dao,Git git,
                                                                 ObjectId from,ObjectId to) {

    ItemVersionChangedData itemVersionChangedData = new ItemVersionChangedData();
    Collection<DiffEntry> diffs = dao.revisionDiff(context, git, from, to);
    Collection<ChangedElementData> changedElementInfoCollection = new ArrayList<>();

    if (diffs != null) {
      Set<String> elementDataSet = new HashSet<>();
      String elementId;
      ElementData elementData;
      ChangedElementData changedElementData;
      Info changedInfo;
      for (DiffEntry diff : diffs) {
        elementId = extractElementIdFromFilePath(diff.getNewPath());
        if (elementId == null) {
          changedInfo = elementDataUtil.uploadItemVersionInfo(git);
          itemVersionChangedData.setItemVersionInfo(changedInfo);
        }else if (!elementDataSet.contains(elementId)) {
          String elementPath = extractElementPathFromFilePath(diff.getNewPath());
          elementData = elementDataUtil.uploadElementData(git, elementPath,
              elementId);
          elementDataSet.add(elementId);
          changedElementData = new ChangedElementData();
          changedElementData.setChangeType(ChangeType.valueOf(diff.getChangeType().name()));
          changedElementData.setElementData(elementData);
          changedElementInfoCollection.add(changedElementData);
        }
      }
    }

    itemVersionChangedData.setChangedElements(changedElementInfoCollection);

    return itemVersionChangedData;
  }

  private ObjectId getNewRevisionId(Collection<PushResult> pushResults) {

    PushResult pushResult;
    if(pushResults.iterator().hasNext())
      pushResult = pushResults.iterator().next();
    else return null;
    Collection<RemoteRefUpdate> remoteUpdates =
        pushResult.getRemoteUpdates();
    if(remoteUpdates.iterator().hasNext()) {
      ObjectId id = remoteUpdates.iterator().next().getNewObjectId();
      if(id ==null || id.equals(ObjectId.zeroId())) return null;

      return id;
    }
    else return null;
  }

  private ObjectId getOldRevisionId(Collection<PushResult> pushResults) {
    PushResult pushResult;
    if(pushResults.iterator().hasNext())
      pushResult = pushResults.iterator().next();
    else return null;
    Collection<RemoteRefUpdate> remoteUpdates =
        pushResult.getRemoteUpdates();
    if(remoteUpdates.iterator().hasNext()) {
      ObjectId id = remoteUpdates.iterator().next().getExpectedOldObjectId();
      if (id == null || id.equals(ObjectId.zeroId())) return null;
      return id;
    }
    else return null;
  }
}
