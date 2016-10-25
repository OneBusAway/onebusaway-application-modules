/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.webapp.actions.admin.bundles;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.admin.model.ui.DataValidationMode;
import org.onebusaway.admin.model.ui.DataValidationRouteCounts;
import org.onebusaway.admin.model.ui.DataValidationStopCt;
import org.onebusaway.admin.service.DiffService;
import org.onebusaway.admin.service.FileService;
import org.onebusaway.admin.service.FixedRouteParserService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;

/**
 * Action class used by Transit Data Bundle Utility to compare two bundles.
 *
 */
@Namespace(value="/admin/bundles")
@Results({
  @Result(name="diffResult", type="json", 
  params={"root", "combinedDiffs"})
})
public class CompareBundlesAction extends OneBusAwayNYCAdminActionSupport {
  private static Logger _log = LoggerFactory.getLogger(CompareBundlesAction.class);
  private static final long serialVersionUID = 1L;

  private String datasetName;
  private String buildName;
  private String datasetName2;
  private String buildName2;
  private FileService fileService;
  private List<String> diffResult = new ArrayList<String>();
  private Map<String, List> combinedDiffs;
  private DiffService diffService;
  @Autowired
  private FixedRouteParserService _fixedRouteParserService;


  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  public void setBuildName(String buildName) {
    this.buildName = buildName;
  }

  public void setDatasetName2(String datasetName2) {
    this.datasetName2 = datasetName2;
  }

  public void setBuildName2(String buildName2) {
    this.buildName2 = buildName2;
  }

  @Autowired
  public void setFileService(FileService fileService) {
    this.fileService = fileService;
  }

  public Map<String, List> getCombinedDiffs() {
    return combinedDiffs;
  }

  @Autowired
  public void setDiffService(DiffService diffService) {
    this.diffService = diffService;
  }

  public String diffResult() {
    String gtfsStatsFile1 = fileService.getBucketName() + File.separator 
        + datasetName + "/builds/" + buildName + "/outputs/gtfs_stats.csv"; 
    String gtfsStatsFile2 = fileService.getBucketName()
        + File.separator
        + datasetName2 + "/builds/"
        + buildName2 + "/outputs/gtfs_stats.csv";
    diffResult.clear();
    diffResult = diffService.diff(gtfsStatsFile2, gtfsStatsFile1);

    // Added code to compare Fixed Route Date Validation reports from the
    // two specified bundles and builds
    List<DataValidationMode> fixedRouteDiffs = compareFixedRouteValidations(
        datasetName, buildName, datasetName2, buildName2);
    combinedDiffs = new HashMap<String, List>();
    combinedDiffs.put("diffResults", diffResult);
    combinedDiffs.put("fixedRouteDiffs", fixedRouteDiffs);
    return "diffResult";
  }

  private List<DataValidationMode> compareFixedRouteValidations(
      String datasetName, String buildName,
      String datasetName2, String buildName2) {
    String currentValidationReportPath = fileService.getBucketName()
        + File.separator  + datasetName + "/builds/" + buildName
        + "/outputs/fixed_route_validation.csv";
    File currentValidationReportFile = new File(currentValidationReportPath);
    String selectedValidationReportPath = fileService.getBucketName()
        + File.separator
        + datasetName2 + "/builds/"
        + buildName2 + "/outputs/fixed_route_validation.csv";
    File selectedValidationReportFile = new File(selectedValidationReportPath);

    // parse input files
    List<DataValidationMode> currentModes
      = _fixedRouteParserService.parseFixedRouteReportFile(currentValidationReportFile);
    List<DataValidationMode> selectedModes
      = _fixedRouteParserService.parseFixedRouteReportFile(selectedValidationReportFile);

    // compare and get diffs
    List<DataValidationMode> fixedRouteDiffs
      = findFixedRouteDiffs(currentModes, selectedModes);

    return  fixedRouteDiffs;
  }

  private List<DataValidationMode> findFixedRouteDiffs(
      List<DataValidationMode> currentModes,
      List<DataValidationMode> selectedModes) {

    List<DataValidationMode> fixedRouteDiffs = new ArrayList<>();
    if (currentModes != null && selectedModes == null) {
      return currentModes;
    }
    if (currentModes == null && selectedModes != null) {
      return selectedModes;
    }
    if (currentModes == null && selectedModes == null) {
      return fixedRouteDiffs;
    }

    for (DataValidationMode currentMode : currentModes) {
      // Check if this mode exists in selectedModes
      DataValidationMode diffMode = null;
      if (currentMode == null) continue;
      String modeName = currentMode.getModeName();
      for (DataValidationMode selectedMode : selectedModes) {
        if (modeName.equals(selectedMode.getModeName())) {
          selectedModes.remove(selectedMode);
          diffMode = compareModes(currentMode, selectedMode);
          break;
        }
      }
      if (diffMode == null && currentMode != null) {
        currentMode.setSrcCode("1");
        diffMode = currentMode;
      }
      if (diffMode.getRoutes().size() > 0) {
        fixedRouteDiffs.add(diffMode);
      }
    }
    if (selectedModes.size() > 0) {
      for (DataValidationMode selectedMode : selectedModes) {
        selectedMode.setSrcCode("2");
        fixedRouteDiffs.add(selectedMode);
      }
    }
    return fixedRouteDiffs;
  }

  private DataValidationMode compareModes(
      DataValidationMode currentMode, DataValidationMode selectedMode) {

    DataValidationMode diffMode = new DataValidationMode();
    diffMode.setModeName(currentMode.getModeName());
    diffMode.setRoutes(new ArrayList<DataValidationRouteCounts>());

    for (DataValidationRouteCounts currentRoute : currentMode.getRoutes()) {
      // Check if this route exists in selectedMode
      DataValidationRouteCounts diffRoute = null;
      String routeName = currentRoute.getRouteName();
      for (DataValidationRouteCounts selectedRoute : selectedMode.getRoutes()) {
        if (routeName.equals(selectedRoute.getRouteName())) {
          selectedMode.getRoutes().remove(selectedRoute);
          diffRoute = compareStops(currentRoute, selectedRoute);
          break;
        }
      }
      if (diffRoute == null) {
        currentRoute.setSrcCode("1");
        diffRoute = currentRoute;
      }
      if (diffRoute.getStopCounts().size() > 0) {
        diffMode.getRoutes().add(diffRoute);
      }
    }
    if (selectedMode.getRoutes().size() > 0) {
      for (DataValidationRouteCounts selectedRoute : selectedMode.getRoutes()) {
        selectedRoute.setSrcCode("2");
        diffMode.getRoutes().add(selectedRoute);
      }
    }
    return diffMode;
  }

  private DataValidationRouteCounts compareStops(
      DataValidationRouteCounts currentRoute,
      DataValidationRouteCounts selectedRoute) {

    DataValidationRouteCounts diffRoute = new DataValidationRouteCounts();
    diffRoute.setRouteName(currentRoute.getRouteName());
    diffRoute.setStopCounts(new ArrayList<DataValidationStopCt>());

    for (DataValidationStopCt currentStopCt : currentRoute.getStopCounts()) {
      boolean stopCtMatched = false;
      // Check if this stop exists in selectedRoute
      for (DataValidationStopCt selectedStopCt : selectedRoute.getStopCounts()) {
        if (currentStopCt.getStopCt() == selectedStopCt.getStopCt()) {
          stopCtMatched = true;
          selectedRoute.getStopCounts().remove(selectedStopCt);
          if ((currentStopCt.getTripCts()[0] != selectedStopCt.getTripCts()[0])
              || (currentStopCt.getTripCts()[1] != selectedStopCt.getTripCts()[1])
              || (currentStopCt.getTripCts()[2] != selectedStopCt.getTripCts()[2])){
            currentStopCt.setSrcCode("1");
            diffRoute.getStopCounts().add(currentStopCt);
            selectedStopCt.setSrcCode("2");
            diffRoute.getStopCounts().add(selectedStopCt);
          }
          break;
        }
      }
      if (stopCtMatched) {
        continue;
      } else {
        currentStopCt.setSrcCode("1");
        diffRoute.getStopCounts().add(currentStopCt);
      }
    }
    if (selectedRoute.getStopCounts().size() > 0) {
      for (DataValidationStopCt selectedStopCt : selectedRoute.getStopCounts()) {
        selectedStopCt.setSrcCode("2");
        diffRoute.getStopCounts().add(selectedStopCt);
      }
    }
    return diffRoute;
  }
}
