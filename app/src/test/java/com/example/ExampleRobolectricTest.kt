package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.AspectRatio
import com.example.data.model.RecordedVideo
import com.example.data.model.Script
import com.example.data.model.TeleprompterConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var application: Application
    private lateinit var database: AppDatabase
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(application, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = MainViewModel(application)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read app name string from context`() {
        val appName = application.getString(R.string.app_name)
        assertEquals("Teleprompter Camera", appName)
    }

    @Test
    fun `calculate word count for english and hindi text`() {
        val englishText = "Hello world from mobile teleprompter app"
        assertEquals(6, Script.calculateWordCount(englishText))

        val hindiText = "नमस्ते दोस्तों आज हम बात करेंगे"
        assertEquals(6, Script.calculateWordCount(hindiText))
    }

    @Test
    fun `calculate estimated reading time`() {
        val wordCount = 130
        val duration = Script.calculateEstimatedSeconds(wordCount, wordsPerMinute = 130)
        assertEquals(60, duration)
    }

    @Test
    fun `aspect ratios resolution and parsing`() {
        val ratio916 = AspectRatio.fromId("9:16")
        assertEquals(AspectRatio.RATIO_9_16, ratio916)
        assertEquals(9f / 16f, ratio916.ratioValue, 0.001f)

        val ratio11 = AspectRatio.fromId("1:1")
        assertEquals(AspectRatio.RATIO_1_1, ratio11)
        assertEquals(1f, ratio11.ratioValue, 0.001f)

        val ratio169 = AspectRatio.fromId("16:9")
        assertEquals(AspectRatio.RATIO_16_9, ratio169)
        assertEquals(16f / 9f, ratio169.ratioValue, 0.001f)

        val ratio45 = AspectRatio.fromId("4:5")
        assertEquals(AspectRatio.RATIO_4_5, ratio45)
        assertEquals(4f / 5f, ratio45.ratioValue, 0.001f)

        val ratio34 = AspectRatio.fromId("3:4")
        assertEquals(AspectRatio.RATIO_3_4, ratio34)
        assertEquals(3f / 4f, ratio34.ratioValue, 0.001f)
    }

    @Test
    fun `script dao insert get and delete flow`() = runBlocking {
        val script = Script(
            title = "YouTube Video Script - Hindi",
            content = "नमस्ते दोस्तों! आज के इस वीडियो में हम बात करेंगे मोबाइल टेलीप्रॉम्प्टर के बारे में।"
        )
        val id = database.scriptDao().insertScript(script)
        assertTrue(id > 0)

        val retrieved = database.scriptDao().getScriptById(id)
        assertNotNull(retrieved)
        assertEquals("YouTube Video Script - Hindi", retrieved?.title)
        assertTrue(retrieved?.content?.contains("टेलीप्रॉम्प्टर") == true)

        val allScripts = database.scriptDao().getAllScripts().first()
        assertEquals(1, allScripts.size)

        database.scriptDao().deleteScript(retrieved!!)
        val emptyScripts = database.scriptDao().getAllScripts().first()
        assertEquals(0, emptyScripts.size)
    }

    @Test
    fun `video dao insert get and delete flow`() = runBlocking {
        val tempFile = File(application.cacheDir, "test_video.mp4")
        tempFile.writeText("fake video data")

        val video = RecordedVideo(
            title = "Test Vlog 9:16",
            filePath = tempFile.absolutePath,
            aspectRatio = "9:16",
            durationMs = 15000L,
            fileSizeBytes = tempFile.length(),
            resolution = "1080x1920"
        )
        val id = database.videoDao().insertVideo(video)
        assertTrue(id > 0)

        val retrieved = database.videoDao().getVideoById(id)
        assertNotNull(retrieved)
        assertEquals("Test Vlog 9:16", retrieved?.title)
        assertEquals("9:16", retrieved?.aspectRatio)

        val allVideos = database.videoDao().getAllVideos().first()
        assertEquals(1, allVideos.size)
    }

    @Test
    fun `teleprompter config default and reset logic`() {
        val defaultConfig = TeleprompterConfig.DEFAULT
        assertEquals(36f, defaultConfig.fontSizeSp)
        assertEquals(1.0f, defaultConfig.scrollSpeedMultiplier)
        assertEquals(0.75f, defaultConfig.backgroundOpacity)
        assertEquals(3, defaultConfig.countdownSeconds)
        assertFalse(defaultConfig.eyeLineGuideEnabled)

        val modified = defaultConfig.copy(
            fontSizeSp = 64f,
            scrollSpeedMultiplier = 2.0f,
            eyeLineGuideEnabled = true
        )
        viewModel.updateConfig(modified)
        val current = viewModel.teleprompterConfig.value
        assertEquals(64f, current.fontSizeSp)
        assertEquals(2.0f, current.scrollSpeedMultiplier)
        assertTrue(current.eyeLineGuideEnabled)

        viewModel.resetConfig()
        val reset = viewModel.teleprompterConfig.value
        assertEquals(36f, reset.fontSizeSp)
        assertEquals(1.0f, reset.scrollSpeedMultiplier)
    }

    @Test
    fun `view model script flow and aspect ratio selection`() {
        viewModel.setScriptFromText(
            title = "Social Reel",
            content = "5 Tips for Content Creators in 2026"
        )
        viewModel.setAspectRatio(AspectRatio.RATIO_9_16)

        val currentScript = viewModel.currentScript.value
        assertNotNull(currentScript)
        assertEquals("Social Reel", currentScript?.title)
        assertEquals("5 Tips for Content Creators in 2026", currentScript?.content)
        assertEquals(AspectRatio.RATIO_9_16, viewModel.selectedAspectRatio.value)
    }

    @Test
    fun `admin freeze control and pin authentication`() {
        // Default PIN is 1234
        assertTrue(viewModel.verifyAdminPin("1234"))
        assertFalse(viewModel.verifyAdminPin("0000"))

        // Toggle app freeze
        assertFalse(viewModel.adminConfig.value.isAppFrozen)
        viewModel.toggleAppFreeze(true, "App is under emergency maintenance")
        assertTrue(viewModel.adminConfig.value.isAppFrozen)
        assertEquals("App is under emergency maintenance", viewModel.adminConfig.value.freezeMessage)

        // Unfreeze
        viewModel.toggleAppFreeze(false)
        assertFalse(viewModel.adminConfig.value.isAppFrozen)
    }

    @Test
    fun `ads and admob monetization simulation`() {
        val defaultAdConfig = viewModel.adConfig.value
        assertTrue(defaultAdConfig.adsEnabled)
        assertTrue(defaultAdConfig.testMode)

        // Record ad impressions
        viewModel.triggerAdImpression("banner")
        viewModel.triggerAdImpression("interstitial")
        val updated = viewModel.adConfig.value
        assertTrue(updated.impressionsCount >= 2)
        assertTrue(updated.estimatedEarningsUsd > 0.0)
    }

    @Test
    fun `teleprompter custom background color and height size options`() {
        val config = TeleprompterConfig(
            backgroundColorHex = "#0F172A",
            backgroundOpacity = 0.5f,
            boxHeightFactor = 0.20f, // Chhoti
            fontSizeSp = 20f // Halka chhota
        )
        viewModel.updateConfig(config)
        val current = viewModel.teleprompterConfig.value
        assertEquals("#0F172A", current.backgroundColorHex)
        assertEquals(0.5f, current.backgroundOpacity, 0.01f)
        assertEquals(0.20f, current.boxHeightFactor, 0.01f)
        assertEquals(20f, current.fontSizeSp, 0.01f)
    }

    @Test
    fun `video recording 16_9 landscape preserves aspect ratio and resolution`() = runBlocking {
        val tempFile = File(application.cacheDir, "landscape_16_9.mp4")
        tempFile.writeText("sample 16:9 video content")

        val ratio169 = AspectRatio.RATIO_16_9
        viewModel.setAspectRatio(ratio169)
        assertEquals(AspectRatio.RATIO_16_9, viewModel.selectedAspectRatio.value)

        val video = RecordedVideo(
            title = "YouTube Tutorial 16:9",
            filePath = tempFile.absolutePath,
            aspectRatio = ratio169.id,
            durationMs = 45000L,
            fileSizeBytes = tempFile.length(),
            resolution = ratio169.recommendedResolution
        )
        val id = database.videoDao().insertVideo(video)
        val retrieved = database.videoDao().getVideoById(id)

        assertNotNull(retrieved)
        assertEquals("16:9", retrieved?.aspectRatio)
        assertEquals("1920 x 1080", retrieved?.resolution)
        assertEquals(16f / 9f, ratio169.ratioValue, 0.001f)
    }

    @Test
    fun `video recording 9_16 portrait preserves aspect ratio and resolution`() = runBlocking {
        val tempFile = File(application.cacheDir, "portrait_9_16.mp4")
        tempFile.writeText("sample 9:16 video content")

        val ratio916 = AspectRatio.RATIO_9_16
        viewModel.setAspectRatio(ratio916)
        assertEquals(AspectRatio.RATIO_9_16, viewModel.selectedAspectRatio.value)

        val video = RecordedVideo(
            title = "Instagram Reel 9:16",
            filePath = tempFile.absolutePath,
            aspectRatio = ratio916.id,
            durationMs = 30000L,
            fileSizeBytes = tempFile.length(),
            resolution = ratio916.recommendedResolution
        )
        val id = database.videoDao().insertVideo(video)
        val retrieved = database.videoDao().getVideoById(id)

        assertNotNull(retrieved)
        assertEquals("9:16", retrieved?.aspectRatio)
        assertEquals("1080 x 1920", retrieved?.resolution)
        assertEquals(9f / 16f, ratio916.ratioValue, 0.001f)
    }

    @Test
    fun `no login required to create scripts record videos or access features`() {
        // Scripts can be created freely without any authentication token or user session
        viewModel.setScriptFromText("Open Script", "No login required anywhere in the application.")
        assertNotNull(viewModel.currentScript.value)
        assertEquals("Open Script", viewModel.currentScript.value?.title)

        // Preferences and settings work directly without account
        val config = viewModel.teleprompterConfig.value
        assertNotNull(config)
    }
}

