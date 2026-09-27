package com.ingridientsinc.recipe

import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeStoreTest {

    @Test
    fun `encode then decode round trips every field`() {
        val recipes = listOf(
            Recipe(
                id = 7,
                name = "Toast",
                category = "Breakfast",
                isFavorite = true,
                ingredients = listOf(
                    Ingredient("bread", 2f, "piece"),
                    Ingredient("butter", 0.5f, "tbsp")
                ),
                instructions = listOf("Toast it", "Butter it")
            ),
            Recipe(id = 8, name = "Water", category = "Dinner")
        )

        assertEquals(recipes, decode(encode(recipes)))
    }

    @Test
    fun `quotes and newlines survive the round trip`() {
        val recipes = listOf(
            Recipe(1, "He said \"hi\"", "Lunch", instructions = listOf("line one\nline two"))
        )

        assertEquals(recipes, decode(encode(recipes)))
    }

    @Test
    fun `a corrupt file falls back to the seed instead of crashing`() {
        assertEquals(SEED, decode("{not json"))
        assertEquals(SEED, decode(""))
        assertEquals(SEED, decode("""[{"id":1}]"""))
    }
}
