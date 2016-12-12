package org.amdocs.tsuzammen;

import org.amdocs.tsuzammen.commons.datatypes.Id;
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
    Id itemId = initItem(contextA);

    Id versionId = createItemVersion(contextA, itemId, null);
    publishItemVersion(contextA, itemId, versionId, "publish new version");
    SessionContext contextB = new SessionContext();
    contextB.setUser(new UserInfo("testB"));

    syncItem(contextB, itemId, versionId);


  }

  private static void publishItemVersion(SessionContext context, Id itemId, Id versionId, String
      message) {
    GitCollaborationStorePluginImpl gitCollaborationStorePlugin =
        new GitCollaborationStorePluginImpl();
    gitCollaborationStorePlugin.init(context);
    gitCollaborationStorePlugin.publishItemVersion(context, itemId, versionId, message);
    System.out.println("publish item version:" + itemId.getValue() + ":" + versionId.getValue());
  }

  private static void syncItem(SessionContext context, Id itemId, Id versionId) {
    GitCollaborationStorePluginImpl gitCollaborationStorePlugin =
        new GitCollaborationStorePluginImpl();
    gitCollaborationStorePlugin.init(context);
    gitCollaborationStorePlugin.syncItemVersion(context, itemId, versionId);
    System.out.println("sync item version:" + itemId.getValue() + ":" + versionId.getValue());
  }

  private static Id createItemVersion(SessionContext context, Id itemId, Info info) {
    GitCollaborationStorePluginImpl gitCollaborationStorePlugin =
        new GitCollaborationStorePluginImpl();
    gitCollaborationStorePlugin.init(context);
    Info versionInfo = createInfo(info);

    Id versionId = new Id(CommonMethods.nextUUID());
    gitCollaborationStorePlugin.createItemVersion(context, itemId, new Id("main"), versionId,
        versionInfo);
    System.out.println("create item version:" + itemId.getValue() + ":" + versionId.getValue
        ());
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
    gitCollaborationStorePlugin.init(context);
    Id itemId = new Id(CommonMethods.nextUUID());
    Id versionId = new Id("main");
    gitCollaborationStorePlugin.createItem(context, itemId, versionId,null);
    //gitCollaborationStorePlugin.createItemVersion(context, itemId, null, versionId, null);
    //gitCollaborationStorePlugin.publishItemVersion(context, itemId, versionId, "");
    System.out.println("init item:" + itemId.getValue());
    return itemId;
  }
}
