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
 * Definition of the OGEMA text logging capabilities, which are employed using an
 * {@link OgemaLogger}. The logging is defined as an extension over the well-known
 * logging framework slf4j. It defines an additional {@link LogOutput}, the
 * cache. It is a cyclic memory constructed for debug messages that only become
 * relevant in case of an error. If an error occurs, the cache can be written to
 * disk for further analysis. Otherwise, entries in the cache will be overwritten
 * by newer entries, later.
 */
package org.ogema.core.logging;

