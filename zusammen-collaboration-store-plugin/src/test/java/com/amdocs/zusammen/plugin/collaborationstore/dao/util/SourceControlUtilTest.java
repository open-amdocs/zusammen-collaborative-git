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

package com.amdocs.zusammen.plugin.collaborationstore.dao.util;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.plugin.collaborationstore.git.util.TestUtil;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

//import com.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;

public class SourceControlUtilTest {

  @Mock
  private SourceControlUtil sourceControlUtil;
  @Mock
  private SessionContext context = TestUtil.createSessionContext();
  @Mock
  private ElementUtil elementUtil;
  @Mock
  ObjectId oid = mock(ObjectId.class);

  @Mock
  ObjectId zeroOid = ObjectId.zeroId();

  @BeforeMethod
  public void init() {
    MockitoAnnotations.initMocks(this);
    sourceControlUtil = Mockito.spy(new SourceControlUtil());
    elementUtil = spy(new ElementUtil());
    when(sourceControlUtil.getCollaborationElementUtil()).thenReturn(elementUtil);
  }

  /*@Test
  public void testHandleSyncResponse() throws Exception {
    PullResult pullResult = Mockito.mock(PullResult.class);
    when(pullResult.isSuccessful()).thenReturn(true);
    Mockito.doReturn(null).when(sourceControlUtil).handleMergeResponse(
        anyObject(),
        anyObject(),
        anyObject());


    sourceControlUtil.handleSyncResponse(context, null, pullResult);
  }*/

  /*@Test
  public void testHandleMergeResponse() throws Exception {

    CollaborationElementConflict elementConflict = new CollaborationElementConflict();

    MergeResult mergeResult = mock(MergeResult.class);
    Mockito.doReturn(true).when(sourceControlUtil).isMergeSuccesses(mergeResult);
    Mockito.doReturn("10000").when(sourceControlUtil).extractIdFromFilePath(anyObject());
    Mockito.doReturn(elementConflict).when(sourceControlUtil).handleElementConflict(anyObject(),
        anyObject(), anyObject());
    Map<String, int[][]> conflicts = new HashMap<>();
    conflicts.put("fileA", new int[][]{{1, 2}, {3, 4}});
    mergeResult.setConflicts(conflicts);
    CollaborationMergeConflict result = sourceControlUtil.handleMergeResponse(context, null,
        mergeResult);

    Assert.assertNotNull(result);
  }
*/
  @Test
  public void testIsMergeSuccesses() throws Exception {

  }

  /*@Test
  public void testHandleElementConflict() throws Exception {
    String elementPath = "elementPath";
    String fullPath = "fullPath";
    String elementId = "elementId";
    Info info = new Info();
    List<Relation> relations = new ArrayList();
    Relation rel = new Relation();
    relations.add(rel);
    rel.setEdge1(new RelationEdge());
    rel.setEdge2(new RelationEdge());
    Optional<byte[]> dataOp = Optional.of("data".getBytes());
    Optional<byte[]> searchDataOp = Optional.of("searchData".getBytes());
    Optional<byte[]> visualizationOp = Optional.of("visualization".getBytes());
    Optional<byte[]> infoOp = Optional.of(JsonUtil.object2Json(info).getBytes());
    Optional<byte[]> relationOp = Optional.of(JsonUtil.object2Json(relations).getBytes());

    CollaborationElement element = Mockito.mock(CollaborationElement.class);

    Mockito.doReturn(fullPath).when(sourceControlUtil).getRepositoryFullPath(null, elementPath);
    Mockito.doReturn(dataOp).when(elementUtil).getFileContentAsByteArray(fullPath,
        PluginConstants
            .DATA_FILE_NAME);
    Mockito.doReturn(searchDataOp).when(elementUtil).getFileContentAsByteArray(fullPath,
        PluginConstants
            .SEARCH_DATA_FILE_NAME);
    Mockito.doReturn(visualizationOp).when(elementUtil).getFileContentAsByteArray(fullPath,
        PluginConstants
            .VISUALIZATION_FILE_NAME);
    Mockito.doReturn(infoOp).when(elementUtil).getFileContentAsByteArray(fullPath,
        PluginConstants
            .INFO_FILE_NAME);
    Mockito.doReturn(relationOp).when(elementUtil).getFileContentAsByteArray(fullPath,
        PluginConstants
            .RELATIONS_FILE_NAME);

    Mockito.doReturn("").when(elementUtil).getRepositoryPath(null);

    Mockito.doReturn(element).when(elementUtil)
        .createCollaborationElement(anyObject(), anyObject(), anyObject());

    CollaborationElementConflict elementConflict = sourceControlUtil.handleElementConflict(null,
        elementPath, elementId);

  }
*/
  @Test
  public void testConsumeConflictContentAsObjects() throws Exception {

  }

  @Test
  public void testConsumeConflictContentAsInputStream() throws Exception {

  }

  @Test
  public void testHandleMergeFileDiff() throws Exception {

  }

  @Test
  public void testCalculateItemVersionChangedData() throws Exception {


  }

  @Test
  public void testExtractElementIdFromFilePath() throws Exception {

  }

  @Test
  public void testExtractElementPathFromFilePath() throws Exception {

  }

  @Test
  public void testHandleMergeFileDiff1() throws Exception {

  }

  @Test
  public void testGetChangeData() throws Exception {

  }

/*
  @Test
  public void testGetRepoData() throws Exception {

    String elementId = "elementId";
    String elementPath = "elementPath";
    Collection<String> files = new ArrayList<>();
    files.add("1");
    files.add("2");

    Mockito.doReturn(files).when(gitSourceControlDaoMock).getBranchFileList(anyObject(), anyObject
        ());
    Mockito.doReturn(elementId).when(sourceControlUtil).extractIdFromFilePath((anyObject()));

    Mockito.doReturn(elementPath).when(sourceControlUtil).extractElementPathFromFilePath(
        (anyObject()));
    Mockito.doReturn(elementUtil).when(sourceControlUtil).getCollaborationElementUtil();

    ItemVersion itemVersionMock = new ItemVersion();
    CollaborationElement elementMock = Mockito.mock(CollaborationElement.class);
    Mockito.doReturn(elementMock).when(elementUtil).uploadCollaborationElement(null, elementPath,
        elementId
    );
    Git git = null;
    Mockito.doReturn(itemVersionMock).when(elementUtil).uploadItemVersionData(git);

    sourceControlUtil.getRepoData(context, gitSourceControlDaoMock, null);
  }
*/

  @Test
  public void testGetNewRevisionId() throws Exception {
    Collection<PushResult> pushResults = new ArrayList<>();
    PushResult pushResult1 = Mockito.mock(PushResult.class);
    pushResults.add(pushResult1);
    Collection<RemoteRefUpdate> remoteUpdates = new ArrayList<>();
    RemoteRefUpdate remoteRefUpdate = Mockito.mock(RemoteRefUpdate.class);
    remoteUpdates.add(remoteRefUpdate);
    Mockito.doReturn(remoteUpdates).when(pushResult1).getRemoteUpdates();

    Mockito.doReturn(zeroOid).when(remoteRefUpdate).getNewObjectId();
    sourceControlUtil.getNewRevisionId(pushResults);


    Mockito.doReturn(oid).when(remoteRefUpdate).getNewObjectId();
    sourceControlUtil.getNewRevisionId(pushResults);
  }

  @Test
  public void testGetOldRevisionId() throws Exception {
    Collection<PushResult> pushResults = new ArrayList<>();
    PushResult pushResult1 = Mockito.mock(PushResult.class);
    pushResults.add(pushResult1);
    Collection<RemoteRefUpdate> remoteUpdates = new ArrayList<>();
    RemoteRefUpdate remoteRefUpdate = Mockito.mock(RemoteRefUpdate.class);
    remoteUpdates.add(remoteRefUpdate);
    Mockito.doReturn(remoteUpdates).when(pushResult1).getRemoteUpdates();

    Mockito.doReturn(zeroOid).when(remoteRefUpdate).getExpectedOldObjectId();
    sourceControlUtil.getOldRevisionId(pushResults);

    Mockito.doReturn(oid).when(remoteRefUpdate).getExpectedOldObjectId();
    sourceControlUtil.getOldRevisionId(pushResults);

  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testGetAction() throws Exception {

    Action action;

    action = sourceControlUtil.getAction(DiffEntry.ChangeType.ADD);
    Assert.assertEquals(action, Action.CREATE);
    action = sourceControlUtil.getAction(DiffEntry.ChangeType.DELETE);
    Assert.assertEquals(action, Action.DELETE);
    action = sourceControlUtil.getAction(DiffEntry.ChangeType.MODIFY);
    Assert.assertEquals(action, Action.UPDATE);
    action = sourceControlUtil.getAction(DiffEntry.ChangeType.COPY);
  }

}