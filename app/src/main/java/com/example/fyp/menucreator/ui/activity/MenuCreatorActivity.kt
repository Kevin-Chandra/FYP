package com.example.fyp.menucreator.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.fyp.R
import com.example.fyp.database.ProductDatabase
import com.example.fyp.databinding.ActivityMenuCreatorBinding
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuCreatorActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMenuCreatorBinding

    private val productCollectionRef = Firebase.firestore.collection("products")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMenuCreatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)


        //test
//        val db = ProductDatabase
//        val withSides = ModifierItem("MI-2", "Sides", 5.0)
//        val withoutSides = ModifierItem("MI-1", "No Sides", 0.0)
////
//        val sugar0 = ModifierItem("MI-3", "Sugar 0%", 0.0)
//        val sugar25 = ModifierItem("MI-4", "Sugar 25%", 0.0)
//        val sugar50 = ModifierItem("MI-5", "Sugar 50%", 0.0)
//        val sugar75 = ModifierItem("MI-6", "Sugar 75%", 0.0)
//        val sugar100 = ModifierItem("MI-7", "Sugar 100%", 0.0)
////
//        val modifier1 = Modifier("M-2", "Sugar Level", false, false, arrayListOf("MI-3","MI-4","MI-5","MI-6","MI-7"))
//        modifier1.addItem("MI-3")
//        modifier1.addItem("MI-4")
//        modifier1.addItem("MI-5")
//        modifier1.addItem("MI-6")
//        modifier1.addItem("MI-7")
//
//        db.insertModifierItem(sugar0)
//        db.insertModifierItem(sugar25)
//        db.insertModifierItem(sugar50)
//        db.insertModifierItem(sugar75)
//        db.insertModifierItem(sugar100)
//        db.insertModifier(modifier1)
//
//        val modifier2 = Modifier("M-1", "Sides", true, false, arrayListOf("MI-1","MI-2"))
//        modifier2.addItem("MI-2")
//        modifier2.addItem("MI-1")
//
//        db.insertModifier(modifier2)
//        db.insertModifierItem(withSides)
//        db.insertModifierItem(withoutSides)

//        val steak = Food("F-1", "Steak", 15.0, "", null,true,0, ArrayList<String>())
////        steak.addModifier("M-1")
//
//        val coke = Food("B-1", "Coke", 2.0, "", null,false,0,ArrayList<String>())
//        val tea = Food("B-2", "Tea", 1.0,  "", null,false,0,ArrayList<String>())
//        tea.addModifier("M-2")

//        db.insertFood(steak)
////        saveFoodToDatabase(steak)
//        db.insertFood(coke)
//        db.insertFood(tea)

//        val db1 = ModifierItemRepository()
//        db1.addModifierItem(withSides)
//        db1.addModifierItem(withoutSides)
//        db1.addModifierItem(sugar0)
//        db1.addModifierItem(sugar100)
//        db1.addModifierItem(sugar25)
//        db1.addModifierItem(sugar50)
//        db1.addModifierItem(sugar75)
//
//        val db2 = ModifierRepository()
//        db2.addModifier(modifier1)
//        db2.addModifier(modifier2)


//        db.saveToDatabase()
//        val list = db.getFoodList()
//        for (f in list)
//            Log.d("AAAA", "${f.productId}")

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}