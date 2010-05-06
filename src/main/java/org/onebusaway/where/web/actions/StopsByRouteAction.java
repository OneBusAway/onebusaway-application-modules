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
package org.onebusaway.where.web.actions;


import com.opensymphony.xwork2.ActionSupport;

import org.onebusaway.where.web.common.client.model.NameBean;
import org.onebusaway.where.web.common.client.model.NameTreeBean;
import org.onebusaway.where.web.common.client.model.StopBean;
import org.onebusaway.where.web.common.client.rpc.ServiceException;
import org.onebusaway.where.web.common.client.rpc.WhereService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class StopsByRouteAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private WhereService _service;

  private String _route;

  private List<Integer> _selection = new ArrayList<Integer>();

  private NameTreeBean _tree;

  private String _stopId;

  @Autowired
  public void setOneBusAwayService(WhereService service) {
    _service = service;
  }

  public void setRoute(String route) {
    _route = route;
  }

  public String getRoute() {
    return _route;
  }

  public void setSelection(String selection) {
    _selection.clear();
    for (String token : selection.split(","))
      _selection.add(Integer.parseInt(token));
  }

  public NameTreeBean getTree() {
    return _tree;
  }

  public String getStopId() {
    return _stopId;
  }

  @Override
  public String execute() throws ServiceException {

    NameTreeBean names = _service.getStopByRoute(_route, _selection);

    if (names.hasStop()) {
      StopBean stop = names.getStop();
      _stopId = stop.getId();
      return "stop";
    }

    _tree = names;
    return SUCCESS;
  }

  public String getNameAsString(NameBean name) {
    return name.getName();
  }

  public String extendSelection(int index) {
    StringBuilder b = new StringBuilder();
    for (int x : _selection) {
      if (b.length() > 0)
        b.append(',');
      b.append(x);
    }
    if (b.length() > 0)
      b.append(',');
    b.append(index);
    return b.toString();
  }
}
