# SMS Configuration Guide

This guide will instruct you on how to configure an instance of the `onebusaway-sms-webapp` SMS interface, as described
in the [SMS service feature guide](../features/phone-and-sms.html).

## SMS Providers

There is no standard for writing SMS applications, so the configuration will largely depend on which SMS provider you
go with.  OneBusAway supports the following providers by default:

### Textmarks

OneBusAway supports the [Textmarks](http://textmarks.com/) provider by default.  When configuring your Textmarks
keyword, use the following url (adjusted for your installation location): 

http://localhost:8080/sms/textmarks.action?userId=\u&amp;phoneNumber=\p&amp;message=\0

This will configure Textmarks to pass in the userId and phoneNumber (which we use for session management and identifying
the user) as well as the contents of the text itself.
