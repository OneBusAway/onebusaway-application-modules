/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.admin.service.impl;

import org.onebusaway.admin.service.EmailService;
import org.onebusaway.util.services.configuration.ConfigurationService;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AWSJavaMailTransport;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.web.context.ServletContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;

public class EmailServiceImpl implements EmailService, ServletContextAware {

  private static Logger _log = LoggerFactory.getLogger(EmailServiceImpl.class);
  private ConfigurationService configurationService;
  private AWSCredentials _credentials;
  AmazonSimpleEmailServiceAsyncClient _eClient;
  private String _username;
  private String _password;
  private Properties _properties;
  private Session _session;
  private Transport _transport;
  
  private static final String SMTP_HOST_NOT_FOUND = "smtp host not found";
  
  /**
   * @param configurationService the configurationService to set
   */
  @Autowired
  public void setConfigurationService(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  @Override
  public void setSmtpUser(String user) {
    _username = user;
  }
  @Override
  public void setSmtpPassword(String password) {
    _password = password;
  }
  
  @PostConstruct
  @Override
  public void setup() {
    try {
      String mailSMTPServer = "";
      String  mailSMTPServerPort = "";
      // Try getting smtp host and port values from configurationService.
      try {
        mailSMTPServer = configurationService.getConfigurationValueAsString("admin.smtpHost", SMTP_HOST_NOT_FOUND);
        mailSMTPServerPort = configurationService.getConfigurationValueAsString("admin.smtpPort", "25");
      } catch(RemoteConnectFailureException e) {
        _log.error("Setting smtp host to value : '" + SMTP_HOST_NOT_FOUND + "' due to failure to connect to TDM");
        mailSMTPServer = SMTP_HOST_NOT_FOUND;
        e.printStackTrace();
      }
      
      // If smtp host name was not found, assume this should use AWS
      _properties = new Properties();
      boolean useSMTP = mailSMTPServer.equals(SMTP_HOST_NOT_FOUND) ? false : true;
      if (useSMTP) {    // Configure for SMTP
        _properties.setProperty("mail.transport.protocol","smtp");
        _properties.setProperty("mail.smtp.starttls.enable","false");
        _properties.setProperty("mail.smtp.host",mailSMTPServer);
        _properties.setProperty("mail.smtp.auth","false");
        _properties.setProperty("mail.debug","false");
        _properties.setProperty("mail.smtp.port",mailSMTPServerPort);
      } else {          // Configure for AWS
        // AWS specifics
        _credentials = new BasicAWSCredentials(_username, _password);
        _eClient = new AmazonSimpleEmailServiceAsyncClient(_credentials);
        // Java specifics
        _properties.setProperty("mail.transport.protocol", "aws");
        _properties.setProperty("mail.aws.user", _credentials.getAWSAccessKeyId());
        _properties.setProperty("mail.aws.password", _credentials.getAWSSecretKey());
      }
      
      _session = Session.getInstance(_properties);
      Session session=Session.getDefaultInstance(_properties);
      session.setDebug(false);
      _transport = useSMTP ? _session.getTransport("smtp") : new AWSJavaMailTransport(_session, null);
    } catch (Exception ioe) {
      // log this heavily, but don't let it prevent context startup
      _log.error("EmailServiceImpl setup failed, likely due to missing or invalid credentials.");
      _log.error(ioe.toString());
    }
  }
  
  @Override
  public void sendAsync(String to, String from, String subject, StringBuffer messageBody) {
    List<String> toAddresses = new ArrayList<String>();
    for (String address : to.split(",")) {
      toAddresses.add(address);
    }
    Destination destination = new Destination(toAddresses);
    Body body = new Body();
    body.setText(new Content(messageBody.toString()));
    Message message = new Message(new Content(subject), body);
    SendEmailRequest sendEmailRequest = new SendEmailRequest(from, destination, message); 
    Future<SendEmailResult> result = _eClient.sendEmailAsync(sendEmailRequest);
    _log.info("sent email to " + to + " with finished=" + result.isDone());
  }

  @Override
  public void send(String to, String from, String subject, StringBuffer messageBody) {
    sendJava(to, from, subject, messageBody);
  }
  
  public void sendSES(String to, String from, String subject, StringBuffer messageBody) {
    List<String> toAddresses = new ArrayList<String>();
    for (String address : to.split(",")) {
      toAddresses.add(address);
    }
    Destination destination = new Destination(toAddresses);
    Body body = new Body();
    body.setText(new Content(messageBody.toString()));
    Message message = new Message(new Content(subject), body);
    SendEmailRequest sendEmailRequest = new SendEmailRequest(from, destination, message); 
    SendEmailResult result = _eClient.sendEmail(sendEmailRequest);
    _log.info("sent email to " + to + " with result=" + result);
  }

  public void sendJava(String to, String from, String subject, StringBuffer messageBody) {
    try {
      javax.mail.Message msg = new MimeMessage(_session);
      msg.setFrom(new InternetAddress(from));
      msg.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
      msg.setSubject(subject);
      if (messageBody != null) { 
        msg.setText(messageBody.toString());
      } 
      msg.saveChanges();
      if (!_transport.isConnected()) {
        _transport.connect();
      }
      _transport.send(msg);
    } catch (Exception e) {
      _log.error("sendJava failed", e);
    }
  }
  @Override
  public void setServletContext(ServletContext servletContext) {
    if (servletContext != null) {
      String user = servletContext.getInitParameter("smtp.user");
      _log.info("servlet context provided smtp.user=" + user);
      if (user != null) {
        setSmtpUser(user);
      }
      String password = servletContext.getInitParameter("smtp.password");
      if (password != null) {
        _log.info("servlet context provided smtp.password");
        setSmtpPassword(password);
      }
    }
  }

}
