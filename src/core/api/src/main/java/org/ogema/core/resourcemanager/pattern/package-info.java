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
 * Definitions of the pattern access to resources. An application can define a
 * pattern in the resource graph by defining a class that inherits from
 * {@link org.ogema.core.resourcemanager.pattern.ResourcePattern}. The generic
 * parameter of the pattern defines the resource type of the root node. Resource
 * fields in the defining class can the be used to define the pattern. The
 * annotations in {@link org.ogema.core.resourcemanager.pattern.ResourcePattern}
 * can be added to these fields to define the required
 * {@link org.ogema.core.resourcemanager.AccessMode} (for searching commands,
 * only) as well as optional fields in the pattern. The
 * {@link org.ogema.core.resourcemanager.pattern.ResourcePatternAccess} allows
 * to create such patterns and to register
 * {@link org.ogema.core.resourcemanager.pattern.PatternListener}s that keep the
 * application updated on all matches for the pattern that exist in the resource
 * graph.
 */
package org.ogema.core.resourcemanager.pattern;

