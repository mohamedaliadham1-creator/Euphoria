package com.example

import com.example.euphoria.theme.BuiltInThemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testBuiltInThemesCount() {
    assertTrue("Should have at least 15 built-in themes", BuiltInThemes.all.size >= 15)
    assertTrue("Should contain Midnight Purple", BuiltInThemes.all.any { it.name == "Midnight Purple" })
    assertTrue("Should contain AMOLED Black", BuiltInThemes.all.any { it.name == "AMOLED Black" })
    assertTrue("Should contain Cyberpunk Neon", BuiltInThemes.all.any { it.name == "Cyberpunk Neon" })
  }
}
