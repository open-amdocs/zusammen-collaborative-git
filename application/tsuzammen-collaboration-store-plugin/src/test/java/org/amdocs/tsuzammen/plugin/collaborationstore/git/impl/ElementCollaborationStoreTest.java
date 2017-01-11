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
import org.amdocs.tsuzammen.datatypes.Namespace;
import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.datatypes.item.ElementContext;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.util.SourceControlUtil;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.util.TestUtil;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.tsuzammen.sdk.types.ElementData;
import org.eclipse.jgit.api.Git;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class ElementCollaborationStoreTest {


  @Spy
  private ElementCollaborationStore elementCollaborationStore;//
  // = spy(new ItemVersionCollaborationStore());
  @Mock
  private GitSourceControlDao gitSourceControlDaoMock;
  @Mock
  private SourceControlUtil sourceControlUtil;

  private static final Id ITEM_ID = new Id();
  private static final Id VERSION_ID = new Id();
  private static final Id BASE_VERSION_ID = new Id();
  private static final SessionContext context = TestUtil.createSessionContext();
  private static final ElementContext elementContext = new ElementContext(ITEM_ID.toString(),
      VERSION_ID.toString());

  @BeforeMethod
  public void init() {

    MockitoAnnotations.initMocks(this);




    Mockito.doNothing().when(elementCollaborationStore).addFileContent(anyObject(), anyObject(),
        anyObject(),
        anyObject(),
        anyObject());

    Mockito.doNothing().when(elementCollaborationStore).updateElementData(anyObject(),anyObject()
        ,anyObject(),anyObject());

    when(elementCollaborationStore.getSourceControlDao(anyObject())).thenReturn
        (gitSourceControlDaoMock);
    when(gitSourceControlDaoMock.clone
        (anyObject(), anyObject(), anyObject(), anyObject())).thenReturn(null);
    when(gitSourceControlDaoMock.openRepository
        (anyObject(), anyObject())).thenReturn(null);

    when(gitSourceControlDaoMock.getHead
        (anyObject(), anyObject())).thenReturn(null);

    when(gitSourceControlDaoMock.add
        (anyObject(), anyObject(),anyObject())).thenReturn(null);

    when(gitSourceControlDaoMock.clone
        (anyObject(), anyObject(), anyObject())).thenReturn(null);
  }



  @Test
  public void testCreate() throws Exception {
    ElementData elementData = new ElementData();
    Namespace namespace = new Namespace();
    namespace.setValue("10000/20000");

    elementCollaborationStore.create(context,elementContext,namespace,elementData);

    verify(gitSourceControlDaoMock).openRepository(context,
        "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.getValue().toString());

    verify(elementCollaborationStore).updateElementData(context,null,
        "/git/test/private\\users\\COLLABORATION_TEST\\"+ITEM_ID
            .toString()+"\\10000\\20000",elementData);

  }

  @Test
  public void testSave() throws Exception {

    ElementData elementData = new ElementData();
    Namespace namespace = new Namespace();
    namespace.setValue("10000/20000");

    elementCollaborationStore.save(context,elementContext,namespace,elementData);

    verify(gitSourceControlDaoMock).openRepository(context,
        "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.getValue().toString());

    verify(elementCollaborationStore).updateElementData(context,null,
        "/git/test/private\\users\\COLLABORATION_TEST\\"+ITEM_ID
            .toString()+"\\10000\\20000",elementData);
  }

  @Test
  public void testDelete() throws Exception {
    ElementData elementData = new ElementData();
    Namespace namespace = new Namespace();
    namespace.setValue("10000/20000");

    elementCollaborationStore.delete(context,elementContext,namespace,elementData);

    verify(gitSourceControlDaoMock).openRepository(context,
        "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID.toString());

    verify(gitSourceControlDaoMock).delete(context,
        null,"/git/test/private\\users\\COLLABORATION_TEST\\"+ITEM_ID.toString()+"\\10000/20000");
  }

  @Test
  public void testGet() throws Exception {

  }


}