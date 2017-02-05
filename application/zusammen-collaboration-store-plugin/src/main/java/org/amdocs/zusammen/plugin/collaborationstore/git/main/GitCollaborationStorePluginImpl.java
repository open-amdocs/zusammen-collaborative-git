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

package org.amdocs.zusammen.plugin.collaborationstore.git.main;

import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.Namespace;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.item.ElementContext;
import org.amdocs.zusammen.datatypes.item.Info;
import org.amdocs.zusammen.datatypes.item.ItemVersionData;
import org.amdocs.zusammen.datatypes.itemversion.ItemVersionHistory;
import org.amdocs.zusammen.plugin.collaborationstore.git.impl.ElementCollaborationStore;
import org.amdocs.zusammen.plugin.collaborationstore.git.impl.ItemCollaborationStore;
import org.amdocs.zusammen.plugin.collaborationstore.git.impl.ItemVersionCollaborationStore;
import org.amdocs.zusammen.sdk.collaboration.CollaborationStore;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeResult;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationPublishResult;

public class GitCollaborationStorePluginImpl implements CollaborationStore {

  private final ItemCollaborationStore itemCollaborationStore =
      new ItemCollaborationStore();
  private final ItemVersionCollaborationStore itemVersionCollaborationStore =
      new ItemVersionCollaborationStore();
  private final ElementCollaborationStore elementCollaborationStore =
      new ElementCollaborationStore();


  @Override
  public void createItem(SessionContext context, Id itemId, Info info) {

    itemCollaborationStore.create(context, itemId, info);
  }

  @Override
  public void deleteItem(SessionContext context, Id itemId) {
    itemCollaborationStore.delete(context, itemId);

  }

  @Override
  public void createItemVersion(SessionContext context, Id itemId, Id baseVersionId,
                                Id versionId,
                                ItemVersionData itemVersionData) {
    getItemVersionCollaborationStore()
        .create(context, itemId, baseVersionId, versionId, itemVersionData);
  }

  @Override
  public void updateItemVersion(SessionContext context, Id itemId, Id versionId,
                                ItemVersionData itemVersionData) {

    getItemVersionCollaborationStore().save(context, itemId, versionId, itemVersionData);
  }

  @Override
  public void createElement(SessionContext context, CollaborationElement element) {
    getElementCollaborationStore().create(context, element);
  }

  @Override
  public void updateElement(SessionContext context, CollaborationElement element) {
    getElementCollaborationStore().update(context, element);
  }

  @Override
  public void deleteElement(SessionContext context, CollaborationElement element) {

    getElementCollaborationStore().delete(context, element);
  }

  @Override
  public void deleteItemVersion(SessionContext context, Id itemId, Id versionId) {
    getItemVersionCollaborationStore().delete(context, itemId, versionId);
  }

  @Override
  public CollaborationPublishResult publishItemVersion(SessionContext context, Id itemId,
                                                       Id versionId,
                                                       String message) {
    return getItemVersionCollaborationStore().publish(context, itemId, versionId, message);
  }

  @Override
  public CollaborationMergeResult syncItemVersion(SessionContext context, Id itemId, Id versionId) {
    return getItemVersionCollaborationStore().sync(context, itemId, versionId);
  }

  @Override
  public CollaborationMergeResult mergeItemVersion(SessionContext context, Id itemId, Id
      versionId, Id sourceVersionId) {
    return getItemVersionCollaborationStore().merge(context, itemId, versionId, sourceVersionId);
  }

  @Override
  public CollaborationElement getElement(SessionContext context, ElementContext elementContext,
                                         Namespace namespace, Id elementId) {
    return getElementCollaborationStore().get(context, elementContext, namespace, elementId);
  }

  @Override
  public ItemVersionHistory listItemVersionHistory(SessionContext context, Id itemId,
                                                   Id versionId) {
    return getItemVersionCollaborationStore().listHistory(context, itemId, versionId);
  }

  @Override
  public CollaborationMergeChange revertItemVersionHistory(SessionContext context, Id itemId, Id
      versionId, Id changeId) {
    return getItemVersionCollaborationStore().resetHistory(context, itemId, versionId, changeId);
  }


  protected ItemCollaborationStore getItemCollaborationStore() {
    return this.itemCollaborationStore;
  }

  protected ItemVersionCollaborationStore getItemVersionCollaborationStore() {
    return this.itemVersionCollaborationStore;
  }

  protected ElementCollaborationStore getElementCollaborationStore() {
    return this.elementCollaborationStore;
  }


}