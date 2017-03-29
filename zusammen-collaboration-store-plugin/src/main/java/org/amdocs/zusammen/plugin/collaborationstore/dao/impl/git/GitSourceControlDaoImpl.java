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

package org.amdocs.zusammen.plugin.collaborationstore.dao.impl.git;

import org.amdocs.zusammen.commons.log.ZusammenLogger;
import org.amdocs.zusammen.commons.log.ZusammenLoggerFactory;
import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.itemversion.Change;
import org.amdocs.zusammen.datatypes.response.Module;
import org.amdocs.zusammen.datatypes.response.ReturnCode;
import org.amdocs.zusammen.datatypes.response.ZusammenException;
import org.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlCommandFactory;
import org.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.dao.api.git.GitSourceControlCommand;
import org.amdocs.zusammen.plugin.collaborationstore.dao.util.SourceControlUtil;
import org.amdocs.zusammen.plugin.collaborationstore.types.CollaborationConflictResult;
import org.amdocs.zusammen.plugin.collaborationstore.types.CollaborationDiffResult;
import org.amdocs.zusammen.plugin.collaborationstore.types.CollaborationSyncResult;
import org.amdocs.zusammen.plugin.collaborationstore.types.LocalRemoteDataConflict;
import org.amdocs.zusammen.plugin.collaborationstore.types.Repository;
import org.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GitSourceControlDaoImpl implements SourceControlDao<Git> {
  private SourceControlUtil sourceControlUtil;
  private GitSourceControlUtil gitSourceControlUtil;

  private static ZusammenLogger logger = ZusammenLoggerFactory.getLogger
      (GitSourceControlDaoImpl.class.getName());


  @Override
  public Repository<Git> initRepository(SessionContext context, Id itemId) {

    String repositoryPath =
        getSourceControlUtil().getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH,
            itemId);
    Git git = getSourceControlCommand(context).openRepository
        (context, repositoryPath);
    Repository<Git> repository = new Repository<>();
    repository.setRepository(git);
    return repository;
  }

  private GitSourceControlCommand getSourceControlCommand(SessionContext context) {

    return SourceControlCommandFactory.getInstance().createInterface(context,
        SourceControlCommandFactory.GIT);
  }

  @Override
  public CollaborationDiffResult publish(SessionContext context,
                                         Repository<Git> repository) {
    String branch;
    try {
      branch = getSourceControlCommand(context).getBranch(context, repository.getRepository());
      getSourceControlCommand(context).publish(context, repository.getRepository(), branch);
    } catch (IOException ioe) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_GET_BRANCH, Module.ZCSP, ioe
          .getMessage(), null);
      logger.error(returnCode.toString(), ioe);
      throw new ZusammenException(returnCode);
    }

    return getSourceControlCommand(context).publish(context, repository.getRepository(), branch);
  }

  @Override
  public CollaborationSyncResult sync(SessionContext context, Repository<Git> repository,
                                      Id versionId) {

    CollaborationSyncResult collaborationSyncResult = new CollaborationSyncResult();
    ObjectId previousRevisionId = getSourceControlCommand(context).getHead(context, repository
        .getRepository());
    PullResult pullResult =
        getSourceControlCommand(context).sync(context, repository.getRepository(), versionId
            .getValue());
    String elementId;

    if (pullResult != null && pullResult.isSuccessful()) {
      ObjectId currentRevisionId = getSourceControlCommand(context).getHead(context, repository
          .getRepository());
      CollaborationDiffResult collaborationDiffResult;
      if (previousRevisionId != null) {
        Collection diff = getSourceControlCommand(context)
            .diff(context, repository.getRepository(), previousRevisionId, currentRevisionId);
        collaborationDiffResult =
            getGitSourceControlUtil().getFileDiff(diff);
      } else {
        collaborationDiffResult = getSourceControlUtil().getRepoFiles(context, repository);
      }
      collaborationSyncResult.setCollaborationDiffResult(collaborationDiffResult);
    } else {
      CollaborationConflictResult collaborationConflictResult = new CollaborationConflictResult();
      for (String file : pullResult.getMergeResult().getConflicts().keySet()) {
        elementId = getSourceControlUtil().extractIdFromFilePath(file);
        if (elementId == null) {
          collaborationConflictResult.addItemVersionFile(file);
        } else {
          //elementPath = getSourceControlUtil().extractElementPathFromFilePath(file);
          collaborationConflictResult.addElementFile(elementId, file.replace("/", File.separator));
        }


      }
      collaborationSyncResult.setCollaborationConflictResult(collaborationConflictResult);
      getSourceControlCommand(context)
          .reset(context, repository.getRepository(), previousRevisionId);
    }

    return collaborationSyncResult;
  }


  @Override
  public CollaborationSyncResult merge(SessionContext context, Repository<Git> repository, Id
      sourceVersionId) {

    CollaborationSyncResult collaborationSyncResult = new CollaborationSyncResult();
    ObjectId previousRevisionId = getSourceControlCommand(context).getHead(context, repository
        .getRepository());

    MergeResult result =
        getSourceControlCommand(context).merge(context, repository.getRepository(),
            sourceVersionId
                .getValue(), MergeCommand.FastForwardMode.FF, null, "merge branches.");
    if (result != null && result.getConflicts() == null) {
      ObjectId currentRevisionId = getSourceControlCommand(context).getHead(context, repository
          .getRepository());
      CollaborationDiffResult collaborationDiffResult;
      if (previousRevisionId != null) {

        Collection diff = getSourceControlCommand(context)
            .diff(context, repository.getRepository(), previousRevisionId, currentRevisionId);
        collaborationDiffResult =
            getGitSourceControlUtil().getFileDiff(diff);
      } else {
        collaborationDiffResult = getSourceControlUtil().getRepoFiles(context, repository);
      }
      collaborationSyncResult.setCollaborationDiffResult(collaborationDiffResult);
    } else {
      CollaborationConflictResult collaborationConflictResult = new CollaborationConflictResult();
      String elementId;
      for (String file : result.getConflicts().keySet()) {
        elementId = getSourceControlUtil().extractIdFromFilePath(file);
        if (elementId == null) {
          collaborationConflictResult.addItemVersionFile(file);
        } else {
          //elementPath = getSourceControlUtil().extractElementPathFromFilePath(file);
          collaborationConflictResult
              .addElementFile(elementId, file.replace("/", File.separator));
        }

      }
      collaborationSyncResult.setCollaborationConflictResult(collaborationConflictResult);
    }
    return collaborationSyncResult;
  }

  @Override
  public void delete(SessionContext context, Repository<Git> repository, String fullPath) {
    getSourceControlCommand(context).delete(context, repository.getRepository(), fullPath);
  }

  @Override
  public CollaborationDiffResult reset(SessionContext context, Repository<Git> repository,
                                       Id changeId) {


    ObjectId revId = ObjectId.fromString(changeId.getValue());


    Collection<DiffEntry> resetResult =
        getSourceControlCommand(context).diff(context, repository.getRepository(), revId, null);
    getSourceControlCommand(context).reset(context, repository.getRepository(),
        revId);
    CollaborationDiffResult collaborationDiffResult = getGitSourceControlUtil().getFileDiff
        (resetResult);
    return collaborationDiffResult;


  }

  @Override
  public List<Change> listRevisionHistory(SessionContext context, Repository<Git> repository,
                                          Id versionId) {
    List<Change> changeList = new ArrayList<>();
    Iterable<RevCommit> listRev = getSourceControlCommand(context).listRevisionList(repository
            .getRepository(),
        versionId);

    for (RevCommit rev : listRev) {
      changeList.add(getChange(rev));
    }
    return changeList;
  }

  @Override
  public LocalRemoteDataConflict splitFileContentConflict(SessionContext context, byte[] data) {

    return GitConflictFileSplitter.splitMergedFile(data);
  }


  @Override
  public void close(SessionContext context, Repository<Git> repository) {
    getSourceControlCommand(context).close(context, repository.getRepository());
  }

  @Override
  public String getRepositoryLocation(SessionContext context, Repository<Git> repository) {
    String path = repository.getRepository().getRepository().getWorkTree().getAbsolutePath();
    String tenant = context.getTenant() != null ? context.getTenant() : "";
    return path.replace("{tenant}", context.getTenant());

  }

  @Override
  public void createBranch(SessionContext context, Repository<Git> repository, String
      baseBranchId,
                           Id versionId) {
    getSourceControlCommand(context).createBranch(context, repository.getRepository()
        , baseBranchId, versionId.getValue());
  }

  @Override
  public boolean checkoutBranch(SessionContext context, Repository<Git> repository, Id
      versionId) {
    return getSourceControlCommand(context).checkoutBranch(context, repository.getRepository(),
        versionId
            .getValue());
  }

  @Override
  public void  store(SessionContext context, Repository<Git> repository, Collection<String> files) {
    getSourceControlCommand(context).add(context, repository.getRepository(), files);
  }

  @Override
  public void commit(SessionContext context, Repository<Git> repository, String message) {
    getSourceControlCommand(context).commit(context, repository.getRepository(), message);
  }

  @Override
  public Repository<Git> cloneRepository(SessionContext context, String source, String target,
                                         Id branch) {
    Repository<Git> repository = new Repository<>();
    Git git = getSourceControlCommand(context).clone(context, source, target, branch.getValue());
    repository.setRepository(git);
    return repository;
  }

  @Override
  public Collection<String> getBranchFileList(SessionContext
                                                  context, Repository<Git> repository) {

    return getSourceControlCommand(context).getBranchFileList(context, repository
        .getRepository());
  }


  private SourceControlUtil getSourceControlUtil() {
    if (this.sourceControlUtil == null) {
      this.sourceControlUtil = new SourceControlUtil();
    }
    return this.sourceControlUtil;

  }

  private GitSourceControlUtil getGitSourceControlUtil() {
    if (this.gitSourceControlUtil == null) {
      this.gitSourceControlUtil = new GitSourceControlUtil();
    }
    return this.gitSourceControlUtil;

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
