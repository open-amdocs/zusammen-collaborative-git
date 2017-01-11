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

package org.amdocs.tsuzammen.plugin.collaborationstore.git.main;

import org.amdocs.tsuzammen.datatypes.Id;
import org.amdocs.tsuzammen.datatypes.Namespace;
import org.amdocs.tsuzammen.datatypes.SessionContext;
import org.amdocs.tsuzammen.datatypes.collaboration.PublishResult;
import org.amdocs.tsuzammen.datatypes.item.ElementContext;
import org.amdocs.tsuzammen.datatypes.item.Info;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.impl.ElementCollaborationStore;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.impl.ItemCollaborationStore;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.impl.ItemVersionCollaborationStore;
import org.amdocs.tsuzammen.sdk.CollaborationStore;
import org.amdocs.tsuzammen.sdk.types.CollaborationSyncResult;
import org.amdocs.tsuzammen.sdk.types.ElementData;

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
  public void createElement(SessionContext context, ElementContext elementContext,
                            Namespace namespace, ElementData elementData) {
    elementCollaborationStore.create(context, elementContext, namespace, elementData);
  }

  @Override
  public void saveElement(SessionContext context, ElementContext elementContext,
                          Namespace namespace, ElementData elementData) {
    elementCollaborationStore.save(context, elementContext, namespace, elementData);
  }

  @Override
  public void deleteElement(SessionContext context, ElementContext elementContext,
                            Namespace namespace, ElementData elementData) {

    elementCollaborationStore.delete(context, elementContext, namespace, elementData);
  }

  @Override
  public void deleteItemVersion(SessionContext context, Id itemId, Id versionId) {
    itemVersionCollaborationStore.delete(context, itemId, versionId);
  }

  @Override
  public PublishResult publishItemVersion(SessionContext context, Id itemId, Id versionId,
                                          String message) {
    return itemVersionCollaborationStore.publish(context, itemId, versionId, message);
  }

  @Override
  public CollaborationSyncResult syncItemVersion(SessionContext context, Id itemId, Id versionId) {
    return itemVersionCollaborationStore.sync(context, itemId, versionId);
  }

  @Override
  public CollaborationSyncResult mergeItemVersion(SessionContext context, Id itemId, Id
      versionId, Id sourceVersionId) {
    return itemVersionCollaborationStore.merge(context, itemId, versionId, sourceVersionId);
  }

  @Override
  public ElementData getElement(SessionContext context, ElementContext elementContext,
                                Namespace namespace, Id elementId) {
    return elementCollaborationStore.get(context, elementContext, namespace, elementId);
  }

}