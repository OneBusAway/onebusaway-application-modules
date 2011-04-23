#!/bin/env python                                                                                                                                                                                                                 

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
