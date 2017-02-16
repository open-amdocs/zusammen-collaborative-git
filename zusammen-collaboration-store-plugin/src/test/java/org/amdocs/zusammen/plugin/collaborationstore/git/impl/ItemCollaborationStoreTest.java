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
import org.amdocs.zusammen.datatypes.item.Info;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.util.TestUtil;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ItemCollaborationStoreTest {

  private static final String BP_PATH = "/git/test/public/BP";
  private static final String PUBLIC_PATH = "/git/test/public" + File.separator;
  private static final String PRIVATE_PATH_USER = "/git/test/private" + File.separator + "users"
      + File.separator + "COLLABORATION_TEST" + File.separator;
  private final ItemCollaborationStore itemCollaborationStore = spy(new ItemCollaborationStore());
  @Mock
  private GitSourceControlDao gitSourceControlDaoMock;

  private static final Id ITEM_ID = new Id();

  @BeforeClass
  public void init() {

    MockitoAnnotations.initMocks(this);


    Mockito.doNothing().when(itemCollaborationStore).addFileContent(anyObject(), anyObject(),
        anyObject(),
        anyObject(),
        anyObject(),
        anyObject());

    when(itemCollaborationStore.getSourceControlDao(anyObject())).thenReturn
        (gitSourceControlDaoMock);
    when(gitSourceControlDaoMock.clone
        (anyObject(), anyObject(), anyObject(), anyObject())).thenReturn(null);

    when(gitSourceControlDaoMock.clone
        (anyObject(), anyObject(), anyObject())).thenReturn(null);
  }

  @Test
  public void testCreate() throws Exception {
    SessionContext context = TestUtil.createSessionContext();

    Info info = new Info();
    info.setName("test_name");
    itemCollaborationStore.create(context, ITEM_ID, info);

    verify(itemCollaborationStore.getSourceControlDao(context)).clone(context,
        BP_PATH,
        PUBLIC_PATH + ITEM_ID.toString(), "main");


    verify(itemCollaborationStore)
        .addFileContent(context, null, PRIVATE_PATH_USER + ITEM_ID.getValue(), null,
            "info.json", info);
  }

  @Test
  public void testDelete() throws Exception {
    //todo - method not implemented
  }

}