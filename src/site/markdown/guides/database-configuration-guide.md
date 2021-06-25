# Database Configuration Guide

OneBusAway makes use of relational databases in a number of places to store transit data and user account information. 
Here we attempt to describe how to setup and configure data-sources as needed and list all the places databases get used.

## Quickstart

To configure your database, you'll need to do the following:

* Edit your `data-sources.xml` file to specify your JDBC data-source configuration
* Copy the JAR for your JDBC provider into your webapp classpath

Read on for more details.

## Common Config Examples

The one piece of configuration you'll typically always need to supply is a `dataSource` definition.  This provides
information about the JDBC data-source and how to authenticate to it.  Since we typically don't want database
configuration information, including usernames and passwords to reside in the public source code, we often define a
`data-sources.xml` Spring configuration file that is imported by each application but that doesn't get included in SVN.
In `data-sources.xml`, we define our data-source:

~~~
<!-- Database Connection Configuration -->
<bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    <property name="driverClassName" value="com.mysql.jdbc.Driver" />
    <property name="url" value="jdbc:mysql://127.0.01/your_database_name?characterEncoding=UTF-8" />
    <property name="username" value="username_goes_here" />
    <property name="password" value="password_goes_here" />
</bean>
~~~

Notes:
  
* **DO**: Do make sure you've copied the JAR file for your JDBC provider into your webapp classpath.  We only include HSQLDB out of the box.
* **DO**: Do make sure you've already created the database where appropriate and that the credentials you've supplied work correctly
* **DON'T**: Don't worry about creating the initial tables in the database.  Hibernate should automatically take care of that the first time the application is run.

Below we include some comon `dataSource` configuration examples.

### hsqldb

Remember that we support HSQLDB 2.0.0 out of the box, so you don't have to add a JDBC driver jar to your classpath to
use it.

An example of an embedded in-memory HSQLDB instance:

    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="org.hsqldb.jdbcDriver" />
        <property name="url" value="jdbc:hsqldb:file:/path/to/your/org_onebusaway_database" />
        <property name="username" value="sa" />
        <property name="password" value="" />
    </bean>

An example of using a stand-alone hsqldb instance running as a process:

    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="org.hsqldb.jdbcDriver" />
        <property name="url" value="jdbc:hsqldb:hsql://localhost:/org_onebusaway_database" />
        <property name="username" value="sa" />
        <property name="password" value="" />
    </bean>

### postgresql

    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="org.postgresql.Driver" />
        <property name="url" value="jdbc:postgresql://localhost/org_onebusaway_database" />
        <property name="username" value="YOUR_USERNAME" />
        <property name="password" value="YOUR_PASSWORD" />
    </bean>

### mysql

    <bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://127.0.0.1/org_onebusaway_database?characterEncoding=UTF-8" />
        <property name="username" value="YOUR_USERNAME" />
        <property name="password" value="YOUR_PASSWORD" />
    </bean>
    
## Additional Configuration

Typically, specifying your database `dataSource` is enough configuration for simple uses.  However, you may wish to
perform more complex configuration, including tweaking the database configuration or adding new classes for persistence.
Read on for more advanced details.

We make heavy use of [Hibernate](http://hibernate.org/) for database persistence and [Spring](http://static.springsource.org/spring/docs/2.5.x/reference/)
for configuring and wiring up our application modules.  As a general strategy, we'll be using Spring to define the
various beans that will instantiate our Hibernate SessionFactory that will eventually be auto-injected into our service
implementation classes requiring database access.

## Shared Hibernate Configuration

To understand how Hibernate and Spring work together, let's start with some common Spring bean definitions shared
between all !OneBusAway modules that wish to access a database.

The `onebusaway-container` module has a resource:

   src/main/resources/org/onebusaway/container/application-context-hibernate.xml

that does the bulk of the Hibernate configuration, but that also provides extension points so you can configure your
application as appropriate.

If you take a look at `application-context-hibernate.xml`, you'll see sections like:

    <bean id="hibernateProperties" class="org.springframework.beans.factory.config.PropertiesFactoryBean">
        <property name="properties">
            <props>
                <prop key="hibernate.hbm2ddl.auto">update</prop>
                <prop key="hibernate.show_sql">false</prop>
                <!-- ... -->
            </props>
        </property>
    </bean>

that define common Hibernate configuration properties.  These properties are eventually passed to the Hibernate
SessionFactory bean:

    <!-- Hibernate session factory, where all the pieces above are wired together -->
    <bean id="sessionFactory" class="org.springframework.orm.hibernate5.LocalSessionFactoryBean" primary="true">
        <qualifier value="main" />
        <property name="dataSource" ref="dataSource" />
        <property name="hibernateProperties" ref="hibernateProperties" />
        <property name="annotatedClasses" ref="hibernateAnnotatedClasses" />
        <property name="mappingLocations" ref="hibernateMappingLocations" />
    </bean>

The only thing that isn't defined with a default is the `dataSource` reference, which you'll always need to provide.
Details on the pieces you configure are described below.

## Adding additional Hibernate entity classes

There are a couple of ways to add additional entity classes to the list supported by the Hibernate SessionFactory.
This typically only needs to be done when you are working inside one of the !OneBusAway application modules and need to
extend the set of classes that can be serialized to a database or manage the set of stored queries that can operate on
those entity classes.

The first is to configure them in a `hibernate.cfg.xml` file or by annotated the entity classes with @Entity and other
annotations and telling Hibernate to scan the classes directly.

Both approaches are used by the `onebusaway-transit-data-federation` module, as can be seen in the resource:

    onebusaway-transit-data-federation/src/main/resources/org/onebusaway/transit_data_federation/application-context-common.xml

Specifically, we specify two overrides:

    <bean class="org.onebusaway.container.spring.ListBeanPostProcessor">
        <property name="target" value="hibernateAnnotatedClasses" />
        <property name="values">
            <list>
                <value>org.onebusaway.transit_data_federation.model.RouteCollection</value>
            </list>
        </property>
    </bean>

    <bean class="org.onebusaway.container.spring.ListBeanPostProcessor">
        <property name="target" value="hibernateMappingLocations" />
        <property name="values">
            <list>
                <value>classpath:org/onebusaway/transit_data_federation/impl/ExtendedGtfsRelationalDaoImpl.hibernate.xml</value>
                <value>classpath:org/onebusaway/transit_data_federation/impl/TransitDataFederationDaoImpl.hibernate.xml</value>
            </list>
        </property>
    </bean>

The first override add the `RouteCollection` class to the set of annotated classes that will be scanned by the
SessionFactory for management by Hibernate.  The second add a couple of Hibernate xml mapping files that will included
in the SessionFactory as well.

## Where Databases Get Used

The bulk of database operations happen in one of two modules:

### onebusaway-users

The `onebusaway-users` module uses a database to manage user account information.  This module is included by three of
the major UI modules:

* onebusaway-sms-webapp
* onebusaway-phone-webapp
* onebusaway-webapp

As such, each of the three application modules typically need a `data-sources.xml` resource with the appropriate
`dataSource` definition pointing to your user database.

### onebusaway-transit-data-federation

The `onebusaway-transit-data-federation` archives transit data in a back-end database as well.

The main transit data federation application container, `onebusaway-transit-data-federation-webapp` needs a
`data-sources.xml` resource with the appropriate `dataSource` definition pointing to your transit data database. This is
typically the same data source you specified when [ creating your transit data bundle](transit-data-bundle-guide.html).