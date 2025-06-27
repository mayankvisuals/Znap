package com.devmayank.znap

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine









@Composable
fun permission() {
    val permission = listOf(
        android.Manifest.permission.CAMERA
    )


    val isGranted = remember {
      mutableStateOf(false)
    }


    val context = LocalContext.current


    val launcher = rememberLauncherForActivityResult(

        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            permission->
            isGranted.value =permission[android.Manifest.permission.CAMERA] == true
        }
    )





    if (isGranted.value){
        cameraScreen()
    }else
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    launcher.launch(permission.toTypedArray())
                }
            ) {

                Text(text = "Request")
            }
        }

}




@Composable
fun cameraScreen() {

    val context = LocalContext.current

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val previewView : PreviewView = remember {
        PreviewView(context)
    }

    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    val preview = Preview.Builder().build()

    val imageCapture = remember {
        ImageCapture.Builder().build()
    }



  LaunchedEffect(Unit) {

      val cameraProvider = context.getCameraProvider()

      cameraProvider.unbindAll()

      cameraProvider.bindToLifecycle(
          lifecycleOwner = lifecycleOwner,
          cameraSelector = cameraSelector,
          preview,
          imageCapture
      )

      preview.setSurfaceProvider (previewView.surfaceProvider)



  }




    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
        ){

        AndroidView(factory = {previewView} , modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp),

            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    capturePhoto(imageCapture, context)
                },
                modifier = Modifier.size(50.dp)
                    .background(Color.White , CircleShape)
                    .padding(8.dp)
                    .background(Color.Red , CircleShape)
            ) { }



        }
    }

}














private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->

    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

    cameraProviderFuture.addListener({
        continuation.resume(cameraProviderFuture.get())
    }, ContextCompat.getMainExecutor(this)
    )

    //change image name and format from here
}
private fun capturePhoto(imageCapture: ImageCapture , context: Context){
    val name = "Znap_${System.currentTimeMillis()}.jpg"


    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME , name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH ,"Pictures/Znap" )

    }
    val outputOption = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()


    imageCapture.takePicture(
        outputOption,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Toast.makeText(context, "Image Saved to gallery", Toast.LENGTH_SHORT).show()
            }


            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "Failed to save : ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    )



}