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
package edu.washington.cs.rse.transit.phone.actions.schedule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

import edu.washington.cs.rse.transit.common.services.BookmarkService;
import edu.washington.cs.rse.transit.web.oba.common.client.model.PredictedArrivalBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.OneBusAwayService;

public class AbstractPredictedScheduleAction extends ActionSupport {

    private static final long serialVersionUID = 1L;

    protected OneBusAwayService _obaService;

    protected List<PredictedArrivalBean> _predictions;

    protected BookmarkService _bookmarkService;

    protected String _userId;

    @Autowired
    public void setOneBusAwayService(OneBusAwayService obaService) {
        _obaService = obaService;
    }

    @Autowired
    public void setBookmarkService(BookmarkService bookmarkService) {
        _bookmarkService = bookmarkService;
    }

    public void setUserId(String userId) {
        _userId = userId;
    }

    public List<PredictedArrivalBean> getArrivals() {
        return _predictions;
    }
}
