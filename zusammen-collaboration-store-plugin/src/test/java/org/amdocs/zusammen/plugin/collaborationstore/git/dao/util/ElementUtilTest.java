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

package org.amdocs.zusammen.plugin.collaborationstore.git.dao.util;

import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.Namespace;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.item.Action;
import org.amdocs.zusammen.datatypes.item.Info;
import org.amdocs.zusammen.plugin.collaborationstore.dao.util.ElementUtil;
import org.amdocs.zusammen.plugin.collaborationstore.git.util.TestUtil;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ElementUtilTest {
  private static final String PRIVATE_PATH = "/git/test/private" + File.separator
      + "users" + File.separator + "COLLABORATION_TEST" + File.separator;

  //@Spy
  private ElementUtil elementUtil;
  // = spy(new ItemVersionCollaborationStore());
  private static final Id ITEM_ID = new Id();
  private static final Id VERSION_ID = new Id();
  private static final Id ELEMENT_ID = new Id();



  private static final String NAME_SPACE = (new Id()).toString();

  @BeforeMethod
  public void init() {

    MockitoAnnotations.initMocks(this);
    elementUtil = spy(new ElementUtil());
    Collection<String> files = new ArrayList<>();
    files.add("info.json");
    Mockito.doReturn(true).when(elementUtil).addFileContent(
        anyObject(),
        anyObject(),
        anyObject(),
        anyObject());




  }





  @Test
  public void testUpdateCollaborationElementContainDataAndInfo() throws Exception {

    Namespace namespace = new Namespace();
    namespace.setValue(NAME_SPACE);

    CollaborationElement element =
        new CollaborationElement(ITEM_ID, VERSION_ID, namespace, ELEMENT_ID);

    element.setData(new ByteArrayInputStream("testUpdateCollaborationElement()".getBytes()));
    Info info = new Info();
    info.setDescription("testUpdateCollaborationElementContainDataAndInfo");
    element.setInfo(info);
    elementUtil
        .updateCollaborationElement(null, PRIVATE_PATH + ITEM_ID
            .toString(),  element, Action.UPDATE);


    verify(elementUtil, times(4)).addFileContent(anyObject(),
        anyObject(),
        anyObject(),
        anyObject());

  }


  @Test
  public void testAddFileContent() throws Exception {

  }

  @Test
  public void testGetSourceControlDao() throws Exception {

  }

}