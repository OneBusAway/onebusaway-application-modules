/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.web.actions.iphone;

import org.onebusaway.common.web.common.client.model.StopBean;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.where.web.common.client.model.NearbyRoutesBean;
import org.onebusaway.where.web.common.client.rpc.NoSuchStopServiceException;
import org.onebusaway.where.web.common.client.rpc.WhereService;

import com.opensymphony.xwork2.ActionSupport;

import org.springframework.beans.factory.annotation.Autowired;

public class StopInfoAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private WhereService _service;

  private String _id;

  private StopBean _stop;

  private NearbyRoutesBean _nearbyRoutes;

  public void setId(String id) {
    _id = id;
  }

  public StopBean getStop() {
    return _stop;
  }

  public NearbyRoutesBean getNearbyRoutes() {
    return _nearbyRoutes;
  }

  @Override
  public String execute() throws ServiceException {
    _stop = _service.getStop(_id);
    _nearbyRoutes = _service.getNearbyRoutes(_id, 5280 / 4);
    if (_stop == null)
      throw new NoSuchStopServiceException(_id);
    return SUCCESS;
  }
}
