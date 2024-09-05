## The OneBusAway Application Suite [![Java CI with Maven](https://github.com/OneBusAway/onebusaway-application-modules/actions/workflows/maven.yml/badge.svg)](https://github.com/OneBusAway/onebusaway-application-modules/actions/workflows/maven.yml) [Join the OneBusAway chat](https://onebusaway.slack.com/)

A project of the non-profit [Open Transit Software Foundation](https://opentransitsoftwarefoundation.org/)!

The OneBusAway application suite's primary function is to share real-time public transit information with riders across a variety of interfaces:

  * [OneBusAway Web](http://pugetsound.onebusaway.org/) - A variety of web interfaces to transit data
    * A standard web interface to transit data, including maps and stop pages with real-time info
    * [Sign-mode](https://github.com/OneBusAway/onebusaway-application-modules/wiki/Sign-Mode) - Same information as the standard web interface, but in a large format for large displays (e.g., large televisions)
    * A mobile-optimized version of the web interface for smart phone mobile browsers
    * A text-only version of the web interface for more-basic mobile browsers
  * [OneBusAway REST API]( https://developer.onebusaway.org/api/where) - A RESTful web-service that can be used to quickly write applications built on top of transit data. This API powers the following apps:
    * [OneBusAway iOS (iPhone)](https://github.com/OneBusAway/onebusaway-ios)
    * [OneBusAway Android](https://github.com/OneBusAway/onebusaway-android)
    * [OneBusAway Windows Phone](https://github.com/OneBusAway/onebusaway-windows-phone)
    * [OneBusAway Windows 8](https://github.com/OneBusAway/onebusaway-windows8)
    * [OneBusAway Windows 10 (under development)](https://github.com/OneBusAway/onebusaway-windows10)
    * [OneBusAway Fire Phone (based on Android)](https://github.com/OneBusAway/onebusaway-android)
    * [OneBusAway Alexa (for Amazon Echo, etc.)](https://github.com/OneBusAway/onebusaway-alexa)
    * [OneBusAway Glassware (for Google Glass)](https://github.com/OneBusAway/onebusaway-android/pull/219)
    * [OneBusAway for Pebble Smartwatch](https://github.com/onebusaway/onebusaway-pebbletime)
  * [OneBusAway GTFS-realtime export](http://developer.onebusaway.org/modules/onebusaway-application-modules/current-SNAPSHOT/api/gtfs-realtime.html) - A bulk export of all trip updates (predictions), vehicle positions, and service alerts for a transit system in the [GTFS-realtime format](https://developers.google.com/transit/gtfs-realtime/).
  * [OneBusAway Phone](https://github.com/OneBusAway/onebusaway-application-modules/wiki/OneBusAway-Phone-and-SMS) - A Interactive Voice Response (IVR) phone application for accessing real-time transit information
  * [OneBusAway SMS](https://github.com/OneBusAway/onebusaway-application-modules/wiki/OneBusAway-Phone-and-SMS) - An SMS service for accessing real-time transit information
  * [OneBusAway Watchdog](https://github.com/OneBusAway/onebusaway-application-modules/wiki/OneBusAway-Watchdog) - A module hosting webservices for monitoring realtime data

Watch the [YouTube video](http://www.youtube.com/watch?v=CBctcyE7Am4&feature=player_embedded) for more information.

## Getting Started

Here are the high-level steps you'll need to take to launch the OneBusAway mobile apps in your area:

1. Get your schedule transit data in the [GTFS format](https://developers.google.com/transit/gtfs/)
2. Have an AVL system that produces arrival estimates (*Note: we're working removing this requirement - [contact us](https://groups.google.com/forum/#!forum/onebusaway-developers) if you're interested.  Alternatively, you may be able to use other open-source projects, such as [The Transit Clock](https://thetransitclock.github.io), to go directly from raw vehicle locations to arrival times that are shared via GTFS-realtime and SIRI - this would replace Steps 2 and 3.*)
3. Implement a [GTFS-realtime](https://developers.google.com/transit/gtfs-realtime/) or [SIRI](http://en.wikipedia.org/wiki/Service_Interface_for_Real_Time_Information) real-time data feed *(We also support [other formats](https://github.com/OneBusAway/onebusaway-application-modules/wiki/Real-Time-Data-Configuration-Guide))*
4. Set up [a OneBusAway server](https://github.com/OneBusAway/onebusaway-application-modules#setting-up-a-onebusaway-server)
5. Do some quality-control testing of arrival times
6. Request to be added as a OneBusAway region

See the [multi-region page](https://github.com/OneBusAway/onebusaway/wiki/Multi-Region) for more details.

#### Setting up a OneBusAway server

There are two options for setting up your own OneBusAway instance:
* [Configuration and Deployment Guide for v2.x](https://github.com/OneBusAway/onebusaway/wiki/Configuration-and-Deployment-Guide-for-v2.x) - Designed to provide a comprehensive deployment method for users who wish to set up a simple OneBusAway application with minimal configurations.
* [onebusaway-docker (Under development)](https://github.com/OneBusAway/onebusaway-docker) - A community-supported Docker configuration for OneBusAway v2.x is currently under development.

## Docker Development Instructions

Build the image:

```
docker build -t oba-app-modules .
docker exec -it onebusaway-application-modules-builder-1 /bin/bash
./build-and-test.sh
```



## Status

* Latest Stable Release - `2.4.18-cs`:
    * [onebusaway-transit-data-federation-webapp](https://repo.camsys-apps.com/releases/org/onebusaway/onebusaway-transit-data-federation-webapp/2.4.18-cs/onebusaway-transit-data-federation-webapp-2.4.18-cs.war)
    * [onebusaway-api-webapp](https://repo.camsys-apps.com/releases/org/onebusaway/onebusaway-api-webapp/2.4.18-cs/onebusaway-api-webapp-2.4.18-cs.war)
    * [onebusaway-enterprise-acta-webapp](https://repo.camsys-apps.com/releases/org/onebusaway/onebusaway-enterprise-acta-webapp/2.4.18-cs/onebusaway-enterprise-acta-webapp-2.4.18-cs.war)
* [Previous releases](https://developer.onebusaway.org/release-notes)

## Deployments

OneBusAway is used in a number of places:

* http://pugetsound.onebusaway.org - the original Seattle-area deployment that started it all
* http://tampa.onebusaway.org - a deployment in Tampa, Florida
* http://bustime.mta.info - real-time info for NYC MTA buses

Check out the full list on the [OneBusAway Deployments page](https://github.com/OneBusAway/onebusaway/wiki/OneBusAway-Deployments).  Check out the main project page at http://onebusaway.org.

## Download

* [Latest Stable Release](https://developer.onebusaway.org/downloads)

## Code Repository

To browse the source online visit https://github.com/OneBusAway/onebusaway-application-modules.

To create a local copy of the repository, use the following command:

`$ git clone https://github.com/OneBusAway/onebusaway-application-modules.git`


## Developer Information

 * [Installation Guide](https://github.com/OneBusAway/onebusaway-application-modules/wiki) - How to set up your own OneBusAway instance using the code and Eclipse.
 * [Real-time Data Compatibility](https://github.com/OneBusAway/onebusaway-application-modules/wiki/Real-Time-Data-Configuration-Guide) - Have a real-time transit data source (e.g., GTFS-realtime, SIRI)?  Check out this page to see how you can make it work with OneBusAway.
 * [Troubleshooting tips](https://github.com/OneBusAway/onebusaway/wiki/Troubleshooting) - Having problems?  Check out this guide.
 * [Mobile App Design Considerations](https://github.com/OneBusAway/onebusaway-application-modules/wiki/Mobile-App-Design-Considerations) - Want to create a mobile app for OneBusAway?  Check out this page first.
 * [Multi-Region](https://github.com/OneBusAway/onebusaway/wiki/Multi-Region) - OneBusAway is launching in [new cities](https://github.com/OneBusAway/onebusaway/wiki/OneBusAway-Deployments)!  Check out this page to learn how to launch the OneBusAway apps in your city.
 * [Contribution Guide](https://github.com/OneBusAway/onebusaway/wiki/Developer-Guide) - Information for developers on how to contribute code to OneBusAway, including general code conventions, how to submit patches, etc.
 * [Project Governance](https://github.com/OneBusAway/onebusaway/wiki/Governance) - Guide to OneBusAway project governance
 * [Maven Repository Info](https://github.com/OneBusAway/onebusaway/wiki/Maven-Repository) - Where the release and snapshot artifacts of OneBusAway libraries are found.
 * Mailing lists:
     - [Developer discussion list](https://groups.google.com/group/onebusaway-developers)
     - [User discussion list](https://groups.google.com/group/onebusaway-users)
 * IRC channel:
     - `#onebusaway` on Freenode
     - You can connect using your favorite IRC client or [chat through the web](http://webchat.freenode.net/?channels=onebusaway) (just enter a username and click *Connect*)


## Contact Info

There are [lots of ways to get in touch with us](https://github.com/OneBusAway/onebusaway/wiki/Contact-Us).
