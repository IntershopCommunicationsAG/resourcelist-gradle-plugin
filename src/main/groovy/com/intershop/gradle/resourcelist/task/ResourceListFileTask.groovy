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
package com.intershop.gradle.resourcelist.task

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

/**
 * Task implementation
 */
@CompileStatic
class ResourceListFileTask extends DefaultTask {

    /**
     * Returns the file extension of the resource list file.
     */
    final Property<String> fileExtension = project.objects.property(String)

    @Input
    String getFileExtension() {
        return fileExtension.get()
    }

    void setFileExtension(String fileExtension) {
        this.fileExtension.set(fileExtension)
    }

    void setFileExtension(Provider<String> fileExtension) {
        this.fileExtension.set(fileExtension)
    }

    /**
     * path of the new resource file
     */
    final Property<String> resourceListFileName = project.objects.property(String)

    @Input
    String getResourceListFileName() {
        return resourceListFileName.get()
    }

    void setResourceListFileName(String resourceListFileName) {
        this.resourceListFileName.set(resourceListFileName)
    }

    void setResourceListFileName(Provider<String> resourceListFileName) {
        this.resourceListFileName.set(resourceListFileName)
    }

    /**
     * SourceSet name for java files
     */
    final Property<String> sourceSetName = project.objects.property(String)

    @Input
    String getSourceSetName() {
        return sourceSetName.get()
    }

    void setSourceSetName(String sourceSetName) {
        this.sourceSetName.set(sourceSetName)
    }

    void setSourceSetName(Provider<String> sourceSetName) {
        this.sourceSetName.set(sourceSetName)
    }

    /**
     * Exclude filter
     */
    final ListProperty<String> excludes = project.objects.listProperty(String)

    @Input
    List<String> getExcludes() {
        return excludes.get()
    }

    void setExcludes(List<String> excludes) {
        this.excludes.set(excludes)
    }

    void setExcludes(Provider<List<String>> excludes) {
        this.excludes.set(excludes)
    }

    /**
     * Include filter
     */
    final ListProperty<String> includes = project.objects.listProperty(String)

    @Input
    List<String> getIncludes() {
        return includes.get()
    }

    void setIncludes(List<String> includes) {
        this.includes.set(includes)
    }

    void setIncludes(Provider<List<String>> includes) {
        this.includes.set(includes)
    }

    /**
     * Returns the source for this task, after the include and exclude patterns
     * have been applied. Ignores source files which do not exist.
     */
    final ConfigurableFileCollection sourceFiles = project.files()

    @InputFiles
    FileCollection getSourceFiles() {
        JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class)
        if(! javaPluginConvention) {
            throw new GradleException('It is necessary to apply a Java plugin! (The JavaPluginConvention is missing.)')
        }
        SourceSet sourceSet = javaPluginConvention.getSourceSets().findByName(getSourceSetName())

        if(sourceSet != null) {
            for(File srcdir: sourceSet.getAllJava().getSrcDirs()) {
                sourceFiles.from(project.fileTree(srcdir, new Action<ConfigurableFileTree>() {
                    @Override
                    void execute(ConfigurableFileTree files) {
                        files.setIncludes(getIncludes())
                        files.setExcludes(getExcludes())
                    }
                }).getFiles())
            }
        } else {
            throw new GradleException("The specified sourceset ${getSourceSetName()} does not exist.")
        }

        return sourceFiles
    }

    final DirectoryProperty outputDir = project.layout.directoryProperty()

    /**
     * The ouput directory of this task.
     */
    @OutputDirectory
    Directory getOutputDir() {
        return outputDir.get()
    }

    void setOutputDir(File outputDir) {
        this.outputDir.set(outputDir)
    }

    void setOutputDir(Provider<Directory> outputDir) {
        this.outputDir.set(outputDir)
    }

    @TaskAction
    void create() {
        File targetFile = getOutputDir().file(getResourceListFileName()).asFile

        project.delete(targetFile)

        //set content
        if(! getSourceFiles().isEmpty()) {
            targetFile.getParentFile().mkdirs()
            targetFile.createNewFile()

            for(File file: getSourceFiles()) {
                if(! file.isDirectory()) {
                    logger.debug('"{}" will be added.', "${file.getPath()} - \\. ${getFileExtension()}")
                    targetFile << file.path.replaceAll('\\\\', '/')\
                                                 .replaceFirst("\\." + getFileExtension() + "\$", '')\
                                                 .replaceAll("/", ".")
                    targetFile << '\n'
                }
            }
        } else {
            project.logger.info('Collection of files is empty for {}', getName())
        }
    }
}
