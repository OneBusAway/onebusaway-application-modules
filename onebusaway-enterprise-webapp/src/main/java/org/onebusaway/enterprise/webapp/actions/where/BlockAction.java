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

package org.onebusaway.enterprise.webapp.actions.where;

import java.util.Date;
import java.util.TimeZone;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.blocks.BlockConfigurationBean;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

public class BlockAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _service;

  private String _id;

  private Date _serviceDate;

  private BlockInstanceBean _blockInstance;

  private TimeZone _timeZone;

  public void setId(String id) {
    _id = id;
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateConverter")
  public void setServiceDate(Date serviceDate) {
    _serviceDate = serviceDate;
  }

  public BlockInstanceBean getBlockInstance() {
    return _blockInstance;
  }

  public TimeZone getTimeZone() {
    return _timeZone;
  }

  @Override
  @Actions({
      @Action(value = "/where/block"),
      @Action(value = "/where/iphone/block")})
  public String execute() throws ServiceException {

    if (_id == null)
      return INPUT;

    _blockInstance = _service.getBlockInstance(_id, _serviceDate.getTime());

    if (_blockInstance == null)
      return ERROR;

    BlockConfigurationBean blockConfig = _blockInstance.getBlockConfiguration();
    _timeZone = TimeZone.getTimeZone(blockConfig.getTimeZone());

    return SUCCESS;
  }
}
