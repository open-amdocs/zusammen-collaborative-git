package org.amdocs.tsuzammen.plugin.collaborationstore.git.plugin;

import org.amdocs.tsuzammen.commons.configuration.impl.ConfigurationAccessor;
import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.commons.datatypes.impl.item.EntityData;
import org.amdocs.tsuzammen.commons.datatypes.item.Info;
import org.amdocs.tsuzammen.commons.datatypes.item.ItemVersion;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.SourceControlDaoFactory;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.SourceControlFileStore;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.SourceControlUtil;
import org.amdocs.tsuzammen.sdk.CollaborationStore;
import org.amdocs.tsuzammen.sdk.utils.SdkConstants;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.net.URI;
import java.util.List;

import static org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants.ITEM_VERSION_INFO_FILE_NAME;

public class GitCollaborationStorePluginImpl implements CollaborationStore {

  private static final String CREATE_ITEM_VERSION_MESSAGE = "Create Item Version";
  private static String PUBLIC_PATH;
  private static String PRIVATE_PATH;
  private static String PUBLIC_URL;
  private static String BP_PATH;

  public void init(SessionContext context) {

    PUBLIC_PATH =
        ConfigurationAccessor.getPluginProperty(SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE,
            PluginConstants.PUBLIC_PATH);

    PRIVATE_PATH = ConfigurationAccessor
        .getPluginProperty(SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE,
            PluginConstants.PRIVATE_PATH);

    PUBLIC_URL = ConfigurationAccessor
        .getPluginProperty(SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE,
            PluginConstants.PUBLIC_URL);

    BP_PATH = ConfigurationAccessor
        .getPluginProperty(SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE,
            PluginConstants.BP_PATH);
  }

  @Override
  public void createItem(SessionContext context, String itemId, String initialVersion, Info info) {


    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);

    String itemPublicPath = PUBLIC_PATH + File.separator + itemId;
    String itemPrivatePath = PRIVATE_PATH + File.separator + "users" + File.separator +
        context.getUser().getUserName() +
        File.separator + itemId;
    String itemPublicUrl = PUBLIC_URL + "/" + itemId;
    String bluePrintPath = BP_PATH; /*todo - add item type to the blue print*/

    Git git = dao.clone(context, bluePrintPath, itemPublicPath, initialVersion);
    dao.clone(context, itemPublicUrl, itemPrivatePath, null);
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
    //baseVersionId = baseVersionId == null ? new String(PluginConstants.MASTER_BRANCH) : baseVersionId;
    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH, itemId);

    Git git = dao.openRepository(context, repositoryPath);
    createItemVersion(context, git, baseVersionId, versionId, versionInfo);
    dao.commit(context, git, CREATE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }

  @Override
  public void createItemVersionEntity(SessionContext context, String itemId, String
      versionId,
                                      URI namespace, String entityId, EntityData entityData) {

    String entityPath = getNamespacePath(namespace, entityId);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH, itemId);

    String fullPath = repositoryPath + File.separator + entityPath;
    File entityPathFile = new File(fullPath);
    entityPathFile.mkdirs();

    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId);
    updateEntityData(context, git, fullPath, entityData);
    dao.close(context, git);
  }


  @Override
  public void saveItemVersionEntity(SessionContext context, String itemId, String
      versionId,
                                    URI namespace, String entityId, EntityData entityData) {
    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    String entityPath = getNamespacePath(namespace, entityId);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH, itemId);
    String fullPath = repositoryPath + File.separator + entityPath;
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId);
    updateEntityData(context, git, fullPath, entityData);
    dao.close(context, git);
  }

  @Override
  public void deleteItemVersionEntity(SessionContext context, String itemId, String
      versionId, URI
                                          namespace, String entityId) {

    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    String entityPath = getNamespacePath(namespace, entityId);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH, itemId);
    String fullPath = repositoryPath + File.separator + entityPath;
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId);

    List<File> files = FileUtils.getFiles(fullPath);
    FileUtils.delete(new File(fullPath));
    File[] fileArray = new File[files.size()];
    fileArray = files.toArray(fileArray);
    dao.delete(context, git, fileArray);
    dao.close(context, git);

  }

  @Override
  public void commitItemVersionEntities(SessionContext sessionContext, String itemId, String
      versionId, String message) {

  }

  @Override
  public void saveItemVersion(SessionContext context, String itemId, String versionId, ItemVersion
      itemVersion, String message) {
    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH, itemId);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId);
    SourceControlFileStore sourceControlFileStore =
        new SourceControlFileStore(context, repositoryPath);
    sourceControlFileStore.store(itemVersion);


    dao.add(context, git, sourceControlFileStore.getFilesToAdd());
    dao.delete(context, git, sourceControlFileStore.getFilesToRemove());
    dao.commit(context, git, "Saved item vestion information");

    dao.close(context, git);
  }


  @Override
  public void deleteItemVersion(SessionContext sessionContext, String itemId, String versionId) {
    /*todo*/
  }

  @Override
  public void publishItemVersion(SessionContext context, String itemId, String versionId,
                                 String message) {
    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH, itemId);
    Git git = dao.openRepository(context, repositoryPath);
    dao.publish(context, git, versionId);
    dao.checkoutBranch(context, git, versionId);
//    dao.inComing(context,git,versionId.getValue());
    dao.close(context, git);
  }

  @Override
  public void syncItemVersion(SessionContext context, String itemId, String versionId) {
    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, PRIVATE_PATH, itemId);
    Git git;
    if (FileUtils.exists(repositoryPath)) {
      git = dao.openRepository(context, repositoryPath);
      dao.sync(context, git, versionId);
    } else {
      git = dao.clone(context,
          SourceControlUtil.getPublicRepositoryPath(context, PUBLIC_PATH, itemId),
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

  private void updateEntityData(SessionContext context, Git git, String entityPath, EntityData
      entityData) {
    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    File file2Add;
    if (entityData.getRelations() != null) {
      file2Add = FileUtils.writeFile(entityPath, PluginConstants.RELATIONS_FILE_NAME,
          entityData.getRelations());
      dao.add(context, git, file2Add);
    }
    if (entityData.getVisualization() != null) {

      file2Add = FileUtils.writeFile(entityPath, PluginConstants.VISUALIZATION_FILE_NAME,
          entityData.getVisualization());
      dao.add(context, git, file2Add);
    }

    if (entityData.getData() != null) {
      file2Add = FileUtils.writeFile(entityPath, PluginConstants.DATA_FILE_NAME,
          entityData.getData());
      dao.add(context, git, file2Add);
    }

    if (entityData.getInfo() != null) {
      file2Add = FileUtils.writeFile(entityPath, PluginConstants.INFO_FILE_NAME,
          entityData.getInfo());
      dao.add(context, git, file2Add);
    }
  }


  private void createItemVersion(SessionContext context, Git git, String baseBranch, String branch,
                                 Info versionInfo) {
    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    dao.createBranch(context, git, baseBranch, branch);

    if (versionInfo == null) {
      return;
    }

    File itemVersionInfoFile = FileUtils.writeFile(
        SourceControlUtil.getPrivateRepositoryPath(git) +
            File.separator,
        ITEM_VERSION_INFO_FILE_NAME,
        versionInfo);

    dao.add(context, git, itemVersionInfoFile);

  }

  private String getNamespacePath(URI namespace, String entityId) {
    return namespace + File.separator + entityId;
  }


}