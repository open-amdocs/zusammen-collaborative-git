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

package com.amdocs.zusammen.plugin.collaborationstore.dao.impl.git;

import com.amdocs.zusammen.plugin.collaborationstore.types.LocalRemoteDataConflict;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GitConflictFileSplitterTest {

  private static String fileContent = "000000000" + System.lineSeparator() +
      GitConflictFileSplitter.HEADER_END + System.lineSeparator() +
      "aaaaaaaaaa" + System.lineSeparator() +
      GitConflictFileSplitter.SWITCH_FILE + System.lineSeparator() +
      "bbbbbbbbbb" + System.lineSeparator() +
      GitConflictFileSplitter.TRAILER_START + System.lineSeparator() +
      "1111111111";

  @Test
  public void testSplitMergedFile() throws Exception {
    GitConflictFileSplitter gitConflictFileSplitter = new GitConflictFileSplitter();
    LocalRemoteDataConflict localRemoteDataConflict = gitConflictFileSplitter.splitMergedFile
        (fileContent.getBytes());

    Assert.assertEquals(new String(localRemoteDataConflict.getLocal()), "000000000" + System
        .lineSeparator() +
        "aaaaaaaaaa" + System.lineSeparator() +
        "1111111111" + System.lineSeparator());
    Assert.assertEquals(new String(localRemoteDataConflict.getRemote()), "000000000" + System
        .lineSeparator() +
        "bbbbbbbbbb" + System.lineSeparator() +
        "1111111111" + System.lineSeparator());

  }

}