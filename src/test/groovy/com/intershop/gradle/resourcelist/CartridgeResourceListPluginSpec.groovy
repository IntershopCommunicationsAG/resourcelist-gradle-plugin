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

import com.intershop.gradle.resourcelist.extension.CartridgeResourceListExtension
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
        project.tasks.findByName('resourceListOrm')
        project.tasks.findByName('resourceListPipelets')
    }

    def 'should create cartridgeResourceList extension'() {
        when:
        project.plugins.apply(JavaPlugin)
        plugin.apply(project)

        then:
        project.extensions.findByName('cartridgeResourceList') != null
        project.extensions.findByType(CartridgeResourceListExtension) != null
    }

    def 'should allow configuring excludeDirs'() {
        when:
        project.plugins.apply(JavaPlugin)
        plugin.apply(project)

        def ext = project.extensions.getByType(CartridgeResourceListExtension)
        ext.excludeDirs.add('src/main')
        ext.excludeDirs.add('build/generated/**')

        then:
        ext.excludeDirs.get().size() == 2
        ext.excludeDirs.get().contains('src/main')
        ext.excludeDirs.get().contains('build/generated/**')
    }
}
