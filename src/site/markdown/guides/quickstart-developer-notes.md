# Developer Notes

Looking for Developer Notes for OneBusAway itself?  Check out our code-site:
  
[https://github.com/OneBusAway/onebusaway-application-modules/wiki](https://github.com/OneBusAway/onebusaway-application-modules/wiki)
  
The `onebusaway-quickstart` module constructs an executable war that can be used to both build a transit data
bundle and then host an embedded Jetty webapp instance for that transit data bundle.  The Maven modules used to
construct the quickstart executable war are a bit complicated, so we document them here.

The `onebusaway-quickstart` module itself is just a parent module for the sub-modules that do the actual work:
  
* `onebusaway-quickstart-common`
* `onebusaway-quickstart-bootstrap`
* `onebusaway-quickstart-mains`
* `onebusaway-quickstart-webapp`
* `onebusaway-quickstart-assembly`
    
The `onebusaway-quickstart-common` module contains a number of classes that are shared between all the quickstart
modules.  It doesn't have dependencies on any other modules.

The `onebusaway-quickstart-bootstrap` module contains the main entry point for the executable war.  It constructs
an initial classpath from the JAR files embedded in META-INF/bootstrap-lib directory of the executable war, which
includes the `onebusaway-quickstart-mains` module described below, along with its dependencies.  It then passes
off execution to either a bundle build phase or a webapp hosting phase, both defined in the
`onebusaway-quickstart-mains` module.

The `onebusaway-quickstart-mains` module contains the main entry points for building the transit data bundle and
for running an embedded Jetty instance for hosting a OneBusAway webapp instance.  It has dependencies on
`onebusaway-transit-data-federation-builder` for building the transit data bundle and Jetty for hosting the
webapps.  Most of the dependencies necessary for building a transit data bundle are already included in the
`onebusaway-transit-data-federation-webapp`, which is part of the executable war, with the exception of
`onebusaway-transit-data-federation-builder`.  As such, we include the builder JAR in the WEB-INF/lib, along
with its dependencies, and construct the transit data bundle builder classpath to include the WEB-INF/lib jars.

The `onebusaway-quickstart-webapp` contains a few helper classes to assist with hosting the executable webapp.
Primary among them is `BootstrapWebApplicationContext`, which injects custom bean definitions into the Spring
webapp application context, as determined by command-line parameters.  This allows us to dynamically configure features
like real-time data sources from the command-line of the executable war.

The `onebusaway-quickstart-assembly` module is in charge of assembling everything together into an executable
WAR.  It does this using the `maven-assembly-plugin`.  See the assembly descriptors in the `src/main/assembly`
directory of the module for details on how everything is combined to create the WAR.