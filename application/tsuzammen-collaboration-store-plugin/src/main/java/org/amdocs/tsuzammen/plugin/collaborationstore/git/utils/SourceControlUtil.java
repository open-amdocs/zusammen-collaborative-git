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
import org.amdocs.tsuzammen.datatypes.collaboration.Conflict;
import org.amdocs.tsuzammen.datatypes.collaboration.FileConflicts;
import org.amdocs.tsuzammen.datatypes.collaboration.MergeResponse;
import org.amdocs.tsuzammen.datatypes.collaboration.SyncResponse;
import org.amdocs.tsuzammen.utils.common.CommonMethods;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.transport.FetchResult;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SourceControlUtil {

  private static final String HEADER_END="<<<<<<<";
  private static final String TRAILER_START=">>>>>>>";
  private static final String SWITCH_FILE="=======";

  private static String convertNamespaceToPath(String namespace) {
    String[] pathArray = namespace.split(".");

    return CommonMethods.arrayToSeparatedString(pathArray, File.separatorChar);
  }

  public static String getPrivateRepositoryPath(Git git) {
    return git.getRepository().getWorkTree().getPath();
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

  public static SyncResponse handleSyncResponse(Git git , PullResult pullResult) {
    SyncResponse result = new SyncResponse();

    if(pullResult!= null && !pullResult.isSuccessful()) {
      if(pullResult.getMergeResult() !=null ) {
        Set<String> conflictFiles = pullResult.getMergeResult().getConflicts().keySet();
        conflictFiles.forEach(file->result.addConflict(handleFileConflict(git.getRepository()
            .getWorkTree().getPath(),file)));
      }else if(pullResult.getRebaseResult()!= null){
        Collection<String> conf = pullResult.getRebaseResult().getConflicts();

      }


    }
    return result;
  }

  public static MergeResponse handleMergeResponse(Git git , MergeResult mergeResult) {
    MergeResponse result = new MergeResponse();

    if(!isMergeSuccesses(mergeResult)){
      if(mergeResult !=null ) {
        Set<String> conflictFiles = mergeResult.getConflicts().keySet();
        conflictFiles.forEach(file->result.addConflict(handleFileConflict(git
            .getRepository()
            .getWorkTree().getPath(),file)));
      }


    }
    return result;
  }

  private static boolean isMergeSuccesses(MergeResult mergeResult) {
    return !mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.ALREADY_UP_TO_DATE) &&
        !mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.MERGED) &&
        !mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.MERGED_NOT_COMMITTED) &&
        !mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.FAST_FORWARD);
  }


  private static FileConflicts handleFileConflict(String repositoryPath, String file) {




    InputStream conflictFileIS = FileUtils.getFileInputStream(repositoryPath+File.separator+file);
    String mergedFile = new String(FileUtils.toByteArray(conflictFileIS));
    String[] lines = mergedFile.split("\\r\\n|\\r|\\n");
    StringBuffer localSB = new StringBuffer();
    StringBuffer remoteSB = new StringBuffer();
    StringBuffer localConflictSB = new StringBuffer();
    StringBuffer remoteConflictSB = new StringBuffer();
    boolean headerEnd = false; //until <<<<<<<
    boolean trailerStart = false; // from >>>>>>>
    boolean switchFile = false; // from =======
    for(String line:lines){
      if(line.startsWith(HEADER_END)) {
        headerEnd=true;
        continue;
      }
      if(line.startsWith(TRAILER_START)) {
        trailerStart=true;
        continue;
      }
      if(line.startsWith(SWITCH_FILE)) {
        switchFile=true;
        continue;
      }
      if( !switchFile || trailerStart){
        localSB.append(line).append(System.lineSeparator());
      }
      if(switchFile || trailerStart || !headerEnd){
        remoteSB.append(line).append(System.lineSeparator());
      }

      if (headerEnd && !switchFile && !trailerStart ){
        localConflictSB.append(line).append(System.lineSeparator());
      }
      if (headerEnd && switchFile && !trailerStart){
        remoteConflictSB.append(line).append(System.lineSeparator());
      }
    }
    FileConflicts fileConflicts = new FileConflicts();
    Conflict conflict = new Conflict();
    fileConflicts.setFileName(file);
    fileConflicts.setLocal(localSB.toString().getBytes());
    fileConflicts.setRemote(remoteSB.toString().getBytes());
    Collection<Conflict> conflictList = new ArrayList<>();
    conflictList.add(conflict);
    fileConflicts.setConflicts(conflictList);
    conflict.setLocalConflict(localConflictSB.toString().getBytes());
    conflict.setRemoteConflict(remoteConflictSB.toString().getBytes());
    return fileConflicts;
  }

  public static SyncResponse handleFetchResult(Git git, FetchResult fetchResult) {
    return null;
  }


}
