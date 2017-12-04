# Transit Data Bundle Guide

OneBusAway has the concept of a **transit data bundle**, which is a collection of all the data artifacts for a transit
agency (or group of transit agencies) in the internal format needed to power OneBusAway.  These transit data bundles
are typically created from external data such as GTFS feeds for transit data.  This document will walk you through the steps in creating a new transit data bundle.

## Creating the Bundle

To create the bundle, you'll need to download the `onebusaway-transit-data-federation` application.  Go to the
[Downloads page](../downloads.html) to download the application. 
  
The jar file is automatically configured to run the main class for building transit data bundles:

~~~
org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleCreatorMain
~~~

Thus, you can simply run the builder with:
  
~~~
java -jar onebusaway-transit-data-federation-builder.jar ...
~~~

**Note:** Depending on the size of your transit network, you may need to increase the amount of memory available to the
Java VM with an argument like `java -Xmx1G -jar ...`.  If your Java VM supports it, you might also consider adding the
`-server` argument, as it often makes Java run much faster.

## Quick Configuration

By default, the builder accepts two command line options:

* `path/to/your/gtfs.zip` - path to your GTFS feed
* `bundle_output_path` - the output directory where bundle artifacts will be written

This quick start mode can be used to quickly build a transit data bundle for a single transit agency, but without out
much configuration flexibility.  If you need more flexibility, see Advanced Config below.

## Advanced Configuration

The bundle also accepts an xml file command line argument for more advanced configuration options:

* `bundle.xml` - path to your bundle config xml file
* `bundle_output_path` - the output directory where bundle artifacts will be written
  
The configuration details for the bundle are captured in an xml file.  The `bundle.xml` xml config file gives you a lot
of control of how your bundle is built:

* combine multiple GTFS feeds
* override and remap GTFS agency ids
* add additional build phases

Let's look at a quick example:

~~~
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-2.5.xsd">

    <bean id="gtfs-bundles" class="org.onebusaway.transit_data_federation.bundle.model.GtfsBundles">
        <property name="bundles">
            <list>
                <ref bean="gtfsA" />
                <!-- References to other GTFS feeds could go here -->
            </list>
        </property>
    </bean>

    <bean id="gtfsA" class="org.onebusaway.transit_data_federation.bundle.model.GtfsBundle">
        <property name="path" value="path/to/your/gtfs.zip" />
        <property name="defaultAgencyId" value="1" />
        <property name="agencyIdMappings">
            <map>
                <!-- Map GTFS Agency IDs to their APTA Agency Id -->
                <entry key="KCM" value="1" />
                <entry key="EOS" value="23" />
                <entry key="ST" value="40" />
            </map>
        </property>
    </bean>

    <!-- Need a mechanism to combine stops from different feeds? -->
    <bean id="entityReplacementStrategyFactory" class="org.onebusaway.transit_data_federation.bundle.tasks.EntityReplacementStrategyFactory">
        <property name="entityMappings">
            <map>
                <entry key="org.onebusaway.gtfs.model.Stop" value="path/to/PugetSoundStopConsolidation.wiki" />
            </map>
        </property>
    </bean>
    <bean id="entityReplacementStrategy" factory-bean="entityReplacementStrategyFactory" factory-method="create"/>
 
</beans>
~~~

This configuration file is just a [Spring bean configuration file](http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/beans.html),
so you can perform arbitrarily complex configuration here.  Documentation on specific advanced configuration features
can be found below.

### GTFS Agency Id Mapping

If you are buidling a transit data bundle with data from multiple agencies, you might wish to remap the agency ids
used in each feed.  Since agencies are free to pick arbitrary agency ids in their GTFS, if they specify ids at all, it
is often necessary to pick ids of your own.  Consider the example:

~~~
<bean id="gtfsA" class="org.onebusaway.transit_data_federation.bundle.model.GtfsBundle">
    <property name="path" value="path/to/king_county_metro_gtfs.zip" />
    <property name="defaultAgencyId" value="1" />
    <property name="agencyIdMappings">
        <map>
            <!-- Map GTFS Agency IDs to their APTA Agency Id -->
            <entry key="KCM" value="1" />
            <entry key="EOS" value="23" />
            <entry key="ST" value="40" />
        </map>
    </property>
</bean>
~~~

In this example, we define a GTFS bundle which points to a GTFS file through the `path` property.  This GTFS is from
King County Metro and includes data for King County Metro, Sound Transit, and City of Seattle.  For http://onebusaway.org
we use the convention of mapping agencies to their APTA assigned id.  In this case, that's "1" for King County Metro,
"23" for City of Seattle, and "40" for Sound Transit.  We map the agency ids in a two ways.  First, we specify a default
agency id of "1", which indicates that GTFS elements like stops and shapes that don't have an agency assignment by
default will be mapped to an id of "1".  We then additionally specify an agency id mapping for the agency ids specified
in the feed.

### Entity Replacement

When working with GTFS feeds from multiple agencies in the same geographic region, it is often the case that multiple
feeds often refer to the same physical stop.  Since GTFS doesn't have a mechanism to indicate that two stops are the
same across feeds, this can often lead to confusion in the user interface, where multiple stop icons show up on the map
at the same location or a user looks for a route at one stop when they should be looking at the other.

To help with these situations, OneBusAway includes a mechanism for entity replacement that allows you to indicate that
two GTFS entities are actually the same.  This is most often used for stops in practice.  The configuration looks like:

~~~
<bean id="entityReplacementStrategyFactory" class="org.onebusaway.transit_data_federation.bundle.tasks.EntityReplacementStrategyFactory">
    <property name="entityMappings">
        <map>
            <entry key="org.onebusaway.gtfs.model.Stop" value="path/to/mapping_file" />
        </map>
    </property>
</bean>
<bean id="entityReplacementStrategy" factory-bean="entityReplacementStrategyFactory" factory-method="create"/>
~~~

Here we configure an `EntityReplacementStrategyFactory` with a specific mapping for GTFS stops.  The mapping refers
to a mapping file path.  The mapping file has the following format:

~~~
agencyIdA_stopIdA agencyIdB_stopIdB [agencyIdC_stopIdC...]
... more entries ...
~~~

Each line contains a list of stop ids separated by spaces.  The first id indicates the stop to keep, while subsequent
ids indicate stops to merge into the first stop.
 
### Other Command-Line Options

The bundle builder accepts a number of command-line options that can control the build process, allowing you to control
which phases of the build are run.

* `-skip phase_name` - skips the specified build phase.  Can be repeated.
* `-only phase_name` - only runs the specified build phase.  Can be repeated.
* `-skipTo phase_name` - jump ahead to the specified build phase.  Only specify once.
* `-include phase_name` - include the specified build phase, useful when the phase is not enabled by default.  Can be repeated.

### Adding a Custom Build Phase

You can add your own custom build phases into the build process.  To do so, you specify a task definition in your
`bundle.xml` config:

~~~
<bean class="org.onebusaway.transit_data_federation.bundle.model.TaskDefinition">
    <!-- Required -->
    <property name="taskName" value="TASK_NAME_GOES_HERE" />
    <!-- Optional -->
    <property name="afterTaskName" value="SOME_EXISTING_TASK_NAME" />
    <!-- Optional -->
    <property name="beforeTaskName" value="SOME_EXISTING_TASK_NAME" />
    <!-- Optional -->
    <property name="enabled" value="true" />
    <!-- Required -->
    <property name="task" ref="taskBeanName" />
</bean>

<bean id="taskBeanName" class="your.task.Definition" />
~~~

As you can see, the TaskDefinition bean allows you to define custom tasks and optionally control where they appear
in the build order.  The only requirement is that your task instance implement `Runnable`.

In addition to adding your task definition to your `bundle.xml`, you'll also need to add the jar or class files with
your task implementation to the classpath when you run the build process.
