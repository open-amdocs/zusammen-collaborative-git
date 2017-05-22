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

package com.amdocs.zusammen.plugin.collaborationstore.dao.api;

import com.amdocs.zusammen.plugin.collaborationstore.dao.api.git.GitSourceControlCommand;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.utils.facade.api.AbstractComponentFactory;
import com.amdocs.zusammen.utils.facade.api.AbstractFactory;


public abstract class SourceControlCommandFactory
    extends AbstractComponentFactory<GitSourceControlCommand> {

  public static final String GIT = "git";

  public static SourceControlCommandFactory getInstance() {
    return AbstractFactory
        .getInstance(
            SourceControlCommandFactory.class);
  }

  public abstract <T> GitSourceControlCommand createInterface(SessionContext context, String type);
}

