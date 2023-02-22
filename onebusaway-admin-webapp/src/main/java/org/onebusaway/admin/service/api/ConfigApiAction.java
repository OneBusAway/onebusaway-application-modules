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

import org.json.JSONObject;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/config")
public class ConfigApiAction {

    @Autowired
    ConfigurationService _configurationService;

    @GET
    @Produces("application/json")
    public Response list(){
        Map<String, String> config = _configurationService.getConfigFromLocalFile();
        JSONObject json = new JSONObject(config);
        String output = json.toString();

        Response result = Response.ok(output).build();
        return result;
    }

    private static Logger _log = LoggerFactory.getLogger(ConfigResource.class);
}
