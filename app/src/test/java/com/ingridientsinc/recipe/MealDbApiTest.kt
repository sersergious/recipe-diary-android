package com.ingridientsinc.recipe

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class MealDbApiTest {

    @Test
    fun `parseMeasure reads whole, fraction and mixed-fraction quantities`() {
        assertEquals(2f to "cups", parseMeasure("2 cups"))
        assertEquals(0.75f to "cup", parseMeasure("3/4 cup"))
        assertEquals(1.5f to "cups plain flour", parseMeasure("1 1/2 cups plain flour"))
        assertEquals(1f to "to taste", parseMeasure("to taste"))
        assertEquals(1f to "piece", parseMeasure(""))
    }

    @Test
    fun `toInstructionSteps splits on newlines and strips numbered prefixes`() {
        val raw = "1. Preheat the oven.\r\n2) Mix everything.\n\nBake for 20 minutes."
        assertEquals(
            listOf("Preheat the oven.", "Mix everything.", "Bake for 20 minutes."),
            raw.toInstructionSteps()
        )
    }

    @Test
    fun `toMealDbMeal maps ingredients, maps unknown category to Dinner, and skips blanks`() {
        val json = JSONObject()
            .put("idMeal", "52772")
            .put("strMeal", "Teriyaki Chicken Casserole")
            .put("strCategory", "Chicken")
            .put("strInstructions", "1. Preheat oven.\r\n2. Combine sauce ingredients.")
            .put("strIngredient1", "soy sauce")
            .put("strMeasure1", "3/4 cup")
            .put("strIngredient2", "")
            .put("strMeasure2", "")
            .put("strIngredient3", "brown sugar")
            .put("strMeasure3", "1/2 cup packed")

        val meal = json.toMealDbMeal()

        assertEquals("52772", meal.id)
        assertEquals("Teriyaki Chicken Casserole", meal.name)
        assertEquals("Dinner", meal.category)
        assertEquals(
            listOf(
                Ingredient("soy sauce", 0.75f, "cup"),
                Ingredient("brown sugar", 0.5f, "cup packed")
            ),
            meal.ingredients
        )
        assertEquals(listOf("Preheat oven.", "Combine sauce ingredients."), meal.instructions)
    }

    @Test
    fun `toMealDbMeal maps Breakfast and Dessert straight through`() {
        val breakfast = JSONObject()
            .put("idMeal", "1")
            .put("strMeal", "Pancakes")
            .put("strCategory", "Breakfast")
            .put("strInstructions", "Mix and fry.")
        assertEquals("Breakfast", breakfast.toMealDbMeal().category)

        val dessert = JSONObject()
            .put("idMeal", "2")
            .put("strMeal", "Cake")
            .put("strCategory", "Dessert")
            .put("strInstructions", "Bake it.")
        assertEquals("Dessert", dessert.toMealDbMeal().category)
    }
}
