/*
 * Copyright 2017 Intershop Communications AG.
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
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.annotations.NotNull;

public class CartridgeResourceListPlugin implements Plugin<Project> {

    /**
     * Default resourcelist configurations
     * orm resource list
     */
    private final static String RESOURCELIST_ORM_CONFIG = "orm";

    /**
     * Settings for orm resource list - include
     */
    private final static String RESOURCELIST_ORM_INCLUDE = "**/**/*.orm";

    /**
     * Settings for orm resource list - extension
     */
    private final static String RESOURCELIST_ORM_EXTENSION = "orm";

    /**
     * Default resourcelist configurations
     * pipelets resource list
     */
    private final static String RESOURCELIST_PIPELETS_CONFIG = "pipelets";
    /**
     * Settings for orm resource list - include
     */
    private final static String RESOURCELIST_PIPELETS_INCLUDE = "**/pipelet/**/*.xml";

    /**
     * Settings for orm resource list - exclude
     */
    private final static String RESOURCELIST_PIPELETS_EXCLUDE = "**/*_??_??.xml";

    /**
     * Settings for orm resource list - extension
     */
    private final static String RESOURCELIST_PIPELETS_EXTENSION = "xml";

    public void apply(@NotNull Project project) {
        // apply the base plugin
        project.getPlugins().apply(ResourceListPlugin.class);

        project.getPlugins().withType(JavaBasePlugin.class, javaBasePlugin -> {
            ResourceListExtension extension = project.getExtensions().findByType(ResourceListExtension.class);

            JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
            SourceSet sourceSet = javaPluginConvention.getSourceSets().findByName(SourceSet.MAIN_SOURCE_SET_NAME);
            if(sourceSet != null && extension != null) {
                extension.getLists().create(RESOURCELIST_PIPELETS_CONFIG, listConfiguration -> {

                    listConfiguration.setResourceListFileName(String.format("resources/%s/pipeline/pipelets.resource", project.getName()));
                    listConfiguration.setFileExtension(RESOURCELIST_PIPELETS_EXTENSION);
                    listConfiguration.setSourceSetName(SourceSet.MAIN_SOURCE_SET_NAME);

                    listConfiguration.include(RESOURCELIST_PIPELETS_INCLUDE);
                    listConfiguration.exclude(RESOURCELIST_PIPELETS_EXCLUDE);
                });
                extension.getLists().create(RESOURCELIST_ORM_CONFIG, listConfiguration -> {
                    listConfiguration.setResourceListFileName(String.format("resources/%s/orm/orm.resource", project.getName()));
                    listConfiguration.setFileExtension(RESOURCELIST_ORM_EXTENSION);
                    listConfiguration.setSourceSetName(SourceSet.MAIN_SOURCE_SET_NAME);

                    listConfiguration.include(RESOURCELIST_ORM_INCLUDE);
                });
            }
        });
    }
}

