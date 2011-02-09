package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.util.List;

public final class ListBean<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<T> list;

  private boolean limitExceeded = false;

  public ListBean() {

  }

  public ListBean(List<T> list, boolean limitExceeded) {
    this.list = list;
    this.limitExceeded = limitExceeded;
  }

  public List<T> getList() {
    return list;
  }

  public void setList(List<T> list) {
    this.list = list;
  }

  public boolean isLimitExceeded() {
    return limitExceeded;
  }

  public void setLimitExceeded(boolean limitExceeded) {
    this.limitExceeded = limitExceeded;
  }
}
