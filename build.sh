#!/usr/bin/env bash
#
# Copyright (C) 2012 Brian Ferris <bdferris@onebusaway.org>
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

#!/bin/bash

# Function to print help message
print_help() {
    echo "Usage: $0 [OPTIONS]"
    echo "Wrapper script for Maven build command with customizable options."
    echo
    echo "Options:"
    echo "  --help              Display this help message"
    echo "  --clean             Run the clean phase before building (optional)"
    echo "  --check-updates     Force a check for updated releases and snapshots"
    echo "  --test              Run tests (disabled by default)"
    echo "  --javadoc           Generate JavaDoc (disabled by default)"
    echo "  --validate-silent VALUE  Set the validate.silent option (default: true)"
    echo "  --log4j-config VALUE     Set the log4j.configuration option (default: empty)"
    echo
    echo "Example:"
    echo "  $0 --clean --check-updates --test --javadoc --validate-silent false --log4j-config my-log4j.properties"
}

# Default values
clean=""
check_updates=""
run_tests="false"
generate_javadoc="false"
validate_silent="true"
log4j_config=""

# Parse command line arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --help) print_help; exit 0;;
        --clean) clean="clean ";;
        --check-updates) check_updates="-U ";;
        --test) run_tests="true";;
        --javadoc) generate_javadoc="true";;
        --validate-silent) validate_silent="$2"; shift;;
        --log4j-config) log4j_config="$2"; shift;;
        *) echo "Unknown parameter: $1"; print_help; exit 1;;
    esac
    shift
done

# Construct the Maven command
cmd="mvn ${check_updates}${clean}install"

# Add options based on parsed arguments
if [ "$run_tests" = "false" ]; then
    cmd="$cmd -DskipTests=true"
fi

if [ "$generate_javadoc" = "false" ]; then
    cmd="$cmd -Dmaven.javadoc.skip=true"
fi

cmd="$cmd -Dvalidate.silent=$validate_silent"

if [ -n "$log4j_config" ]; then
    cmd="$cmd -Dlog4j.configuration=$log4j_config"
else
    cmd="$cmd -Dlog4j.configuration="
fi

# Execute the command
echo "Executing: $cmd"
eval $cmd