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

package org.amdocs.zusammen.plugin.collaborationstore.git.utils;

import org.amdocs.zusammen.commons.configuration.impl.ConfigurationAccessor;
import org.amdocs.zusammen.sdk.SdkConstants;

public class PluginConstants {
  public static final String PUBLIC_PATH_PROP = "public.path";
  public static final String PUBLIC_URL_PROP = "public.url";
  public static final String BP_PATH_PROP = "blueprint.path";
  public static final String PRIVATE_PATH_PROP = "private.path";
  public static final String MASTER_BRANCH_PROP = "master.branch";

  //file names
  public static final String ITEM_VERSION_INFO_FILE_NAME = "itemVersionInfo.json";
  public static final String INFO_FILE_NAME = "info.json";
  public static final String RELATIONS_FILE_NAME = "relations.json";
  public static final String VISUALIZATION_FILE_NAME = "visualization.json";
  public static final String DATA_FILE_NAME = "data";
  public static final String SEARCH_DATA_FILE_NAME = "search";

  public static final String SAVE_ITEM_VERSION_MESSAGE = "Save Item Version";
  public static final String DELETE_ITEM_VERSION_MESSAGE = "Delete Item Version";
  public static final String ADD_ITEM_INFO_MESSAGE = "Add Item Info";
  public static final String ZUSAMMEN_TAGGING_FILE_NAME = ".zusammen";
  public static final String ITEM_VERSION_BASE_ID = "itemVersionBaseId";
  public static final String ITEM_VERSION_ID = "itemVersionId";
  public static String PUBLIC_PATH = ConfigurationAccessor.getPluginProperty(SdkConstants
      .ZUSAMMEN_COLLABORATIVE_STORE, PUBLIC_PATH_PROP);
  public static String PRIVATE_PATH = ConfigurationAccessor.getPluginProperty(
      SdkConstants.ZUSAMMEN_COLLABORATIVE_STORE, PRIVATE_PATH_PROP);
  public static String PUBLIC_URL = ConfigurationAccessor.getPluginProperty(
      SdkConstants.ZUSAMMEN_COLLABORATIVE_STORE, PUBLIC_URL_PROP);
  public static String BP_PATH = ConfigurationAccessor.getPluginProperty(
      SdkConstants.ZUSAMMEN_COLLABORATIVE_STORE, BP_PATH_PROP);
  public static String TENANT = "{tenant}";
}
