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
import org.amdocs.zusammen.datatypes.response.ErrorCode;
import org.amdocs.zusammen.datatypes.response.Module;
import org.amdocs.zusammen.datatypes.response.Response;
import org.amdocs.zusammen.datatypes.response.ReturnCode;
import org.amdocs.zusammen.datatypes.response.ZusammenException;
import org.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDaoFactory;
import org.amdocs.zusammen.plugin.collaborationstore.impl.ElementCollaborationStore;
import org.amdocs.zusammen.plugin.collaborationstore.impl.ItemCollaborationStore;
import org.amdocs.zusammen.plugin.collaborationstore.impl.ItemVersionCollaborationStore;
import org.amdocs.zusammen.sdk.collaboration.CollaborationStore;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeResult;
import org.amdocs.zusammen.sdk.collaboration.types.CollaborationPublishResult;

public class GitCollaborationStorePluginImpl implements CollaborationStore {

  private final ItemCollaborationStore itemCollaborationStore =
      new ItemCollaborationStore(SourceControlDaoFactory.getInstance());
  private final ItemVersionCollaborationStore itemVersionCollaborationStore =
      new ItemVersionCollaborationStore(SourceControlDaoFactory.getInstance());
  private final ElementCollaborationStore elementCollaborationStore =
      new ElementCollaborationStore(SourceControlDaoFactory.getInstance());


  @Override
  public Response<Void> createItem(SessionContext context, Id itemId, Info info) {

    try {
      itemCollaborationStore.create(context, itemId, info);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ITEM_CREATE, Module.ZCSP, null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<Void> deleteItem(SessionContext context, Id itemId) {
    try {
      itemCollaborationStore.delete(context, itemId);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ITEM_DELETE, Module.ZCSP, null, ze
          .getReturnCode()));
    }


  }

  @Override
  public Response<Void> createItemVersion(SessionContext context, Id itemId, Id baseVersionId,
                                          Id versionId,
                                          ItemVersionData itemVersionData) {
    try {
      getItemVersionCollaborationStore()
          .create(context, itemId, baseVersionId, versionId, itemVersionData);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ITEM_VERSION_CREATE, Module.ZCSP, null, ze
          .getReturnCode()));
    }

  }

  @Override
  public Response<Void> updateItemVersion(SessionContext context, Id itemId, Id versionId,
                                          ItemVersionData itemVersionData) {

    try {
      getItemVersionCollaborationStore().save(context, itemId, versionId, itemVersionData);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return
          new Response(new ReturnCode(ErrorCode.CL_ITEM_VERSION_UPDATE, Module.ZCSP, null, ze
              .getReturnCode()));
    }

  }

  @Override
  public Response<Void> commit(SessionContext context,Id itemId, Id versionId,String message){
    try {
      getElementCollaborationStore().commit(context, itemId, versionId, message);
      return new Response(Void.TYPE);
    }catch (ZusammenException zue){
      return new Response(new ReturnCode(ErrorCode.CL_ELEMENT_CREATE, Module.ZCSP, null, zue
          .getReturnCode()));
    }
  }

  @Override
  public Response<Void> createElement(SessionContext context, CollaborationElement element) {
    try {
      getElementCollaborationStore().create(context, element);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ELEMENT_CREATE, Module.ZCSP, null, ze
          .getReturnCode()));
    }

  }

  @Override
  public Response<Void> updateElement(SessionContext context, CollaborationElement element) {
    try {
      getElementCollaborationStore().update(context, element);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ELEMENT_UPDATE, Module.ZCSP, null, ze
          .getReturnCode()));
    }

  }

  @Override
  public Response<Void> deleteElement(SessionContext context, CollaborationElement element) {

    try {
      getElementCollaborationStore().delete(context, element);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ELEMENT_DELETE, Module.ZCSP, null, ze
          .getReturnCode()));
    }

  }

  @Override
  public Response<Void> deleteItemVersion(SessionContext context, Id itemId, Id versionId) {
    try {
      getItemVersionCollaborationStore().delete(context, itemId, versionId);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ITEM_VERSION_DELETE, Module.ZCSP, null, ze
          .getReturnCode()));
    }

  }

  @Override
  public Response<CollaborationPublishResult> publishItemVersion(SessionContext context, Id itemId,
                                                                 Id versionId, String message) {

    try {
      CollaborationPublishResult collaborationPublishResult = getItemVersionCollaborationStore()
          .publish(context, itemId, versionId, message);
      return new Response(collaborationPublishResult);
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ITEM_VERSION_PUBLISH, Module.ZCSP, null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<CollaborationMergeResult> syncItemVersion(SessionContext context, Id itemId, Id
      versionId) {
    try {
      return new Response(getItemVersionCollaborationStore().sync(context, itemId, versionId));
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ITEM_VERSION_SYNC, Module.ZCSP, null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<CollaborationMergeResult> mergeItemVersion(SessionContext context, Id itemId, Id
      versionId, Id sourceVersionId) {
    try {
      return new Response(getItemVersionCollaborationStore().merge(context, itemId, versionId,
          sourceVersionId));
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ITEM_VERSION_MERGE, Module.ZCSP, null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<CollaborationElement> getElement(SessionContext context, ElementContext
      elementContext,
                                                   Namespace namespace, Id elementId) {
    try {
      return new Response(getElementCollaborationStore().get(context, elementContext, namespace,
          elementId));
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ELEMENT_GET, Module.ZCSP, null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<ItemVersionHistory> listItemVersionHistory(SessionContext context, Id itemId,
                                                             Id versionId) {
    try {
      return new Response(
          getItemVersionCollaborationStore().listHistory(context, itemId, versionId));
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ITEM_VERSION_HISTORY, Module.ZCSP,
          null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<CollaborationMergeChange> revertItemVersionHistory(SessionContext context, Id
      itemId, Id
                                                                         versionId, Id changeId) {
    try {
      ElementContext elementContext = new ElementContext(itemId, versionId);
      return new Response(getItemVersionCollaborationStore().resetHistory(context,elementContext, changeId));
    } catch (ZusammenException ze) {
      return new Response(new ReturnCode(ErrorCode.CL_ITEM_VERSION_REVERT_HISTORY, Module.ZCSP,
          null, ze
          .getReturnCode()));
    }
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