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

FROM tomcat:8.5.98-jdk11-temurin

ARG OBA_VERSION=2.5.13-otsf
ENV OBA_VERSION_ENV=$OBA_VERSION

# Start configuring OBA
WORKDIR /oba

COPY ./build_bundle.sh .

CMD /oba/build_bundle.sh ${OBA_VERSION_ENV}

# for debugging
# CMD ["tail", "-f", "/dev/null"]