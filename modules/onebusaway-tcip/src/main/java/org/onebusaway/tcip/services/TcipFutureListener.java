package org.onebusaway.tcip.services;

public interface TcipFutureListener<T> {
  void operationCompleted(T operation);
}
