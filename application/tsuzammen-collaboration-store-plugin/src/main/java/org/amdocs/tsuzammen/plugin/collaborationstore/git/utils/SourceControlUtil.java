package org.amdocs.tsuzammen.plugin.collaborationstore.git.utils;


import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.commons.datatypes.item.Entity;
import org.amdocs.tsuzammen.utils.common.CommonMethods;
import org.eclipse.jgit.api.Git;

import java.io.File;

public class SourceControlUtil {


  private static String convertNamespaceToPath(String namespace) {
    String[] pathArray = namespace.split(".");

    return CommonMethods.arrayToSeparatedString(pathArray, File.separatorChar);
  }

  public static String getPrivateRepositoryPath(Git git) {
    return git.getRepository().getWorkTree().getPath();
  }

  public static String getPrivateRepositoryPath(SessionContext context, String path, String itemId) {
    StringBuffer sb = new StringBuffer();
    sb.append(path).append(File.separator).append("users").append(File.separator).append(context
        .getUser()
        .getUserName())
        .append(File
            .separator).append(itemId);

    return sb.toString();
  }

  public static String getPublicRepositoryPath(SessionContext context, String
      path, String itemId) {
    StringBuffer sb = new StringBuffer();
    sb.append(path).append(File.separator).append(itemId);

    return sb.toString();
  }

  public static boolean isEmpty(Entity entity) {
    return (entity.getOid() != null &&
            entity.getData() == null &&
            entity.getInfo() == null &&
            entity.getRelations() == null &&
            entity.getContents() ==null &&
            entity.getVisualization() ==null );
  }
}
