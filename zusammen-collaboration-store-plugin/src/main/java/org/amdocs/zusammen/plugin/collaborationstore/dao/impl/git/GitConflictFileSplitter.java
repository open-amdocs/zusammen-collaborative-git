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

package org.amdocs.zusammen.plugin.collaborationstore.dao.impl.git;

import org.amdocs.zusammen.plugin.collaborationstore.types.LocalRemoteDataConflict;




public class GitConflictFileSplitter {

  protected static final String HEADER_END = "<<<<<<<";
  protected static final String TRAILER_START = ">>>>>>>";
  protected static final String SWITCH_FILE = "=======";

  public static LocalRemoteDataConflict splitMergedFile(byte[] mergedFile) {
    LocalRemoteDataConflict localRemoteDataConflict = new LocalRemoteDataConflict();
    String content = new String (mergedFile);
    String[] lines = content.split("\\r\\n|\\r|\\n");

    boolean headerEnd = false; //until <<<<<<<
    boolean trailerStart = false; // from >>>>>>>
    boolean switchFile = false; // from =======
    for (String line : lines) {
      if (line.startsWith(HEADER_END)) {
        headerEnd = true;
        continue;
      }
      if (line.startsWith(TRAILER_START)) {
        trailerStart = true;
        continue;
      }
      if (line.startsWith(SWITCH_FILE)) {
        switchFile = true;
        continue;
      }
      if (!switchFile || trailerStart) {
        localRemoteDataConflict.appendL(line);
      }
      if (switchFile || trailerStart || !headerEnd) {
        localRemoteDataConflict.appendR(line);
      }
    }
    return localRemoteDataConflict;
  }
}
