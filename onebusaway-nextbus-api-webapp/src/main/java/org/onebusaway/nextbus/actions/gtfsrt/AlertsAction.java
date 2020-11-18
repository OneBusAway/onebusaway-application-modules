/**
 * Copyright (C) 2017 Cambridge Systematics
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
package org.onebusaway.nextbus.actions.gtfsrt;

import com.google.transit.realtime.GtfsRealtime.*;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;
import com.opensymphony.xwork2.ModelDriven;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.nextbus.actions.api.NextBusApiBase;
import org.onebusaway.nextbus.impl.gtfsrt.GtfsrtCache;
import org.onebusaway.nextbus.impl.gtfsrt.GtfsrtHelper;
import org.onebusaway.nextbus.util.HttpUtil;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.onebusaway.nextbus.impl.gtfsrt.GtfsrtCache.ALL_AGENCIES;

public class AlertsAction extends NextBusApiBase implements
		ModelDriven<FeedMessage> {

	private static Logger _log = LoggerFactory.getLogger(AlertsAction.class);

	@Autowired
	private HttpUtil _httpUtil;

	@Autowired
	private TransitDataService _transitDataService;

	@Autowired
	private GtfsrtCache _cache;

	private GtfsrtHelper _gtfsrtHelper = new GtfsrtHelper();

	private String agencyId;

	public String getAgencyId() {
		return agencyId;
	}

	public String getAgencyIdHashKey() {
		if (StringUtils.isBlank(agencyId)) {
			return ALL_AGENCIES;
		}
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
		FeedMessage cachedAlerts = _cache.getAlerts(getAgencyIdHashKey());
		if(cachedAlerts != null){
			return cachedAlerts;
		}
		else {

			FeedMessage.Builder feedMessage = createFeedWithDefaultHeader(null);

			List<String> agencyIds = new ArrayList<String>();

			if (agencyId != null) {
				agencyIds.add(agencyId);
			} else {
				Map<String, List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();
				agencyIds.addAll(agencies.keySet());
			}

			for (String agencyId : agencyIds) {
				ListBean<ServiceAlertBean> serviceAlertBeans = _transitDataService.getAllServiceAlertsForAgencyId(agencyId);

				for (ServiceAlertBean serviceAlert : serviceAlertBeans.getList()) {
					try {

						if (matchesFilter(serviceAlert)) {
							Alert.Builder alert = Alert.newBuilder();

							fillAlertHeader(alert, serviceAlert.getSummaries());
							// description is no longer populated
							fillAlertDescriptions(alert, serviceAlert.getDescriptions());
							if (!alert.hasDescriptionText()) {
								_log.info("copying header text of " + alert.getHeaderText().getTranslation(0));
								alert.setDescriptionText(alert.getHeaderText());
							}
							fillActiveWindows(alert, serviceAlert.getActiveWindows());
							fillSituationAffects(alert, serviceAlert.getAllAffects());

							FeedEntity.Builder feedEntity = FeedEntity.newBuilder();
							feedEntity.setAlert(alert);
							feedEntity.setId(id(agencyId, serviceAlert.getId()));
							feedMessage.addEntity(feedEntity);
						}
					} catch (Exception e) {
						_log.error("Unable to process service alert", e);
					}
				}
			}

			FeedMessage builtFeedMessage = feedMessage.build();
			_cache.putAlerts(getAgencyIdHashKey(), builtFeedMessage);

			return builtFeedMessage;
		}

	}

	private boolean matchesFilter(ServiceAlertBean bean) {
		if (_cache.getAlertFilter() == null || bean.getSource() == null) {
			return true;
		}

		boolean match = bean.getSource().matches(_cache.getAlertFilter());
		return match;
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
			if (affects.getRouteId() != null) {
				entitySelector.setRouteId(sanitize(affects.getRouteId()));
			}
			if (affects.getTripId() != null) {
				TripDescriptor.Builder trip = TripDescriptor.newBuilder();
				trip.setTripId(sanitize(affects.getTripId()));
				entitySelector.setTrip(trip);
			}
			if (affects.getStopId() != null) {
				entitySelector.setStopId(sanitize(affects.getStopId()));
			}
			alert.addInformedEntity(entitySelector);
		}
	}

	private String sanitize(String s) {
		if (s == null) return s;
		int pos = s.indexOf('_');
		if (pos > 0 && pos+1 < s.length())
			return s.substring(pos+1, s.length());
		return s;
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

	private FeedMessage.Builder createFeedWithDefaultHeader(Long timestampInSeconds) {
		return _gtfsrtHelper.createFeedWithDefaultHeader(timestampInSeconds);
	}

	private String id(String agencyId, String id) {
		if (agencyId == null) return id;
		return _gtfsrtHelper.id(agencyId, id);
	}
}