/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.model.assignments;

import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ActiveBlock {

    private String blockId;
    private BlockInstanceBean blockInstanceBean;
    private Set<String> routes;
    private String startTime;
    private String endTime;

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public BlockInstanceBean getBlockInstanceBean() {
        return blockInstanceBean;
    }

    public void setBlockInstanceBean(BlockInstanceBean blockInstanceBean) {
        this.blockInstanceBean = blockInstanceBean;
    }

    public Set<String> getRoutes() {
        if(routes == null){
            this.routes = new HashSet<>();
        }
        return routes;
    }

    public void setRoutes(Set<String> routes) {
        this.routes = routes;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActiveBlock that = (ActiveBlock) o;
        return blockId.equals(that.blockId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockId);
    }
}
