/**
 * Copyright (C) 2015 Cambridge Systematics
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
/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.impl.rest.handler;

import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.rest.handler.XStreamHandler;
import org.onebusaway.nextbus.impl.rest.xstream.BodyErrorConverter;
import org.onebusaway.nextbus.impl.rest.xstream.ScheduleHeaderConverter;
import org.onebusaway.nextbus.impl.rest.xstream.ScheduleStopConverter;
import org.onebusaway.nextbus.impl.rest.xstream.ScheduleTransitStopConverter;
import org.onebusaway.nextbus.model.nextbus.Agency;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.DisplayRoute;
import org.onebusaway.nextbus.model.nextbus.Message;
import org.onebusaway.nextbus.model.nextbus.Route;
import org.onebusaway.nextbus.model.transiTime.Prediction;
import org.onebusaway.nextbus.model.transiTime.PredictionsDirection;
import org.onebusaway.nextbus.model.transiTime.ScheduleRoute;
import org.onebusaway.nextbus.model.nextbus.Vehicle;
import org.onebusaway.nextbus.model.transiTime.Predictions;

import com.thoughtworks.xstream.XStream;


public class CustomXStreamHandler extends XStreamHandler {

  @Override
  protected XStream createXStream(ActionInvocation invocation) {
    return createXStream();
  }

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
    xstream.processAnnotations(PredictionsDirection.class);
    xstream.processAnnotations(Prediction.class);
    xstream.processAnnotations(ScheduleRoute.class);
    
    xstream.registerConverter(new BodyErrorConverter());
    //xstream.registerConverter(new ScheduleStopConverter());
    xstream.registerConverter(new ScheduleTransitStopConverter());
    xstream.registerConverter(new ScheduleHeaderConverter());
    
    return xstream;
  }
}
