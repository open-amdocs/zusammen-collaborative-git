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

package org.amdocs.zusammen.plugin.collaborationstore.impl;

import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDaoFactory;

import org.amdocs.zusammen.plugin.collaborationstore.dao.util.ElementUtil;
import org.amdocs.zusammen.plugin.collaborationstore.dao.util.SourceControlUtil;
import org.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants;

public class CollaborationStore {

  ElementUtil elementUtil;
  SourceControlUtil sourceControlUtil;
  SourceControlDaoFactory sourceControlDaoFactory;
  public CollaborationStore(){
    elementUtil = new ElementUtil();
    sourceControlUtil = new SourceControlUtil();
  }

  protected SourceControlDao getSourceControlDao(SessionContext context) {
    return this.sourceControlDaoFactory.createInterface(context);
  }

  protected SourceControlUtil getSourceControlUtil() {
    return this.sourceControlUtil;
  }


  protected boolean addFileContent(SessionContext context, String basePath, String
      relativePath, String fileName, Object fileContent) {
    return getElementUtil().addFileContent(basePath,relativePath, fileName, fileContent);

  }

  protected ElementUtil getElementUtil() {
    return this.elementUtil;
  }

  protected String resolveTenantPath(SessionContext context, String path) {
    String tenant = context.getTenant() != null ? context.getTenant() : "zusammen";
    return path.replace(PluginConstants.TENANT, tenant);
  }

}
