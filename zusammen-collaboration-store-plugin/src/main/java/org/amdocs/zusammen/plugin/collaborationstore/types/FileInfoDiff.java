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

package org.amdocs.zusammen.plugin.collaborationstore.types;

import org.amdocs.zusammen.datatypes.item.Action;

public class FileInfoDiff {


  private final String filePath;
  private final Action action;

  public FileInfoDiff(String filePath, Action action) {
      this.filePath = filePath;
      this.action = action;
  }

  public String getFilePath() {
    return filePath;
  }

  public Action getAction() {
    return action;
  }
}
