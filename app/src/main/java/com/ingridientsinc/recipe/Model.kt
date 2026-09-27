package com.ingridientsinc.recipe

data class Ingredient(
    val name: String,
    val quantity: Float,
    val unit: String
)

data class Recipe(
    val id: Long,
    val name: String,
    val category: String,
    val isFavorite: Boolean = false,
    val ingredients: List<Ingredient> = emptyList(),
    // ponytail: step number is the list index. No separate field to keep in sync.
    val instructions: List<String> = emptyList()
)

/** The one place categories are defined: dropdown, browse grouping and seed all read this. */
val CATEGORIES = listOf("Breakfast", "Lunch", "Dinner", "Dessert")

val UNITS = listOf("tsp", "tbsp", "cup", "fl oz", "pt", "qt", "gal", "oz", "lb", "pinch", "piece")

/** "2 cup", "1.5 tsp" — trims the decimal when the quantity is whole. */
fun Ingredient.formatQuantity(): String =
    if (quantity == quantity.toLong().toFloat()) quantity.toLong().toString()
    else "%.2f".format(quantity).trimEnd('0').trimEnd('.')
