package org.amdocs.tsuzammen.plugin.collaborationstore.git.plugin;

import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.commons.datatypes.UserInfo;

import static org.testng.Assert.*;

public class GitCollaborationStorePluginImplTest {
  @org.testng.annotations.Test
  public void testInit() throws Exception {
    SessionContext context = new SessionContext();
    context.setUser(new UserInfo("testUser"));
    context.setTenant("test");
    GitCollaborationStorePluginImpl gitCollaborationStorePlugin = new
        GitCollaborationStorePluginImpl();
    gitCollaborationStorePlugin.init(context);


  }

  @org.testng.annotations.Test
  public void testCreateItem() throws Exception {

  }

  @org.testng.annotations.Test
  public void testDeleteItem() throws Exception {

  }

  @org.testng.annotations.Test
  public void testCreateItemVersion() throws Exception {

  }

  @org.testng.annotations.Test
  public void testCreateItemVersionEntity() throws Exception {

  }

  @org.testng.annotations.Test
  public void testSaveItemVersionEntity() throws Exception {

  }

  @org.testng.annotations.Test
  public void testDeleteItemVersionEntity() throws Exception {

  }

  @org.testng.annotations.Test
  public void testCommitItemVersionEntities() throws Exception {

  }

  @org.testng.annotations.Test
  public void testDeleteItemVersion() throws Exception {

  }

  @org.testng.annotations.Test
  public void testPublishItemVersion() throws Exception {

  }

  @org.testng.annotations.Test
  public void testSyncItemVersion() throws Exception {

  }

  @org.testng.annotations.Test
  public void testGetItemVersion() throws Exception {

  }

  @org.testng.annotations.Test
  public void testGetNamespacePath() throws Exception {

  }

}