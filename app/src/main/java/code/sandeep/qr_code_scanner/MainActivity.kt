package code.sandeep.qr_code_scanner

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import code.sandeep.qr_code_scanner.ui.theme.Qr_code_scannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Qr_code_scannerTheme {
                var code by remember {
                    mutableStateOf("")
                }
                val context = LocalContext.current
                val cameraProviderFuture = remember {
                    ProcessCameraProvider.getInstance(context)
                }
                val lifecycleOwner = LocalLifecycleOwner.current
                var hasCameraPermission = remember {
                    ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                }
                val launcher =
                    rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(),
                        onResult = { granted ->
                            hasCameraPermission = granted
                        })
                LaunchedEffect(key1 = true) {
                    launcher.launch(android.Manifest.permission.CAMERA)
                }
                Column(
                    Modifier.fillMaxSize()
                ) {
                    if (hasCameraPermission) {
                        AndroidView(factory = { context ->
                            val previewView = PreviewView(context)
                            val preview = Preview.Builder().build()
                            val selector = CameraSelector.Builder().requireLensFacing(
                                LENS_FACING_BACK
                            ).build()
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST).build()
                            imageAnalysis.setAnalyzer(
                                ContextCompat.getMainExecutor(context),
                                QrCodeAnalyser { result ->
                                    code = result
                                })
                            try {
                                cameraProviderFuture.get().bindToLifecycle(
                                    lifecycleOwner, selector, preview, imageAnalysis
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            previewView

                        }, modifier = Modifier.weight(1f))
                        Text(
                            text = code,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }

                }
            }
        }
    }
}