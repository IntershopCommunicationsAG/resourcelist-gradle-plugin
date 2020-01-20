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
package com.intershop.gradle.resourcelist.extension

import com.intershop.gradle.resourcelist.utils.getValue
import com.intershop.gradle.resourcelist.utils.setValue
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import java.io.File
import javax.inject.Inject

/**
 * Configuration of a single resource list artefact.
 *
 * @constructor creates a single configuration container
 * @param name name / id of the configuration
 */
abstract class ListConfiguration(val name: String) {

    /**
     * Inject service of ObjectFactory (See "Service injection" in Gradle documentation.
     */
    @get:Inject
    abstract val objectFactory: ObjectFactory

    /**
     * Inject service of ProjectLayout (See "Service injection" in Gradle documentation.
     */
    @get:Inject
    abstract val layout: ProjectLayout

    private val outputDirProperty = objectFactory.directoryProperty()
    private val excludesProperty = objectFactory.listProperty(String::class.java)
    private val includesProperty = objectFactory.listProperty(String::class.java)
    private val sourceSetNameProperty = objectFactory.property(String::class.java)
    private val fileExtensionProperty = objectFactory.property(String::class.java)
    private val resourceListFileNameProperty = objectFactory.property(String::class.java)

    /**
     * Provider for excludes property.
     */
    val excludesProvider: Provider<MutableList<String>>
        get() = excludesProperty

    /**
     * Excludes files from input directory.
     *
     * @property excludes
     */
    var excludes by excludesProperty

    /**
     * Add an exclude configuration to the list
     * of excludes.
     *
     * @param exclude
     */
    fun exclude(exclude: String) {
        excludesProperty.add(exclude)
    }

    /**
     * Provider for include property.
     */
    val includesProvider: Provider<MutableList<String>>
        get() = includesProperty

    /**
     * Includes files from input directory.
     *
     * @property excludes
     */
    var includes by includesProperty

    /**
     * Add an include configuration to the list
     * of includes.
     *
     * @param include
     */
    fun include(include: String) {
        includesProperty.add(include)
    }

    /**
     * Provider for sourceSetName property.
     */
    val sourceSetNameProvider: Provider<String>
        get() = sourceSetNameProperty

    /**
     * The generated resources will be add to the
     * resources of the specified sourceSet.
     *
     * @property sourceSetName
     */
    var sourceSetName: String by sourceSetNameProperty

    /**
     * Provider for fileExtension property.
     */
    val fileExtensionProvider: Provider<String>
        get() = fileExtensionProperty

    /**
     * This is used for the replacement during
     * the generation. It depends on the include
     * configuration.
     *
     * @property fileExtension
     */
    var fileExtension: String by fileExtensionProperty

    /**
     * Provider for resourceListFileName property.
     */
    val resourceListFileNameProvider: Provider<String>
        get() = resourceListFileNameProperty

    /**
     * The filename of the resource list.
     *
     * @property resourceListFileName
     */
    var resourceListFileName: String by resourceListFileNameProperty

    /**
     * Provider for outputDir property.
     */
    val outputDirProvider: Provider<Directory>
        get() = outputDirProperty

    /**
     * Output dir for Schema generation from Java code.
     *
     * @property outputDir
     */
    var outputDir: File
        get() = outputDirProperty.get().asFile
        set(value) = outputDirProperty.set(value)

    init {
        outputDirProperty.convention(layout.getBuildDirectory().
                dir("${ResourceListExtension.RESOURCELIST_OUTPUTPATH}/${name.replace(' ', '_')}").get())
        sourceSetNameProperty.convention(SourceSet.MAIN_SOURCE_SET_NAME)
    }

    /**
     * Calculates the task name.
     *
     * @property taskName name with prefix
     */
    val taskName = "resourceList" + name.capitalize()
}
