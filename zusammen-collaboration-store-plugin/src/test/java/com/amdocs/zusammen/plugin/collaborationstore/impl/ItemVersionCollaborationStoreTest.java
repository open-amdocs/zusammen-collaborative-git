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

package com.amdocs.zusammen.plugin.collaborationstore.impl;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionDataConflict;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionRevisions;
import com.amdocs.zusammen.datatypes.itemversion.Revision;
import com.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDao;
import com.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDaoFactory;
import com.amdocs.zusammen.plugin.collaborationstore.dao.util.SourceControlUtil;
import com.amdocs.zusammen.plugin.collaborationstore.git.util.TestUtil;
import com.amdocs.zusammen.plugin.collaborationstore.types.CollaborationConflictResult;
import com.amdocs.zusammen.plugin.collaborationstore.types.CollaborationDiffResult;
import com.amdocs.zusammen.plugin.collaborationstore.types.CollaborationSyncResult;
import com.amdocs.zusammen.plugin.collaborationstore.types.FileInfoDiff;
import com.amdocs.zusammen.plugin.collaborationstore.types.ItemVersionRawData;
import com.amdocs.zusammen.plugin.collaborationstore.types.Repository;
import com.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElementConflict;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
      "/git/{tenant}/private" + File.separator + "users" + File.separator +
          context.getUser().getUserName() + File.separator;


  @Spy
  private SourceControlUtil sourceControlUtil;
  @Mock
  private SourceControlDao sourceControlDaoMock;

  @Spy
  @InjectMocks
  private ItemVersionCollaborationStore itemVersionCollaborationStore;


  private static final Id ITEM_ID = new Id();
  private static final Id VERSION_ID = new Id();
  private static final Id BASE_VERSION_ID = new Id();

  @BeforeMethod
  public void init() {
    MockitoAnnotations.initMocks(this);
    //doReturn(null).when(sourceControlUtil).getRepoData(anyObject(), anyObject(), anyObject());
    doReturn(null).when(sourceControlDaoMock).initRepository(anyObject(), anyObject());
    Mockito.doReturn(true).when(itemVersionCollaborationStore)
        .addFileContent(anyObject(), anyObject(), anyObject(), anyObject(),
            anyObject());
  }

  @Test
  public void testCreate() throws Exception {

    final ItemVersionCollaborationStoreTestMock.SourceControlDaoMock sourceControlDaoMock =
        new ItemVersionCollaborationStoreTestMock.SourceControlDaoMock();
    SourceControlDaoFactory sourceControlDaoFactoryMock = new SourceControlDaoFactory() {

      @Override
      public SourceControlDao createInterface(SessionContext sessionContext) {
        return sourceControlDaoMock;
      }
    };

    sourceControlDaoMock.setRepositoryLocation(PRIVATE_PATH_TENANT_TEST_USER + ITEM_ID.getValue());


    ItemVersionCollaborationStore itemVersionCollaborationStoreTest = Mockito.spy(new
        ItemVersionCollaborationStore(sourceControlDaoFactoryMock));


    ItemVersionData itemVersionData = new ItemVersionData();
    Info info = new Info();
    itemVersionData.setInfo(info);
    info.setName("createItemVersion");

    doReturn(new ArrayList<>()).when(itemVersionCollaborationStoreTest)
        .storeItemVersionData(anyObject(),
            anyObject(), anyObject(), anyObject(), anyObject(), anyObject());

    itemVersionCollaborationStoreTest.create(context, ITEM_ID, null, VERSION_ID, itemVersionData);


    verify(itemVersionCollaborationStoreTest).storeItemVersionData(context, ITEM_ID, VERSION_ID,
        PRIVATE_PATH_TENANT_TEST_USER + ITEM_ID.getValue(),
        itemVersionData, Action.CREATE);
  }

  @Test
  public void testCreateBaseBranchNotNull() throws Exception {
    final ItemVersionCollaborationStoreTestMock.SourceControlDaoMock sourceControlDaoMock =
        new ItemVersionCollaborationStoreTestMock.SourceControlDaoMock();
    SourceControlDaoFactory sourceControlDaoFactoryMock = new SourceControlDaoFactory() {

      @Override
      public SourceControlDao createInterface(SessionContext sessionContext) {
        return sourceControlDaoMock;
      }
    };

    sourceControlDaoMock.setRepositoryLocation(PRIVATE_PATH_TENANT_TEST_USER + ITEM_ID.getValue());


    ItemVersionCollaborationStore itemVersionCollaborationStoreTest = Mockito.spy(new
        ItemVersionCollaborationStore(sourceControlDaoFactoryMock));

    ItemVersionData itemVersionData = new ItemVersionData();
    Info info = new Info();
    itemVersionData.setInfo(info);
    info.setName("createItemVersion");
    doReturn(new ArrayList<>()).when(itemVersionCollaborationStoreTest)
        .storeItemVersionData(anyObject(),
            anyObject(), anyObject(), anyObject(), anyObject(), anyObject());


    itemVersionCollaborationStoreTest
        .create(context, ITEM_ID, BASE_VERSION_ID, VERSION_ID, itemVersionData);

    verify(itemVersionCollaborationStoreTest).storeItemVersionData(context, ITEM_ID, VERSION_ID,
        PRIVATE_PATH_TENANT_TEST_USER + ITEM_ID.getValue(),
        itemVersionData, Action.CREATE);
  }

  @Test
  public void testSave() throws Exception {

    final ItemVersionCollaborationStoreTestMock.SourceControlDaoMock sourceControlDaoMock =
        new ItemVersionCollaborationStoreTestMock.SourceControlDaoMock();
    SourceControlDaoFactory sourceControlDaoFactoryMock = new SourceControlDaoFactory() {

      @Override
      public SourceControlDao createInterface(SessionContext sessionContext) {
        return sourceControlDaoMock;
      }
    };

    sourceControlDaoMock.setRepositoryLocation(PRIVATE_PATH_TENANT_TEST_USER + ITEM_ID.getValue());


    ItemVersionCollaborationStore itemVersionCollaborationStoreTest = Mockito.spy(new
        ItemVersionCollaborationStore(sourceControlDaoFactoryMock));

    ItemVersionData itemVersionData = new ItemVersionData();
    Info info = new Info();
    itemVersionData.setInfo(info);
    info.setName("saveItemVersion");

    doReturn(new ArrayList<>()).when(itemVersionCollaborationStoreTest)
        .storeItemVersionData(anyObject(),
            anyObject(), anyObject(), anyObject(), anyObject(), anyObject());


    itemVersionCollaborationStoreTest.save(context, ITEM_ID, VERSION_ID, itemVersionData);

    verify(itemVersionCollaborationStoreTest)
        .storeItemVersionData(context, ITEM_ID, VERSION_ID, PRIVATE_PATH_TENANT_TEST_USER +
                ITEM_ID.getValue(), itemVersionData,
            Action.UPDATE);

  }

  @Test
  public void testPublish() throws Exception {

    Repository repository = new Repository();
    when(sourceControlDaoMock.initRepository(anyObject(), anyObject())).thenReturn
        (repository);
    doReturn(sourceControlDaoMock).when(itemVersionCollaborationStore).getSourceControlDao(context);
    CollaborationDiffResult collaborationDiffResultMock = mock(CollaborationDiffResult.class);

    doReturn(collaborationDiffResultMock).when(sourceControlDaoMock).publish(anyObject(), anyObject
        ());
    doReturn(new ArrayList<FileInfoDiff>()).when(collaborationDiffResultMock).getFileInfoDiffs();
    String message = "publishItemVersion";
    itemVersionCollaborationStore.publish(context, ITEM_ID, VERSION_ID, message);
    verify(sourceControlDaoMock)
        .initRepository(context, ITEM_ID);
    verify(sourceControlDaoMock).publish(context, repository);
  }

  @Test
  public void testMerge() throws Exception {

    final ItemVersionCollaborationStoreTestMock.SourceControlDaoMock sourceControlDaoMock =
        new ItemVersionCollaborationStoreTestMock.SourceControlDaoMock();
    SourceControlDaoFactory sourceControlDaoFactoryMock = new SourceControlDaoFactory() {

      @Override
      public SourceControlDao createInterface(SessionContext sessionContext) {
        return sourceControlDaoMock;
      }
    };

    sourceControlDaoMock.setCheckOutBranch(true);

    CollaborationDiffResult collaborationDiffResult = new CollaborationDiffResult(
        CollaborationDiffResult.MODE.NEW);
    CollaborationSyncResult collaborationSyncResult = new CollaborationSyncResult();
    collaborationSyncResult.setCollaborationDiffResult(collaborationDiffResult);
    sourceControlDaoMock.setMergeResult(collaborationSyncResult);


    ItemVersionCollaborationStore itemVersionCollaborationStoreTest = Mockito.spy(new
        ItemVersionCollaborationStore(sourceControlDaoFactoryMock));

    doReturn(sourceControlUtil).when(itemVersionCollaborationStoreTest).getSourceControlUtil();
    doReturn(".").when(sourceControlUtil)
        .getPrivateRepositoryPath(anyObject(), anyObject(), anyObject());

    CollaborationMergeChange change = new CollaborationMergeChange();
    doReturn(change).when(sourceControlUtil).loadFileDiffElements(context, "abcd", ITEM_ID,
        VERSION_ID, collaborationDiffResult);


    itemVersionCollaborationStoreTest.merge(context, ITEM_ID, VERSION_ID, BASE_VERSION_ID);

  }


  @Test
  public void testMergeFirstTimeOnItem() throws Exception {

    final ItemVersionCollaborationStoreTestMock.SourceControlDaoMock sourceControlDaoMock =
        new ItemVersionCollaborationStoreTestMock.SourceControlDaoMock();
    SourceControlDaoFactory sourceControlDaoFactoryMock = new SourceControlDaoFactory() {

      @Override
      public SourceControlDao createInterface(SessionContext sessionContext) {
        return sourceControlDaoMock;
      }
    };


    ItemVersionCollaborationStore itemVersionCollaborationStoreTest = Mockito.spy(new
        ItemVersionCollaborationStore(sourceControlDaoFactoryMock));

    doReturn(sourceControlUtil).when(itemVersionCollaborationStoreTest).getSourceControlUtil();
    doReturn("abcd").when(sourceControlUtil).getPrivateRepositoryPath(anyObject(), anyObject(),
        anyObject());
    CollaborationDiffResult collaborationDiffResult = new CollaborationDiffResult(
        CollaborationDiffResult.MODE.NEW);
    doReturn(collaborationDiffResult).when(sourceControlUtil).getRepoFiles(context, null);
    CollaborationMergeChange change = new CollaborationMergeChange();
    doReturn(change).when(sourceControlUtil).loadFileDiffElements(context, "abcd", ITEM_ID,
        VERSION_ID, collaborationDiffResult);

    itemVersionCollaborationStoreTest.merge(context, ITEM_ID, VERSION_ID, BASE_VERSION_ID);
  }

  @Test
  public void testSync() throws Exception {

    final ItemVersionCollaborationStoreTestMock.SourceControlDaoMock sourceControlDaoMock =
        new ItemVersionCollaborationStoreTestMock.SourceControlDaoMock();
    SourceControlDaoFactory sourceControlDaoFactoryMock = new SourceControlDaoFactory() {

      @Override
      public SourceControlDao createInterface(SessionContext sessionContext) {
        return sourceControlDaoMock;
      }
    };

    sourceControlDaoMock.setCheckOutBranch(true);

    CollaborationDiffResult collaborationDiffResult = new CollaborationDiffResult(
        CollaborationDiffResult.MODE.NEW);
    CollaborationSyncResult collaborationSyncResult = new CollaborationSyncResult();
    collaborationSyncResult.setCollaborationDiffResult(collaborationDiffResult);
    sourceControlDaoMock.setSyncResult(collaborationSyncResult);


    ItemVersionCollaborationStore itemVersionCollaborationStoreTest = Mockito.spy(new
        ItemVersionCollaborationStore(sourceControlDaoFactoryMock));

    doReturn(sourceControlUtil).when(itemVersionCollaborationStoreTest).getSourceControlUtil();
    doReturn(".").when(sourceControlUtil)
        .getPrivateRepositoryPath(anyObject(), anyObject(), anyObject());

    CollaborationMergeChange change = new CollaborationMergeChange();
    doReturn(change).when(sourceControlUtil).loadFileDiffElements(context, "abcd", ITEM_ID,
        VERSION_ID, collaborationDiffResult);


    itemVersionCollaborationStoreTest.sync(context, ITEM_ID, VERSION_ID);

  }

  @Test
  public void testSyncWithConflict() throws Exception {

    final ItemVersionCollaborationStoreTestMock.SourceControlDaoMock sourceControlDaoMock =
        new ItemVersionCollaborationStoreTestMock.SourceControlDaoMock();
    SourceControlDaoFactory sourceControlDaoFactoryMock = new SourceControlDaoFactory() {

      @Override
      public SourceControlDao createInterface(SessionContext sessionContext) {
        return sourceControlDaoMock;
      }
    };

    sourceControlDaoMock.setCheckOutBranch(true);

    CollaborationSyncResult collaborationSyncResult = new CollaborationSyncResult();
    CollaborationConflictResult collaborationConflictResult = new CollaborationConflictResult();
    collaborationConflictResult.addElementFile("11111", ".");
    collaborationConflictResult.addItemVersionFile(PluginConstants.ITEM_VERSION_INFO_FILE_NAME);
    collaborationSyncResult.setCollaborationConflictResult(collaborationConflictResult);
    sourceControlDaoMock.setSyncResult(collaborationSyncResult);

    ItemVersionCollaborationStore itemVersionCollaborationStoreTest = Mockito.spy(new
        ItemVersionCollaborationStore(sourceControlDaoFactoryMock));

    doReturn(sourceControlUtil).when(itemVersionCollaborationStoreTest).getSourceControlUtil();
    doReturn(".").when(sourceControlUtil)
        .getPrivateRepositoryPath(anyObject(), anyObject(), anyObject());
    //doReturn(".").when(itemVersionCollaborationStoreTest).resolveTenantPath(context,
    //    PRIVATE_PATH_TENANT_USER + ITEM_ID.getValue());


    CollaborationMergeChange change = new CollaborationMergeChange();
    doReturn(null).when(sourceControlUtil).uploadRawElementData(context, "abcd", ".");

    doReturn(null).when(sourceControlUtil).uploadRawItemVersionData(context, "abcd");

    Collection<CollaborationElementConflict> elementConflicts = new ArrayList<>();
    doReturn(elementConflicts).when(sourceControlUtil).resolveSyncElementConflicts(context,
        ITEM_ID, VERSION_ID, null, collaborationConflictResult);


    ItemVersionRawData itemVersionRawData = new ItemVersionRawData();
    doReturn(itemVersionRawData).when(sourceControlUtil).uploadRawItemVersionData(context, ".");

    ItemVersionDataConflict itemVersionConflict = new ItemVersionDataConflict();
    doReturn(itemVersionConflict).when(sourceControlUtil).resolveSyncItemVersionConflicts
        (context, itemVersionRawData, collaborationConflictResult.getItemVersionConflictFiles());


    itemVersionCollaborationStoreTest.sync(context, ITEM_ID, VERSION_ID);

  }


  @Test
  public void testSyncFirstTimeOnItem() throws Exception {

    final ItemVersionCollaborationStoreTestMock.SourceControlDaoMock sourceControlDaoMock =
        new ItemVersionCollaborationStoreTestMock.SourceControlDaoMock();
    SourceControlDaoFactory sourceControlDaoFactoryMock = new SourceControlDaoFactory() {

      @Override
      public SourceControlDao createInterface(SessionContext sessionContext) {
        return sourceControlDaoMock;
      }
    };


    ItemVersionCollaborationStore itemVersionCollaborationStoreTest = Mockito.spy(new
        ItemVersionCollaborationStore(sourceControlDaoFactoryMock));

    doReturn(sourceControlUtil).when(itemVersionCollaborationStoreTest).getSourceControlUtil();
    doReturn("abcd").when(sourceControlUtil).getPrivateRepositoryPath(anyObject(), anyObject(),
        anyObject());
    CollaborationDiffResult collaborationDiffResult = new CollaborationDiffResult(
        CollaborationDiffResult.MODE.NEW);
    doReturn(collaborationDiffResult).when(sourceControlUtil).getRepoFiles(context, null);
    CollaborationMergeChange change = new CollaborationMergeChange();
    doReturn(change).when(sourceControlUtil).loadFileDiffElements(context, "abcd", ITEM_ID,
        VERSION_ID, collaborationDiffResult);

    itemVersionCollaborationStoreTest.sync(context, ITEM_ID, VERSION_ID);


  }


  @Test
  public void testListRevisions() {


    final ItemVersionCollaborationStoreTestMock.SourceControlDaoMock sourceControlDaoMock =
        new ItemVersionCollaborationStoreTestMock.SourceControlDaoMock();
    SourceControlDaoFactory sourceControlDaoFactoryMock = new SourceControlDaoFactory() {

      @Override
      public SourceControlDao createInterface(SessionContext sessionContext) {
        return sourceControlDaoMock;
      }
    };

    ItemVersionCollaborationStore itemVersionCollaborationStoreTest = Mockito.spy(new
        ItemVersionCollaborationStore(sourceControlDaoFactoryMock));

    List<Revision> revCommits = new ArrayList<>();

    Revision revision1 = new Revision();
    revision1.setRevisionId(new Id());
    Revision revision2 = new Revision();
    revision2.setRevisionId(new Id());
    revCommits.add(revision1);
    revCommits.add(revision2);

    sourceControlDaoMock.setListRevisionRevisions(revCommits);
    Revision change = new Revision();

    ItemVersionRevisions changes =
        itemVersionCollaborationStoreTest.listRevisions(context, ITEM_ID, VERSION_ID);
    Assert.assertNotNull(changes);
    Assert.assertEquals(changes.getItemVersionRevisions().size(), revCommits.size());
  }

  @Test
  public void testResetRevisions() {

    final ItemVersionCollaborationStoreTestMock.SourceControlDaoMock sourceControlDaoMock =
        new ItemVersionCollaborationStoreTestMock.SourceControlDaoMock();
    SourceControlDaoFactory sourceControlDaoFactoryMock = new SourceControlDaoFactory() {

      @Override
      public SourceControlDao createInterface(SessionContext sessionContext) {
        return sourceControlDaoMock;
      }
    };

    ItemVersionCollaborationStore itemVersionCollaborationStoreTest = Mockito.spy(new
        ItemVersionCollaborationStore(sourceControlDaoFactoryMock));

    CollaborationDiffResult collaborationDiffResult = new CollaborationDiffResult(
        CollaborationDiffResult.MODE.NEW);
    FileInfoDiff fileDiff = new FileInfoDiff(".", Action.CREATE);
    collaborationDiffResult.add(fileDiff);
    sourceControlDaoMock.setCollaborationDiffResult(collaborationDiffResult);
    itemVersionCollaborationStoreTest
        .resetRevisions(context, new ElementContext(ITEM_ID, VERSION_ID),
            new Id(ObjectId.zeroId().getName()));
  }

  @Test
  public void testStoreItemVersionData() {

    Git git = mock(Git.class);
    ItemVersionData itemVersionData = new ItemVersionData();
    itemVersionData.setInfo(new Info());
    itemVersionCollaborationStore.storeItemVersionData(context, ITEM_ID, VERSION_ID,
        PRIVATE_PATH_TENANT_TEST_USER + ITEM_ID.getValue(),
        itemVersionData,
        Action.CREATE);
  }
}