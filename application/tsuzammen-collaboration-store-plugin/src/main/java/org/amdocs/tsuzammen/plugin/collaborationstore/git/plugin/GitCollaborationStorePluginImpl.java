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
import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.commons.datatypes.impl.item.EntityData;
import org.amdocs.tsuzammen.commons.datatypes.item.Info;
import org.amdocs.tsuzammen.commons.datatypes.item.ItemVersion;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.SourceControlDaoFactory;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.SourceControlUtil;
import org.amdocs.tsuzammen.sdk.CollaborationStore;
import org.amdocs.tsuzammen.sdk.utils.SdkConstants;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

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
  public void createItem(SessionContext context, String itemId, Info info) {

    GitSourceControlDao dao = getSourceControlDao(context);
    String itemPublicPath = PUBLIC_PATH + File.separator + itemId;
    itemPublicPath = resolveTenantPath(context,itemPublicPath);
    String itemPrivatePath =
        PRIVATE_PATH + File.separator + "users" + File.separator +
            context.getUser().getUserName() + File.separator + itemId;
    itemPrivatePath = resolveTenantPath(context,itemPrivatePath);
    String itemPublicUrl = PUBLIC_URL +
        "/" +
        itemId;

    itemPublicUrl = resolveTenantPath(context,itemPublicUrl);

    String bluePrintPath = resolveTenantPath(context,BP_PATH); /*todo - add item type to the blue print*/



    String initialVersion = ConfigurationAccessor.getPluginProperty(SdkConstants
        .TSUZAMMEN_COLLABORATIVE_STORE, PluginConstants.INITIAL_BRANCH);
    Git git = dao.clone(context, bluePrintPath, itemPublicPath, initialVersion);
    dao.clone(context, itemPublicUrl, itemPrivatePath, null);
    if(info != null)
    {
      addFileContent(context,git,itemPrivatePath,INFO_FILE_NAME,info);
      dao.commit(context,git,ADD_ITEM_INFO_MESSAGE);
    }

    dao.close(context, git);
  }

  @Override
  public void deleteItem(SessionContext context, String itemId) {
    /*todo*/

  }

  @Override
  public void createItemVersion(SessionContext context, String itemId, String baseVersionId,
                                String versionId,
                                Info versionInfo) {
    baseVersionId = baseVersionId == null ? ConfigurationAccessor.getPluginProperty(SdkConstants
        .TSUZAMMEN_COLLABORATIVE_STORE, PluginConstants.INITIAL_BRANCH) : baseVersionId;
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH,itemId);
    repositoryPath = resolveTenantPath(context,repositoryPath);
    Git git = dao.openRepository(context, repositoryPath);
    createItemVersionInt(context, git, baseVersionId, versionId, versionInfo);

    dao.close(context, git);
  }

  @Override
  public void saveItemVersion(SessionContext context, String itemId, String versionId,
                       Info versionInfo) {
    String repositoryPath = SourceControlUtil.getPrivateRepositoryPath(context,PRIVATE_PATH,itemId);
    repositoryPath = resolveTenantPath(context,repositoryPath);
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId);

    dao.commit(context,git,SAVE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }


  @Override
  public void createItemVersionEntity(SessionContext context, String itemId, String
      versionId,
                                      URI namespace, String entityId, EntityData entityData) {
    String repositoryPath = SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH,
        itemId);
    repositoryPath = resolveTenantPath(context,repositoryPath);
    GitSourceControlDao dao = getSourceControlDao(context);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId);

    String entityPath = getNamespacePath(namespace, entityId);
    String fullPath = repositoryPath + File.separator + entityPath;

    File entityPathFile = new File(fullPath);
    entityPathFile.mkdirs();

    updateEntityData(context, git, fullPath, entityData);
    dao.commit(context, git, SAVE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }


  @Override
  public void saveItemVersionEntity(SessionContext context, String itemId, String
      versionId,
                                    URI namespace, String entityId, EntityData entityData) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String entityPath = getNamespacePath(namespace, entityId);
    String repositoryPath = SourceControlUtil.getPrivateRepositoryPath(
        context,
        PRIVATE_PATH.replace(TENANT, context.getTenant()),
        itemId);
    String fullPath = repositoryPath + File.separator + entityPath;
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId);
    updateEntityData(context, git, fullPath, entityData);
    dao.commit(context, git, SAVE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }

  @Override
  public void deleteItemVersionEntity(SessionContext context, String itemId, String
      versionId, URI
                                          namespace, String entityId) {

    GitSourceControlDao dao = getSourceControlDao(context);
    String entityPath = getNamespacePath(namespace, entityId);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH,itemId);
    repositoryPath = resolveTenantPath(context,repositoryPath);
    String fullPath = repositoryPath + File.separator + entityPath;
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId);
    dao.delete(context, git, fullPath);
    dao.commit(context, git, DELETE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);

  }

  @Override
  public void commitItemVersionEntities(SessionContext sessionContext, String itemId, String
      versionId, String message) {

  }

  @Override
  public void deleteItemVersion(SessionContext sessionContext, String itemId, String versionId) {
    /*todo*/
  }

  @Override
  public void publishItemVersion(SessionContext context, String itemId, String versionId,
                                 String message) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH,itemId);
    repositoryPath = resolveTenantPath(context,repositoryPath);
    Git git = dao.openRepository(context, repositoryPath);
    dao.publish(context, git, versionId);
    dao.checkoutBranch(context, git, versionId);
//    dao.inComing(context,git,versionId.getValue());
    dao.close(context, git);
  }

  @Override
  public void syncItemVersion(SessionContext context, String itemId, String versionId) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH,itemId);
    repositoryPath = resolveTenantPath(context,repositoryPath);
    Git git;
    if (FileUtils.exists(repositoryPath)) {
      git = dao.openRepository(context, repositoryPath);
      dao.sync(context, git, versionId);
    } else {
      String publicPath = resolveTenantPath(context,PUBLIC_PATH);
      git = dao.clone(context,
          SourceControlUtil.getPublicRepositoryPath(context,publicPath,itemId),
          repositoryPath, versionId);
    }
    dao.checkoutBranch(context, git, versionId);
    dao.close(context, git);

  }

  @Override
  public ItemVersion getItemVersion(SessionContext sessionContext, String itemId, String versionId,
                                    ItemVersion itemVersion) {
    return null;
  }

  protected void updateEntityData(SessionContext context, Git git, String entityPath, EntityData
      entityData) {
    GitSourceControlDao dao = getSourceControlDao(context);
    File file2Add;
    if (entityData.getRelations() != null) {
      addFileContent(context,git,
          entityPath,PluginConstants.RELATIONS_FILE_NAME,entityData.getRelations());
    }
    if (entityData.getVisualization() != null) {
      addFileContent(context,git,
          entityPath,PluginConstants.VISUALIZATION_FILE_NAME,entityData.getVisualization());
    }

    if (entityData.getData() != null) {
      addFileContent(context,git,
          entityPath,PluginConstants.DATA_FILE_NAME,entityData.getData());
    }

    if (entityData.getInfo() != null && entityData.getInfo().getProperties() != null &&
        !entityData.getInfo().getProperties().isEmpty()) {
      addFileContent(context,git,
          entityPath,PluginConstants.INFO_FILE_NAME,entityData.getInfo());
    }
  }


  protected void createItemVersionInt(SessionContext context, Git git, String baseBranch, String
      branch,
                                      Info versionInfo) {
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

  protected String getNamespacePath(URI namespace, String entityId) {
    return namespace.getPath() + File.separator + entityId;
  }

  protected GitSourceControlDao getSourceControlDao(SessionContext context) {
    return SourceControlDaoFactory.getInstance().createInterface(context);
  }

  private void addFileContent(SessionContext context, Git git, String path,String fileName,
                              Object
                              fileContent) {

    if(fileContent instanceof InputStream){
      FileUtils.writeFileFromInputStream(path,ITEM_VERSION_INFO_FILE_NAME,(InputStream) fileContent);
    }else{
      FileUtils.writeFile(path,ITEM_VERSION_INFO_FILE_NAME,fileContent);
    }
    getSourceControlDao(context).add(context, git, new File(path+File.separator+fileName));
  }

  private String resolveTenantPath(SessionContext context, String path) {
    String tenant = context.getTenant()!=null?context.getTenant():"tsuzammen";
    return path.replace(TENANT,tenant);
  }

}