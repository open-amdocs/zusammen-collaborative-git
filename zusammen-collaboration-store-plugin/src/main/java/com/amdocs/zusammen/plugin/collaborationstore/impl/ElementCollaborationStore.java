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

package com.amdocs.zusammen.plugin.collaborationstore.impl;

import com.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.Namespace;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDao;
import com.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDaoFactory;
import com.amdocs.zusammen.plugin.collaborationstore.types.Repository;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;

import java.io.File;
import java.util.Collection;

public class ElementCollaborationStore extends CollaborationStore {

  public ElementCollaborationStore(SourceControlDaoFactory sourceControlDaoFactory) {
    this.sourceControlDaoFactory = sourceControlDaoFactory;
  }

  public void commit(SessionContext context, Id itemId, Id versionId, String message) {
    SourceControlDao dao = getSourceControlDao(context);
    Repository repository = dao.initRepository(context, itemId);
    dao.checkoutBranch(context, repository, versionId);
    dao.commit(context, repository, message);
  }

  public void create(SessionContext context, CollaborationElement element) {
    SourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        sourceControlUtil.getPrivateRepositoryPath(context, PluginConstants.PRIVATE_PATH,
            element.getItemId());
    Repository repository = dao.initRepository(context, element.getItemId());
    dao.checkoutBranch(context, repository, element.getVersionId());
    String elementPath = getSourceControlUtil().getElementRelativePath(element.getNamespace(),
        element.getId());
    String elementFullPath = repositoryPath + File.separator + elementPath;
    File elementPathFile = new File(elementFullPath);
    elementPathFile.mkdirs();
    Collection<String> files = updateCollaborationElement(context, repositoryPath, elementPath,
        element, Action.CREATE);
    dao.store(context, repository, files);
    //dao.commit(context, repository, PluginConstants.CREATE_ELEMENT_MESSAGE);
    dao.close(context, repository);

    //return new CollaborationNamespace(elementPath);
  }

  public void update(SessionContext context, CollaborationElement element) {
    SourceControlDao dao = getSourceControlDao(context);
    String repositoryPath = sourceControlUtil.getPrivateRepositoryPath(context,
        PluginConstants.PRIVATE_PATH.replace(PluginConstants.TENANT, context.getTenant()),
        element.getItemId());
    String elementPath =
        sourceControlUtil.getElementRelativePath(element.getNamespace(), element.getId());
    /*String fullPath = repositoryPath + File.separator +
        sourceControlUtil.getElementRelativePath(element.getNamespace(), element.getId());*/
    Repository repository = dao.initRepository(context, element.getItemId());
    dao.checkoutBranch(context, repository, element.getVersionId());
    Collection<String> files =
        updateCollaborationElement(context, repositoryPath, elementPath, element, Action.UPDATE);
    dao.store(context, repository, files);
    //dao.commit(context, repository, PluginConstants.UPDATE_ELEMENT_MESSAGE);
    dao.close(context, repository);
  }

  public void delete(SessionContext context, CollaborationElement element) {
    SourceControlDao dao = getSourceControlDao(context);
    Repository repository = dao.initRepository(context, element.getItemId());
    dao.checkoutBranch(context, repository, element.getVersionId());
    dao.delete(context, repository,
        getSourceControlUtil().getElementRelativePath(element.getNamespace(), element.getId()));
    //dao.commit(context, repository, PluginConstants.DELETE_ELEMENT_MESSAGE);
    dao.close(context, repository);
  }

  public CollaborationElement get(SessionContext context, ElementContext elementContext,
                                  Namespace namespace, Id elementId) {
    SourceControlDao dao = getSourceControlDao(context);
    String elementPath;
    if (elementId == null) {
      elementId = Id.ZERO;
      elementPath = Namespace.ROOT_NAMESPACE.getValue();
    } else {
      elementPath = getSourceControlUtil().getElementRelativePath(namespace, elementId);
    }
    String repositoryPath = getSourceControlUtil().getPrivateRepositoryPath(context,
        PluginConstants.PRIVATE_PATH.replace(PluginConstants.TENANT, context.getTenant()),
        elementContext.getItemId());

    Repository repository = dao.initRepository(context, elementContext.getItemId());
    try {
      if (elementContext.getRevisionId() == null) {
        dao.checkoutBranch(context, repository, elementContext.getVersionId());
      } else {
        dao.checkoutChange(context, repository, elementContext.getRevisionId());
      }

      // TODO: 4/18/2017 hide the use of file (as this class shouldn't be aware of the repo impl)
      return new File(repositoryPath + File.separator + elementPath).exists()
          ? uploadCollaborationElement(context, elementContext, repositoryPath, elementPath,
          elementId)
          : null;
    } finally {
      if (repository != null) {
        dao.close(context, repository);
      }
    }
  }

  protected Collection<String> updateCollaborationElement(SessionContext context,
                                                          String basePath,
                                                          String relativePath,
                                                          CollaborationElement element,
                                                          Action action) {
    return elementUtil.updateCollaborationElement(basePath, relativePath, element, action);
  }


  protected CollaborationElement uploadCollaborationElement(SessionContext context, ElementContext
      elementContext, String repositoryPath, String elementPath,
                                                            Id elementId) {
    return getElementUtil().uploadCollaborationElement(elementContext, repositoryPath,
        elementPath, elementId
    );

  }


}
