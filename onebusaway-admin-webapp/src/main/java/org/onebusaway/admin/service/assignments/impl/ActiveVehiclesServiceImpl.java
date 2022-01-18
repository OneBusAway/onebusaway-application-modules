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
package org.onebusaway.admin.service.assignments.impl;

import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.admin.model.assignments.opengts.AccountRecord;
import org.onebusaway.admin.model.assignments.opengts.DeviceList;
import org.onebusaway.admin.service.assignments.ActiveVehiclesService;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ActiveVehiclesServiceImpl implements ActiveVehiclesService {

    private ScheduledExecutorService _executor;
    private Locale _locale = null;
    private List<String> _activeVehicles;
    private String _activeVehiclesUrl;
    private ObjectMapper mapper = new ObjectMapper();

    private ConfigurationService configurationService;
    private static Logger _log = LoggerFactory.getLogger(ActiveVehiclesServiceImpl.class);



    @PostConstruct
    public void start() throws Exception {
        if (_locale == null)
            _locale = Locale.getDefault();

        _activeVehicles = Collections.synchronizedList(new ArrayList<String>());
        _activeVehiclesUrl = configurationService.getConfigurationValueAsString("vehicleListUrl", null);

        _executor = Executors.newSingleThreadScheduledExecutor();

        // poll feed and build up cache
        _executor.scheduleAtFixedRate(new PollActiveVehicles(), 0, 60, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stop() throws IOException {
        if (_executor != null)
            _executor.shutdownNow();
    }

    public boolean isEnabled() {
        return StringUtils.isNotBlank(_activeVehiclesUrl);
    }

    @Override
    public List<String> getActiveVehicles(String query){
        List<String> results = new ArrayList<>();
        for(String activeVehicle : _activeVehicles){
            if(activeVehicle.trim().toLowerCase().contains(query.toLowerCase())){
                results.add(activeVehicle);
            }
        }
        return results;
    }

    @Override
    public List<String> getActiveVehicles(){
        return _activeVehicles;
    }

    private class PollActiveVehicles implements Runnable {

        @Override
        public void run() {
            if (!isEnabled()) {
                return;
            }
            try {
                URL url = new URL(_activeVehiclesUrl);
                AccountRecord accountRecord = mapper.readValue(url, AccountRecord.class);
                List<String> latestActiveVehicles = new ArrayList<>();
                for (DeviceList deviceList : accountRecord.getDeviceList()) {
                    latestActiveVehicles.add(deviceList.getDevice());
                }
                _activeVehicles = latestActiveVehicles;

            } catch (MalformedURLException mue) {
                _log.error("URL is formatted incorrectly. Please double check the provided URL " + _activeVehiclesUrl, mue);
            } catch (JsonMappingException jme) {
                _log.error("Unable to map the json to the AccountRecord Object", jme);
            } catch (JsonParseException jpe){
                _log.error("Unable to parse the list of vehicles from url " + _activeVehiclesUrl, jpe);
            } catch(IOException ioe){
                _log.error("Unable to get the list of vehicles from url " + _activeVehiclesUrl, ioe);
            } catch(Exception e){
                _log.error("Other uncaught exception", e);
            }
        }
    }

    @Autowired
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
