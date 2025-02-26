#!/bin/bash

#
# Copyright (C) 2024 Open Transit Software Foundation <info@onebusaway.org>
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

if [ -z "$GTFS_URL" ]; then
    echo "GTFS_URL is not set"
    exit 1
fi

echo "OBA Bundle Builder Starting"
echo "GTFS_URL: $GTFS_URL"
echo "OBA Version: $OBA_VERSION"

wget -O /bundle/gtfs.zip ${GTFS_URL}

# The JAR must be executed from within the same directory
# as the bundle, or else some necessary files are not generated.

cp /src/build/org/onebusaway/onebusaway-transit-data-federation-builder/$OBA_VERSION/onebusaway-transit-data-federation-builder-$OBA_VERSION-withAllDependencies.jar \
   /oba/builder.jar

cd /bundle \
    && java -Xss4m -Xmx3g \
        -jar /oba/builder.jar \
        ./gtfs.zip \
        .
