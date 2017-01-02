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

package org.amdocs.tsuzammen.plugin.collaborationstore.git;


import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.transport.PushResult;

import java.io.File;
import java.util.Collection;
import java.util.List;

public interface GitSourceControlDao {

  Git clone(SessionContext context, String source, String target, String... branch);

  void createBranch(SessionContext context, Git git, String baseBranch, String branch);

  void checkoutBranch(SessionContext context, Git git, String branch);

  Git openRepository(SessionContext context, String repositoryPath);

  List<File> add(SessionContext context, Git git, File... files);

  void delete(SessionContext context, Git git, String... files);

  void commit(SessionContext context, Git git, String message);

  void resetMerge(SessionContext context, Git git);

  Collection<PushResult> publish(SessionContext context, Git git, String branch);

  PullResult sync(SessionContext context, Git git, String branchId);

  MergeResult merge(SessionContext context, Git git, String branchId, MergeCommand.FastForwardMode mode);

  void close(SessionContext context, Git git);

  void fetch(SessionContext contaxt, Git git, String branch);

  PullResult inComing(SessionContext context, Git git, String branch);

  Iterable<PushResult> outGoing(SessionContext context, Git git, String branch);

  Status status(SessionContext context,Git git);


}
