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

package org.amdocs.tsuzammen.plugin.collaborationstore.git.plugin;

import org.amdocs.tsuzammen.commons.datatypes.Id;
import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.commons.datatypes.UserInfo;
import org.amdocs.tsuzammen.commons.datatypes.impl.item.EntityData;
import org.amdocs.tsuzammen.commons.datatypes.item.Info;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.plugin.mocks.GitSourceControlDaoEmptyImpl;
import org.eclipse.jgit.api.Git;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GitCollaborationStorePluginImplTest {

  private GitCollaborationStorePluginImpl gitCollaborationStorePlugin = spy(new
      GitCollaborationStorePluginImpl());
  private GitSourceControlDaoEmptyImpl gitSourceControlDaoMock =
      spy(new GitSourceControlDaoEmptyImpl());

  private static final UserInfo USER = new UserInfo("GitCollaborationStorePluginImplTest_user");
  private static final Id ITEM_ID = new Id();
  private static final Id VERSION_ID = new Id();
  private static final Id ELEMENT_ID = new Id();

  @BeforeClass
  public void init() {
    when(gitCollaborationStorePlugin.getSourceControlDao(anyObject()))
        .thenReturn(gitSourceControlDaoMock);

    Mockito.doNothing().when(gitCollaborationStorePlugin)
        .createItemVersionInt(any(), any(Git.class),
            any(), any(), any());
  }

  @Test
  public void testCreateItem() throws Exception {
    SessionContext context = createSessionContext(USER, "test");
    gitCollaborationStorePlugin.createItem(context, ITEM_ID, null);

    verify(gitSourceControlDaoMock).clone(context, "C:/_dev/Collaboration/git/test/public/BP",
        "C:/_dev/Collaboration/git/test/public\\" + ITEM_ID.getValue().toString(), "main");
  }

  @Test
  public void testDeleteItem() throws Exception {

  }

  @Test
  public void testCreateItemVersion() throws Exception {

    SessionContext context = createSessionContext(USER, "test");


    Info versionInfo = new Info();
    versionInfo.addProperty("prop", "val");

    gitCollaborationStorePlugin.createItemVersion(context, ITEM_ID, null, VERSION_ID,
        versionInfo);

    verify(gitCollaborationStorePlugin).createItemVersionInt(context, null, "main",
        VERSION_ID.getValue().toString(), versionInfo);

  }

  @Test
  public void testCreateEntity() throws Exception {
    SessionContext context = createSessionContext(USER, "test");
    URI nameSpace = new URI("content_0001");
    EntityData entityData = new EntityData();
    entityData.setElementId(ELEMENT_ID);
    entityData.setInfo(new Info());
    entityData.setData(new ByteArrayInputStream("00000000000011111111111111111111".getBytes()));
    gitCollaborationStorePlugin.createEntity(context, ITEM_ID,
        VERSION_ID, nameSpace, entityData);
    verify(gitCollaborationStorePlugin).updateEntityData(context, null,
        "C:/_dev/Collaboration/git/test/private\\users\\GitCollaborationStorePluginImplTest_user" +
            "\\" + ITEM_ID.getValue().toString() + "\\content_0001\\" + entityData.getElementId()
            .getValue().toString(),
        entityData);
    verify(gitSourceControlDaoMock).commit(context, null,
        "Save Item Version");


  }

  @Test
  public void testSaveEntity() throws Exception {

    SessionContext context = createSessionContext(USER, "test");
    URI nameSpace = new URI("content_0001");
    EntityData entityData = new EntityData();
    entityData.setElementId(ELEMENT_ID);
    entityData.setInfo(new Info());
    entityData.setData(new ByteArrayInputStream("00000000000011111111111111111111".getBytes()));
    gitCollaborationStorePlugin.saveEntity(context, ITEM_ID,
        VERSION_ID, nameSpace, entityData);
    verify(gitCollaborationStorePlugin).updateEntityData(context, null,
        "C:/_dev/Collaboration/git/test/private\\users\\GitCollaborationStorePluginImplTest_user" +
            "\\" + ITEM_ID.getValue().toString() + "\\content_0001\\" + entityData.getElementId()
            .getValue().toString(),
        entityData);
    verify(gitSourceControlDaoMock).commit(context, null,
        "Save Item Version");


  }

  @Test
  public void testDeleteEntity() throws Exception {
    SessionContext context = createSessionContext(USER, "test");
    URI nameSpace = new URI("content_0001");
    gitCollaborationStorePlugin.deleteEntity(context, ITEM_ID,
        VERSION_ID, nameSpace, ELEMENT_ID);

    verify(gitSourceControlDaoMock).delete(context, null,
        "C:/_dev/Collaboration/git/test/private\\users\\GitCollaborationStorePluginImplTest_user" +
            "\\" + ITEM_ID.getValue().toString() + "\\content_0001\\" +
            ELEMENT_ID.getValue().toString());
    verify(gitSourceControlDaoMock).commit(context, null,
        "Delete Item Version");
  }

  @Test
  public void testCommitItemVersionEntities() throws Exception {

  }

  @Test
  public void testDeleteItemVersion() throws Exception {

  }

  @Test
  public void testPublishItemVersion() throws Exception {
    SessionContext context = createSessionContext(USER, "test");

    gitCollaborationStorePlugin.publishItemVersion(context, ITEM_ID,
        VERSION_ID, "publish!");

    verify(gitSourceControlDaoMock).publish(context, null, VERSION_ID.getValue().toString());
    /*verify(gitSourceControlDaoMock).commit(context, null,
        "publish!");*/

  }

  @Test
  public void testSyncItemVersion() throws Exception {
    SessionContext context = createSessionContext(USER, "test");

    gitCollaborationStorePlugin.syncItemVersion(context, ITEM_ID, VERSION_ID);

    verify(gitSourceControlDaoMock).sync(context, null,
        VERSION_ID.getValue().toString());
  }

  @Test
  public void testGetItemVersion() throws Exception {

  }


  static SessionContext createSessionContext(UserInfo user, String tenant) {
    SessionContext context = new SessionContext();
    context.setUser(user);
    context.setTenant(tenant);
    return context;
  }


}