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
import org.amdocs.zusammen.datatypes.Namespace;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.item.ElementContext;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.zusammen.sdk.types.ElementData;
import org.eclipse.jgit.api.Git;

import java.io.File;

public class ElementCollaborationStore extends CollaborationStore {

  public void create(SessionContext context, ElementData elementData) {
    String repositoryPath =
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH,
            elementData.getItemId());
    repositoryPath = resolveTenantPath(context, repositoryPath);
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, elementData.getVersionId().toString());
    String elementPath = sourceControlUtil.getElementRelativePath(elementData.getNamespace(), elementData.getId());
    String fullPath = repositoryPath + File.separator + elementPath;


    File elementPathFile = new File(fullPath);
    elementPathFile.mkdirs();

    updateElementData(context, git, repositoryPath,elementPath, elementData);
    dao.commit(context, git, PluginConstants.SAVE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
    //return new CollaborationNamespace(elementPath);
  }

  public void update(SessionContext context, ElementData elementData) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath = sourceControlUtil.getPrivateRepositoryPath(context,
        PluginConstants.PRIVATE_PATH.replace(PluginConstants.TENANT, context.getTenant()),
        elementData.getItemId());
    String elementPath = sourceControlUtil.getElementRelativePath(elementData.getNamespace(), elementData.getId());
    /*String fullPath = repositoryPath + File.separator +
        sourceControlUtil.getElementRelativePath(elementData.getNamespace(), elementData.getId());*/
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, elementData.getVersionId().toString());
    updateElementData(context, git, repositoryPath,elementPath, elementData);
    dao.commit(context, git, PluginConstants.SAVE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }

  public void delete(SessionContext context, ElementData elementData) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        sourceControlUtil
            .getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH,
                elementData.getItemId());
    repositoryPath = resolveTenantPath(context, repositoryPath);
    String fullPath = repositoryPath + File.separator +
        sourceControlUtil.getElementRelativePath(elementData.getNamespace(), elementData.getId());
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, elementData.getVersionId().toString());
    dao.delete(context, git, fullPath);
    dao.commit(context, git, PluginConstants.DELETE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }

  public ElementData get(SessionContext context, ElementContext elementContext,
                         Namespace namespace, Id elementId) {
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git;

    String elementPath = sourceControlUtil.getElementRelativePath(namespace, elementId);
    String repositoryPath = sourceControlUtil.getPrivateRepositoryPath(context,
        PluginConstants.PRIVATE_PATH.replace(PluginConstants.TENANT, context.getTenant()),
        elementContext.getItemId());
    //String fullPath = repositoryPath + File.separator + elementPath;

    git = dao.openRepository(context, repositoryPath);
    try {
      dao.checkoutBranch(context, git, elementContext.getVersionId().toString());
      return uploadElementData(context, git, elementPath, elementId);
    } finally {
      if (git != null) {
        dao.close(context, git);
      }
    }

  }

  protected void updateElementData(SessionContext context, Git git,
                                   String basePath,
                                   String relativePath,
                                   ElementData elementData) {
    elementDataUtil.updateElementData(context, git,basePath, relativePath, elementData);
  }

  protected ElementData uploadElementData(SessionContext context, Git git, String elementPath,
                                          Id elementId) {
    return elementDataUtil.uploadElementData(git, elementPath, elementId.toString());

  }

}
