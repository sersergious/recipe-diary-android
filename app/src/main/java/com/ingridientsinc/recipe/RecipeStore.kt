package com.ingridientsinc.recipe

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

/**
 * The whole recipe library in one JSON file, rewritten on every change.
 *
 * ponytail: fine up to a few hundred recipes on a personal device. If it ever needs
 * partial reads, indexed queries or migrations, swap [encode]/[decode] for Room — nothing
 * above this class knows the storage format.
 */
class RecipeStore(private val file: File) {

    private val _recipes = MutableStateFlow(
        if (file.exists()) decode(file.readText()) else SEED
    )
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    suspend fun mutate(block: (List<Recipe>) -> List<Recipe>) {
        val next = block(_recipes.value)
        _recipes.value = next
        withContext(Dispatchers.IO) { file.writeText(encode(next)) }
    }
}

// ponytail: one store per process, built on first use. A DI container for a single
// object is not worth its own framework. Only touched from the main thread (composition).
private var instance: RecipeStore? = null

fun recipeStore(context: Context): RecipeStore = instance ?: RecipeStore(
    File(context.applicationContext.filesDir, "recipes.json")
).also { instance = it }

internal fun encode(recipes: List<Recipe>): String =
    JSONArray(
        recipes.map { recipe ->
            JSONObject()
                .put("id", recipe.id)
                .put("name", recipe.name)
                .put("category", recipe.category)
                .put("isFavorite", recipe.isFavorite)
                .put("ingredients", JSONArray(recipe.ingredients.map {
                    JSONObject()
                        .put("name", it.name)
                        .put("quantity", it.quantity.toDouble())
                        .put("unit", it.unit)
                }))
                .put("instructions", JSONArray(recipe.instructions))
        }
    ).toString(2)

/**
 * Falls back to [SEED] on anything malformed — a half-written or hand-edited file must
 * not stop the app from launching.
 */
internal fun decode(text: String): List<Recipe> = try {
    JSONArray(text).objects().map { o ->
        Recipe(
            id = o.getLong("id"),
            name = o.getString("name"),
            category = o.getString("category"),
            isFavorite = o.optBoolean("isFavorite"),
            ingredients = o.optJSONArray("ingredients").objects().map {
                Ingredient(
                    name = it.getString("name"),
                    quantity = it.getDouble("quantity").toFloat(),
                    unit = it.getString("unit")
                )
            },
            instructions = o.optJSONArray("instructions").strings()
        )
    }
} catch (e: JSONException) {
    SEED
}

private fun JSONArray?.objects(): List<JSONObject> =
    if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }

private fun JSONArray?.strings(): List<String> =
    if (this == null) emptyList() else (0 until length()).map { getString(it) }

/** Written on first launch so the app opens with something to look at. */
internal val SEED = listOf(
    Recipe(
        id = 1,
        name = "Avocado Toast",
        category = "Breakfast",
        ingredients = listOf(
            Ingredient("sourdough bread", 2f, "piece"),
            Ingredient("ripe avocado", 1f, "piece"),
            Ingredient("lemon juice", 1f, "tsp")
        ),
        instructions = listOf(
            "Toast the bread until deeply golden.",
            "Mash the avocado with the lemon juice, salt and pepper.",
            "Spread thickly over the toast and finish with chilli flakes."
        )
    ),
    Recipe(
        id = 2,
        name = "Weeknight Tomato Pasta",
        category = "Dinner",
        ingredients = listOf(
            Ingredient("spaghetti", 8f, "oz"),
            Ingredient("olive oil", 2f, "tbsp"),
            Ingredient("garlic cloves", 3f, "piece"),
            Ingredient("crushed tomatoes", 14f, "oz")
        ),
        instructions = listOf(
            "Boil the spaghetti in well salted water until al dente.",
            "Sizzle the sliced garlic in the oil until fragrant but not brown.",
            "Add the tomatoes, simmer 10 minutes, then toss with the drained pasta."
        )
    )
)
