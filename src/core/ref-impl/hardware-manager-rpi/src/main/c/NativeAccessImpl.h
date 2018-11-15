/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
