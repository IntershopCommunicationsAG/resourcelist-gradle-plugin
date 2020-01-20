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

import com.intershop.gradle.resourcelist.extension.ResourceListExtension
import com.intershop.gradle.test.AbstractProjectSpec
import org.gradle.api.Plugin
import org.gradle.api.plugins.JavaPlugin

class CartridgeResourceListPluginSpec extends AbstractProjectSpec {

    @Override
    Plugin getPlugin() {
        return new CartridgeResourceListPlugin()
    }

    def 'should add default tasks from orm and pipelet config'() {
        when:
        project.plugins.apply(JavaPlugin)
        plugin.apply(project)

        then:
        project.extensions.getByName(ResourceListExtension.RESOURCELIST_EXTENSION_NAME).lists.orm
        project.extensions.getByName(ResourceListExtension.RESOURCELIST_EXTENSION_NAME).lists.pipelets

        project.tasks.findByName('resourceListOrm')
        project.tasks.findByName('resourceListPipelets')
    }
}
