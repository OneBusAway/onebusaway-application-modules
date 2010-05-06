package org.onebusaway.transit_data_federation.model;

public class TimepointPredictionSummary {

  private int numberOfPredictions;

  private int goalDeviation;

  public int getNumberOfPredictions() {
    return numberOfPredictions;
  }

  public void setNumberOfPredictions(int numberOfPredictions) {
    this.numberOfPredictions = numberOfPredictions;
  }

  public int getGoalDeviation() {
    return goalDeviation;
  }

  public void setGoalDeviation(int goalDeviation) {
    this.goalDeviation = goalDeviation;
  }

}
