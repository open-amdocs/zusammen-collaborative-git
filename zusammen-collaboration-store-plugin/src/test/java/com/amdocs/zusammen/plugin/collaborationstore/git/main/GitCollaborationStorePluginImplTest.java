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

package com.amdocs.zusammen.plugin.collaborationstore.git.main;

import com.amdocs.zusammen.plugin.collaborationstore.git.util.TestUtil;
import com.amdocs.zusammen.plugin.collaborationstore.impl.ElementCollaborationStore;
import com.amdocs.zusammen.plugin.collaborationstore.impl.ItemCollaborationStore;
import com.amdocs.zusammen.plugin.collaborationstore.impl.ItemVersionCollaborationStore;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GitCollaborationStorePluginImplTest {

  @Mock
  private SessionContext context = TestUtil.createSessionContext();

  @Mock
  GitCollaborationStorePluginImpl gitCollaborationStorePluginMock;
  @Mock
  ItemCollaborationStore itemCollaborationStore = mock(ItemCollaborationStore.class);
  @Mock
  ItemVersionCollaborationStore itemVersionCollaborationStore =
      mock(ItemVersionCollaborationStore.class);
  @Mock
  ElementCollaborationStore elementCollaborationStore = mock(ElementCollaborationStore.class);
  ;


  @BeforeMethod
  public void init() {
    MockitoAnnotations.initMocks(this);
    gitCollaborationStorePluginMock = Mockito.spy(new GitCollaborationStorePluginImpl());

    when(gitCollaborationStorePluginMock.getElementCollaborationStore()).thenReturn
        (elementCollaborationStore);
    when(gitCollaborationStorePluginMock.getItemCollaborationStore()).thenReturn
        (itemCollaborationStore);
    when(gitCollaborationStorePluginMock.getItemVersionCollaborationStore()).thenReturn
        (itemVersionCollaborationStore);
  }



  /*@Test
  public void testCreateItem() throws Exception {
    Mockito.doNothing().when(itemCollaborationStore).create(anyObject(),anyObject(),anyObject());
    gitCollaborationStorePluginMock.createItem(context,null,null);
  }*/

  @Test
  public void testDeleteItem() throws Exception {

    gitCollaborationStorePluginMock.deleteItem(context, null);

  }

  @Test
  public void testCreateItemVersion() throws Exception {

    gitCollaborationStorePluginMock.createItemVersion(context, null, null, null, null);
    verify(itemVersionCollaborationStore).create(context, null, null, null, null);
  }

  @Test
  public void testUpdateItemVersion() throws Exception {

    gitCollaborationStorePluginMock.updateItemVersion(context, null, null, null);
    verify(itemVersionCollaborationStore).save(context, null, null, null);
  }

  @Test
  public void testCreateElement() throws Exception {

    gitCollaborationStorePluginMock.createElement(context, null);
    verify(elementCollaborationStore).create(context, null);
  }

  @Test
  public void testUpdateElement() throws Exception {

    gitCollaborationStorePluginMock.updateElement(context, null);
    verify(elementCollaborationStore).update(context, null);
  }

  @Test
  public void testDeleteElement() throws Exception {

    gitCollaborationStorePluginMock.deleteElement(context, null);
    verify(elementCollaborationStore).delete(context, null);
  }

  @Test
  public void testDeleteItemVersion() throws Exception {
    gitCollaborationStorePluginMock.deleteItemVersion(context, null, null);
    verify(itemVersionCollaborationStore).delete(context, null, null);
  }

  @Test
  public void testPublishItemVersion() throws Exception {
    gitCollaborationStorePluginMock.publishItemVersion(context, null, null, null);
    verify(itemVersionCollaborationStore).publish(context, null, null, null);
  }

  @Test
  public void testSyncItemVersion() throws Exception {
    gitCollaborationStorePluginMock.syncItemVersion(context, null, null);
    verify(itemVersionCollaborationStore).sync(anyObject(), anyObject(), anyObject());
  }

  @Test
  public void testMergeItemVersion() throws Exception {
    gitCollaborationStorePluginMock.mergeItemVersion(context, null, null, null);
    verify(itemVersionCollaborationStore).merge(context, null, null, null);
  }

  @Test
  public void testGetElement() throws Exception {

    gitCollaborationStorePluginMock.getElement(context, null, null, null);
    verify(elementCollaborationStore).get(context, null, null, null);
  }

  @Test
  public void testGetItemVersionRevisions() {
    Id itemId = new Id("itemId");
    Id versionId = new Id("versionId");

    gitCollaborationStorePluginMock.listItemVersionRevisions(context, itemId, versionId);
    verify(itemVersionCollaborationStore).listRevisions(context, itemId, versionId);
  }

  @Test
  public void testResetItemVersionRevisions() {
    Id itemId = new Id("itemId");
    Id versionId = new Id("versionId");
    Id revisionId = new Id("changeId");

    gitCollaborationStorePluginMock.resetItemVersionRevision(context, itemId, versionId,
        revisionId);
    verify(itemVersionCollaborationStore)
        .resetRevisions(context, new ElementContext(itemId, versionId), revisionId);
  }
}