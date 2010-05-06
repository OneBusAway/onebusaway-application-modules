/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.web.oba.common.client;

import com.google.gwt.i18n.client.Messages;

import edu.washington.cs.rse.transit.web.oba.common.client.pages.StopByNumberPage;
import edu.washington.cs.rse.transit.web.oba.iphone.client.pages.CustomStopByNumberPage;
import edu.washington.cs.rse.transit.web.oba.iphone.client.pages.StopsByNumberPage;
import edu.washington.cs.rse.transit.web.oba.iphone.client.pages.StopsByRoutePage;
import edu.washington.cs.rse.transit.web.oba.standard.client.pages.IndexPage;

public interface OneBusAwayMessages extends Messages {

  public String go();

  /*****************************************************************************
   * {@link IndexPage} Messages
   ****************************************************************************/

  public String standardIndexPageWhereIsYourBus();

  public String standardIndexPageWelcome();

  public String standardIndexPageStopIdentification();

  public String standardIndexPageSearchForStops();

  public String standardIndexPageSearchByAddressExample();

  public String standardIndexPageSearchByRouteExample();

  public String standardIndexPageSearchByNumberExample();

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

  public String standardIndexPageRouteNumber(int routeNumber);

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
}
