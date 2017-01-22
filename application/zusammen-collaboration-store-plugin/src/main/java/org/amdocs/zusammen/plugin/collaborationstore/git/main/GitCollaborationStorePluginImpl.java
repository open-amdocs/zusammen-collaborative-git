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

package org.amdocs.zusammen.plugin.collaborationstore.git.main;

import org.amdocs.zusammen.datatypes.Id;
import org.amdocs.zusammen.datatypes.Namespace;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.datatypes.item.ElementContext;
import org.amdocs.zusammen.datatypes.item.Info;
import org.amdocs.zusammen.plugin.collaborationstore.git.impl.ElementCollaborationStore;
import org.amdocs.zusammen.plugin.collaborationstore.git.impl.ItemCollaborationStore;
import org.amdocs.zusammen.plugin.collaborationstore.git.impl.ItemVersionCollaborationStore;
import org.amdocs.zusammen.sdk.CollaborationStore;
import org.amdocs.zusammen.sdk.types.ElementData;
import org.amdocs.zusammen.sdk.types.ElementsPublishResult;
import org.amdocs.zusammen.sdk.types.ItemVersionMergeResult;
import org.amdocs.zusammen.sdk.types.ItemVersionPublishResult;

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
                                Info versionInfo) {
    itemVersionCollaborationStore.create(context, itemId, baseVersionId, versionId, versionInfo);
  }

  @Override
  public void saveItemVersion(SessionContext context, Id itemId, Id versionId,
                              Info versionInfo) {

    itemVersionCollaborationStore.save(context, itemId, versionId, versionInfo);
  }

  @Override
  public void createElement(SessionContext context, ElementData elementData) {
    elementCollaborationStore.create(context, elementData);
  }

  @Override
  public void updateElement(SessionContext context, ElementData elementData) {
    elementCollaborationStore.update(context, elementData);
  }

  @Override
  public void deleteElement(SessionContext context, ElementData elementData) {

    elementCollaborationStore.delete(context, elementData);
  }

  @Override
  public void deleteItemVersion(SessionContext context, Id itemId, Id versionId) {
    itemVersionCollaborationStore.delete(context, itemId, versionId);
  }

  @Override
  public ItemVersionPublishResult publishItemVersion(SessionContext context, Id itemId,
                                                     Id versionId,
                                                     String message) {
    return itemVersionCollaborationStore.publish(context, itemId, versionId, message);
  }

  @Override
  public ItemVersionMergeResult syncItemVersion(SessionContext context, Id itemId, Id versionId) {
    return itemVersionCollaborationStore.sync(context, itemId, versionId);
  }

  @Override
  public ItemVersionMergeResult mergeItemVersion(SessionContext context, Id itemId, Id
      versionId, Id sourceVersionId) {
    return itemVersionCollaborationStore.merge(context, itemId, versionId, sourceVersionId);
  }

  @Override
  public ElementData getElement(SessionContext context, ElementContext elementContext,
                                Namespace namespace, Id elementId) {
    return elementCollaborationStore.get(context, elementContext, namespace, elementId);
  }

}