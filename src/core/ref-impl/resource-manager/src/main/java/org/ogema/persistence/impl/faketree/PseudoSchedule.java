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
package org.ogema.persistence.impl.faketree;

import org.ogema.core.model.Resource;

/**
 * Pseudo resource type required to create a sub-tree-element holding the
 * schedule extra information.
 *
 * @deprecated not used anymore, but must not be removed either, since 
 * existing database files containing this type could not be loaded any more,
 * otherwise 
 * @author Timo Fischer, Fraunhofer IWES
 */
@Deprecated
interface PseudoSchedule extends Resource {
}
