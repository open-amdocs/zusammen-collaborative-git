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
import org.amdocs.zusammen.datatypes.Namespace;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.item.Action;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.util.SourceControlUtil;
import org.amdocs.zusammen.plugin.collaborationstore.git.util.TestUtil;
import org.amdocs.zusammen.sdk.types.ElementData;
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

public class ElementCollaborationStoreTest {
  private static final String PRIVATE_PATH = "/git/test/private" + File.separator
      + "users" + File.separator + "COLLABORATION_TEST" + File.separator;

  @Spy
  private ElementCollaborationStore elementCollaborationStore;
  @Mock
  private GitSourceControlDao gitSourceControlDaoMock;
  @Mock
  private SourceControlUtil sourceControlUtil;

  private static final Id ITEM_ID = new Id();
  private static final Id VERSION_ID = new Id();
  private static final Id ELEMENT_ID = new Id();
  private static final String NAME_SPACE = (new Id()).toString() + File.separator + (new Id());
  private static final SessionContext context = TestUtil.createSessionContext();


  @BeforeMethod
  public void init() {
    MockitoAnnotations.initMocks(this);
    Mockito.doNothing().when(elementCollaborationStore).addFileContent(anyObject(), anyObject(),
        anyObject(),
        anyObject(),
        anyObject(),
        anyObject());

    Mockito.doNothing().when(elementCollaborationStore).updateElementData(anyObject(), anyObject()
        , anyObject(), anyObject(), anyObject(), anyObject());

    when(elementCollaborationStore.getSourceControlDao(anyObject())).thenReturn
        (gitSourceControlDaoMock);
    when(gitSourceControlDaoMock.clone
        (anyObject(), anyObject(), anyObject(), anyObject())).thenReturn(null);
    when(gitSourceControlDaoMock.openRepository
        (anyObject(), anyObject())).thenReturn(null);

    when(gitSourceControlDaoMock.getHead
        (anyObject(), anyObject())).thenReturn(null);

    when(gitSourceControlDaoMock.add
        (anyObject(), anyObject(), anyObject())).thenReturn(null);

    when(gitSourceControlDaoMock.clone
        (anyObject(), anyObject(), anyObject())).thenReturn(null);
  }


  @Test
  public void testCreate() throws Exception {

    Namespace namespace = new Namespace();
    namespace.setValue(NAME_SPACE);
    ElementData elementData =
        new ElementData(ITEM_ID, VERSION_ID, namespace, ELEMENT_ID);
    elementCollaborationStore.create(context, elementData);

    verify(gitSourceControlDaoMock).openRepository(context,
        PRIVATE_PATH + ITEM_ID.toString());

    verify(elementCollaborationStore)
        .updateElementData(context, null, PRIVATE_PATH + ITEM_ID.toString(),
            NAME_SPACE + File.separator + ELEMENT_ID, elementData, Action.CREATE);

  }

  @Test
  public void testSave() throws Exception {
    Namespace namespace = new Namespace();
    namespace.setValue(NAME_SPACE);

    ElementData elementData =
        new ElementData(ITEM_ID, VERSION_ID, namespace, ELEMENT_ID);


    elementCollaborationStore.update(context, elementData);

    verify(gitSourceControlDaoMock).openRepository(context,
        PRIVATE_PATH + ITEM_ID.toString());

    verify(elementCollaborationStore)
        .updateElementData(context, null, PRIVATE_PATH + ITEM_ID.toString(),
            NAME_SPACE + File.separator + ELEMENT_ID,
            elementData, Action.UPDATE);
  }

  @Test
  public void testDelete() throws Exception {

    Namespace namespace = new Namespace();
    namespace.setValue(NAME_SPACE);
    ElementData elementData =
        new ElementData(ITEM_ID, VERSION_ID, namespace, ELEMENT_ID);
    elementCollaborationStore.delete(context, elementData);

    verify(gitSourceControlDaoMock).openRepository(context,
        PRIVATE_PATH + ITEM_ID.toString());

    verify(gitSourceControlDaoMock).delete(context, null,
        PRIVATE_PATH + ITEM_ID.toString() + File.separator + NAME_SPACE + File.separator +
            ELEMENT_ID);
  }

  @Test
  public void testGet() throws Exception {

  }
}