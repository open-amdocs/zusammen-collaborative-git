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

import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.commons.datatypes.UserInfo;
import org.amdocs.tsuzammen.commons.datatypes.impl.item.EntityData;
import org.amdocs.tsuzammen.commons.datatypes.item.Info;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.plugin.mocks.GitSourceControlDaoEmptyImpl;
import org.eclipse.jgit.api.Git;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;

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
  public static final UserInfo USER = new UserInfo("GitCollaborationStorePluginImplTest_user");

  @BeforeClass
  public void init() {
    when(gitCollaborationStorePlugin.getSourceControlDao(anyObject()))
        .thenReturn(gitSourceControlDaoMock);

    Mockito.doNothing().when(gitCollaborationStorePlugin)
        .createItemVersionInt(any(), any(Git.class),
            any(), any(), any());
  }

  @org.testng.annotations.Test
  public void testCreateItem() throws Exception {
    SessionContext context = createSessionContext(USER, "test");
    gitCollaborationStorePlugin.createItem(context, "itemID_0001", null);

    verify(gitSourceControlDaoMock).clone(context, "C:/_dev/Collaboration/git/test/public/BP",
        "C:/_dev/Collaboration/git/test/public\\itemID_0001", "main");
  }

  @org.testng.annotations.Test
  public void testDeleteItem() throws Exception {

  }

  @org.testng.annotations.Test
  public void testCreateItemVersion() throws Exception {

    SessionContext context = createSessionContext(USER, "test");


    Info versionInfo = new Info();
    gitCollaborationStorePlugin.createItemVersion(context, "itemID_0001", null, "itemID_0001_0001",
        versionInfo);

    verify(gitCollaborationStorePlugin).createItemVersionInt(context, null, "main",
        "itemID_0001_0001",
        versionInfo);

    verify(gitSourceControlDaoMock).commit(context, null,
        "Create Item Version");
  }

  @org.testng.annotations.Test
  public void testCreateItemVersionEntity() throws Exception {
    SessionContext context = createSessionContext(USER, "test");
    URI nameSpace = new URI("content_0001");
    EntityData entityData = new EntityData();
    entityData.setInfo(new Info());
    entityData.setData("00000000000011111111111111111111".getBytes());
    gitCollaborationStorePlugin.createItemVersionEntity(context, "itemID_0001",
        "itemID_0001_0001", nameSpace,"itemID_0001_0001_0001",entityData);
    verify(gitCollaborationStorePlugin).updateEntityData(context, null,
        "C:/_dev/Collaboration/git/test/private\\users\\GitCollaborationStorePluginImplTest_user" +
            "\\itemID_0001\\content_0001\\itemID_0001_0001_0001",
       entityData);
    verify(gitSourceControlDaoMock).commit(context, null,
        "Save Item Version");


  }

  @org.testng.annotations.Test
  public void testSaveItemVersionEntity() throws Exception {

    SessionContext context = createSessionContext(USER, "test");
    URI nameSpace = new URI("content_0001");
    EntityData entityData = new EntityData();
    entityData.setInfo(new Info());
    entityData.setData("00000000000011111111111111111111".getBytes());
    gitCollaborationStorePlugin.saveItemVersionEntity(context, "itemID_0001",
        "itemID_0001_0001", nameSpace,"itemID_0001_0001_0001",entityData);
    verify(gitCollaborationStorePlugin).updateEntityData(context, null,
        "C:/_dev/Collaboration/git/test/private\\users\\GitCollaborationStorePluginImplTest_user" +
            "\\itemID_0001\\content_0001\\itemID_0001_0001_0001",
        entityData);
    verify(gitSourceControlDaoMock).commit(context, null,
        "Save Item Version");


  }

  @org.testng.annotations.Test
  public void testDeleteItemVersionEntity() throws Exception {
    SessionContext context = createSessionContext(USER, "test");
    URI nameSpace = new URI("content_0001");
    gitCollaborationStorePlugin.deleteItemVersionEntity(context, "itemID_0001",
        "itemID_0001_0001", nameSpace,"itemID_0001_0001_0001");

    verify(gitSourceControlDaoMock).delete(context, null,
        "C:/_dev/Collaboration/git/test/private\\users\\GitCollaborationStorePluginImplTest_user\\itemID_0001\\content_0001\\itemID_0001_0001_0001");
    verify(gitSourceControlDaoMock).commit(context, null,
        "Delete Item Version");
  }

  @org.testng.annotations.Test
  public void testCommitItemVersionEntities() throws Exception {

  }

  @org.testng.annotations.Test
  public void testDeleteItemVersion() throws Exception {

  }

  @org.testng.annotations.Test
  public void testPublishItemVersion() throws Exception {
    SessionContext context = createSessionContext(USER, "test");

    gitCollaborationStorePlugin.publishItemVersion(context, "itemID_0001",
        "itemID_0001_0001","publish!");

    verify(gitSourceControlDaoMock).publish(context, null,
        "itemID_0001_0001");
    /*verify(gitSourceControlDaoMock).commit(context, null,
        "publish!");*/

  }

  @org.testng.annotations.Test
  public void testSyncItemVersion() throws Exception {
    SessionContext context = createSessionContext(USER, "test");

    gitCollaborationStorePlugin.syncItemVersion(context, "itemID_0001",
        "itemID_0001_0001");

    verify(gitSourceControlDaoMock).sync(context, null,
        "itemID_0001_0001");
  }

  @org.testng.annotations.Test
  public void testGetItemVersion() throws Exception {

  }



  static SessionContext createSessionContext(UserInfo user, String tenant) {
    SessionContext context = new SessionContext();
    context.setUser(user);
    context.setTenant(tenant);
    return context;
  }


}