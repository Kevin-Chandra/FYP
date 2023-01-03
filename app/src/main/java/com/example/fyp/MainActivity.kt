package com.example.fyp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp.databinding.ActivityMainBinding
import com.example.fyp.menucreator.activity.MenuCreatorActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.menuCreatorButton.setOnClickListener {
            val i = Intent(applicationContext, MenuCreatorActivity::class.java)
            startActivity(i)
        }

        val db = Firebase.firestore.collection("Products")

    }

}