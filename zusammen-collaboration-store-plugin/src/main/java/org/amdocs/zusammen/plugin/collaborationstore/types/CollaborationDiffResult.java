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

import java.util.ArrayList;
import java.util.Collection;

public class CollaborationDiffResult {

  private Collection<FileInfoDiff> fileInfoDiffs = new ArrayList<>();
  public enum MODE {
    NEW,UPDATE
  }

  private MODE mode;

  public CollaborationDiffResult(MODE mode){
    this.mode = mode;
  }

  public void add(FileInfoDiff fileInfoDiff) {
      this.fileInfoDiffs.add(fileInfoDiff);
  }

  public Collection<FileInfoDiff> getFileInfoDiffs() {
    return fileInfoDiffs;
  }

  public MODE getMode() {
    return mode;
  }
}
