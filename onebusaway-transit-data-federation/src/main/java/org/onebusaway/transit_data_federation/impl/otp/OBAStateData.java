package org.onebusaway.transit_data_federation.impl.otp;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
import org.opentripplanner.routing.core.StateFactory;

public class OBAStateData extends StateData {

  public static final StateFactory STATE_FACTORY = new StateFactoryImpl();

  private final int maxBlockSequence;

  private final long initialWaitTime;

  private final TripSequence tripSequence;
  
  private final boolean lookaheadItinerary;

  public OBAStateData() {
    this(new OBAEditor());
  }

  protected OBAStateData(OBAEditor editor) {
    super(editor);
    this.maxBlockSequence = editor.maxBlockSequence;
    this.initialWaitTime = editor.initialWaitTime;
    this.tripSequence = editor.tripSequence;
    this.lookaheadItinerary = editor.lookaheadItinerary;
  }

  public int getMaxBlockSequence() {
    return maxBlockSequence;
  }

  public long getInitialWaitTime() {
    return initialWaitTime;
  }

  public TripSequence getTripSequence() {
    return tripSequence;
  }
  
  public boolean isLookaheadItinerary() {
    return lookaheadItinerary;
  }

  @Override
  public Editor edit() {
    return new OBAEditor(this);
  }

  public static class OBAEditor extends StateData.Editor {

    private int maxBlockSequence = -1;

    private long initialWaitTime = 0;

    private TripSequence tripSequence = TripSequence.EMPTY;
    
    private boolean lookaheadItinerary = false;

    protected OBAEditor() {

    }

    protected OBAEditor(OBAStateData state) {
      super(state);
      this.maxBlockSequence = state.maxBlockSequence;
      this.initialWaitTime = state.initialWaitTime;
      this.tripSequence = state.tripSequence;
      this.lookaheadItinerary = state.lookaheadItinerary;
    }

    @Override
    public StateData createData() {
      return new OBAStateData(this);
    }

    public void setMaxBlockSequence(int maxBlockSequence) {
      this.maxBlockSequence = maxBlockSequence;
    }

    public void incrementInitialWaitTime(long time) {
      this.initialWaitTime += time;
    }

    public void appendTripSequence(Object blockTrip) {
      if (tripSequence == null)
        tripSequence = new TripSequence(blockTrip);
      else
        tripSequence = tripSequence.extend(blockTrip);
    }
    
    public void setLookaheadItinerary() {
      this.lookaheadItinerary = true;
    }
  }

  private static class StateFactoryImpl implements StateFactory {

    @Override
    public State createState(long time) {
      return new State(time, new OBAStateData());
    }
  }
}
