package org.amdocs.tsuzammen.plugin.collaborationstore.git.impl;

import org.amdocs.tsuzammen.commons.datatypes.Id;
import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.GitSourceControlDao;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
  public void createBranch(SessionContext context, Git git, Id baseBranch, Id branch) {

    checkoutBranch(context, git, baseBranch.getValue());
    CheckoutCommand command = git.checkout();
    //CreateBranchCommand command = git.branchCreate();

    try {
      command.setCreateBranch(true);
      command.setName(branch.getValue());
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
  public List<File> delete(SessionContext context, Git git, File... files) {
    return null;
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
  public void publish(SessionContext context, Git git, Id branchId) {

    PushCommand command = git.push();
    try {
      command.setRefSpecs(new RefSpec(branchId.getValue()));
      command.call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PullResult sync(SessionContext context, Git git, Id branchId) {

    PullCommand command = git.pull();
    try {
      command.setRemoteBranchName(branchId.getValue());
      return command.call();
    } catch (GitAPIException e) {
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


}
