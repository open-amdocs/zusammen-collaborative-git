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

import com.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDao;
import com.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDaoFactory;
import com.amdocs.zusammen.plugin.collaborationstore.dao.util.ElementUtil;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.plugin.collaborationstore.git.util.TestUtil;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class ItemCollaborationStoreTest {



  @Mock
  private ElementUtil elementUtil;


  private static final Id ITEM_ID = new Id();

  @BeforeClass
  public void init() {

    MockitoAnnotations.initMocks(this);


  }

  @Test
  public void testCreate() throws Exception {
    SessionContext context = TestUtil.createSessionContext();

    final ItemCollaborationStoreTestMock.SourceControlDaoMock sourceControlDaoMock =
        new ItemCollaborationStoreTestMock.SourceControlDaoMock();
    SourceControlDaoFactory sourceControlDaoFactoryMock = new SourceControlDaoFactory() {

      @Override
      public SourceControlDao createInterface(SessionContext sessionContext) {
        return sourceControlDaoMock;
      }
    };

    ItemCollaborationStore itemCollaborationStore = spy(new ItemCollaborationStore(
        sourceControlDaoFactoryMock));

    doReturn(elementUtil).when(itemCollaborationStore).getElementUtil();

    Info info = new Info();
    info.setName("test_name");
    itemCollaborationStore.create(context, ITEM_ID, info);


  }

  @Test
  public void testDelete() throws Exception {
    //todo - method not implemented
  }

}