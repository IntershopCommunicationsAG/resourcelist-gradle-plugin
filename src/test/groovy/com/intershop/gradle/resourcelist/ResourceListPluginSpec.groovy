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
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ResourceListPluginSpec extends Specification {

    private final Project project = ProjectBuilder.builder().build()
    private final ResourceListPlugin basePlugin = new ResourceListPlugin()
    private final CartridgeResourceListPlugin plugin = new CartridgeResourceListPlugin()
    private final JavaPlugin javaPlugin = new JavaPlugin()

    def 'should add extension named resourcelist'() {
        when:
        javaPlugin.apply(project)
        basePlugin.apply(project)

        then:
        project.extensions.getByName(ResourceListExtension.RESOURCELIST_EXTENSION_NAME)
    }

    def 'should add default tasks from orm and pipelet config'() {
        when:
        javaPlugin.apply(project)
        plugin.apply(project)

        then:
        project.extensions.getByName(ResourceListExtension.RESOURCELIST_EXTENSION_NAME).lists.orm
        project.extensions.getByName(ResourceListExtension.RESOURCELIST_EXTENSION_NAME).lists.pipelets

        project.tasks.findByName('resourceListOrm')
        project.tasks.findByName('resourceListPipelets')
    }
}