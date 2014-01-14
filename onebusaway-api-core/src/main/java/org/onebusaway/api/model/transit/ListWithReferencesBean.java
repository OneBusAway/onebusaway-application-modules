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
package org.onebusaway.api.model.transit;

import java.io.Serializable;
import java.util.List;

public class ListWithReferencesBean<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  private ReferencesBean references;

  private List<T> list;

  private boolean limitExceeded = false;

  public ListWithReferencesBean() {

  }

  public ListWithReferencesBean(List<T> list, boolean limitExceeded, ReferencesBean references) {
    this.list = list;
    this.limitExceeded = limitExceeded;
    this.references = references;
  }

  public ReferencesBean getReferences() {
    return references;
  }

  public void setReferences(ReferencesBean references) {
    this.references = references;
  }

  public List<T> getList() {
    return list;
  }

  public void setList(List<T> list) {
    this.list = list;
  }
  
  public void addElement(T element) {
    this.list.add(element);
  }

  public boolean isLimitExceeded() {
    return limitExceeded;
  }

  public void setLimitExceeded(boolean limitExceeded) {
    this.limitExceeded = limitExceeded;
  }
}
