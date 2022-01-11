/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.admin.service.server.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.admin.service.RemoteConnectionService;
import org.onebusaway.admin.service.server.BundleServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;

public class BundleServerServiceImpl implements BundleServerService, ServletContextAware {

  private static Logger _log = LoggerFactory.getLogger(BundleServerServiceImpl.class);
  private static final String PING_API = "/ping/remote";
  private static final String LOCAL_HOST = "localhost";
  private static final String DEFAULT_CONTEXT = "onebusaway-admin-webapp/api";
  private RemoteConnectionService remoteConnectionService;

  private AWSCredentials _credentials;
  private AmazonEC2Client _ec2;
  private final ObjectMapper _mapper = new ObjectMapper();
  
 	private String _username;
	private String _password;
	private String _context = DEFAULT_CONTEXT;
	private boolean _isAws = false;
  private int _adminPort = 8080;

	@Override
	public void setEc2User(String user) {
		_username = user;
	}
	@Override
	public void setEc2Password(String password) {
		_password = password;
	}

	@PostConstruct
	@Override
  public void setup() {
	  _mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	  
		try {
			if (_isAws) {
				_credentials = new BasicAWSCredentials(_username, _password);
				_ec2 = new AmazonEC2Client(_credentials);
			} else {
				_log.info("setup preferring local config");
			}
		} catch (Throwable t) {
		  _log.error("BundleServerServiceImpl setup failed, likely due to missing or invalid credentials");
			_log.error("BundleServerService setup failed:", t);
		}

  }
  
	@Override
	public void setServletContext(ServletContext servletContext) {
		if (servletContext != null) {
			String user = servletContext.getInitParameter("ec2.user");
			if (user != null) {
				_isAws = true;
				setEc2User(user);
			}
			String password = servletContext.getInitParameter("ec2.password");
			if (password != null) {
				_isAws = true;
				setEc2Password(password);
			}
			String context = servletContext.getInitParameter("admin.context");
			if (context != null) {
			  _log.info("using context=" + context);
			  _context = context;
			}
     String adminPortStr = servletContext.getInitParameter("admin.port");
	      if (adminPortStr != null) {
	        _log.info("using admin port=" + adminPortStr);
	        _adminPort = Integer.parseInt(adminPortStr);
	      }

		}
	}

  @Override
  public String start(String instanceId) {
	if (!_isAws) {
		return LOCAL_HOST;
	}
	
    if (LOCAL_HOST.equalsIgnoreCase(instanceId)) {
      return instanceId;
    }
    
    List<String> instances = new ArrayList<String>();
    instances.add(instanceId);
    StartInstancesRequest startInstancesRequest = new StartInstancesRequest(instances);
    StartInstancesResult startInstancesResult = _ec2.startInstances(startInstancesRequest);
    InstanceStateChange change = null;
    if (!startInstancesResult.getStartingInstances().isEmpty()) {
      change = startInstancesResult.getStartingInstances().get(0);
      _log.info("from state=" + change.getPreviousState() +  " to state=" + change.getCurrentState());
      return change.getInstanceId();
    }
    return null;
  }

  private Instance getInstance(String instanceId) {
    try {
      DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
      List<String> list = new ArrayList<String>();
      list.add(instanceId);
      describeInstancesRequest.setInstanceIds(list);
      // this call can timeout and throw AmazonClientException if AWS is having
      // a bad day
      DescribeInstancesResult result = _ec2.describeInstances(describeInstancesRequest);
      if (!result.getReservations().isEmpty()) {
        if (!result.getReservations().get(0).getInstances().isEmpty()) {
          Instance i = result.getReservations().get(0).getInstances().get(0);
          return i;
        }
      }
    } catch (Exception e) {
      _log.error("Call to AWS threw exception", e);
    }
    return null;
  }
  
  public String pollPublicDns(String instanceId, int maxWaitSeconds) {
    try {
      int count = 0;
      String dns = findPublicDns(instanceId);
      while ((dns == null || dns.length() == 0) && count < maxWaitSeconds) {
        Thread.sleep(1000);
        dns = findPublicDns(instanceId);
        count++;
      }
      return dns;
    } catch (InterruptedException ie) {
      return null;
    }
  }
  
  @Override
  public String findPublicDns(String instanceId) {
    if (LOCAL_HOST.equalsIgnoreCase(instanceId)) {
      return instanceId;
    }
    
    Instance i = getInstance(instanceId);
    if (i != null && i.getPublicDnsName() != null) {
      return i.getPublicDnsName();
    }
    if (i != null && !i.getNetworkInterfaces().isEmpty()) {

      // if you need public IP, you need to lookup the association
      return i.getNetworkInterfaces().get(0).getPrivateDnsName();
    }
    return null;
  }
  
  @Override
  public String findPublicIp(String instanceId) {
    if (LOCAL_HOST.equalsIgnoreCase(instanceId)) {
      return instanceId;
    }

    Instance i = getInstance(instanceId);
    if (i != null && i.getPublicIpAddress() != null) {
      return i.getPublicDnsName();
    }
    if (i != null && !i.getNetworkInterfaces().isEmpty()) {
      if (i.getNetworkInterfaces().get(0).getAssociation() != null) {
        return i.getNetworkInterfaces().get(0).getAssociation().getPublicIp();
      }
    }
    return null;
  }
  
  @Override
  public String stop(String instanceId) {
	if (!_isAws) {
		return LOCAL_HOST;
	}
    if (LOCAL_HOST.equalsIgnoreCase(instanceId)) {
      return instanceId;
    }
    
    List<String> instances = new ArrayList<String>();
    instances.add(instanceId);
    StopInstancesRequest stopInstancesRequest = new StopInstancesRequest(instances);
    StopInstancesResult stopInstancesResult = _ec2.stopInstances(stopInstancesRequest);
    InstanceStateChange change = null;
    if (!stopInstancesResult.getStoppingInstances().isEmpty()) {
      change = stopInstancesResult.getStoppingInstances().get(0);
      _log.info("from state=" + change.getPreviousState() +  " to state=" + change.getCurrentState());
      return change.getInstanceId();
    }
    return null;
  }

  @Override
  public boolean ping(String instanceId) {
    String json = (String)makeRequestInternal(instanceId, PING_API, null, String.class);
    _log.debug("json=" + json);
    if (json != null) json = json.trim();
    return "{1}".equals(json);
  }

   private String generateUrl(String host, String apiCall) {
     if (LOCAL_HOST.equalsIgnoreCase(host)) { 
       String url = "http://" + host + ":" + _adminPort + "/" + _context + apiCall;
       _log.debug("generateUrl=" + url + " (use admin.context to override)");
       return url;
     }
     return "http://" + host + ":" + _adminPort + "/api" + apiCall;
   }
   
   protected <T> T makeRequestInternal(String instanceId, String apiCall, String jsonPayload, Class<T> returnType) {
	   return makeRequestInternal(instanceId, apiCall, jsonPayload, returnType, null, null);
   }
   
   protected <T> T makeRequestInternal(String instanceId, String apiCall, String jsonPayload, Class<T> returnType, Map<String, String> params, String sessionId) {	   
	   _log.debug("makeRequestInternal(" + instanceId + ", " + apiCall + ")");
	   String host = this.findPublicDns(instanceId);
	   if (host == null || host.length() == 0) {
		   _log.error("makeRequest called with unknown instanceId=" + instanceId);
		   return null;
	   }
	
	   String url = generateUrl(host, apiCall);
	   _log.debug("making request for " + url);

	   // copy stream into String
	   String content;
	   if (params == null)
		   content = remoteConnectionService.getContent(url, sessionId);
	   else
		   content = remoteConnectionService.postContent(url, params, sessionId);
	   
	   if (content == null) return null;
	   
	   // parse content to appropriate return type
     T t = null;
     if (returnType == String.class) {
       t = (T) content;
     } else {
       String json = content;
       try {
         t = _mapper.readValue(json, returnType);
       } catch (JsonParseException e) {
         _log.error("Error parsing json content : " + e);
         e.printStackTrace();
       } catch (JsonMappingException e) {
         _log.error("Error mapping parsed json content : " + e);
         e.printStackTrace();
       } catch (IOException e) {
        _log.error("Error parsing json content : " + e);
         e.printStackTrace();
       }
     }
     _log.debug("got |" + t + "|");
     return t;
   }
   
   @Override
   public <T> T makeRequest(String instanceId, String apiCall, Object payload, Class<T> returnType, int waitTimeInSeconds, Map params, String sessionId) {
	   try {
	       // serialize payload
	       String jsonPayload = toJson(payload);
	       
	       // wait for it to answer pings
	       int count = 0;
	       boolean isAlive= ping(instanceId);
	       while (!isAlive && count < waitTimeInSeconds) {
	         count++;
	         Thread.sleep(5 * 1000);
	         isAlive = ping(instanceId);
	       }

	       if (!isAlive) {
	         _log.error("instanceId=" + instanceId + " failed to start");
	         return null;
	       }
	       // make our request
	       return (T) makeRequestInternal(instanceId, apiCall, jsonPayload, returnType, params, sessionId);
	     } catch (InterruptedException ie) {
	       return null;
	     } finally {
	       _log.debug("exiting makeRequest");
	     }
   }
   
   @Override
   public <T> T makeRequest(String instanceId, String apiCall, Object payload, Class<T> returnType, int waitTimeInSeconds, Map params) {
	   return makeRequest(instanceId, apiCall, payload, returnType, waitTimeInSeconds, params, null);
   }
   
   @Override
   public <T> T makeRequest(String instanceId, String apiCall, Object payload, Class<T> returnType, int waitTimeInSeconds) {
     return makeRequest(instanceId, apiCall, payload, returnType, waitTimeInSeconds, null);
   }
   
  private String toJson(Object payload) {
    String jsonPayload = null;
     if (payload != null){
       StringWriter sw = new StringWriter();
       try {
         final MappingJsonFactory jsonFactory = new MappingJsonFactory();
         final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(sw);
         _mapper.writeValue(jsonGenerator, payload);
       } catch (Exception any){
         _log.error("json execption=", any);
       }
        jsonPayload = sw.toString();
     }
    return jsonPayload;
  }
	/**
	 * Injects RemoteConnectionService
	 * @param remoteConnectionService the remoteConnectionService to set
	 */
  	@Autowired
	public void setRemoteConnectionService(
			RemoteConnectionService remoteConnectionService) {
		this.remoteConnectionService = remoteConnectionService;
	}
}
