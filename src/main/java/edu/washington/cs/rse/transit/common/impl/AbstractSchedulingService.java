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
package edu.washington.cs.rse.transit.common.impl;

import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.aggregate.ICommonStopTime;
import edu.washington.cs.rse.transit.common.model.aggregate.ScheduledArrivalTime;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class AbstractSchedulingService {

    /***************************************************************************
     * Timining
     **************************************************************************/

    protected static final TimeZone _tz = TimeZone.getTimeZone("US/Pacific");

    protected static final long MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;

    protected static final long MILLISECONDS_IN_WEEK = 7 * MILLISECONDS_IN_DAY;

    protected static final long SCHEDULE_WINDOW = 60 * 60 * 1000;

    protected static final int MAX_WINDOW = 45;

    /***************************************************************************
     * Protected Members
     **************************************************************************/

    protected MetroKCDAO _dao;

    /***************************************************************************
     * Public Methods
     **************************************************************************/

    @Autowired
    public void setMetrokcDAO(MetroKCDAO dao) {
        _dao = dao;
    }

    /***************************************************************************
     * Protected Methods
     **************************************************************************/

    protected TimingBean getTiming() {
        return getTiming(System.currentTimeMillis());
    }

    protected TimingBean getTiming(long now) {
        long timeOfWeek = getMillisecondsSinceStartOfWeek(now);
        long startOfWeek = now - timeOfWeek;
        TimingBean timing = new TimingBean(now, startOfWeek);
        return timing;
    }

    protected long getMillisecondsSinceStartOfWeek(long time) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(_tz);
        calendar.setTimeInMillis(time);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
            calendar.add(Calendar.DAY_OF_WEEK, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startOfWeek = calendar.getTimeInMillis();
        return time - startOfWeek;
    }

    protected int getMinutesSinceStartOfWeek(long millisecondsSinceStartOfWeek) {
        return (int) ((millisecondsSinceStartOfWeek / (1000 * 60)) % (24 * 60));
    }

    protected long getClosestTime(TimingBean timing, long targetTimeOfDay) {

        long startOfWeek = timing.getStartOfWeek();
        long timeOfWeek = timing.getTimeOfWeek();

        int days = (int) (timeOfWeek / MILLISECONDS_IN_DAY);
        long nowTimeOfDay = timeOfWeek % MILLISECONDS_IN_DAY;
        if (nowTimeOfDay - targetTimeOfDay > MILLISECONDS_IN_DAY / 2)
            targetTimeOfDay += MILLISECONDS_IN_DAY;
        else if (targetTimeOfDay - nowTimeOfDay > MILLISECONDS_IN_DAY / 2)
            targetTimeOfDay -= MILLISECONDS_IN_DAY;
        return startOfWeek + days * MILLISECONDS_IN_DAY + targetTimeOfDay;
    }

    /***************************************************************************
     * 
     **************************************************************************/

    protected <T extends ICommonStopTime> List<ScheduledArrivalTime> getStopTimesAsScheduledArrivalTimes(
            Iterable<T> stopTimes, TimingBean timing) {
        List<ScheduledArrivalTime> arrivals = new ArrayList<ScheduledArrivalTime>();
        for (T cst : stopTimes) {
            ScheduledArrivalTime sat = getStopTimeAsScheduledArrivalTime(cst, timing);
            if (sat != null)
                arrivals.add(sat);
        }
        return arrivals;
    }

    protected ScheduledArrivalTime getStopTimeAsScheduledArrivalTime(ICommonStopTime cst, TimingBean timing) {

        long tOffsetWeek = getInTimeOfWeek(cst, timing.getTimeOfWeek());

        if (tOffsetWeek <= 0)
            return null;

        long t = timing.getStartOfWeek() + tOffsetWeek;
        while (t - timing.getNow() > MILLISECONDS_IN_WEEK / 2)
            t -= MILLISECONDS_IN_WEEK;
        while (t - timing.getNow() < -MILLISECONDS_IN_WEEK / 2)
            t += MILLISECONDS_IN_WEEK;

        return new ScheduledArrivalTime(cst, t);
    }

    protected long getInTimeOfWeek(ICommonStopTime stopTime, long timeOfWeek) {
        return getInTimeOfWeek(stopTime, timeOfWeek - SCHEDULE_WINDOW, timeOfWeek + SCHEDULE_WINDOW);
    }

    /**
     * 
     * @param timeOfWeekFrom
     *            time in milliseconds since the start of the week
     * @return
     */
    private long getInTimeOfWeek(ICommonStopTime stopTime, long timeOfWeekFrom, long timeOfWeekTo) {

        long[] times = getTimeSinceSundayMidnight(stopTime);

        for (long t : times) {
            if (isTimeLessThanOrEqualTo(timeOfWeekFrom, t) && isTimeLessThanOrEqualTo(t, timeOfWeekTo))
                return t;
        }

        return -1;
    }

    private long[] getTimeSinceSundayMidnight(ICommonStopTime stopTime) {

        String scheduleType = stopTime.getTrip().getScheduleType();

        long ms = (long) stopTime.getPassingTime() * 60 * 1000;
        long[] times = null;

        if (scheduleType.equals("WEEKDAY")) {
            times = new long[5];
            for (int i = 0; i < 5; i++)
                times[i] = i * MILLISECONDS_IN_DAY + ms;
        } else if (scheduleType.equals("SATURDAY")) {
            times = new long[1];
            times[0] = 5 * MILLISECONDS_IN_DAY + ms;
        } else if (scheduleType.equals("SUNDAY")) {
            times = new long[1];
            times[0] = 6 * MILLISECONDS_IN_DAY + ms;
        }

        return times;
    }

    private boolean isTimeLessThanOrEqualTo(long timeA, long timeB) {
        timeA = getNormalizedTime(timeA);
        timeB = getNormalizedTime(timeB);
        return (timeA <= timeB && (timeB - timeA) < MILLISECONDS_IN_WEEK / 2)
                || (timeB <= timeA && (timeA - timeB > MILLISECONDS_IN_WEEK / 2));
    }

    private long getNormalizedTime(long t) {
        while (t < 0)
            t += MILLISECONDS_IN_WEEK;
        t = t % MILLISECONDS_IN_WEEK;
        return t;
    }
}
