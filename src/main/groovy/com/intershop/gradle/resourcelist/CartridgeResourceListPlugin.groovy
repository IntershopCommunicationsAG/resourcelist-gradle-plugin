/*
 * Copyright 2015 Intershop Communications AG.
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
package com.intershop.gradle.resourcelist

import com.intershop.gradle.resourcelist.extension.ResourceListExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.SourceSet

class CartridgeResourceListPlugin implements Plugin<Project> {

    /**
     * Default resourcelist configurations
     * orm resource list
     */
    final static String RESOURCELIST_ORM_CONFIG = 'orm'

    /**
     * Settings for orm resource list - include
     */
    final static String RESOURCELIST_ORM_INCLUDE = '**/**/*.orm'

    /**
     * Settings for orm resource list - extension
     */
    final static String RESOURCELIST_ORM_EXTENSION = 'orm'

    /**
     * Default resourcelist configurations
     * pipelets resource list
     */
    final static String RESOURCELIST_PIPELETS_CONFIG = 'pipelets'
    /**
     * Settings for orm resource list - include
     */
    final static String RESOURCELIST_PIPELETS_INCLUDE = '**/pipelet/**/*.xml'

    /**
     * Settings for orm resource list - exclude
     */
    final static String RESOURCELIST_PIPELETS_EXCLUDE = '**/*_??_??.xml'

    /**
     * Settings for orm resource list - extension
     */
    final static String RESOURCELIST_PIPELETS_EXTENSION = 'xml'

    void apply (Project project) {

        // apply the base plugin
        project.plugins.apply(ResourceListPlugin)
        // add default configurations
        if (project.plugins.hasPlugin(JavaBasePlugin)) {
            ResourceListExtension extension = project.getExtensions().findByType(ResourceListExtension.class)
            if (extension != null) {
                //... for pipelets
                extension.lists.create(RESOURCELIST_PIPELETS_CONFIG) {
                    sourceSetName = SourceSet.MAIN_SOURCE_SET_NAME
                    include RESOURCELIST_PIPELETS_INCLUDE
                    exclude RESOURCELIST_PIPELETS_EXCLUDE
                    resourceListFilePath = "resources/${project.name}/pipeline/pipelets.resource"
                    fileExtension = RESOURCELIST_PIPELETS_EXTENSION
                }
                //... for orm files
                extension.lists.create(RESOURCELIST_ORM_CONFIG) {
                    sourceSetName = SourceSet.MAIN_SOURCE_SET_NAME
                    include RESOURCELIST_ORM_INCLUDE
                    resourceListFilePath = "resources/${project.name}/orm/orm.resource"
                    fileExtension = RESOURCELIST_ORM_EXTENSION
                }
            }
        }
    }
}
