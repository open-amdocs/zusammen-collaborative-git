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

package org.amdocs.zusammen.plugin.collaborationstore.dao.api;

import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.itemversion.Change;
import org.amdocs.zusammen.plugin.collaborationstore.types.LocalRemoteDataConflict;
import org.amdocs.zusammen.plugin.collaborationstore.types.CollaborationDiffResult;
import org.amdocs.zusammen.plugin.collaborationstore.types.CollaborationSyncResult;
import org.amdocs.zusammen.plugin.collaborationstore.types.Repository;

import java.util.Collection;
import java.util.List;


public interface SourceControlDao<T> {
  Repository<T> initRepository(SessionContext context, Id ItemId);

  CollaborationDiffResult publish(SessionContext context, Repository<T> repository);

  void close(SessionContext context, Repository<T> repository);

  String getRepositoryLocation(SessionContext context,Repository<T> repository);

  void createBranch(SessionContext context, Repository<T> repository, String baseBranchId,
                    Id versionId);

  boolean checkoutBranch(SessionContext context, Repository<T> repository, Id versionId);

  void  store(SessionContext context, Repository<T> repository, Collection<String> files);

  void commit(SessionContext context, Repository<T> repository, String message);

  CollaborationSyncResult sync(SessionContext context, Repository<T> repository, Id versionId);

  LocalRemoteDataConflict splitFileContentConflict(SessionContext context, byte[] data);

  Repository<T> cloneRepository(SessionContext context, String source, String target,
                                  Id branch);

  Collection<String> getBranchFileList(SessionContext context, Repository<T> repository);

  CollaborationSyncResult merge(SessionContext context, Repository<T> repository, Id
      sourceVersionId);

  void delete(SessionContext context, Repository<T> repository, String fullPath);

  CollaborationDiffResult reset(SessionContext context, Repository<T> repository, Id changeId);

  List<Change> listRevisionHistory(SessionContext context, Repository<T> repository, Id versionId);

}
