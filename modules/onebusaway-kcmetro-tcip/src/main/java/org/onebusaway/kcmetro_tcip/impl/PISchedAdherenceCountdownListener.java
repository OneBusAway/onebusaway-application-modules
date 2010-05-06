package org.onebusaway.kcmetro_tcip.impl;

import org.onebusaway.tcip.model.PISchedAdherenceCountdown;

import java.util.List;

public interface PISchedAdherenceCountdownListener {
  public void handle(List<PISchedAdherenceCountdown> events);
}
