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
