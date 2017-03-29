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

package org.amdocs.zusammen.plugin.collaborationstore.impl;

import org.amdocs.zusammen.commons.configuration.impl.ConfigurationAccessor;
import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.item.Action;
import org.amdocs.zusammen.datatypes.item.ElementContext;
import org.amdocs.zusammen.datatypes.item.ItemVersionData;
import org.amdocs.zusammen.datatypes.item.ItemVersionDataConflict;
import org.amdocs.zusammen.datatypes.itemversion.Change;
import org.amdocs.zusammen.datatypes.itemversion.ItemVersionHistory;
import org.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDaoFactory;
import org.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants;
import org.amdocs.zusammen.plugin.collaborationstore.types.CollaborationConflictResult;
import org.amdocs.zusammen.plugin.collaborationstore.types.CollaborationDiffResult;
import org.amdocs.zusammen.plugin.collaborationstore.types.CollaborationSyncResult;
import org.amdocs.zusammen.plugin.collaborationstore.types.ElementRawData;
import org.amdocs.zusammen.plugin.collaborationstore.types.ItemVersionRawData;
import org.amdocs.zusammen.plugin.collaborationstore.types.Repository;
import org.amdocs.zusammen.sdk.SdkConstants;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationElementConflict;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeConflict;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeResult;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationPublishResult;
import org.amdocs.zusammen.utils.fileutils.FileUtils;
import org.amdocs.zusammen.utils.fileutils.json.JsonUtil;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants.ITEM_VERSION_INFO_FILE_NAME;
import static org.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants.RELATIONS_FILE_NAME;
import static org.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants.ZUSAMMEN_TAGGING_FILE_NAME;

public class ItemVersionCollaborationStore extends CollaborationStore {

  public ItemVersionCollaborationStore(SourceControlDaoFactory sourceControlDaoFactory) {
    this.sourceControlDaoFactory = sourceControlDaoFactory;
  }

  public void create(SessionContext context, Id itemId, Id baseVersionId, Id versionId,
                     ItemVersionData itemVersionData) {
    String baseBranchId = baseVersionId == null ?
        ConfigurationAccessor.getPluginProperty(SdkConstants.ZUSAMMEN_COLLABORATIVE_STORE,
            PluginConstants.MASTER_BRANCH_PROP) : baseVersionId.toString();
    SourceControlDao dao = getSourceControlDao(context);
    Repository repository = dao.initRepository(context, itemId);
    dao.createBranch(context, repository, baseBranchId, versionId);
    dao.checkoutBranch(context, repository, versionId);
    Collection<String> files = storeItemVersionData(context, itemId, versionId, dao
        .getRepositoryLocation(context,
        repository),
        itemVersionData,
        Action.CREATE);
    dao.store(context, repository,files);
    dao.commit(context, repository, PluginConstants.SAVE_ITEM_VERSION_MESSAGE);

    dao.close(context, repository);
  }

  protected Collection<String> storeItemVersionData(SessionContext context, Id itemId, Id versionId,
                                         String rootPath, ItemVersionData itemVersionData, Action
                                             action) {
    List<String> files  =new ArrayList<>();
    if (itemVersionData == null || (itemVersionData.getInfo() == null && itemVersionData
        .getRelations() == null)) {
      return files;
    }
    if (action.equals(Action.CREATE)) {
      if(storeZusammenTaggingInfo(context, rootPath, itemId, versionId))
        files.add(PluginConstants.ZUSAMMEN_TAGGING_FILE_NAME);
    }
    if (itemVersionData.getInfo() != null) {
      if(storeData(context, itemId, ITEM_VERSION_INFO_FILE_NAME, itemVersionData
          .getInfo()))
      files.add(PluginConstants.ITEM_VERSION_INFO_FILE_NAME);
    }
    if (itemVersionData.getRelations() != null && itemVersionData.getRelations().size() > 0) {
      if(storeData(context, itemId, RELATIONS_FILE_NAME, itemVersionData.getRelations()))
        files.add(PluginConstants.RELATIONS_FILE_NAME);
    }

    return files;
  }


  private boolean storeZusammenTaggingInfo(SessionContext context, String path, Id itemId, Id
      versionId) {

    Optional<InputStream> is = FileUtils
        .readFile(path, ZUSAMMEN_TAGGING_FILE_NAME);
    String baseId;
    Map<String, String> itemVersionInformation;
    if (is.isPresent()) {
      itemVersionInformation = JsonUtil.json2Object(is.get(), Map.class);
      baseId = itemVersionInformation.get(PluginConstants.ITEM_VERSION_ID);
    } else {
      itemVersionInformation = new HashMap<>();
      baseId = null;
    }
    itemVersionInformation.put(PluginConstants.ITEM_VERSION_ID, versionId.getValue());
    itemVersionInformation.put(PluginConstants.ITEM_VERSION_BASE_ID, baseId);
    return storeData(context, itemId, ZUSAMMEN_TAGGING_FILE_NAME, itemVersionInformation);
  }


  protected boolean storeData(SessionContext context, Id itemId, String fileName,
                              Object data) {
    return addFileContent(
        context,

        getSourceControlUtil().getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH,
            itemId) +
            File.separator,
        null,
        fileName,
        data);

  }


  public void save(SessionContext context, Id itemId, Id versionId,
                   ItemVersionData itemVersionData) {

    SourceControlDao dao = getSourceControlDao(context);
    Repository repository = dao.initRepository(context, itemId);
    dao.checkoutBranch(context, repository, versionId);
    Collection<String> files = storeItemVersionData(context, itemId, versionId, dao
            .getRepositoryLocation(context, repository),
        itemVersionData,
        Action.UPDATE);
    if (files!=null && files.size()>0) {
      dao.store(context, repository, files);
      dao.commit(context, repository, PluginConstants.SAVE_ITEM_VERSION_MESSAGE);
    }
    dao.close(context, repository);

  }


  public void delete(SessionContext sessionContext, Id itemId, Id versionId) {
    /*todo*/
  }


  public CollaborationPublishResult publish(SessionContext context, Id itemId, Id versionId,
                                            String message) {
    SourceControlDao dao = getSourceControlDao(context);
    Repository repository = dao.initRepository(context, itemId);
    CollaborationDiffResult collaborationDiffResult = dao.publish(context, repository);
    CollaborationMergeChange change = getSourceControlUtil()
        .loadFileDiffElements(context, dao.getRepositoryLocation(context, repository), itemId,
            versionId,
            collaborationDiffResult);
    CollaborationPublishResult collaborationPublishResult = new CollaborationPublishResult();
    collaborationPublishResult.setChange(change);
    dao.close(context, repository);
    return collaborationPublishResult;
  }

  public CollaborationMergeResult sync(SessionContext context, Id itemId, Id versionId) {
    CollaborationMergeResult collaborationMergeResult = new CollaborationMergeResult();
    SourceControlDao dao = getSourceControlDao(context);
    String repositoryPath = getSourceControlUtil().getPrivateRepositoryPath(context, PluginConstants
        .PRIVATE_PATH, itemId);
    Repository repository;
    if (FileUtils.exists(repositoryPath)) {
      repository = dao.initRepository(context, itemId);

      if (dao.checkoutBranch(context, repository, versionId)) {
        CollaborationSyncResult collaborationSyncResult = dao.sync(context, repository, versionId);
        collaborationMergeResult = handleSyncResult(context, itemId, versionId,
            repositoryPath, collaborationSyncResult);
      }
    } else {
      repository = dao.cloneRepository(context, getSourceControlUtil()
          .getPublicRepositoryPath(context,
              PluginConstants
                  .PUBLIC_PATH, itemId), repositoryPath, versionId);
      CollaborationDiffResult collaborationDiffResult =
          getSourceControlUtil().getRepoFiles(context, repository);
      CollaborationMergeChange change = getSourceControlUtil()
          .loadFileDiffElements(context, repositoryPath, itemId,
              versionId, collaborationDiffResult);
      collaborationMergeResult.setChange(change);
    }
    return collaborationMergeResult;
  }

  private CollaborationMergeResult handleSyncResult(SessionContext context, Id itemId, Id versionId,
                                                    String repositoryPath,
                                                    CollaborationSyncResult collaborationSyncResult) {
    CollaborationMergeResult collaborationMergeResult = new CollaborationMergeResult();
    if (collaborationSyncResult.isSuccessful()) {
      CollaborationMergeChange change = getSourceControlUtil()
          .loadFileDiffElements(context, getSourceControlUtil().getPrivateRepositoryPath(context,
              PluginConstants.PRIVATE_PATH, itemId), itemId,
              versionId,
              collaborationSyncResult.getCollaborationDiffResult());
      collaborationMergeResult.setChange(change);

    } else {
      CollaborationMergeConflict conflicts = new CollaborationMergeConflict();

      CollaborationConflictResult collaborationConflictResult = collaborationSyncResult
          .getCollaborationConflictResult();

      if (collaborationConflictResult.getConflictingElementList() != null &&
          collaborationConflictResult.getConflictingElementList().size() > 0) {

        Set<String> elementList = collaborationConflictResult.getConflictingElementList();

        Map<String, ElementRawData> elementRawDataMap = new HashMap<>();
        elementList.stream()
            .forEach(elementId -> elementRawDataMap.put(elementId, getSourceControlUtil()
                .uploadRawElementData(context,
                    repositoryPath,
                    getSourceControlUtil()
                        .extractElementPathFromFilePath(collaborationConflictResult
                            .getConflictingFilesByElementId
                                (elementId).get(0)))));

        Collection<CollaborationElementConflict> elementConflicts = getSourceControlUtil()
            .resolveSyncElementConflicts(context, itemId, versionId, elementRawDataMap,
                collaborationConflictResult);
        conflicts.setElementConflicts(elementConflicts);
      }
      if (collaborationConflictResult.getItemVersionConflictFiles() != null) {

        ItemVersionRawData itemVersionRawData = resolveItemVersionConflicts(context,
            repositoryPath);

        ItemVersionDataConflict itemVersionConflict =
            getSourceControlUtil().resolveSyncItemVersionConflicts(context,
                itemVersionRawData,
                collaborationConflictResult.getItemVersionConflictFiles());
        conflicts.setVersionDataConflict(itemVersionConflict);
      }
      collaborationMergeResult.setConflict(conflicts);
    }
    return collaborationMergeResult;
  }

  private ItemVersionRawData resolveItemVersionConflicts(SessionContext context,
                                                         String repositoryPath) {
    return getSourceControlUtil().uploadRawItemVersionData(context, repositoryPath);
  }


  public CollaborationMergeResult merge(SessionContext context, Id itemId, Id
      versionId, Id sourceVersionId) {
    CollaborationMergeResult result = new CollaborationMergeResult();

    SourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        getSourceControlUtil()
            .getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH, itemId);
    Repository repository;
    if (FileUtils.exists(repositoryPath)) {
      repository = dao.initRepository(context, itemId);
      CollaborationSyncResult collaborationSyncResult = dao.merge(context, repository,
          sourceVersionId);
      result = handleSyncResult(context, itemId, versionId,
          repositoryPath, collaborationSyncResult);
    } else {
      repository = dao.cloneRepository(context, getSourceControlUtil()
          .getPublicRepositoryPath(context,
              PluginConstants
                  .PUBLIC_PATH, itemId), repositoryPath, versionId);
      CollaborationDiffResult collaborationDiffResult =
          getSourceControlUtil().getRepoFiles(context, repository);
      CollaborationMergeChange change = getSourceControlUtil()
          .loadFileDiffElements(context, repositoryPath, itemId,
              versionId, collaborationDiffResult);
      result.setChange(change);
    }

    dao.close(context, repository);
    return result;
  }


  public CollaborationMergeChange resetHistory(SessionContext context,
                                               ElementContext elementContext,


                                               Id changeId) {
    SourceControlDao dao = getSourceControlDao(context);
    Repository repository = dao.initRepository(context, elementContext.getItemId());

    CollaborationDiffResult collaborationDiffResult = dao.reset(context, repository, changeId);
    CollaborationMergeChange changes = getSourceControlUtil()
        .loadFileDiffElements(context, getSourceControlUtil().getPrivateRepositoryPath(context,
            PluginConstants.PRIVATE_PATH, elementContext.getItemId()),
            elementContext.getItemId(),
            elementContext.getVersionId(), collaborationDiffResult);
    return changes;
  }

 /* private Git init(SessionContext context, Id itemId, Id versionId, GitSourceControlDao dao) {
    String repositoryPath =
        getSourceControlUtil().getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH,
            itemId);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId.getValue());
    return git;
  }*/

  public ItemVersionHistory listHistory(SessionContext context, Id itemId, Id versionId) {
    SourceControlDao dao = getSourceControlDao(context);
    Repository repository = dao.initRepository(context, itemId);
    List<Change> commitIdList = dao.listRevisionHistory(context, repository,versionId);
    ItemVersionHistory itemVersionHistory = new ItemVersionHistory();

    for (Change change : commitIdList) {
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
