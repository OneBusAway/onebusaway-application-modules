/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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

import org.onebusaway.admin.service.NotificationService;
import org.onebusaway.presentation.impl.service_alerts.NotificationStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Implementation of notifications.
 */
public class NotificationServiceImpl implements NotificationService {

    private TwitterServiceImpl _twitter;
    private NotificationStrategy _tweetStrategy;

    @Autowired
    public void setTwitterServiceImpl(TwitterServiceImpl twitter) {
        _twitter = twitter;
    }

    @Autowired
    public void setNotificationStrategy(NotificationStrategy strategy) { _tweetStrategy = strategy; }

    public NotificationStrategy getNotificationStrategy() {
        return _tweetStrategy;
    }

    public String tweet(String message) throws IOException {
        if (_twitter == null) {
            throw new IOException("Twitter not configured!");
        }
        // twitter calls tweeting an update of status
        return _twitter.updateStatus(message);
    }
}
