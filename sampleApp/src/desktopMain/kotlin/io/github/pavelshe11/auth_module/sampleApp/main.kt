package io.github.pavelshe11.auth_module.sampleApp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Messanger",
    ) {
//        RootUI()
    }
}