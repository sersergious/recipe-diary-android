package com.ingridientsinc.recipe.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ingridientsinc.recipe.RecipeViewModel
import com.ingridientsinc.recipe.ui.AddRecipeScreen
import com.ingridientsinc.recipe.ui.FavoritesScreen
import com.ingridientsinc.recipe.ui.RecipeDetailScreen
import com.ingridientsinc.recipe.ui.RecipeListScreen
import com.ingridientsinc.recipe.ui.SearchMealScreen

sealed class Screen(val route: String) {
    data object Browse : Screen("browse")
    data object Favorites : Screen("favorites")
    data object Add : Screen("add")
    data object Search : Screen("search")
    data object Detail : Screen("detail/{recipeId}") {
        fun createRoute(recipeId: Long) = "detail/$recipeId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: RecipeViewModel
) {
    val openDetail: (Long) -> Unit = { navController.navigate(Screen.Detail.createRoute(it)) }

    NavHost(navController = navController, startDestination = Screen.Browse.route) {
        composable(Screen.Browse.route) {
            RecipeListScreen(viewModel = viewModel, onRecipeClick = openDetail)
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(viewModel = viewModel, onRecipeClick = openDetail)
        }

        composable(Screen.Add.route) {
            AddRecipeScreen(
                recipeVm = viewModel,
                onSaved = {
                    navController.navigate(Screen.Browse.route) {
                        popUpTo(Screen.Add.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Search.route) {
            SearchMealScreen(recipeVm = viewModel)
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            RecipeDetailScreen(
                recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
