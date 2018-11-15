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
/**
 * Definition a key element of the OGEMA framework, the {@link Resource} interface.
 * Also defines the special resource type {@link ResourceList}, which tries to
 * mimic the resource equivalent of a Java List (with reduced functionality) and
 * the marker interface {@link SimpleResource} which identifies resources that
 * can contain a value or an array of values (note that schedules are not simple
 * resources). The {@link ModelModifiers} are framework-defined annotations that
 * are used in the modeling of new resource types. <br>
 * The sub-packages define the core resource types defined in OGEMA. The non-simple
 * models describing devices, relations, communication data, ... are not defined
 * in the OGEMA API but are contained in the separate package with the OGEMA Data Model.<br>
 */
package org.ogema.core.model;

