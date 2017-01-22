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

package org.amdocs.zusammen.plugin.collaborationstore.git.dao.impl;


import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.commands.RevisionDiffCommand;
import org.amdocs.zusammen.utils.fileutils.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GitSourceControlDaoImpl implements GitSourceControlDao {
  @Override
  public Git clone(SessionContext context, String source, String target, String... branch) {
    File targetRepositoryDir = FileUtils.getFile(target);
    try {
      Git git;
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
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void createBranch(SessionContext context, Git git, String baseBranch, String branch) {

    if(baseBranch!= null) checkoutBranch(context, git, baseBranch);
    //CheckoutCommand command = git.checkout();
    CreateBranchCommand command = git.branchCreate();

    try {
      //command.setCreateBranch(true);
      command.setName(branch);
      command.call();
/*
      StoredConfig config = git.getRepository().getConfig();
      config.setString( CONFIG_BRANCH_SECTION, branch, baseBranch, baseBranch );
      config.setString( CONFIG_BRANCH_SECTION, "branch", "merge",
          "refs/heads/"+baseBranch );
*/
      /*config.setString( CONFIG_REMOTE_SECTION, baseBranch, "url", git.getRepository().get );
      config.setString( CONFIG_REMOTE_SECTION, "remote-alias-name", "fetch", "ref-spec" );
      config.update();*/
    } catch (GitAPIException /*| IOException*/ e) {
      throw new RuntimeException(e);
    }


  }

  @Override
  public void checkoutBranch(SessionContext context, Git git, String branch) {
    if(branch == null) branch="master";
    try {
      if (branch.equals(git.getRepository().getBranch())) {
        return;
      }
      CheckoutCommand command = git.checkout();
      command.setName(branch);
      command.call();
    } catch (IOException | GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Git openRepository(SessionContext context, String repositoryPath) {
    File repositoryDir = new File(repositoryPath);

    try {
      return Git.open(repositoryDir);
    } catch (IOException e) {
      throw new RuntimeException(e);
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
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
    return filesAdded;
  }

  @Override
  public void delete(SessionContext context, Git git, String... files) {

    if(files != null && files.length>0){
      RmCommand command = git.rm();
      for(String file:files){
        command.addFilepattern(file);
      }

      try {
        DirCache ret = command.call();
      } catch (GitAPIException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public RevCommit commit(SessionContext context, Git git, String message) {
    CommitCommand command = git.commit();

    try {
      command.setMessage(message);
      return command.call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void resetMerge(SessionContext context, Git git){
    ResetCommand command = git.reset();

    try {
      command.setMode(ResetCommand.ResetType.MERGE);
      command.call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
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
      for(PushResult result:pushResults){
        results.add(result);
      }
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
    return results;
  }

  @Override
  public PullResult sync(SessionContext context, Git git, String branchId) {


    PullCommand command = git.pull();

    try {
      command.setRemoteBranchName(branchId);
      //command.setRebase(true);
      command.setStrategy(MergeStrategy.RESOLVE);
      return command.call();

    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public MergeResult merge(SessionContext context, Git git, String branchId, MergeCommand.FastForwardMode mode) {
    MergeCommand command = git.merge();
    try {
      command.include(git.getRepository().findRef(branchId));
      command.setFastForward(mode);
      return command.call();
    } catch (GitAPIException | IOException e) {
      throw new RuntimeException(e);
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
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PullResult inComing(SessionContext context, Git git, String branch) {
    PullCommand command = git.pull();
    FetchCommand fetchCommand = git.fetch();
    PushCommand pushCommand = git.push();
    //fetchCommand.setRemote("origin").setDryRun(true).call().getTrackingRefUpdate(git
    //    .getRepository().getBranch()).getResult().
    try {
      command.setRemoteBranchName(branch);
      return command.call();

    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Iterable<PushResult> outGoing(SessionContext context, Git git, String branch) {
    PushCommand command = git.push();

    try {
      command.setDryRun(true);
      return command.call();

    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Status status(SessionContext context, Git git){
    try {
      return git.status().call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Collection<DiffEntry> revisionDiff(SessionContext context, Git git, ObjectId from, ObjectId to) {

    RevisionDiffCommand command = RevisionDiffCommand.init(git);
    try {
      command.from(from);
      command.to(to);
      return command.call();

    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ObjectId getHead(SessionContext context, Git git) {
    try {
      return git.getRepository().resolve(Constants.HEAD);

    }catch (IOException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public ObjectId getRemoteHead(SessionContext context, Git git) {
    try {
      return git.getRepository().exactRef(Constants.R_HEADS+"/"+git.getRepository().getBranch())
          .getObjectId();

    }catch (IOException e){
      throw new RuntimeException(e);
    }
  }




}
