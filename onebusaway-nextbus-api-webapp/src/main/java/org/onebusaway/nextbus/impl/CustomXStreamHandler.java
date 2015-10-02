/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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

package org.onebusaway.nextbus.impl;

import org.apache.struts2.rest.handler.XStreamHandler;
import org.onebusaway.nextbus.impl.xstream.BodyErrorConverter;
import org.onebusaway.nextbus.impl.xstream.DisplayStopConverter;
import org.onebusaway.nextbus.impl.xstream.ScheduleStopConverter;
import org.onebusaway.nextbus.model.nextbus.Agency;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.DisplayRoute;
import org.onebusaway.nextbus.model.nextbus.Message;
import org.onebusaway.nextbus.model.nextbus.Route;
import org.onebusaway.nextbus.model.nextbus.ScheduleRoute;
import org.onebusaway.nextbus.model.nextbus.Vehicle;
import org.onebusaway.nextbus.model.transiTime.Predictions;

import com.thoughtworks.xstream.XStream;


public class CustomXStreamHandler extends XStreamHandler {

  @Override
  protected XStream createXStream() {
    XStream xstream = super.createXStream();
    xstream.setMode(XStream.NO_REFERENCES);
    
    xstream.processAnnotations(Body.class);
    xstream.processAnnotations(DisplayRoute.class);
    xstream.processAnnotations(Route.class);
    xstream.processAnnotations(Agency.class);
    xstream.processAnnotations(ScheduleRoute.class);
    xstream.processAnnotations(Vehicle.class);
    xstream.processAnnotations(Message.class);
    xstream.processAnnotations(Predictions.class);
    
    xstream.registerConverter(new BodyErrorConverter());
    xstream.registerConverter(new DisplayStopConverter());
    xstream.registerConverter(new ScheduleStopConverter());
    
    return xstream;
  }
}
