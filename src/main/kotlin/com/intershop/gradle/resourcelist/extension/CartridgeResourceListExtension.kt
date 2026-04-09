package com.intershop.gradle.resourcelist.extension

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import javax.inject.Inject

/**
 * Extension for the Cartridge Resource List plugin.
 *
 * Allows consuming projects to exclude certain directories from being processed by the plugin.
 * Entries are matched against source directory paths relative to the project directory.
 */
open class CartridgeResourceListExtension @Inject constructor(objectFactory: ObjectFactory) {

    companion object {
        /**
         * Extension name for the cartridge resource list plugin.
         */
        const val CARTRIDGE_RESOURCELIST_EXTENSION_NAME = "cartridgeResourceList"
    }

    /**
     * Directories to exclude from resource list processing.
     *
     * Supports plain relative paths and glob patterns.
     */
    val excludeDirs: ListProperty<String> = objectFactory.listProperty(String::class.java)
}
