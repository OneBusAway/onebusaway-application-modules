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

import com.opensymphony.xwork2.conversion.TypeConversionException;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.impl.MaxCountSupport;
import org.onebusaway.api.impl.SearchBoundsFactory;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;
import org.onebusaway.presentation.impl.conversion.DateTimeConverter;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.services.IntervalFactory;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * inspired by ArrivalsAndDepartureForStop, but returns multiple stops
 * found within a given location/radius
 *
 */
public class ArrivalsAndDeparturesForLocationAction extends ApiActionSupport {

    private static Logger _log = LoggerFactory.getLogger(ArrivalsAndDeparturesForLocationAction.class);

    private static final long serialVersionUID = 1L;
    // this api doesn't support v1
    private static final int V2 = 2;
    private static final double MAX_BOUNDS_RADIUS = 20000.0;
    private static boolean EMPTY_RETURNS_NOT_FOUND = false;

    @Autowired
    private TransitDataService _service;
    @Autowired
    private ConfigurationService _configService;

    @Autowired
    private RouteSorting customRouteSort;

    @Autowired
    private IntervalFactory _factory;

    @Autowired(required = false)
    public void setFilterChain(FilterChain filterChain) {
        _query.setSystemFilterChain(filterChain);
    }

    private SearchBoundsFactory _searchBoundsFactory = new SearchBoundsFactory(MAX_BOUNDS_RADIUS);
    private MaxCountSupport _maxCount = new MaxCountSupport(250, 1000);

    private ArrivalsAndDeparturesQueryBean _query = new ArrivalsAndDeparturesQueryBean();
    private long _time = 0;

    private boolean emptyReturnsNotFound = EMPTY_RETURNS_NOT_FOUND;

    private DateTimeConverter dateTimeConverter = new DateTimeConverter();

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

    // The DateTimeConvertor runs between index and show so can't be used here!
//    @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
    public void setTime(String timeStr) {
        _time = dateTimeConverter.parse(timeStr);
    }

    public void setEmptyReturnsNotFound(boolean flag) {
        emptyReturnsNotFound = flag;
    }

    /**
     * comma delimited list of GTFS route types
      */
    public void setRouteType(String routeType) {
        _query.setRouteType(routeType);
    }
    public void setMaxCount(int maxCount) {
        _maxCount.setMaxCount(maxCount);
    }

    public DefaultHttpHeaders index() throws IOException, ServiceException {
        if (hasErrors())
            return setValidationErrorsResponse();

        CoordinateBounds bounds = _searchBoundsFactory.createBounds();
        int maxCount = _maxCount.getMaxCount();
        if (_time == 0)
            _time = SystemTime.currentTimeMillis();
        BeanFactoryV2 factory = getBeanFactoryV2();
        SearchQueryBean searchQuery = new SearchQueryBean();
        searchQuery.setBounds(bounds);
        searchQuery.setMaxCount(maxCount);
        // new EQueryType that keeps results consistent if limitExceed is true
        // previous searches deliberately shuffled results
        searchQuery.setType(SearchQueryBean.EQueryType.ORDERED_BY_CLOSEST);
        searchQuery.setSystemFilterChain(_query.getSystemFilterChain());

        ArrivalsAndDeparturesQueryBean adQuery = _query;
        adQuery.setIncludeInputIdsInNearby(true); // include the queried ids in nearby
        adQuery.setMaxCount(maxCount); // limit nearby results appropriately
        adQuery.setBounds(searchQuery.getBounds());
        adQuery.setAgenciesExcludingScheduled(this.getAgenciesExcludingScheduled());
        StopsWithArrivalsAndDeparturesBean adResult = null;
        try {
            StopsBean stopResult = _service.getStops(searchQuery);
            if (stopResult == null) return emptyResponse();
            List<String> stopIds = new ArrayList<>();
            for (StopBean bean : stopResult.getStops()) {
                stopIds.add(bean.getId());
            }
            if (stopIds.isEmpty())
                return emptyResponse();
            AgencyServiceInterval serviceInterval = _factory.constructForDate(new Date(_time));
            adQuery.setServiceInterval(serviceInterval);
            adResult = _service.getStopsWithArrivalsAndDepartures(stopIds, adQuery, serviceInterval);
        } catch (OutOfServiceAreaServiceException ex) {
            return setOkResponse(new StopsWithArrivalsAndDeparturesBean());
        }
        if (adResult == null) {
            return emptyResponse();
        }
        factory.setCustomRouteSort(customRouteSort);
        try {
            return setOkResponse(factory.getResponse(adResult));
        } catch (Throwable t) {
            _log.error("BeanFactory Exception {}", t, t);
            return setExceptionResponse();
        }
    }

    private DefaultHttpHeaders emptyResponse() {
        if (emptyReturnsNotFound)
            setResourceNotFoundResponse();
        return setOkResponse(new StopsWithArrivalsAndDeparturesBean());
    }


}
