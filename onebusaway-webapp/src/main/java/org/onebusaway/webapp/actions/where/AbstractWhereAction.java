package org.onebusaway.webapp.actions.where;

import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.webapp.actions.AbstractAction;

@InterceptorRefs( {@InterceptorRef("onebusaway-webapp-stack")})
@Results( {
    @Result(type = "chain", name = "query-default-search-location", location = "query-default-search-location"),
    @Result(type = "chain", name = "set-default-search-location", location = "set-default-search-location"),
    @Result(type = "chain", name = "stops", location = "stops"),
    @Result(type = "chain", name = "routes", location = "routes")})
public abstract class AbstractWhereAction extends AbstractAction {

  private static final long serialVersionUID = 1L;
}
