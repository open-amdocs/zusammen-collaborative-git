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

import org.amdocs.tsuzammen.commons.datatypes.CollaborationNamespace;
import org.amdocs.tsuzammen.commons.datatypes.Id;
import org.amdocs.tsuzammen.commons.datatypes.Namespace;
import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.commons.datatypes.UserInfo;
import org.amdocs.tsuzammen.commons.datatypes.impl.item.ElementData;
import org.amdocs.tsuzammen.commons.datatypes.item.ElementContext;
import org.amdocs.tsuzammen.commons.datatypes.item.ElementId;
import org.amdocs.tsuzammen.commons.datatypes.item.Info;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.plugin.mocks.GitSourceControlDaoEmptyImpl;
import org.eclipse.jgit.api.Git;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GitCollaborationStorePluginImplTest {

  private GitCollaborationStorePluginImpl gitCollaborationStorePlugin =
      spy(new GitCollaborationStorePluginImpl());
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
        "C:/_dev/Collaboration/git/test/public\\" + ITEM_ID.toString(), "main");
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
        VERSION_ID.toString(), versionInfo);

  }

  @Test
  public void testCreateElement() throws Exception {
    SessionContext context = createSessionContext(USER, "test");

    ElementData elementData = new ElementData();
    elementData.setElementId(new ElementId(ELEMENT_ID, new Info()));
    elementData.setData(new ByteArrayInputStream("00000000000011111111111111111111".getBytes()));
    Namespace parentNamespace = new Namespace();
    
    ElementContext elementContext = new ElementContext();
    elementContext.setItemId(ITEM_ID);
    elementContext.setVersionId(VERSION_ID);

    CollaborationNamespace collaborationNamespace = gitCollaborationStorePlugin
        .createElement(context, elementContext, parentNamespace, elementData);

    Assert.assertEquals(collaborationNamespace.getValue(), File.separator + ELEMENT_ID.toString());

    verify(gitCollaborationStorePlugin).updateElementData(context, null,
        "C:/_dev/Collaboration/git/test/private\\users\\GitCollaborationStorePluginImplTest_user"
            + "\\" + ITEM_ID.toString()
            + "\\\\" + ELEMENT_ID.toString(),
        elementData);
    verify(gitSourceControlDaoMock).commit(context, null, "Save Item Version");
  }

  @Test
  public void testSaveElement() throws Exception {
    SessionContext context = createSessionContext(USER, "test");

    ElementData elementData = new ElementData();
    elementData.setElementId(new ElementId(ELEMENT_ID, new Info()));
    elementData.setData(new ByteArrayInputStream("00000000000011111111111111111111".getBytes()));
    CollaborationNamespace collaborationNamespace =
        new CollaborationNamespace(ELEMENT_ID.toString());

    ElementContext elementContext = new ElementContext();
    elementContext.setItemId(ITEM_ID);
    elementContext.setVersionId(VERSION_ID);

    gitCollaborationStorePlugin
        .saveElement(context, elementContext, collaborationNamespace, elementData);

    verify(gitCollaborationStorePlugin).updateElementData(context, null,
        "C:/_dev/Collaboration/git/test/private\\users\\GitCollaborationStorePluginImplTest_user"
            + "\\" + ITEM_ID.toString()
            + "\\" + ELEMENT_ID.toString(),
        elementData);
    verify(gitSourceControlDaoMock).commit(context, null, "Save Item Version");
  }

  @Test
  public void testDeleteElement() throws Exception {
    SessionContext context = createSessionContext(USER, "test");

    CollaborationNamespace collaborationNamespace =
        new CollaborationNamespace(ELEMENT_ID.toString());

    ElementContext elementContext = new ElementContext();
    elementContext.setItemId(ITEM_ID);
    elementContext.setVersionId(VERSION_ID);

    gitCollaborationStorePlugin
        .deleteElement(context, elementContext, collaborationNamespace, ELEMENT_ID);

    verify(gitSourceControlDaoMock).delete(context, null,
        "C:/_dev/Collaboration/git/test/private\\users\\GitCollaborationStorePluginImplTest_user"
            + "\\" + ITEM_ID.toString()
            + "\\" + ELEMENT_ID.toString());
    verify(gitSourceControlDaoMock).commit(context, null, "Delete Item Version");
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

    gitCollaborationStorePlugin.publishItemVersion(context, ITEM_ID, VERSION_ID, "publish!");

    verify(gitSourceControlDaoMock).publish(context, null, VERSION_ID.toString());
    /*verify(gitSourceControlDaoMock).commit(context, null,
        "publish!");*/
  }

  @Test
  public void testSyncItemVersion() throws Exception {
    SessionContext context = createSessionContext(USER, "test");

    gitCollaborationStorePlugin.syncItemVersion(context, ITEM_ID, VERSION_ID);

    verify(gitSourceControlDaoMock).sync(context, null, VERSION_ID.toString());
  }

  @Test
  public void testGetItemVersion() throws Exception {

  }

  private static SessionContext createSessionContext(UserInfo user, String tenant) {
    SessionContext context = new SessionContext();
    context.setUser(user);
    context.setTenant(tenant);
    return context;
  }


}