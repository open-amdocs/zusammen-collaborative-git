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

package org.amdocs.tsuzammen.plugin.collaborationstore.git.impl;

import org.amdocs.tsuzammen.commons.configuration.impl.ConfigurationAccessor;
import org.amdocs.tsuzammen.datatypes.Id;
import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.datatypes.collaboration.PublishResult;
import org.amdocs.tsuzammen.datatypes.item.Info;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.tsuzammen.sdk.types.CollaborationChangedElementData;
import org.amdocs.tsuzammen.sdk.types.CollaborationSyncResult;
import org.amdocs.tsuzammen.sdk.utils.SdkConstants;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.PushResult;

import java.io.File;
import java.util.Collection;

import static org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants.ITEM_VERSION_INFO_FILE_NAME;

public class ItemVersionCollaborationStore extends CollaborationStore {
  public void create(SessionContext context, Id itemId, Id baseVersionId, Id versionId,
                     Info versionInfo) {
    String baseBranchId = baseVersionId == null ?
        ConfigurationAccessor.getPluginProperty(SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE,
            PluginConstants.MASTER_BRANCH_PROP) : baseVersionId.getValue().toString();
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH, itemId);
    repositoryPath = resolveTenantPath(context, repositoryPath);
    Git git = dao.openRepository(context, repositoryPath);
    createInt(context, git, baseBranchId, versionId.getValue().toString(), versionInfo);
    boolean commitRequired = storeItemVersionInfo(context, git, itemId, versionInfo);
    if (commitRequired) {
      dao.commit(context, git, PluginConstants.SAVE_ITEM_VERSION_MESSAGE);
    }
    dao.close(context, git);
  }


  protected void createInt(SessionContext context, Git git, String baseBranch,
                                      String branch, Info versionInfo) {
    GitSourceControlDao dao = getSourceControlDao(context);
    dao.createBranch(context, git, baseBranch, branch);

  }

  protected boolean storeItemVersionInfo(SessionContext context, Git git, Id itemId, Info info) {
    if (info == null) {
      return false;
    }

    addFileContent(
        context,
        git,
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH, itemId) +
            File.separator,
        ITEM_VERSION_INFO_FILE_NAME,
        info);
    return true;
  }


  public void save(SessionContext context, Id itemId, Id versionId, Info versionInfo) {
    String repositoryPath =
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH, itemId);
    repositoryPath = resolveTenantPath(context, repositoryPath);
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId.getValue().toString());
    boolean commitRequired = storeItemVersionInfo(context, git, itemId, versionInfo);
    if (commitRequired) {
      dao.commit(context, git, PluginConstants.SAVE_ITEM_VERSION_MESSAGE);
    }
    dao.close(context, git);
  }


  public void delete(SessionContext sessionContext, Id itemId, Id versionId) {
    /*todo*/
  }


  public PublishResult publish(SessionContext context, Id itemId, Id versionId,
                                          String message) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH, itemId);
    repositoryPath = resolveTenantPath(context, repositoryPath);
    Git git = dao.openRepository(context, repositoryPath);
    String branchId = versionId.getValue().toString();
    Collection<PushResult> pushResult = dao.publish(context, git, branchId);
    dao.checkoutBranch(context, git, branchId);
//    dao.inComing(context,git,versionId.getValue());
    dao.close(context, git);
    return  convertPushresultToPublishResult(pushResult);
  }

  protected PublishResult convertPushresultToPublishResult(Collection<PushResult> pushResult) {
    //todo - create publish result
    return null;
  }


  public CollaborationSyncResult sync(SessionContext context, Id itemId, Id versionId) {
    CollaborationSyncResult result;
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH, itemId);
    repositoryPath = resolveTenantPath(context, repositoryPath);
    Git git;
    String branch = versionId.getValue().toString();
    ObjectId oldId = null;
    if (FileUtils.exists(repositoryPath)) {
      git = dao.openRepository(context, repositoryPath);
      dao.checkoutBranch(context, git, branch);
      oldId = dao.getHead(context, git);
      PullResult syncResult = dao.sync(context, git, branch);
      result = sourceControlUtil.handleSyncResponse(context, git, syncResult);
    } else {
      String publicPath = resolveTenantPath(context, PluginConstants.PUBLIC_PATH);
      git = dao.clone(context,
          sourceControlUtil.getPublicRepositoryPath(context, publicPath, itemId),
          repositoryPath, branch);
      result = new CollaborationSyncResult();
    }
    Collection<CollaborationChangedElementData> changedElementDataCollection = sourceControlUtil
        .handleSyncFileDiff
            (context, dao, git, itemId, versionId, oldId);

    result.setCollaborationChangedElementDataCollection(changedElementDataCollection);
    dao.close(context, git);
    return result;
  }


  public CollaborationSyncResult merge(SessionContext context, Id itemId, Id
      versionId, Id sourceVersionId) {
    CollaborationSyncResult result;
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH, itemId);
    repositoryPath = resolveTenantPath(context, repositoryPath);
    Git git;
    String branch = versionId.getValue().toString();
    String sourceBranch = sourceVersionId.getValue().toString();
    ObjectId oldId = null;
    if (FileUtils.exists(repositoryPath)) {
      git = dao.openRepository(context, repositoryPath);
      dao.checkoutBranch(context, git, branch);
      oldId = dao.getHead(context, git);
      MergeResult mergeResult =
          dao.merge(context, git, sourceBranch, MergeCommand.FastForwardMode.FF);
      result = sourceControlUtil.handleMergeResponse(context, git, mergeResult);
    } else {
      String publicPath = resolveTenantPath(context, PluginConstants.PUBLIC_PATH);
      git = dao.clone(context,
          sourceControlUtil.getPublicRepositoryPath(context, publicPath, itemId),
          repositoryPath, branch);
      result = new CollaborationSyncResult();
    }

    Collection<CollaborationChangedElementData> changedElementDataCollection = sourceControlUtil
        .handleSyncFileDiff
            (context, dao, git, itemId, versionId, oldId);

    result.setCollaborationChangedElementDataCollection(changedElementDataCollection);

    dao.close(context, git);
    return result;
  }






}
