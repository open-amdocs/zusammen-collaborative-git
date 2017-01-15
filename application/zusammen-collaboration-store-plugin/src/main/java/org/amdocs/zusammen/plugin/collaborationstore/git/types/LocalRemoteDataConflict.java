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

package org.amdocs.zusammen.plugin.collaborationstore.git.types;

import java.io.InputStream;

public class LocalRemoteDataConflict {
  StringBuffer localSB = new StringBuffer();
  StringBuffer remoteSB = new StringBuffer();


  public void appendL(String line) {
    this.localSB.append(line).append(System.lineSeparator());
  }

  public void appendR(String line) {
    this.remoteSB.append(line).append(System.lineSeparator());
  }

  public byte[] getLocal() {
    return this.localSB.toString().getBytes();
  }

  public byte[] getRemote() {
    return this.remoteSB.toString().getBytes();
  }
}
