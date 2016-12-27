package org.amdocs.tsuzammen;


import org.amdocs.tsuzammen.commons.datatypes.Id;
import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.commons.datatypes.UserInfo;
import org.amdocs.tsuzammen.commons.datatypes.item.Info;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.plugin.GitCollaborationStorePluginImpl;

import java.util.ArrayList;
import java.util.List;

public class GitTest_toDelete {


  public static void main(String[] args) {

    SessionContext contextA = new SessionContext();
    contextA.setUser(new UserInfo("testA"));
    Id itemId = initItem(contextA);

    Id versionId = createItemVersion(contextA, itemId, null);
    publishItemVersion(contextA, itemId, versionId, "publish new version");
    SessionContext contextB = new SessionContext();
    contextB.setUser(new UserInfo("testB"));

    syncItem(contextB, itemId, versionId);


  }

  private static void publishItemVersion(SessionContext context, Id itemId, Id versionId,
                                         String message) {
    GitCollaborationStorePluginImpl gitCollaborationStorePlugin =
        new GitCollaborationStorePluginImpl();

    gitCollaborationStorePlugin.publishItemVersion(context, itemId, versionId, message);
    System.out.println("publish item version:" + itemId + ":" + versionId);
  }

  private static void syncItem(SessionContext context, Id itemId, Id versionId) {
    GitCollaborationStorePluginImpl gitCollaborationStorePlugin =
        new GitCollaborationStorePluginImpl();

    gitCollaborationStorePlugin.syncItemVersion(context, itemId, versionId);
    System.out.println("sync item version:" + itemId + ":" + versionId);
  }

  private static Id createItemVersion(SessionContext context, Id itemId, Info info) {
    GitCollaborationStorePluginImpl gitCollaborationStorePlugin =
        new GitCollaborationStorePluginImpl();

    Info versionInfo = createInfo(info);

    Id versionId = new Id();
    gitCollaborationStorePlugin.createItemVersion(context, itemId, null, versionId,
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

  private static Id initItem(SessionContext context) {
    GitCollaborationStorePluginImpl gitCollaborationStorePlugin =
        new GitCollaborationStorePluginImpl();

    Id itemId = new Id();
    String versionId = new String("main");
    gitCollaborationStorePlugin.createItem(context, itemId, null);
    //gitCollaborationStorePlugin.createItemVersion(context, itemId, null, versionId, null);
    //gitCollaborationStorePlugin.publishItemVersion(context, itemId, versionId, "");
    System.out.println("init item:" + itemId);
    return itemId;
  }
}
