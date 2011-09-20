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
package org.onebusaway.api.model.transit.blocks;

import org.onebusaway.api.model.transit.FrequencyV2Bean;

public class BlockInstanceV2Bean {

  private String blockId;

  private BlockConfigurationV2Bean blockConfiguration;

  private long serviceDate;

  private FrequencyV2Bean frequency;

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public BlockConfigurationV2Bean getBlockConfiguration() {
    return blockConfiguration;
  }

  public void setBlockConfiguration(BlockConfigurationV2Bean blockConfiguration) {
    this.blockConfiguration = blockConfiguration;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public FrequencyV2Bean getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyV2Bean frequency) {
    this.frequency = frequency;
  }
}
