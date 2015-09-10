package org.onebusaway.enterprise.webapp.actions.api;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;

@ParentPackage("json-default")
@Result(type="json", params={"callbackParameter", "callback"})

public class GtfsrtProxyAction extends OneBusAwayEnterpriseActionSupport {

	private static final long serialVersionUID = 1L;
	
	private String url = "http://mobullity.forest.usf.edu:8088/vehicle-positions?debug";
	
	private String _results;
	
	
	@Override
	public String execute() throws Exception {
		
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url);
		client.executeMethod(method);
		_results = method.getResponseBodyAsString();
		
	  return SUCCESS;
	}
	
	public String getResults() {
		return _results;
	}
}
