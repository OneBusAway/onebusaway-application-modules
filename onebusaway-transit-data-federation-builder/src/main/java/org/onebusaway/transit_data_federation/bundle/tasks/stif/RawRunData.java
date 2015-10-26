/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif;

public class RawRunData {

  private String runId;
  private String reliefRunId;
  private String nextOperatorRunId;
  private String block;
  private String depotCode;

  public RawRunData(String runId, String reliefRunId, String nextOperatorRunId, String block, String depotCode) {
    this.runId = runId;
    this.reliefRunId = reliefRunId;
    this.nextOperatorRunId = nextOperatorRunId;
    this.block = block;
    this.depotCode = depotCode;
  }

  public String getRunId() {
    return runId;
  }

  public void setRunId(String run) {
    this.runId = run;
  }

  public String getReliefRunId() {
    return reliefRunId;
  }

  public void setReliefRunId(String run) {
    this.reliefRunId = run;
  }

  public String getNextRun() {
    return nextOperatorRunId;
  }

  public String getBlock() {
    return block;
  }

  public void setBlock(String block) {
    this.block = block;
  }

  public String getDepotCode() {
    return depotCode;
  }

  public void setDepotCode(String depotCode) {
    this.depotCode = depotCode;
  }
}
