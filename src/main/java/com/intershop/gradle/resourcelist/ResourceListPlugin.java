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
package com.intershop.gradle.resourcelist;

import com.intershop.gradle.resourcelist.extension.ResourceListExtension;
import com.intershop.gradle.resourcelist.task.ResourceListFileTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.annotations.NotNull;

/**
 * Plugin implementation
 */
public class ResourceListPlugin implements Plugin<Project> {

    private ResourceListExtension extension;

    public void apply (@NotNull Project project) {
        project.getLogger().info("Create extension {} for {}", ResourceListExtension.RESOURCELIST_EXTENSION_NAME, project.getName());
        extension = project.getExtensions().findByType(ResourceListExtension.class);
        if(extension == null) {
            extension = project.getExtensions().create(ResourceListExtension.RESOURCELIST_EXTENSION_NAME, ResourceListExtension.class, project);
        }
        configureResourceListConfigurations(project);
    }

    /**
     * Configures task and default resource lists.
     */
    private void configureResourceListConfigurations(Project project) {
        extension.getLists().all(listConfiguration -> {
            if(project.getTasks().findByName(listConfiguration.getTaskName()) == null) {
                project.getTasks().create(listConfiguration.getTaskName(), ResourceListFileTask.class, resourceListFileTask -> {
                    resourceListFileTask.setGroup(ResourceListExtension.RESOURCELIST_TASK_GROUP);
                    resourceListFileTask.setDescription("Creates resource list for " + listConfiguration.getName());

                    resourceListFileTask.setFileExtension(listConfiguration.getFileExtensionProvider());
                    resourceListFileTask.setResourceListFileName(listConfiguration.getResourceListFileNameProvider());

                    resourceListFileTask.setSourceSetName(listConfiguration.getSourceSetNameProvider());
                    resourceListFileTask.setExcludes(listConfiguration.getExcludesProvider());
                    resourceListFileTask.setIncludes(listConfiguration.getIncludesProvider());

                    resourceListFileTask.setOutputDir(listConfiguration.getOutputDirProvider());

                    project.getPlugins().withType(JavaBasePlugin.class, javaBasePlugin -> {
                        JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
                        SourceSet sourceSet = javaPluginConvention.getSourceSets().findByName(listConfiguration.getSourceSetName());
                        if (sourceSet != null) {
                            if (!sourceSet.getResources().getSrcDirs().contains(resourceListFileTask.getOutputs().getFiles().getSingleFile())) {
                                sourceSet.getResources().srcDir(resourceListFileTask.getOutputs().getFiles().getSingleFile());
                            }
                            project.getTasks().getByName(sourceSet.getProcessResourcesTaskName()).dependsOn(resourceListFileTask);
                        }
                    });
                });
            }
        });
    }
}

