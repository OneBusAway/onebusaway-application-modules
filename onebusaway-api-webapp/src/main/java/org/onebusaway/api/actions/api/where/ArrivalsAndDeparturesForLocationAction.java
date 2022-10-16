/**
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
package org.onebusaway.api.actions.api.where;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.impl.MaxCountSupport;
import org.onebusaway.api.impl.SearchBoundsFactory;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * inspired by ArrivalsAndDepartureForStop, but returns multiple stops
 * found within a given location/radius
 *
 */
public class ArrivalsAndDeparturesForLocationAction extends ApiActionSupport {

    private static final long serialVersionUID = 1L;
    // this api doesn't support v1
    private static final int V2 = 2;
    private static final double MAX_BOUNDS_RADIUS = 20000.0;

    @Autowired
    private TransitDataService _service;
    @Autowired
    private ConfigurationService _configService;
    private SearchBoundsFactory _searchBoundsFactory = new SearchBoundsFactory(MAX_BOUNDS_RADIUS);
    private MaxCountSupport _maxCount = new MaxCountSupport();

    private ArrivalsAndDeparturesQueryBean _query = new ArrivalsAndDeparturesQueryBean();
    private long _time = 0;

    public ArrivalsAndDeparturesForLocationAction() {
        super(V2);
    }

    public void setLat(double lat) {
        _searchBoundsFactory.setLat(lat);
    }

    public void setLon(double lon) {
        _searchBoundsFactory.setLon(lon);
    }

    public void setRadius(double radius) {
        _searchBoundsFactory.setRadius(radius);
    }

    public void setLatSpan(double latSpan) {
        _searchBoundsFactory.setLatSpan(latSpan);
    }

    public void setLonSpan(double lonSpan) {
        _searchBoundsFactory.setLonSpan(lonSpan);
    }
    public void setMinutesBefore(int minutesBefore) {
        _query.setMinutesBefore(minutesBefore);
    }

    public void setMinutesAfter(int minutesAfter) {
        _query.setMinutesAfter(minutesAfter);
    }

    public void setFrequencyMinutesBefore(int frequncyMinutesBefore) {
        _query.setFrequencyMinutesBefore(frequncyMinutesBefore);
    }

    public void setFrequencyMinutesAfter(int frequencyMinutesAfter) {
        _query.setFrequencyMinutesAfter(frequencyMinutesAfter);
    }

    @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
    public void setTime(Date time) {
        _time = time.getTime();
    }

    public DefaultHttpHeaders index() throws IOException, ServiceException {
        if (hasErrors())
            return setValidationErrorsResponse();

        CoordinateBounds bounds = _searchBoundsFactory.createBounds();
        int maxCount = _maxCount.getMaxCount();
        long time = SystemTime.currentTimeMillis();
        if (_time != 0)
            time = _time;
        BeanFactoryV2 factory = getBeanFactoryV2();
        SearchQueryBean searchQuery = new SearchQueryBean();
        searchQuery.setBounds(bounds);
        searchQuery.setMaxCount(maxCount);
        searchQuery.setType(SearchQueryBean.EQueryType.BOUNDS);

        ArrivalsAndDeparturesQueryBean adQuery = _query;
        StopsWithArrivalsAndDeparturesBean adResult = null;
        try {
            StopsBean stopResult = _service.getStops(searchQuery);
            if (stopResult == null) return setResourceNotFoundResponse();
            List<String> stopIds = new ArrayList<>();
            for (StopBean bean : stopResult.getStops()) {
                stopIds.add(bean.getId());
            }
            if (stopIds.isEmpty())
                return setResourceNotFoundResponse();
            adResult = _service.getStopsWithArrivalsAndDepartures(stopIds, adQuery);
        } catch (OutOfServiceAreaServiceException ex) {
            return setOkResponse(StopsWithArrivalsAndDeparturesBean.class);
        }
        if (adResult == null) {
            return setResourceNotFoundResponse();
        }
        return setOkResponse(factory.getResponse(adResult));
    }


}
