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

import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.SourceControlDaoFactory;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.util.ElementDataUtil;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.util.SourceControlUtil;
import org.amdocs.zusammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.eclipse.jgit.api.Git;

public class CollaborationStore {

  protected ElementDataUtil elementDataUtil;
  protected SourceControlUtil sourceControlUtil;

  public CollaborationStore(){
    elementDataUtil = new ElementDataUtil();
    sourceControlUtil = new SourceControlUtil();
  }


  protected GitSourceControlDao getSourceControlDao(SessionContext context) {
    return SourceControlDaoFactory.getInstance().createInterface(context);
  }

  protected void addFileContent(SessionContext context, Git git, String basePath,String
      relativePath,String fileName, Object fileContent) {
    this.elementDataUtil.addFileContent(basePath,relativePath, fileName, fileContent);
  }

  protected String resolveTenantPath(SessionContext context, String path) {
    String tenant = context.getTenant() != null ? context.getTenant() : "zusammen";
    return path.replace(PluginConstants.TENANT, tenant);
  }

}
