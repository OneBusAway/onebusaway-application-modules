#!/bin/env python                                                                                                                                                                                                                  
# A simple program for combining TransferPattern text output files into one unified output

import fileinput
import csv
import sys

def main():

    totalLineCount = 0
    idMapping = {}

    out = csv.writer(sys.stdout,lineterminator='\n')

    fi = fileinput.FileInput(openhook=fileinput.hook_compressed)

    for line in fi:

        if fi.isfirstline():
            idMapping = {}
        
        for row in csv.reader([line]):

            if len(row) == 3:
                idMapping = {}

            index = row[0]
            newIndex = totalLineCount
            idMapping[index] = newIndex
            row[0] = str(newIndex)

            if len(row) == 4:
                parentIndex = row[3]
                newParentIndex = idMapping[parentIndex]
                row[3] = str(newParentIndex)

            out.writerow(row)

        totalLineCount += 1

if __name__ == '__main__':
    main()
