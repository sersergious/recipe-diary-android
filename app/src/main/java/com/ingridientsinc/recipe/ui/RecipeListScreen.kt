package com.ingridientsinc.recipe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ingridientsinc.recipe.CATEGORIES
import com.ingridientsinc.recipe.Recipe
import com.ingridientsinc.recipe.RecipeViewModel

@Composable
fun RecipeListScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (Long) -> Unit
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CATEGORIES.forEach { category ->
            val inCategory = recipes.filter { it.category == category }.sortedBy { it.name }
            if (inCategory.isEmpty()) return@forEach

            item(key = category) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(inCategory, key = { it.id }) { recipe ->
                RecipeItem(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) },
                    onFavoriteToggle = { viewModel.toggleFavorite(recipe.id) }
                )
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (Long) -> Unit
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val favorites = recipes.filter { it.isFavorite }

    if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No favorites yet — tap the heart on a recipe to add it.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(32.dp)
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(favorites, key = { it.id }) { recipe ->
            RecipeItem(
                recipe = recipe,
                onClick = { onRecipeClick(recipe.id) },
                onFavoriteToggle = { viewModel.toggleFavorite(recipe.id) }
            )
        }
    }
}

@Composable
fun RecipeItem(
    recipe: Recipe,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (recipe.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (recipe.isFavorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
