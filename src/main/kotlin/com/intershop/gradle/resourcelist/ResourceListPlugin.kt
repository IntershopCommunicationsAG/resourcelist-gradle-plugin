/*
 * Copyright 2018 Intershop Communications AG.
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
import com.intershop.gradle.resourcelist.task.ResourceListFileTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention

/**
 *  Basis plugin implementation of the resource list plugin.
 */
open class ResourceListPlugin : Plugin<Project> {

    companion object {
        /**
         * Description for main task.
         */
        const val TASKDESCRIPTION = "Creates resource list for "

        /**
         * Group for all resource list plugins.
         */
        const val RESOURCELIST_TASK_GROUP = "resource list generation"
    }

    override fun apply(project: Project) {
        with(project) {
            logger.info("Create extension {} for {}", ResourceListExtension.RESOURCELIST_EXTENSION_NAME, name)

            val extension = extensions.findByType(
                    ResourceListExtension::class.java
            ) ?: extensions.create( ResourceListExtension.RESOURCELIST_EXTENSION_NAME,
                    ResourceListExtension::class.java)

            configureResourceListConfigurations(this, extension)
        }
    }

    /**
     * Configures task and default resource lists.
     */
    private fun configureResourceListConfigurations(project: Project, extension: ResourceListExtension) {
        extension.lists.all { listConfiguration: ListConfiguration ->
            with(project) {
                plugins.withType(JavaBasePlugin::class.java) {
                    val javaPluginConvention = convention.getPlugin(JavaPluginConvention::class.java)
                    javaPluginConvention.sourceSets.matching {
                        it.name == listConfiguration.sourceSetName
                    }.forEach {sourceSet ->
                        with(listConfiguration) {
                            val rtask = tasks.register(taskName, ResourceListFileTask::class.java) { task ->
                                task.description = TASKDESCRIPTION + name
                                task.group = RESOURCELIST_TASK_GROUP

                                task.provideFileExtension(fileExtensionProvider)
                                task.provideResourceListFileName(resourceListFileNameProvider)
                                task.provideSourceSetName(sourceSetNameProvider)
                                task.provideExcludes(excludesProvider)
                                task.provideIncludes(includesProvider)
                                task.outputDir.set((outputDirProvider))

                                sourceSet.resources.srcDir(task.outputs)
                            }

                            tasks.named(sourceSet.processResourcesTaskName).configure {
                                it.dependsOn(rtask)
                            }
                        }
                        return@forEach
                    }
                }
            }
        }
    }
}
