## The OneBusAway Application Suite [![Java CI with Maven](https://github.com/OneBusAway/onebusaway-application-modules/actions/workflows/maven.yml/badge.svg)](https://github.com/OneBusAway/onebusaway-application-modules/actions/workflows/maven.yml) [Join the OneBusAway chat](https://onebusaway.slack.com/)

A project of the non-profit [Open Transit Software Foundation](https://opentransitsoftwarefoundation.org/)!

The OneBusAway application suite's primary function is to share real-time public transit information with riders across a variety of interfaces:

  * [Wayfinder](https://github.com/onebusAway/wayfinder) - A high-performance web application built with with the SvelteKit JavaScript web app framework
  * [OneBusAway Web](http://pugetsound.onebusaway.org/) - A variety of web interfaces to transit data
    * A standard web interface to transit data, including maps and stop pages with real-time info
    * [Sign-mode](https://github.com/OneBusAway/onebusaway-application-modules/wiki/Sign-Mode) - Same information as the standard web interface, but in a large format for large displays (e.g., large televisions)
    * A mobile-optimized version of the web interface for smart phone mobile browsers
    * A text-only version of the web interface for more-basic mobile browsers
  * [OneBusAway REST API]( https://developer.onebusaway.org/api/where) - A RESTful web-service that can be used to quickly write applications built on top of transit data. This API powers the following apps:
    * [OneBusAway iOS (iPhone)](https://github.com/OneBusAway/onebusaway-ios)
    * [OneBusAway Android](https://github.com/OneBusAway/onebusaway-android)
    * [OneBusAway Alexa (for Amazon Echo, etc.)](https://github.com/OneBusAway/onebusaway-alexa)
  * [OneBusAway GTFS-realtime export](http://developer.onebusaway.org/modules/onebusaway-application-modules/current-SNAPSHOT/api/gtfs-realtime.html) - A bulk export of all trip updates (predictions), vehicle positions, and service alerts for a transit system in the [GTFS-realtime format](https://developers.google.com/transit/gtfs-realtime/).
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

## Development Instructions with Docker

```sh
docker compose up builder

# Now, open another window or tab and continue running commands:

docker compose exec builder bash
./build.sh --help # acquaint yourself with the build.sh options
./build.sh --clean --check-updates --test

# now you have built all of the OBA artifacts:
ls build/org/onebusaway/onebusaway-application-modules

# Next, build a data bundle:
cd /oba
./build_bundle.sh

# Finally, copy all of the built WAR resources into /usr/local/tomcat
./copy_resources.sh

# wait a few seconds for everything to spin up...
```

Finally, verify that everything works as expected!

* Check out the Tomcat Web App Manager at http://localhost:8080/manager/html (user/pass: admin/admin) to verify that your OBA WARs deployed correctly
* Check out the config.json API endpoint to verify that everything built correctly: http://localhost:8080/onebusaway-api-webapp/api/where/config.json?key=test

## Debugging

### VSCode

0. Make sure that you have completed all of the earlier steps and that this endpoint loads in your browser: `http://localhost:8080/onebusaway-api-webapp/api/where/config.json?key=test`
1. Install these extensions:
  * [Debugger for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-debug)
  * [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
2. Set breakpoints in the code file that you want to debug. If you're just getting started, we recommend that you set a breakpoint in `onebusaway-api-webapp/src/main/java/org/onebusaway/api/actions/api/where/ConfigAction.java` in the `index()` method. 
3. Open the Run and Debug tab in VSCode, make sure that the debug target dropdown lists `Debug (Attach)`.
4. Click the Start Debugging button.
5. Open your web browser and navigate to the API action where you have set a breakpoint. If you have set a breakpoint in ConfigAction.java, navigate to http://localhost:8080/onebusaway-api-webapp/api/where/config.json?key=test

## Deploy OneBusAway

* Download pre-built JARs and WARs: https://developer.onebusaway.org/downloads
* Terraform/Open Tofu/Infrastructure as Code: https://github.com/onebusaway/onebusaway-deployment
* Docker
  * Pre-built images: https://hub.docker.com/r/opentransitsoftwarefoundation/
  * Instructions: https://github.com/onebusAway/onebusaway-docker

## Deployments

OneBusAway is used in a number of places:

* http://pugetsound.onebusaway.org - the original Seattle-area deployment that started it all
* http://tampa.onebusaway.org - a deployment in Tampa, Florida
* http://bustime.mta.info - real-time info for NYC MTA buses

Check out the full list on the [OneBusAway Deployments page](https://github.com/OneBusAway/onebusaway/wiki/OneBusAway-Deployments).  Check out the main project page at http://onebusaway.org.

## Download

* [Latest Stable Release](https://developer.onebusaway.org/downloads)

## Build and Deploy OBA Artifacts to Maven Central

1. Set up your environment, including GPG
   * [Maven Central documentation](https://central.sonatype.org/publish/requirements/gpg/)
   * [Some helpful information elided by Sonatype](https://www.swissarmyronin.dk/Miscellaneous/Gpg-maven/)
2. Run the command `mvn deploy -DskipTests`
3. Upload the Zip file at `./target/central-publishing/central-bundle.zip` to [Maven Central's publishing page](https://central.sonatype.com/publishing/deployments).
    * The Zip file is about 600MB in size. Be sure to have a fast, reliable connection. 

Open questions and issues:

* I haven't figured out why I cannot get the `mvn deploy` command to upload to Maven Central automatically.
* We need to automate deployment to Maven Central via GitHub Actions eventually, too.

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

## Contact Info

There are [lots of ways to get in touch with us](https://developer.onebusaway.org/getting-help).
