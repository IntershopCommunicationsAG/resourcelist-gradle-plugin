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
package com.intershop.gradle.resourcelist

import com.intershop.gradle.test.AbstractIntegrationSpec
import org.gradle.testkit.runner.TaskOutcome

class ProjectSpec extends AbstractIntegrationSpec {

    File settingsGradle

    def setup() {
        settingsGradle = new File(testProjectDir, 'settings.gradle')

        settingsGradle << """
            rootProject.name = 'resourcelisttest'
        """.stripIndent()
    }

    def 'Test orm resource file generation'() {
        given:
        createStandardTestOrmContent()

        buildFile << """
            plugins {
                id 'java'
                id 'com.intershop.gradle.cartridge-resourcelist'
            }

            version = '1.0.0'

            repositories {
                mavenCentral()
            }
        """.stripIndent()

        when:
        List<String> args = ['tasks', '--all', '-s']

        def result = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result.output.contains('resourceListOrm')
        result.output.contains('Resource list generation')

        when:
        List<String> jarArgs = ['jar', '-s']

        def resultJar = getPreparedGradleRunner()
                .withArguments(jarArgs)
                .withGradleVersion(gradleVersion)
                .build()

        //check resource file
        File resourceFile = new File(testProjectDir, 'build/generated/resourcelist/orm/resources/resourcelisttest/orm/orm.resource')

        boolean contentExists = true
        if(resourceFile.exists()) {
            String contentTxt = resourceFile.text
            (1..5).each {
                contentExists &= contentTxt.contains("com.corporate.build.test.file${it}")
            }
        }

        then:
        resultJar.task(":resourceListOrm").outcome == TaskOutcome.SUCCESS
        resourceFile.exists()
        contentExists

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'Test pipelet resource file generation'() {
        given:
        createStandardTestPipeletsContent()

        buildFile << """
            plugins {
                id 'java'
                id 'com.intershop.gradle.cartridge-resourcelist'
            }

            version = '1.0.0'

            repositories {
                mavenCentral()
            }
        """.stripIndent()

        when:
        List<String> args = ['tasks', '--all', '-s']

        def result = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result.output.contains('resourceListPipelets')
        result.output.contains('Resource list generation')

        when:
        List<String> jarArgs = ['jar', '-s']

        def resultJar = getPreparedGradleRunner()
                .withArguments(jarArgs)
                .withGradleVersion(gradleVersion)
                .build()

        //check resource file
        File resourceFile = new File(testProjectDir, 'build/generated/resourcelist/pipelets/resources/resourcelisttest/pipeline/pipelets.resource')

        boolean contentExists = true
        boolean addContentExists = false
        if(resourceFile.exists()) {
            String contentTxt = resourceFile.text
            (1..5).each {
                contentExists &= contentTxt.contains("com.corporate.build.pipelet.test.file${it}")
            }
            (1..5).each {
                addContentExists |= contentTxt.contains("com.corporate.build.pipelet.test.exfile${it}_de_de.xml")
            }
        }

        File jarFile = new File(testProjectDir, 'build/libs/resourcelisttest-1.0.0.jar')

        then:
        resultJar.output.contains(':resourceListPipelets')
        resourceFile.exists()
        jarFile.exists()
        contentExists
        !addContentExists

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'Test orm and pipelet resource file generation'() {
        given:
        createStandardTestOrmContent()
        createStandardTestPipeletsContent()

        buildFile << """
            plugins {
                id 'java'
                id 'com.intershop.gradle.cartridge-resourcelist'
            }

            version = '1.0.0'

            repositories {
                mavenCentral()
            }
        """.stripIndent()

        when:
        List<String> args = ['tasks', '--all', '-s']

        def result = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result.output.contains('resourceListOrm')
        result.output.contains('resourceListPipelets')
        result.output.contains('Resource list generation')

        when:
        List<String> jarArgs = ['jar', '-s']

        def resultJar = getPreparedGradleRunner()
                .withArguments(jarArgs)
                .withGradleVersion(gradleVersion)
                .build()

        //check resource file
        File resourceOrmFile = new File(testProjectDir, 'build/generated/resourcelist/orm/resources/resourcelisttest/orm/orm.resource')
        File resourcePipeletsFile = new File(testProjectDir, 'build/generated/resourcelist/pipelets/resources/resourcelisttest/pipeline/pipelets.resource')

        boolean contentExists = true
        boolean addContentExists = false
        if(resourceOrmFile.exists()) {
            String contentOrmTxt = resourceOrmFile.text
            (1..5).each {
                contentExists &= contentOrmTxt.contains("com.corporate.build.test.file${it}")
            }
        }
        if(resourcePipeletsFile.exists()) {
            String contentPipeletsTxt = resourcePipeletsFile.text
            (1..5).each {
                contentExists &= contentPipeletsTxt.contains("com.corporate.build.pipelet.test.file${it}")
            }
            (1..5).each {
                addContentExists |= contentPipeletsTxt.contains("com.corporate.build.pipelet.test.exfile${it}_de_de.xml")
            }
        }

        File jarFile = new File(testProjectDir, 'build/libs/resourcelisttest-1.0.0.jar')

        then:
        resultJar.output.contains(':resourceListOrm')
        resultJar.output.contains(':resourceListPipelets')
        resourceOrmFile.exists()
        resourcePipeletsFile.exists()
        jarFile.exists()
        contentExists
        !addContentExists

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'Test orm and pipelet resource file generation - changed'() {
        given:
        createStandardTestOrmContent()
        createStandardTestPipeletsContent()

        buildFile << """
            plugins {
                id 'java'
                id 'com.intershop.gradle.cartridge-resourcelist'
            }
            version = '1.0.0'

            repositories {
                mavenCentral()
            }
        """.stripIndent()

        when:
        List<String> jarArgs = ['jar', '-s']

        def resultJar = getPreparedGradleRunner()
                .withArguments(jarArgs)
                .withGradleVersion(gradleVersion)
                .build()

        //check resource file
        File resourceOrmFile = new File(testProjectDir, 'build/generated/resourcelist/orm/resources/resourcelisttest/orm/orm.resource')
        File resourcePipeletsFile = new File(testProjectDir, 'build/generated/resourcelist/pipelets/resources/resourcelisttest/pipeline/pipelets.resource')

        boolean contentExists = true
        boolean addContentExists = false
        if(resourceOrmFile.exists()) {
            String contentOrmTxt = resourceOrmFile.text
            (1..5).each {
                contentExists &= contentOrmTxt.contains("com.corporate.build.test.file${it}")
            }
        }
        if(resourcePipeletsFile.exists()) {
            String contentPipeletsTxt = resourcePipeletsFile.text
            (1..5).each {
                contentExists &= contentPipeletsTxt.contains("com.corporate.build.pipelet.test.file${it}")
            }
            (1..5).each {
                addContentExists |= contentPipeletsTxt.contains("com.corporate.build.pipelet.test.exfile${it}_de_de.xml")
            }
        }

        File jarFile = new File(testProjectDir, 'build/libs/resourcelisttest-1.0.0.jar')

        then:
        resultJar.output.contains(':resourceListOrm')
        resultJar.output.contains(':resourceListPipelets')
        resourceOrmFile.exists()
        resourcePipeletsFile.exists()
        jarFile.exists()
        contentExists
        !addContentExists

        when:
        (1..5).each {
            File f = file("src/main/java/com/corporate/build/test/file${it}.orm")
            f.delete()
        }

        def resultChange = getPreparedGradleRunner()
                .withArguments(jarArgs)
                .withGradleVersion(gradleVersion)
                .build()

        //check resource file
        File resourceChangedOrmFile = new File(testProjectDir, 'build/generated/resourcelist/orm/resources/resourcelisttest/orm/orm.resource')
        File resourceChangedPipeletsFile = new File(testProjectDir, 'build/generated/resourcelist/pipelets/resources/resourcelisttest/pipeline/pipelets.resource')

        if(resourcePipeletsFile.exists()) {
            String contentPipeletsTxt = resourcePipeletsFile.text
            (1..5).each {
                contentExists &= contentPipeletsTxt.contains("com.corporate.build.pipelet.test.file${it}")
            }
            (1..5).each {
                addContentExists |= contentPipeletsTxt.contains("com.corporate.build.pipelet.test.exfile${it}_de_de.xml")
            }
        }

        then:
        resultChange.output.contains(':resourceListOrm')
        resultChange.output.contains(':resourceListPipelets')
        ! resourceChangedOrmFile.exists()
        resourceChangedPipeletsFile.exists()

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'Test orm resource file generation - baseplugin'() {
        given:
        createStandardTestOrmContent()

        buildFile << """
            plugins {
                id 'java'
                id 'com.intershop.gradle.resourcelist'
            }

            version = '1.0.0'

            resourcelist {
                lists {
                    orm {
                        sourceSetName = 'main'
                        include '**/**/*.orm'
                        resourceListFilePath = "resources/\${project.name}/orm/orm.resource"
                        fileExtension = 'orm'
                    }
                }
            }

            repositories {
                mavenCentral()
            }
        """.stripIndent()

        when:
        List<String> args = ['tasks', '--all', '-s']

        def result = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result.output.contains('resourceListOrm')
        result.output.contains('Resource list generation')

        when:
        List<String> jarArgs = ['jar', '-s']

        def resultJar = getPreparedGradleRunner()
                .withArguments(jarArgs)
                .withGradleVersion(gradleVersion)
                .build()

        //check resource file
        File resourceFile = new File(testProjectDir, 'build/generated/resourcelist/orm/resources/resourcelisttest/orm/orm.resource')

        boolean contentExists = true
        if(resourceFile.exists()) {
            String contentTxt = resourceFile.text
            (1..5).each {
                contentExists &= contentTxt.contains("com.corporate.build.test.file${it}")
            }
        }

        File jarFile = new File(testProjectDir, 'build/libs/resourcelisttest-1.0.0.jar')

        then:
        resultJar.output.contains(':resourceListOrm')
        resourceFile.exists()
        jarFile.exists()
        contentExists

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'Test orm and pipelet resource file generation - baseplugin'() {
        given:
        createStandardTestOrmContent()
        createStandardTestPipeletsContent()

        buildFile << """
            plugins {
                id 'java'
                id 'com.intershop.gradle.resourcelist'
            }

            version = '1.0.0'

            resourcelist {
                lists {
                    orm {
                        sourceSetName = 'main'
                        include '**/**/*.orm'
                        resourceListFilePath = "resources/\${project.name}/orm/orm.resource"
                        fileExtension = 'orm'
                    }
                    pipelets {
                        sourceSetName = 'main'
                        include '**/pipelet/**/*.xml'
                        exclude '**/*_??_??.xml'
                        resourceListFilePath = "resources/\${project.name}/pipeline/pipelets.resource"
                        fileExtension = 'xml'
                    }
                }
            }

            repositories {
                jcenter()
            }
        """.stripIndent()

        when:
        List<String> args = ['tasks', '--all', '-s']

        def result = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result.output.contains('resourceListOrm')
        result.output.contains('resourceListPipelets')
        result.output.contains('Resource list generation')

        when:
        List<String> jarArgs = ['jar', '-s']

        def resultJar = getPreparedGradleRunner()
                .withArguments(jarArgs)
                .withGradleVersion(gradleVersion)
                .build()

        //check resource file
        File resourceOrmFile = new File(testProjectDir, 'build/generated/resourcelist/orm/resources/resourcelisttest/orm/orm.resource')
        File resourcePipeletsFile = new File(testProjectDir, 'build/generated/resourcelist/pipelets/resources/resourcelisttest/pipeline/pipelets.resource')

        boolean contentExists = true
        boolean addContentExists = false
        if(resourceOrmFile.exists()) {
            String contentOrmTxt = resourceOrmFile.text
            (1..5).each {
                contentExists &= contentOrmTxt.contains("com.corporate.build.test.file${it}")
            }
        }
        if(resourcePipeletsFile.exists()) {
            String contentPipeletsTxt = resourcePipeletsFile.text
            (1..5).each {
                contentExists &= contentPipeletsTxt.contains("com.corporate.build.pipelet.test.file${it}")
            }
            (1..5).each {
                addContentExists |= contentPipeletsTxt.contains("com.corporate.build.pipelet.test.exfile${it}_de_de.xml")
            }
        }

        File jarFile = new File(testProjectDir, 'build/libs/resourcelisttest-1.0.0.jar')

        then:
        resultJar.output.contains(':resourceListOrm')
        resultJar.output.contains(':resourceListPipelets')
        resourceOrmFile.exists()
        resourcePipeletsFile.exists()
        jarFile.exists()
        contentExists
        !addContentExists

        where:
        gradleVersion << supportedGradleVersions
    }

    void createStandardTestOrmContent() {
        writeJavaTestClass('com.corporate.build.orm.test')
        (1..5).each {
            File f = file("src/main/java/com/corporate/build/test/file${it}.orm")
            f << """TestFile ${it}
            """.stripIndent()
        }
    }

    void createStandardTestPipeletsContent() {
        writeJavaTestClass('com.corporate.build.pipelet.test')
        (1..5).each {
            File f = file("src/main/java/com/corporate/build/pipelet/test/file${it}.xml")
            f << """TestFile ${it}
            """.stripIndent()
        }
        (1..5).each {
            File f = file("src/main/java/com/corporate/build/pipelet/test/exfile${it}_de_de.xml")
            f << """TestFile ${it}
            """.stripIndent()
        }
    }
}
