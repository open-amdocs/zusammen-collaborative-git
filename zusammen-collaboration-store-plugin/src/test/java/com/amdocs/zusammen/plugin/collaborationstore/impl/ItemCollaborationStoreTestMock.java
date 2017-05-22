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

import com.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDao;
import com.amdocs.zusammen.plugin.collaborationstore.types.CollaborationSyncResult;
import com.amdocs.zusammen.plugin.collaborationstore.types.Repository;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.itemversion.Change;
import com.amdocs.zusammen.plugin.collaborationstore.types.CollaborationDiffResult;
import com.amdocs.zusammen.plugin.collaborationstore.types.LocalRemoteDataConflict;
import org.eclipse.jgit.api.Git;

import java.util.Collection;
import java.util.List;

public class ItemCollaborationStoreTestMock {


  public static class SourceControlDaoMockBase implements SourceControlDao<Git> {
    @Override
    public Repository initRepository(SessionContext context, Id ItemId) {
      return null;
    }

    @Override
    public boolean checkoutChange(SessionContext context, Repository<Git> repository,
                                  String changeRef) {
      return false;
    }

    @Override
    public CollaborationDiffResult publish(SessionContext context, Repository repository) {
      return null;
    }

    @Override
    public void close(SessionContext context, Repository repository) {

    }

    @Override
    public String getRepositoryLocation(SessionContext context, Repository repository) {
      return null;
    }

    @Override
    public void createBranch(SessionContext context, Repository repository, String baseBranchId,
                             Id versionId) {

    }

    @Override
    public boolean checkoutBranch(SessionContext context, Repository repository, Id versionId) {
      return false;
    }

    @Override
    public void store(SessionContext context, Repository<Git> repository, Collection<String>
        files) {

    }

    @Override
    public void tag(SessionContext context, Repository<Git> repository, Id changeId, String tag,
                    String message) {

    }

    @Override
    public void commit(SessionContext context, Repository repository, String message) {

    }

    @Override
    public CollaborationSyncResult sync(SessionContext context, Repository repository,
                                        Id versionId) {
      return null;
    }

    @Override
    public LocalRemoteDataConflict splitFileContentConflict(SessionContext context, byte[] data) {
      return null;
    }

    @Override
    public Repository cloneRepository(SessionContext context, String source, String target,
                                      Id branch) {
      return null;
    }

    @Override
    public Collection<String> getBranchFileList(SessionContext context, Repository repository) {
      return null;
    }

    @Override
    public CollaborationSyncResult merge(SessionContext context, Repository repository,
                                         Id sourceVersionId) {
      return null;
    }

    @Override
    public void delete(SessionContext context, Repository repository, String fullPath) {

    }

    @Override
    public CollaborationDiffResult reset(SessionContext context, Repository repository,
                                         String changeRef) {
      return null;
    }

    @Override
    public List<Change> listRevisionHistory(SessionContext context, Repository repository,
                                            Id versionId) {
      return null;
    }
  }


  public static class SourceControlDaoMock extends SourceControlDaoMockBase{
  }
}
