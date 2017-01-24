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

import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.item.Action;
import org.amdocs.zusammen.datatypes.item.Info;
import org.amdocs.zusammen.datatypes.item.ItemVersionData;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.util.SourceControlUtil;
import org.amdocs.zusammen.plugin.collaborationstore.git.util.TestUtil;
import org.eclipse.jgit.api.MergeCommand;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ItemVersionCollaborationStoreTest {


  @Spy
  private SourceControlUtil sourceControlUtil;
  @Mock
  private GitSourceControlDao gitSourceControlDaoMock;


  @Spy
  @InjectMocks
  private ItemVersionCollaborationStore itemVersionCollaborationStore;//
  // = spy(new ItemVersionCollaborationStore());

  private static final Id ITEM_ID = new Id();
  private static final Id VERSION_ID = new Id();
  private static final Id BASE_VERSION_ID = new Id();
  private static final SessionContext context = TestUtil.createSessionContext();

  @BeforeMethod
  public void init() {

    MockitoAnnotations.initMocks(this);


    /*doReturn(null).when(sourceControlUtil).handlePublishResponse(anyObject(),
        anyObject(), anyObject(), anyObject());
*/
    doReturn(null).when(sourceControlUtil).getRepoData(anyObject(), anyObject(), anyObject());

    Mockito.doNothing().when(itemVersionCollaborationStore).addFileContent(anyObject(), anyObject(),
        anyObject(),
        anyObject(),
        anyObject(),
        anyObject());

    doReturn(true).when(itemVersionCollaborationStore).storeItemVersionData(anyObject(),
        anyObject(), anyObject(), anyObject(), anyObject());

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


    ItemVersionData itemVersionData = new ItemVersionData();
    Info info = new Info();
    itemVersionData.setInfo(info);
    info.setName("createItemVersion");
    itemVersionCollaborationStore.create(context, ITEM_ID, null, VERSION_ID, itemVersionData);
    verify(gitSourceControlDaoMock).openRepository(context,
        "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.getValue().toString());
    verify(itemVersionCollaborationStore).createInt(context, null, "main", VERSION_ID.getValue()
            .toString()
        );
    verify(itemVersionCollaborationStore).storeItemVersionData(context,null,ITEM_ID,
        itemVersionData,Action.CREATE);
  }

  @Test
  public void testCreateBaseBranchNotNull() throws Exception {


    ItemVersionData itemVersionData = new ItemVersionData();
    Info info = new Info();
    itemVersionData.setInfo(info);
    info.setName("createItemVersion");

    itemVersionCollaborationStore.create(context, ITEM_ID, BASE_VERSION_ID, VERSION_ID, itemVersionData);
    verify(gitSourceControlDaoMock).openRepository(context,
        "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.getValue().toString());
    verify(itemVersionCollaborationStore).createInt(context, null, BASE_VERSION_ID.getValue()
            .toString(),
        VERSION_ID.getValue()
            .toString());
    verify(itemVersionCollaborationStore).storeItemVersionData(context,null,ITEM_ID,
        itemVersionData,Action.CREATE);
  }

  @Test
  public void testSave() throws Exception {


    ItemVersionData itemVersionData = new ItemVersionData();
    Info info = new Info();
    itemVersionData.setInfo(info);
    info.setName("saveItemVersion");
    itemVersionCollaborationStore.save(context, ITEM_ID, VERSION_ID, itemVersionData);
    verify(gitSourceControlDaoMock).openRepository(context,
        "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.getValue().toString());

    verify(gitSourceControlDaoMock).checkoutBranch(context, null, VERSION_ID.getValue().toString());
    verify(itemVersionCollaborationStore).storeItemVersionData(context, null, ITEM_ID,itemVersionData,
        Action.UPDATE);

  }

  @Test
  public void testPublish() throws Exception {


    String message = "publishItemVersion";
    itemVersionCollaborationStore.publish(context, ITEM_ID, VERSION_ID, message);
    verify(gitSourceControlDaoMock).openRepository(context,
        "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.getValue().toString());

    verify(gitSourceControlDaoMock).checkoutBranch(context, null, VERSION_ID.getValue().toString());
    //verify(itemVersionCollaborationStore).(anyObject());
    verify(sourceControlUtil).handlePublishFileDiff(anyObject(), anyObject(),
        anyObject(), anyObject());

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