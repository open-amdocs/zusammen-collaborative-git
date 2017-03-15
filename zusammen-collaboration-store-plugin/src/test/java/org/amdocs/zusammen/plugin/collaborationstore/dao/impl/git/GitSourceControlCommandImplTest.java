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

package org.amdocs.zusammen.plugin.collaborationstore.dao.impl.git;

import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.response.ZusammenException;
import org.amdocs.zusammen.plugin.collaborationstore.types.CollaborationDiffResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.*;

public class GitSourceControlCommandImplTest {

  private SessionContext context = new SessionContext();

  @Test
  public void testGetBranch() throws Exception {

  }

  @Test
  public void testOpenRepository() throws Exception {
      GitSourceControlCommandImpl command = new GitSourceControlCommandImpl();
    boolean ind = false;
    String path = "error";
    try {
      command.openRepository(context, path);
    }catch (ZusammenException zex){
     ind = true;
    }
    Assert.assertTrue(ind);
  }

  @Test
  public void testPublish() throws Exception {
    GitSourceControlCommandImpl command = spy(new GitSourceControlCommandImpl());
    Git git = mock(Git.class);
    PushCommand pushCommand = mock(PushCommand.class);
    PushResult pushResult = mock(PushResult.class);
    RemoteRefUpdate remoteRefUpdate = mock(RemoteRefUpdate.class);
    ObjectId from = spy(ObjectId.fromString("1000000000000000000000000000000000000000"));
    ObjectId to = spy(ObjectId.fromString("1000000000000000000000000000000000000000"));

    List<PushResult> list = new ArrayList<>();
    list.add(pushResult);
    Iterable<PushResult> pushResults = list;
    String branch = "main";

    Collection<DiffEntry> diffEntries = new ArrayList<>();
    List<RemoteRefUpdate> remoteRefUpdates = new ArrayList<>();
    remoteRefUpdates.add(remoteRefUpdate);
    doReturn(pushCommand).when(git).push();
    doReturn(pushResults).when(pushCommand).call();
    doReturn(remoteRefUpdates).when(pushResult).getRemoteUpdates();
    doReturn(from).when(remoteRefUpdate).getExpectedOldObjectId();
    doReturn(to).when(remoteRefUpdate).getNewObjectId();
    doReturn(diffEntries).when(command).diff(context,git,from,to);
    CollaborationDiffResult result = command.publish(context,git,branch);
    Assert.assertNotNull(result);
  }

  @Test
  public void testMerge() throws Exception {

  }

  @Test
  public void testSync() throws Exception {

  }

  @Test
  public void testDiff() throws Exception {

  }

  @Test
  public void testClose() throws Exception {

  }

  @Test
  public void testCreateBranch() throws Exception {

  }

  @Test
  public void testCheckoutBranch() throws Exception {

  }

  @Test
  public void testAdd() throws Exception {

  }

  @Test
  public void testCommit() throws Exception {

  }

  @Test
  public void testGetHead() throws Exception {

  }

  @Test
  public void testClone() throws Exception {

  }

  @Test
  public void testGetBranchFileList() throws Exception {

  }

  @Test
  public void testResetMerge() throws Exception {

  }

  @Test
  public void testDelete() throws Exception {

  }

  @Test
  public void testReset() throws Exception {

  }

  @Test
  public void testListRevisionList() throws Exception {

  }

}