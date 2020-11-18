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
package org.onebusaway.admin.job;

import java.util.List;

import org.onebusaway.admin.service.UserManagementService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * A simple job to ping the database to keep the connection pool active.
 * The application is not used for long period of time leading to "database down"
 * error messages.
 *
 */
@Transactional(transactionManager = "transactionManager")
public class PingJob extends QuartzJobBean {

  private static Logger _log = LoggerFactory.getLogger(PingJob.class);

  @Override
  @Transactional
  protected void executeInternal(JobExecutionContext arg0)
      throws JobExecutionException {
    // execute a simple query to keep db connection alive
    UserManagementService userManagementService = 
        (UserManagementService)arg0.getJobDetail().getJobDataMap().get("userManagementService");
    
    
    final String searchString = "a";
    if (userManagementService != null) {
      userManagementService.getUserNames(searchString);
    } else {
      _log.error("userManagementService not provisioned");
    }
    
  }

}
