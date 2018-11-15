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
 * This package contains interface that maintain the installation of OGEMA applications.
 * The InstallationManager owns the registration of different ApplicationSource implementation
 * and could provide a default implementation of this interface. Since the installation of an
 * application process can take more than one steps it manages this process and the states
 * of the application during the installation process. The status information of the application
 * that should be installed are covered by InstallableApplication.
 * 
 */
package org.ogema.core.installationmanager;

