package com.ingridientsinc.recipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ingridientsinc.recipe.navigation.NavGraph
import com.ingridientsinc.recipe.navigation.Screen
import com.ingridientsinc.recipe.ui.theme.RecipesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipesTheme {
                RecipesApp()
            }
        }
    }
}

@Composable
fun RecipesApp() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val context = LocalContext.current
    val viewModel: RecipeViewModel = viewModel { RecipeViewModel(recipeStore(context)) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                icon = { Icon(Icons.Default.Home, contentDescription = "Browse") },
                label = { Text("Browse") },
                selected = currentRoute == Screen.Browse.route,
                onClick = { navController.switchTab(Screen.Browse.route) }
            )
            item(
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                label = { Text("Add") },
                selected = currentRoute == Screen.Add.route,
                onClick = { navController.switchTab(Screen.Add.route) }
            )
            item(
                icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                label = { Text("Favorites") },
                selected = currentRoute == Screen.Favorites.route,
                onClick = { navController.switchTab(Screen.Favorites.route) }
            )
            item(
                icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                label = { Text("Search") },
                selected = currentRoute == Screen.Search.route,
                onClick = { navController.switchTab(Screen.Search.route) }
            )
        }
    ) {
        // One inset for every screen — edge-to-edge is on, so without this the content
        // draws under the status bar.
        Box(Modifier.statusBarsPadding()) {
            NavGraph(navController = navController, viewModel = viewModel)
        }
    }
}

/** Keeps one entry per tab on the back stack and restores each tab's own state. */
private fun NavHostController.switchTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
