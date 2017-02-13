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

import org.amdocs.zusammen.plugin.collaborationstore.git.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CollaborationStoreTest {


  @Test
  public void testResolveTenantPath() throws Exception {
    CollaborationStore collaborationStore = new CollaborationStore();
    String path = collaborationStore.resolveTenantPath(TestUtil.createSessionContext(),
        "/prefixPath/{tenant}/suffixPath");
    Assert.assertEquals(path, "/prefixPath/test/suffixPath");
  }

}