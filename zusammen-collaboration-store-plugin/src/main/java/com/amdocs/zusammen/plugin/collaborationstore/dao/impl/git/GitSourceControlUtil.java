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

package com.amdocs.zusammen.plugin.collaborationstore.dao.impl.git;

import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.plugin.collaborationstore.types.CollaborationDiffResult;
import com.amdocs.zusammen.plugin.collaborationstore.types.FileInfoDiff;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;

import java.util.Collection;

public class GitSourceControlUtil {
  public ObjectId getOldRevisionId(PushResult pushResult) {

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


  public ObjectId getOldRevisionId(MergeResult mergeResult) {

    mergeResult.getNewHead();
    return null;
  }


  protected ObjectId getNewRevisionId(PushResult pushResult) {

    ObjectId id = null;
    Collection<RemoteRefUpdate> remoteUpdates = pushResult.getRemoteUpdates();
    if (remoteUpdates.iterator().hasNext()) {
      id = remoteUpdates.iterator().next().getNewObjectId();
    }
    return id == null || id.equals(ObjectId.zeroId()) ? null : id;
  }

  public CollaborationDiffResult getFileDiff( Collection<DiffEntry> diffs) {
    CollaborationDiffResult collaborationMergeResult = new CollaborationDiffResult
        (CollaborationDiffResult.MODE.UPDATE);
    FileInfoDiff fileInfoDiff;
    for (DiffEntry diff : diffs) {
      fileInfoDiff = new FileInfoDiff(diff.getNewPath(),
          getAction(diff
              .getChangeType()));
      collaborationMergeResult.add(fileInfoDiff);

    }
    return collaborationMergeResult;
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

 /* public CollaborationElementConflict handleElementConflict(Git git, String elementPath, String
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

}
