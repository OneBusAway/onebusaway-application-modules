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
package org.onebusaway.common.replacement;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class NameNormalizationStrategy {

  private List<IReplacementStrategy> _replacements = new ArrayList<IReplacementStrategy>();

  public NameNormalizationStrategy() {

    // Fix Typos First
    addReplacement("ands atlantic", "and s atlantic");
    addReplacement("&se", "& se");
    addReplacement("ne&", "ne &");
    addReplacement("ne/ne", "ne & ne");
    addReplacement("ne\\ne", "ne & ne");
    addReplacement("nw/nw", "nw & nw");
    addReplacement("nw\\nw", "nw & nw");
    addReplacement("sw/w", "sw & w");
    addReplacement("n\\denny", "n & denny");
    addReplacement("s\\.everett", "s everett");
    addReplacement("ns/b", "");
    addReplacement("e/b-far/mid", "");

    // Place Name Abbreviations
    addReplacement("bthl", "bothell");
    addReplacement("iss", "issaquah");
    addReplacement("hlnds", "highlands");
    addReplacement("lkview", "lakeview");
    addReplacement("g washington", "george washington");
    addReplacement("wash", "washington");

    addReplacement("ml", "martin luther");
    addReplacement("m\\. l\\. king", "martin luther king");
    addReplacement("jr", "junior");
    addReplacement("u wy ne & ne pac st",
        "university way ne and ne pacific street");

    // Punctuation
    addReplacement("\\.", "");
    addReplacement("\\(", "");
    addReplacement("\\)", "");

    // Highway Name Abberviations
    addReplacement("i-5/ne", "i-5 north east");
    addReplacement("i 5", "i-5");
    addReplacement("i-405s", "i-405 south");
    addReplacement("i 90", "i-90");
    addReplacement("sr 520", "sr-520");
    addReplacement("sr 203", "sr-203");
    addReplacement("exp lanes", "express lanes");
    addReplacement("reg lanes", "regular lanes");

    // Normalize Transit Bay Names
    addReplacement("bay\\s*#{0,1}\\s*(\\d+)", "bay \\1");

    // Cardinal Directions
    addReplacement("n/s", "north-south");
    addReplacement("e/w", "east-west");
    addReplacement("e/n", "east-north");
    addReplacement("w/s", "west-south");

    addReplacement("n\\\\b", "north bound");
    addReplacement("n/b", "north bound");
    addReplacement("nb", "north bound");

    addReplacement("s\\\\b", "south bound");
    addReplacement("s/b", "south bound");
    addReplacement("sb", "south bound");

    addReplacement("eastbnd", "east bound");
    addReplacement("e\\\\b", "east bound");
    addReplacement("e/b", "east bound");
    addReplacement("eb", "east bound");

    addReplacement("w\\\\b", "west bound");
    addReplacement("w/b", "west bound");
    addReplacement("wb", "west bound");

    addReplacement("n", "north");
    addReplacement("ne", "north east");
    addReplacement("e", "east");
    addReplacement("se", "south east");
    addReplacement("s", "south");
    addReplacement("sw", "south west");
    addReplacement("w", "west");
    addReplacement("nw", "north");

    addReplacement("so", "south");

    // General Abberviations
    addReplacement("av", "avenue");
    addReplacement("ave", "avenue");
    addReplacement("bch", "beach");
    addReplacement("bl", "boulevard");
    addReplacement("bldg", "building");
    addReplacement("blk", "block");
    addReplacement("blvd", "boulevard");
    addReplacement("cc", "community college");
    addReplacement("cntr", "center");
    addReplacement("co", "county");
    addReplacement("conv", "convention");
    addReplacement("ctr", "center");
    addReplacement("dr", "drive");
    addReplacement("ext", "extension");
    addReplacement("exp", "express");
    addReplacement("fed", "federal");
    addReplacement("frwy", "freeway");
    addReplacement("fwy", "freeway");
    addReplacement("fwystn", "freeway station");
    addReplacement("hs", "high school");
    addReplacement("hwy", "highway");
    addReplacement("intrntnl", "international");
    addReplacement("isl", "island");
    addReplacement("lay", "layover");
    addReplacement("lk", "lake");
    addReplacement("ln", "lane");
    addReplacement("lns", "lanes");
    addReplacement("mem", "memorial");
    addReplacement("mt", "mount");
    addReplacement("p & r", "park and ride");
    addReplacement("pr", "park and ride");
    addReplacement("pkwy", "parkway");
    addReplacement("pl", "place");
    addReplacement("pt", "point");
    addReplacement("rdwy", "roadway");
    addReplacement("rdwy", "roadway");
    addReplacement("reg", "regular");
    addReplacement("st", "street");
    addReplacement("sta", "station");
    addReplacement("stn", "station");
    addReplacement("t\\.c\\.", "transit center");
    addReplacement("tc", "transit center");
    addReplacement("temp", "temporary");
    addReplacement("univ", "university");
    addReplacement("vly", "valley");
    addReplacement("wy", "way");

    // General
    addReplacement("&", "and");
  }

  public String getNormalizedName(String name) {

    name = name.toLowerCase();

    for (IReplacementStrategy strategy : _replacements)
      name = strategy.replace(name);

    return name;
  }

  /***************************************************************************
   * Private Methods
   **************************************************************************/

  private void addReplacement(String pattern, String replacement) {
    _replacements.add(new DefaultReplacementStrategy("\\b" + pattern + "\\b",
        replacement));
  }
}