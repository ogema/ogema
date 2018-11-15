#!/bin/bash
#
# Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# create the libogemahwmanager shared library on raspberryPi

set -o errexit

#javah -classpath core/ref-impl/hardware-manager/target/classes:src/core/ref-impl/hardware-manager-rpi/target/classes org.ogema.hardwaremanager.api.Container org.ogema.hardwaremanager.impl.rpi.NativeAccessImpl
gcc -std=c99 -Wall -pedantic -shared -fPIC -ludev -I/usr/lib/jvm/java-7-openjdk-armhf/include -o libogemahwmanager.so org_ogema_hardwaremanager_rpi_NativeAccessImpl.c NativeAccessImpl.c
#gcc -shared -fPIC -I/usr/lib/jvm/java-7-openjdk-armhf/include -Wl,-soname,libogemahwmanager.so.1 -o libogemahwmanager.so.1.0.1 org_ogema_hardwaremanager_impl_NativeAccessImpl.c NativeAccessImpl.c -ludev
