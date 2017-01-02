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

package org.amdocs.tsuzammen.plugin.collaborationstore.git.plugin;

import org.amdocs.tsuzammen.commons.configuration.impl.ConfigurationAccessor;
import org.amdocs.tsuzammen.datatypes.CollaborationNamespace;
import org.amdocs.tsuzammen.datatypes.Id;
import org.amdocs.tsuzammen.datatypes.Namespace;
import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.datatypes.collaboration.PublishResult;
import org.amdocs.tsuzammen.datatypes.collaboration.SyncResult;
import org.amdocs.tsuzammen.datatypes.item.ElementContext;
import org.amdocs.tsuzammen.datatypes.item.Info;
import org.amdocs.tsuzammen.datatypes.item.ItemVersion;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.SourceControlDaoFactory;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.SourceControlUtil;
import org.amdocs.tsuzammen.sdk.CollaborationStore;
import org.amdocs.tsuzammen.sdk.types.ElementData;
import org.amdocs.tsuzammen.sdk.utils.SdkConstants;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;

import java.io.File;
import java.io.InputStream;

import static org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants.INFO_FILE_NAME;
import static org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants.ITEM_VERSION_INFO_FILE_NAME;

public class GitCollaborationStorePluginImpl implements CollaborationStore {

  private static final String ADD_ITEM_VERSION_INFO_MESSAGE = "Add Item Version Info";
  private static final String SAVE_ITEM_VERSION_MESSAGE = "Save Item Version";
  private static final String DELETE_ITEM_VERSION_MESSAGE = "Delete Item Version";
  private static final String ADD_ITEM_INFO_MESSAGE = "Add Item Info";
  private static String PUBLIC_PATH = ConfigurationAccessor.getPluginProperty(SdkConstants
      .TSUZAMMEN_COLLABORATIVE_STORE, PluginConstants.PUBLIC_PATH);
  private static String PRIVATE_PATH = ConfigurationAccessor.getPluginProperty(
      SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE, PluginConstants.PRIVATE_PATH);
  private static String PUBLIC_URL = ConfigurationAccessor.getPluginProperty(
      SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE, PluginConstants.PUBLIC_URL);
  private static String BP_PATH = ConfigurationAccessor.getPluginProperty(
      SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE, PluginConstants.BP_PATH);
  private static String TENANT = "{tenant}";

  @Override
  public void createItem(SessionContext context, Id itemId, Info info) {

    GitSourceControlDao dao = getSourceControlDao(context);
    String itemPublicPath = PUBLIC_PATH + File.separator + itemId;
    itemPublicPath = resolveTenantPath(context, itemPublicPath);
    String itemPrivatePath =
        PRIVATE_PATH + File.separator + "users" + File.separator +
            context.getUser().getUserName() + File.separator + itemId;
    itemPrivatePath = resolveTenantPath(context, itemPrivatePath);
    String itemPublicUrl = PUBLIC_URL +
        "/" +
        itemId;

    itemPublicUrl = resolveTenantPath(context, itemPublicUrl);

    String bluePrintPath = resolveTenantPath(context, BP_PATH); /*todo - add item type to the blue print*/


    String initialVersion = ConfigurationAccessor.getPluginProperty(SdkConstants
        .TSUZAMMEN_COLLABORATIVE_STORE, PluginConstants.INITIAL_BRANCH);
    Git git = dao.clone(context, bluePrintPath, itemPublicPath, initialVersion);
    dao.clone(context, itemPublicUrl, itemPrivatePath, null);
    if (info != null) {
      addFileContent(context, git, itemPrivatePath, INFO_FILE_NAME, info);
      dao.commit(context, git, ADD_ITEM_INFO_MESSAGE);
    }

    dao.close(context, git);
  }

  @Override
  public void deleteItem(SessionContext context, Id itemId) {
    /*todo*/

  }

  @Override
  public void createItemVersion(SessionContext context, Id itemId, Id baseVersionId,
                                Id versionId,
                                Info versionInfo) {
    String baseBranchId = baseVersionId == null
        ? ConfigurationAccessor.getPluginProperty
        (SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE, PluginConstants.INITIAL_BRANCH)
        : baseVersionId.getValue().toString();
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH, itemId);
    repositoryPath = resolveTenantPath(context, repositoryPath);
    Git git = dao.openRepository(context, repositoryPath);
    createItemVersionInt(context, git, baseBranchId, versionId.getValue().toString(), versionInfo);

    dao.close(context, git);
  }

  @Override
  public void saveItemVersion(SessionContext context, Id itemId, Id versionId,
                              Info versionInfo) {
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH, itemId);
    repositoryPath = resolveTenantPath(context, repositoryPath);
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId.getValue().toString());

    dao.commit(context, git, SAVE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }


  @Override
  public CollaborationNamespace createElement(SessionContext context, ElementContext elementContext,
                                              Namespace parentNamespace, ElementData elementData) {
    String repositoryPath = SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH,
        elementContext.getItemId());
    repositoryPath = resolveTenantPath(context, repositoryPath);
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, elementContext.getVersionId().toString());

    String elementPath = getNamespacePath(parentNamespace, elementData.getId());
    String fullPath = repositoryPath + File.separator + elementPath;

    File elementPathFile = new File(fullPath);
    elementPathFile.mkdirs();

    updateElementData(context, git, fullPath, elementData);
    dao.commit(context, git, SAVE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
    return new CollaborationNamespace(elementPath);
  }


  @Override
  public void saveElement(SessionContext context, ElementContext elementContext,
                          CollaborationNamespace collaborationNamespace, ElementData elementData) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String elementPath = collaborationNamespace.getValue();
    String repositoryPath = SourceControlUtil.getPrivateRepositoryPath(context,
        PRIVATE_PATH.replace(TENANT, context.getTenant()),
        elementContext.getItemId());
    String fullPath = repositoryPath + File.separator + elementPath;
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, elementContext.getVersionId().toString());
    updateElementData(context, git, fullPath, elementData);
    dao.commit(context, git, SAVE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }

  @Override
  public void deleteElement(SessionContext context, ElementContext elementContext,
                            CollaborationNamespace collaborationNamespace, Id elementId) {

    GitSourceControlDao dao = getSourceControlDao(context);
    String elementPath = collaborationNamespace.getValue();
    String repositoryPath =
        SourceControlUtil
            .getPrivateRepositoryPath(context, PRIVATE_PATH, elementContext.getItemId());
    repositoryPath = resolveTenantPath(context, repositoryPath);
    String fullPath = repositoryPath + File.separator + elementPath;
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, elementContext.getVersionId().toString());
    dao.delete(context, git, fullPath);
    dao.commit(context, git, DELETE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);

  }



  @Override
  public void deleteItemVersion(SessionContext sessionContext, Id itemId, Id versionId) {
    /*todo*/
  }

  @Override
  public PublishResult publishItemVersion(SessionContext context, Id itemId, Id versionId,
                                          String message) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH, itemId);
    repositoryPath = resolveTenantPath(context, repositoryPath);
    Git git = dao.openRepository(context, repositoryPath);
    String branchId = versionId.getValue().toString();
    dao.publish(context, git, branchId);
    dao.checkoutBranch(context, git, branchId);
//    dao.inComing(context,git,versionId.getValue());
    dao.close(context, git);
    return null;
  }

  @Override
  public SyncResult syncItemVersion(SessionContext context, Id itemId, Id versionId) {
    SyncResult result;
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH, itemId);
    repositoryPath = resolveTenantPath(context, repositoryPath);
    Git git;
    String branchId = versionId.getValue().toString();
    if (FileUtils.exists(repositoryPath)) {
      git = dao.openRepository(context, repositoryPath);
      PullResult syncResult = dao.sync(context, git, branchId);
      result = SourceControlUtil.handleSyncResult(repositoryPath,syncResult);
    } else {
      String publicPath = resolveTenantPath(context, PUBLIC_PATH);
      git = dao.clone(context,
          SourceControlUtil.getPublicRepositoryPath(context, publicPath, itemId),
          repositoryPath, branchId);
      result = new SyncResult();
      result.setResultStatus(true);
    }
    dao.checkoutBranch(context, git, branchId);
    dao.close(context, git);
    return result;
  }

  @Override
  public ItemVersion getItemVersion(SessionContext sessionContext, Id itemId, Id versionId,
                                    ItemVersion itemVersion) {
    return null;
  }

  @Override
  public ElementData getElement(SessionContext context, ElementContext elementContext,
                                CollaborationNamespace collaborationNamespace, Id elementId) {
    return null;
  }

  protected void updateElementData(SessionContext context, Git git, String elementPath, ElementData
      elementData) {
    addFileContent(context, git,
        elementPath, PluginConstants.IMPL_FILE_NAME, elementData.getElementImplClass().getName());
    if (elementData.getRelations() != null) {
      addFileContent(context, git,
          elementPath, PluginConstants.RELATIONS_FILE_NAME, elementData.getRelations());
    }
    if (elementData.getVisualization() != null) {
      addFileContent(context, git,
          elementPath, PluginConstants.VISUALIZATION_FILE_NAME, elementData.getVisualization());
    }

    if (elementData.getData() != null) {
      addFileContent(context, git,
          elementPath, PluginConstants.DATA_FILE_NAME, elementData.getData());
    }

    Info info = elementData.getInfo();
    if (info != null && info.getProperties() != null && !info.getProperties().isEmpty()) {
      addFileContent(context, git,
          elementPath, PluginConstants.INFO_FILE_NAME, info);
    }
  }


  protected void createItemVersionInt(SessionContext context, Git git, String baseBranch,
                                      String branch, Info versionInfo) {
    GitSourceControlDao dao = getSourceControlDao(context);
    dao.createBranch(context, git, baseBranch, branch);

    if (versionInfo == null) {
      return;
    }

    addFileContent(
        context,
        git,
        SourceControlUtil.getPrivateRepositoryPath(git) + File.separator,
        ITEM_VERSION_INFO_FILE_NAME,
        versionInfo);
    dao.commit(context, git, ADD_ITEM_VERSION_INFO_MESSAGE);
  }

  protected String getNamespacePath(Namespace namespace, Id elementId) {
    if(namespace.getValue() == null || "".equals(namespace.getValue())) return elementId.toString();
    return namespace.getValue() + File.separator + elementId.toString();
  }

  protected GitSourceControlDao getSourceControlDao(SessionContext context) {
    return SourceControlDaoFactory.getInstance().createInterface(context);
  }

  private void addFileContent(SessionContext context, Git git, String path, String fileName,
                              Object
                                  fileContent) {

    if (fileContent instanceof InputStream) {
      FileUtils
          .writeFileFromInputStream(path, ITEM_VERSION_INFO_FILE_NAME, (InputStream) fileContent);
    } else {
      FileUtils.writeFile(path, ITEM_VERSION_INFO_FILE_NAME, fileContent);
    }
    getSourceControlDao(context).add(context, git, new File(path + File.separator + fileName));
  }

  private String resolveTenantPath(SessionContext context, String path) {
    String tenant = context.getTenant() != null ? context.getTenant() : "tsuzammen";
    return path.replace(TENANT, tenant);
  }

}