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

package org.amdocs.tsuzammen.plugin.collaborationstore.git.plugin.mocks;

import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.GitSourceControlDao;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;

import java.util.Collection;
import java.util.List;

public class GitSourceControlDaoEmptyImpl implements GitSourceControlDao {
  @Override
  public Git clone(SessionContext context, String source, String target, String... branch) {
    return null;
  }

  @Override
  public void createBranch(SessionContext context, Git git, String baseBranch, String branch) {

  }

  @Override
  public void checkoutBranch(SessionContext context, Git git, String branch) {

  }

  @Override
  public Git openRepository(SessionContext context, String repositoryPath) {

    return null;
  }

  @Override
  public List<String> add(SessionContext context, Git git, String... files) {
    return null;
  }

  @Override
  public void delete(SessionContext context, Git git, String... files) {

  }

  @Override
  public RevCommit commit(SessionContext context, Git git, String message) {

    return null;
  }

  @Override
  public void resetMerge(SessionContext context, Git git) {

  }

  @Override
  public Collection<PushResult> publish(SessionContext context, Git git, String branch) {
    return null;
  }

  @Override
  public PullResult sync(SessionContext context, Git git, String branchId) {
    return null;
  }
  @Override
  public MergeResult merge(SessionContext context, Git git, String branchId,
                           MergeCommand.FastForwardMode mode) {
    return null;
  }

  @Override
  public void close(SessionContext context, Git git) {

  }

  @Override
  public FetchResult fetch(SessionContext contaxt, Git git, String branch) { return null;

  }

  @Override
  public PullResult inComing(SessionContext context, Git git, String branch) {
    return null;
  }

  @Override
  public Iterable<PushResult> outGoing(SessionContext context, Git git, String branch) {
    return null;
  }

  @Override
  public Status status(SessionContext context, Git git){return null;}

  @Override
  public Collection<DiffEntry> revisionDiff(SessionContext context, Git git, ObjectId from, ObjectId to) {
    return null;
  }

  @Override
  public ObjectId getHead(SessionContext context, Git git) {
    return null;
  }
}
