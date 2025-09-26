androidApplication {
    namespace = "org.example.app"

    dependencies {
        // Existing sample deps
        implementation("org.apache.commons:commons-text:1.11.0")
        implementation(project(":utilities"))

        // Jetpack Compose core libraries (Material3 UI support) with explicit versions
        implementation("androidx.activity:activity-compose:1.9.2")
        implementation("androidx.compose.ui:ui:1.7.2")
        implementation("androidx.compose.ui:ui-tooling-preview:1.7.2")
        implementation("androidx.compose.material3:material3:1.3.0")
        // Tooling and tests with explicit versions
        implementation("androidx.compose.ui:ui-tooling:1.7.2")
        implementation("androidx.compose.ui:ui-test-junit4:1.7.2")

        // Navigation for Compose
        implementation("androidx.navigation:navigation-compose:2.8.1")

        // Material Icons (extended set for Translate/Pause/Settings outlined)
        implementation("androidx.compose.material:material-icons-extended:1.7.2")

        // Room (KTX and runtime) - compiler integration deferred until KSP/KAPT is enabled in DCL
        implementation("androidx.room:room-runtime:2.6.1")
        implementation("androidx.room:room-ktx:2.6.1")

        // DataStore (Preferences)
        implementation("androidx.datastore:datastore-preferences:1.1.1")

        // Lifecycle & Coroutines helpers (explicit versions)
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    }
}
