package com.ingridientsinc.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Thin wrapper over [RecipeStore]. It exists for [viewModelScope]: a save must not be
 * cancelled halfway through because a composable left the composition.
 *
 * Screens filter and group [recipes] themselves — one line each, no derived flows here.
 */
class RecipeViewModel(private val store: RecipeStore) : ViewModel() {

    val recipes: StateFlow<List<Recipe>> = store.recipes

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            store.mutate { list ->
                list.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it }
            }
        }
    }

    fun add(
        name: String,
        category: String,
        ingredients: List<Ingredient>,
        instructions: List<String>
    ) {
        viewModelScope.launch {
            store.mutate { list ->
                list + Recipe(
                    id = (list.maxOfOrNull(Recipe::id) ?: 0L) + 1,
                    name = name,
                    category = category,
                    ingredients = ingredients,
                    instructions = instructions
                )
            }
        }
    }
}
