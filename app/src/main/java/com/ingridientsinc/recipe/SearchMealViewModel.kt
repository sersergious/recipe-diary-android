package com.ingridientsinc.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import org.json.JSONException

data class SearchMealState(
    val query: String = "",
    val results: List<MealDbMeal> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val importedIds: Set<String> = emptySet()
)

/** Search state for [MealDbApi]; import hands the mapped fields to [RecipeViewModel.add]. */
class SearchMealViewModel : ViewModel() {

    private val _state = MutableStateFlow(SearchMealState())
    val state: StateFlow<SearchMealState> = _state.asStateFlow()

    fun updateQuery(query: String) {
        _state.value = _state.value.copy(query = query)
    }

    fun search() {
        val query = _state.value.query.trim()
        if (query.isBlank()) return

        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            _state.value = try {
                _state.value.copy(loading = false, results = MealDbApi.search(query))
            } catch (e: IOException) {
                _state.value.copy(loading = false, error = "Couldn't reach TheMealDB. Check your connection and try again.")
            } catch (e: JSONException) {
                _state.value.copy(loading = false, error = "TheMealDB returned something unexpected. Try again.")
            }
        }
    }

    fun import(meal: MealDbMeal, recipeVm: RecipeViewModel) {
        recipeVm.add(
            name = meal.name,
            category = meal.category,
            ingredients = meal.ingredients,
            instructions = meal.instructions
        )
        _state.value = _state.value.copy(importedIds = _state.value.importedIds + meal.id)
    }
}
