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

import com.intershop.gradle.resourcelist.extension.ListConfiguration
import com.intershop.gradle.resourcelist.extension.ResourceListExtension
import com.intershop.gradle.resourcelist.task.ResourceListFileTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
/**
 * Plugin implementation
 */
class ResourceListPlugin implements Plugin<Project> {

    private ResourceListExtension extension
    
    void apply (Project project) {
        project.logger.info('Create extension {} for {}', ResourceListExtension.RESOURCELIST_EXTENSION_NAME, project.name)
        extension = project.extensions.create(ResourceListExtension.RESOURCELIST_EXTENSION_NAME, ResourceListExtension, project)

        configureResourceListConfigurations(project)
    }
    
    /**
     * Configures task and default resource lists.
     */
    private void configureResourceListConfigurations(Project project) {

        extension.lists.all {ListConfiguration config ->
            ResourceListFileTask task = project.tasks.create(config.getTaskName(), ResourceListFileTask.class)
            task.group = ResourceListExtension.RESOURCELIST_TASK_GROUP

            task.conventionMapping.source = {
                String srcSourceSetName = config.getSourceSetName() ?: SourceSet.MAIN_SOURCE_SET_NAME
                SourceSet sourceSet = project.convention.getPlugin(JavaPluginConvention.class).sourceSets.findByName(srcSourceSetName)
                return project.files(sourceSet.getAllSource().getSrcDirs()).asFileTree.matching {
                    if (config.getIncludes()) {
                        include config.getIncludes()
                    }
                    if (config.getExcludes()) {
                        exclude config.getExcludes()
                    }
                }
            }
            task.conventionMapping.resourceListFilePath = { config.getResourceListFilePath() }
            task.conventionMapping.fileExtension = { config.getFileExtension() }
            task.conventionMapping.outputDirectory = { config.getOutputDir() ?: new File(project.buildDir, "generated/${ResourceListExtension.RESOURCELIST_OUTPUTPATH}/${config.getName().replace(' ', '_')}" ) }


            project.plugins.withType(JavaBasePlugin){
                //set dependencies for this task to the process resources task
                String srcSourceSetName = config.getSourceSetName() ?: SourceSet.MAIN_SOURCE_SET_NAME
                SourceSet sourceSet = project.convention.getPlugin(JavaPluginConvention.class).sourceSets.findByName(srcSourceSetName)
                if(sourceSet != null) {
                    if(! sourceSet.resources.srcDirs.contains(task.getOutputDirectory())) {
                        sourceSet.resources.srcDir(task.getOutputDirectory())
                    }
                    project.tasks.getByName(sourceSet.processResourcesTaskName).dependsOn(task)
                }
            }
        }


    }
}