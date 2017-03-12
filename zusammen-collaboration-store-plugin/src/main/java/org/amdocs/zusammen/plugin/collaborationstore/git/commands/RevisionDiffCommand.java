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

package org.amdocs.zusammen.plugin.collaborationstore.git.commands;

import org.amdocs.zusammen.utils.common.CommonMethods;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class RevisionDiffCommand {

  private Git git;
  private ObjectId from;
  private ObjectId to;
  private TreeFilter treeFilter;

  public static RevisionDiffCommand init(Git git){
    return new RevisionDiffCommand(git);
  }

  public RevisionDiffCommand from(ObjectId from){
    this.from=from;
    return this;
  }

  public RevisionDiffCommand to(ObjectId to){
    this.to=to;
    return this;
  }

  public RevisionDiffCommand filter(TreeFilter treeFilter){
    this.treeFilter=treeFilter;
    return this;
  }

  public Collection<DiffEntry> call()
      throws GitAPIException {

    if(this.from==null){
      from = getFirstCommit();
    }
    List<DiffEntry> returnDiffs = new ArrayList<>();
    if(from!= null && from.getName().equals(to.getName())) return returnDiffs;

    try {
      RevWalk walk = new RevWalk(git.getRepository());
      RevCommit last = walk.parseCommit(to);
      RevCommit fromCommit = walk.parseCommit(from);
      Iterable<RevCommit> revCommitIter = git.log().addRange(this.from,this.to).call();
      List<RevCommit> revCommitList = CommonMethods.iteratorToList(revCommitIter);
      revCommitList.add(fromCommit);
      TreeWalk tw = getTreeWalk(last, fromCommit);
      List<DiffEntry> diffs = DiffEntry.scan(tw);
      returnDiffs.addAll(diffs);

    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }

    return returnDiffs;
  }

  private TreeWalk getTreeWalk(RevCommit last, RevCommit fromCommit) throws IOException {
    TreeWalk tw = new TreeWalk(git.getRepository());
    tw.addTree(fromCommit.getTree());
    tw.addTree(last.getTree());
    tw.setRecursive(true);
    if(this.treeFilter != null)
      tw.setFilter(this.treeFilter);
    return tw;
  }

  private ObjectId getFirstCommit() {
    try {
      Iterable<RevCommit> commitsIterator = git.log().call();
      Iterator<RevCommit> iterator = commitsIterator.iterator();
      RevCommit commit = null;
      while((iterator.hasNext())){
        commit = iterator.next();
        if(commit.getParents() ==null || commit.getParents().length==0 || commit.getParents()
            [0]==null) return commit;
      }
      return commit;
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }

  }

  private RevisionDiffCommand(Git git) {
    this.git = git;
  }
}
