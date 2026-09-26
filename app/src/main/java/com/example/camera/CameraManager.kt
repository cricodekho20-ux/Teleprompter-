package com.example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

sealed class RecordingStatus {
    object Idle : RecordingStatus()
    data class Recording(val durationMs: Long, val isPaused: Boolean) : RecordingStatus()
    data class Success(val file: File, val uri: Uri, val durationMs: Long, val galleryUri: Uri? = null) : RecordingStatus()
    data class Error(val message: String) : RecordingStatus()
}

class CameraManager(private val context: Context) {

    private val TAG = "CameraManager"

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null

    var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
        private set

    val isFrontCamera: Boolean
        get() = lensFacing == CameraSelector.LENS_FACING_FRONT

    var isTorchOn: Boolean = false
        private set

    var isAudioEnabled: Boolean = true
    private var currentAspectRatio: com.example.data.model.AspectRatio = com.example.data.model.AspectRatio.RATIO_9_16

    fun initCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        selectedAspectRatio: com.example.data.model.AspectRatio = com.example.data.model.AspectRatio.RATIO_9_16,
        resolutionQuality: String = "1080p",
        useFrontCamera: Boolean = true,
        onInitialized: (Boolean) -> Unit = {}
    ) {
        lensFacing = if (useFrontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        currentAspectRatio = selectedAspectRatio

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(lifecycleOwner, previewView, selectedAspectRatio, resolutionQuality)
                onInitialized(true)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                onInitialized(false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        selectedAspectRatio: com.example.data.model.AspectRatio,
        resolutionQuality: String
    ) {
        val provider = cameraProvider ?: return
        currentAspectRatio = selectedAspectRatio

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val targetRatio = when (selectedAspectRatio) {
            com.example.data.model.AspectRatio.RATIO_16_9,
            com.example.data.model.AspectRatio.RATIO_9_16 -> androidx.camera.core.AspectRatio.RATIO_16_9
            else -> androidx.camera.core.AspectRatio.RATIO_4_3
        }

        preview = Preview.Builder()
            .setTargetAspectRatio(targetRatio)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val quality = when (resolutionQuality.lowercase()) {
            "4k" -> Quality.UHD
            "720p" -> Quality.HD
            "480p", "sd" -> Quality.SD
            else -> Quality.FHD // 1080p default
        }

        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    quality,
                    androidx.camera.video.FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                )
            )
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    fun switchCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        resolutionQuality: String
    ) {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        isTorchOn = false
        bindCameraUseCases(lifecycleOwner, previewView, currentAspectRatio, resolutionQuality)
    }

    fun updateAspectRatio(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        selectedAspectRatio: com.example.data.model.AspectRatio,
        resolutionQuality: String
    ) {
        bindCameraUseCases(lifecycleOwner, previewView, selectedAspectRatio, resolutionQuality)
    }

    fun toggleTorch(): Boolean {
        camera?.let { cam ->
            if (cam.cameraInfo.hasFlashUnit()) {
                isTorchOn = !isTorchOn
                cam.cameraControl.enableTorch(isTorchOn)
                return isTorchOn
            }
        }
        return false
    }

    @SuppressLint("MissingPermission")
    fun startRecording(
        outputTitle: String,
        onStatusUpdate: (RecordingStatus) -> Unit
    ) {
        val videoCapture = this.videoCapture ?: run {
            onStatusUpdate(RecordingStatus.Error("Camera recorder not ready"))
            return
        }

        // Create local output file
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val cleanTitle = outputTitle.replace("[^a-zA-Z0-9_]".toRegex(), "_").take(30)
        val videoFile = File(outputDir, "Teleprompter_${cleanTitle}_$timestamp.mp4")

        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        var recordingBuilder = videoCapture.output
            .prepareRecording(context, outputOptions)

        // Enable audio if permission is granted and option is selected
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (isAudioEnabled && hasAudioPermission) {
            try {
                recordingBuilder = recordingBuilder.withAudioEnabled()
            } catch (e: SecurityException) {
                Log.w(TAG, "Audio recording permission denied at runtime", e)
            }
        }

        currentRecording = recordingBuilder.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
            when (recordEvent) {
                is VideoRecordEvent.Start -> {
                    onStatusUpdate(RecordingStatus.Recording(durationMs = 0L, isPaused = false))
                }
                is VideoRecordEvent.Status -> {
                    val totalRecordedMs = recordEvent.recordingStats.recordedDurationNanos / 1_000_000L
                    onStatusUpdate(RecordingStatus.Recording(durationMs = totalRecordedMs, isPaused = false))
                }
                is VideoRecordEvent.Pause -> {
                    val totalRecordedMs = recordEvent.recordingStats.recordedDurationNanos / 1_000_000L
                    onStatusUpdate(RecordingStatus.Recording(durationMs = totalRecordedMs, isPaused = true))
                }
                is VideoRecordEvent.Resume -> {
                    val totalRecordedMs = recordEvent.recordingStats.recordedDurationNanos / 1_000_000L
                    onStatusUpdate(RecordingStatus.Recording(durationMs = totalRecordedMs, isPaused = false))
                }
                is VideoRecordEvent.Finalize -> {
                    if (!recordEvent.hasError()) {
                        val duration = recordEvent.recordingStats.recordedDurationNanos / 1_000_000L
                        // AUTOMATIC DIRECT SAVE TO PHONE GALLERY
                        val galleryUri = autoSaveToGallery(context, videoFile, cleanTitle)
                        
                        onStatusUpdate(RecordingStatus.Success(
                            file = videoFile,
                            uri = Uri.fromFile(videoFile),
                            durationMs = duration,
                            galleryUri = galleryUri
                        ))
                    } else {
                        currentRecording?.close()
                        currentRecording = null
                        onStatusUpdate(RecordingStatus.Error("Recording error: ${recordEvent.cause?.message ?: "Unknown"}"))
                    }
                }
            }
        }
    }

    private fun autoSaveToGallery(context: Context, sourceFile: File, title: String): Uri? {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "Teleprompter_${title}_${System.currentTimeMillis()}.mp4")
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Teleprompter")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(sourceFile).use { input ->
                            input.copyTo(out)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)

                    // Trigger scanner
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(sourceFile.absolutePath),
                        arrayOf("video/mp4"),
                        null
                    )
                    Log.d(TAG, "Video auto-saved to public gallery: $uri")
                    return uri
                }
            } else {
                val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                val appFolder = File(moviesDir, "Teleprompter")
                if (!appFolder.exists()) appFolder.mkdirs()
                val destFile = File(appFolder, "Teleprompter_${title}_${System.currentTimeMillis()}.mp4")

                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(destFile.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )
                return Uri.fromFile(destFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error auto-saving video to gallery", e)
        }
        return null
    }

    fun pauseRecording() {
        try {
            currentRecording?.pause()
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing recording", e)
        }
    }

    fun resumeRecording() {
        try {
            currentRecording?.resume()
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming recording", e)
        }
    }

    fun stopRecording() {
        try {
            currentRecording?.stop()
            currentRecording = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
        }
    }

    fun release() {
        try {
            currentRecording?.stop()
            currentRecording = null
            cameraProvider?.unbindAll()
            cameraExecutor.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing camera", e)
        }
    }
}
