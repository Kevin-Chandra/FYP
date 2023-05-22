package com.example.fyp.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fyp.ordering_system.ui.viewmodel.IncomingOrderViewModel
import com.example.fyp.ordering_system.ui.viewmodel.ProductViewModel
import com.example.fyp.pos.ui.navigation.PosNavGraph
import com.example.fyp.pos.ui.theme.FYPTheme
import com.example.fyp.pos.ui.viewmodel.IncomingOrderItemViewModel
import com.example.fyp.pos.ui.viewmodel.ManageOrderViewModel
import com.example.fyp.pos.ui.viewmodel.ManageTableViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lateinit var navController: NavHostController


        setContent {
            val productViewModel = hiltViewModel<ProductViewModel>()
            val incomingOrderItemViewModel = hiltViewModel<IncomingOrderItemViewModel>()
            val manageOrderViewModel = hiltViewModel<ManageOrderViewModel>()
            val manageTableViewModel = hiltViewModel<ManageTableViewModel>()

            navController = rememberNavController()

            FYPTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PosNavGraph(
                        navController = navController,
                        productViewModel = productViewModel,
                        incomingOrderItemViewModel = incomingOrderItemViewModel,
                        manageOrderViewModel = manageOrderViewModel,
                        manageTableViewModel = manageTableViewModel
                    )
                }
            }
        }
    }
}
