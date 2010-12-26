package org.onebusaway.webapp.actions;

import org.onebusaway.presentation.impl.users.SetupAction;
import org.onebusaway.presentation.services.cachecontrol.CacheControl;

@SetupAction
@CacheControl(maxAge = 365 * 24 * 60 * 60)
public class ResourceAction extends
    org.onebusaway.presentation.impl.resources.ResourceAction {

  private static final long serialVersionUID = 1L;

}
