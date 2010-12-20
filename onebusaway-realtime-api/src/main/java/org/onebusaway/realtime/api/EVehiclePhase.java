package org.onebusaway.realtime.api;

import java.util.EnumSet;

public enum EVehiclePhase {

  /**
   * We're at a transit base
   */
  AT_BASE,

  /**
   * Non-revenue travel to the start of a block or trip
   */
  DEADHEAD_BEFORE,

  /**
   * A pause before a block starts
   */
  LAYOVER_BEFORE,

  /**
   * The vehicle is actively serving a block
   */
  IN_PROGRESS,

  /**
   * Non-revenue travel between trip segments of a block
   */
  DEADHEAD_DURING,

  /**
   * A pause before a block starts or between trip segments
   */
  LAYOVER_DURING,

  /**
   * Non-revenue from the end of a block back to the transit base
   */
  DEADHEAD_AFTER,

  /**
   * A pause after a vehicle has completed its block
   */
  @Deprecated
  LAYOVER_AFTER,

  /**
   * The vehicle is doing something unexpected
   */
  UNKNOWN;

  private static EnumSet<EVehiclePhase> _activeBeforeBlock = EnumSet.of(
      EVehiclePhase.AT_BASE, EVehiclePhase.DEADHEAD_BEFORE,
      EVehiclePhase.LAYOVER_BEFORE);

  private static EnumSet<EVehiclePhase> _activeDuringBlock = EnumSet.of(
      EVehiclePhase.IN_PROGRESS, EVehiclePhase.DEADHEAD_DURING,
      EVehiclePhase.LAYOVER_DURING);

  private static EnumSet<EVehiclePhase> _activeAfterBlock = EnumSet.of(
      EVehiclePhase.DEADHEAD_AFTER, EVehiclePhase.LAYOVER_AFTER);

  public static boolean isActiveBeforeBlock(EVehiclePhase phase) {
    return _activeBeforeBlock.contains(phase);
  }

  public static boolean isActiveDuringBlock(EVehiclePhase phase) {
    return _activeDuringBlock.contains(phase);
  }

  public static boolean isActiveAfterBlock(EVehiclePhase phase) {
    return _activeAfterBlock.contains(phase);
  }

  public static boolean isLayover(EVehiclePhase phase) {
    return phase == EVehiclePhase.LAYOVER_BEFORE
        || phase == EVehiclePhase.LAYOVER_DURING;
  }

  public String toLabel() {
    return toString().toLowerCase();
  }
}