package com.example

import com.example.model.AspectRatioSetting
import com.example.model.CameraMode
import com.example.model.NavDestination
import com.example.model.PhotoFilter
import com.example.model.ProSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun `verify camera modes and badges`() {
    assertEquals("AI AUTO", CameraMode.AI_AUTO.title)
    assertEquals("AI 4K", CameraMode.AI_AUTO.badge)
    assertEquals("PORTRAIT", CameraMode.PORTRAIT.title)
    assertEquals("BOKEH", CameraMode.PORTRAIT.badge)
    assertEquals("NIGHT MODE", CameraMode.NIGHT.title)
    assertEquals("PRO CAMERA", CameraMode.PRO.title)
    assertEquals("MANUAL", CameraMode.PRO.badge)
    assertEquals("4K VIDEO", CameraMode.VIDEO.title)
  }

  @Test
  fun `verify navigation destinations`() {
    val destinations = NavDestination.values()
    assertEquals(5, destinations.size)
    assertTrue(destinations.contains(NavDestination.CAMERA))
    assertTrue(destinations.contains(NavDestination.PRO_MODE))
    assertTrue(destinations.contains(NavDestination.AI_STUDIO))
    assertTrue(destinations.contains(NavDestination.GALLERY))
    assertTrue(destinations.contains(NavDestination.SETTINGS))
  }

  @Test
  fun `verify pro settings default state`() {
    val settings = ProSettings()
    assertEquals("Auto", settings.iso)
    assertEquals(0.0f, settings.ev, 0.001f)
    assertEquals("Auto", settings.shutter)
    assertTrue(settings.resolution4K)
    assertTrue(settings.hdrEnabled)
    assertTrue(settings.audioEnabled)
  }

  @Test
  fun `verify photo filter presets`() {
    val filters = PhotoFilter.values()
    assertTrue(filters.contains(PhotoFilter.AI_VIBRANT))
    assertTrue(filters.contains(PhotoFilter.NIGHT_BRIGHT))
    assertTrue(filters.contains(PhotoFilter.CINEMATIC_GOLD))
    assertTrue(filters.contains(PhotoFilter.STUDIO_BOKEH))
    assertTrue(filters.contains(PhotoFilter.NOIR_DRAMA))
  }

  @Test
  fun `verify aspect ratio calculations`() {
    assertEquals(4f / 3f, AspectRatioSetting.RATIO_4_3.ratio, 0.001f)
    assertEquals(16f / 9f, AspectRatioSetting.RATIO_16_9.ratio, 0.001f)
    assertEquals(1f, AspectRatioSetting.RATIO_1_1.ratio, 0.001f)
  }
}
