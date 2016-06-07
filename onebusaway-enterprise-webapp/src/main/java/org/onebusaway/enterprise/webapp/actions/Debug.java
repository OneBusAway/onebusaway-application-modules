package org.onebusaway.enterprise.webapp.actions;

import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

public class Debug extends OneBusAwayEnterpriseActionSupport {

	private static final long serialVersionUID = 1L;
	@Autowired
	private ConfigurationService _configurationService;
	private String q;
	public void setQ(String q) {
		this.q = q;
	}
	
	public String getQ(String q) {
		return q;
	}
	
    public String getGoogleMapsClientId() {
      return _configurationService.getConfigurationValueAsString("display.googleMapsClientId", "");    
    }

	@Override
	  public String execute() throws Exception {
	    ActionContext context = ActionContext.getContext();
	    ActionInvocation invocation = context.getActionInvocation();
	    ActionProxy proxy = invocation.getProxy();

	    String name = proxy.getActionName().toLowerCase();
	    String namespace = proxy.getNamespace().toLowerCase();

	    // FIXME: since Struts doesn't seem to like wildcard namespaces (in wiki/IndexAction) and default
	    // actions, we have to have this action check to see if it's being called as a "default" action and
	    // return the 404 message if so. There has to be a better way than this? 
	    if((name.equals("") || name.equals("index") || name.equals("debug")) && (namespace.equals("") || namespace.equals("/"))) {
	      return SUCCESS;
	    }

	    return "NotFound";
	  }
}
