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
