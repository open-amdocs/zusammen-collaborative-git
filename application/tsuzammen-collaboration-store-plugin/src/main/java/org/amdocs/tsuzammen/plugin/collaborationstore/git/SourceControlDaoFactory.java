package org.amdocs.tsuzammen.plugin.collaborationstore.git;

import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.utils.facade.api.AbstractComponentFactory;
import org.amdocs.tsuzammen.utils.facade.api.AbstractFactory;

public abstract class SourceControlDaoFactory
    extends AbstractComponentFactory<GitSourceControlDao> {

  public static SourceControlDaoFactory getInstance() {
    return AbstractFactory.getInstance(SourceControlDaoFactory.class);
  }

  public abstract <T> GitSourceControlDao createInterface(SessionContext context);
}
