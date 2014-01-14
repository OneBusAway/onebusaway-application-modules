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
package org.onebusaway.presentation.tags;

import java.io.IOException;
import java.io.Writer;

import org.apache.struts2.components.ContextBean;
import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.trips.TripBean;

import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

public class RouteNameComponent extends ContextBean {

  private static final Logger LOG = LoggerFactory.getLogger(RouteNameComponent.class);

  private String _value;

  public RouteNameComponent(ValueStack stack) {
    super(stack);
  }

  public void setValue(String value) {
    _value = value;
  }

  @Override
  public boolean usesBody() {
    return false;
  }

  @Override
  public boolean end(Writer writer, String body) {

    if (_value == null)
      _value = "top";

    Object obj = findValue(_value);

    if( obj instanceof TripBean) {
      
      TripBean trip = (TripBean) obj;
      String routeShortName = trip.getRouteShortName();
      
      if( routeShortName != null) {
        try {
          writer.write(routeShortName);
        } catch (IOException e) {
          LOG.error("Could not write out Text tag", e);
        } 
      }
      else {
        obj = trip.getRoute();
      }
    }
    
    if (obj instanceof RouteBean) {
      RouteBean route = (RouteBean) obj;
      String name = RoutePresenter.getNameForRoute(route);
      try {
        writer.write(name);
      } catch (IOException e) {
        LOG.error("Could not write out Text tag", e);
      }
    }

    return super.end(writer, "");
  }
}
