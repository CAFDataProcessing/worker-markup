#!/bin/bash
#
# Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

apt-get update -y && apt-get upgrade -y
apt-get install -y python-dev=2.7.9-1 python-pip=1.5.6-5 python-setuptools=5.5.1-1 libxml2-dev=2.9.1+dfsg1-5+deb8u6 libxslt1-dev=1.1.28-2+deb8u3 zlib1g-dev=1:1.2.8.dfsg-2+b1 python-numpy=1:1.8.2-2 python-lxml=3.4.0-1 python-scipy=0.14.0-2 python-matplotlib=1.4.2-3.1
pip install lxml==3.6.0 --upgrade
pip install regex==2014.12.24 talon==1.3.4 jep==3.5.2
