package com.example

import android.Manifest
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.CameraCapture
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CameraCaptureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCameraPermissionCardShownWhenPermissionNotGranted() {
        composeTestRule.setContent {
            MyApplicationTheme {
                CameraCapture(
                    onPhotoCaptured = { _, _ -> },
                    onClose = { },
                    onPickFromGallery = { }
                )
            }
        }

        // When CAMERA permission is not pre-granted, CameraCapture displays the permission explanation & grant button
        composeTestRule.onNodeWithTag("grant_camera_permission_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Camera Access Needed").assertIsDisplayed()
    }
}
