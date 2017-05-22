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

import com.amdocs.zusammen.commons.configuration.impl.ConfigurationAccessor;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDao;
import com.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDaoFactory;
import com.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants;
import com.amdocs.zusammen.plugin.collaborationstore.types.Repository;
import com.amdocs.zusammen.sdk.SdkConstants;

import static com.amdocs.zusammen.plugin.collaborationstore.utils.PluginConstants.INFO_FILE_NAME;


public class ItemCollaborationStore extends CollaborationStore {

  public ItemCollaborationStore(SourceControlDaoFactory sourceControlDaoFactory) {
    this.sourceControlDaoFactory = sourceControlDaoFactory;
  }

  public void create(SessionContext context, Id itemId, Info info) {
    SourceControlDao dao = getSourceControlDao(context);
    String itemPublicPath = getSourceControlUtil().getPublicRepositoryPath(context, PluginConstants
        .PUBLIC_PATH, itemId);
    String itemPrivatePath = getSourceControlUtil().getPrivateRepositoryPath(context,
        PluginConstants.PRIVATE_PATH, itemId);
    String tenant = context.getTenant() != null ? context.getTenant() : "";
    String itemPublicUrl = (PluginConstants.PUBLIC_URL + "/" + itemId).replace(PluginConstants
        .TENANT, tenant);
    //todo - add item    type to the blue print
    String bluePrintPath = PluginConstants.BP_PATH.replace(PluginConstants.TENANT, tenant);
    String initialVersion = ConfigurationAccessor.getPluginProperty(SdkConstants
        .ZUSAMMEN_COLLABORATIVE_STORE, PluginConstants.MASTER_BRANCH_PROP);
    Repository repository = dao.cloneRepository(context, bluePrintPath, itemPublicPath, new Id
        (initialVersion));
    dao.cloneRepository(context, itemPublicUrl, itemPrivatePath, new Id
        (initialVersion));
    if (info != null) {
      addFileContent(context, itemPrivatePath, null, INFO_FILE_NAME, info);
      dao.commit(context, repository, PluginConstants.ADD_ITEM_INFO_MESSAGE);
    }

    dao.close(context, repository);
  }

  public void delete(SessionContext context, Id itemId) {
    //todo - implement delete item
  }


}
