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

package org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.util;

import org.amdocs.tsuzammen.datatypes.Id;
import org.amdocs.tsuzammen.datatypes.Namespace;
import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.datatypes.item.ElementContext;
import org.amdocs.tsuzammen.datatypes.item.Info;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.util.TestUtil;
import org.amdocs.tsuzammen.sdk.types.ElementData;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ElementDataUtilTest {


  //@Spy
  private ElementDataUtil elementDataUtil;
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
  private static final String NAME_SPACE = (new Id()).toString() + File.separator + (new Id())
      .toString();

  @BeforeMethod
  public void init() {

    MockitoAnnotations.initMocks(this);


    elementDataUtil = spy(new ElementDataUtil());
    Mockito.doNothing().when(elementDataUtil).addFileContent(anyObject(), anyObject(),
        anyObject(),
        anyObject(),
        anyObject());



    /*Mockito.doNothing().when(elementDataUtil).getSubElementIds(anyObject(), anyObject(),
        anyObject());*/


  }


  @Test
  public void testUploadElementData() throws Exception {
    /*Namespace namespace = new Namespace();
    namespace.setValue(NAME_SPACE);

   when(elementDataUtil.getSubElementIds(context, null,"/git/test/private\\users\\COLLABORATION_TEST\\"+ITEM_ID
        .toString()+"\\"+NAME_SPACE)).thenReturn(null);

    when(elementDataUtil
        .uploadElementInfo(context, null, "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID
            .toString() + "\\" + NAME_SPACE)).thenReturn(new ElementInfo(new Id()));

    elementDataUtil
        .uploadElementData(context, null, "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID
            .toString() + "\\" + NAME_SPACE);

    verify(elementDataUtil, times(1)).getFileContent(anyObject(), anyObject(), anyObject(),
        anyObject());
*/
  }

  @Test
  public void testUploadElementInfo() throws Exception {

/*
    Namespace namespace = new Namespace();
    namespace.setValue(NAME_SPACE);

    elementDataUtil.uploadElementInfo(context,null,
        "/git/test/private\\users\\COLLABORATION_TEST\\"+ITEM_ID
        .toString()+"\\"+NAME_SPACE);

    verify(elementDataUtil,times(1)).getFileContent(anyObject(),anyObject(),anyObject(),
        anyObject());
*/

  }

  @Test
  public void testUpdateElementDataContainData() throws Exception {

    Namespace namespace = new Namespace();
    namespace.setValue(NAME_SPACE);

    ElementData elementData = new ElementData();
    elementData.setElementImplClass(ElementDataUtilTest.class);
    elementData.setData(new ByteArrayInputStream("testUpdateElementData()".getBytes()));

    elementDataUtil
        .updateElementData(context, null, "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID
            .toString() + "\\" + NAME_SPACE, elementData);


    verify(elementDataUtil, times(3)).addFileContent(anyObject(), anyObject(), anyObject(),
        anyObject(),
        anyObject());

  }

  @Test
  public void testUpdateElementDataContainDataAndInfo() throws Exception {

    Namespace namespace = new Namespace();
    namespace.setValue(NAME_SPACE);

    ElementData elementData = new ElementData();
    elementData.setElementImplClass(ElementDataUtilTest.class);
    elementData.setData(new ByteArrayInputStream("testUpdateElementData()".getBytes()));
    Info info = new Info();
    info.setDescription("testUpdateElementDataContainDataAndInfo");
    elementData.setInfo(info);
    elementDataUtil
        .updateElementData(context, null, "/git/test/private\\users\\COLLABORATION_TEST\\" + ITEM_ID
            .toString() + "\\" + NAME_SPACE, elementData);


    verify(elementDataUtil, times(4)).addFileContent(anyObject(), anyObject(), anyObject(),
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