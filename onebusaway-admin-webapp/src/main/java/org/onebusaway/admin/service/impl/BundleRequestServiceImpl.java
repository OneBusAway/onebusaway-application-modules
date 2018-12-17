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
package org.onebusaway.admin.service.impl;


import org.onebusaway.admin.model.BundleBuildRequest;
import org.onebusaway.admin.model.BundleBuildResponse;
import org.onebusaway.admin.model.BundleRequest;
import org.onebusaway.admin.model.BundleResponse;
import org.onebusaway.admin.service.BundleRequestService;
import org.onebusaway.admin.service.EmailService;
import org.onebusaway.admin.service.bundle.BundleBuildingService;
import org.onebusaway.admin.service.server.BundleServerService;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ServletContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

public class BundleRequestServiceImpl implements BundleRequestService, ServletContextAware {

  private static final int WAIT_SECONDS = 120;
  protected static Logger _log = LoggerFactory.getLogger(BundleRequestServiceImpl.class);
  private ExecutorService _executorService = null;
  private ConfigurationService configurationService;
  private BundleServerService _bundleServer;
  private EmailService _emailService;
  private Integer jobCounter = 0;
	private String serverURL;
	private String _instanceId;
  private Map<String, BundleResponse> _validationMap = new HashMap<String, BundleResponse>();
  private Map<String, BundleBuildResponse> _buildMap = new HashMap<String, BundleBuildResponse>();

  @Autowired
  private BundleBuildingService _bundleService;

  @Autowired
  public void setEmailService(EmailService service) {
    _emailService = service;
  }

  @Autowired
  public void setBundleServerService(BundleServerService service) {
    _bundleServer = service;
  }
  
  @PostConstruct
  public void setup() {
    _executorService = Executors.newFixedThreadPool(1);
  }

  public void setInstanceId(String instanceId) {
    _instanceId = instanceId;
  }
  
  public String getInstanceId() {
    if (_instanceId == null)
      try {
        return configurationService.getConfigurationValueAsString("admin.instanceId", null);
      } catch (RemoteConnectFailureException e) {
        _log.error("issue looking up instanceId:", e);
        return null;
      }
    return _instanceId;
  }
  
  @Override
  /**
   * Make an asynchronous request to validate bundle(s).  The BundleResponse object is
   * updated upon completion (successful or otherwise) of the validation process.
   */
  public BundleResponse validate(BundleRequest bundleRequest) {
    String id = getNextId();
    bundleRequest.setId(id);
    BundleResponse bundleResponse = new BundleResponse(id);
    bundleResponse.addStatusMessage("queueing...");
    _log.debug("validate id=" + bundleResponse.getId());
    _validationMap.put(bundleResponse.getId(), bundleResponse);
    _executorService.execute(new ValidateThread(bundleRequest, bundleResponse));
    return bundleResponse;
  }

  @Override
  /**
   * Retrieve a BundleResponse object for the given id.
   */
  public BundleResponse lookupValidationRequest(String id) {
    return _validationMap.get(id);
  }

  @Override
  /**
   * Retrieve a BundleBuildResponse object for the given id.
   */
  public BundleBuildResponse lookupBuildRequest(String id) {
    BundleBuildResponse bbr = _buildMap.get(id);
    if (bbr == null && _bundleService != null) {
      bbr = _bundleService.getBundleBuildResponseForId(id);
    }
    return bbr;
  }
  
  /**
   * Sends email to the given email address. 
   * @param request bundle request
   * @param response bundle response
   */
  public void sendEmail(BundleBuildRequest request, BundleBuildResponse response) {
    _log.debug("in send email for requestId=" + response.getId() 
        + " with email=" + request.getEmailAddress());
    if (request.getEmailAddress() != null && request.getEmailAddress().length() > 1
        && !"null".equals(request.getEmailAddress())) {
    	String from;
    	try {
    		from = configurationService.getConfigurationValueAsString("admin.senderEmailAddress", "admintest@oba.sound.com");
    	} catch(RemoteConnectFailureException e) {
    		_log.error("Setting from email address to default value : 'admin.oba.sound.com' due to failure to connect to TDM");
    		from = "adminerror@oba.sound.com";
    		e.printStackTrace();
    	}
    	StringBuffer msg = new StringBuffer();
    	msg.append("Your Build Results are available at ");
    	msg.append(getResultLink(request.getBundleName(), response.getId(),
    			request.getBundleStartDateString(), request.getBundleEndDateString()));
    	String subject = "Bundle Build " + response.getId() + " complete";
    	_emailService.send(request.getEmailAddress(), from, subject, msg);
    }
  }
  
  @Override
  public BundleBuildResponse buildBundleResultURL(String id) {
	  BundleBuildResponse bundleResponse = this.lookupBuildRequest(id);
	  bundleResponse.setBundleResultLink(getResultLink(bundleResponse.getBundleBuildName(), bundleResponse.getId(),
			  bundleResponse.getBundleStartDate(), bundleResponse.getBundleEndDate()));
	  return bundleResponse;
  }
  
  
  private String getResultLink(String bundleName, String responseId, String bundleStartDate,
		  String bundleEndDate) {
	  StringBuffer resultLink = new StringBuffer();
	  resultLink.append(getServerURL());
	  resultLink.append("/admin/bundles/manage-bundles.action#Build");
	  resultLink.append("?fromEmail=true&id=" + responseId);
	  resultLink.append("&name=" + bundleName);
	  resultLink.append("&startDate=" + bundleStartDate);
	  resultLink.append("&endDate=" + bundleEndDate);
	  return resultLink.toString();
  }

  @Override
  /**
   * cleanup resources.
   */
  public void cleanup() {
    _validationMap.clear();
  }

  /**
   * Trivial implementation of creating unique Ids. Security is not a
   * requirement here.
   */
  private String getNextId() {
    return "" + inc();
  }

  private Integer inc() {
    synchronized (jobCounter) {
      if (jobCounter == 0 && _bundleService != null) {
        jobCounter = _bundleService.getBundleBuildResponseMaxId();
      }
      jobCounter++;
    }
    return jobCounter;
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    if (servletContext != null) {
      String key = servletContext.getInitParameter("server.url");
      if (key != null) {
        String port = servletContext.getInitParameter("admin.port");
        if (port != null) {
          key = key + ":" + port;
        }
        setServerURL(key);
      }
      String instanceOverride = servletContext.getInitParameter("admin.instanceId");
      if (instanceOverride != null) {
        setInstanceId(instanceOverride);
      }
    }
  }
 	public String getServerURL() {
	  if (serverURL == null) {
	    serverURL = "http://localhost:8080/onebusaway-admin-webapp";
	  }
	  return serverURL;
	}
	
	public void setServerURL(String url) {
	  serverURL = url;
	}

  @Override
  public BundleBuildResponse build(BundleBuildRequest bundleRequest) {
    String id = getNextId();
    bundleRequest.setId(id);
    BundleBuildResponse bundleResponse = new BundleBuildResponse(id);
    bundleResponse.setBundleBuildName(bundleRequest.getBundleName());
    bundleResponse.setBundleStartDate(bundleRequest.getBundleStartDateString());
    bundleResponse.setBundleEndDate(bundleRequest.getBundleEndDateString());
    bundleResponse.setBundleComment(bundleRequest.getBundleComment());
    _buildMap.put(bundleResponse.getId(), bundleResponse);
    bundleResponse.addStatusMessage("queueing...");
    if (_bundleService != null) {
      _bundleService.createBundleBuildResponse(bundleResponse);
    }
    _executorService.execute(new BuildThread(bundleRequest, bundleResponse));
    return bundleResponse;
  }

  protected <T> T makeRequest(String instanceId, String apiCall, Object payload, Class<T> returnType, String sessionId) {
    return _bundleServer.makeRequest(instanceId, apiCall, payload, returnType, WAIT_SECONDS, null, sessionId);
  }
  
  protected <T> T makeRequest(String instanceId, String apiCall, Object payload, Class<T> returnType, Map params, String sessionId) {
    return _bundleServer.makeRequest(instanceId, apiCall, payload, returnType, WAIT_SECONDS, params, sessionId);
  }
  
  private class ValidateThread implements Runnable {
    private static final int MAX_COUNT = 3600; //(5 hours at 5 second increments)
    private static final int MAX_PING_COUNT = 60; //(5 minutes at 5 second increments)
    private static final int MAX_ERRORS = 20;
    private BundleRequest _request;
    private BundleResponse _response;
    private int pingCount = 0;
    private int errorCount = 0;
    private int pollCount = 0;

    public ValidateThread(BundleRequest request, BundleResponse response) {
      _request = request;
      _response = response;
    }

    @Override
    public void run() {
      String serverId = getInstanceId();
      _response.addStatusMessage("starting server...");
      try {
        serverId = _bundleServer.start(getInstanceId());
        boolean isAlive = _bundleServer.ping(serverId);
        while (isAlive == false && pingCount < MAX_PING_COUNT) {
          Thread.sleep(5 * 1000);
          pingCount++;
          isAlive = _bundleServer.ping(serverId);
        }
        if (!isAlive) {
          _log.error("server " + serverId + " failed to start");
          return;
        }

        String url = "/validate/remote/" + _request.getBundleDirectory() + "/"
            + _request.getBundleBuildName() + "/"
            + _request.getId() + "/create";
        
        String sessionId = _request.getSessionId();
        
        _response = makeRequest(serverId, url, null, BundleResponse.class, sessionId);
        
        if (_response != null && _response.getId() != null) {
          String id = _response.getId();
          // put response in map
          _validationMap.put(id, _response);

          // should this response look ok, query until it completes
          while (!isComplete(_response)) {
            url = "/validate/remote/" + id + "/list";

            _response = makeRequest(serverId, url, null, BundleResponse.class, sessionId);
            _validationMap.put(id, _response);
            pollCount++;
            Thread.sleep(5 * 1000);
          }
        }

        /*
         * Here either the server didn't start, or it crashed before we finished.
         * ABORT as there is nothing else we can do.
         */
        if (_response == null || _response.getId() == null) {
          _log.error("null response; assuming no response from server");
          _response = new BundleResponse(_request.getId());
          _response.setException(new RuntimeException("no response from server"));
          _validationMap.put(_request.getId(), _response);
        }
      } catch (Exception any) {
        _log.error(any.toString(), any);
        _response.setException(any);
      } catch (Throwable t) {
        RuntimeException re = new RuntimeException(t);
        _log.error(t.toString(), re);
        _response.setException(re);
      } finally {
        _response.setComplete(true);
        // allow machine to power down
        _log.info("validateThread exiting, shutting down server");
        _response.addStatusMessage("shutting down server");
        _bundleServer.stop(serverId);
        try {
          Thread.sleep(30 * 1000); // allow time for instance to power down
        } catch (InterruptedException ie) {
          return;
        }
      }
    }

    private boolean isComplete(BundleResponse response) {
      if (response == null && errorCount < MAX_ERRORS) {
        errorCount++;
        return false;
      } else if (response == null && errorCount > MAX_ERRORS) {
        _log.error("Received " + MAX_ERRORS + " errors, bailing");
        return true;
      } else if (response.isComplete()) {
        return true;
      } else if (pollCount > MAX_COUNT) {
        _log.error("Build timed-out, bailing");
        response.addStatusMessage("Bailing due to build timeout");
        return true;
      }
      errorCount = 0; // reset errorCount
      return false;
    }

  }

  private class BuildThread implements Runnable {
    private static final int MAX_COUNT = 4320; // 6 hours at 5 second increments
    private static final int MAX_PING_COUNT = 60; // 5 minutes
    private static final int MAX_ERRORS = 20;
    private BundleBuildRequest _request;
    private BundleBuildResponse _response;
    private int errorCount = 0;
    private int pollCount = 0;
    
    
    public BuildThread(BundleBuildRequest request, BundleBuildResponse response) {
      _request = request;
      _response = response;
    }

    @Override
    public void run() {
      String serverId = getInstanceId();
      _response.addStatusMessage("starting server " + serverId + "...");
      try {
        serverId = _bundleServer.start(getInstanceId());
        int pingCount = 0;
        boolean isAlive = _bundleServer.ping(serverId);
        while (isAlive == false && pingCount < MAX_PING_COUNT) {
          Thread.sleep(5 * 1000);
          pingCount++;
          isAlive = _bundleServer.ping(serverId);
        }
        if (!isAlive) {
          _log.error("server " + serverId + " failed to start");
          _response.addStatusMessage("Server failed to start.  Please try again");
          return;
        }
        
        String url = "/build/remote/create";
        Map <String, String> params = new HashMap<String, String>();
        params.put("bundleDirectory", _request.getBundleDirectory());
        params.put("bundleBuildName", _request.getBundleName());
        params.put("email", _request.getEmailAddress());
        params.put("id", _request.getId());
        params.put("bundleStartDate", _request.getBundleStartDateString());
        params.put("bundleEndDate", _request.getBundleEndDateString());
        params.put("bundleComment", _request.getBundleComment());
        params.put("archive", ""+_request.getArchiveFlag());
        params.put("consolidate", ""+_request.getConsolidateFlag());
        params.put("predate", ""+_request.getPredate());
        String sessionId = _request.getSessionId();
        _response = makeRequest(serverId, url, null, BundleBuildResponse.class, params, sessionId);
        if (_response != null && _response.getId() != null) {
          String id = _response.getId();
          // put response in map
          _response.setBundleResultLink(getResultLink(_request.getBundleName(), _request.getId(),
    			_request.getBundleStartDateString(), _request.getBundleEndDateString()));
          _buildMap.put(id, _response);
          if (_bundleService != null) {
            _bundleService.updateBundleBuildResponse(_response);
          }
          // should this response look ok, query until it completes
          while (!isComplete(_response)) {
            url = "/build/remote/" + id + "/list";
            _response = makeRequest(serverId, url, null, BundleBuildResponse.class, sessionId);
            if (_response != null) {
            	_response.setBundleResultLink(getResultLink(_request.getBundleName(), _request.getId(),
            			_request.getBundleStartDateString(), _request.getBundleEndDateString()));
              _buildMap.put(id, _response);
              if (_bundleService != null) {
                _bundleService.updateBundleBuildResponse(_response);
              }
            }
            pollCount++;
            Thread.sleep(5 * 1000);
          }
        }

          /*
           * either the server didn't start or crashed mid way; abort either way.
           */
        if (_response == null || _response.getId() == null) {
          _log.error("null response; assuming no response from server");
          _response = new BundleBuildResponse(_request.getId());
          _response.setException(new RuntimeException("no response from server"));
          _buildMap.put(_request.getId(), _response);
        }

        _response.addStatusMessage("version=" + _response.getVersionString());
        _response.addStatusMessage("complete");
      } catch (Exception any) {
        _log.error(any.toString(), any);
        _response.setException(any);
      } finally {
        try {
          _response.setComplete(true);
          _response.addStatusMessage("shutting down server");
          _log.info("buildThread exiting, shutting down server");
          _bundleServer.stop(serverId);
          if (_bundleService != null) {
            _bundleService.updateBundleBuildResponse(_response);
          }
          sendEmail(_request, _response);
          try {
            // allow machine to power down
            Thread.sleep(30 * 1000);
          } catch (InterruptedException ie) {
            return;
          }
        } catch (Throwable t) {
          // we don't add this to the response as it would hide existing exceptions
          _log.error("sendEmail failed", t);
        }
      }
    }
    
    private boolean isComplete(BundleBuildResponse response) {
      if (response == null && errorCount < MAX_ERRORS) {
        errorCount++;
        return false;
      } else if (response == null && errorCount > MAX_ERRORS) {
        _log.error("Received " + MAX_ERRORS + " errors, bailing");
        return true;
      } else if (response != null && response.isComplete()) {
        return true;
      } else if (pollCount > MAX_COUNT) {
        _log.error("Build timed-out, bailing");
        response.addStatusMessage("Bailing due to build timeout");
        return true;
      }
      errorCount = 0; // reset errorCount
      return false;
    }
  }

	/**
	 * @param configurationService the configurationService to set
	 */
  	@Autowired
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

}
