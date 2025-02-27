plugins {
    id("com.gradle.develocity") version "3.18.1"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
    }
}

rootProject.name = "resourcelist-gradle-plugin"
