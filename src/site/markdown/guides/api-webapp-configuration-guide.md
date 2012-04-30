# API Service Configuration Guide

This guide will instruct you on how to configure an instance of `onebusaway-api-webapp`.  These are specific
configuration instructions, part of your larger [OneBusAway installation](installation-guide.html). The
`onebusaway-api-wepapp` powers the [OneBusAway REST API](../api/where/index.html).

## API Keys

The API service typically expects an API key with each request, as identified by the `key` parameter.  The API key
is a loose form of authentication that can be used for simple access control and logging purposes.  It also supports
basic connection throttling to prevent a single client from abusing the API service.

### Managing API Keys using the Admin Interface

You can manage API keys using the OneBusAway admin interface if it's included in your OneBusAway deployment.  Browse
to the following url:

http://localhost:8080/admin/api-keys.action

where the base URL is changed to reflect your installation location.  Through this interface, you can add, edit, and
delete API keys, along with determining how much traffic is allowed from the specified key. 

### Creating an API Key in data-sources.xml

It's possible to create an API key directly in your `data-sources.xml` configuration file.  Simply add a section like:

~~~
<bean class="org.onebusaway.users.impl.CreateApiKeyAction">
  <property name="key" value="YOUR_KEY_HERE" />
</bean>
~~~

On application startup, an API key with the specified name will be created if it does not already exist.

 