#!/bin/env python                                                                                                                                                                                                                 
#
# Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


import fileinput
import csv
import sys

def main():

    isSegmentStart = newSegmentStartMapping()

    fi = fileinput.FileInput(openhook=fileinput.hook_compressed)

    transferCounts = {}

    for line in fi:

        if fi.isfirstline():
            isSegmentStart = newSegmentStartMapping()
            print >> sys.stderr, fi.filename()

        for row in csv.reader([line]):

            if len(row) == 3:
                isSegmentStart = newSegmentStartMapping()

            index = row[0]
            stopId = row[1]

            parentIndex = '-1'
            if len(row) == 4:
                parentIndex = row[3]

            parentWasSegmentStart = isSegmentStart[parentIndex]
            currentIsSegmentStart = not parentWasSegmentStart

            isSegmentStart[index] = currentIsSegmentStart

            if stopId not in transferCounts:
                transferCounts[stopId] = long(0);

            transferCounts[stopId] += 1

    for key,value in transferCounts.items():
        print "%d\t%s" % (value,key)

def newSegmentStartMapping():
    return {'-1': True}

if __name__ == '__main__':
    main()
