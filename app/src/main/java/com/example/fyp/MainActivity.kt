package com.example.fyp

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.fyp.account_management.AuthActivity
import com.example.fyp.account_management.ui.view_model.MainAuthViewModel
import com.example.fyp.databinding.ActivityMainBinding
import com.example.fyp.menucreator.ui.activity.MenuCreatorActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var navController: NavController

    private val authViewModel by viewModels<MainAuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController

        authViewModel.getSession {
            if (it == null){
                val i = Intent(applicationContext, AuthActivity::class.java)
                startActivity(i)
                finish()
            }
        }

    }

}