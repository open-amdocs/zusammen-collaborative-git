package org.amdocs.tsuzammen.plugin.collaborationstore.git;


import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.transport.PushResult;

import java.io.File;
import java.util.List;

public interface GitSourceControlDao {

  Git clone(SessionContext context, String source, String target, String... branch);

  void createBranch(SessionContext context, Git git, String baseBranch, String branch);

  void checkoutBranch(SessionContext context, Git git, String branch);


  Git openRepository(SessionContext context, String repositoryPath);

  List<File> add(SessionContext context, Git git, File... files);

  void delete(SessionContext context, Git git, String... files);

  void commit(SessionContext context, Git git, String message);

  void publish(SessionContext context, Git git, String branch);

  PullResult sync(SessionContext context, Git git, String branchId);

  void close(SessionContext context, Git git);

  void fetch(SessionContext contaxt, Git git, String branch);

  PullResult inComing(SessionContext context, Git git, String branch);

  Iterable<PushResult> outGoing(SessionContext context, Git git, String branch);


}
