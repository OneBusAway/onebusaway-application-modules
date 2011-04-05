package org.onebusaway.transit_data_federation.impl.otp;

import java.util.Arrays;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public class TripSequence {

  public static final TripSequence EMPTY = new TripSequence(
      new BlockTripEntry[] {});

  private final BlockTripEntry[] sequence;

  public TripSequence(BlockTripEntry blockTrip) {
    sequence = new BlockTripEntry[] {blockTrip};
  }

  private TripSequence(BlockTripEntry[] newSequence) {
    sequence = newSequence;
  }

  public TripSequence extend(BlockTripEntry blockTrip) {
    if (sequence.length > 0 && sequence[sequence.length - 1] == blockTrip)
      return this;
    BlockTripEntry[] newSequence = new BlockTripEntry[sequence.length + 1];
    System.arraycopy(sequence, 0, newSequence, 0, sequence.length);
    newSequence[newSequence.length - 1] = blockTrip;
    return new TripSequence(newSequence);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(sequence);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TripSequence other = (TripSequence) obj;
    return Arrays.equals(this.sequence, other.sequence);
  }
}
