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

public class TimingBean {

    private long _now;

    private long _startOfWeek;

    public TimingBean(long now, long startOfWeek) {
        _now = now;
        _startOfWeek = startOfWeek;
    }

    public long getNow() {
        return _now;
    }

    public long getStartOfWeek() {
        return _startOfWeek;
    }

    public long getTimeOfWeek() {
        return _now - _startOfWeek;
    }

    public int getTimeOfWeekInMinutes() {
        return (int) ((getTimeOfWeek() / (1000 * 60)) % (24 * 60));
    }
}
