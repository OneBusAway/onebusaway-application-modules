package org.onebusaway.gtfs.model;

public final class Transfer extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private int id;

  private Stop fromStop;

  private Stop toStop;

  private int transferType;

  private int minTransferTime = -1;

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public Stop getFromStop() {
    return fromStop;
  }

  public void setFromStop(Stop fromStop) {
    this.fromStop = fromStop;
  }

  public Stop getToStop() {
    return toStop;
  }

  public void setToStop(Stop toStop) {
    this.toStop = toStop;
  }

  public int getTransferType() {
    return transferType;
  }

  public void setTransferType(int transferType) {
    this.transferType = transferType;
  }

  public int getMinTransferTime() {
    return minTransferTime;
  }

  public void setMinTransferTime(int minTransferTime) {
    this.minTransferTime = minTransferTime;
  }

  public String toString() {
    return "<Transfer " + getId() + ">";
  }
}
