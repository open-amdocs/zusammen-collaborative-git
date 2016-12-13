package org.amdocs.tsuzammen;


import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.commons.datatypes.UserInfo;
import org.amdocs.tsuzammen.commons.datatypes.item.Info;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.plugin.GitCollaborationStorePluginImpl;
import org.amdocs.tsuzammen.utils.common.CommonMethods;

import java.util.ArrayList;
import java.util.List;

public class GitTest_toDelete {


  public static void main(String[] args) {

    SessionContext contextA = new SessionContext();
    contextA.setUser(new UserInfo("testA"));
    String itemId = initItem(contextA);

    String versionId = createItemVersion(contextA, itemId, null);
    publishItemVersion(contextA, itemId, versionId, "publish new version");
    SessionContext contextB = new SessionContext();
    contextB.setUser(new UserInfo("testB"));

    syncItem(contextB, itemId, versionId);


  }

  private static void publishItemVersion(SessionContext context, String itemId, String versionId, String
      message) {
    GitCollaborationStorePluginImpl gitCollaborationStorePlugin =
        new GitCollaborationStorePluginImpl();
    gitCollaborationStorePlugin.init(context);
    gitCollaborationStorePlugin.publishItemVersion(context, itemId, versionId, message);
    System.out.println("publish item version:" + itemId + ":" + versionId);
  }

  private static void syncItem(SessionContext context, String itemId, String versionId) {
    GitCollaborationStorePluginImpl gitCollaborationStorePlugin =
        new GitCollaborationStorePluginImpl();
    gitCollaborationStorePlugin.init(context);
    gitCollaborationStorePlugin.syncItemVersion(context, itemId, versionId);
    System.out.println("sync item version:" + itemId + ":" + versionId);
  }

  private static String createItemVersion(SessionContext context, String itemId, Info info) {
    GitCollaborationStorePluginImpl gitCollaborationStorePlugin =
        new GitCollaborationStorePluginImpl();
    gitCollaborationStorePlugin.init(context);
    Info versionInfo = createInfo(info);

    String versionId = new String(CommonMethods.nextUUID());
    gitCollaborationStorePlugin.createItemVersion(context, itemId, "main", versionId,
        versionInfo);
    System.out.println("create item version:" + itemId + ":" + versionId);
    return versionId;
  }

  private static Info createInfo(Info info) {
    if (info != null) {
      return info;
    }
    Info versionInfo = new Info();
    versionInfo.addProperty("type", "testProp");
    List<String> listProp = new ArrayList<>();
    listProp.add("prop1");
    listProp.add("prop2");
    versionInfo.addProperty("listType", listProp);
    return versionInfo;
  }

  private static String initItem(SessionContext context) {
    GitCollaborationStorePluginImpl gitCollaborationStorePlugin =
        new GitCollaborationStorePluginImpl();
    gitCollaborationStorePlugin.init(context);
    String itemId = new String(CommonMethods.nextUUID());
    String versionId = new String("main");
    gitCollaborationStorePlugin.createItem(context, itemId, versionId,null);
    //gitCollaborationStorePlugin.createItemVersion(context, itemId, null, versionId, null);
    //gitCollaborationStorePlugin.publishItemVersion(context, itemId, versionId, "");
    System.out.println("init item:" + itemId);
    return itemId;
  }
}
