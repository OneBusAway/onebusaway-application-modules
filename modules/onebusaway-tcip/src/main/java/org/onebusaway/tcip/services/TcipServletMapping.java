package org.onebusaway.tcip.services;

public class TcipServletMapping {

  private Class<?> messageType;

  private TcipServlet servlet;

  public TcipServletMapping() {

  }

  public TcipServletMapping(Class<?> messageType, TcipServlet servlet) {
    this.messageType = messageType;
    this.servlet = servlet;
  }

  public Class<?> getMessageType() {
    return messageType;
  }

  public void setMessageType(Class<?> messageType) {
    this.messageType = messageType;
  }

  public TcipServlet getServlet() {
    return servlet;
  }

  public void setServlet(TcipServlet servlet) {
    this.servlet = servlet;
  }
}
