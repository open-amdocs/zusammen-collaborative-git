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
import org.amdocs.zusammen.datatypes.response.Response;
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
  public Response<Void> createItem(SessionContext context, Id itemId, Info info) {

    itemCollaborationStore.create(context, itemId, info);
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> deleteItem(SessionContext context, Id itemId) {
    itemCollaborationStore.delete(context, itemId);
    return new Response(Void.TYPE);

  }

  @Override
  public Response<Void> createItemVersion(SessionContext context, Id itemId, Id baseVersionId,
                                Id versionId,
                                ItemVersionData itemVersionData) {
    getItemVersionCollaborationStore()
        .create(context, itemId, baseVersionId, versionId, itemVersionData);
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> updateItemVersion(SessionContext context, Id itemId, Id versionId,
                                ItemVersionData itemVersionData) {

    getItemVersionCollaborationStore().save(context, itemId, versionId, itemVersionData);
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> createElement(SessionContext context, CollaborationElement element) {
    getElementCollaborationStore().create(context, element);
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> updateElement(SessionContext context, CollaborationElement element) {
    getElementCollaborationStore().update(context, element);
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> deleteElement(SessionContext context, CollaborationElement element) {

    getElementCollaborationStore().delete(context, element);
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> deleteItemVersion(SessionContext context, Id itemId, Id versionId) {
    getItemVersionCollaborationStore().delete(context, itemId, versionId);
    return new Response(Void.TYPE);
  }

  @Override
  public Response<CollaborationPublishResult> publishItemVersion(SessionContext context, Id itemId,
                                                       Id versionId,
                                                       String message) {

    return new Response(getItemVersionCollaborationStore().publish(context, itemId, versionId,
        message));
  }

  @Override
  public Response<CollaborationMergeResult> syncItemVersion(SessionContext context, Id itemId, Id
      versionId) {
    return new Response(getItemVersionCollaborationStore().sync(context, itemId, versionId));
  }

  @Override
  public Response<CollaborationMergeResult> mergeItemVersion(SessionContext context, Id itemId, Id
      versionId, Id sourceVersionId) {
    return new Response(getItemVersionCollaborationStore().merge(context, itemId, versionId,
        sourceVersionId));
  }

  @Override
  public Response<CollaborationElement> getElement(SessionContext context, ElementContext
      elementContext,
                                         Namespace namespace, Id elementId) {
    return new Response(getElementCollaborationStore().get(context, elementContext, namespace,
        elementId));
  }

  @Override
  public Response<ItemVersionHistory> listItemVersionHistory(SessionContext context, Id itemId,
                                                   Id versionId) {
    return new Response(getItemVersionCollaborationStore().listHistory(context, itemId, versionId));
  }

  @Override
  public Response<CollaborationMergeChange> revertItemVersionHistory(SessionContext context, Id
      itemId, Id
      versionId, Id changeId) {
    return new Response(getItemVersionCollaborationStore().resetHistory(context, itemId,
        versionId, changeId));
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