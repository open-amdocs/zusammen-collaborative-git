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


import org.amdocs.tsuzammen.datatypes.Id;
import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.datatypes.collaboration.ChangeType;
import org.amdocs.tsuzammen.datatypes.collaboration.FileSyncInfo;
import org.amdocs.tsuzammen.datatypes.collaboration.MergeResponse;
import org.amdocs.tsuzammen.datatypes.item.ElementInfo;
import org.amdocs.tsuzammen.datatypes.item.Info;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.SourceControlDaoFactory;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.types.LocalRemoteDataConflict;
import org.amdocs.tsuzammen.sdk.types.ChangedElementData;
import org.amdocs.tsuzammen.sdk.types.ElementConflicts;
import org.amdocs.tsuzammen.sdk.types.ElementData;
import org.amdocs.tsuzammen.sdk.types.SyncResult;
import org.amdocs.tsuzammen.utils.common.CommonMethods;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;
import org.amdocs.tsuzammen.utils.fileutils.json.JsonUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SourceControlUtil {

  private static final String HEADER_END = "<<<<<<<";
  private static final String TRAILER_START = ">>>>>>>";
  private static final String SWITCH_FILE = "=======";

  private static String convertNamespaceToPath(String namespace) {
    String[] pathArray = namespace.split(".");

    return CommonMethods.arrayToSeparatedString(pathArray, File.separatorChar);
  }


  public static String getPrivateRepositoryPath(SessionContext context, String path, Id itemId) {
    StringBuffer sb = new StringBuffer();
    sb.append(path).append(File.separator).append("users").append(File.separator).append(context
        .getUser()
        .getUserName())
        .append(File
            .separator).append(itemId.getValue().toString());

    return sb.toString();
  }

  public static String getPublicRepositoryPath(SessionContext context, String
      path, Id itemId) {
    StringBuffer sb = new StringBuffer();
    sb.append(path).append(File.separator).append(itemId.getValue().toString());

    return sb.toString();
  }

  public static SyncResult handleSyncResponse(SessionContext context, Git git, PullResult
      pullResult) {

    if (pullResult != null && !pullResult.isSuccessful()){
      return handleMergeResponse(context,git,pullResult.getMergeResult());
    }
    return new SyncResult();
  }

  public static SyncResult handleMergeResponse(SessionContext context,Git git, MergeResult
      mergeResult) {

    SyncResult result = new SyncResult();
    String elementId;
    Map<String, ElementData> elementDataMap = new HashMap<>();
    ElementData elementData;
    if (!isMergeSuccesses(mergeResult)) {
      for (String file : mergeResult.getConflicts().keySet()) {
        elementId = extractEelementIdFromFilePath(file);
        if (!elementDataMap.containsKey(elementId)) {
          elementData = ElementDataUtil.init().uploadElementData(context, git, FileUtils.trimPath
              (file));
          elementDataMap.put(elementId, elementData);
        }
      }
    }

    for (Map.Entry<String, ElementData> entry : elementDataMap.entrySet()) {
      result.addElementConflicts(handleElementConflict(entry.getValue()));
    }
    return result;

  }

  private static boolean isMergeSuccesses(MergeResult mergeResult) {
    return mergeResult!=null &&
        !mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.ALREADY_UP_TO_DATE) &&
        !mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.MERGED) &&
        !mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.MERGED_NOT_COMMITTED) &&
        !mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.FAST_FORWARD);
  }


  private static ElementConflicts handleElementConflict(ElementData elementData) {
    ElementConflicts elementConflicts = new ElementConflicts();
    elementConflicts.setLocalElementData(new ElementData());
    elementConflicts.setRemoteElementData(new ElementData());

    //data
    LocalRemoteDataConflict localRemoteDataConflict = splitMergedFile(elementData.getData());
    elementConflicts.getLocalElementData().setData(new ByteArrayInputStream
        (localRemoteDataConflict.getLocal()));
    elementConflicts.getRemoteElementData().setData(new ByteArrayInputStream
        (localRemoteDataConflict.getRemote()));

    //info
    localRemoteDataConflict = splitMergedFile(JsonUtil.object2Json(elementData.getInfo()));
    elementConflicts.getLocalElementData().setInfo(JsonUtil.json2Object(new
        String(localRemoteDataConflict.getLocal()), Info.class));
    elementConflicts.getRemoteElementData().setInfo(JsonUtil.json2Object(new
        String(localRemoteDataConflict.getRemote()), Info.class));

    //search data
    localRemoteDataConflict = splitMergedFile(elementData.getSearchData());
    elementConflicts.getLocalElementData().setSearchData(new ByteArrayInputStream
        (localRemoteDataConflict.getLocal()));
    elementConflicts.getRemoteElementData().setSearchData(new ByteArrayInputStream
        (localRemoteDataConflict.getRemote()));

    return elementConflicts;

  }

  public static LocalRemoteDataConflict splitMergedFile(InputStream is) {
    String mergedFile = new String(FileUtils.toByteArray(is));
    return splitMergedFile(mergedFile);
  }

  public static LocalRemoteDataConflict splitMergedFile(String mergedFile) {
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

  public static Collection<ChangedElementData> handleSyncFileDiff(SessionContext context,
                                                                  GitSourceControlDao dao, Git git,
                                                                  Id itemId, Id versionId,
                                                                  ObjectId from) {
    ObjectId newId = dao.getHead(context, git);
    Collection<DiffEntry> diffs = dao.revisionDiff(context, git, from, newId);
    Collection<ChangedElementData> changedElementInfoCollection = new ArrayList<>();

    if (diffs != null) {
      Set<String> elementDataSet = new HashSet();
      String elementId;
      ElementData elementData;
      ChangedElementData changedElementData;
      for (DiffEntry diff : diffs) {
        elementId = extractEelementIdFromFilePath(diff.getNewPath());
        if (!elementDataSet.contains(elementId)) {
          elementData = ElementDataUtil.init().uploadElementData(context, git, diff.getNewPath());
          elementDataSet.add(elementId);
          changedElementData = new ChangedElementData();
          changedElementData.setChangeType(ChangeType.valueOf(diff.getChangeType().name()));
          changedElementData.setElementData(elementData);
          changedElementInfoCollection.add(changedElementData);
        }
      }
    }
    return changedElementInfoCollection;
  }

  private static String extractEelementIdFromFilePath(String path) {
    String[] splitPath = path.split(File.separator);
    return splitPath[splitPath.length - 2];
  }

  private static FileSyncInfo convertDiffEntityToFilesyncInfo(DiffEntry diff) {
    FileSyncInfo fileSyncInfo = new FileSyncInfo();
    fileSyncInfo.setFileName(diff.getNewPath());
    fileSyncInfo.setAction(ChangeType.valueOf(diff.getChangeType().name()));
    return fileSyncInfo;
  }


  private static GitSourceControlDao getSourceControlDao(SessionContext context) {
    return SourceControlDaoFactory.getInstance().createInterface(context);
  }
}
