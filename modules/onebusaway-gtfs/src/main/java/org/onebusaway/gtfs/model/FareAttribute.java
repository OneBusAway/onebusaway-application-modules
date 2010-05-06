package org.onebusaway.gtfs.model;

public final class FareAttribute extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = 1L;

  private AgencyAndId id;

  private float price;

  private String currencyType;

  private int paymentMethod;

  private int transfers = -1;

  private int transferDuration = -1;

  @Override
  public AgencyAndId getId() {
    return id;
  }

  @Override
  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public float getPrice() {
    return price;
  }

  public void setPrice(float price) {
    this.price = price;
  }

  public String getCurrencyType() {
    return currencyType;
  }

  public void setCurrencyType(String currencyType) {
    this.currencyType = currencyType;
  }

  public int getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(int paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public int getTransfers() {
    return transfers;
  }

  public void setTransfers(int transfers) {
    this.transfers = transfers;
  }

  public int getTransferDuration() {
    return transferDuration;
  }

  public void setTransferDuration(int transferDuration) {
    this.transferDuration = transferDuration;
  }
  
  public String toString() {
    return "<FareAttribute " + getId() + ">";
  }
}
