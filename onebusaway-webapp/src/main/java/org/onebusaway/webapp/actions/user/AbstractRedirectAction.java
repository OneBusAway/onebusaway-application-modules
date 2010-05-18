package org.onebusaway.webapp.actions.user;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.webapp.actions.AbstractAction;

@Results( {@Result(type = "redirectAction", params = {"actionName", "index"})})
public class AbstractRedirectAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

}
