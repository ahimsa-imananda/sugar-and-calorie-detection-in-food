package com.daftech.sweetectapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.daftech.sweetectapp.databinding.ActivityDetailBinding
import com.daftech.sweetectapp.ml.ModelRegulerizer
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        //Prepare Tflite model and data
        val fileName = "labels.txt"
        val sugarName = "sugar.txt"
        val caloriesName = "calorie.txt"

        val inputString = application.assets.open(fileName).bufferedReader().use { it.readText() }
        val sugarString = application.assets.open(sugarName).bufferedReader().use { it.readText() }
        val caloriesString = application.assets.open(caloriesName).bufferedReader().use { it.readText() }

        val foodList = inputString.split("\n")
        val sugarList = sugarString.split("\n")
        val caloriesList = caloriesString.split("\n")

        //get uri image bitmap
        val imageUri: Uri? = intent.data
        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

        //load image
        loadImage(imageUri)

        binding.btnPredicted.setOnClickListener {
            //implement Tflite
            val model = ModelRegulerizer.newInstance(this)

            val imageProcessor: ImageProcessor =
                ImageProcessor.Builder().add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                    .build()
            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            val byteBuffer = tensorImage.buffer
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            val max = getMax(outputFeature0.floatArray)

            val resultFood = foodList[max]
            val resultSugar = sugarList[max]
            val resultCalorie = caloriesList[max]

            binding.foodName.text = "Your Food Is $resultFood"
            binding.foodDesc.text = "your sugar and calories take from $resultFood (as per 100gr food / portion)"

            //calorie chart
            if (resultCalorie != null){
                binding.calorieChart.visibility = View.VISIBLE
                binding.progbar.visibility = View.GONE
                calorieChart(resultCalorie)
            }else{
                binding.calorieChart.visibility = View.GONE
            }

            binding.btnPredicted.visibility = View.GONE
            binding.btnSelectTake.visibility = View.VISIBLE
            binding.progbar.visibility = View.VISIBLE

            //sugar chart
            if (resultSugar != null){
                binding.sugarChart.visibility = View.VISIBLE
                binding.progbar.visibility = View.GONE
                sugarChart(resultSugar)
            }else{
                binding.sugarChart.visibility = View.GONE
            }




        }

        binding.btnSelectTake.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    //load image function
    private fun loadImage(image : Uri?){
        Log.d("photo", image.toString())
        if (image != null){
            binding.foodPhoto.setImageURI(image)
        }else{
            binding.foodName.text = "Tidak Ada Foto"
        }
    }

    //get value of data
    private fun getMax(arr:FloatArray) : Int{
        var ind = 0
        var min = 0.0f

        for (i in 0..100){
            if (arr[i] > min){
                ind = i
                min = arr[i]
            }
        }
        return ind
    }

    //chart function
    private fun calorieChart(data: String){
        val visitors: ArrayList<PieEntry> = ArrayList()

        visitors.add(PieEntry(data.toFloat(), ""))
        val pieDataSet = PieDataSet(visitors, "Calories / 100g")
        pieDataSet.color = Color.rgb(157,190,185)
        pieDataSet.valueTextSize = 12f

        val pieData = PieData(pieDataSet)

        binding.apply {
            calorieChart.data = pieData
            calorieChart.description.isEnabled = false
            calorieChart.centerText = "Calories"
            calorieChart.animate()
        }
    }

    private fun sugarChart(resultSugar: String) {
        val visitors: ArrayList<PieEntry> = ArrayList()



        if (resultSugar.toFloat() > 0.0){
            visitors.add(PieEntry(resultSugar.toFloat(), ""))

            val pieDataSet = PieDataSet(visitors, "Sugar / 100g")
            pieDataSet.color = Color.rgb(255,192,203)
            pieDataSet.valueTextColor = Color.BLACK
            pieDataSet.valueTextSize = 12f

            val pieData = PieData(pieDataSet)

            binding.apply {
                sugarChart.data = pieData
                sugarChart.description.isEnabled = false
                sugarChart.centerText = "Sugar"
                sugarChart.animate()
            }
        }else{
            visitors.add(PieEntry(resultSugar.toFloat(), ""))

            val pieDataSet = PieDataSet(visitors, "Sugar / 100g")
            pieDataSet.color = Color.rgb(255,192,203)
            pieDataSet.valueTextColor = Color.BLACK
            pieDataSet.valueTextSize = 12f

            val pieData = PieData(pieDataSet)

            binding.apply {
                sugarChart.data = pieData
                sugarChart.description.isEnabled = false
                sugarChart.centerText = "No Sugar"
                sugarChart.animate()
            }
        }
    }
}
