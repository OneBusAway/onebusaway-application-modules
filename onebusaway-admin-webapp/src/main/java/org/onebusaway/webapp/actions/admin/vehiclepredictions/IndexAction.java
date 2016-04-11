package org.onebusaway.webapp.actions.admin.vehiclepredictions;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;

@Namespace(value="/admin/vehiclepredictions")
//@Results({
//  @Result(type = "redirectAction", name = "redirect",
//      params={"actionName", "index"})
//})
public class IndexAction extends OneBusAwayNYCAdminActionSupport {
  
  
  public String input() {
    return SUCCESS;
  }
}