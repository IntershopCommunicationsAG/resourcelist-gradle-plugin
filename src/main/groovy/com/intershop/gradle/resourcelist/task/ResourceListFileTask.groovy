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
import org.gradle.api.file.FileTreeElement
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet

/**
 * Task implementation
 */
class ResourceListFileTask extends DefaultTask implements PatternFilterable {

    private final List<Object> source = new ArrayList<Object>()
    private final PatternFilterable patternSet = new PatternSet()

    /**
     * Sets the source for this task. The given source object is evaluated
     * as per {@link org.gradle.api.Project#files(Object...)}.
     *
     * @param source The source.
     */
    public void setSource(Object source) {
        this.source.clear()
        this.source.add(source)
    }

    /**
     * Returns the source for this task, after the include and exclude patterns
     * have been applied. Ignores source files which do not exist.
     *
     * @return The source.
     */
    @InputFiles
    public FileTree getSource() {
        FileTree src = getProject().files(source).getAsFileTree()
        return src == null ? getProject().files().getAsFileTree() : src.matching(patternSet)
    }

    @Input
    String fileExtension
    
    @Input
    String resourceListFilePath

    @OutputDirectory
    File outputDirectory

    @TaskAction
    void create(IncrementalTaskInputs inputs) {
        if (!inputs.incremental) {
            project.delete(getOutputDirectory().listFiles())
        }

        inputs.outOfDate { change ->
            setFileContent(new File(getOutputDirectory(), getResourceListFilePath()))
        }

        inputs.removed { change ->
            project.delete(new File(getOutputDirectory(), getResourceListFilePath()))
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
        }
    }

    /**
     * Adds some source to this task. The given source objects will be evaluated as
     * per {@link org.gradle.api.Project#files(Object...)}.
     *
     * @param sources The source to add
     * @return this
     */
    public void source(Object... sources) {
        sources.each {
            this.source.add(it)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Set<String> getIncludes() {
        return patternSet.getIncludes()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Set<String> getExcludes() {
        return patternSet.getIncludes()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable setIncludes(Iterable<String> includes) {
        patternSet.setIncludes(includes)
        return this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable setExcludes(Iterable<String> excludes) {
        patternSet.setExcludes(excludes)
        return this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable include(String... includes) {
        patternSet.include(includes)
        return this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable include(Iterable<String> includes) {
        patternSet.include(includes)
        return this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable include(Spec<FileTreeElement> includeSpec) {
        patternSet.include(includeSpec)
        return this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable include(Closure includeSpec) {
        patternSet.include(includeSpec)
        return this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable exclude(String... excludes) {
        patternSet.exclude(excludes)
        return this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable exclude(Iterable<String> excludes) {
        patternSet.exclude(excludes)
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable exclude(Spec<FileTreeElement> excludeSpec) {
        patternSet.exclude(excludeSpec)
        return this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable exclude(Closure excludeSpec) {
        patternSet.exclude(excludeSpec)
        return this
    }
}
