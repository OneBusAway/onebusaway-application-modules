/**
 * Copyright (C) 2022 Cayla Savitzky <caylasavitzky@gmail.com>
 * Copyright (C) 2022 Cambridge Systematics, Inc.
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

package org.onebusaway.api.model.transit;

public class ScheduleStopTimeInstanceExtendedWithStopIdV2Bean extends ScheduleStopTimeInstanceV2Bean{
    String stopId;


    public void setStopId(String stopId){
        this.stopId = stopId;
    }

    public String getStopId(){
        return stopId;
    }
}
