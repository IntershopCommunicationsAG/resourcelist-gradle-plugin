/*
 * Copyright 2019 Intershop Communications AG.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intershop.gradle.resourcelist.extension

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/**
 * Main extension of the resource list plugin.
 */
open class ResourceListExtension @Inject constructor(objectFactory: ObjectFactory) {

    companion object {
        /**
         * Extension name of plugin.
         */
        const val RESOURCELIST_EXTENSION_NAME = "resourcelist"

        /**
         * Output path for all resource list tasks.
         */
        const val RESOURCELIST_OUTPUTPATH = "generated/resourcelist"
    }

    /**
     * Lists of all configuration containers for resource list artifacts.
     *
     * @property lists
     */
    val lists: NamedDomainObjectContainer<ListConfiguration> =
            objectFactory.domainObjectContainer(ListConfiguration::class.java)
}
