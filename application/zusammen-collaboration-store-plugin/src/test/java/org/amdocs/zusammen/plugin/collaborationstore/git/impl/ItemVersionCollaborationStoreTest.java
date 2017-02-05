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

package org.amdocs.zusammen.plugin.collaborationstore.git.impl;

import javafx.scene.control.TextFormatter;
import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.item.Action;
import org.amdocs.zusammen.datatypes.item.Info;
import org.amdocs.zusammen.datatypes.item.ItemVersionData;
import org.amdocs.zusammen.datatypes.itemversion.Change;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.util.SourceControlUtil;
import org.amdocs.zusammen.plugin.collaborationstore.git.util.TestUtil;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ItemVersionCollaborationStoreTest {
  private static final SessionContext context = TestUtil.createSessionContext();

  private static final String PUBLIC_PATH = "/git/test/public" + File.separator;
  private static final String PRIVATE_PATH_TENANT_TEST =
      "/git/test/private" + File.separator + "users" + File.separator;
  private static final String PRIVATE_PATH_TENANT_TEST_USER =
      PRIVATE_PATH_TENANT_TEST + context.getUser().getUserName() + File.separator;
  private static final String PRIVATE_PATH_TENANT_USER =
      "/git/{tenant}/private" + File.separator + "users" + File.separator + context.getUser().getUserName() + File.separator;


  @Spy
  private SourceControlUtil sourceControlUtil;
  @Mock
  private GitSourceControlDao gitSourceControlDaoMock;

  @Spy
  @InjectMocks
  private ItemVersionCollaborationStore itemVersionCollaborationStore;



  private static final Id ITEM_ID = new Id();
  private static final Id VERSION_ID = new Id();
  private static final Id BASE_VERSION_ID = new Id();

  @BeforeMethod
  public void init() {
    MockitoAnnotations.initMocks(this);
    doReturn(null).when(sourceControlUtil).getRepoData(anyObject(), anyObject(), anyObject());

    Mockito.doNothing().when(itemVersionCollaborationStore)
        .addFileContent(anyObject(), anyObject(), anyObject(), anyObject(), anyObject(),
            anyObject());

    doReturn(true).when(itemVersionCollaborationStore).storeItemVersionData(anyObject(),
        anyObject(), anyObject(), anyObject(), anyObject());

    when(itemVersionCollaborationStore.getSourceControlDao(anyObject()))
        .thenReturn(gitSourceControlDaoMock);
    when(gitSourceControlDaoMock.clone(anyObject(), anyObject(), anyObject(), anyObject()))
        .thenReturn(null);
    when(gitSourceControlDaoMock.openRepository(anyObject(), anyObject())).thenReturn(null);
    when(gitSourceControlDaoMock.getHead(anyObject(), anyObject())).thenReturn(null);
    when(gitSourceControlDaoMock.clone(anyObject(), anyObject(), anyObject())).thenReturn(null);

  }

  @Test
  public void testCreate() throws Exception {
    ItemVersionData itemVersionData = new ItemVersionData();
    Info info = new Info();
    itemVersionData.setInfo(info);
    info.setName("createItemVersion");
    itemVersionCollaborationStore.create(context, ITEM_ID, null, VERSION_ID, itemVersionData);
    verify(gitSourceControlDaoMock).openRepository(context, PRIVATE_PATH_TENANT_TEST_USER + ITEM_ID.getValue());
    verify(itemVersionCollaborationStore).createInt(context, null, "main", VERSION_ID.getValue()
    );
    verify(itemVersionCollaborationStore).storeItemVersionData(context, null, ITEM_ID,
        itemVersionData, Action.CREATE);
  }

  @Test
  public void testCreateBaseBranchNotNull() throws Exception {
    ItemVersionData itemVersionData = new ItemVersionData();
    Info info = new Info();
    itemVersionData.setInfo(info);
    info.setName("createItemVersion");

    itemVersionCollaborationStore
        .create(context, ITEM_ID, BASE_VERSION_ID, VERSION_ID, itemVersionData);
    verify(gitSourceControlDaoMock).openRepository(context, PRIVATE_PATH_TENANT_TEST_USER + ITEM_ID.getValue());
    verify(itemVersionCollaborationStore).createInt(context, null, BASE_VERSION_ID.getValue(),
        VERSION_ID.getValue());
    verify(itemVersionCollaborationStore).storeItemVersionData(context, null, ITEM_ID,
        itemVersionData, Action.CREATE);
  }

  @Test
  public void testSave() throws Exception {


    ItemVersionData itemVersionData = new ItemVersionData();
    Info info = new Info();
    itemVersionData.setInfo(info);
    info.setName("saveItemVersion");
    itemVersionCollaborationStore.save(context, ITEM_ID, VERSION_ID, itemVersionData);
    verify(gitSourceControlDaoMock).openRepository(context, PRIVATE_PATH_TENANT_TEST_USER + ITEM_ID.getValue());

    verify(gitSourceControlDaoMock).checkoutBranch(context, null, VERSION_ID.getValue());
    verify(itemVersionCollaborationStore)
        .storeItemVersionData(context, null, ITEM_ID, itemVersionData,
            Action.UPDATE);

  }

  @Test
  public void testPublish() throws Exception {


    String message = "publishItemVersion";
    itemVersionCollaborationStore.publish(context, ITEM_ID, VERSION_ID, message);
    verify(gitSourceControlDaoMock).openRepository(context, PRIVATE_PATH_TENANT_TEST_USER + ITEM_ID.getValue());

    verify(gitSourceControlDaoMock).checkoutBranch(context, null, VERSION_ID.getValue());
    //verify(itemVersionCollaborationStore).(anyObject());
    verify(sourceControlUtil).handleMergeFileDiff(anyObject(), anyObject(),
        anyObject(), anyObject());

  }

  @Test
  public void testSync() throws Exception {
    doReturn(".").when(itemVersionCollaborationStore).resolveTenantPath(context,
        PRIVATE_PATH_TENANT_USER + ITEM_ID.getValue());

    doReturn(true).when(gitSourceControlDaoMock)
        .checkoutBranch(eq(context), anyObject(), eq(VERSION_ID.toString()));

    itemVersionCollaborationStore.sync(context, ITEM_ID, VERSION_ID);
    verify(gitSourceControlDaoMock).openRepository(context,
        ".");

    verify(gitSourceControlDaoMock).checkoutBranch(context, null, VERSION_ID.getValue());
    verify(gitSourceControlDaoMock, times(2)).getHead(context, null);
    verify(gitSourceControlDaoMock).sync(context, null, VERSION_ID.getValue());
  }

  @Test
  public void testSyncFirstTimeOnVersion() throws Exception {
    doReturn(".").when(itemVersionCollaborationStore)
        .resolveTenantPath(context, PRIVATE_PATH_TENANT_USER + ITEM_ID.getValue());

    itemVersionCollaborationStore.sync(context, ITEM_ID, VERSION_ID);
    verify(gitSourceControlDaoMock).openRepository(context,
        ".");

    verify(gitSourceControlDaoMock).checkoutBranch(context, null, VERSION_ID.getValue());
    verify(gitSourceControlDaoMock).getHead(context, null);
    verify(gitSourceControlDaoMock).sync(context, null, VERSION_ID.getValue());
  }

  @Test
  public void testSyncFirstTimeOnItem() throws Exception {
    itemVersionCollaborationStore.sync(context, ITEM_ID, VERSION_ID);
    verify(gitSourceControlDaoMock)
        .clone(context, PUBLIC_PATH + ITEM_ID.toString(), PRIVATE_PATH_TENANT_TEST_USER + ITEM_ID.toString(),
            VERSION_ID.getValue());
    verify(gitSourceControlDaoMock, times(1)).getHead(context, null);
  }

  @Test
  public void testMerge() throws Exception {
    doReturn(".").when(itemVersionCollaborationStore)
        .resolveTenantPath(context, PRIVATE_PATH_TENANT_USER + ITEM_ID.getValue());

    itemVersionCollaborationStore.merge(context, ITEM_ID, VERSION_ID, BASE_VERSION_ID);
    verify(gitSourceControlDaoMock).openRepository(context, ".");
    verify(gitSourceControlDaoMock).checkoutBranch(context, null, VERSION_ID.getValue());
    verify(gitSourceControlDaoMock, times(2)).getHead(context, null);
    verify(gitSourceControlDaoMock).merge(context, null, BASE_VERSION_ID.toString(),
        MergeCommand.FastForwardMode.FF, null, null);
  }

  @Test
  public void testListHistory(){


    Collection<RevCommit> revCommits = new ArrayList<>();

    RevCommit revCommit1 = Mockito.mock(RevCommit.class);
    RevCommit revCommit2 = Mockito.mock(RevCommit.class);
    revCommits.add(revCommit1);
    revCommits.add(revCommit2);
    when(gitSourceControlDaoMock.listHistory(anyObject(),anyObject())).thenReturn(revCommits);

    Change change = new Change();
    Mockito.doReturn(change).when(itemVersionCollaborationStore).getChange(anyObject());

    itemVersionCollaborationStore.listHistory(context,ITEM_ID,VERSION_ID);
    verify(gitSourceControlDaoMock).listHistory(context,null);
  }

  @Test
  public void testResetHistory(){
    Mockito.doNothing().when(gitSourceControlDaoMock).revert(context,null,ObjectId
        .zeroId());
    itemVersionCollaborationStore.resetHistory(context,ITEM_ID,VERSION_ID,new Id(ObjectId.zeroId
        ().getName()));
  }

}