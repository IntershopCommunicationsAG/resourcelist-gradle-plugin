/*
 * Copyright 2018 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.intershop.gradle.resourcelist.extension

import groovy.transform.CompileStatic
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

@CompileStatic
class ResourceListExtension {

    /**
     * Extension name
     */
    public final static String RESOURCELIST_EXTENSION_NAME = 'resourcelist'

    /**
     * Extension name
     */
    public final static String RESOURCELIST_OUTPUTPATH = 'resourcelist'

    /**
     * Task group name
     */
    public final static String RESOURCELIST_TASK_GROUP = 'resource list generation'

    /**
     * Container for list generation configurations
     */
    final NamedDomainObjectContainer<ListConfiguration> lists

    private Project project

    ResourceListExtension(Project project) {
        this.project = project
        lists = project.container(ListConfiguration, new ListConfigurationFactory(project))
    }

    /**
     * Container for all resource list configurations
     */
    def lists(Closure c) {
        lists.configure(c)
    }
}