#!/bin/bash
#
# This file is part of OGEMA.
#
# OGEMA is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# OGEMA is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
#

# create the libogemahwmanager shared library on raspberryPi

set -o errexit

#javah -classpath core/ref-impl/hardware-manager/target/classes:src/core/ref-impl/hardware-manager-rpi/target/classes org.ogema.hardwaremanager.api.Container org.ogema.hardwaremanager.impl.rpi.NativeAccessImpl
gcc -std=c99 -Wall -pedantic -shared -fPIC -ludev -I/usr/lib/jvm/java-7-openjdk-armhf/include -o libogemahwmanager.so org_ogema_hardwaremanager_rpi_NativeAccessImpl.c NativeAccessImpl.c
#gcc -shared -fPIC -I/usr/lib/jvm/java-7-openjdk-armhf/include -Wl,-soname,libogemahwmanager.so.1 -o libogemahwmanager.so.1.0.1 org_ogema_hardwaremanager_impl_NativeAccessImpl.c NativeAccessImpl.c -ludev
