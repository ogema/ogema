/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
// ============================================================================
//               Copyright 2013
//               Fraunhofer-Gesellschaft
//               Institute for Integrated Circuits
//               All rights reserved
//
//               Proprietary and Confidential Information
//
// Title       : NativeAccessImpl.c
// Comment     : libudev part of org.ogema.hardwaremanager.impl.NativeAccess
// Assumptions :
// Limitations :
// ----------------------------------------------------------------------------
// Modification History:
// ----------------------------------------------------------------------------
// Notes:
// ----------------------------------------------------------------------------
// $Id: $
//
// ============================================================================

#ifndef NATIVE_ACCESS_IMPL_H
#define NATIVE_ACCESS_IMPL_H

/* The native part of the hardware manager ist single threaded.
   It is only used during initilization and in the context of the
   event thread. So a static error string buffer can be used. */
#define MAX_ERROR_STRING 2048
struct NativeAccessData {
   struct udev *udev;
   struct udev_monitor *mon;
   int fdMonitor;
   int fdWakeupRead;
   int fdWakeupWrite;
   char errorbuf[MAX_ERROR_STRING];
};

extern struct NativeAccessData nativeAccessData;
extern int dummyDevice;

#define TYPE_NONE 0
#define TYPE_USB 1
#define TYPE_TTY 2
#define TYPE_USB_TTY 4

#define EVENT_NONE   0
#define EVENT_ADD    1
#define EVENT_REMOVE 2

struct deviceEntry {
  int type;
  char *string;
};

struct devicesArray {
  int max;
  int current;
  struct deviceEntry devices[];
};


void cleanup(struct NativeAccessData *data);
int initialize(struct NativeAccessData *data);
void arrayFree(struct devicesArray *devices);
struct devicesArray * getHandles(struct NativeAccessData *data);
void unblock(struct NativeAccessData *data);
char * getIdString(struct NativeAccessData *data, const char *syspath);
struct udev_device *getNewDevice(struct NativeAccessData *data);
int getType(struct udev_device *dev);
int getEvent(struct udev_device *dev);

#endif /* NATIVE_ACCESS_IMPL_H */
