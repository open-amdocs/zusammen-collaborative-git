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
import org.amdocs.zusammen.datatypes.item.Action;
import org.amdocs.zusammen.datatypes.item.ElementContext;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.eclipse.jgit.api.Git;

import java.io.File;

public class ElementCollaborationStore extends CollaborationStore {

  public void create(SessionContext context, CollaborationElement element) {
    String repositoryPath =
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH,
            element.getItemId());
    repositoryPath = resolveTenantPath(context, repositoryPath);
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, element.getVersionId().toString());
    String elementPath =
        sourceControlUtil.getElementRelativePath(element.getNamespace(), element.getId());
    String fullPath = repositoryPath + File.separator + elementPath;


    File elementPathFile = new File(fullPath);
    elementPathFile.mkdirs();

    updateCollaborationElement(context, git, repositoryPath, elementPath, element, Action.CREATE);
    dao.add(context, git, ".");
    dao.commit(context, git, PluginConstants.SAVE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
    //return new CollaborationNamespace(elementPath);
  }

  public void update(SessionContext context, CollaborationElement element) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath = sourceControlUtil.getPrivateRepositoryPath(context,
        PluginConstants.PRIVATE_PATH.replace(PluginConstants.TENANT, context.getTenant()),
        element.getItemId());
    String elementPath =
        sourceControlUtil.getElementRelativePath(element.getNamespace(), element.getId());
    /*String fullPath = repositoryPath + File.separator +
        sourceControlUtil.getElementRelativePath(element.getNamespace(), element.getId());*/
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, element.getVersionId().toString());
    updateCollaborationElement(context, git, repositoryPath, elementPath, element, Action.UPDATE);
    dao.add(context, git, ".");
    dao.commit(context, git, PluginConstants.SAVE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }

  public void delete(SessionContext context, CollaborationElement element) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        sourceControlUtil
            .getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH,
                element.getItemId());
    repositoryPath = resolveTenantPath(context, repositoryPath);
    String fullPath = repositoryPath + File.separator +
        sourceControlUtil.getElementRelativePath(element.getNamespace(), element.getId());
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, element.getVersionId().toString());
    dao.delete(context, git, fullPath);
    dao.commit(context, git, PluginConstants.DELETE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }

  public CollaborationElement get(SessionContext context, ElementContext elementContext,
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
      return uploadCollaborationElement(context, git, elementPath, elementId);
    } finally {
      if (git != null) {
        dao.close(context, git);
      }
    }

  }

  protected void updateCollaborationElement(SessionContext context, Git git,
                                            String basePath,
                                            String relativePath,
                                            CollaborationElement element, Action action) {
    elementUtil.updateCollaborationElement(git, basePath, relativePath, element, action);
  }

  protected CollaborationElement uploadCollaborationElement(SessionContext context, Git git,
                                                            String elementPath,
                                                            Id elementId) {
    return elementUtil.uploadCollaborationElement(git, elementPath, elementId.toString());

  }

}
