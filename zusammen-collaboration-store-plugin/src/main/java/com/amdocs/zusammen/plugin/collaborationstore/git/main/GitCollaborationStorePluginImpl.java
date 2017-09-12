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

package com.amdocs.zusammen.plugin.collaborationstore.git.main;

import com.amdocs.zusammen.commons.health.data.HealthInfo;
import com.amdocs.zusammen.commons.health.data.HealthStatus;
import com.amdocs.zusammen.commons.log.ZusammenLogger;
import com.amdocs.zusammen.commons.log.ZusammenLoggerFactory;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.Namespace;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionStatus;
import com.amdocs.zusammen.datatypes.item.Resolution;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionHistory;
import com.amdocs.zusammen.datatypes.itemversion.Tag;
import com.amdocs.zusammen.datatypes.response.ErrorCode;
import com.amdocs.zusammen.datatypes.response.Module;
import com.amdocs.zusammen.datatypes.response.Response;
import com.amdocs.zusammen.datatypes.response.ReturnCode;
import com.amdocs.zusammen.datatypes.response.ZusammenException;
import com.amdocs.zusammen.plugin.collaborationstore.dao.api.CollaborationHealthCheck;
import com.amdocs.zusammen.plugin.collaborationstore.dao.api.SourceControlDaoFactory;
import com.amdocs.zusammen.plugin.collaborationstore.impl.ElementCollaborationStore;
import com.amdocs.zusammen.plugin.collaborationstore.impl.ItemCollaborationStore;
import com.amdocs.zusammen.plugin.collaborationstore.impl.ItemVersionCollaborationStore;
import com.amdocs.zusammen.sdk.collaboration.CollaborationStore;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElementConflict;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationItemVersionConflict;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeResult;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationPublishResult;
import org.eclipse.jgit.api.errors.JGitInternalException;

import java.util.Collection;
import java.util.stream.Collectors;

public class GitCollaborationStorePluginImpl implements CollaborationStore {

  public static final Id HEALTH_CHECK_ID = new Id("HealthCheck");
  private final ItemCollaborationStore itemCollaborationStore =
      new ItemCollaborationStore(SourceControlDaoFactory.getInstance());
  private final ItemVersionCollaborationStore itemVersionCollaborationStore =
      new ItemVersionCollaborationStore(SourceControlDaoFactory.getInstance());
  private final ElementCollaborationStore elementCollaborationStore =
      new ElementCollaborationStore(SourceControlDaoFactory.getInstance());
  private static final ZusammenLogger loggger =
      ZusammenLoggerFactory.getLogger(GitCollaborationStorePluginImpl.class.getName());

  @Override
  public Response<HealthInfo> checkHealth(SessionContext sessionContext) {
    try {
      createItem(sessionContext, HEALTH_CHECK_ID, null);
      createItem(sessionContext, HEALTH_CHECK_ID, null);
    } catch (JGitInternalException e) {
      if (e.getMessage().contains("already exists")) {
        return new Response<HealthInfo>(new HealthInfo(CollaborationHealthCheck.MODULE_NAME,
            HealthStatus.UP, ""));
      }
      loggger.error("Health check failed " + e.getMessage(), e);
      return new Response<>(new HealthInfo(CollaborationHealthCheck.MODULE_NAME,
          HealthStatus.DOWN, e.getMessage()));

    } catch (Throwable ze) {
      loggger.error("Health check failed " + ze);
      return new Response<>(new HealthInfo(CollaborationHealthCheck.MODULE_NAME,
          HealthStatus.DOWN, ze.getMessage()));
    }
    return new Response<>(new HealthInfo(CollaborationHealthCheck.MODULE_NAME,
        HealthStatus.DOWN, "Should never succeed"));
  }

  @Override
  public Response<Void> createItem(SessionContext context, Id itemId, Info info) {
    try {
      itemCollaborationStore.create(context, itemId, info);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response<>(new ReturnCode(ErrorCode.CL_ITEM_CREATE, Module.ZCSP, null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<Void> deleteItem(SessionContext context, Id itemId) {
    try {
      itemCollaborationStore.delete(context, itemId);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response<>(new ReturnCode(ErrorCode.CL_ITEM_DELETE, Module.ZCSP, null, ze
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
      return new Response<>(new ReturnCode(ErrorCode.CL_ITEM_VERSION_CREATE, Module.ZCSP, null, ze
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
          new Response<>(new ReturnCode(ErrorCode.CL_ITEM_VERSION_UPDATE, Module.ZCSP, null, ze
              .getReturnCode()));
    }

  }

  @Override
  public Response<Void> commitElements(SessionContext context, Id itemId, Id versionId,
                                       String message) {
    try {
      getElementCollaborationStore().commit(context, itemId, versionId, message);
      return new Response(Void.TYPE);
    } catch (ZusammenException zue) {
      return new Response<>(new ReturnCode(ErrorCode.CL_ELEMENT_CREATE, Module.ZCSP, null, zue
          .getReturnCode()));
    }
  }

  @Override
  public Response<Void> createElement(SessionContext context, CollaborationElement element) {
    try {
      getElementCollaborationStore().create(context, element);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response<>(new ReturnCode(ErrorCode.CL_ELEMENT_CREATE, Module.ZCSP, null, ze
          .getReturnCode()));
    }

  }

  @Override
  public Response<Void> updateElement(SessionContext context, CollaborationElement element) {
    try {
      getElementCollaborationStore().update(context, element);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response<>(new ReturnCode(ErrorCode.CL_ELEMENT_UPDATE, Module.ZCSP, null, ze
          .getReturnCode()));
    }

  }

  @Override
  public Response<Void> deleteElement(SessionContext context, CollaborationElement element) {

    try {
      getElementCollaborationStore().delete(context, element);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response<>(new ReturnCode(ErrorCode.CL_ELEMENT_DELETE, Module.ZCSP, null, ze
          .getReturnCode()));
    }

  }

  @Override
  public Response<Void> resolveElementConflict(SessionContext context, CollaborationElement element,
                                               Resolution resolution) {
    throw new UnsupportedOperationException(
        "conflict resolution is not supported in the current git plugin");
  }

  @Override
  public Response<Void> deleteItemVersion(SessionContext context, Id itemId, Id versionId) {
    try {
      getItemVersionCollaborationStore().delete(context, itemId, versionId);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response<>(new ReturnCode(ErrorCode.CL_ITEM_VERSION_DELETE, Module.ZCSP, null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<ItemVersionStatus> getItemVersionStatus(SessionContext context, Id itemId,
                                                          Id versionId) {
    throw new UnsupportedOperationException("getItemVersionStatus is not yet supported");
  }

  @Override
  public Response<Void> tagItemVersion(SessionContext context, Id itemId, Id versionId,
                                       Id changeId, Tag tag) {
    try {
      getItemVersionCollaborationStore().tag(context, itemId, versionId, changeId, tag);
      return new Response(Void.TYPE);
    } catch (ZusammenException ze) {
      return new Response<>(
          new ReturnCode(ErrorCode.CL_ITEM_VERSION_TAG, Module.ZCSP, null, ze.getReturnCode()));
    }
  }

  @Override
  public Response<CollaborationPublishResult> publishItemVersion(SessionContext context, Id itemId,
                                                                 Id versionId, String message) {

    try {
      CollaborationPublishResult collaborationPublishResult = getItemVersionCollaborationStore()
          .publish(context, itemId, versionId, message);
      return new Response<>(collaborationPublishResult);
    } catch (ZusammenException ze) {
      return new Response<>(new ReturnCode(ErrorCode.CL_ITEM_VERSION_PUBLISH, Module.ZCSP, null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<CollaborationMergeResult> syncItemVersion(SessionContext context, Id itemId, Id
      versionId) {
    try {
      return new Response<>(getItemVersionCollaborationStore().sync(context, itemId, versionId));
    } catch (ZusammenException ze) {
      return new Response<>(new ReturnCode(ErrorCode.CL_ITEM_VERSION_SYNC, Module.ZCSP, null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<CollaborationMergeResult> mergeItemVersion(SessionContext context, Id itemId, Id
      versionId, Id sourceVersionId) {
    try {
      return new Response<>(getItemVersionCollaborationStore().merge(context, itemId, versionId,
          sourceVersionId));
    } catch (ZusammenException ze) {
      return new Response<>(new ReturnCode(ErrorCode.CL_ITEM_VERSION_MERGE, Module.ZCSP, null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<CollaborationElement> getElement(SessionContext context, ElementContext
      elementContext,
                                                   Namespace namespace, Id elementId) {
    try {
      return new Response<>(getElementCollaborationStore().get(context, elementContext, namespace,
          elementId));
    } catch (ZusammenException ze) {
      return new Response<>(new ReturnCode(ErrorCode.CL_ELEMENT_GET, Module.ZCSP, null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<CollaborationElementConflict> getElementConflict(SessionContext context,
                                                                   ElementContext elementContext,
                                                                   Namespace namespace,
                                                                   Id elementId) {
    throw new UnsupportedOperationException(
        "conflict resolution is not supported in the current git plugin");
  }

  @Override
  public Response<ItemVersionHistory> listItemVersionHistory(SessionContext context, Id itemId,
                                                             Id versionId) {
    try {
      return new Response<>(
          getItemVersionCollaborationStore().listHistory(context, itemId, versionId));
    } catch (ZusammenException ze) {
      return new Response<>(new ReturnCode(ErrorCode.CL_ITEM_VERSION_HISTORY, Module.ZCSP,
          null, ze
          .getReturnCode()));
    }
  }

  @Override
  public Response<CollaborationMergeChange> resetItemVersionHistory(SessionContext context,
                                                                    Id itemId, Id versionId,
                                                                    String changeRef) {
    try {
      ElementContext elementContext = new ElementContext(itemId, versionId);
      return new Response<>(
          getItemVersionCollaborationStore().resetHistory(context, elementContext, changeRef));
    } catch (ZusammenException ze) {
      return new Response<>(
          new ReturnCode(ErrorCode.CL_ITEM_VERSION_RESET_HISTORY, Module.ZCSP, null,
              ze.getReturnCode()));
    }
  }

  @Override
  public Response<Collection<CollaborationElement>> listElements(SessionContext context,
                                                                 ElementContext elementContext,
                                                                 Namespace namespace,
                                                                 Id elementId) {
    ElementCollaborationStore collaborationStore = getElementCollaborationStore();
    CollaborationElement parentElement =
        collaborationStore.get(context, elementContext, namespace, elementId);

    Namespace childNamespace = Id.ZERO.getValue().equals(parentElement.getId().getValue())
        ? Namespace.ROOT_NAMESPACE
        : new Namespace(namespace, parentElement.getId());

    return new Response<>(parentElement.getSubElements().stream()
        .map(id -> collaborationStore.get(context, elementContext, childNamespace, id))
        .collect(Collectors.toList()));
  }

  @Override
  public Response<CollaborationItemVersionConflict> getItemVersionConflict(SessionContext context,
                                                                           Id itemId,
                                                                           Id versionId) {
    throw new UnsupportedOperationException(
        "conflict resolution is not supported in the current git plugin");
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