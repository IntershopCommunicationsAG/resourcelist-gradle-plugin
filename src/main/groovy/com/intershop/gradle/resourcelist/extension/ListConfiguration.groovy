/*
 * Copyright 2018 Intershop Communications AG.
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
package com.intershop.gradle.resourcelist.extension

import groovy.transform.CompileStatic
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.util.GUtil

@CompileStatic
class ListConfiguration implements Named {

    final Project project

    /**
     * The object's name.
     * <p>
     * Must be constant for the life of the object.
     *
     * @return The name. Never null.
     */
    String name

    /**
     * Output path
     */
    private final DirectoryProperty outputDir

    Provider<Directory> getOutputDirProvider() {
        return outputDir
    }

    Directory getOutputDir() {
        return outputDir.get()
    }

    void setOutputDir(File outputDir) {
        this.outputDir.set(outputDir)
    }

    private final Property<String> resourceListFileName

    Provider<String> getResourceListFileNameProvider() {
        return resourceListFileName
    }

    String getResourceListFileName() {
        return resourceListFileName.get()
    }

    void setResourceListFileName(String resourceListFileName) {
        this.resourceListFileName.set(resourceListFileName)
    }

    /**
     * File extension
     */
    private final Property<String> fileExtension

    Provider<String> getFileExtensionProvider() {
        return fileExtension
    }

    String getFileExtension() {
        return fileExtension.get()
    }

    void setFileExtension(String fileExtension) {
        this.fileExtension.set(fileExtension)
    }

    /**
     * SourceSet name
     * The generated resources will be add to the
     * resources of the specified sourceSet
     */
    private final Property<String> sourceSetName

    Provider<String> getSourceSetNameProvider() {
        return sourceSetName
    }

    String getSourceSetName() {
        return sourceSetName.get()
    }

    void setSourceSetName(String sourceSetName) {
        this.sourceSetName.set(sourceSetName)
    }

    private final ListProperty<String> excludes

    /**
     * Exclude configuration
     */
    Provider<List<String>> getExcludesProvider() {
        return excludes
    }

    List<String> getExcludes() {
        return excludes.get()
    }

    void setExcludes(List<String> excludes) {
        this.excludes.set(excludes)
    }

    void exclude(String exclude) {
        excludes.add(exclude)
    }

    private final ListProperty<String> includes

    /**
     * Include configuration
     */
    Provider<List<String>> getIncludesProvider() {
        return includes
    }

    List<String> getIncludes() {
        return includes.get()
    }

    void setIncludes(List<String> includes) {
        this.includes.set(includes)
    }

    void include(String include) {
        includes.add(include)
    }

    /**
     * Constructor for named configuration
     * @param name
     */
    ListConfiguration(Project project, String name) {
        this.project = project
        this.name = name

        outputDir = project.layout.directoryProperty()
        outputDir.set(project.layout.buildDirectory.dir("generated/${ResourceListExtension.RESOURCELIST_OUTPUTPATH}/${name.replace(' ', '_')}"))

        resourceListFileName = project.objects.property(String)
        fileExtension = project.objects.property(String)

        sourceSetName = project.objects.property(String)
        sourceSetName.set(SourceSet.MAIN_SOURCE_SET_NAME)

        includes = project.objects.listProperty(String)
        excludes = project.objects.listProperty(String)
    }
    
    /**
     * Calculates the task name
     *
     * @return task name with prefix resourceList
     */
    String getTaskName() {
        return "resourceList" + GUtil.toCamelCase(name)
    }
}
