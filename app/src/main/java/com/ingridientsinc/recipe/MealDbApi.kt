package com.ingridientsinc.recipe

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** One search result from TheMealDB, already shaped for [RecipeViewModel.add]. */
data class MealDbMeal(
    val id: String,
    val name: String,
    val category: String,
    val ingredients: List<Ingredient>,
    val instructions: List<String>
)

/**
 * TheMealDB search — free test key ("1"), no signup, matches this app's zero-config
 * philosophy. Plain [HttpURLConnection] and the [org.json] already used by [RecipeStore];
 * no networking dependency for one endpoint.
 */
object MealDbApi {

    suspend fun search(query: String): List<MealDbMeal> = withContext(Dispatchers.IO) {
        val url = URL("https://www.themealdb.com/api/json/v1/1/search.php?s=${URLEncoder.encode(query, "UTF-8")}")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val meals = JSONObject(body).optJSONArray("meals") ?: return@withContext emptyList()
            (0 until meals.length()).map { meals.getJSONObject(it).toMealDbMeal() }
        } finally {
            connection.disconnect()
        }
    }
}

/** This app only has Breakfast/Lunch/Dinner/Dessert; everything unmapped lands in Dinner. */
private val CATEGORY_MAP = mapOf(
    "Breakfast" to "Breakfast",
    "Dessert" to "Dessert",
    "Starter" to "Lunch",
    "Side" to "Lunch"
)

internal fun JSONObject.toMealDbMeal(): MealDbMeal = MealDbMeal(
    id = getString("idMeal"),
    name = getString("strMeal"),
    category = CATEGORY_MAP[optString("strCategory")] ?: "Dinner",
    ingredients = (1..20).mapNotNull { i ->
        val name = optString("strIngredient$i").trim()
        if (name.isBlank() || name.equals("null", ignoreCase = true)) return@mapNotNull null
        val (quantity, unit) = parseMeasure(optString("strMeasure$i"))
        Ingredient(name = name, quantity = quantity, unit = unit)
    },
    instructions = optString("strInstructions").toInstructionSteps()
)

private val STEP_PREFIX = Regex("""^\d+[.):]\s*""")

internal fun String.toInstructionSteps(): List<String> =
    split(Regex("\r\n|\n|\r"))
        .map { it.trim().replace(STEP_PREFIX, "") }
        .filter { it.isNotBlank() }

private val MIXED_FRACTION = Regex("""^(\d+)\s+(\d+)/(\d+)\s*(.*)$""")
private val FRACTION = Regex("""^(\d+)/(\d+)\s*(.*)$""")
private val WHOLE_NUMBER = Regex("""^(\d+(?:\.\d+)?)\s*(.*)$""")

/**
 * "1 1/2 cups", "3/4 tsp", "2", "to taste" -> (quantity, unit). Free-text unit, since
 * TheMealDB measures ("g", "cloves", "handful") don't fit this app's [UNITS] dropdown —
 * imported ingredients just aren't editable through that picker.
 */
internal fun parseMeasure(raw: String): Pair<Float, String> {
    val text = raw.trim()
    if (text.isEmpty()) return 1f to "piece"

    MIXED_FRACTION.find(text)?.let { match ->
        val (whole, num, den, rest) = match.destructured
        return (whole.toFloat() + num.toFloat() / den.toFloat()) to rest.trim().ifBlank { "piece" }
    }
    FRACTION.find(text)?.let { match ->
        val (num, den, rest) = match.destructured
        return (num.toFloat() / den.toFloat()) to rest.trim().ifBlank { "piece" }
    }
    WHOLE_NUMBER.find(text)?.let { match ->
        val (num, rest) = match.destructured
        return num.toFloat() to rest.trim().ifBlank { "piece" }
    }
    return 1f to text
}
