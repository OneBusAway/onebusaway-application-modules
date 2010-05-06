/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common.interceptors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.asteriskjava.fastagi.AgiRequest;

@Aspect
public class LoggingInterceptor {

    private static DateFormat _dateFormat = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");

    private File _outputDirectory;

    private PrintWriter _out;

    public void setOutputDirectory(File outputDirectory) {
        _outputDirectory = outputDirectory;
    }

    public void startup() throws IOException {
        File outputFile = getOutputFile();
        _out = new PrintWriter(new FileWriter(outputFile, true));
    }

    public void shutdown() {
        _out.close();
    }

    /***************************************************************************
     * Interceptors
     **************************************************************************/

    @After("execution(* edu.washington.cs.rse.transit.common.services.StopSchedulingService.getPredictedArrivalsByStopId(..))")
    public void doLogSchedulingServiceStopCalls(JoinPoint point) throws Throwable {
        String v = getArgsAsString(point);
        log("edu.washington.cs.rse.transit.common.services.StopSchedulingService.getPredictedArrivalsByStopId," + v);
    }

    @After("execution(* edu.washington.cs.rse.transit.common.services.TimepointSchedulingService.getPredictedArrivalsByTimepointId(..))")
    public void doLogSchedulingServiceTimepointCalls(JoinPoint point) throws Throwable {
        String v = getArgsAsString(point);
        log("edu.washington.cs.rse.transit.common.services.TimepointSchedulingService.getPredictedArrivalsByTimepointId," + v);
    }

    @Before("execution(void org.traditionalcake.probablecalls.AgiEntryPoint.service(..))")
    public void doLogAgiEntryPointServiceCalls(JoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        AgiRequest request = (AgiRequest) args[0];
        log("org.traditionalcake.probablecalls.AgiEntryPoint.service," + request.getCallerIdNumber());
    }

    /***************************************************************************
     * Private Methods
     **************************************************************************/

    private void log(String message) {
        if (_out != null) {
            _out.println(_dateFormat.format(new Date()) + "," + message);
            _out.flush();
        }
    }

    private File getOutputFile() {
        if (_outputDirectory == null)
            throw new IllegalStateException("No output directory set");
        return new File(_outputDirectory, "onebusaway.log");
    }

    private String getArgsAsString(JoinPoint point) {
        StringBuilder args = new StringBuilder();
        for (Object arg : point.getArgs()) {
            if (args.length() > 0)
                args.append(",");
            args.append(arg);
        }
        return args.toString();
    }
}
