/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.aws.monitoring.model.metrics;


public enum MetricName {
	
    CPUUtilization("CPUUtilization"),
	  CurrentBundleCount("CurrentBundleCount"),
	  DatabaseConnections("DatabaseConnections"),
    DesktopUiValid("DesktopUiValid"),
    FirstValidBundleFilesCount("FirstValidBundleFilesCount"),
    FreeStorageSpace("FreeStorageSpace"),
    NegativeReplicaLag("NegativeReplicaLag"),
    NextBusApiErrorResponse("NextBusApiErrorResponse"),
    NumberOfMessagesDelayed("ApproximateNumberOfMessagesDelayed"),
    NumberOfMessagesDeleted("NumberOfMessagesDeleted"),
    NumberOfMessagesReceived("NumberOfMessagesReceived"),
    NumberOfMessagesSent("NumberOfMessagesSent"),
    ReadLatency("ReadLatency"),
    RealtimeInvalidLatLonPct("RealtimeInvalidLatLonDeltaPercentage"),
    RealtimeLocationsInvalid("RealtimeLocationsInvalid"),
    RealtimeLocationsTotal("RealtimeLocationsTotal"),
    RealtimeLocationsTotalPct("RealtimeLocationsTotalPercentage"),
    RealtimeStopsMatched("RealtimeStopsMatched"),
    RealtimeStopsMatchedPct("RealtimeStopsMatchedPercentage"),
    RealtimeStopsUnmatched("RealtimeStopsUnmatched"),
    RealtimeStopsUnmatchedPct("RealtimeStopsUnmatchedPercentage"),
    RealtimeTripsMatched("RealtimeTripsMatched"),
    RealtimeTripsMatchedAvg("RealtimeTripsMatchedAvg"),
    RealtimeTripsTotal("RealtimeTripsTotal"),
    RealtimeTripTotalPct("RealtimeTripTotalPercentage"),
    RealtimeTripsUnmatched("RealtimeTripsUnmatched"),
    RealtimeBusesInServicePct("RealtimeBusesInServicePercentage"),
    ReplicaLag("ReplicaLag"),
    ScheduleAgencyTotal("ScheduleAgencyTotal"),
    ScheduleExpiryDateDelta("ScheduleExpiryDateDelta"),
    ScheduleRealtimeDelta("ScheduleRealtimeDelta"),
    ScheduleTotalTrips("ScheduleTotalTrips"),
    SentMessageSize("SentMessageSize"),
    SMSWebappErrorResponse("SMSWebappErrorResponse"),
    SMSWebappResponseTime("SMSWebappResponseTime"),
    StopMonitoringErrorResponse("StopMonitoringErrorResponse"),
    StopMonitoringResponseTime("StopMonitoringResponseTime"), 
    TransitimeApiErrorResponse("TransitimeApiErrorResponse"),
    VehicleMonitoringErrorResponse("VehicleMonitoringErrorResponse"),
    VehicleMonitoringResponseTime("VehicleMonitoringResponseTime"),
    WriteLatency("WriteLatency"), 
    PredictionDeserializeQueueSize("PredictionDeserializeQueueSize"), 
    PredictionProcessingTime("PredictionProcessingTime"), 
    PredictionTotalLatency("PredictionTotalLatency"), 
    PredictionPredictablePercentage("PredictionPredictablePercentage"), 
    PredictionTotalQueueLatency("PredictionTotalQueueLatency");
    
    private String value;

    private MetricName(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Use this in place of valueOf.
     *
     * @param value
     *        real value
     * @return MetricName corresponding to the value
     */
    public static MetricName fromValue(String value) {
        if (value == null || "".equals(value)) {
            throw new IllegalArgumentException("Value cannot be null or empty!");
        } else if ("CurrentBundleCount".equals(value)) {
            return CurrentBundleCount;
        } else if ("DesktopUiValid".equals(value)) {
            return DesktopUiValid;
        } else if ("FirstValidBundleFilesCount".equals(value)) {
            return FirstValidBundleFilesCount;
        } else if ("NextBusApiErrorResponse".equals(value)) {
            return NextBusApiErrorResponse;
        } else if ("RealtimeInvalidLatLonDeltaPercentage".equals(value)) {
            return RealtimeInvalidLatLonPct;
        } else if ("RealtimeLocationsInvalid".equals(value)) {
            return RealtimeLocationsInvalid;
        } else if ("RealtimeLocationsTotal".equals(value)) {
            return RealtimeLocationsTotal;
        } else if ("RealtimeLocationsTotalPercentage".equals(value)) {
            return RealtimeLocationsTotalPct;
        } else if ("RealtimeStopsMatched".equals(value)) {
            return RealtimeStopsMatched;
        } else if ("RealtimeStopsMatchedPercentage".equals(value)) {
            return RealtimeStopsMatchedPct;
        } else if ("RealtimeStopsUnmatched".equals(value)) {
            return RealtimeStopsUnmatched;
        } else if ("RealtimeStopsUnmatchedPercentage".equals(value)) {
            return RealtimeStopsUnmatchedPct;
        } else if ("RealtimeTripsMatched".equals(value)) {
            return RealtimeTripsMatched;
        } else if ("RealtimeTripsMatchedAvg".equals(value)) {
            return RealtimeTripsMatchedAvg;
        } else if ("RealtimeTripsTotal".equals(value)) {
            return RealtimeTripsTotal;
        } else if ("RealtimeTripTotalPercentage".equals(value)) {
            return RealtimeTripTotalPct;
        } else if ("RealtimeTripsUnmatched".equals(value)) {
            return RealtimeTripsUnmatched;
        } else if ("SMSWebappErrorResponse".equals(value)) {
            return SMSWebappErrorResponse;
        } else if ("SMSWebappResponseTime".equals(value)) {
            return SMSWebappResponseTime;
        } else if ("ScheduleAgencyTotal".equals(value)) {
            return ScheduleAgencyTotal;
        } else if ("ScheduleExpiryDateDelta".equals(value)) {
            return ScheduleExpiryDateDelta;
        } else if ("ScheduleRealtimeDelta".equals(value)) {
            return ScheduleRealtimeDelta;
        } else if ("ScheduleTotalTrips".equals(value)) {
            return ScheduleTotalTrips;
        } else if ("StopMonitoringErrorResponse".equals(value)) {
            return StopMonitoringErrorResponse;
        } else if ("StopMonitoringResponseTime".equals(value)) {
            return StopMonitoringResponseTime;
        } else if ("TransitimeApiErrorResponse".equals(value)) {
            return TransitimeApiErrorResponse;
        } else if ("VehicleMonitoringErrorResponse".equals(value)) {
            return VehicleMonitoringErrorResponse;
        } else if ("VehicleMonitoringResponseTime".equals(value)) {
            return VehicleMonitoringResponseTime;
        } else if ("PredictionDeserializeQueueSize".equals(value)) {
          return PredictionDeserializeQueueSize;
        }  else if ("PredictionProcessingTime".equals(value)) {
            return PredictionProcessingTime;
        }  else if ("PredictionTotalLatency".equals(value)) {
          return PredictionTotalLatency;
        }  else if ("PredictionTotalQueueLatency".equals(value)) {
          return PredictionTotalQueueLatency;
        }  else if ("PredictionPredictablePercentage".equals(value)) {
          return PredictionPredictablePercentage;
        } else {
            throw new IllegalArgumentException("Cannot create enum from "
                    + value + " value!");
        }
    }
}
