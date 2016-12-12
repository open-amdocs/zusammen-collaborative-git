package org.amdocs.tsuzammen.plugin.collaborationstore.git.impl;

import org.amdocs.tsuzammen.commons.configuration.impl.ConfigurationAccessor;
import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.sdk.utils.SdkConstants;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;

import java.util.BitSet;
import java.util.Optional;


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
              .getPluginProperty(SdkConstants.TSUZAMMEN_COLLABORATIVE_STORE,
                  GIT_USERNAME);

          private final String password = ConfigurationAccessor.getPluginProperty( SdkConstants
                  .TSUZAMMEN_COLLABORATIVE_STORE,
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
