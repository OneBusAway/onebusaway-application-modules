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
package org.onebusaway.transit_data_federation.services;

public class ArrivalAndDeparturePairQuery {

  private int resultCount = 1;

  private boolean applyRealTime = false;

  private int lookaheadTime = 0;

  private boolean includePrivateService = false;

  public int getResultCount() {
    return resultCount;
  }

  public void setResultCount(int resultCount) {
    this.resultCount = resultCount;
  }

  public boolean isApplyRealTime() {
    return applyRealTime;
  }

  public void setApplyRealTime(boolean applyRealTime) {
    this.applyRealTime = applyRealTime;
  }

  public int getLookaheadTime() {
    return lookaheadTime;
  }

  public void setLookaheadTime(int lookaheadTime) {
    this.lookaheadTime = lookaheadTime;
  }

  public boolean isIncludePrivateService() {
    return includePrivateService;
  }

  public void setIncludePrivateService(boolean includePrivateService) {
    this.includePrivateService = includePrivateService;
  }
}
