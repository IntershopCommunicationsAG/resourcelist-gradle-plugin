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
package com.intershop.gradle.resourcelist.task

import com.intershop.gradle.resourcelist.utils.getValue
import com.intershop.gradle.resourcelist.utils.setValue
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * This task creates a resource list artefacts
 * from other files configured with parameters.
 */
abstract class ResourceListFileTask
    @Inject constructor(objectFactory: ObjectFactory,
                        private val fileSystemOps: FileSystemOperations) : DefaultTask() {

    private val excludesProperty = objectFactory.listProperty(String::class.java)
    private val includesProperty = objectFactory.listProperty(String::class.java)
    private val sourceSetNameProperty = objectFactory.property(String::class.java)

    private val fileExtensionProperty = objectFactory.property(String::class.java)
    private val resourceListFileNameProperty = objectFactory.property(String::class.java)

    /**
     * Output directory for file gen.
     *
     * @property outputDir
     */
    @get:OutputDirectory
    val outputDir: DirectoryProperty = objectFactory.directoryProperty()

    /**
     * List of excludes patterns for input directory.
     * @property excludes
     */
    @get:Input
    var excludes: List<String>
        get() = excludesProperty.getOrElse(listOf<String>())
        set(value) = excludesProperty.set(value)

    /**
     * Add an exclude pattern to the list of excludes.
     * @param exclude file pattern in ant style
     */
    fun exclude(exclude: String) {
        excludesProperty.add(exclude)
    }

    /**
     * This function set the excludes provider.
     */
    fun provideExcludes(excludes: Provider<MutableList<String>>) = excludesProperty.set(excludes)

    /**
     * List of includes patterns for input directory.
     *
     * @property includes
     */
    @get:Input
    var includes: List<String>
        get() = includesProperty.getOrElse(listOf<String>())
        set(value) = includesProperty.set(value)

    /**
     * Add an include pattern to the list of includes.
     * @param include file pattern in ant style
     */
    fun include(include: String) {
        includesProperty.add(include)
    }

    /**
     * This function set the includes provider.
     */
    fun provideIncludes(includes: Provider<MutableList<String>>) =
            includesProperty.set(includes)

    /**
     * File extension is used for the creation of the
     * resource list from resource files.
     *
     * @property fileExtension
     */
    @get:Input
    var fileExtension: String by fileExtensionProperty

    /**
     * This function set the fileExtension provider.
     */
    fun provideFileExtension(fileExtension: Provider<String>) =
            fileExtensionProperty.set(fileExtension)

    /**
     * This is the filename of the artifact with
     * the created resource list.
     *
     * @property resourceListFileName
     */
    @get:Input
    var resourceListFileName: String by resourceListFileNameProperty

    /**
     * This function set the resourceListFileName provider.
     */
    fun provideResourceListFileName(resourceListFileName: Provider<String>) =
            resourceListFileNameProperty.set(resourceListFileName)

    /**
     * This is the name of the source set with
     * the resource files for resource list.
     *
     * @property sourceSetName
     */
    @get:Input
    var sourceSetName: String by sourceSetNameProperty

    /**
     * This function set the sourceSetName provider.
     */
    fun provideSourceSetName(sourceSetName: Provider<String>) =
            sourceSetNameProperty.set(sourceSetName)

    /**
     * This is the set of source paths of resources
     * for the resource list (read only).
     *
     * @property sourcePaths
     */
    @get:Input
    val sourcePaths: Set<String> by lazy {
        val setFilePaths = hashSetOf<String>()
        try {
            val java = project.extensions.getByType(JavaPluginExtension::class.java)
            val srcset = java.sourceSets.findByName(sourceSetName)

            if(srcset != null) {
                (srcset.resources.srcDirs + srcset.allSource.srcDirs).forEach { srcDir ->
                    project.fileTree(srcDir) {
                        it.setIncludes(includes)
                        it.setExcludes(excludes)
                    }.files.filter { !it.isDirectory }.forEach { file ->
                        setFilePaths.add(file.path.substring(srcDir.path.length + 1))
                    }
                }
            }
        } catch(ex: IllegalStateException) {
            throw GradleException("It is necessary to apply a Java plugin! (The JavaPluginConvention is missing.)")
        }

        setFilePaths
    }

    /**
     * This is logic of the task, that creates
     * the resource list artifact.
     */
    @TaskAction
    fun create() {
        val targetFile = File(outputDir.get().asFile, resourceListFileName)

        if(targetFile.exists()) {
            fileSystemOps.delete {
                it.delete(targetFile)
            }
        }

        try {
            //set content
            if (sourcePaths.isNotEmpty()) {
                targetFile.parentFile.mkdirs()
                targetFile.createNewFile()
                File(targetFile.absolutePath).printWriter().use {out ->
                    sourcePaths.forEach {
                        val entry = it.replace("\\", "/").replaceFirst(".${fileExtension}", "").replace("/", ".")
                        if (logger.isDebugEnabled) {
                            logger.debug("'{}' will be added to list.", entry)
                        }

                        out.println(entry)
                    }
                }
            } else {
                project.logger.info("Collection of files is empty for {}", project.name)
            }
        } catch (ex: IOException) {
            throw GradleException("File operation for ${this.name} failed (${ex.message}).")
        }
    }
}
