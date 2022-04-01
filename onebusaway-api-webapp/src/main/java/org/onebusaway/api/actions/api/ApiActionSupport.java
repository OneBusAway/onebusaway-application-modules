/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.api.actions.api;

import java.util.ArrayList;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.ResponseCodes;
import org.onebusaway.api.actions.OneBusAwayApiActionSupport;
import org.onebusaway.api.impl.MaxCountSupport;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.util.SystemTime;

import com.opensymphony.xwork2.ModelDriven;

public class ApiActionSupport extends OneBusAwayApiActionSupport implements
    ModelDriven<ResponseBean> {

  private static final long serialVersionUID = 1L;

  private static final int NO_VERSION = -999;

  private int _defaultVersion;

  protected ResponseBean _response;

  private int _version = -999;

  private String _key;

  private boolean _includeReferences = true;
  
  private Long time = null;
  
  public void setTime(long time) {
    this.time = time;
  }
  
  public long getTime() {
    if(time != null) {
      return time;
    } else {
      return SystemTime.currentTimeMillis();
    }
  }
  

  public ApiActionSupport(int defaultVersion) {
    _defaultVersion = defaultVersion;
  }

  public void setVersion(int version) {
    _version = version;
  }

  public void setKey(String key) {
    _key = key;
  }

  public void setIncludeReferences(boolean includeReferences) {
    _includeReferences = includeReferences;
  }

  public ResponseBean getModel() {
    return _response;
  }

  /****
   * Protected Methods
   * 
   * @param version
   * @return
   */

  protected boolean isVersion(int version) {
    if (_version == NO_VERSION)
      return version == _defaultVersion;
    else
      return version == _version;
  }

  protected BeanFactoryV2 getBeanFactoryV2() {
    BeanFactoryV2 factory = new BeanFactoryV2(_includeReferences);
    factory.setApplicationKey(_key);
    return factory;
  }

  protected BeanFactoryV2 getBeanFactoryV2(MaxCountSupport maxCount) {
    BeanFactoryV2 factory = getBeanFactoryV2();
    factory.setMaxCount(maxCount);
    return factory;
  }

  /*****************************************************************************
   * Response Bean Generation Methods
   ****************************************************************************/

  protected DefaultHttpHeaders setOkResponse(Object data) {
    _response = new ResponseBean(getReturnVersion(), ResponseCodes.RESPONSE_OK,
        "OK", data);
    return new DefaultHttpHeaders();
  }

  protected DefaultHttpHeaders setOkResponseText(String text) {
    _response = new ResponseBean(getReturnVersion(), ResponseCodes.RESPONSE_OK,
            "OK", text, true);
    return new DefaultHttpHeaders();
  }

  protected DefaultHttpHeaders setValidationErrorsResponse() {
    ValidationErrorBean bean = new ValidationErrorBean(new ArrayList<String>(
        getActionErrors()), getFieldErrors());
    _response = new ResponseBean(getReturnVersion(),
        ResponseCodes.RESPONSE_INVALID_ARGUMENT, "validation error", bean);
    return new DefaultHttpHeaders().withStatus(_response.getCode());
  }

  protected DefaultHttpHeaders setResourceNotFoundResponse() {
    _response = new ResponseBean(getReturnVersion(),
        ResponseCodes.RESPONSE_RESOURCE_NOT_FOUND, "resource not found", null);
    return new DefaultHttpHeaders().withStatus(_response.getCode());
  }

  protected DefaultHttpHeaders setExceptionResponse() {
    _response = new ResponseBean(getReturnVersion(),
        ResponseCodes.RESPONSE_SERVICE_EXCEPTION, "internal error", null);
    return new DefaultHttpHeaders().withStatus(_response.getCode());
  }

  protected DefaultHttpHeaders setUnknownVersionResponse() {
    _response = new ResponseBean(getReturnVersion(),
        ResponseCodes.RESPONSE_SERVICE_EXCEPTION, "unknown version: "
            + _version, null);
    return new DefaultHttpHeaders().withStatus(_response.getCode());
  }

  protected int getReturnVersion() {
    if (_version == NO_VERSION)
      return _defaultVersion;
    return _version;
  }

}
