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
package org.onebusaway.common.web.common.client.model;

public class RouteBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private String _id;

  private String _number;

  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public void setNumber(String number) {
    _number = number;
  }

  public String getNumber() {
    return _number;
  }

  @Override
  public int hashCode() {
    return _id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RouteBean))
      return false;
    RouteBean other = (RouteBean) obj;
    return _id.equals(other._id);
  }
}
