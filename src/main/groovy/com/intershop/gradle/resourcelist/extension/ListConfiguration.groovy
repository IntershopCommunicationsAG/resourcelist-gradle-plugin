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


package com.intershop.gradle.resourcelist.extension

import org.gradle.api.Named
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.GUtil

class ListConfiguration extends PatternSet implements Named {

    /**
     * Constructor for named configuration
     * @param name
     */
    ListConfiguration(String name) {
        this.name = name
        this.fileExtension = name
    }

    /**
     * The object's name.
     * <p>
     * Must be constant for the life of the object.
     *
     * @return The name. Never null.
     */
    String name

    /**
     * Resource file extension.
     * The default value is the name of the object.
     */
    String fileExtension

    /**
     * Resource list file
     */
    String resourceListFilePath

    /**
     * Output path
     */
    File outputDir

    /**
     * Sources for the srcDir inputs
     */
    String sourceSetName

    /**
     * Calculates the task name
     *
     * @return task name with prefix resourceList
     */
    String getTaskName() {
        return "resourceList" + GUtil.toCamelCase(name);
    }
}
