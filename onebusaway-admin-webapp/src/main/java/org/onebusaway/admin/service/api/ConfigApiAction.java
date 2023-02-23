/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.util.impl.configuration.ConfigItem;
import org.onebusaway.util.impl.configuration.ConfigParameter;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/config")
public class ConfigApiAction {

    @Autowired
    ConfigurationService _configurationService;

    @GET
    @Path("/list")
    @Produces("application/json")
    public Response list(){
        try {
            Map<String, List<ConfigParameter>> parameters = _configurationService.getParametersFromLocalFile();

            Map<String, List<ConfigItem>> config = translate(parameters);
            ObjectMapper mapper = new ObjectMapper();
            String output = null;
            try {
                output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
            } catch (JsonProcessingException e) {
                _log.error("exception processing =" + e, e);
                return Response.serverError().build();
            }

            Response result = Response.ok(output).build();
            return result;
        } catch (Throwable t) {
            _log.error("list issue=", t, t);
            return Response.serverError().build();
        }
    }

    private Map<String, List<ConfigItem>> translate(Map<String, List<ConfigParameter>> parameters) {
        Map<String, List<ConfigItem>> map = new HashMap();
        List<ConfigItem> items = new ArrayList<>();
        if (parameters == null) return map;
        for (String agencyKey : parameters.keySet()) {
            for (ConfigParameter configParameter : parameters.get(agencyKey)) {
                ConfigItem item = new ConfigItem("agency_" + agencyKey, configParameter.getKey(), configParameter.getValue());
                items.add(item);
            }
        }
        map.put("config", items);
        return map;
    }

    private static Logger _log = LoggerFactory.getLogger(ConfigResource.class);
}
