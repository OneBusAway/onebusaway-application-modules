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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NameBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private String type;

  private List<String> names = new ArrayList<String>();

  public NameBean() {

  }

  public NameBean(String type, String... names) {
    this.type = type;
    for (String name : names)
      this.names.add(name);
  }

  public NameBean(String type, List<String> names) {
    this.type = type;
    this.names.addAll(names);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return names.get(0);
  }
  
  public void setName(String name) {
    this.names = Arrays.asList(name);
  }

  public String getName(int index) {
    return names.get(index);
  }

  public List<String> getNames() {
    return names;
  }
  
  public void setNames(List<String> names) {
    this.names = names;
  }

  /***************************************************************************
   * {@link Object} Interface
   **************************************************************************/

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof NameBean))
      return false;
    NameBean bean = (NameBean) obj;
    return type.equals(bean.type) && names.equals(bean.names);
  }

  @Override
  public int hashCode() {
    return type.hashCode() + names.hashCode();
  }

  @Override
  public String toString() {
    return "name(type=" + type + " name=" + names + ")";
  }
}
