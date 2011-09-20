/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

  private static EnumSet<EVehiclePhase> _activeLayovers = EnumSet.of(
      EVehiclePhase.LAYOVER_BEFORE, EVehiclePhase.LAYOVER_DURING);

  private static EnumSet<EVehiclePhase> _layovers = EnumSet.of(
      EVehiclePhase.LAYOVER_BEFORE, EVehiclePhase.LAYOVER_DURING,
      EVehiclePhase.LAYOVER_AFTER);

  public static boolean isActiveBeforeBlock(EVehiclePhase phase) {
    return _activeBeforeBlock.contains(phase);
  }

  public static boolean isActiveDuringBlock(EVehiclePhase phase) {
    return _activeDuringBlock.contains(phase);
  }

  public static boolean isActiveAfterBlock(EVehiclePhase phase) {
    return _activeAfterBlock.contains(phase);
  }
  
  public static boolean isActiveLayover(EVehiclePhase phase) {
    return _activeLayovers.contains(phase);
  }

  public static boolean isLayover(EVehiclePhase phase) {
    return _layovers.contains(phase);
  }

  public String toLabel() {
    return toString().toLowerCase();
  }
}