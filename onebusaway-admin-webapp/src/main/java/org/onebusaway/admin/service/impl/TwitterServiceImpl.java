/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.impl;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.presentation.impl.service_alerts.NotificationStrategy;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of twitter tweets.  Due to the complexity of oauth we delegate
 * the actual connections to twitter4j
 */
public class TwitterServiceImpl {

    private static Logger _log = LoggerFactory.getLogger(TwitterServiceImpl.class);

    private String _consumerKey;
    private String _consumerSecret;
    private String _accessToken;
    private String _accessTokenSecret;
    // handle to twitter4j api wrapper
    private Twitter _twitter;

    public void setConsumerKey(String key) {
        _consumerKey = key;
    }

    public void setConsumerSecret(String secret) {
        _consumerSecret = secret;
    }

    public void setAccessToken(String token) {
        _accessToken = token;
    }

    public void setAccessTokenSecret(String secret) {
        _accessTokenSecret = secret;
    }

    @PostConstruct
    private void setup() {

        // nothing to do if we are not configured properly
        if (StringUtils.isBlank(_consumerKey)
                || StringUtils.isBlank(_consumerSecret)
                || StringUtils.isBlank(_accessToken)
                || StringUtils.isBlank(_accessTokenSecret)
                ) {
            _log.warn("Twitter service missing necessary configuration, exiting");
            return;
        }

        try {
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
            configurationBuilder
                    .setOAuthConsumerKey(_consumerKey)
                    .setOAuthConsumerSecret(_consumerSecret)
                    .setOAuthAccessToken(_accessToken)
                    .setOAuthAccessTokenSecret(_accessTokenSecret);
            TwitterFactory twitterFactory = new TwitterFactory(configurationBuilder.build());
            _twitter = twitterFactory.getInstance();
        } catch (Exception any) {
            // don't let this failure prevent startup
            _log.error("Twitter integraton failed.  Check spring configuration");
            _log.error("Configuration exception:", any);
        }
    }

    public String updateStatus(String statusMessage) throws IOException {
        if (statusMessage == null) {
            _log.info("nothing to tweet!  Exiting");
            return null;
        }

        Map<String, String> params = new HashMap<>();
        _log.info("tweeting: " + statusMessage);
        params.put("status", statusMessage);


        if (_twitter == null) {
            throw new IOException("Invalid Configuration:  Missing consumer / access keys in spring configuration");
        }

        String response = null;
        try {
            Status status = _twitter.updateStatus(statusMessage);
            if (status != null) {
                response = "Successfully tweeted \""
                        + status.getText()
                        + "\" at "
                        + status.getCreatedAt();
            }
        } catch (TwitterException te) {
            _log.error(te.getExceptionCode() + ":" + ":" + te.getStatusCode() + te.getErrorMessage());
            throw new IOException(te);
        }
        return response;
    }

    /**
     * convert a service alert to a tweet. @See NotificationStrategy to change
     * behaviour.
     *
     */
    public static String toTweet(ServiceAlertBean bean, NotificationStrategy strategy) {
        if (bean == null) return null;
        if (bean.getSummaries() == null) return null;
        if (bean.getSummaries().isEmpty()) return null;

        List<String> routes = new ArrayList<>();
        List<String> stops = new ArrayList<>();
        boolean foundClause = false;
        // this is making a big assumption
        String tweet = bean.getSummaries().get(0).getValue();

        if (bean.getAllAffects() != null && !bean.getAllAffects().isEmpty()) {

            for (SituationAffectsBean allAffects : bean.getAllAffects()) {
                if (allAffects != null && StringUtils.isNotBlank(allAffects.getRouteId())) {
                    // agency id is optional -- if route_id has it already do not prepend
                    if (allAffects.getAgencyId() == null || "null".equals(allAffects.getAgencyId())) {
                        routes.add(strategy.summarizeRoute(allAffects.getRouteId()));
                        foundClause = true;
                    } else {
                        AgencyAndId routeId = new AgencyAndId(allAffects.getAgencyId(), allAffects.getRouteId());
                        routes.add(strategy.summarizeRoute(routeId.toString()));
                        foundClause = true;
                    }
                } else if (allAffects != null && StringUtils.isNotBlank(allAffects.getStopId())) {
                    AgencyAndId stopId = null;
                    try {
                        stopId = AgencyAndId.convertFromString(allAffects.getStopId());
                    } catch (IllegalStateException ise) {
                        stopId = new AgencyAndId(allAffects.getAgencyId(), allAffects.getStopId());
                    }

                    stops.add(strategy.summarizeStop(stopId.toString()));
                    foundClause = true;
                } else if (allAffects != null
                        && StringUtils.isBlank(allAffects.getRouteId())
                        && StringUtils.isBlank(allAffects.getStopId())
                        && StringUtils.isBlank(allAffects.getTripId())
                        && StringUtils.isNotBlank(allAffects.getAgencyId())) {
                    // agency wide service alert
                    foundClause = true;
                }
            }
        } else {
            // nothing to do -- no affects
            _log.info("tweet rejected -- no affects clauses");
            return null;
        }

        if (!foundClause) {
            _log.info("no route/stop specified, rejecting tweet");
            return null;
        }

        if (!routes.isEmpty()) {
            tweet += " affecting route(s) ";
            for (String r : routes) {
                tweet += r + ", ";
            }
            tweet = tweet.substring(0, tweet.length() -2);

            if (!stops.isEmpty()) {
                tweet += " and";
            }
        }

        if (!stops.isEmpty()) {
            if (routes.isEmpty()) {
                tweet += " affecting stop(s) ";
            } else {
                tweet += " stop(s) ";
            }
            for (String s : stops) {
                tweet += s + ", ";
            }
            tweet = tweet.substring(0, tweet.length() -2);
        }

        // now add description and assume user is watching length
        if (bean.getDescriptions() != null && ! bean.getDescriptions().isEmpty()) {
            tweet += " -- " + bean.getDescriptions().get(0).getValue();
        }

        return tweet;

    }

}
