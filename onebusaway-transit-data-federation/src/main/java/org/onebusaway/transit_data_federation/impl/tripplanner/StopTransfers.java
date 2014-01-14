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
package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;

public class StopTransfers {

  private final List<StopTransfer> transfersFromStop;

  private final List<StopTransfer> transfersToStop;

  public StopTransfers(List<StopTransfer> transfersFromStop,
      List<StopTransfer> transfersToStop) {
    this.transfersFromStop = transfersFromStop;
    this.transfersToStop = transfersToStop;
  }

  public List<StopTransfer> getTransfersFromStop() {
    return transfersFromStop;
  }

  public List<StopTransfer> getTransfersToStop() {
    return transfersToStop;
  }
}
