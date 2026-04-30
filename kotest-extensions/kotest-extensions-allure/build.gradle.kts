plugins {
   id("kotest-jvm-conventions")
   id("kotest-publishing-conventions")
   alias(libs.plugins.allure)
}

kotlin {
   sourceSets {
      jvmMain {
         dependencies {
            implementation(libs.kotlin.reflect)
            implementation(projects.kotestFramework.kotestFrameworkEngine)
            api(libs.allure.commons)
         }
      }
      jvmTest {
         dependencies {
            implementation(libs.jackson.module.kotlin)
            implementation(projects.kotestAssertions.kotestAssertionsTable)
            implementation(projects.kotestProperty)
         }
      }
   }
}

tasks.withType<Test>().configureEach {
   exclude ("io/kotest/extensions/allure/template/**")
}

allure {
   adapter.autoconfigure.set(false)
   adapter.autoconfigureListeners.set(false)
   adapter.aspectjWeaver.set(true)
   adapter {
      frameworks {
         junit5.enabled.set(false)
      }
   }
}
