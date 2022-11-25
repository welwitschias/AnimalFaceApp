package com.example.animalfaceapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.animalfaceapp.databinding.ActivityInputBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class InputActivity : AppCompatActivity() {
    private var getGalleryImage: Int = 200
    private lateinit var currentPhotoPath: String
    private lateinit var selectedImgUri: Uri
    private lateinit var filePath: String
    private lateinit var binding: ActivityInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermission()

        binding.selectImg.setOnClickListener {
            val dialogBtn = AlertDialog.Builder(this)
            dialogBtn.setTitle("이미지 가져오기")
            dialogBtn.setPositiveButton("사진촬영") { dialog, which ->
                startCapture()
            }
            dialogBtn.setNegativeButton("앨범선택") { dialog, which ->
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, getGalleryImage)
            }
            dialogBtn.setNeutralButton("취소") { dialog, which ->
                null
            }
            dialogBtn.show()
        }

        binding.fab.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.resultBtn.setOnClickListener {
            val stream = ByteArrayOutputStream()
            val bitmap = (binding.selectImg!!.drawable as BitmapDrawable).bitmap
            val scale = (1024 / bitmap.width.toFloat())
            val imageWidth = (bitmap.width * scale).toInt()
            val imageHeight = (bitmap.height * scale).toInt()
            val resize = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, true)
            resize.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            val byteArray = stream.toByteArray()
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("image", byteArray)
            startActivity(intent)
        }
    }

    private fun inSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        try {
            var inputStream = contentResolver.openInputStream(fileUri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream!!.close()
            inputStream = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth)
                inSampleSize *= 2
        }

        return inSampleSize
    }

    private val requestCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val calRatio = inSampleSize(Uri.fromFile(File(filePath)), 150, 150)
            val option = BitmapFactory.Options()
            option.inSampleSize = calRatio

            val bitmap = BitmapFactory.decodeFile(filePath, option)
            bitmap?.let {
                binding.selectImg.setImageBitmap(bitmap)
            } ?: let {
                Log.d("myLog", "(in camera)bitmap is null")
            }
        }

    private fun requestPermission() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
//                    Toast.makeText(this@InputActivity, "완료", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Toast.makeText(this@InputActivity, "권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
                }
            })
            .setDeniedMessage("권한을 허용해주세요. [설정] > [앱 및 알림] > [고급] > [앱 권한]")
            .setPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .check()
    }

    // 사진 촬영본 캡쳐
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun startCapture() {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir((Environment.DIRECTORY_PICTURES))
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        filePath = file.absolutePath
        val photoUri: Uri =
            FileProvider.getUriForFile(this, "com.example.animalfaceapp.fileprovider", file)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        requestCameraLauncher.launch(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var bitmap: Bitmap? = null

        if (resultCode == Activity.RESULT_OK) {
            // 갤러리 선택
            if (requestCode == getGalleryImage && data != null) {
                selectedImgUri = data.data!!
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImgUri)
                binding.selectImg.setImageBitmap(bitmap)
            }
            // 사진 촬영
            else if (requestCode == 1) {
                val file = File(currentPhotoPath)
                if (Build.VERSION.SDK_INT < 28) {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                    binding.selectImg.setImageBitmap(bitmap)
                } else {
                    val decode = ImageDecoder.createSource(this.contentResolver, Uri.fromFile(file))
                    val bitmap = ImageDecoder.decodeBitmap(decode)
                    binding.selectImg.setImageBitmap(bitmap)
                }
            }
        }
    }
}