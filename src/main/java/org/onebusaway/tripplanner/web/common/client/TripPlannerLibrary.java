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
package org.onebusaway.tripplanner.web.common.client;

import org.onebusaway.common.web.common.client.CommonLibrary;
import org.onebusaway.tripplanner.web.common.client.resources.TripPlannerResources;
import org.onebusaway.tripplanner.web.common.client.rpc.TripPlannerWebServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.libideas.client.StyleInjector;

public class TripPlannerLibrary implements EntryPoint {

  public void onModuleLoad() {
    CommonLibrary.registerService(TripPlannerWebServiceAsync.SERVICE_PATH, TripPlannerWebServiceAsync.SERVICE);
    StyleInjector.injectStylesheet(TripPlannerResources.INSTANCE.getCSS().getText());
  }
}
