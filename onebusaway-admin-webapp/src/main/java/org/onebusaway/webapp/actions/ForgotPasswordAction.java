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

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Message;

import java.util.Properties;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.onebusaway.admin.service.TemporaryPasswordService;
import org.slf4j.Logger;

public class ForgotPasswordAction extends OneBusAwayNYCAdminActionSupport {
	private static final long serialVersionUID = 1L;

	private static Logger _log = LoggerFactory.getLogger(ForgotPasswordAction.class);
	
	private static final String EMAIL_FROM = "admin@onebusaway.com";
	private static final String EMAIL_SUBJECT = "OneBusAway password reset";
	private static final String EMAIL_BODY = "Your temporary password is: ";

	private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	private String temporaryPassword;
	
	@Autowired
	private TemporaryPasswordService _passwordService;
	
	// Send email with password reset link. Assume email address = username.
	@Override
	public String execute() {
		super.execute();

		temporaryPassword = _passwordService.getTemporaryPasswordForUser(username);
		try {
			// User default session
			Session session = Session.getDefaultInstance(new Properties());

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(EMAIL_FROM));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username));
			message.setSubject(EMAIL_SUBJECT);
			message.setText(EMAIL_BODY + temporaryPassword);
			Transport.send(message);
			
			return SUCCESS;
			
		} catch (Exception e) {
			_log.warn("Could not send email: " + e.getMessage());
			throw new RuntimeException("Unable to send email.");
		}
	}
	
}
