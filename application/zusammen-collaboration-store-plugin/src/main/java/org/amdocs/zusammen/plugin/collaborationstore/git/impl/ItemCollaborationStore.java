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

package org.amdocs.zusammen.plugin.collaborationstore.git.impl;

import org.amdocs.zusammen.commons.configuration.impl.ConfigurationAccessor;
import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.item.Info;
import org.amdocs.zusammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.zusammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.zusammen.sdk.SdkConstants;
import org.eclipse.jgit.api.Git;

import java.io.File;

import static org.amdocs.zusammen.plugin.collaborationstore.git.utils.PluginConstants.INFO_FILE_NAME;

public class ItemCollaborationStore extends CollaborationStore {
  public void create(SessionContext context, Id itemId, Info info) {
    GitSourceControlDao dao = getSourceControlDao(context);
    String itemPublicPath = PluginConstants.PUBLIC_PATH +
        File.separator + itemId;
    itemPublicPath = resolveTenantPath(context, itemPublicPath);
    String itemPrivatePath = PluginConstants.PRIVATE_PATH +
        File.separator +
        "users" +
        File.separator +
        context.getUser().getUserName() +
        File.separator + itemId;
    itemPrivatePath = resolveTenantPath(context, itemPrivatePath);
    String itemPublicUrl = PluginConstants.PUBLIC_URL + "/" + itemId;
    itemPublicUrl = resolveTenantPath(context, itemPublicUrl);
    String bluePrintPath = resolveTenantPath(context, PluginConstants.BP_PATH);
    //todo - add item    type to the blue print

    String initialVersion = ConfigurationAccessor.getPluginProperty(SdkConstants
        .ZUSAMMEN_COLLABORATIVE_STORE, PluginConstants.MASTER_BRANCH_PROP);
    Git git = dao.clone(context, bluePrintPath, itemPublicPath, initialVersion);
    dao.clone(context, itemPublicUrl, itemPrivatePath, null);
    if (info != null) {
      addFileContent(context, git, itemPrivatePath,null, INFO_FILE_NAME, info);
      dao.commit(context, git, PluginConstants.ADD_ITEM_INFO_MESSAGE);
    }

    dao.close(context, git);
  }

  public void delete(SessionContext context, Id itemId) {
    //todo - implement delete item
  }


}
