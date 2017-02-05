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

package org.amdocs.zusammen.plugin.collaborationstore.git.impl;

import org.amdocs.zusammen.commons.configuration.impl.ConfigurationAccessor;
import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.item.Action;
import org.amdocs.zusammen.datatypes.item.ItemVersionData;
import org.amdocs.zusammen.datatypes.itemversion.Change;
import org.amdocs.zusammen.datatypes.itemversion.ItemVersionHistory;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.zusammen.sdk.SdkConstants;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeConflict;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeResult;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationPublishResult;
import org.amdocs.zusammen.utils.fileutils.FileUtils;
import org.amdocs.zusammen.utils.fileutils.json.JsonUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.amdocs.zusammen.plugin.collaborationstore.git.utils.PluginConstants.ITEM_VERSION_INFO_FILE_NAME;
import static org.amdocs.zusammen.plugin.collaborationstore.git.utils.PluginConstants.RELATIONS_FILE_NAME;
import static org.amdocs.zusammen.plugin.collaborationstore.git.utils.PluginConstants.ZUSAMMEN_TAGGING_FILE_NAME;

public class ItemVersionCollaborationStore extends CollaborationStore {
  public void create(SessionContext context, Id itemId, Id baseVersionId, Id versionId,
                     ItemVersionData itemVersionData) {
    String baseBranchId = baseVersionId == null ?
        ConfigurationAccessor.getPluginProperty(SdkConstants.ZUSAMMEN_COLLABORATIVE_STORE,
            PluginConstants.MASTER_BRANCH_PROP) : baseVersionId.toString();
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = init(context, itemId, new Id(baseBranchId), dao);
    createInt(context, git, baseBranchId, versionId.getValue());
    dao.checkoutBranch(context,git,versionId.getValue());
    boolean commitRequired =
        storeItemVersionData(context, git, itemId, itemVersionData, Action.CREATE);
    if (commitRequired) {
      dao.add(context, git, ".");
      dao.commit(context, git, PluginConstants.SAVE_ITEM_VERSION_MESSAGE);
    }
    dao.close(context, git);
  }

  protected void createInt(SessionContext context, Git git, String baseBranch,
                           String branch) {
    GitSourceControlDao dao = getSourceControlDao(context);
    dao.createBranch(context, git, baseBranch, branch);
  }

  protected boolean storeItemVersionData(SessionContext context, Git git, Id itemId,
                                         ItemVersionData itemVersionData, Action action) {

    if (itemVersionData == null || (itemVersionData.getInfo() == null && itemVersionData
        .getRelations() == null)) {
      return false;
    }
    if (action.equals(Action.CREATE)) {
      storeZusammenTaggingInfo(context, git, itemId);
    }
    if (itemVersionData.getInfo() != null) {
      storeData(context, git, itemId, ITEM_VERSION_INFO_FILE_NAME, itemVersionData.getInfo());
    }
    if (itemVersionData.getRelations() != null && itemVersionData.getRelations().size() > 0) {
      storeData(context, git, itemId, RELATIONS_FILE_NAME, itemVersionData.getRelations());
    }

    return true;
  }

  private void storeZusammenTaggingInfo(SessionContext context, Git git, Id itemId) {
    try {
      Optional<InputStream> is = FileUtils
          .readFile(git.getRepository().getWorkTree().getPath(), ZUSAMMEN_TAGGING_FILE_NAME);
      String baseId;
      Map<String, String> itemVersionInformation;
      if (is.isPresent()) {
        itemVersionInformation = JsonUtil.json2Object(is.get(), Map.class);
        baseId = itemVersionInformation.get(PluginConstants.ITEM_VERSION_ID);
      } else {
        itemVersionInformation = new HashMap<>();
        baseId = null;
      }
      itemVersionInformation.put(PluginConstants.ITEM_VERSION_ID, git.getRepository().getBranch());
      itemVersionInformation.put(PluginConstants.ITEM_VERSION_BASE_ID, baseId);
      storeData(context, git, itemId, ZUSAMMEN_TAGGING_FILE_NAME, itemVersionInformation);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  protected boolean storeData(SessionContext context, Git git, Id itemId, String fileName,
                              Object data) {
    addFileContent(
        context,
        git,
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH,
            itemId) +
            File.separator,
        null,
        fileName,
        data);
    return true;
  }


  public void save(SessionContext context, Id itemId, Id versionId,
                   ItemVersionData itemVersionData) {
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = init(context, itemId, versionId, dao);
    boolean commitRequired = storeItemVersionData(context, git, itemId, itemVersionData,
        Action.UPDATE);
    if (commitRequired) {
      dao.add(context, git, ".");
      dao.commit(context, git, PluginConstants.SAVE_ITEM_VERSION_MESSAGE);
    }
    dao.close(context, git);
  }


  public void delete(SessionContext sessionContext, Id itemId, Id versionId) {
    /*todo*/
  }


  public CollaborationPublishResult publish(SessionContext context, Id itemId, Id versionId,
                                            String message) {
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = init(context, itemId, versionId, dao);

    String branchId = versionId.toString();

    Collection<PushResult> pushResult = dao.publish(context, git, branchId);

    CollaborationPublishResult publishResult =
        sourceControlUtil.handleMergeFileDiff(context, dao, git, pushResult);
    dao.close(context, git);
    return publishResult;
  }


  public CollaborationMergeResult sync(SessionContext context, Id itemId, Id versionId) {
    CollaborationMergeResult result = new CollaborationMergeResult();

    GitSourceControlDao dao = getSourceControlDao(context);

    String repositoryPath =
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH, itemId);
    repositoryPath = resolveTenantPath(context, repositoryPath);
    Git git;
    String branch = versionId.toString();
    ObjectId oldId = null;
    if (FileUtils.exists(repositoryPath)) {
      git = dao.openRepository(context, repositoryPath);
      if (dao.checkoutBranch(context, git, branch)) {
        oldId = dao.getHead(context, git);
      }
      PullResult syncResult = dao.sync(context, git, branch);
      CollaborationMergeConflict mergeConflict =
          sourceControlUtil.handleSyncResponse(context, git, syncResult);
      result.setConflict(mergeConflict);
      if (syncResult != null && !syncResult.isSuccessful()) {
        ElementCollaborationStore elementCollaborationStore = new ElementCollaborationStore();
        mergeConflict.getElementConflicts().forEach(element ->
            elementCollaborationStore.update(context, element.getLocalElement()));

      }
    } else {
      String publicPath = resolveTenantPath(context, PluginConstants.PUBLIC_PATH);
      git = dao.clone(context,
          sourceControlUtil.getPublicRepositoryPath(context, publicPath, itemId),
          repositoryPath, branch);

    }

    CollaborationMergeChange changedData = sourceControlUtil.handleMergeFileDiff(context, dao, git,
        oldId, null);
    result.setChange(changedData);
    dao.close(context, git);

    return result;
  }


  public CollaborationMergeResult merge(SessionContext context, Id itemId, Id
      versionId, Id sourceVersionId) {
    CollaborationMergeResult result = new CollaborationMergeResult();

    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH, itemId);
    repositoryPath = resolveTenantPath(context, repositoryPath);
    Git git;
    String branch = versionId.toString();
    String sourceBranch = sourceVersionId.toString();
    ObjectId oldId = null;
    if (FileUtils.exists(repositoryPath)) {
      git = dao.openRepository(context, repositoryPath);
      dao.checkoutBranch(context, git, branch);
      oldId = dao.getHead(context, git);
      MergeResult mergeResult =
          dao.merge(context, git, sourceBranch, MergeCommand.FastForwardMode.FF, null,
              null);
      CollaborationMergeConflict conflict =
          sourceControlUtil.handleMergeResponse(context, git, mergeResult);
      result.setConflict(conflict);
    } else {
      String publicPath = resolveTenantPath(context, PluginConstants.PUBLIC_PATH);
      git = dao.clone(context,
          sourceControlUtil.getPublicRepositoryPath(context, publicPath, itemId),
          repositoryPath, branch);

    }

    //Collection<ChangedCollaborationElement> changedElements =
    CollaborationMergeChange changedData =
        sourceControlUtil.handleMergeFileDiff(context, dao, git, oldId,
            null);

    result.setChange(changedData);

    dao.close(context, git);
    return result;
  }


  public CollaborationMergeChange resetHistory(SessionContext context, Id itemId, Id versionId,
                                               Id changeId) {
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = init(context, itemId, versionId, dao);
    ObjectId revId = ObjectId.fromString(changeId.getValue());
    ObjectId head = dao.getHead(context, git);

    Collection<DiffEntry> resetResult =
        dao.revisionDiff(context, git, revId, head, null);
    dao.revert(context, git, revId);
    CollaborationMergeChange changes = sourceControlUtil.handleResetResponse(context, git,
        resetResult);
    return changes;
  }

  private Git init(SessionContext context, Id itemId, Id versionId, GitSourceControlDao dao) {
    String repositoryPath =
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH,
            itemId);
    repositoryPath = resolveTenantPath(context, repositoryPath);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId.getValue());
    return git;
  }

  public ItemVersionHistory listHistory(SessionContext context, Id itemId, Id versionId) {
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = init(context, itemId, versionId, dao);
    Iterable<RevCommit> iter = dao.listHistory(context, git);
    ItemVersionHistory itemVersionHistory = new ItemVersionHistory();
    Change change;
    for (RevCommit revCommit : iter) {
      change = getChange(revCommit);
      itemVersionHistory.addChange(change);

    }
    return itemVersionHistory;
  }

  protected Change getChange(RevCommit revCommit) {
    Change change;
    change = new Change();
    change.setChangeId(new Id(revCommit.getId().getName()));
    change.setTime(revCommit.getCommitTime());
    change.setMessage(revCommit.getFullMessage());
    change.setUser(revCommit.getAuthorIdent().getName());
    return change;
  }
}
