package org.onebusaway.transit_data_federation.services.realtime;

public class ScheduleDeviationSamples {

  private final double[] scheduleTimes;
  private final double[] scheduleDeviationMus;
  private final double[] scheduleDeviationSigmas;

  public ScheduleDeviationSamples(double[] scheduleTimes,
      double[] scheduleDeviationMus, double[] scheduleDeviationSigmas) {
    this.scheduleTimes = scheduleTimes;
    this.scheduleDeviationMus = scheduleDeviationMus;
    this.scheduleDeviationSigmas = scheduleDeviationSigmas;
  }

  public double[] getScheduleTimes() {
    return scheduleTimes;
  }

  public double[] getScheduleDeviationMus() {
    return scheduleDeviationMus;
  }

  public double[] getScheduleDeviationSigmas() {
    return scheduleDeviationSigmas;
  }

  public boolean isEmpty() {
    return scheduleTimes.length == 0;
  }
}
