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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
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
abstract class ListConfiguration @Inject constructor(objectFactory: ObjectFactory,
                                                     layout: ProjectLayout,
                                                     val name: String) {

    private val outputDirProperty: DirectoryProperty = objectFactory.directoryProperty()
    private val excludesProperty: ListProperty<String> = objectFactory.listProperty(String::class.java)
    private val includesProperty: ListProperty<String> = objectFactory.listProperty(String::class.java)
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
    var excludes: List<String> by excludesProperty

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
    var includes: List<String> by includesProperty

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
     * Provider for selected source set property.
     */
    val sourceSetNameProvider: Provider<String>
        get() = sourceSetNameProperty

    /**
     * Generated files will be added to the source set.
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
        outputDirProperty.convention(layout.buildDirectory.
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
