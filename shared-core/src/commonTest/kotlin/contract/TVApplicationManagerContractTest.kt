package com.kmptv.shared_core.contract

import com.kmptv.shared_core.models.*
import com.kmptv.shared_core.services.TVApplicationManagerImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TVApplicationManagerContractTest {

    private fun androidTvConfig(): PlatformConfiguration = PlatformConfiguration(
        platform = Platform.AndroidTV,
        inputMethods = listOf(InputMethod.RemoteControl),
        screenResolution = Resolution(1920, 1080, "16:9"),
        supportedFormats = listOf(MediaFormat("mp4", "h264", "aac", null)),
        navigationStyle = NavigationStyle.DirectionalPad,
    )

    @Test
    fun initialize_returns_tv_application() = runTest {
        val manager = TVApplicationManagerImpl()
        val app = manager.initialize(Platform.AndroidTV, androidTvConfig())
        assertEquals(Platform.AndroidTV, app.platform)
        assertTrue(app.isInitialized)
        assertTrue(app.id.isNotEmpty())
    }

    @Test
    fun getCurrentSession_is_null_before_initialization() = runTest {
        val manager = TVApplicationManagerImpl()
        assertNull(manager.getCurrentSession())
    }

    @Test
    fun getCurrentSession_is_non_null_after_initialization() = runTest {
        val manager = TVApplicationManagerImpl()
        manager.initialize(Platform.AndroidTV, androidTvConfig())
        assertNotNull(manager.getCurrentSession())
    }

    @Test
    fun shutdown_completes_without_throwing_and_clears_session() = runTest {
        val manager = TVApplicationManagerImpl()
        manager.initialize(Platform.AndroidTV, androidTvConfig())
        manager.shutdown()
        assertNull(manager.getCurrentSession())
    }

    @Test
    fun updateConfiguration_succeeds_with_valid_config() = runTest {
        val manager = TVApplicationManagerImpl()
        manager.initialize(Platform.AndroidTV, androidTvConfig())
        val newConfig = androidTvConfig().copy(uiScaling = 1.25f)
        val result = manager.updateConfiguration(newConfig)
        assertTrue(result.isSuccess, "Expected success, got $result")
    }

    @Test
    fun updateConfiguration_fails_on_platform_mismatch() = runTest {
        val manager = TVApplicationManagerImpl()
        manager.initialize(Platform.AndroidTV, androidTvConfig())
        val appleConfig = PlatformConfiguration(
            platform = Platform.AppleTV,
            inputMethods = listOf(InputMethod.SiriRemote),
            screenResolution = Resolution(1920, 1080, "16:9"),
            supportedFormats = emptyList(),
            navigationStyle = NavigationStyle.DirectionalPad,
        )
        val result = manager.updateConfiguration(appleConfig)
        assertFalse(result.isSuccess)
    }

    @Test
    fun updateConfiguration_fails_before_initialization() = runTest {
        val manager = TVApplicationManagerImpl()
        val result = manager.updateConfiguration(androidTvConfig())
        assertFalse(result.isSuccess)
    }
}
