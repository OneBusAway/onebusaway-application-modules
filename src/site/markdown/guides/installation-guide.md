# OneBusAway Installation Guide

This guide will instruct you on how to install and run an instance of OneBusAway with your transit data.  These
instructions are intended to be thorough.  If you are looking to get a quick demo instance of OneBusAway up-and-running
with your data, check out our [quick-start guide](../../onebusaway-quickstart/current/index.html).

## Downloading the Software

Check out the [Downloads page](../downloads.html) for information about downloading the OneBusAway application modules.

At minimum you need to download `onebusaway-transit-data-federation-builder.jar` to build your transit data bundle and
one of the webapps to host your OneBusAway instance.

## Building a Bundle

OneBusAway has the concept of a transit data bundle, which is a collection of all the data artifacts for a transit
agency (or group of transit agencies) in the internal format needed to power OneBusAway. These transit data bundles
are typically created from external data such as GTFS feeds for transit data.

You will use the downloaded `onebusaway-transit-data-federation-builder.jar` to build the bundle, but the instructions
are complex enough to deserve there own page:

* [Guide to Building a Transit Data Bundle](transit-data-bundle-guide.html)

## Configuring the Webapps

OneBusAway is composed of a series of webapps that are designed to be run in a standard Java webapp container.  You can
choose whichever container you like, but we use Apache Tomcat by default.  You can download it here:

* [Download Apache Tomcat 5.5](http://tomcat.apache.org/download-55.cgi)
  
Which webapps will you be running?  For your first install, we recommend the `onebusaway-combined-webapp.war`, which
combines all the major OneBusAway modules and functionality into one application.  It includes the following modules:

* `onebusaway-transit-data-federation-webapp` - back-end transit data
* `onebusaway-webapp` - standard web interface  
* `onebusaway-api-webapp` - REST and SIRI api interfaces  
* `onebusaway-sms-webapp` - SMS interface
* `onebusaway-phone-webapp` - phone (IVR) interface
  
Optionally, you can also download and run each OneBusAway webapp independently.  This is useful when you just need one
piece of OneBusAway functionality or want to do a more complex deployment.  See special configuration details towards
the end of this guide.

There is plenty of documentation on the web for installing webapps in your container of choice, so we will focus on
specific configuration details here along with some tips for making things work with Apache Tomcat.

### data-sources.xml

For the most part, configuration means editing instances of:

~~~
data-sources.xml
~~~

This configuration file is just a [Spring bean configuration file](http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/beans.html)
and can be used to define beans and other resources that determine the configuration of your OneBusAway instance.

The webapps look for `data-sources.xml` in the `WEB-INF/classes` directory of each webapp by default.  You can add the
file to the war file directly or copy it into the exploded war directory structure.

### Spring Configuration Example

OneBusAway is powered by a number of Java classes that provide different pieces of functionality.  While we try to
provide reasonable default behavior, you may wish to tweak OneBusAway at times.  Many options can be set using a
`PropertyOverrideConfigurer` to override specific class properties.  Here is a quick example of the config you'd add
to your `data-sources.xml` file:

~~~
<bean class="org.onebusaway.container.spring.PropertyOverrideConfigurer">
  <property name="properties">
    <props>
      <prop key="cacheManager.cacheManagerName">org.onebusaway.webapp.cacheManager</prop>
      <prop key="defaultWebappConfigurationSource.googleMapsApiKey">ABC</prop>
      ...
    </props>
  </property>
</bean>
~~~

Here, each `<prop/>` entry specifies a key and a value.  The key takes the form `objectName.propertyName` where
`objectName` is a OneBusAway object and `propertyName` is a property of that object whose value you'd like to override.

For the full list of documented configuration options, check out the
[list of configuration parameters](../oba-configs/index.html) auto-generated from the OBA source code. 

### Tomcat and an external data-sources.xml

As a Tomcat tip, you can override the location of the `data-sources.xml` to point to an external file instead, which is
handy for injecting `data-sources.xml` without modifying the war.  The key is to use a context xml file to define your
webapp:

~~~
<Context path="onebusaway-webapp" docBase="path/to/onebusaway-webapp.war">
  <Parameter name="contextConfigLocation"
            value="classpath:application-context-webapp.xml file:path/to/data-sources.xml"
         override="false" />
</Context>
~~~

For more info, see http://tomcat.apache.org/tomcat-5.5-doc/config/context.html

It's important to note that when you override contextConfigLocation in this way, you'll need to additionally import the
`application-context-webapp.xml` for the webapp you are attempting to configure (it's normally included in the
'contextConfigLocation' entry in web.xml for the webapp, but we lose it when we override).  The location of the webapp
is dependent on the webapps you are using:

* onebusaway-combined-webapp: classpath:application-context-webapp.xml

If you are deploying the webapps individually, the locations are:

* onebusaway-transit-data-federation-webapp: classpath:org/onebusaway/transit_data_federation/application-context-webapp.xml
* onebusaway-api-webapp: classpath:org/onebusaway/api/application-context-webapp.xml
* onebusaway-phone-webapp: classpath:org/onebusaway/phone/application-context-webapp.xml
* onebusaway-sms-webapp: classpath:org/onebusaway/sms/application-context-webapp.xml
* onebusaway-webapp: classpath:org/onebusaway/webapp/application-context-webapp.xml

## Configuring the Combined Webapp

There is a minimum amount of configuration you need to perform to prepare the `onebusaway-combined-webapp` to be run.
At minimum, you need to add the following entries to your `data-sources.xml` file:

~~~
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-2.5.xsd">
    
    <!-- Define your bundle path.  You can also do this externally with a "bundlePath" System property -->
    <bean class="org.onebusaway.container.spring.SystemPropertyOverrideConfigurer">
        <property name="order" value="-2" />
        <property name="properties">
            <props>
                <prop key="bundlePath">/path/to/your/transit-data-bundle</prop>
            </props>
        </property>
    </bean>
</beans>
~~~

The primary configuration element sets your `bundlePath`, pointing to your [Transit Data Bundle](transit-data-bundle-guide.html).
In addition, you might optionally consider [changing the default database](database-configuration-guide.html) or
[adding a real-time data source](realtime-configuration-guide.html).

## Specific Confugration Guides

For configuration details for each of user-interface components, see the specific configuration guides:

* [API Config](api-webapp-configuration-guide.html)
* [SMS Config](sms-webapp-configuration-guide.html)
* [Phone Config](phone-webapp-configuration-guide.html)
* [Web Config](webapp-configuration-guide.html)

## Configuring Webapps Independently

As mentioned previously, there may be situations where you do not wish to use the `onebusaway-combined-webapp.war`, but
instead you wish to use the individual OneBusAway webapps.  This gives you more power and flexibility for your
installation, but the configuration is more complex as result.  As described above, each webapp is configured through
its own `data-sources.xml` file.

The key issue is that when you run the transit data webapp (`onebusaway-transit-data-federation-webapp`) independently
of the user interface webapps (`onebusaway-webapp`, `onebusaway-api-webapp`, etc.), you will need to explicitly
configure an RPC mechanism that allows the webapps to communicate with each other.  Details are included below.

#### Configuring onebusaway-transit-data-federation-webapp

As described above, `onebusaway-transit-data-federation-webapp` does the heavy lifting of exposing the transit data
bundle to the various user interface modules.  As such, the main job of the `data-sources.xml` configuration file for
the webapp is to point to the location of the bundle and the database where you installed the bundle.
See [Database Setup and Configuration](database-configuration-guide.html) for more specific details about database
setup and configuration.

Here is a quick example:

~~~
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-2.5.xsd">

    <!-- Define your bundle path.  You can also do this externally with a "bundlePath" System property -->
    <bean class="org.onebusaway.container.spring.SystemPropertyOverrideConfigurer">
        <property name="order" value="-2" />
        <property name="properties">
            <props>
                <prop key="bundlePath">/Users/bdferris/oba/local-bundles/puget_sound/current</prop>
            </props>
        </property>
    </bean>

    <!-- Database Connection Configuration -->
    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://127.0.0.1/org_onebusaway_agency_data?characterEncoding=UTF-8" />
        <property name="username" value="USERNAME" />
        <property name="password" value="PASSWORD" />
    </bean>

</beans>
~~~

#### Configuring user interface webapps

While each user interface webapp has specific configuration details, they share a lot of configuration in common.
Specifically, we need to configure where they will find the `onebusaway-transit-data-federation-webapp`.  Optionally,
you can also override the default database configuration.  For more details, see
[Database Setup and Configuration](database-configuration-guide.html).

Each user interface webapp `data-sources.xml` should include these common entries:

~~~
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-2.5.xsd">

    <!-- Wire up the transit data service.  Adjust the url, including port and path, to match
         your own deployment of the onebusaway-transit-data-federation-webapp -->
    <bean id="transitDataService" class="org.springframework.remoting.caucho.HessianProxyFactoryBean">
        <property name="serviceUrl" value="http://localhost:8080/onebusaway-transit-data-federation-webapp/remoting/transit-data-service" />
        <property name="serviceInterface" value="org.onebusaway.transit_data.services.TransitDataService" />
    </bean>

    <!-- Optionally configure a user database -->
    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://127.0.0.1/org_onebusaway_users?characterEncoding=UTF-8" />
        <property name="username" value="USERNAME" />
        <property name="password" value="PASSWORD" />
    </bean>

</beans>
~~~