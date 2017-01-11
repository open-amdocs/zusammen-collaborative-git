/*
 * Copyright © 2016 Amdocs Software Systems Limited
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

package org.amdocs.tsuzammen.plugin.collaborationstore.git.impl;

import org.amdocs.tsuzammen.commons.configuration.impl.ConfigurationAccessor;
import org.amdocs.tsuzammen.datatypes.Id;
import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.datatypes.item.Info;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.dao.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants;
import org.amdocs.tsuzammen.sdk.utils.SdkConstants;
import org.eclipse.jgit.api.Git;

import java.io.File;

import static org.amdocs.tsuzammen.plugin.collaborationstore.git.utils.PluginConstants.INFO_FILE_NAME;

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
        .TSUZAMMEN_COLLABORATIVE_STORE, PluginConstants.MASTER_BRANCH_PROP);
    Git git = dao.clone(context, bluePrintPath, itemPublicPath, initialVersion);
    dao.clone(context, itemPublicUrl, itemPrivatePath, null);
    if (info != null) {
      addFileContent(context, git, itemPrivatePath, INFO_FILE_NAME, info);
      dao.commit(context, git, PluginConstants.ADD_ITEM_INFO_MESSAGE);
    }

    dao.close(context, git);
  }

  public void delete(SessionContext context, Id itemId) {
    //todo - implement delete item
  }


}
