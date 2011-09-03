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
package org.onebusaway.webapp.gwt.viewkit;

import java.util.List;
import java.util.Map;

public class ListViewModel implements ContextAware {

  private ListViewController _listViewController;

  private boolean _willRespondToRowClicks = false;

  public void setListViewController(ListViewController listViewController) {
    _listViewController = listViewController;
  }
  
  public ListViewController getListViewController() {
    return _listViewController;
  }

  public void willReload() {
    
  }

  public void didReload() {

  }

  public int getNumberOfSections() {
    return 0;
  }

  public int getNumberOfRowsInSection(int sectionIndex) {
    return 0;
  }

  public ListViewRow getListViewRowForSectionAndRow(int sectionIndex,
      int rowIndex) {
    return null;
  }

  public boolean willRespondToRowClicks() {
    return _willRespondToRowClicks;
  }

  public void onRowClick(ListViewController listViewController,
      int sectionIndex, int rowIndex) {

  }
  
  @Override
  public void handleContext(List<String> path, Map<String, String> context) {
    
  }

  @Override
  public void retrieveContext(List<String> path, Map<String, String> context) {
    
  }

  protected void setWillRespondToRowClicks(boolean willRespondToRowClicks) {
    _willRespondToRowClicks = willRespondToRowClicks;
  }
}
