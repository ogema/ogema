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
/* ============================================================================
//               Copyright 2013
//               Fraunhofer-Gesellschaft
//               Institute for Integrated Circuits
//               All rights reserved
//
//               Proprietary and Confidential Information
//
// Title       : org_ogema_hardwaremanager_rpi_NativeAccessImpl.c
// Comment     : JNI part of org.ogema.hardwaremanager.rpi.NativeAccessImpl
// Assumptions :
// Limitations :
// ----------------------------------------------------------------------------
// Modification History:
// ----------------------------------------------------------------------------
// Notes:
// ----------------------------------------------------------------------------
// $Id: $
//
// ========================================================================= */

#include <stdlib.h>
#include <libudev.h>
#include <jni.h>

#include "NativeAccessImpl.h"
#include "org_ogema_hardwaremanager_rpi_NativeAccessImpl.h"
#include "org_ogema_hardwaremanager_api_Container.h"

/* make sure the values match */
#if org_ogema_hardwaremanager_api_Container_EVENT_NONE != EVENT_NONE
#error "org_ogema_hardwaremanager_api_Container_EVENT_NONE != EVENT_NONE"
#endif

#if org_ogema_hardwaremanager_api_Container_EVENT_ADD != EVENT_ADD
#error "org_ogema_hardwaremanager_api_Container_EVENT_ADD != EVENT_ADD"
#endif

#if org_ogema_hardwaremanager_api_Container_EVENT_REMOVE != EVENT_REMOVE
#error "org_ogema_hardwaremanager_api_Container_EVENT_REMOVE != EVENT_REMOVE"
#endif

#if org_ogema_hardwaremanager_api_Container_TYPE_USB != TYPE_USB
#error "org_ogema_hardwaremanager_api_Container_TYPE_USB != TYPE_USB"
#endif

#if org_ogema_hardwaremanager_api_Container_TYPE_SERIAL != TYPE_TTY
#define "org_ogema_hardwaremanager_api_Container_TYPE_SERIAL != TYPE_TTY"
#endif

/* ------------------------------------------------------------------------- */
jclass containerClass;
jclass exceptionClass;
jfieldID containerHandle;
jfieldID containerType;
jfieldID containerEvent;
jmethodID containerConstructor;
jmethodID mapPut;

/* ------------------------------------------------------------------------- */
void
throwException(JNIEnv *env, const char *msg)
{
  (*env)->ThrowNew(env, exceptionClass, msg);
}

void
throwNPException(JNIEnv *env, const char *msg)
{
  jclass clazz;

  clazz = (*env)->FindClass(env, "java/lang/NullPointerException");

  if (0 == clazz)
    return;

  (*env)->ThrowNew(env, clazz, msg);
}
/* ------------------------------------------------------------------------- */
JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM *jvm, void *reserved)
{
  JNIEnv *env;

  cleanup(&nativeAccessData);

  if (0 > (*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_2))
    return;

  if (0 != containerClass)
    (*env)->DeleteWeakGlobalRef(env, containerClass);
  if (0 != exceptionClass)
    (*env)->DeleteWeakGlobalRef(env, exceptionClass);
}

/* ------------------------------------------------------------------------- */
/* Initialize libudev and JNI specific stuff
   Throws an Exception if anything went wrong. */
JNIEXPORT void JNICALL Java_org_ogema_hardwaremanager_rpi_NativeAccessImpl_initialize
  (JNIEnv *env, jobject thiz)
{
  jclass tmp;

  tmp = (*env)->FindClass(env, "java/lang/Exception");
  if (0 == tmp)
    return;

  exceptionClass = (*env)->NewWeakGlobalRef(env, tmp);
  if((*env)->ExceptionCheck(env))
    return;

  tmp = (*env)->FindClass(env, "org/ogema/hardwaremanager/api/Container");
  if (0 == tmp)
    return;

  containerClass = (*env)->NewWeakGlobalRef(env, tmp);
  if((*env)->ExceptionCheck(env))
    return;

  containerConstructor = (*env)->GetMethodID(env, containerClass, "<init>",
    "(Ljava/lang/Object;II)V");
  if((*env)->ExceptionCheck(env))
    return;

  containerHandle = (*env)->GetFieldID(env, containerClass, "handle",
    "Ljava/lang/Object;");
  if((*env)->ExceptionCheck(env))
    return;

  containerType = (*env)->GetFieldID(env, containerClass, "type", "I");
  if((*env)->ExceptionCheck(env))
    return;

  containerEvent = (*env)->GetFieldID(env, containerClass, "event", "I");
  if((*env)->ExceptionCheck(env))
    return;

  tmp = (*env)->FindClass(env, "java/util/Map");
  if (0 == tmp)
    return;

  mapPut = (*env)->GetMethodID(env, tmp, "put",
    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
  if((*env)->ExceptionCheck(env))
    return;

  if (0 > initialize(&nativeAccessData))
  {
    throwException(env, nativeAccessData.errorbuf);
    return;
  }
}

/* ------------------------------------------------------------------------- */
/* Store all currently available devices (syspath) into an Container[]
   and report it to Java.
   Throws an Exception if anything went wrong. */
JNIEXPORT jobjectArray JNICALL Java_org_ogema_hardwaremanager_rpi_NativeAccessImpl_getHandles
  (JNIEnv *env, jobject thiz)
{
  struct devicesArray *array;
  jobjectArray result = 0;
  jsize i;

  nativeAccessData.errorbuf[0] = '\0';

  array = getHandles(&nativeAccessData);

  /* something went wrong */
  if (0 == array)
  {
    throwException(env, nativeAccessData.errorbuf);
    return 0;
  }

  /* allocate the array of containers */
  result = (*env)->NewObjectArray(env, array->current, containerClass, 0);

  if (0 != result)
  {
    /* create and add each individual container object */
    for (i = 0; i < array->current; i++)
    {
      jobject container;
      jstring str;

      str = (*env)->NewStringUTF(env, array->devices[i].string);

      if (0 == str)
        break;

      container = (*env)->NewObject(env, containerClass, containerConstructor,
        str, array->devices[i].type, EVENT_NONE);

      if (0 == container)
        break;

      (*env)->SetObjectArrayElement(env, result, i, container);
      (*env)->DeleteLocalRef(env, container);
      (*env)->DeleteLocalRef(env, str);

      if ((*env)->ExceptionCheck(env))
        break;
    }
  }

  arrayFree(array);
  return result;
}

/* ------------------------------------------------------------------------- */
/* Block until a Device is connected/disconnected to the system.
   Return device/devicetype/action as a Container.
   Throws an Exception if anything went wrong. */
JNIEXPORT jobject JNICALL Java_org_ogema_hardwaremanager_rpi_NativeAccessImpl_getEvent
  (JNIEnv *env, jobject thiz, jobject container)
{
  struct udev_device *dev;
  jstring str;
  int type;
  int event;
  const char *syspath;

  if (0 == container)
  {
    throwNPException(env, "container == null");
    return 0;
  }

  nativeAccessData.errorbuf[0] = '\0';

  dev = getNewDevice(&nativeAccessData);

  if (0 == dev)
  {
     throwException(env, nativeAccessData.errorbuf);
  }
  // unblock has been called, return
  else if ((struct udev_device *)&dummyDevice == dev)
  {
    (*env)->SetObjectField(env, container, containerHandle, 0);
    (*env)->SetIntField(env, container, containerType, TYPE_NONE);
    (*env)->SetIntField(env, container, containerEvent, EVENT_NONE);
  }
  else
  {
    type = getType(dev);

    if (TYPE_USB_TTY == type)
      type = TYPE_USB;

    syspath = udev_device_get_syspath(dev);
    event = getEvent(dev);

    str = (*env)->NewStringUTF(env, syspath);

    if (0 == str)
    {
      udev_device_unref(dev);
      return 0;
    }

    (*env)->SetObjectField(env, container, containerHandle, str);
    (*env)->SetIntField(env, container, containerType, type);
    (*env)->SetIntField(env, container, containerEvent, event);

    udev_device_unref(dev);
  }

  return container;
}

/* ------------------------------------------------------------------------- */
/* Wake up thread waiting in getEvent(). */
JNIEXPORT void JNICALL Java_org_ogema_hardwaremanager_rpi_NativeAccessImpl_unblock
  (JNIEnv *env, jobject thiz)
{
  unblock(&nativeAccessData);
}

/* ------------------------------------------------------------------------- */
/* Return id string as used by Java. */
JNIEXPORT jstring JNICALL Java_org_ogema_hardwaremanager_rpi_NativeAccessImpl_getIdString
  (JNIEnv *env, jobject thiz, jobject handle)
{
  char *id;
  const char *syspath;
  jstring result;

  if (0 == handle)
  {
    throwNPException(env, "handle == null");
    return 0;
  }

  syspath = (const char *)(*env)->GetStringUTFChars(env, handle, NULL);

  if (0 == syspath)
    return 0; /* OutOfMemoryError already thrown */

  id = getIdString(&nativeAccessData, syspath);
  (*env)->ReleaseStringUTFChars(env, handle, syspath);
  result = (*env)->NewStringUTF(env, id);
  free(id);
  return result;
}

/* ------------------------------------------------------------------------- */
/* Return port id (dev/XXX or COMXXX) if available. */
JNIEXPORT jstring JNICALL Java_org_ogema_hardwaremanager_rpi_NativeAccessImpl_getPortString
  (JNIEnv *env, jobject thiz, jobject handle)
{
  struct udev_device *dev;
  const char *devnode;
  const char *syspath;
  jstring result;

  if (0 == handle)
  {
    throwNPException(env, "handle == null");
    return 0;
  }

  syspath = (const char *)(*env)->GetStringUTFChars(env, handle, NULL);

  if (0 == syspath)
    return 0; /* OutOfMemoryError already thrown */

  dev = udev_device_new_from_syspath(nativeAccessData.udev, syspath);
  (*env)->ReleaseStringUTFChars(env, handle, syspath);
  devnode = udev_device_get_devnode(dev);
  result = (*env)->NewStringUTF(env, devnode);
  udev_device_unref(dev);
  return result;
}

/* ------------------------------------------------------------------------- */
int addToMap(JNIEnv *env, jobject map, struct udev_device *dev, const char *key)
{
  int result = 0;
  jstring jkey;
  jstring jvalue;
  const char *value;

  value = udev_device_get_sysattr_value(dev, key);

  jkey = (*env)->NewStringUTF(env, key);

  if ((*env)->ExceptionCheck(env))
    return -1;

  jvalue = (*env)->NewStringUTF(env, value);

  if ((*env)->ExceptionCheck(env))
    return -1;

  // map.put(key, value)
  (*env)->CallObjectMethod(env, map, mapPut, jkey, jvalue);
  (*env)->DeleteLocalRef(env, jvalue);
  (*env)->DeleteLocalRef(env, jkey);

  if ((*env)->ExceptionCheck(env))
    result = -1;

  return result;
}

/* ------------------------------------------------------------------------- */
/* Return map of USB properties. */
JNIEXPORT jobject JNICALL Java_org_ogema_hardwaremanager_rpi_NativeAccessImpl_getNativeUsbInfo
  (JNIEnv *env, jobject thiz, jobject handle, jobject map)
{
  struct udev_device *parent;
  struct udev_device *interface;
  struct udev_device *usb;

  const char *syspath;
  int type;

  if (0 == handle)
  {
    throwNPException(env, "handle == null");
    return 0;
  }

  if (0 == map)
  {
    throwNPException(env, "map == null");
    return 0;
  }

  syspath = (*env)->GetStringUTFChars(env, handle, NULL);

  if (0 == syspath)
    return 0;

  parent = udev_device_new_from_syspath(nativeAccessData.udev, syspath);

  (*env)->ReleaseStringUTFChars(env, handle, syspath);

  if (0 == parent)
  {
    throwException(env, "Could not access udev device");
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
    throwException(env, "Unkown device type");
    goto err;
  }

  // usb device
  usb = udev_device_get_parent_with_subsystem_devtype(interface, "usb",
      "usb_device");

  if (0 == usb)
  {
    throwException(env, "Could not access udev usb_device");
    goto err;
  }

  // interface number
  if (0 > addToMap(env, map, interface, "bInterfaceNumber")) goto err;

  // interface class
  if (0 > addToMap(env, map, interface, "bInterfaceClass")) goto err;

  // interface subclass
  if (0 > addToMap(env, map, interface, "bInterfaceSubClass")) goto err;

  // interface protocol
  if (0 > addToMap(env, map, interface, "bInterfaceProtocol")) goto err;

  // interface name
  if (0 > addToMap(env, map, interface, "interface")) goto err;


  // hersteller id
  if (0 > addToMap(env, map, usb, "idVendor")) goto err;

  // produkt id
  if (0 > addToMap(env, map, usb, "idProduct")) goto err;

  // hersteller name
  if (0 > addToMap(env, map, usb, "manufacturer")) goto err;

  // produkt name
  if (0 > addToMap(env, map, usb, "product")) goto err;

  // serial
  if (0 > addToMap(env, map, usb, "serial")) goto err;

  // aktuelle Configuration
  if (0 > addToMap(env, map, usb, "bConfigurationValue")) goto err;

  // device class
  if (0 > addToMap(env, map, usb, "bDeviceClass")) goto err;

  // device subclass
  if (0 > addToMap(env, map, usb, "bDeviceSubClass")) goto err;

  // device protocol
  if (0 > addToMap(env, map, usb, "bDeviceProtocol")) goto err;

err:
  udev_device_unref(parent);
  return map;
}
