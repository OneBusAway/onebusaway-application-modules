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
package org.onebusaway.webapp.actions;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.onebusaway.admin.service.TemporaryPasswordService;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;

public class ForgotPasswordAction extends OneBusAwayNYCAdminActionSupport {
	private static final long serialVersionUID = 1L;

	private static Logger _log = LoggerFactory.getLogger(ForgotPasswordAction.class);
	
	
	private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	@Autowired
	private TemporaryPasswordService _passwordService;
		
	private static final String EMAIL_SUBJECT = "OneBusAway password reset";
	private static final String DEFAULT_EMAIL_BODY = "Your temporary password is: @PASSWORD@";
	
	@Autowired
	JavaMailSender _mailSender;
	
	@Autowired
	private ConfigurationService configService;
	
	private SimpleMailMessage getMessage() {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(username);
		msg.setSubject(EMAIL_SUBJECT);

		String tempPass = _passwordService.getTemporaryPasswordForUser(username);
		msg.setText(getEmailBody(tempPass));
		return msg;
	}
	
	private String getEmailBody(String tempPass) {
	  String emailBody = getEmailBodyConfig();
	  return emailBody.replace("@PASSWORD@", tempPass);
  }

  private String getEmailBodyConfig() {
    return configService.getConfigurationValueAsString(
        "forgot.password.email.body", DEFAULT_EMAIL_BODY);
  }

  @Override
	public String execute() {
		// Don't check permissions.
		SimpleMailMessage msg = getMessage();
		try {
			_mailSender.send(msg);
			_log.info("Sent email.");
		} catch(Throwable e) {
			_log.error(e.toString(), e);
			_log.error("Could not send email: " + e.getMessage());
			throw new RuntimeException("Unable to send email:  " + e.toString());
		}
		return SUCCESS;
	}
	
	
}
