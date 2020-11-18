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
package org.onebusaway.admin.service.assignments.impl;

import org.onebusaway.admin.model.assignments.AssignmentConfig;
import org.onebusaway.admin.service.assignments.AssignmentConfigDao;
import org.onebusaway.admin.service.assignments.AssignmentConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class AssignmentConfigServiceImpl implements AssignmentConfigService {

    protected static Logger _log = LoggerFactory.getLogger(AssignmentConfigServiceImpl.class);


    @Autowired
    AssignmentConfigDao assignmentConfigDao;

    @Override
    public String getConfigValueAsString(String key) {
        AssignmentConfig config = assignmentConfigDao.getAssignmentConfig(key);
        if(config != null){
            return config.getValue();
        }
        return null;
    }

    @Override
    public Date getConfigValueAsDate(String key) {
        AssignmentConfig config = assignmentConfigDao.getAssignmentConfig(key);
        if(config != null){
            String dateString = config.getValue();
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            try {
                Date date = format.parse(dateString);
                return date;
            } catch (ParseException e) {
                _log.error("Unable to parse " + dateString + "into date.",e );
            }
        }
        return null;
    }

    @Override
    public Date getConfigValueAsDateTime(String key) {
        AssignmentConfig config = assignmentConfigDao.getAssignmentConfig(key);
        if(config != null){
            String dateString = config.getValue();
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
            try {
                Date date = format.parse(dateString);
                return date;
            } catch (ParseException e) {
                _log.error("Unable to parse " + dateString + "into date.",e );
            }
        }
        return null;
    }

    @Override
    public void setConfigValue(String key, String value){
        assignmentConfigDao.save(new AssignmentConfig(key,value));
    }

    @Override
    public void setConfigValueAsDateTime(String key, Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        String stringDate = sdf.format(date);
        assignmentConfigDao.save(new AssignmentConfig(key, stringDate));
    }

    @Override
    public void deleteConfigValue(String key){
        AssignmentConfig assignmentConfig = assignmentConfigDao.getAssignmentConfig(key);
        assignmentConfigDao.delete(assignmentConfig);
    }

    @Override
    public void deleteAll(){
        assignmentConfigDao.deleteAll();
    }

}
