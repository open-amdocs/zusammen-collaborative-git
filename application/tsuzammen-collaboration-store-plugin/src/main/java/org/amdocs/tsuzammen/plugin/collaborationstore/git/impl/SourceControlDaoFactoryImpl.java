package org.amdocs.tsuzammen.plugin.collaborationstore.git.impl;

import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.GitSourceControlDao;
import org.amdocs.tsuzammen.plugin.collaborationstore.git.SourceControlDaoFactory;

public class SourceControlDaoFactoryImpl extends SourceControlDaoFactory {
  @Override
  public GitSourceControlDao createInterface(SessionContext context) {
    return new GitSourceControlDaoImpl();
  }
}
