/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.webapp.gwt.where_library;

import com.google.gwt.i18n.client.Messages;

public interface WhereMessages extends Messages {

  public String go();

  /*****************************************************************************
   * {@link IndexPage} Messages
   ****************************************************************************/

  public String standardIndexPageWhereIsYourBus();

  public String standardIndexPageChangeDefaultSearchLocation();

  public String standardIndexPageSearchForStops();

  public String standardIndexPageSearchByAddressExample();

  public String standardIndexPageSearchByRouteExample();

  public String standardIndexPageSearchByNumberExample();
  
  public String standardIndexPageSearchByNumberExampleLink();

  public String standardIndexPageSearch();

  public String standardIndexPageAddressFound();

  public String standardIndexPageHideThisMarker();

  public String standardIndexPageDidYouMean();

  public String standardIndexPageFindNearbyStops();

  public String standardIndexPageSearchForStopsInThisArea();

  public String standardIndexPageTooManyResultsPre();

  public String standardIndexPageTooManyResultsLink();

  public String standardIndexPageTooManyResultsPost();

  public String standardIndexPageGetRealtimeArrivalInfo();

  public String standardIndexPageRoutes();

  public String standardIndexPageRouteNumber(String routeNumber);

  public String standardIndexPageSelectADestination();

  public String standardIndexPageInvalidLocationSpecified();

  public String standardIndexPageInvalidAreaSpecified();

  public String standardIndexPageInvalidRouteSpecified();

  /*****************************************************************************
   * {@link}
   ****************************************************************************/

  public String iphoneIndexPageCredits();

  /*****************************************************************************
   * 
   ****************************************************************************/

  public String iphoneStopPageSearchByStopNumber();

  public String iphoneStopPageSearchByRouteNumber();

  /*****************************************************************************
   * {@link StopsByNumberPage} Messages
   ****************************************************************************/

  public String stopsByNumberPageEnterYourStopNumber();

  public String stopsByNumberPageFindingYourStopNumber();

  /*****************************************************************************
   * {@link StopsByRoutePage} Messages
   ****************************************************************************/

  public String stopsByRoutePageEnterYourRouteNumber();

  public String stopsByRoutePageSelectYourDestination();

  public String stopsByRoutePageSelectStopsAt();

  public String stopsByRoutePageRegionAfter(String name);

  public String stopsByRoutePageRegionBefore(String name);

  public String stopsByRoutePageRegionBetween(String a, String b);

  public String stopsByRoutePageRegionUncertain();

  public String stopsByRoutePageStopDescription(String id, String direction);

  public String stopsByRoutePageRoutePrefix();

  public String stopsByRoutePageDestinationPrefix();

  public String stopsByRoutePageMainStreetPrefix();

  public String stopsByRoutePageCrossStreetPrefix();

  public String stopsByRoutePageInvalidRouteNumberSpecified();

  public String stopsByRoutePageInvalidSelectionSpecified();

  /*****************************************************************************
   * {@link StopIdentifcationPage} Messages
   ****************************************************************************/

  public String stopIdentificationPageTitle();

  public String stopIdentificationPageDescription();

  /*****************************************************************************
   * StopByNumberPage Messages
   ****************************************************************************/

  public String stopByNumberPageStopNumberShebang();

  public String stopByNumberPageRoute();

  public String stopByNumberPageDestination();

  public String stopByNumberPageMinutes();

  public String stopByNumberPageScheduledDeparture();

  public String stopByNumberPageScheduledArrival();

  public String stopByNumberPageDepartedOnTime();
  
  public String stopByNumberPageDepartedEarly(int minutes);
  
  public String stopByNumberPageDepartedLate(int minutes);

  public String stopByNumberPageOnTime();

  public String stopByNumberPageEarly(int minutes);

  public String stopByNumberPageDelayed(int minutes);

  public String stopByNumberPageBound();

  public String stopByNumberPageShowAllArrivals();

  public String stopByNumberPageKey();

  public String stopByNumberPageLastUpdate();

  public String stopByNumberPageWaitingForUpdate();

  public String stopByNumberPageRefresh();

  public String stopByNumberPageNearbyStops();

  public String stopByNumberPageNoStopNumberSpecified();

  public String stopByNumberPageInvalidStopNumberSpecified();

  public String stopByNumberPageInvalidRouteNumberSpecified();

  /*****************************************************************************
   * {@link CustomStopByNumberPage} Interface
   ****************************************************************************/

  public String standardCustomStopByNumberPageSearchForStops();

  /*****************************************************************************
   * 
   ****************************************************************************/

  public String commonInvalidURL(String token);

  public String commonInvalidPage(String token);

  public String commonNoSuchRoute();

  public String commonNoSuchStop();

  public String commonNoSuchServicePattern();

  public String commonInvalidSelection();
  

  /****
   * Arrivals and Departures Fields
   ****/
  
  public String arrivedEarly(int minutes);
  
  public String departedEarly(int minutes);

  public String early(int minutes);
  
  public String arrivedOnTime();
  
  public String departedOnTime();

  public String onTime();
  
  public String arrivedLate(int minutes);

  public String departedLate(int minutes);

  public String delayed(int minutes);

  public String scheduledArrival();
  
  public String scheduledDeparture();
}
