package com.example.fyp.ordering_system.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomerOrderBottomNavigation(navController: NavController,modifier: Modifier = Modifier) {
    val items = listOf(
        CustomerBottomNavItem.OrderScreen,
        CustomerBottomNavItem.OngoingScreen,
        CustomerBottomNavItem.OrderHistoryScreen,
    )
    NavigationBar(
        modifier = modifier.semantics {
            testTagsAsResourceId = true
        }
    ){
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.title) },
                label = { Text(
                    text = item.title,)
                        },
                selected = currentRoute == item.screen_route,
                alwaysShowLabel = true,
                onClick = {
                    navController.navigate(item.screen_route) {

                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.testTag(item.title)
            )
        }
    }
}