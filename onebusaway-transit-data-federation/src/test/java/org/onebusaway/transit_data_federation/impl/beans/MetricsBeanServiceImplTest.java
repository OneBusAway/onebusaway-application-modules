package org.onebusaway.transit_data_federation.impl.beans;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.MetricsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.bundle.RealtimeSourceServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MetricsBeanServiceImplTest {

    private MetricsBeanServiceImpl _service;
    private TransitDataService _transitDataService;
    private RealtimeSourceServiceImpl _realtimeSourceService;

    @Before
    public void setUp() {
        _service = new MetricsBeanServiceImpl();
        _transitDataService = Mockito.mock(TransitDataService.class);
        _realtimeSourceService = Mockito.mock(RealtimeSourceServiceImpl.class);
        _service.setTransitDataService(_transitDataService);
        _service.setRealtimeSourceServiceImpl(_realtimeSourceService);

        setupMockAgencies();
        setupMockTrips();
        setupMockRealtimeSources();
    }

    private void setupMockAgencies() {
        AgencyWithCoverageBean agency1 = new AgencyWithCoverageBean();
        agency1.setAgency(new AgencyBean());
        agency1.getAgency().setId("1");

        AgencyWithCoverageBean agency2 = new AgencyWithCoverageBean();
        agency2.setAgency(new AgencyBean());
        agency2.getAgency().setId("2");

        when(_transitDataService.getAgenciesWithCoverage()).thenReturn(Arrays.asList(agency1, agency2));
    }

    private void setupMockTrips() {
        TripDetailsBean trip1 = new TripDetailsBean();
        trip1.setTripId("1_trip1");
        TripDetailsBean trip2 = new TripDetailsBean();
        trip2.setTripId("1_trip2");
        TripDetailsBean trip3 = new TripDetailsBean();
        trip3.setTripId("2_trip1");

        ListBean<TripDetailsBean> trips = new ListBean<>();
        trips.setList(Arrays.asList(trip1, trip2, trip3));
        when(_transitDataService.getTripsForAgency(any(TripsForAgencyQueryBean.class))).thenReturn(trips);
    }

    private void setupMockRealtimeSources() {
        GtfsRealtimeSource source1 = createMockDataSource("feed1", "1", new HashSet<>(Arrays.asList("1_trip1")),
                new HashSet<>(Arrays.asList("1_trip3")), new HashSet<>(Arrays.asList("1_stop1")), new HashSet<>(Arrays.asList("1_stop2")), 100);

        GtfsRealtimeSource source2 = createMockDataSource("feed2", "2", new HashSet<>(Arrays.asList("2_trip1")),
                new HashSet<>(Arrays.asList("2_trip2")), new HashSet<>(Arrays.asList("2_stop1")), new HashSet<>(Arrays.asList("2_stop2")), 200);

        when(_realtimeSourceService.getSources()).thenReturn(Arrays.asList(source1, source2));
    }

    private GtfsRealtimeSource createMockDataSource(String feedId, String agencyId, Set<String> matchedTrips,
                                                    Set<String> unmatchedTrips, Set<String> matchedStops, Set<String> unmatchedStops, int totalRecords) {
        GtfsRealtimeSource source = Mockito.mock(GtfsRealtimeSource.class);
        MonitoredResult result = Mockito.mock(MonitoredResult.class);

        when(source.getFeedId()).thenReturn(feedId);
        when(source.getMonitoredResult()).thenReturn(result);

        when(result.getAgencyIds()).thenReturn(Arrays.asList(agencyId));
        when(result.getMatchedTripIds()).thenReturn(matchedTrips);
        when(result.getUnmatchedTripIds()).thenReturn(unmatchedTrips);
        when(result.getMatchedStopIds()).thenReturn(matchedStops);
        when(result.getUnmatchedStopIds()).thenReturn(unmatchedStops);
        when(result.getRecordsTotal()).thenReturn(totalRecords);

        return source;
    }

    @Test
    public void testGetAgencyFields() {
        MetricsBean result = _service.getMetrics();

        assertNotNull(result);
        assertEquals(2, result.getAgenciesWithCoverageCount());
        assertArrayEquals(new String[]{"1", "2"}, result.getAgencyIDs());
    }

    @Test
    public void testGetScheduledTrips() {
        MetricsBean result = _service.getMetrics();

        assertNotNull(result);
        assertEquals(2, result.getScheduledTripsCount().get("1").intValue());
        assertEquals(1, result.getScheduledTripsCount().get("2").intValue());
    }

    @Test
    public void testGetRealtimeTripCountsMatched() {
        MetricsBean result = _service.getMetrics();

        assertNotNull(result);
        assertEquals(1, result.getRealtimeTripCountsMatched().get("1").intValue());
        assertEquals(1, result.getRealtimeTripCountsMatched().get("2").intValue());
    }

    @Test
    public void testGetRealtimeTripCountsUnmatched() {
        MetricsBean result = _service.getMetrics();

        assertNotNull(result);
        assertEquals(1, result.getRealtimeTripCountsUnmatched().get("1").intValue());
        assertEquals(1, result.getRealtimeTripCountsUnmatched().get("2").intValue());
    }

    @Test
    public void testGetRealtimeTripIDsUnmatched() {
        MetricsBean result = _service.getMetrics();

        assertNotNull(result);
        assertEquals(1, result.getRealtimeTripIDsUnmatched().get("1").size());
        assertEquals(1, result.getRealtimeTripIDsUnmatched().get("2").size());
        assertEquals("1_trip3", result.getRealtimeTripIDsUnmatched().get("1").get(0));
        assertEquals("2_trip2", result.getRealtimeTripIDsUnmatched().get("2").get(0));
    }

    @Test
    public void testGetStopIDsMatchedCount() {
        MetricsBean result = _service.getMetrics();

        assertNotNull(result);
        assertEquals(1, result.getStopIDsMatchedCount().get("1").intValue());
        assertEquals(1, result.getStopIDsMatchedCount().get("2").intValue());
    }

    @Test
    public void testGetStopIDsUnmatchedCount() {
        MetricsBean result = _service.getMetrics();

        assertNotNull(result);
        assertEquals(1, result.getStopIDsUnmatchedCount().get("1").intValue());
        assertEquals(1, result.getStopIDsUnmatchedCount().get("2").intValue());
    }

    @Test
    public void testGetStopIDsUnmatched() {
        MetricsBean result = _service.getMetrics();

        assertNotNull(result);
        assertEquals(1, result.getStopIDsUnmatched().get("1").size());
        assertEquals(1, result.getStopIDsUnmatched().get("2").size());
        assertEquals("1_stop2", result.getStopIDsUnmatched().get("1").get(0));
        assertEquals("2_stop2", result.getStopIDsUnmatched().get("2").get(0));
    }

    @Test
    public void testGetTotalRecordsCounts() {
        MetricsBean result = _service.getMetrics();

        assertNotNull(result);
        assertEquals(100, result.getRealtimeRecordsTotal().get("1").intValue());
        assertEquals(200, result.getRealtimeRecordsTotal().get("2").intValue());
    }
}