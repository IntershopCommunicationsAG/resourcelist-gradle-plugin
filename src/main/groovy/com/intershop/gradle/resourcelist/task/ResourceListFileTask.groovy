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


package com.intershop.gradle.resourcelist.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.tasks.*
/**
 * Task implementation
 */
class ResourceListFileTask extends DefaultTask {

    /**
     * Returns the source for this task, after the include and exclude patterns
     * have been applied. Ignores source files which do not exist.
     *
     * @return The source.
     */
    @InputFiles
    FileTree source

    @Input
    String fileExtension

    @Input
    String resourceListFilePath

    @OutputDirectory
    File outputDirectory

    @Internal
    File getOutputFile() {
        return new File(getOutputDirectory(), getResourceListFilePath())
    }

    @TaskAction
    void create() {
        File targetFile = getOutputFile()

        setFileContent(targetFile)

        if(! targetFile.exists() ||  targetFile.text.trim() == '') {
            project.delete(getOutputDirectory().listFiles())
        }
    }

    void setFileContent(File targetFile) {
        //reset
        project.delete(targetFile)

        //set content
        FileTree filetree = getSource()
        if(! filetree.isEmpty()) {
            targetFile.getParentFile().mkdirs()
            targetFile.createNewFile()

            filetree.visit { FileVisitDetails fileDetails ->
                if (!fileDetails.directory) {
                    logger.debug('"{}" will be added.', "${fileDetails.path} - \\. ${getFileExtension()}")
                    targetFile << fileDetails.path.replaceAll('\\\\', '/')\
                                                 .replaceFirst("\\." + getFileExtension() + "\$", '')\
                                                 .replaceAll("/", ".")
                    targetFile << '\n'
                }
            }
        } else {
            project.logger.info('Filetree is empty for {}', getResourceListFilePath())
        }
    }
}
