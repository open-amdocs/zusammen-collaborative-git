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


import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.GitSourceControlDao;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
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
import org.eclipse.jgit.dircache.DirCache;
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

    checkoutBranch(context, git, baseBranch);
    CheckoutCommand command = git.checkout();
    //CreateBranchCommand command = git.branchCreate();

    try {
      command.setCreateBranch(true);
      command.setName(branch);
      command.call();
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void checkoutBranch(SessionContext context, Git git, String branch) {
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
  public List<File> add(SessionContext context, Git git, File... files) {
    AddCommand command = git.add();
    List<File> filesAdded = new ArrayList<>();
    try {
      for (File file : files) {
        command.addFilepattern(file.getName());

        command.call();
        filesAdded.add(file);
      }
    } catch (GitAPIException e) {
      e.printStackTrace();
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
        e.printStackTrace();
      }
    }
  }

  @Override
  public void commit(SessionContext context, Git git, String message) {
    CommitCommand command = git.commit();

    try {
      command.setMessage(message);
      command.call();
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
      command.setRefSpecs(new RefSpec(branchId));
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
  public void fetch(SessionContext contaxt, Git git, String branch) {
    FetchCommand command = git.fetch();
    try {
      if (branch != null) {
        command.setRefSpecs(new RefSpec(branch));
      }
      command.call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PullResult inComing(SessionContext context, Git git, String branch) {
    PullCommand command = git.pull();
    FetchCommand fetchCommand = git.fetch();
    PushCommand pushCommand = git.push();

    try {

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

}
