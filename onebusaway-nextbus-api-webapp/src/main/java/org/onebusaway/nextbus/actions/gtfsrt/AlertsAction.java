package org.onebusaway.nextbus.actions.gtfsrt;

import com.google.transit.realtime.GtfsRealtime.*;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;
import com.google.transit.realtime.GtfsRealtimeConstants;
import com.opensymphony.xwork2.ModelDriven;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.actions.api.NextBusApiBase;
import org.onebusaway.nextbus.impl.gtfsrt.GtfsrtHelper;
import org.onebusaway.nextbus.util.HttpUtil;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.service_alerts.ServiceAlertLocalizedString;
import org.onebusaway.transit_data_federation.impl.service_alerts.ServiceAlertRecord;
import org.onebusaway.transit_data_federation.services.service_alerts.RssServiceAlertsService;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertsAction extends NextBusApiBase implements
		ModelDriven<FeedMessage> {

	private static Logger _log = LoggerFactory.getLogger(AlertsAction.class);

	@Autowired
	private HttpUtil _httpUtil;

	@Autowired
	private TransitDataService _transitDataService;

	private GtfsrtHelper _gtfsrtHelper = new GtfsrtHelper();

	private String agencyId;

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public DefaultHttpHeaders index() {
		return new DefaultHttpHeaders("success");
	}

	@Override
	public FeedMessage getModel() {

		FeedMessage.Builder feedMessage = createFeedWithDefaultHeader();

		List<String> agencyIds = new ArrayList<String>();

		if (agencyId != null) {
			agencyIds.add(agencyId);
		} else {
			Map<String,List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();
			agencyIds.addAll(agencies.keySet());
		}

		for(String agencyId : agencyIds) {
			ListBean<ServiceAlertBean> serviceAlertBeans = _transitDataService.getAllServiceAlertsForAgencyId(agencyId);

			for (ServiceAlertBean serviceAlert : serviceAlertBeans.getList()) {
				try {
					Alert.Builder alert = Alert.newBuilder();

					fillAlertHeader(alert, serviceAlert.getSummaries());
					fillAlertDescriptions(alert, serviceAlert.getDescriptions());
					fillActiveWindows(alert, serviceAlert.getActiveWindows());
					fillSituationAffects(alert, serviceAlert.getAllAffects());

					FeedEntity.Builder feedEntity = FeedEntity.newBuilder();
					feedEntity.setAlert(alert);
					feedEntity.setId(id(agencyId, serviceAlert.getId()));
					feedMessage.addEntity(feedEntity);
				}
				catch(Exception e){
					_log.error("Unable to process service alert", e);
				}
			}
		}

		return feedMessage.build();

	}

	private void fillAlertHeader(Alert.Builder alert, List<NaturalLanguageStringBean> summaries){
		if (summaries != null) {
			for (NaturalLanguageStringBean string : summaries) {
				TranslatedString translated = convertTranslatedString(string);
				alert.setHeaderText(translated);
			}
		}
	}

	private void fillAlertDescriptions(Alert.Builder alert, List<NaturalLanguageStringBean> descriptions){
		if (descriptions != null) {
			for (NaturalLanguageStringBean string : descriptions) {
				TranslatedString translated = convertTranslatedString(string);
				alert.setDescriptionText(translated);
			}
		}
	}

	private void fillActiveWindows(Alert.Builder alert, List<TimeRangeBean> activeWindows){
		if( activeWindows != null) {
			for (TimeRangeBean range : activeWindows) {
				com.google.transit.realtime.GtfsRealtime.TimeRange.Builder timeRange = com.google.transit.realtime.GtfsRealtime.TimeRange.newBuilder();
				// TODO - check to see what valid ranges are
				//if (range.getFrom() > 0)
					timeRange.setStart(range.getFrom());
				if (range.getTo() <= 0)
					timeRange.setEnd(Long.MAX_VALUE);
				alert.addActivePeriod(timeRange);
			}
		}
	}

	private void fillSituationAffects(Alert.Builder alert, List<SituationAffectsBean> affectsList){
		for (SituationAffectsBean affects : affectsList) {
			EntitySelector.Builder entitySelector = EntitySelector.newBuilder();
			if (affects.getAgencyId() != null)
				entitySelector.setAgencyId(affects.getAgencyId());
			if (affects.getRouteId() != null)
				entitySelector.setRouteId(id(agencyId, affects.getRouteId()));
			if (affects.getTripId() != null) {
				TripDescriptor.Builder trip = TripDescriptor.newBuilder();
				trip.setTripId(id(agencyId, affects.getTripId()));
				entitySelector.setTrip(trip);
			}
			if (affects.getStopId() != null)
				entitySelector.setStopId(id(agencyId, affects.getStopId()));
			alert.addInformedEntity(entitySelector);
		}
	}

	private TranslatedString convertTranslatedString(
			NaturalLanguageStringBean ts) {
		TranslatedString.Builder translated = TranslatedString.newBuilder();

		Translation.Builder builder = Translation.newBuilder();
		builder.setText(ts.getValue());
		if (ts.getLang() != null)
			builder.setLanguage(ts.getLang());
		translated.addTranslation(builder);
		return translated.build();
	}

	private FeedMessage.Builder createFeedWithDefaultHeader() {
		return _gtfsrtHelper.createFeedWithDefaultHeader();
	}

	private String id(String agencyId, String id) {
		return _gtfsrtHelper.id(agencyId, id);
	}
}