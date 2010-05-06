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
package edu.washington.cs.rse.transit.phone;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.washington.cs.rse.transit.common.DefaultReplacementStrategy;
import edu.washington.cs.rse.transit.common.DigitReplacementStrategy;
import edu.washington.cs.rse.transit.common.IReplacementStrategy;
import edu.washington.cs.rse.transit.common.NameNormalizationStrategy;
import edu.washington.cs.rse.transit.common.model.StreetName;

@Component
public class PronunciationStrategy {

    private List<IReplacementStrategy> _replacements = new ArrayList<IReplacementStrategy>();

    private NameNormalizationStrategy _normalization;

    @Autowired
    public void setNameNormalizationStrategy(NameNormalizationStrategy normalization) {
        _normalization = normalization;
    }

    public PronunciationStrategy() {

        // We let someone else handle name normalization
        // Now it's just about pronuciation
        addReplacement("i-5", "eye five");
        addReplacement("i-405", "eye four oh five");
        addReplacement("i-90", "eye ninety");
        addReplacement("sr-520", "ess are five twenty");
        addReplacement("sr-203", "ess are two oh three");
        addReplacement("noaa", "noah");
        addReplacement("uw/ccc", "you dub, cascadia community college");
        addReplacement("uw", "you dub");
        addReplacement("lwhs", "lake washington high school");

        addReplacement("alki", "<phoneme ph=\"ae1 l k ay\">alki</phoneme>");
        addReplacement("burien", "<phoneme ph=\"b j er i eh n\">burien</phoneme>");
        addReplacement("des moines", "<phoneme ph=\"d eh m oy n\">desmoines</phoneme>");
        addReplacement("enumclaw", "<phoneme ph=\"j uw n eh m k l aa\">enumclaw</phoneme>");
        addReplacement("fauntleroy", "<phoneme ph=\"f ao n t t l eh r oy\">fauntleroy</phoneme>");
        addReplacement("fremont", "<phoneme ph=\"f r i1 m aa1 n t\">fremont</phoneme>");
        addReplacement("laurelhurst", "<phoneme ph=\"l ao1 r ah l h er1 s t\">laurelhurst</phoneme>");
        addReplacement("madrona", "<phoneme ph=\"m uh1 d r ow n ah\">madrona</phoneme>");
        addReplacement("puyallup", "<phoneme ph=\"p j1 uw aa l ah p\">puyallup</phoneme>");
        addReplacement("sammamish", "<phoneme ph=\"s ah m ae1 m ih sh\">sammamish</phoneme>");
        addReplacement("tacoma", "<phoneme ph=\"t aa k ow1 m ah\">tacoma</phoneme>");
        addReplacement("tukwila", "<phoneme ph=\"t ah k w ih1 l ah\">tukwila</phoneme>");
        addReplacement("wedgwood", "<phoneme ph=\"w eh d jh w uh d\">wedgwood</phoneme>");

        _replacements.add(new DigitReplacementStrategy());
    }

    public String getRouteNumberAsText(int routeNumber) {
        String route = Integer.toString(routeNumber);
        int n = route.length();
        if (n > 2)
            route = route.substring(0, n - 2) + " " + route.substring(n - 2);
        return route;
    }

    public String getDestinationAsText(String destination) {

        destination = _normalization.getNormalizedName(destination);

        for (IReplacementStrategy strategy : _replacements)
            destination = strategy.replace(destination);

        return destination;
    }

    public String getStreetAsText(StreetName street) {
        return getDestinationAsText(street.getCombinedName());
    }

    public String getDirectionAsText(String direction) {
        direction = direction.toUpperCase();
        if (direction.equals("N"))
            return "north";
        if (direction.equals("NW"))
            return "north west";
        if (direction.equals("W"))
            return "west";
        if (direction.equals("SW"))
            return "south west";
        if (direction.equals("S"))
            return "south";
        if (direction.equals("SE"))
            return "south east";
        if (direction.equals("E"))
            return "east";
        if (direction.equals("NE"))
            return "north east";
        return direction.toLowerCase();

    }

    private void addReplacement(String pattern, String replacement) {
        _replacements.add(new DefaultReplacementStrategy("\\b" + pattern + "\\b", replacement));
    }
}
