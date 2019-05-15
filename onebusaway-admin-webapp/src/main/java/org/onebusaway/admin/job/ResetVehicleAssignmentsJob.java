/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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

import org.onebusaway.admin.service.assignments.AssignmentConfigDao;
import org.onebusaway.admin.service.assignments.AssignmentDao;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;

public class ResetVehicleAssignmentsJob extends QuartzJobBean {

    private static Logger _log = LoggerFactory.getLogger(ResetVehicleAssignmentsJob.class);


    @Override
    protected void executeInternal(JobExecutionContext arg0)
            throws JobExecutionException {

        AssignmentDao assignmentDao =
                (AssignmentDao)arg0.getJobDetail().getJobDataMap().get("assignmentDao");

        AssignmentConfigDao assignmentConfigDao =
                (AssignmentConfigDao)arg0.getJobDetail().getJobDataMap().get("assignmentConfigDao");

        assignmentConfigDao.deleteAll();
        assignmentDao.deleteAll();
        _log.info("Vehicle assignments reset");
    }

    private Date getCurrentDate(){
        return new Date();
    }
}

