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

import org.amdocs.tsuzammen.datatypes.Id;
import org.amdocs.tsuzammen.datatypes.Namespace;
import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.datatypes.item.ElementContext;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.util.SourceControlUtil;
import org.amdocs.tsuzammen.sdk.types.ElementData;
import org.eclipse.jgit.api.Git;

import java.io.File;

public class ElementCollaborationStore extends CollaborationStore{

  public void create(SessionContext context, ElementContext elementContext,
                            Namespace namespace, ElementData elementData) {
    String repositoryPath =
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH,
            elementContext.getItemId());
    repositoryPath = resolveTenantPath(context, repositoryPath);
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, elementContext.getVersionId().toString());

    String elementPath =
        namespace.getValue().replace(Namespace.NAMESPACE_DELIMITER, File.separator);
    String fullPath = repositoryPath + File.separator + elementPath;

    File elementPathFile = new File(fullPath);
    elementPathFile.mkdirs();

    updateElementData(context, git, fullPath, elementData);
    dao.commit(context, git, PluginConstants.SAVE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
    //return new CollaborationNamespace(elementPath);
  }

  public void save(SessionContext context, ElementContext elementContext,
                          Namespace namespace, ElementData elementData) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String elementPath = namespace.getValue().replace(Namespace.NAMESPACE_DELIMITER, File.separator);
    String repositoryPath = sourceControlUtil.getPrivateRepositoryPath(context,
        PluginConstants.PRIVATE_PATH.replace(PluginConstants.TENANT, context.getTenant()),
        elementContext.getItemId());
    String fullPath = repositoryPath + File.separator + elementPath;
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, elementContext.getVersionId().toString());
    updateElementData(context, git, fullPath, elementData);
    dao.commit(context, git, PluginConstants.SAVE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }

  public void delete(SessionContext context, ElementContext elementContext,
                            Namespace namespace, ElementData elementData) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String elementPath = namespace.getValue();
    String repositoryPath =
        sourceControlUtil
            .getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH,
                elementContext.getItemId());
    repositoryPath = resolveTenantPath(context, repositoryPath);
    String fullPath = repositoryPath + File.separator + elementPath;
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, elementContext.getVersionId().toString());
    dao.delete(context, git, fullPath);
    dao.commit(context, git, PluginConstants.DELETE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }

  public ElementData get(SessionContext context, ElementContext elementContext,
                                Namespace namespace, Id elementId) {
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git;

    String elementPath = namespace.getValue();
    String repositoryPath = sourceControlUtil.getPrivateRepositoryPath(context,
        PluginConstants.PRIVATE_PATH.replace(PluginConstants.TENANT, context.getTenant()),
        elementContext.getItemId());
    String fullPath = repositoryPath + File.separator + elementPath;

    git = dao.openRepository(context, repositoryPath);
    try {
      dao.checkoutBranch(context, git, elementContext.getVersionId().toString());
      ElementData elementData = uploadElementData(context, git, fullPath);
      return elementData;
    } finally {
      if (git != null) {
        dao.close(context, git);
      }
    }

  }

  protected void updateElementData(SessionContext context, Git git, String elementPath,
                                   ElementData elementData) {
    elementDataUtil.updateElementData(context, git, elementPath, elementData);
  }

  protected ElementData uploadElementData(SessionContext context, Git git, String elementPath
  ) {
    return elementDataUtil.uploadElementData(context, git, elementPath);

  }

}
