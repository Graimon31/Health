pluginManagement {
    repositories {
        gradlePluginPortal()   // для плагинов Kotlin, Android Gradle Plugin
        google()               // для Android SDK
        mavenCentral()         // для остальных артефактов
    }
}

dependencyResolutionManagement {
    // запретим модулям объявлять свои репозитории
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyApplication"  // имя вашего проекта
include(":app")                     // подключаем модуль app
