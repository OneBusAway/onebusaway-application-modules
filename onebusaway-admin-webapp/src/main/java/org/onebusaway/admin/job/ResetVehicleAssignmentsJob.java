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
        assignmentDao.deleteAllExceptDate(getCurrentDate());
        _log.info("Vehicle assignments reset");
    }

    private Date getCurrentDate(){
        return new Date();
    }
}

