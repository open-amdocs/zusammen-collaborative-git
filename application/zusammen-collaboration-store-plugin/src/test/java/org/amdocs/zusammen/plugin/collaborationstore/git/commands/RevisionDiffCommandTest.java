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

package org.amdocs.zusammen.plugin.collaborationstore.git.commands;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class RevisionDiffCommandTest {
  @Test
  public void testCall() throws Exception {
    /*Git mockGit = Mockito.mock(Git.class);
    RevisionDiffCommand command = RevisionDiffCommand.init(mockGit);
    ObjectId from = ObjectId.fromString(UUID.randomUUID().toString()+"0000");
    ObjectId to = ObjectId.fromString(UUID.randomUUID().toString()+"0000");
    command.from(from);
    command.to(to);
    command.call();*/
  }

}