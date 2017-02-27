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

package org.amdocs.zusammen.plugin.collaborationstore.git.dao.impl;


import org.amdocs.zusammen.commons.log.ZusammenLogger;
import org.amdocs.zusammen.commons.log.ZusammenLoggerFactory;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.response.ErrorCode;
import org.amdocs.zusammen.datatypes.response.Module;
import org.amdocs.zusammen.datatypes.response.ReturnCode;
import org.amdocs.zusammen.datatypes.response.ZusammenException;
import org.amdocs.zusammen.plugin.collaborationstore.git.commands.RevisionDiffCommand;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.utils.fileutils.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GitSourceControlDaoImpl implements GitSourceControlDao {


  private static ZusammenLogger logger = ZusammenLoggerFactory.getLogger
      (GitSourceControlDaoImpl.class.getName());
  @Override
  public Git clone(SessionContext context, String source, String target, String... branch) {
    File targetRepositoryDir = FileUtils.getFile(target);
    try {
      CloneCommand command = Git.cloneRepository();
      command.setURI(source);
      command.setDirectory(targetRepositoryDir);
      //command.setCredentialsProvider(CredentialsProvider.getCredentialsProvider(context));
      if (branch != null && branch.length == 1) {
        command.setBranch(branch[0]);
      } else if (branch != null) {
        command.setBranchesToClone(Arrays.asList(branch));
      } else {
        command.setCloneAllBranches(true);
      }
      command.setBare(false);
      return command.call();
    } catch (GitAPIException gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_CLONE,Module.ZCSP,gae.getMessage(),null);
      logger.error(returnCode.toString());

      throw new RuntimeException(gae.getMessage());
    }
  }

  @Override
  public void createBranch(SessionContext context, Git git, String baseBranch, String branch) {

    if (baseBranch != null) {
      checkoutBranch(context, git, baseBranch);
    }
    CreateBranchCommand command = git.branchCreate();

    try {

      command.setName(branch);
      command.call();

    } catch (GitAPIException /*| IOException*/ gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_CREATE_BRANCH,Module.ZCSP,gae
          .getMessage(),null);
      logger.error(returnCode.toString());

      throw new RuntimeException(gae.getMessage());
    }


  }

  @Override
  public boolean checkoutBranch(SessionContext context, Git git, String branch) {
    if (branch == null) {
      branch = "master";
    }
    try {
      if (branch.equals(git.getRepository().getBranch())) {
        return true;
      }
      CheckoutCommand command = git.checkout();
      command.setName(branch);
      command.call();
    } catch (RefNotFoundException noSuchBranch) {
      return false;
    } catch (IOException | GitAPIException gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_CHECKOUT_BRANCH,Module.ZCSP,gae
          .getMessage(),null);
      logger.error(returnCode.toString());

      throw new RuntimeException(gae.getMessage());
    }
    return true;
  }

  @Override
  public Git openRepository(SessionContext context, String repositoryPath) {
    File repositoryDir = new File(repositoryPath);

    try {
      return Git.open(repositoryDir);
    } catch (IOException ioe) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_OPEN,Module.ZCSP,ioe
          .getMessage(),null);
      logger.error(returnCode.toString());

      throw new RuntimeException(ioe.getMessage());
    }

  }

  @Override
  public List<String> add(SessionContext context, Git git, String... files) {
    AddCommand command = git.add();
    List<String> filesAdded = new ArrayList<>();
    try {
      for (String file : files) {
        command.addFilepattern(file);

        command.call();
        filesAdded.add(file);
      }
    } catch (GitAPIException gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_ADD,Module.ZCSP,gae
          .getMessage(),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(gae.getMessage());
    }
    return filesAdded;
  }

  @Override
  public void delete(SessionContext context, Git git, String... files) {

    if (files != null && files.length > 0) {
      RmCommand command = git.rm();
      for (String file : files) {
        command.addFilepattern(file);
      }

      try {
        DirCache ret = command.call();
      } catch (GitAPIException gae) {
        ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_DELETE, Module.ZCSP, gae.getMessage
            (),null);
        logger.error(returnCode.toString());
        throw new RuntimeException(gae.getMessage());
      }
    }
  }

  @Override
  public RevCommit commit(SessionContext context, Git git, String message) {
    CommitCommand command = git.commit();

    try {
      command.setMessage(message);
      command.setAuthor(context.getUser().getUserName(), "zusammen@amdocs.com");
      return command.call();
    } catch (GitAPIException gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_COMMIT, Module.ZCSP, gae.getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(gae.getMessage());
    }
  }

  @Override
  public void resetMerge(SessionContext context, Git git) {
    ResetCommand command = git.reset();

    try {
      command.setMode(ResetCommand.ResetType.MERGE);
      command.call();
    } catch (GitAPIException gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_RESET_MERGE, Module.ZCSP, gae.getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(gae.getMessage());
    }
  }

  @Override
  public Collection<PushResult> publish(SessionContext context, Git git, String branchId) {
    Collection<PushResult> results = new ArrayList<>();
    PushCommand command = git.push();
    try {
      RefSpec refSpec = new RefSpec(branchId);

      command.setRefSpecs(refSpec);
      Iterable<PushResult> pushResults = command.call();
      for (PushResult result : pushResults) {
        results.add(result);
      }
    } catch (GitAPIException gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_PUBLISH, Module.ZCSP, gae.getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(gae.getMessage());
    }
    return results;
  }

  @Override
  public PullResult sync(SessionContext context, Git git, String branchId) {


    PullCommand command = git.pull();

    try {
      command.setRemoteBranchName(branchId);
      command.setStrategy(MergeStrategy.RESOLVE);
      PullResult result = command.call();

      return result;

    } catch (GitAPIException gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_SYNC, Module.ZCSP, gae.getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(gae.getMessage());
    }

  }

  @Override
  public MergeResult merge(SessionContext context, Git git, String branchId,
                           MergeCommand.FastForwardMode fastForwardMode,
                           MergeStrategy mergeStrategy, String message) {
    MergeCommand command = git.merge();
    try {
      command.include(git.getRepository().findRef(branchId));
      if (fastForwardMode != null) {
        command.setFastForward(fastForwardMode);
      }
      if (mergeStrategy != null) {
        command.setStrategy(mergeStrategy);
      }
      if (message != null) {
        command.setMessage(message);
      }
      return command.call();
    } catch (GitAPIException | IOException ex) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_MERGE, Module.ZCSP, ex.getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(ex.getMessage());
    }
  }

  @Override
  public void close(SessionContext context, Git git) {
    git.close();
  }

  @Override
  public FetchResult fetch(SessionContext contaxt, Git git, String branch) {
    FetchCommand command = git.fetch();
    try {
      if (branch != null) {
        command.setRefSpecs(new RefSpec(branch));

      }
      return command.call();
    } catch (GitAPIException gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_CLOSE, Module.ZCSP, gae.getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(gae.getMessage());
    }
  }

  @Override
  public Status status(SessionContext context, Git git) {
    try {
      return git.status().call();
    } catch (GitAPIException gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_STATUS, Module.ZCSP, gae.getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(gae.getMessage());
    }
  }

  @Override
  public Collection<DiffEntry> revisionDiff(SessionContext context, Git git, ObjectId from,
                                            ObjectId to, TreeFilter treeFilter) {

    RevisionDiffCommand command = RevisionDiffCommand.init(git);
    try {
      command.from(from);
      command.to(to);
      if (treeFilter != null) {
        command.filter(treeFilter);
      }
      return command.call();

    } catch (GitAPIException gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_REV_DIFF, Module.ZCSP, gae.getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(gae.getMessage());
    }
  }

  @Override
  public ObjectId getHead(SessionContext context, Git git) {
    try {
      return git.getRepository().resolve(Constants.HEAD);

    } catch (IOException ioe) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_GET_HEAD, Module.ZCSP, ioe.getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(ioe.getMessage());
    }
  }

  @Override
  public ObjectId getRemoteHead(SessionContext context, Git git) {
    try {
      return git.getRepository().exactRef(Constants.R_HEADS + "/" + git.getRepository().getBranch())
          .getObjectId();

    } catch (IOException ioe) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_GET_REMOTE_HEAD, Module.ZCSP, ioe
          .getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(ioe.getMessage());
    }
  }

  @Override
  public void revert(SessionContext context, Git git, ObjectId revisionId) {
    ResetCommand command = git.reset();
    try {
      command.setRef(revisionId.getName());
      command.setMode(ResetCommand.ResetType.HARD);
      command.call();
    } catch (GitAPIException gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_REVERT, Module.ZCSP, gae.getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(gae.getMessage());
    }

  }

  @Override
  public Collection<String> getBranchFileList(SessionContext context, Git git) {

    Collection<String> files = new ArrayList<>();

    try {
      RevWalk walk = new RevWalk(git.getRepository());
      Ref head = git.getRepository().getRef("HEAD");
      RevCommit commit = walk.parseCommit(head.getObjectId());
      RevTree tree = commit.getTree();

      TreeWalk treeWalk = new TreeWalk(git.getRepository());

      treeWalk.addTree(tree);
      treeWalk.setRecursive(true);
      while (treeWalk.next()) {

        files.add(treeWalk.getPathString());
      }
      return files;

    } catch (IOException ioe) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_GET_BRANCH_FILE_LIST, Module.ZCSP, ioe
          .getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(ioe.getMessage());
    }
  }

  @Override
  public Iterable<RevCommit> listHistory(SessionContext context, Git git) {
    LogCommand command = git.log();
    try {
      return command.call();
    } catch (GitAPIException gae) {
      ReturnCode returnCode = new ReturnCode(GitErrorCode.GI_LIST_HISTORY, Module.ZCSP, gae.getMessage
          (),null);
      logger.error(returnCode.toString());
      throw new RuntimeException(gae.getMessage());
    }
  }
}
