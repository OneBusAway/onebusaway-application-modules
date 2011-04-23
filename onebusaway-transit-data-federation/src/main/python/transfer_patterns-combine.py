#!/usr/bin/env python

import csv
import fileinput
import getopt
import sys

def usage():
    print "usage: -s,stops=HubStops.txt -r,--retain=stopId[,stopId,...] TransferPattern-0000.txt [TranferPattern-0001.gz ...]"

def main():
    
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hs:r:", ["help", "stops=","retain="])
    except getopt.GetoptError, err:
        # print help information and exit:
        print str(err) # will print something like "option -a not recognized"
        usage()
        sys.exit(3)

    hubStopsFile = None
    stopsToRetain = {}

    for o, a in opts:
        if o in ("-h", "--help"):
            usage()
            sys.exit()
        elif o in ("-s", "--stops"):
            hubStopsFile = a
        elif o in ("-r", "--retain"):
            for stopId in a.split(","):
                stopsToRetain[stopId] = True
        else:
            assert False, "unhandled option"

    if hubStopsFile == None:
        print "You must specify a HubStops.txt file"
        usage()
        sys.exit()

    hubStops = {}

    for line in fileinput.input(hubStopsFile):
        line = line.rstrip()
        hubStops[line] = True

    totalLineCount = 0
    idMapping = {}
    originStop = None
    originStopIsHub = False
    pruneFromParent = {}
    skipTree = False

    out = csv.writer(sys.stdout,lineterminator='\n')

    fi = fileinput.FileInput(args, openhook=fileinput.hook_compressed)

    for line in fi:

        if fi.isfirstline():
            print >> sys.stderr, fi.filename()

        for row in csv.reader([line]):

            if fi.isfirstline() or len(row) == 3:
                idMapping = {}
                originStop = row[1]
                originStopIsHub = originStop in hubStops
                pruneFromParent = {}
                skipTree = False
                if len(stopsToRetain) > 0:
                    skipTree = originStop not in stopsToRetain
                
            if skipTree:
                continue

            index = row[0]
            newIndex = totalLineCount
            idMapping[index] = newIndex
            row[0] = str(newIndex)

            stopId = row[1]

            if not originStopIsHub and stopId in hubStops:
                pruneFromParent[index] = True
                row[2] = '2'

            if len(row) == 4:
                parentIndex = row[3]
                if parentIndex in pruneFromParent:
                    pruneFromParent[index] = True
                    continue
                newParentIndex = idMapping[parentIndex]
                row[3] = str(newParentIndex)

            out.writerow(row)
            totalLineCount += 1

if __name__ == "__main__":
  main()

