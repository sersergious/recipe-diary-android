package com.ingridientsinc.recipe

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AddRecipeState(
    val recipeName: String = "",
    val category: String = CATEGORIES.first(),
    val categoryExpanded: Boolean = false,
    val ingredientName: String = "",
    val ingredientQuantity: String = "",
    val ingredientUnit: String = "cup",
    val instructionInput: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<String> = emptyList(),
    val submitted: Boolean = false
)

/**
 * Form state for the add screen. Scoped to that nav entry, so it survives rotation
 * without hand-written Savers for the ingredient list.
 */
class AddRecipeViewModel : ViewModel() {

    private val _state = MutableStateFlow(AddRecipeState())
    val state: StateFlow<AddRecipeState> = _state.asStateFlow()

    fun update(transform: (AddRecipeState) -> AddRecipeState) {
        _state.value = transform(_state.value)
    }

    fun addIngredient() = update { s ->
        if (s.ingredientName.isBlank()) return@update s
        s.copy(
            ingredients = s.ingredients + Ingredient(
                name = s.ingredientName.trim(),
                quantity = s.ingredientQuantity.toFloatOrNull() ?: 0f,
                unit = s.ingredientUnit
            ),
            ingredientName = "",
            ingredientQuantity = ""
        )
    }

    fun removeIngredient(item: Ingredient) = update { it.copy(ingredients = it.ingredients - item) }

    fun addInstruction() = update { s ->
        if (s.instructionInput.isBlank()) return@update s
        s.copy(
            instructions = s.instructions + s.instructionInput.trim(),
            instructionInput = ""
        )
    }

    fun removeInstruction(item: String) = update { it.copy(instructions = it.instructions - item) }

    fun markSubmitted() = update { it.copy(submitted = true) }

    fun isValid(): Boolean = _state.value.let {
        it.recipeName.isNotBlank() && it.ingredients.isNotEmpty() && it.instructions.isNotEmpty()
    }

    fun reset() {
        _state.value = AddRecipeState()
    }
}
