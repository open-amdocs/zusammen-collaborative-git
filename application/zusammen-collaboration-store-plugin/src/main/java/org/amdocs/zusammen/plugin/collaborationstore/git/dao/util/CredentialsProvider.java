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

package org.amdocs.zusammen.plugin.collaborationstore.git.dao.util;

import org.amdocs.zusammen.commons.configuration.impl.ConfigurationAccessor;
import org.amdocs.zusammen.datatypes.SessionContext;
import org.amdocs.zusammen.sdk.SdkConstants;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;

import java.util.BitSet;


public class CredentialsProvider {
  private static final String GIT_USERNAME = "git.username";
  private static final String GIT_PASSWORD = "git.password";
  private static final int USERNAME_BIT = 0;
  private static final int PASSWORD_BIT = 1;


  private static org.eclipse.jgit.transport.CredentialsProvider credentialsProvider;


  public static org.eclipse.jgit.transport.CredentialsProvider getCredentialsProvider(
      SessionContext context) {
    synchronized (CredentialsProvider.class) {
      if (credentialsProvider == null) {
        credentialsProvider = new org.eclipse.jgit.transport.CredentialsProvider() {

          private final String username = ConfigurationAccessor
              .getPluginProperty(SdkConstants.ZUSAMMEN_COLLABORATIVE_STORE,
                  GIT_USERNAME);

          private final String password = ConfigurationAccessor.getPluginProperty(SdkConstants
                  .ZUSAMMEN_COLLABORATIVE_STORE,
              GIT_PASSWORD);

          @Override
          public boolean isInteractive() {
            return false;
          }

          @Override
          public boolean supports(CredentialItem... credentialItems) {

            BitSet flags = new BitSet();

            for (CredentialItem item : credentialItems) {

              if (item instanceof CredentialItem.Username) {
                flags.set(USERNAME_BIT, true);
              } else if (item instanceof CredentialItem.Password) {
                flags.set(PASSWORD_BIT, true);
              } else {
                return false;
              }
            }

            return flags.length() == 2;
          }

          @Override
          public boolean get(URIish urIish, CredentialItem... credentialItems)
              throws UnsupportedCredentialItem {

            if ((this.username == null) || (this.password == null)) {
              throw new IllegalArgumentException("Username and password must be provided via -D"
                  + GIT_USERNAME + " and -D" + GIT_PASSWORD);

            }

            BitSet flags = new BitSet();

            for (CredentialItem item : credentialItems) {

              if (item instanceof CredentialItem.Username) {
                ((CredentialItem.Username) item).setValue(this.username);
                flags.set(USERNAME_BIT);
              } else if (item instanceof CredentialItem.Password) {
                ((CredentialItem.Password) item).setValueNoCopy(this.password.toCharArray());
                flags.set(PASSWORD_BIT);
              } else {
                throw new UnsupportedCredentialItem(urIish, item.toString());
              }
            }

            return flags.length() == 2;
          }
        };




                /*credentialsProvider =
                        new org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider(
                                Configuration.getStringPluginProperty(context, GitCollaborationStorePluginImpl.PLUGIN_NAME, GIT_USERNAME),
                                Configuration.getStringPluginProperty(context, GitCollaborationStorePluginImpl.PLUGIN_NAME, GIT_PASSWORD));*/
      }
    }
    return credentialsProvider;
  }
}
