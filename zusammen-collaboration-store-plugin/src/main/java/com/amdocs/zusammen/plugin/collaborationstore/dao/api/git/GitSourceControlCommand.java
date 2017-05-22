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

package com.amdocs.zusammen.plugin.collaborationstore.dao.api.git;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.plugin.collaborationstore.types.CollaborationDiffResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.Collection;

public interface GitSourceControlCommand<T> {
  String getBranch(SessionContext context, Git git) throws IOException;

  Git openRepository(SessionContext context, String repositoryPath);

  CollaborationDiffResult publish(SessionContext context,
                                  Git git,
                                  String branch);

  Collection<DiffEntry> diff(SessionContext context, Git git, ObjectId from,
                             ObjectId to);

  void close(SessionContext context, Git git);

  void createBranch(SessionContext context, Git git, String baseBranch, String branch);

  boolean checkoutChange(SessionContext context, Git git, String changeRef);

  Collection<String> add(SessionContext context, Git git, Collection<String> files);

  RevCommit commit(SessionContext context, Git git, String message);

  void tag(SessionContext context, Git git, ObjectId revisionId, String tag, String message);

  PullResult sync(SessionContext context, Git git, String branchId);

  ObjectId getHead(SessionContext context, Git git);

  Git clone(SessionContext context, String source, String target, String... branch);

  Collection<String> getBranchFileList(SessionContext context, Git git);

  MergeResult merge(SessionContext context, Git git, String branch,
                    MergeCommand.FastForwardMode fastForwardMode, MergeStrategy mergeStrategy,
                    String message);

  void resetMerge(SessionContext context, Git git);

  void delete(SessionContext context, Git git, String... files);

  void reset(SessionContext context, Git git, String revisionRef);

  Iterable<RevCommit> listRevisionList(Git git, Id versionId);
}
