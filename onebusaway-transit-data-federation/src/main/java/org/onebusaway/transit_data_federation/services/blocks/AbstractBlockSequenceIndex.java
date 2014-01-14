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
package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.blocks.BlockSequence;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

public abstract class AbstractBlockSequenceIndex {

  protected final List<BlockSequence> _sequences;

  protected final ServiceIdActivation _serviceIds;

  protected final boolean _privateService;

  public AbstractBlockSequenceIndex(List<BlockSequence> sequences,
      boolean privateService) {
    if (sequences == null)
      throw new IllegalArgumentException("sequences is null");
    if (sequences.isEmpty())
      throw new IllegalArgumentException("sequences is empty");

    checkSequencesHaveSameServiceids(sequences);

    _sequences = sequences;
    _serviceIds = _sequences.get(0).getBlockConfig().getServiceIds();
    _privateService = privateService;
  }

  public List<BlockSequence> getSequences() {
    return _sequences;
  }

  public ServiceIdActivation getServiceIds() {
    return _serviceIds;
  }

  public boolean isPrivateService() {
    return _privateService;
  }

  public int size() {
    return _sequences.size();
  }

  private static void checkSequencesHaveSameServiceids(
      List<BlockSequence> blocks) {
    ServiceIdActivation expected = blocks.get(0).getBlockConfig().getServiceIds();
    for (int i = 1; i < blocks.size(); i++) {
      ServiceIdActivation actual = blocks.get(i).getBlockConfig().getServiceIds();
      if (!expected.equals(actual))
        throw new IllegalArgumentException("serviceIds mismatch: expected="
            + expected + " actual=" + actual);
    }
  }
}
