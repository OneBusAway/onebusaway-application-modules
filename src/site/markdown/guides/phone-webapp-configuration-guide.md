# Phone Service Configuration Guide

This guide will instruct you on how to configure an instance of the `onebusaway-phone-webapp` interactive-voice-response
(IVR) phone interface, as described in the [phone service feature guide](../features/phone-and-sms.html).

## IVR Providers

There is no standard for writing IVR applications, so the configuration will largely depend on which IVR provider you
go with.  OneBusAway supports the following providers by default:

### Asterisk

Asterisk is a telephony application server that has a ton of features, including the ability to answer an incoming
phone call and redirect it to an IVR application.  Asterisk supports the FastAGI protocol for interacting with
applications, which is the protocol we'll use to communicate with the OneBusAway phone application.  Asterisk
configuration is beyond the scope of this document (and pretty complex even with the right documentation), so we'll
assume you already have some experience configuring and running Asterisk.  To configure the OneBusAway phone IVR
application, add an AGI entry for the application in your dialplan:

~~~
exten => 200,1,NoOp(OneBusAway IVR Application)
exten => 200,2,Agi(agi://localhost:8001/index.agi,60,r)
exten => 200,3,Hangup
~~~

Here, `localhost` is the hostname where you are running your server and `8001` is the default AGI port used by
the phone application.  You can change the default port by adding the following snippet to your `data-sources.xml`
config file:

~~~
<bean class="org.onebusaway.container.spring.PropertyOverrideConfigurer">
  <property name="properties">
    <props>
      <prop key="agiServer.port">PORT</prop>
    </props>
  </property>
</bean>
~~~

#### Text-to-Speech Tips

The default Asterisk text-to-speech is not great.  I had better luck using a text-to-speech engine from Cepstral to
generate the speech for the text in my IVR system.  Cepstral has products that can probably directly integrate into
Asterisk, but they are a bit pricey when you budget is "as close to free as possible."  Instead, I opted for a license
that only allows you to run one text-to-speech session at a time on the command-line and just cache all the results,
since most of the text in the IVR system ends up being largely static over time.  To configure, I installed `swift`,
the Cepstral text-to-speech application and configured my license.  I also installed `sox`, which will be used to
convert the files produced by `swift` into a form more usable by Asterisk.

Next, I override the default text-to-speech factory by adding the following to my `data-sources.xml` file: 

~~~
<bean id="textToSpeechFactory" class="org.onebusaway.phone.impl.SwiftAndSoxTextToSpeechFactoryImpl"/>
~~~

This will instruct the OneBusAway phone application to generate a sound file for a snippet of text, save it to the
Asterisk cache directory, and instruct Asterisk to play the sound file.

## Tweaking Pronunciation

Often, the GTFS schedule data that powers your OneBusAway instance will often use words and abbreviations that are fine
when displayed as text but give text-to-speech engines trouble.  As such, we provide a mechanism for tweaking output
text in the IVR system to aid in pronunciation.  If you text-to-speech engine supports custom syntax for pronouncing
tricky words, it can be used here as well.

First, create an xml file with the following syntax that defines translations for text in a more pronouncable form.

~~~
<?xml version="1.0" encoding="UTF-8"?>
<text-modifications>
  <replacement from="ave" to="avenue" />
  <replacement from="bthl" to="bothell" />
  <replacement from="cc" to="community college" />
  <replacement from="hwy" to="highway" />  
  <replacement from="lkview" to="lakeview" />
  <replacement from="wash" to="washington" />
<text-modifications>
~~~

Then add the following configuration to your `data-sources.xml` file:

~~~
<bean id="pronunciationFactory" class="org.onebusaway.presentation.impl.text.XmlTextModificationsFactory">
    <property name="resource" value="file:path/to/your/pronunciations.xml" />
</bean>
<bean id="pronuncation" factory-bean="pronunciationFactory" factory-method="create">
     <qualifier value="destinationPronunciation" />
</bean>
~~~

All told, you can tweak pronunciation in four different ways by creating beans that implement the
[TextModification](../apidocs/org/onebusaway/presentation/services/text/TextModification.html) interface and tagging
them with a `<qualifier value="KEY" />` tag.  The four supported qualifiers are:

* `destinationPronunciation`: Used to pronounce trip destinations.
* `routeNumberPronunciation`: Used to pronounce route numbers.
* `directionPronunciation`: Used to pronounce directions (eg. "north" vs "sount")
* `locationPronunciation`: Used to pronounce location names.

You can specify multiple qualifiers for a single bean if it can be used to support multiple pronunciations.