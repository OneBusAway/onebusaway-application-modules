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
import org.onebusaway.nextbus.model.Agency;
import org.onebusaway.nextbus.model.Body;
import org.onebusaway.nextbus.model.Route;
import org.onebusaway.nextbus.model.DisplayRoute;
import org.onebusaway.nextbus.model.ScheduleRoute;

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
    
    
    return xstream;
  }
}
