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
package org.onebusaway.transit_data.model;

public class ServicePatternBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private int _id;

  private int _changeDate;

  private String _generalDestination;

  private String _specificDestination;

  private boolean _express;

  public int getId() {
    return _id;
  }

  public void setId(int id) {
    _id = id;
  }

  public void setChangeDate(int id) {
    _changeDate = id;
  }

  public int getChangeDate() {
    return _changeDate;
  }

  public String getGeneralDestination() {
    return _generalDestination;
  }

  public void setGeneralDestination(String generalDestination) {
    _generalDestination = generalDestination;
  }

  public String getSpecificDestination() {
    return _specificDestination;
  }

  public void setSpecificDestination(String specificDestination) {
    _specificDestination = specificDestination;
  }

  public boolean isExpress() {
    return _express;
  }

  public void setExpress(boolean isExpress) {
    _express = isExpress;
  }
}
