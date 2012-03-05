/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.quickstart;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class GuiQuickstartDataModel {

  public enum EQuickStartType {
    BUILD_ONLY, RUN_ONLY, BUILD_AND_RUN
  }

  private PropertyChangeSupport changes = new PropertyChangeSupport(this);

  private String transitDataBundlePath;

  private EQuickStartType quickStartType = EQuickStartType.BUILD_AND_RUN;

  private String gtfsPath;

  private String tripUpdatesUrl;

  private String vehiclePositionsUrl;

  private String alertsUrl;

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    changes.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName,
      PropertyChangeListener listener) {
    changes.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    changes.removePropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(String propertyName,
      PropertyChangeListener listener) {
    changes.removePropertyChangeListener(propertyName, listener);
  }

  public String getTransitDataBundlePath() {
    return transitDataBundlePath;
  }

  public void setTransitDataBundlePath(String transitDataBundlePath) {
    String oldValue = this.transitDataBundlePath;
    this.transitDataBundlePath = transitDataBundlePath;
    changes.firePropertyChange("transitDataBundlePath", oldValue,
        transitDataBundlePath);
  }

  public EQuickStartType getQuickStartType() {
    return quickStartType;
  }

  public void setQuickStartType(EQuickStartType bootstrapType) {
    if (this.quickStartType == bootstrapType)
      return;
    EQuickStartType oldValue = this.quickStartType;
    this.quickStartType = bootstrapType;
    changes.firePropertyChange("quickstartType", oldValue, bootstrapType);
  }

  public boolean isBuildEnabled() {
    return quickStartType == EQuickStartType.BUILD_ONLY
        || quickStartType == EQuickStartType.BUILD_AND_RUN;
  }

  public boolean isRunEnabled() {
    return quickStartType == EQuickStartType.RUN_ONLY
        || quickStartType == EQuickStartType.BUILD_AND_RUN;
  }

  public boolean isBuildOnly() {
    return quickStartType == EQuickStartType.BUILD_ONLY;
  }

  public void setBuildOnly(boolean buildOnly) {
    if (buildOnly)
      setQuickStartType(EQuickStartType.BUILD_ONLY);
  }

  public boolean isRunOnly() {
    return quickStartType == EQuickStartType.RUN_ONLY;
  }

  public void setRunOnly(boolean runOnly) {
    if (runOnly)
      setQuickStartType(EQuickStartType.RUN_ONLY);
  }

  public boolean isBuildAndRun() {
    return quickStartType == EQuickStartType.BUILD_AND_RUN;
  }

  public void setBuildAndRun(boolean buildAndRun) {
    if (buildAndRun)
      setQuickStartType(EQuickStartType.BUILD_AND_RUN);
  }

  public String getGtfsPath() {
    return gtfsPath;
  }

  public void setGtfsPath(String gtfsPath) {
    if (equals(this.gtfsPath, gtfsPath))
      return;
    String oldValue = this.gtfsPath;
    this.gtfsPath = gtfsPath;
    changes.firePropertyChange("gtfsPath", oldValue, gtfsPath);
  }

  public String getTripUpdatesUrl() {
    return tripUpdatesUrl;
  }

  public void setTripUpdatesUrl(String tripUpdatesUrl) {
    if (equals(this.tripUpdatesUrl, tripUpdatesUrl))
      return;
    String oldValue = this.tripUpdatesUrl;
    this.tripUpdatesUrl = tripUpdatesUrl;
    changes.firePropertyChange("tripUpdatesUrl", oldValue, tripUpdatesUrl);
  }

  public String getVehiclePositionsUrl() {
    return vehiclePositionsUrl;
  }

  public void setVehiclePositionsUrl(String vehiclePositionsUrl) {
    if (equals(this.vehiclePositionsUrl, vehiclePositionsUrl))
      return;
    String oldValue = this.vehiclePositionsUrl;
    this.vehiclePositionsUrl = vehiclePositionsUrl;
    changes.firePropertyChange("vehiclePositionsUrl", oldValue,
        vehiclePositionsUrl);
  }

  public String getAlertsUrl() {
    return alertsUrl;
  }

  public void setAlertsUrl(String alertsUrl) {
    if (equals(this.alertsUrl, alertsUrl))
      return;
    String oldValue = this.alertsUrl;
    this.alertsUrl = alertsUrl;
    changes.firePropertyChange("alertsUrl", oldValue, alertsUrl);
  }

  private boolean equals(Object a, Object b) {
    boolean nullA = a == null;
    boolean nullB = b == null;
    if (nullA && nullB) {
      return true;
    } else if (nullA ^ nullB) {
      return false;
    } else {
      return a.equals(b);
    }
  }
}
