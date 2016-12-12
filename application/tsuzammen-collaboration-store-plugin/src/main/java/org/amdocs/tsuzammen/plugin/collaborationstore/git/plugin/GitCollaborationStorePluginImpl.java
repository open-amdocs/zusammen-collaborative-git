package org.amdocs.tsuzammen.plugin.collaborationstore.git.plugin;

import org.amdocs.tsuzammen.commons.configuration.impl.ConfigurationAccessor;
import org.amdocs.tsuzammen.commons.datatypes.Id;
import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
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
import java.util.Optional;

import static org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants.ITEM_VERSION_INFO_FILE_NAME;

public class GitCollaborationStorePluginImpl implements CollaborationStore {

  private static final String CREATE_ITEM_VERSION_MESSAGE = "Create Item Version";
  private static String PUBLIC_PATH;
  private static String PRIVATE_PATH;
  private static String PUBLIC_URL;
  private static String BP_PATH;

  public void init(SessionContext context) {

    this.PUBLIC_PATH = ConfigurationAccessor.getPluginProperty( SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE,
        PluginConstants.PUBLIC_PATH);

    this.PRIVATE_PATH = ConfigurationAccessor
        .getPluginProperty( SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE,
            PluginConstants.PRIVATE_PATH);

    this.PUBLIC_URL = ConfigurationAccessor
        .getPluginProperty( SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE,
            PluginConstants.PUBLIC_URL);

    this.BP_PATH = ConfigurationAccessor
        .getPluginProperty( SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE,
            PluginConstants.BP_PATH);
  }

  @Override
  public void createItem(SessionContext context, Id itemId,Id initialVersion, Info info) {


    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);

    String itemPublicPath = this.PUBLIC_PATH + File.separator + itemId.getValue();
    String itemPrivatePath = this.PRIVATE_PATH + File.separator + "users" + File.separator +
        context.getUser().getUserName() +
        File.separator + itemId.getValue();
    String itemPublicUrl = this.PUBLIC_URL + "/" + itemId.getValue();
    String bluePrintPath = this.BP_PATH; /*todo - add item type to the blue print*/

    Git git = dao.clone(context, bluePrintPath, itemPublicPath, initialVersion.getValue());
    dao.clone(context, itemPublicUrl, itemPrivatePath, null);
    dao.close(context, git);
  }

  @Override
  public void deleteItem(SessionContext context, Id itemId) {
    /*todo*/

  }

  @Override
  public void createItemVersion(SessionContext context, Id itemId, Id baseVersionId, Id versionId,
                                Info versionInfo) {
    //baseVersionId = baseVersionId == null ? new Id(PluginConstants.MASTER_BRANCH) : baseVersionId;
    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, this.PRIVATE_PATH, itemId);

    Git git = dao.openRepository(context, repositoryPath);

    createItemVersion(context, git, baseVersionId, versionId, versionInfo);
    dao.commit(context, git, CREATE_ITEM_VERSION_MESSAGE);
    dao.close(context, git);
  }

  @Override
  public void saveItemVersion(SessionContext context, Id itemId, Id versionId, ItemVersion
      itemVersion, String message) {
    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, this.PRIVATE_PATH, itemId);
    Git git = dao.openRepository(context, repositoryPath);
    dao.checkoutBranch(context, git, versionId.getValue());
    SourceControlFileStore sourceControlFileStore =
        new SourceControlFileStore(context, repositoryPath);
    sourceControlFileStore.store(itemVersion);


    dao.add(context, git, sourceControlFileStore.getFilesToAdd());
    dao.delete(context, git, sourceControlFileStore.getFilesToRemove());
    dao.commit(context, git, "Saved item vestion information");

    dao.close(context, git);
  }


  @Override
  public void deleteItemVersion(SessionContext sessionContext, Id itemId, Id versionId) {
    /*todo*/
  }

  @Override
  public void publishItemVersion(SessionContext context, Id itemId, Id versionId, String message) {
    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, this.PRIVATE_PATH, itemId);
    Git git = dao.openRepository(context, repositoryPath);
    dao.publish(context, git, versionId);
    dao.checkoutBranch(context, git, versionId.getValue());
//    dao.inComing(context,git,versionId.getValue());
    dao.close(context, git);
  }

  @Override
  public void syncItemVersion(SessionContext context, Id itemId, Id versionId) {
    GitSourceControlDao dao = SourceControlDaoFactory.getInstance().createInterface(context);
    String repositoryPath =
        SourceControlUtil.getPrivateRepositoryPath(context, this.PRIVATE_PATH, itemId);
    Git git;
    if (FileUtils.exists(repositoryPath)) {
      git = dao.openRepository(context, repositoryPath);
      dao.sync(context, git, versionId);
    } else {
      git = dao.clone(context,
          SourceControlUtil.getPublicRepositoryPath(context, this.PUBLIC_PATH, itemId),
          repositoryPath, versionId.getValue());
    }
    dao.checkoutBranch(context, git, versionId.getValue());
    dao.close(context, git);

  }

  @Override
  public ItemVersion getItemVersion(SessionContext sessionContext, Id itemId, Id versionId,
                                    ItemVersion itemVersion) {
    return null;
  }

  private void createItemVersion(SessionContext context, Git git, Id baseBranch, Id branch,
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
}