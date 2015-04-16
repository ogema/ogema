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
// Title       : TestNativeAccessImpl.c
// Comment     : test for the udev only part of NativeAccessImpl
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

//#include <sys/socket.h> // socketpair
//#include <sys/types.h> // select
//#include <sys/time.h> // select
//#include <sys/un.h> // PF_UNIX
#include <stdio.h>
#include <unistd.h> // sleep
//#include <stdlib.h>
//#include <stdarg.h>
//#include <errno.h>
#include <pthread.h>

#include <libudev.h>

#include "NativeAccessImpl.h"

int printElement(struct udev_device *dev, const char *key)
{
  printf("%s:%s\n", key, udev_device_get_sysattr_value(dev, key));
  
  return 0;
}

/*
 * Class:     org_ogema_hardwaremanager_impl_NativeAccessImpl
 * Method:    getNativeUsbInfo
 * Signature: (Ljava/lang/Object;Ljava/util/Map;)Ljava/util/Map;
 */
int printUsbInfo (const char *syspath)
{
  struct udev_device *parent;
  struct udev_device *interface;
  struct udev_device *usb;
  
  char *result = 0;
  int size = 0;
  int type;

  if (0 == syspath)
    return 0;

  printf("device: %s\n", syspath);
  
  parent = udev_device_new_from_syspath(nativeAccessData.udev, syspath);
  
  if (0 == parent)
  {
    printf("Could not access syspath %s\n", syspath);
    return 0;
  }
  
  type = getType(parent);
  
  // usb interface
  if (TYPE_USB_TTY == type)
  {
    interface = udev_device_get_parent_with_subsystem_devtype(parent, "usb", 
      "usb_interface");
  } 
  else if (TYPE_USB == type)
  {
    interface = parent;
  }
  else
  {
    printf("Unkown device type\n");
    goto err;
  }
  
  if (0 == interface)
  {
    printf("Could not access usb_interface\n");
    goto err;
  }
  
  // usb device
  usb = udev_device_get_parent_with_subsystem_devtype(interface, "usb", 
      "usb_device");
  
  if (0 == usb)
  {
    printf("Could not access udev usb_device\n");
    goto err;
  }
  
  // interface number
  if (0 > printElement(interface, "bInterfaceNumber")) goto err;
  
  // interface name
  if (0 > printElement(interface, "interface")) goto err;
  
  // interface class
  if (0 > printElement(interface, "bInterfaceClass")) goto err;
  
  // interface subclass
  if (0 > printElement(interface, "bInterfaceSubClass")) goto err;
  
  // interface protocol
  if (0 > printElement(interface, "bInterfaceProtocol")) goto err;
  

  // hersteller id
  if (0 > printElement(usb, "idVendor")) goto err;
  
  // produkt id
  if (0 > printElement(usb, "idProduct")) goto err;
  
  // hersteller name
  if (0 > printElement(usb, "manufacturer")) goto err;
  
  // produkt name
  if (0 > printElement(usb, "product")) goto err;
  
  // serial
  if (0 > printElement(usb, "serial")) goto err;
  
  // aktuelle Configuration
  if (0 > printElement(usb, "bConfigurationValue")) goto err;
  
  // device class
  if (0 > printElement(usb, "bDeviceClass")) goto err;
  
  // device subclass
  if (0 > printElement(usb, "bDeviceSubClass")) goto err;
  
  // device protocol
  if (0 > printElement(usb, "bDeviceProtocol")) goto err;

err:
  udev_device_unref(parent);
  return 0;
}

/* ------------------------------------------------------------------------- */
void printDevices(struct devicesArray *devices)
{
  int i;
  
  for ( i = 0; i < devices->current; i++)
  {
    const char *string = devices->devices[i].string;
    const char *type;
    switch (devices->devices[i].type)
    {
      case TYPE_NONE:
        type = "NONE";
        break;
      case TYPE_USB:
        type = "USB";
        break;
      case TYPE_TTY:
        type = "TTY";
        break;
      case TYPE_USB_TTY:
        type = "USB_TTY";
        break;
      default:
        type = "!unknown!";
        break;
    }
    
    printf("%s %s\n", type, string);
  }
}

void testGetHandles(void)
{
  int i;
  
  printf("getHandles()\n");
  struct devicesArray *devices = getHandles(&nativeAccessData);
  
  if (0 == devices)
    printf("failed.\n");
  else
  {
    printDevices(devices);
    
    for (i = 0; i < devices->current; i++)
    {
      printf("\n\n");
      printf("ID String: %s\n",getIdString(&nativeAccessData, devices->devices[i].string));
      printUsbInfo(devices->devices[i].string);
    }
    
    arrayFree(devices);
  }
}

/* ------------------------------------------------------------------------- */
void testGetEvent(void)
{
  int i;

  for (i=0; i<4; i++)
  {
    struct udev_device *dev;
    struct udev_device *usb;
    printf("getEvent()\n");
    dev = getNewDevice(&nativeAccessData);
    
    printf("event: %s, device: %s, sysname: %s, devnode: %s devtype: %s, subsystem %s\n", 
      udev_device_get_action(dev),
      udev_device_get_syspath(dev), udev_device_get_sysname(dev), 
      udev_device_get_devnode(dev), udev_device_get_devtype(dev), 
      udev_device_get_subsystem(dev));
    
/*
    for(usb = dev; usb != 0; usb = udev_device_get_parent(usb))
    {
      printf("\tdevice: %s, sysname: %s, devnode: %s devtype: %s, subsystem %s\n", 
      udev_device_get_syspath(usb), udev_device_get_sysname(usb), 
      udev_device_get_devnode(usb), udev_device_get_devtype(usb), 
      udev_device_get_subsystem(usb));
    }
*/
    usb = udev_device_get_parent_with_subsystem_devtype(dev, "usb", "usb_device");
    if (usb)
      printf("parent: %s\n", udev_device_get_syspath(usb));
    udev_device_unref(dev);
  }
}

/* ------------------------------------------------------------------------- */
void *second_main( void *ptr )
{
  struct udev_device *dev;
  
  return getNewDevice(&nativeAccessData);
  
}

void testUnblock(void)
{
  pthread_t second;
  void *result;
  
  printf("create second thread.\n");
  // create a second thread that tries to get a new device
  if (0 > pthread_create(&second, 0, second_main, 0))
  {
    printf("pthread_create() failed.\n");
    return;
  }
  
  printf("wait for 5s.\n");
  // wait for 5s
  sleep(5);
  
  printf("unblock()\n");
  // unblock()
  unblock(&nativeAccessData);
  
  // check return value
  if (0 > pthread_join(second, &result))
  {
    printf("pthread_join() failed.\n");
    return;
  }
  
  if(result == &dummyDevice)
    printf("received dummyDevice.\n");
  else
    printf("received unexcepcted result.\n");
}

/* ------------------------------------------------------------------------- */
int main(int argc, const char *argv[])
{
  printf("initialize()\n");
  if (initialize(&nativeAccessData) < 0)
    printf("failed.\n");

  //testGetHandles();
  //testGetEvent();
  testUnblock();
  
  printf("cleanup()\n");
  cleanup(&nativeAccessData);
  
  printf("finished\n");
  return 0;
}
