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

package com.amdocs.zusammen.plugin.collaborationstore.git.util;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;

public class TestUtil {
  private static final String TENANT = "test";
  private static final String USER = "COLLABORATION_TEST";
  public static SessionContext createSessionContext(String tenant, String user){

    tenant = tenant!=null?tenant:TENANT;
    user = user!=null?user:USER;
    SessionContext context = new SessionContext();
    context.setTenant(tenant);
    context.setUser(new UserInfo(user));

    return context;
  }

  public static SessionContext createSessionContext() {
    return createSessionContext(null,null);
  }
}
