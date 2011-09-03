/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.webapp.gwt.oba_application.control;

public class Score {
  /*
  public double calcScore(){
    return Math.round( 100 * ( calcPoints(1)/maxPoints ) );
  }

  int calcPoints( int limit ){
    int totalPoints = 0;  
    for (int i = 0; i < snapSortedResults.length; i++) {
      for (int j = 0; j < limit && j < snapSortedResults[i].length; j++) {
        totalPoints += snapSortedResults[i][j].score();
      }
    }
    return totalPoints;
  }
  
  QueryList.prototype.calcMaxPoints = function() {
    var maxPoints = 0;
    forEach(this.queryArray, function (item, n) { if (item[2] == Q_SINGLE || item[2] == Q_MULTI_START) maxPoints+=10; });
    return maxPoints;
  }

  public void go() {
  var selector = Math.floor( 4 * convertMeters(this.snapDist(), UNITS_MI) ); //this is miles-based regardless of the country
  var score = 0;

  switch ( selector ) {
    case 0: this.score_ = 10; break
    case 1: this.score_ = 8; break
    case 2: this.score_ = 4; break
    case 3: this.score_ = 2; break
    default: this.score_ = 0; break
  }
  return this.score_;
  }
*/
}
