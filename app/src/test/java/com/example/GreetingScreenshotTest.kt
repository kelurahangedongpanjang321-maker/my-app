package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.SettingsEntity
import com.example.ui.theme.SIMPELTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val settings = SettingsEntity()
    
    composeTestRule.setContent { 
      SIMPELTheme(settings = settings) {
        androidx.compose.material3.Surface {
          androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.graphicsLayer(),
            contentAlignment = androidx.compose.ui.Alignment.Center
          ) {
            androidx.compose.material3.Text("SIMPEL: Sistem Manajemen Pelaporan")
          }
        }
      } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
