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
package org.onebusaway.aws.monitoring.impl.alarms;

import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.service.alarms.PredictionAlarms;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

@Component
public class PredictionAlarmsImpl extends AlarmsTemplate implements PredictionAlarms {
  @Override
  public void createDeserializeQueueSizeAlarm() {
    PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.PredictionDeserializeQueueSize);
    putMetricAlarmRequest.setAlarmActions(getCriticalAction());
    putMetricAlarmRequest
        .setInsufficientDataActions(getNonCriticalAction());
    putMetricAlarmRequest.setUnit(StandardUnit.Count);
    putMetricAlarmRequest
        .setComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold);
    putMetricAlarmRequest.setThreshold(_configService
        .getConfigurationValueAsDouble("alarm.predictionDeserializeQueueSize", 1000d));
    _cloudWatchService.publishAlarm(putMetricAlarmRequest);

  }

  @Override
  public void createProcessingTimeAlarm() {
    PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.PredictionProcessingTime);
    putMetricAlarmRequest.setAlarmActions(getCriticalAction());
    putMetricAlarmRequest
        .setInsufficientDataActions(getNonCriticalAction());
    putMetricAlarmRequest.setUnit(StandardUnit.Count);
    putMetricAlarmRequest
        .setComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold);
    putMetricAlarmRequest.setThreshold(_configService
        .getConfigurationValueAsDouble("alarm.predictionProcessingTime", 100d));
    _cloudWatchService.publishAlarm(putMetricAlarmRequest);
    
  }

  @Override
  public void createPredictionTotalLatencyAlarm() {
    PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.PredictionTotalLatency);
    putMetricAlarmRequest.setAlarmActions(getCriticalAction());
    putMetricAlarmRequest
        .setInsufficientDataActions(getNonCriticalAction());
    putMetricAlarmRequest.setUnit(StandardUnit.Count);
    putMetricAlarmRequest
        .setComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold);
    putMetricAlarmRequest.setThreshold(_configService
        .getConfigurationValueAsDouble("alarm.predictionTotalLatency", 30000d));
    _cloudWatchService.publishAlarm(putMetricAlarmRequest);
    
  }

  @Override
  public void createPredictionTotalQueueLatencyAlarm() {
    PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.PredictionTotalQueueLatency);
    putMetricAlarmRequest.setAlarmActions(getCriticalAction());
    putMetricAlarmRequest
        .setInsufficientDataActions(getNonCriticalAction());
    putMetricAlarmRequest.setUnit(StandardUnit.Count);
    putMetricAlarmRequest
        .setComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold);
    putMetricAlarmRequest.setThreshold(_configService
        .getConfigurationValueAsDouble("alarm.predictionTotalQueueLatency", 30000d));
    _cloudWatchService.publishAlarm(putMetricAlarmRequest);
    
  }

  
  @Override
  public void createPredictablePercentageAlarm() {
    PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.PredictionPredictablePercentage);
    putMetricAlarmRequest.setAlarmActions(getCriticalAction());
    putMetricAlarmRequest
        .setInsufficientDataActions(getNonCriticalAction());
    putMetricAlarmRequest.setUnit(StandardUnit.Count);
    putMetricAlarmRequest
        .setComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold);
    putMetricAlarmRequest.setThreshold(_configService
        .getConfigurationValueAsDouble("alarm.predictionProcessingTime", 0.30d));
    _cloudWatchService.publishAlarm(putMetricAlarmRequest);
    
  }

}
