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
package org.onebusaway.webapp.gwt.where_library.view.events;

import org.onebusaway.transit_data.model.StopBean;

import com.google.gwt.event.shared.GwtEvent;

public class StopClickedEvent extends GwtEvent<StopClickedHandler> {
  
  public static final Type<StopClickedHandler> TYPE = new Type<StopClickedHandler>();
  
  private StopBean _stop;


  public StopClickedEvent(StopBean stop) {
    _stop = stop;
  }
  
  public StopBean getStop() {
    return _stop;
  }
  
  @Override
  protected void dispatch(StopClickedHandler handler) {
    handler.handleStopClicked(this);
  }

  @Override
  public final com.google.gwt.event.shared.GwtEvent.Type<StopClickedHandler> getAssociatedType() {
    return TYPE;
  }
}
