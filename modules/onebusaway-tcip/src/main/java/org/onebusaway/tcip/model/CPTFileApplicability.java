package org.onebusaway.tcip.model;

import java.util.List;

public class CPTFileApplicability {
  private List<SCHRouteIden> applicableRoutes;
  private List<CPTVehicleIden> applicablePtvs;
  private List<CPTStoppointIden> applicableStops;
  private List<CPTTransitFacilityIden> applicableFacilities;
  private List<CPTTransitFacilityIden> applicableGarages;
  private List<CPTFleetSubset> applicableFleetSubsets;
  private List<CPTStoppointSubset> applicableStoppointSubsets;
  private List<FCFareEquipmentSubset> applicableFareSubsets;

  public List<SCHRouteIden> getApplicableRoutes() {
    return applicableRoutes;
  }

  public void setApplicableRoutes(List<SCHRouteIden> applicableRoutes) {
    this.applicableRoutes = applicableRoutes;
  }

  public List<CPTVehicleIden> getApplicablePtvs() {
    return applicablePtvs;
  }

  public void setApplicablePtvs(List<CPTVehicleIden> applicablePtvs) {
    this.applicablePtvs = applicablePtvs;
  }

  public List<CPTStoppointIden> getApplicableStops() {
    return applicableStops;
  }

  public void setApplicableStops(List<CPTStoppointIden> applicableStops) {
    this.applicableStops = applicableStops;
  }

  public List<CPTTransitFacilityIden> getApplicableFacilities() {
    return applicableFacilities;
  }

  public void setApplicableFacilities(
      List<CPTTransitFacilityIden> applicableFacilities) {
    this.applicableFacilities = applicableFacilities;
  }

  public List<CPTTransitFacilityIden> getApplicableGarages() {
    return applicableGarages;
  }

  public void setApplicableGarages(
      List<CPTTransitFacilityIden> applicableGarages) {
    this.applicableGarages = applicableGarages;
  }

  public List<CPTFleetSubset> getApplicableFleetSubsets() {
    return applicableFleetSubsets;
  }

  public void setApplicableFleetSubsets(
      List<CPTFleetSubset> applicableFleetSubsets) {
    this.applicableFleetSubsets = applicableFleetSubsets;
  }

  public List<CPTStoppointSubset> getApplicableStoppointSubsets() {
    return applicableStoppointSubsets;
  }

  public void setApplicableStoppointSubsets(
      List<CPTStoppointSubset> applicableStoppointSubsets) {
    this.applicableStoppointSubsets = applicableStoppointSubsets;
  }

  public List<FCFareEquipmentSubset> getApplicableFareSubsets() {
    return applicableFareSubsets;
  }

  public void setApplicableFareSubsets(
      List<FCFareEquipmentSubset> applicableFareSubsets) {
    this.applicableFareSubsets = applicableFareSubsets;
  }
}
