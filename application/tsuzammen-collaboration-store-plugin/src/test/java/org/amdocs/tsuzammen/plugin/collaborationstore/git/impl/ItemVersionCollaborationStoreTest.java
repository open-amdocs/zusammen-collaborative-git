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

import org.amdocs.tsuzammen.datatypes.Id;
import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.datatypes.item.Info;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.util.SourceControlUtil;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.util.TestUtil;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.tsuzammen.sdk.types.CollaborationChangedElementData;
import org.amdocs.tsuzammen.sdk.types.CollaborationSyncResult;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.ObjectId;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class ItemVersionCollaborationStoreTest {

  @Spy
  private ItemVersionCollaborationStore itemVersionCollaborationStore;//
  // = spy(new ItemVersionCollaborationStore());
  @Mock
  private GitSourceControlDao gitSourceControlDaoMock;
  @Mock
  private SourceControlUtil sourceControlUtil;

  private static final Id ITEM_ID = new Id();
  private static final Id VERSION_ID = new Id();
  private static final Id BASE_VERSION_ID = new Id();
  private static final SessionContext context = TestUtil.createSessionContext();

  @BeforeMethod
  public void init() {

    MockitoAnnotations.initMocks(this);


    when(itemVersionCollaborationStore.convertPushresultToPublishResult
        (anyObject())).thenReturn(null);

    Mockito.doNothing().when(itemVersionCollaborationStore).addFileContent(anyObject(), anyObject(),
        anyObject(),
        anyObject(),
        anyObject());

    when(itemVersionCollaborationStore.getSourceControlDao(anyObject())).thenReturn
        (gitSourceControlDaoMock);
    when(gitSourceControlDaoMock.clone
        (anyObject(), anyObject(), anyObject(), anyObject())).thenReturn(null);
    when(gitSourceControlDaoMock.openRepository
        (anyObject(), anyObject())).thenReturn(null);

    when(gitSourceControlDaoMock.getHead
        (anyObject(), anyObject())).thenReturn(null);

    when(gitSourceControlDaoMock.clone
        (anyObject(), anyObject(), anyObject())).thenReturn(null);
  }


  @Test
  public void testCreate() throws Exception {


    Info info = new Info();
    info.setName("createItemVersion");
    itemVersionCollaborationStore.create(context, ITEM_ID, null, VERSION_ID, info);
    verify(gitSourceControlDaoMock).openRepository(context,
        "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.getValue().toString());
    verify(itemVersionCollaborationStore).createInt(context, null, "main", VERSION_ID.getValue()
            .toString(),
        info);
  }

  @Test
  public void testCreateBaseBranchNotNull() throws Exception {


    Info info = new Info();
    info.setName("createItemVersion");

    itemVersionCollaborationStore.create(context, ITEM_ID, BASE_VERSION_ID, VERSION_ID, info);
    verify(gitSourceControlDaoMock).openRepository(context,
        "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.getValue().toString());
    verify(itemVersionCollaborationStore).createInt(context, null, BASE_VERSION_ID.getValue()
            .toString(),
        VERSION_ID.getValue()
            .toString(),
        info);
  }

  @Test
  public void testSave() throws Exception {


    Info info = new Info();
    info.setName("saveItemVersion");
    itemVersionCollaborationStore.save(context, ITEM_ID, VERSION_ID, info);
    verify(gitSourceControlDaoMock).openRepository(context,
        "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.getValue().toString());

    verify(gitSourceControlDaoMock).checkoutBranch(context, null, VERSION_ID.getValue().toString());
    verify(itemVersionCollaborationStore).storeItemVersionInfo(context, null, ITEM_ID, info);

  }

  @Test
  public void testPublish() throws Exception {


    String message = "publishItemVersion";
    itemVersionCollaborationStore.publish(context, ITEM_ID, VERSION_ID, message);
    verify(gitSourceControlDaoMock).openRepository(context,
        "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.getValue().toString());

    verify(gitSourceControlDaoMock).checkoutBranch(context, null, VERSION_ID.getValue().toString());
    verify(itemVersionCollaborationStore).convertPushresultToPublishResult(anyObject());

  }

  @Test
  public void testSync() throws Exception {
    doReturn(".").when(itemVersionCollaborationStore).resolveTenantPath(context,
        "/git/{tenant}/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.getValue().toString());

    itemVersionCollaborationStore.sync(context, ITEM_ID, VERSION_ID);
    verify(gitSourceControlDaoMock).openRepository(context,
        ".");

    verify(gitSourceControlDaoMock).checkoutBranch(context, null, VERSION_ID.getValue().toString());
    verify(gitSourceControlDaoMock, times(2)).getHead(context, null);
    verify(gitSourceControlDaoMock).sync(context, null, VERSION_ID.getValue().toString());
  }

  @Test
  public void testSyncFirstTime() throws Exception {

    itemVersionCollaborationStore.sync(context, ITEM_ID, VERSION_ID);
    verify(gitSourceControlDaoMock).clone(context,
        "/git/test/public\\" + ITEM_ID.toString(),
        "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.toString(), VERSION_ID
            .getValue().toString());


    verify(gitSourceControlDaoMock, times(1)).getHead(context, null);
  }

  @Test
  public void testMerge() throws Exception {

    doReturn(".").when(itemVersionCollaborationStore).resolveTenantPath(context,
        "/git/{tenant}/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.getValue().toString());


    itemVersionCollaborationStore.merge(context, ITEM_ID, VERSION_ID, BASE_VERSION_ID);
    verify(gitSourceControlDaoMock).openRepository(context, ".");
    verify(gitSourceControlDaoMock).checkoutBranch(context, null, VERSION_ID.getValue().toString());
    verify(gitSourceControlDaoMock, times(2)).getHead(context, null);
    verify(gitSourceControlDaoMock).merge(context, null, BASE_VERSION_ID.toString(),
        MergeCommand.FastForwardMode.FF);
  }
}