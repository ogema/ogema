/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
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

#include <sys/select.h>
#include <sys/socket.h> /* socketpair */
#include <stdio.h>
#include <unistd.h> /* close */
#include <stdlib.h>
#include <stdarg.h>
#include <errno.h>
#include <string.h> /* strerror */
#include <libudev.h>

#include "NativeAccessImpl.h"

#define ERR_CAUSE(data, message) err(data, "%s:%d: " message " Cause: %s", __func__, __LINE__, strerror(errno));
#define ERR_FIXED(data, message) err(data, "%s:%d: " message, __func__, __LINE__);

struct NativeAccessData nativeAccessData =
{
  .fdMonitor = -1,
  .fdWakeupRead = -1,
  .fdWakeupWrite = -1
};

// this reserves a return value of getDevice for wakeup events
int dummyDevice;

/* ------------------------------------------------------------------------- */
void cleanup(struct NativeAccessData *data)
{
  if (data->fdWakeupWrite >= 0)
    close(data->fdWakeupWrite);
  if (data->fdWakeupRead >= 0)
    close(data->fdWakeupRead);
  if (data->mon)
    udev_monitor_unref(data->mon);
  if(data->udev)
    udev_unref(data->udev);
}

/* ------------------------------------------------------------------------- */
int err(struct NativeAccessData *data, const char *message, ...)
{
  va_list ap;

  va_start(ap, message);

  vsnprintf(data->errorbuf, sizeof(data->errorbuf), message, ap);
  data->errorbuf[sizeof(data->errorbuf) - 1] = '\0';

  va_end(ap);

  return 0;
}

/* ------------------------------------------------------------------------- */
int initialize(struct NativeAccessData *data)
{
  int fdArray[2];

  /* set up libudev */
  data->udev = udev_new();
  if (!data->udev) {
    ERR_CAUSE(data, "udev_new() failed.");
    return -1;
  }

  /* Set up a monitor to monitor usb device changes */
  data->mon = udev_monitor_new_from_netlink(data->udev, "udev");
  if (!data->mon)
  {
    ERR_CAUSE(data, "udev_monitor_new_from_netlink() failed.");
    return -1;
  }

  if (udev_monitor_filter_add_match_subsystem_devtype(data->mon, "tty", 0) < 0)
  {
    ERR_CAUSE(data, "udev_monitor_filter_add_match_subsystem_devtype(tty) failed.");
    return -1;
  }

  if (udev_monitor_filter_add_match_subsystem_devtype(data->mon, "usb", "usb_interface") < 0)
  {
    ERR_CAUSE(data, "udev_monitor_filter_add_match_subsystem_devtype(usb, usb_interface) failed.");
    return -1;
  }

  if (udev_monitor_enable_receiving(data->mon) < 0)
  {
    ERR_CAUSE(data, "udev_monitor_enable_receiving() failed.");
    return -1;
  }

  /* Get the file descriptor (fd) for the monitor.
     This fd will get passed to select() */
  data->fdMonitor = udev_monitor_get_fd(data->mon);

  /* The event thread waiting on select can be woken if the hardware manager is
     terminated. To achieve this a socketpair is used.
     unblock then writes to one socket, and select listens to it. */
  if (socketpair(PF_LOCAL, SOCK_DGRAM, 0, fdArray) < 0) {
    ERR_CAUSE(data, "socketpair() failed.");
    return -1;
  }

  if (FD_SETSIZE < fdArray[0] || FD_SETSIZE < fdArray[1])
  {
    ERR_FIXED(data, "fd > FD_SETSIZE");
    return -1;
  }

  data->fdWakeupRead = fdArray[0];
  data->fdWakeupWrite = fdArray[1];

  return 0;
}

/* ------------------------------------------------------------------------- */
void arrayFree(struct devicesArray *devices)
{
  int i;

  if (0 != devices)
  {
    // free strings
    for(i = 0; i < devices->current; i++)
    {
      if (0 != devices->devices[i].string)
        free(devices->devices[i].string);
    }
    free(devices);
  }
}

struct devicesArray *arrayNew(int initial)
{
  size_t size = sizeof(struct devicesArray)
    + sizeof(struct deviceEntry) * initial;

  struct devicesArray *result = malloc(size);

  if (0 != result)
  {
    memset(result, 0, size);
    result->max = initial;
  }

  return result;
}

struct devicesArray *arrayIncrease(struct devicesArray *devices)
{
  struct devicesArray *tmp;

  devices->max += 10;
  tmp = realloc(devices, sizeof(struct devicesArray)
    + sizeof(struct deviceEntry) * devices->max);

  // if we can't increase the array size, the system is out of memory.
  // we fail in this case, because something is seriously wrong
  if (0 == tmp)
  {
    arrayFree(devices);
    return 0;
  }
  else
    return tmp;
}

struct devicesArray *arrayAdd(struct devicesArray *devices, const char *path, int type)
{
  char *str = 0;

  if (0 != devices && 0 != path)
  {
    // increase array size if necessary
    if (devices->current >= devices->max)
    {
      devices = arrayIncrease(devices);

      if (0 == devices)
        return 0;
    }

    // get copy of path
    str = malloc(strlen(path) + 1);

    // if malloc fails, the system is out of memory.
    // we fail in this case, because something is seriously wrong.
    if (0 == str)
    {
      arrayFree(devices);
      return 0;
    }
    else
    {
      strcpy(str, path);

      devices->devices[devices->current].string = str;
      devices->devices[devices->current].type = type;
      devices->current++;
    }
  }
  return devices;
}

struct devicesArray *arrayOverwrite(struct devicesArray *devices, const char *path, int type)
{
  char *str;

  if (0 != devices && 0 != path)
  {
    str = malloc(strlen(path) + 1);

    if (0 == str)
    {
      arrayFree(devices);
      return 0;
    }

    strcpy(str, path);
    free(devices->devices[devices->current - 1].string);
    devices->devices[devices->current - 1].string = str;
    devices->devices[devices->current - 1].type = type;
  }

  return devices;
}

/* ------------------------------------------------------------------------- */
struct devicesArray * getHandles(struct NativeAccessData *data)
{
  struct devicesArray *result = 0;
  struct udev_enumerate *enumerate;
  struct udev_list_entry *devices, *dev_list_entry;

  result = arrayNew(50);
  if (0 == result)
  {
    ERR_CAUSE(data, "");
    return 0;
  }

  /* Create a list of the devices in the 'usb' subsystem. */
  enumerate = udev_enumerate_new(data->udev);
  if (0 == enumerate)
  {
    ERR_CAUSE(data, "udev_enumerate_new() failed.");
    return 0;
  }

  if (udev_enumerate_add_match_subsystem(enumerate, "tty") < 0)
  {
    ERR_CAUSE(data, "udev_enumerate_add_match_subsystem(tty) failed.");
    udev_enumerate_unref(enumerate);
    return 0;
  }

  if (udev_enumerate_add_match_subsystem(enumerate, "usb") < 0)
  {
    ERR_CAUSE(data, "udev_enumerate_add_match_subsystem(usb) failed.");
    udev_enumerate_unref(enumerate);
    return 0;
  }

  if (udev_enumerate_scan_devices(enumerate) < 0)
  {
    ERR_CAUSE(data, "udev_enumerate_scan_devices() failed.");
    udev_enumerate_unref(enumerate);
    return 0;
  }

  devices = udev_enumerate_get_list_entry(enumerate);

  udev_list_entry_foreach(dev_list_entry, devices)
  {
    const char *path;
    const char *devtype;
    const char *subsystem;
    struct udev_device *dev;

    /* Get the filename of the /sys entry for the device
       and create a udev_device object (dev) representing it */
    path = udev_list_entry_get_name(dev_list_entry);

    /* An empty udev_list entry doesn't make any sense! */
    if (0 == path)
      continue;

    dev = udev_device_new_from_syspath(data->udev, path);

    /* Something is strange here. It could be because of no memory,
      or the device was disconnected in the meantime.
      If it was out of memory, we know soon enough, because we do other
      allocations that would fail.
      If it was something else, we just skip this entry.
      This leads to the situation that the hardwaremanager does not know
      about an existing device */
    if (0 == dev)
    {
      fprintf(stderr, "%s:%d: udev_device_new_from_syspath(%s) failed. Skipping entry.\n", __func__, __LINE__, path);
      continue;
    }

    devtype = udev_device_get_devtype(dev);
    subsystem = udev_device_get_subsystem(dev);

    /* list serial devices */
    if(0 != subsystem && 0 == strcmp("tty", subsystem))
    {
      /* filter out virtual tty, because this is the hardwaremanager */
      if (0 == strstr(path, "virtual"))
      {
        /* If we found an usb-serial, it is listed directly behind the parent
           usb device. Overwrite the parent with the more specific tty entry */
        if (result->current > 0
		  && TYPE_USB == result->devices[result->current - 1].type
          && 0 != strstr(path, result->devices[result->current - 1].string))
          result = arrayOverwrite(result, path, TYPE_USB);
        else
          result = arrayAdd(result, path, TYPE_TTY);
      }
    }
    /* list usb devices*/
    else if (0 != devtype && 0 == strcmp("usb_interface", devtype))
    {
      result = arrayAdd(result, path, TYPE_USB);
    }

    udev_device_unref(dev);

    /* We could not allocate the necessary memory to hold the result. Abort. */
    if (0 == result)
    {
      ERR_FIXED(data, "Could not allocate result. Abort device listing");
      break;
    }
  }

  /* Free the enumerator object */
  udev_enumerate_unref(enumerate);

  return result;
}

/* ------------------------------------------------------------------------- */
void printAncestors(struct udev_device *dev)
{
  struct udev_device *tmp;

  for(tmp = dev; tmp != 0; tmp = udev_device_get_parent(tmp))
  {
    fprintf(stderr, "%s\n", udev_device_get_sysname(tmp));
  }
}

/* ------------------------------------------------------------------------- */
struct udev_device *getNewDevice(struct NativeAccessData *data)
{
  fd_set set;
  int max;
  int result;
  struct udev_device *dev;

again:
  dev = 0;

  /* wait on select if either an event arrives or the application wants us to unblock() */
  FD_ZERO(&set);
  max = data->fdMonitor > data->fdWakeupRead
    ? data->fdMonitor : data->fdWakeupRead;

  FD_SET(data->fdMonitor, &set);
  FD_SET(data->fdWakeupRead, &set);

  /* can fail, errno */
  result = select(max + 1, &set, 0, 0, 0);

  if (0 > result)
  {
    ERR_CAUSE(data, "select() failed.");
  }
  /* received data on the monitor socket, now find out what happened */
  else
  {
    if (FD_ISSET(data->fdWakeupRead, &set))
    {
      char buf[1];

      // read from socket, so that the unblock condition is removed
      recv(data->fdWakeupRead, &buf, sizeof(buf), 0);
      dev = (struct udev_device *)&dummyDevice;
    }
    else if(FD_ISSET(data->fdMonitor, &set))
    {
      dev = udev_monitor_receive_device (data->mon);

      if (0 == dev)
      {
        ERR_CAUSE(data, "udev_monitor_receive_device() failed.");
      }
      else
      {
        /* filter out virtual tty, because this is the hardwaremanager */
        if (0 != strstr(udev_device_get_syspath(dev), "virtual"))
        {
          udev_device_unref(dev);
          goto again;
        }
      }
    }
  }
  return dev;
}

/* ------------------------------------------------------------------------- */
int getType(struct udev_device *dev)
{
  int result = TYPE_NONE;

  // is it a tty?
  if (0 == strcmp("tty", udev_device_get_subsystem(dev)))
  {
    struct udev_device *parent;

    // does it have an usb parent?
    parent = udev_device_get_parent_with_subsystem_devtype(dev, "usb", 0);

    // no usb parent, actual built-in serial port
    if (0 == parent)
      result = TYPE_TTY;
    else
      result = TYPE_USB_TTY;
  }
  // is it an usb device?
  else if (0 == strcmp("usb", udev_device_get_subsystem(dev)))
    result = TYPE_USB;

  return result;
}

/* ------------------------------------------------------------------------- */
int getEvent(struct udev_device *dev)
{
  int result = EVENT_NONE;
  const char *str;

  str = udev_device_get_action(dev);

  if (0 == strcmp("add", str))
  {
    result = EVENT_ADD;
  }
  else if (0 == strcmp("remove", str))
  {
    result = EVENT_REMOVE;
  }

  return result;
}

/* ------------------------------------------------------------------------- */
// usb:physical bus location + configuration & interface:vendor id:product id:serial number
char * getUsbIdString(struct udev_device *dev)
{
  struct udev_device *parent;
  char *result = 0;
  char *tmp;
  const char *typeString = "usb";
  const char *locationString = udev_device_get_sysname(dev);

  if (0 == dev)
    return 0;

  parent = udev_device_get_parent_with_subsystem_devtype(dev, "usb",
      "usb_device");

  if (0 == parent)
    return 0;

  // this information is located in the usb device, not the interface
  const char *vendorString = udev_device_get_sysattr_value(parent,"idVendor");
  const char *productString = udev_device_get_sysattr_value(parent, "idProduct");
  const char *serialString = udev_device_get_sysattr_value(parent, "serial");

  int typeCount;
  int locationCount;
  int vendorCount;
  int productCount;
  int serialCount = 0;
  int separatorCount;

  separatorCount = 5; // 4x ":" + trailing \0
  typeCount = strlen(typeString);

  locationCount = strlen(locationString);
  vendorCount = strlen(vendorString);
  productCount = strlen(productString);

  if (serialString)
    serialCount = strlen(serialString);

  result = malloc(separatorCount + typeCount + locationCount
    + vendorCount + productCount + serialCount);

  tmp = result;

  strcpy(tmp, typeString);
  tmp += typeCount;

  *tmp = ':';
  tmp++;

  strcpy(tmp, locationString);
  tmp += locationCount;

  *tmp = ':';
  tmp++;

  strcpy(tmp, vendorString);
  tmp += vendorCount;

  *tmp = ':';
  tmp++;

  strcpy(tmp, productString);
  tmp += productCount;

  *tmp = ':';
  tmp++;

  if (serialString)
  {
    strcpy(tmp, serialString);
    tmp += serialCount;
  }
  *tmp = '\0';

  return result;
}

/* ------------------------------------------------------------------------- */
// serial:ttyAMA0
char * getSerialIdString(struct udev_device *dev)
{
  char *result = 0;
  char *tmp;
  const char *typeString = "serial";
  const char *locationString = udev_device_get_sysname(dev);

  int separatorCount;
  int typeCount;
  int locationCount;

  separatorCount = 2; // 1x ":" + trailing \0
  typeCount = strlen(typeString);
  locationCount = strlen(locationString);

  result = malloc(separatorCount + typeCount + locationCount);

  tmp = result;

  strcpy(tmp, typeString);
  tmp += typeCount;

  *tmp = ':';
  tmp++;

  strcpy(tmp, locationString);
  tmp += locationCount;

  *tmp = '\0';

  return result;
}

/* ------------------------------------------------------------------------- */
char * getIdString(struct NativeAccessData *data, const char *syspath)
{
  struct udev_device *dev;
  char *result = 0;

  dev = udev_device_new_from_syspath(data->udev, syspath);

  // is it a tty?
  if (0 == strcmp("tty", udev_device_get_subsystem(dev)))
  {
    struct udev_device *parent;

    // does it have an usb parent?
    parent = udev_device_get_parent_with_subsystem_devtype(dev, "usb",
      "usb_interface");

    // no usb parent, actual built-in serial port
    if (0 == parent)
      result = getSerialIdString(dev);
    else
      result = getUsbIdString(parent);
  }
  // is it an usb device?
  else if (0 == strcmp("usb", udev_device_get_subsystem(dev)))
    result = getUsbIdString(dev);

  udev_device_unref(dev);

  return result;
}

/* ------------------------------------------------------------------------- */
void unblock(struct NativeAccessData *data)
{
  // unblock the waiting event thread
  // unblocking works by signalling a fd so that the thread waiting on select wakes up
  send(data->fdWakeupWrite, 0, 0, 0);
}
