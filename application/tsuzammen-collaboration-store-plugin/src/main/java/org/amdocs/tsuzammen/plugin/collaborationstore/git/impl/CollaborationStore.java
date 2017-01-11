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

import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.SourceControlDaoFactory;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.util.ElementDataUtil;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.util.SourceControlUtil;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.eclipse.jgit.api.Git;

public class CollaborationStore {

  protected final ElementDataUtil elementDataUtil = new ElementDataUtil();
  protected final SourceControlUtil sourceControlUtil = new SourceControlUtil();

  protected GitSourceControlDao getSourceControlDao(SessionContext context) {
    return SourceControlDaoFactory.getInstance().createInterface(context);
  }

  protected void addFileContent(SessionContext context, Git git, String path,
                             String fileName, Object fileContent) {
    this.elementDataUtil.addFileContent(context, git, path, fileName, fileContent);
  }

  protected String resolveTenantPath(SessionContext context, String path) {
    String tenant = context.getTenant() != null ? context.getTenant() : "tsuzammen";
    return path.replace(PluginConstants.TENANT, tenant);
  }

}
