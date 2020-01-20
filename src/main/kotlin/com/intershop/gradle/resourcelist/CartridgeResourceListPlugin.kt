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
package com.intershop.gradle.resourcelist

import com.intershop.gradle.resourcelist.extension.ListConfiguration
import com.intershop.gradle.resourcelist.extension.ResourceListExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

/**
 *  Resourcelist plugin for orm and pipelets resource list artifacts.
 */
open class CartridgeResourceListPlugin : Plugin<Project> {

    companion object {
        /**
         * Default resourcelist configurations
         * orm resource list.
         */
        private const val RESOURCELIST_ORM_CONFIG = "orm"
        /**
         * Settings for orm resource list - include.
         */
        private const val RESOURCELIST_ORM_INCLUDE = "**/**/*.orm"
        /**
         * Settings for orm resource list - extension.
         */
        private const val RESOURCELIST_ORM_EXTENSION = "orm"
        /**
         * Default resourcelist configurations
         * pipelets resource list.
         */
        private const val RESOURCELIST_PIPELETS_CONFIG = "pipelets"
        /**
         * Settings for orm resource list - include.
         */
        private const val RESOURCELIST_PIPELETS_INCLUDE = "**/pipelet/**/*.xml"
        /**
         * Settings for orm resource list - exclude.
         */
        private const val RESOURCELIST_PIPELETS_EXCLUDE = "**/*_??_??.xml"
        /**
         * Settings for orm resource list - extension.
         */
        private const val RESOURCELIST_PIPELETS_EXTENSION = "xml"
    }

    override fun apply(project: Project) {
        // apply the base plugin
        with(project) {
            plugins.apply(ResourceListPlugin::class.java)

            plugins.withType(JavaBasePlugin::class.java) {
                val extension = project.extensions.findByType(ResourceListExtension::class.java)
                val javaPluginConvention = project.convention.getPlugin(JavaPluginConvention::class.java)

                val sourceSet = javaPluginConvention.sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME)

                if (sourceSet != null && extension != null) {
                    extension.lists.create(RESOURCELIST_PIPELETS_CONFIG) { listConfiguration: ListConfiguration ->
                        listConfiguration.resourceListFileName =
                                String.format("resources/%s/pipeline/pipelets.resource", project.name)
                        listConfiguration.fileExtension = RESOURCELIST_PIPELETS_EXTENSION
                        listConfiguration.sourceSetName = SourceSet.MAIN_SOURCE_SET_NAME
                        listConfiguration.include(RESOURCELIST_PIPELETS_INCLUDE)
                        listConfiguration.exclude(RESOURCELIST_PIPELETS_EXCLUDE)
                    }
                    extension.lists.create(RESOURCELIST_ORM_CONFIG) { listConfiguration: ListConfiguration ->
                        listConfiguration.resourceListFileName =
                                String.format("resources/%s/orm/orm.resource", project.name)
                        listConfiguration.fileExtension = RESOURCELIST_ORM_EXTENSION
                        listConfiguration.sourceSetName = SourceSet.MAIN_SOURCE_SET_NAME
                        listConfiguration.include(RESOURCELIST_ORM_INCLUDE)
                    }
                }
            }
        }
    }
}
