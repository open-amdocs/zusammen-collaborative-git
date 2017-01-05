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

package org.amdocs.tsuzammen.plugin.collaborationstore.git.commands;

import org.amdocs.tsuzammen.utils.common.CommonMethods;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RevisionDiffCommand {

  private Git git;
  private ObjectId from;
  private ObjectId to;

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

  public Collection<DiffEntry> call()
      throws GitAPIException {

    List<DiffEntry> returnDiffs = new ArrayList<>();
    try {





      RevWalk walk = new RevWalk(git.getRepository());
      RevCommit last = walk.parseCommit(to);
      RevCommit fromCommit = walk.parseCommit(from);
      Iterable<RevCommit> revCommitIter = git.log().addRange(this.from,this.to).call();
      List<RevCommit> revCommitList = CommonMethods.iteratorToList(revCommitIter);
      revCommitList.add(fromCommit);
      for(RevCommit revCommit:revCommitList){
        if(revCommit.getName().equals(last.getName())) continue;
        TreeWalk tw = new TreeWalk(git.getRepository());
        tw.addTree(revCommit.getTree());
        tw.addTree(last.getTree());
        tw.setRecursive(true);
        List<DiffEntry> diffs = DiffEntry.scan(tw);
        returnDiffs.addAll(diffs);
        for (DiffEntry diffEntry : diffs) {
          System.out.println("start:"+last.getName()+" commit:"+revCommit
              .getName()+" "+diffEntry.toString());
        }
      }
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }

    return returnDiffs;
  }

  private RevisionDiffCommand(Git git) {
    this.git = git;
  }
}
