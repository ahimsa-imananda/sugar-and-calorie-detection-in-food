package com.daftech.sweetectapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.daftech.sweetectapp.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var bitmap : Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()


        when(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            PackageManager.PERMISSION_GRANTED -> {

            }
            else -> {
                checkForPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, "write storage", 102)
            }
        }


        binding.btnSelect.setOnClickListener {
            when(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                PackageManager.PERMISSION_GRANTED -> {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"

                    startActivityForResult(intent, 100)
                }
                else -> {
                    checkForPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, "storage", 100)
                }
            }

        }

        binding.btnPicture.setOnClickListener {
            when(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)){
                PackageManager.PERMISSION_GRANTED -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, 101)
                }
                else -> {
                    checkForPermissions(Manifest.permission.CAMERA, "camera", 101)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK){
            val uri : Uri? = data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            val intent = Intent(this, DetailActivity::class.java)
            intent.data = uri
            startActivity(intent)
        }

        if (requestCode == 101 && resultCode == RESULT_OK){
            val bytes = ByteArrayOutputStream()
            bitmap = data?.getParcelableExtra("data")!!
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(this.contentResolver, bitmap, null, null)
            val imgPhoto = Uri.parse(path)
            val intent = Intent(this, DetailActivity::class.java)
            intent.data = imgPhoto
            startActivity(intent)
        }

    }

    private fun checkForPermissions(permission: String, name: String, requestCode: Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            when{
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> {
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)
                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fun innerCheck(name: String){
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "$name Permission Rejected", Toast.LENGTH_SHORT).show()
            }
        }
        when(requestCode){
            100 -> innerCheck("storage")
            101 -> innerCheck("camera")
            102 -> innerCheck("write storage")
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int){
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Permission required $name")
            setTitle("Permission required")
            setPositiveButton("Ok"){ _, _ ->
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}