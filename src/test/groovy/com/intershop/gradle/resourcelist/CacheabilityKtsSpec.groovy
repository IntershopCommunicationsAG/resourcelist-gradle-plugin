package com.intershop.gradle.resourcelist

import com.intershop.gradle.test.AbstractIntegrationKotlinSpec

import java.nio.file.Files
import java.nio.file.StandardCopyOption

import static org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

/**
 * Integration tests to verify that ResourceListFileTask is properly cacheable.
 */
class CacheabilityKtsSpec extends AbstractIntegrationKotlinSpec {

    // Base build configuration for the cartridge-resourcelist plugin
    String CARTRIDGE_PLUGIN_CONFIGURATION = """
            plugins {
                `java`
                id("com.intershop.gradle.cartridge-resourcelist")
            }

            version = "1.0.0"

            repositories {
                mavenCentral()
            }
    """.stripIndent()

    // Base build configuration for the base resourcelist plugin
    String BASE_PLUGIN_CONFIGURATION = """
            plugins {
                `java`
                id("com.intershop.gradle.resourcelist")
            }

            version = "1.0.0"

            resourcelist {
                lists {
                    register("orm") {
                        sourceSetName = "main"
                        include("**/**/*.orm")
                        resourceListFileName = "\${project.name}/orm/orm.resource"
                        fileExtension = "orm"
                    }
                }
            }

            repositories {
                mavenCentral()
            }
    """.stripIndent()

    // Unique, temporary build cache directory for each test method
    File tmpBuildCacheDir

    def setup() {
        tmpBuildCacheDir = Files.createTempDirectory("gradle-build-cache-${CacheabilityKtsSpec.simpleName}-").toFile()

        settingsFile.text = """
            buildCache {
                local {
                    directory = file("${tmpBuildCacheDir.absolutePath.replace('\\', '\\\\')}")
                }
            }
            rootProject.name = "resourcelisttest"
        """.stripIndent()
    }

    def cleanup() {
        tmpBuildCacheDir?.deleteDir()
    }

    // ---------------------------------------------------------------
    // Cartridge plugin: ORM task cacheability
    // ---------------------------------------------------------------

    def 'resourceListOrm task should be cacheable (cartridge plugin)'() {
        given:
        createStandardTestOrmContent()

        buildFile << """
            ${CARTRIDGE_PLUGIN_CONFIGURATION}
        """.stripIndent()

        when: 'First build populates the build cache'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task executes successfully and result is stored in cache'
        result1.task(':resourceListOrm').outcome == SUCCESS
        ormResourceFile().exists()
        verifyOrmContent(ormResourceFile())

        when: 'Clean and rebuild using the build cache'
        def result2 = getPreparedGradleRunner()
                .withArguments('clean', 'resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task output is restored from cache'
        result2.task(':resourceListOrm').outcome == FROM_CACHE
        ormResourceFile().exists()
        verifyOrmContent(ormResourceFile())

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'resourceListPipelets task should be cacheable (cartridge plugin)'() {
        given:
        createStandardTestPipeletsContent()

        buildFile << """
            ${CARTRIDGE_PLUGIN_CONFIGURATION}
        """.stripIndent()

        when: 'First build populates the build cache'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListPipelets', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task executes successfully and result is stored in cache'
        result1.task(':resourceListPipelets').outcome == SUCCESS
        pipeletsResourceFile().exists()
        verifyPipeletsContent(pipeletsResourceFile())

        when: 'Clean and rebuild using the build cache'
        def result2 = getPreparedGradleRunner()
                .withArguments('clean', 'resourceListPipelets', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task output is restored from cache'
        result2.task(':resourceListPipelets').outcome == FROM_CACHE
        pipeletsResourceFile().exists()
        verifyPipeletsContent(pipeletsResourceFile())

        where:
        gradleVersion << supportedGradleVersions
    }

    // ---------------------------------------------------------------
    // Cartridge plugin: cache miss on input change
    // ---------------------------------------------------------------

    def 'resourceListOrm task should produce a cache miss when an input file changes'() {
        given:
        createStandardTestOrmContent()

        buildFile << """
            ${CARTRIDGE_PLUGIN_CONFIGURATION}
        """.stripIndent()

        when: 'First build populates the build cache'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task executes successfully'
        result1.task(':resourceListOrm').outcome == SUCCESS

        when: 'An input ORM file is modified'
        def ormFile = new File(testProjectDir, 'src/main/resources/com/intershop/build/test/file1.orm')
        ormFile << '\n<!-- modified -->'

        and: 'Rebuild with build cache'
        def result2 = getPreparedGradleRunner()
                .withArguments('resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task re-executes because the input changed'
        result2.task(':resourceListOrm').outcome == SUCCESS

        when: 'Input file is reverted to its original content'
        createStandardTestOrmContent()

        and: 'Rebuild with build cache after clean'
        def result3 = getPreparedGradleRunner()
                .withArguments('clean', 'resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task is loaded from cache because the original input is restored'
        result3.task(':resourceListOrm').outcome == FROM_CACHE

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'resourceListPipelets task should produce a cache miss when an input file is added'() {
        given:
        createStandardTestPipeletsContent()

        buildFile << """
            ${CARTRIDGE_PLUGIN_CONFIGURATION}
        """.stripIndent()

        when: 'First build populates the build cache'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListPipelets', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task executes successfully'
        result1.task(':resourceListPipelets').outcome == SUCCESS

        when: 'A new input XML file is added'
        File newFile = file("src/main/resources/com/intershop/build/pipelet/test/file_new.xml")
        newFile << """TestFile new
        """.stripIndent()

        and: 'Rebuild with build cache'
        def result2 = getPreparedGradleRunner()
                .withArguments('resourceListPipelets', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task re-executes because the inputs changed'
        result2.task(':resourceListPipelets').outcome == SUCCESS

        when: 'The added file is removed (revert)'
        newFile.delete()

        and: 'Rebuild with build cache after clean'
        def result3 = getPreparedGradleRunner()
                .withArguments('clean', 'resourceListPipelets', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task is loaded from cache because the original inputs are restored'
        result3.task(':resourceListPipelets').outcome == FROM_CACHE

        where:
        gradleVersion << supportedGradleVersions
    }

    // ---------------------------------------------------------------
    // Cartridge plugin: cache relocation across project directories
    // ---------------------------------------------------------------

    def 'resourceListOrm task should use cache across different project directories'() {
        given:
        createStandardTestOrmContent()

        buildFile << """
            ${CARTRIDGE_PLUGIN_CONFIGURATION}
        """.stripIndent()

        when: 'First build in the original project directory'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task executes successfully and populates the cache'
        result1.task(':resourceListOrm').outcome == SUCCESS

        when: 'A second project is created in a different directory with identical content'
        def testProjectDir2 = Files.createTempDirectory("gradle-test-project-${CacheabilityKtsSpec.simpleName}-").toFile()
        testProjectDir2.deleteOnExit()
        copyDirectory(testProjectDir, testProjectDir2)

        and: 'Build in the new directory using the shared build cache'
        def result2 = getPreparedGradleRunner()
                .withProjectDir(testProjectDir2)
                .withArguments('clean', 'resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task is restored from cache despite being in a different directory'
        result2.task(':resourceListOrm').outcome == FROM_CACHE

        cleanup:
        testProjectDir2?.deleteDir()

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'resourceListPipelets task should use cache across different project directories'() {
        given:
        createStandardTestPipeletsContent()

        buildFile << """
            ${CARTRIDGE_PLUGIN_CONFIGURATION}
        """.stripIndent()

        when: 'First build in the original project directory'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListPipelets', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task executes successfully and populates the cache'
        result1.task(':resourceListPipelets').outcome == SUCCESS

        when: 'A second project is created in a different directory with identical content'
        def testProjectDir2 = Files.createTempDirectory("gradle-test-project-${CacheabilityKtsSpec.simpleName}-").toFile()
        testProjectDir2.deleteOnExit()
        copyDirectory(testProjectDir, testProjectDir2)

        and: 'Build in the new directory using the shared build cache'
        def result2 = getPreparedGradleRunner()
                .withProjectDir(testProjectDir2)
                .withArguments('clean', 'resourceListPipelets', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task is restored from cache despite being in a different directory'
        result2.task(':resourceListPipelets').outcome == FROM_CACHE

        cleanup:
        testProjectDir2?.deleteDir()

        where:
        gradleVersion << supportedGradleVersions
    }

    // ---------------------------------------------------------------
    // Base plugin: task cacheability
    // ---------------------------------------------------------------

    def 'resourceListOrm task should be cacheable (base plugin)'() {
        given:
        createStandardTestOrmContent()

        buildFile << """
            ${BASE_PLUGIN_CONFIGURATION}
        """.stripIndent()

        when: 'First build populates the build cache'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task executes successfully and result is stored in cache'
        result1.task(':resourceListOrm').outcome == SUCCESS
        basePluginOrmResourceFile().exists()
        verifyOrmContent(basePluginOrmResourceFile())

        when: 'Clean and rebuild using the build cache'
        def result2 = getPreparedGradleRunner()
                .withArguments('clean', 'resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task output is restored from cache'
        result2.task(':resourceListOrm').outcome == FROM_CACHE
        basePluginOrmResourceFile().exists()
        verifyOrmContent(basePluginOrmResourceFile())

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'resourceListOrm task should use cache across different project directories (base plugin)'() {
        given:
        createStandardTestOrmContent()

        buildFile << """
            ${BASE_PLUGIN_CONFIGURATION}
        """.stripIndent()

        when: 'First build in the original project directory'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task executes successfully and populates the cache'
        result1.task(':resourceListOrm').outcome == SUCCESS

        when: 'A second project is created in a different directory with identical content'
        def testProjectDir2 = Files.createTempDirectory("gradle-test-project-${CacheabilityKtsSpec.simpleName}-").toFile()
        testProjectDir2.deleteOnExit()
        copyDirectory(testProjectDir, testProjectDir2)

        and: 'Build in the new directory using the shared build cache'
        def result2 = getPreparedGradleRunner()
                .withProjectDir(testProjectDir2)
                .withArguments('clean', 'resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task is restored from cache despite being in a different directory'
        result2.task(':resourceListOrm').outcome == FROM_CACHE

        cleanup:
        testProjectDir2?.deleteDir()

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'resourceListOrm task should produce a cache miss when an input file changes (base plugin)'() {
        given:
        createStandardTestOrmContent()

        buildFile << """
            ${BASE_PLUGIN_CONFIGURATION}
        """.stripIndent()

        when: 'First build populates the build cache'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task executes successfully'
        result1.task(':resourceListOrm').outcome == SUCCESS

        when: 'An input ORM file is modified'
        def ormFile = new File(testProjectDir, 'src/main/resources/com/intershop/build/test/file1.orm')
        ormFile << '\n<!-- modified -->'

        and: 'Rebuild with build cache'
        def result2 = getPreparedGradleRunner()
                .withArguments('resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task re-executes because the input changed'
        result2.task(':resourceListOrm').outcome == SUCCESS

        when: 'Input file is reverted to its original content'
        createStandardTestOrmContent()

        and: 'Rebuild with build cache after clean'
        def result3 = getPreparedGradleRunner()
                .withArguments('clean', 'resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task is loaded from cache because the original input is restored'
        result3.task(':resourceListOrm').outcome == FROM_CACHE

        where:
        gradleVersion << supportedGradleVersions
    }

    // ---------------------------------------------------------------
    // Source file wiring: include/exclude patterns
    // ---------------------------------------------------------------

    def 'excluded files should not appear in generated resource list (cartridge plugin)'() {
        given:
        createStandardTestPipeletsContent()

        buildFile << """
            ${CARTRIDGE_PLUGIN_CONFIGURATION}
        """.stripIndent()

        when: 'Build generates the pipelets resource list'
        def result = getPreparedGradleRunner()
                .withArguments('resourceListPipelets', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Excluded locale files (_de_de) are not in the resource list'
        result.task(':resourceListPipelets').outcome == SUCCESS
        pipeletsResourceFile().exists()
        String content = pipeletsResourceFile().text
        (1..5).each {
            assert content.contains("com.intershop.build.pipelet.test.file${it}")
        }
        (1..5).each {
            assert !content.contains("com.intershop.build.pipelet.test.exfile${it}_de_de")
        }

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'changing an excluded file should not cause a cache miss (cartridge pipelets)'() {
        given:
        createStandardTestPipeletsContent()

        buildFile << """
            ${CARTRIDGE_PLUGIN_CONFIGURATION}
        """.stripIndent()

        when: 'First build populates the cache'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListPipelets', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result1.task(':resourceListPipelets').outcome == SUCCESS

        when: 'Modify an excluded locale file'
        def excludedFile = new File(testProjectDir, 'src/main/resources/com/intershop/build/pipelet/test/exfile1_de_de.xml')
        excludedFile << '\n<!-- changed excluded file -->'

        and: 'Clean and rebuild'
        def result2 = getPreparedGradleRunner()
                .withArguments('clean', 'resourceListPipelets', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task is restored from cache because the excluded file is not a tracked input'
        result2.task(':resourceListPipelets').outcome == FROM_CACHE

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'adding a non-matching file should not cause a cache miss (cartridge orm)'() {
        given:
        createStandardTestOrmContent()

        buildFile << """
            ${CARTRIDGE_PLUGIN_CONFIGURATION}
        """.stripIndent()

        when: 'First build populates the cache'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result1.task(':resourceListOrm').outcome == SUCCESS

        when: 'Add a file that does not match the ORM include pattern (*.txt instead of *.orm)'
        File nonMatchingFile = file("src/main/resources/com/intershop/build/test/unrelated.txt")
        nonMatchingFile.text = "this should not affect orm task"

        and: 'Clean and rebuild'
        def result2 = getPreparedGradleRunner()
                .withArguments('clean', 'resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task is restored from cache because the non-matching file is not a tracked input'
        result2.task(':resourceListOrm').outcome == FROM_CACHE

        where:
        gradleVersion << supportedGradleVersions
    }

    // ---------------------------------------------------------------
    // Base plugin: multiple list configurations with include/exclude
    // ---------------------------------------------------------------

    def 'base plugin with orm and pipelets lists should be cacheable with correct include/exclude wiring'() {
        given:
        createStandardTestOrmContent()
        createStandardTestPipeletsContent()

        buildFile << """
            plugins {
                `java`
                id("com.intershop.gradle.resourcelist")
            }

            version = "1.0.0"

            resourcelist {
                lists {
                    register("orm") {
                        sourceSetName = "main"
                        include("**/**/*.orm")
                        resourceListFileName = "\${project.name}/orm/orm.resource"
                        fileExtension = "orm"
                    }
                    register("pipelets") {
                        sourceSetName = "main"
                        include("**/pipelet/**/*.xml")
                        exclude("**/*_??_??.xml")
                        resourceListFileName = "\${project.name}/pipeline/pipelets.resource"
                        fileExtension = "xml"
                    }
                }
            }

            repositories {
                mavenCentral()
            }
        """.stripIndent()

        when: 'First build populates the cache'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListOrm', 'resourceListPipelets', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        File ormFile = new File(testProjectDir, 'build/generated/resourcelist/orm/resourcelisttest/orm/orm.resource')
        File pipeletsFile = new File(testProjectDir, 'build/generated/resourcelist/pipelets/resourcelisttest/pipeline/pipelets.resource')

        then: 'Both tasks succeed with correct content'
        result1.task(':resourceListOrm').outcome == SUCCESS
        result1.task(':resourceListPipelets').outcome == SUCCESS
        verifyOrmContent(ormFile)
        verifyPipeletsContent(pipeletsFile)

        when: 'Clean and rebuild from cache'
        def result2 = getPreparedGradleRunner()
                .withArguments('clean', 'resourceListOrm', 'resourceListPipelets', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Both tasks restored from cache'
        result2.task(':resourceListOrm').outcome == FROM_CACHE
        result2.task(':resourceListPipelets').outcome == FROM_CACHE
        verifyOrmContent(ormFile)
        verifyPipeletsContent(pipeletsFile)

        where:
        gradleVersion << supportedGradleVersions
    }

    // ---------------------------------------------------------------
    // Custom source directories
    // ---------------------------------------------------------------

    def 'task should be cacheable with custom source directories (cartridge plugin)'() {
        given:
        createCustomTestOrmContent()

        buildFile << """
            plugins {
                `java`
                id("com.intershop.gradle.cartridge-resourcelist")
            }

            version = "1.0.0"

            sourceSets {
                main {
                    java {
                        srcDir("javasources")
                        include("**/**/*.java")
                    }
                    resources {
                        srcDir("javasources")
                        exclude("**/**/*.java")
                    }
                }
            }

            repositories {
                mavenCentral()
            }
        """.stripIndent()

        when: 'First build populates the cache'
        def result1 = getPreparedGradleRunner()
                .withArguments('resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task executes successfully with custom source dir'
        result1.task(':resourceListOrm').outcome == SUCCESS
        ormResourceFile().exists()

        when: 'Clean and rebuild from cache'
        def result2 = getPreparedGradleRunner()
                .withArguments('clean', 'resourceListOrm', '--build-cache', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then: 'Task is restored from cache'
        result2.task(':resourceListOrm').outcome == FROM_CACHE
        ormResourceFile().exists()

        where:
        gradleVersion << supportedGradleVersions
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private File ormResourceFile() {
        new File(testProjectDir, 'build/generated/resourcelist/orm/resources/resourcelisttest/orm/orm.resource')
    }

    private File pipeletsResourceFile() {
        new File(testProjectDir, 'build/generated/resourcelist/pipelets/resources/resourcelisttest/pipeline/pipelets.resource')
    }

    private File basePluginOrmResourceFile() {
        new File(testProjectDir, 'build/generated/resourcelist/orm/resourcelisttest/orm/orm.resource')
    }

    private static boolean verifyOrmContent(File resourceFile) {
        assert resourceFile.exists()
        String content = resourceFile.text
        (1..5).each {
            assert content.contains("com.intershop.build.test.file${it}")
        }
        return true
    }

    private static boolean verifyPipeletsContent(File resourceFile) {
        assert resourceFile.exists()
        String content = resourceFile.text
        (1..5).each {
            assert content.contains("com.intershop.build.pipelet.test.file${it}")
        }
        (1..5).each {
            assert !content.contains("com.intershop.build.pipelet.test.exfile${it}_de_de.xml")
        }
        return true
    }

    void createStandardTestOrmContent() {
        writeJavaTestClass('com.intershop.build.orm.test')
        (1..5).each {
            File f = file("src/main/resources/com/intershop/build/test/file${it}.orm")
            f.text = """TestFile ${it}
            """.stripIndent()
        }
    }

    void createStandardTestPipeletsContent() {
        writeJavaTestClass('com.intershop.build.pipelet.test')
        (1..5).each {
            File f = file("src/main/resources/com/intershop/build/pipelet/test/file${it}.xml")
            f.text = """TestFile ${it}
            """.stripIndent()
        }
        (1..5).each {
            File f = file("src/main/resources/com/intershop/build/pipelet/test/exfile${it}_de_de.xml")
            f.text = """TestFile ${it}
            """.stripIndent()
        }
    }

    void createCustomTestOrmContent() {
        writeJavaTestClass('com.intershop.build.orm.test')
        (1..5).each {
            File f = file("javasources/com/intershop/build/test/file${it}.orm")
            f.text = """TestFile ${it}
            """.stripIndent()
        }
    }

    private static void copyDirectory(File source, File target) {
        def sourceRoot = source.toPath()
        def targetRoot = target.toPath()

        Files.walk(sourceRoot).withCloseable { stream ->
            stream.forEach { currentPath ->
                def relativePath = sourceRoot.relativize(currentPath)
                def destinationPath = targetRoot.resolve(relativePath)
                if (Files.isDirectory(currentPath)) {
                    Files.createDirectories(destinationPath)
                } else {
                    Files.copy(currentPath, destinationPath, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }
}
