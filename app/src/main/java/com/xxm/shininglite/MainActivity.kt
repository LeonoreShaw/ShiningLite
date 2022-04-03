package com.xxm.shininglite



import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.*
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        const val TAG = "TFLite - ODT"
        const val REQUEST_IMAGE_CAPTURE: Int = 1
        private const val MAX_FONT_SIZE = 96F
    }

    private lateinit var captureImageFab: Button
    private lateinit var captureImageFab2: Button       //”相册“按钮
    private lateinit var buttonF: Button       //”相册“按钮
    private lateinit var inputImageView: ImageView
    private lateinit var imgSampleOne: ImageView
    private lateinit var imgSampleTwo: ImageView
    private lateinit var imgSampleThree: ImageView
    private lateinit var tvPlaceholder: TextView
    private lateinit var currentPhotoPath: String
//    private lateinit var binding: ActivityMainBinding
//    private lateinit var objectDetector: ObjectDetector
//    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        binding = DataBindingUtil.setContentView(this, R.layout.viewFinder)
//
//        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            val cameraProvider = cameraProviderFuture.get()
//            // Bind camera provider
//        }, ContextCompat.getMainExecutor(this))

        captureImageFab = findViewById(R.id.captureImageFab)
        captureImageFab2 = findViewById(R.id.captureImageFab2)
        buttonF = findViewById(R.id.buttonF)
        inputImageView = findViewById(R.id.imageView)
        imgSampleOne = findViewById(R.id.imgSampleOne)
        imgSampleTwo = findViewById(R.id.imgSampleTwo)
        imgSampleThree = findViewById(R.id.imgSampleThree)
        tvPlaceholder = findViewById(R.id.tvPlaceholder)

        captureImageFab.setOnClickListener(this)
        captureImageFab2.setOnClickListener(this)
        buttonF.setOnClickListener(this)
        imgSampleOne.setOnClickListener(this)
        imgSampleTwo.setOnClickListener(this)
        imgSampleThree.setOnClickListener(this)


        val permissions = arrayOf(
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"
        )
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(permissions, 1)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE &&
            resultCode == Activity.RESULT_OK
        ) {
            setViewAndDetect(getCapturedImage())
        }
    }

    /**
     * onClick(v: View?)
     *      Detect touches on the UI components
     *      * onClick（v：查看？）
     * 检测对 UI 组件的触摸
     */
    @SuppressLint("SdCardPath")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.captureImageFab -> {
                try {
                    dispatchTakePictureIntent()
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, e.message.toString())
                }
            }
            R.id.captureImageFab2 -> {
                setViewAndDetect(getSampleImage(R.drawable.s30))
            }
            R.id.imgSampleOne -> {
                setViewAndDetect(getSampleImage(R.drawable.s30))
            }
            R.id.imgSampleTwo -> {
                setViewAndDetect(getSampleImage(R.drawable.s50))
            }
            R.id.imgSampleThree -> {
                setViewAndDetect(getSampleImage(R.drawable.s60))
            }
            R.id.buttonF -> {
                val mmr = MediaMetadataRetriever() //实例化MediaMetadataRetriever对象
                val file =
                    File("/sdcard/AndroidShiningLite/fun.mp4") //实例化File对象，文件路径为/storage/sdcard/Movies/music1.mp4
                if (file.exists()) {
                    mmr.setDataSource(file.absolutePath) //设置数据源为该文件对象指定的绝对路径
//                val bitmap = mmr.frameAtTime //获得视频第一帧的Bitmap对象           27067

                    /**
                     *           获取视频相关信息
                     *           /sdcard/Movies/fun.mp4
                     */
                    //获取视频时长，单位：毫秒(ms)            800043ms
                    val totalTime: String? =
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    //获取视频帧数                         24000Frames
                    val frameCount: String? =
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                    //获取视频宽度（单位：px）               480px
                    val width: String? =
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    //获取视频高度（单位：px）               360px
                    val high: String? =
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)

                    /**
                     ** $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                     ** ###################################################################################
                     **      $$$$$$$$$$$$$$$##   实现位图延迟播放    ##$$$$$$$$$$$$$$$
                     ** ###################################################################################
                     ** $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                     **/

                    val f:Int = frameCount!!.toInt()

                    val bitmaps = mmr.getFramesAtIndex(1, 100)
                    thread {
                        bitmaps.forEach { bm ->
                            runOnUiThread { setViewAndDetect(bm) }
                            Thread.sleep(100)
                        }
                    }



//
//                    val bitmaps = mmr.getFrameAtIndex(10)
//
//                    inputImageView.setImageBitmap(bitmaps)


//                    val f:Int = frameCount!!.toInt()
//                    val bitmap = mmr.getFrameAtIndex( 1) //index为帧序号
//                    if (frameCount != null) {
//                        val f = frameCount.toInt()
//                        for (i in 1..f step 100){
//                            val bitmaps = mmr.getFrameAtIndex(i)
//                            if (bitmaps != null) {
//                                inputImageView.setImageBitmap(bitmaps)
////                                setViewAndDetect(bitmaps)
//                            }
//                            Thread.sleep(1000) // 假装我们正在计算
//                        }
//                    }


//                    if (bitmaps != null) {
//                        for(i in 1..500){
//                            inputImageView.setImageBitmap(bitmaps[50]) //设置ImageView显示的图片
//
//                        }

//                        inputImageView.setImageBitmap(bitmaps) //设置ImageView显示的图片
//                        Toast.makeText(this@MainActivity, "获取视频帧成功", Toast.LENGTH_SHORT)
//                            .show() //获取视频帧成功，弹出消息提示框
//                    } else {
//                        Toast.makeText(this@MainActivity, "获取视频帧失败", Toast.LENGTH_SHORT)
//                            .show() //获取视频帧失败，弹出消息提示框
//                    }



                } else {
                    Toast.makeText(this@MainActivity, "文件不存在", Toast.LENGTH_SHORT)
                        .show() //文件不存在时，弹出消息提示框
                }
            }
        }
    }

//
//    /**
//     * 相机预览
//     *
//     */
//    @SuppressLint("UnsafeOptInUsageError")
//    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
//
//        val preview = Preview.Builder().build()
//
//        val cameraSelector = CameraSelector.Builder()
//            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//            .build()
//
//        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
//
//        val imageAnalysis = ImageAnalysis.Builder()
//            .setTargetResolution(Size(1280, 720))
//            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//            .build()
//
//        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
//
//            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
//            val image = imageProxy.image
//
//            if(image != null){
//                val processImage = InputImage.fromMediaImage(image, rotationDegrees)
//
//                objectDetector
//                    .process(processImage)
//                    .addOnSuccessListener{
//                        imageProxy.close()
//                    }
//            }
//
//            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)
//        }
//
//
//    }




    /**
     * runObjectDetection(bitmap: Bitmap)
     *      TFLite Object Detection function
     *      * runObjectDetection（位图：位图）
     * TFLite 物体检测功能
     */
    private fun runObjectDetection(bitmap: Bitmap) {
        // Step 1: Create TFLite's TensorImage object
        // 第一步：创建 TFLite 的 TensorImage 对象
        val image = TensorImage.fromBitmap(bitmap)

        // Step 2: Initialize the detector object
        // 第二步：初始化检测器对象
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.3f)
            .build()
        val detector = ObjectDetector.createFromFileAndOptions(
            this,
            "labelmodel.tflite",
            options
        )

        // Step 3: Feed given image to the detector
        // 第 3 步：将给定图像输入到检测器
        val results = detector.detect(image)

        // Step 4: Parse the detection result and show it
        // 第四步：解析检测结果并展示
        val resultToDisplay = results.map {
            // Get the top-1 category and craft the display text
            // 获取 top-1 类别并制作显示文本
            val category = it.categories.first()
            val text = "${category.label}, ${category.score.times(100).toInt()}%"

            // Create a data object to display the detection result
            // 创建一个数据对象来显示检测结果
            DetectionResult(it.boundingBox, text)
        }
        // Draw the detection result on the bitmap and show it.
        // 在位图上绘制检测结果并显示。
        val imgWithResult = drawDetectionResult(bitmap, resultToDisplay)
        runOnUiThread {
            inputImageView.setImageBitmap(imgWithResult)
        }
    }

    /**
     * debugPrint(visionObjects: List<Detection>)
     *      Print the detection result to logcat to examine
     *      * debugPrint(visionObjects: List<Detection>)
     * 将检测结果打印到logcat进行检查
     */
    private fun debugPrint(results : List<Detection>) {
        for ((i, obj) in results.withIndex()) {
            val box = obj.boundingBox

            Log.d(TAG, "Detected object: ${i} ")
            Log.d(TAG, "  boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")

            for ((j, category) in obj.categories.withIndex()) {
                Log.d(TAG, "    Label $j: ${category.label}")
                val confidence: Int = category.score.times(100).toInt()
                Log.d(TAG, "    Confidence: ${confidence}%")
            }
        }
    }


    /**
     * setViewAndDetect(bitmap: Bitmap)
     *      Set image to view and call object detection
     *      * setViewAndDetect（位图：位图）
     * 设置图片查看和调用物体检测
     */
    private fun setViewAndDetect(bitmap: Bitmap) {
        // Display capture image
        // 显示捕获图像
        inputImageView.setImageBitmap(bitmap)
        tvPlaceholder.visibility = View.INVISIBLE

        // Run ODT and display result
        // Note that we run this in the background thread to avoid blocking the app UI because
        // TFLite object detection is a synchronised process.
        // 运行 ODT 并显示结果
        // 请注意，我们在后台线程中运行它以避免阻塞应用程序 UI，因为
        // TFLite 对象检测是一个同步过程。
        lifecycleScope.launch(Dispatchers.Default) { runObjectDetection(bitmap) }
    }

    /**
     * getCapturedImage():
     *      Decodes and crops the captured image from camera.
     *      * 获取图像（）：
     *解码并裁剪从相机捕获的图像。
     */
    private fun getCapturedImage(): Bitmap {
        // Get the dimensions of the View
        // 获取视图的尺寸
        val targetW: Int = inputImageView.width
        val targetH: Int = inputImageView.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            // 获取位图的尺寸
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            // 确定图片缩小多少
            val scaleFactor: Int = max(1, min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            // 将图像文件解码为大小以填充视图的位图
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inMutable = true
        }
        val exifInterface = ExifInterface(currentPhotoPath)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        val bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotateImage(bitmap, 90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotateImage(bitmap, 180f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotateImage(bitmap, 270f)
            }
            else -> {
                bitmap
            }
        }
    }

    /**
     * getSampleImage():
     *      Get image form drawable and convert to bitmap.
     *      * get Sample Image():
     *      * 从 drawable 中获取图像并转换为位图。
     */
    private fun getSampleImage(drawable: Int): Bitmap {
        return BitmapFactory.decodeResource(resources, drawable, BitmapFactory.Options().apply {
            inMutable = true
        })
    }


//TODO 添加示例视频
    /**
     * getSampleImage():
     *      Get image form drawable and convert to bitmap.
     *      * get Sample Image():
     *      * 从 drawable 中获取图像并转换为位图。
     */
//    private fun getSampleVideo(raw: Int): Bitmap {
//        return BitmapFactory.decodeResource(resources, raw, BitmapFactory.Options().apply {
//            inMutable = true
//        })
//    }



    /**
     *播放视频
     */
//    var videoView: VideoView? = null
//    var mediaController: MediaController? = null





    /**
     * rotateImage():
     *     Decodes and crops the captured image from camera.
     *     * 旋转图像（）：
     *解码并裁剪从相机捕获的图像。
     */
    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    /**
     * createImageFile():
     *     Generates a temporary image file for the Camera app to write to.
     *     * 创建图像文件（）：
     * 为相机应用程序生成一个临时图像文件以写入。
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        // 创建图片文件名
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            // 保存文件：用于 ACTION_VIEW 意图的路径
            currentPhotoPath = absolutePath
        }
    }

    /**
     * dispatchTakePictureIntent():
     *     Start the Camera app to take a photo.
     *     * dispatchTakePictureIntent():
     * 启动相机应用拍照。
     */
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            // 确保有一个相机活动来处理意图
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                //创建照片应该去的文件
                val photoFile: File? = try {
                    createImageFile()
                } catch (e: IOException) {
                    Log.e(TAG, e.message.toString())
                    null
                }
                // Continue only if the File was successfully created
                // 仅在文件创建成功后继续
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.xxm.shininglite.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    /**
     * drawDetectionResult(bitmap: Bitmap, detectionResults: List<DetectionResult>
     *      Draw a box around each objects and show the object's name.
     *      * drawDetectionResult（位图：位图，检测结果：列表<检测结果>
     * 在每个对象周围画一个框并显示对象的名称。
     */
    private fun drawDetectionResult(
        bitmap: Bitmap,
        detectionResults: List<DetectionResult>
    ): Bitmap {
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        val pen = Paint()
        pen.textAlign = Paint.Align.LEFT

        detectionResults.forEach {
            // draw bounding box
            // 绘制边界框
            pen.color = Color.RED
            pen.strokeWidth = 8F
            pen.style = Paint.Style.STROKE
            val box = it.boundingBox
            canvas.drawRect(box, pen)


            val tagSize = Rect(0, 0, 0, 0)

            // calculate the right font size
            // 计算正确的字体大小
            pen.style = Paint.Style.FILL_AND_STROKE
            pen.color = Color.YELLOW
            pen.strokeWidth = 2F

            pen.textSize = MAX_FONT_SIZE
            pen.getTextBounds(it.text, 0, it.text.length, tagSize)
            val fontSize: Float = pen.textSize * box.width() / tagSize.width()

            // adjust the font size so texts are inside the bounding box
            // 调整字体大小，使文本在边界框内
            if (fontSize < pen.textSize) pen.textSize = fontSize

            var margin = (box.width() - tagSize.width()) / 2.0F
            if (margin < 0F) margin = 0F
            canvas.drawText(
                it.text, box.left + margin,
                box.top + tagSize.height().times(1F), pen
            )
        }
        return outputBitmap
    }
}


/**
 * DetectionResult
 *      A class to store the visualization info of a detected object.
 *      * 检测结果
 * 用于存储检测到的对象的可视化信息的类。
 */
data class DetectionResult(val boundingBox: RectF, val text: String)
