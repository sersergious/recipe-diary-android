package com.ingridientsinc.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ingridientsinc.recipe.AddRecipeViewModel
import com.ingridientsinc.recipe.CATEGORIES
import com.ingridientsinc.recipe.Ingredient
import com.ingridientsinc.recipe.RecipeViewModel
import com.ingridientsinc.recipe.UNITS
import com.ingridientsinc.recipe.formatQuantity

@Composable
fun AddRecipeScreen(
    recipeVm: RecipeViewModel,
    onSaved: () -> Unit,
    addVm: AddRecipeViewModel = viewModel()
) {
    val state by addVm.state.collectAsStateWithLifecycle()
    var showSavedDialog by remember { mutableStateOf(false) }

    if (showSavedDialog) {
        SavedRecipeDialog(onDismiss = {
            showSavedDialog = false
            addVm.reset()
            onSaved()
        })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            AddRecipeHeader()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        value = state.recipeName,
                        onValueChange = { v -> addVm.update { it.copy(recipeName = v) } },
                        label = { Text("Recipe Name") },
                        isError = state.submitted && state.recipeName.isBlank(),
                        supportingText = {
                            if (state.submitted && state.recipeName.isBlank()) {
                                Text("Recipe name is required", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = recipeFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    CategoryDropdown(
                        selected = state.category,
                        expanded = state.categoryExpanded,
                        onExpandedChange = { v -> addVm.update { it.copy(categoryExpanded = v) } },
                        onSelected = { v ->
                            addVm.update { it.copy(category = v, categoryExpanded = false) }
                        }
                    )

                    IngredientInput(
                        nameInput = state.ingredientName,
                        quantityInput = state.ingredientQuantity,
                        selectedUnit = state.ingredientUnit,
                        items = state.ingredients,
                        isError = state.submitted && state.ingredients.isEmpty(),
                        onNameChange = { v -> addVm.update { it.copy(ingredientName = v) } },
                        onQuantityChange = { v -> addVm.update { it.copy(ingredientQuantity = v) } },
                        onUnitChange = { v -> addVm.update { it.copy(ingredientUnit = v) } },
                        onAdd = addVm::addIngredient,
                        onRemove = addVm::removeIngredient
                    )

                    InstructionInput(
                        input = state.instructionInput,
                        items = state.instructions,
                        isError = state.submitted && state.instructions.isEmpty(),
                        onInputChange = { v -> addVm.update { it.copy(instructionInput = v) } },
                        onAdd = addVm::addInstruction,
                        onRemove = addVm::removeInstruction
                    )

                    SaveRecipeButton(
                        enabled = addVm.isValid(),
                        onClick = {
                            addVm.markSubmitted()
                            if (!addVm.isValid()) return@SaveRecipeButton
                            recipeVm.add(
                                name = state.recipeName.trim(),
                                category = state.category,
                                ingredients = state.ingredients,
                                instructions = state.instructions
                            )
                            showSavedDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddRecipeHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = "New Recipe",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selected: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = recipeFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            CATEGORIES.forEach { category ->
                DropdownMenuItem(text = { Text(category) }, onClick = { onSelected(category) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientInput(
    nameInput: String,
    quantityInput: String,
    selectedUnit: String,
    items: List<Ingredient>,
    isError: Boolean,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (Ingredient) -> Unit
) {
    var unitExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(Icons.AutoMirrored.Filled.List, "Ingredients")

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = quantityInput,
                onValueChange = onQuantityChange,
                label = { Text("Qty") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = recipeFieldColors(),
                modifier = Modifier.width(90.dp)
            )
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = it },
                modifier = Modifier.width(130.dp)
            ) {
                OutlinedTextField(
                    value = selectedUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unit") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) },
                    shape = RoundedCornerShape(12.dp),
                    colors = recipeFieldColors(),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
                ) {
                    UNITS.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                onUnitChange(unit)
                                unitExpanded = false
                            }
                        )
                    }
                }
            }
        }

        AddItemRow(
            value = nameInput,
            label = "Ingredient name",
            isError = isError,
            errorText = "Add at least one ingredient",
            contentDescription = "Add ingredient",
            onValueChange = onNameChange,
            onAdd = onAdd
        )

        items.forEachIndexed { index, item ->
            NumberedRow(
                index = index + 1,
                text = "${item.formatQuantity()} ${item.unit} — ${item.name}",
                onRemove = { onRemove(item) }
            )
        }
    }
}

@Composable
private fun InstructionInput(
    input: String,
    items: List<String>,
    isError: Boolean,
    onInputChange: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(Icons.AutoMirrored.Filled.MenuBook, "Instructions")
        AddItemRow(
            value = input,
            label = "Add step",
            isError = isError,
            errorText = "Add at least one step",
            contentDescription = "Add step",
            onValueChange = onInputChange,
            onAdd = onAdd
        )
        items.forEachIndexed { index, item ->
            NumberedRow(index = index + 1, text = item, onRemove = { onRemove(item) })
        }
    }
}

/** Text field plus an add button — the shape both list inputs use. */
@Composable
private fun AddItemRow(
    value: String,
    label: String,
    isError: Boolean,
    errorText: String,
    contentDescription: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = isError,
            supportingText = {
                if (isError) Text(errorText, color = MaterialTheme.colorScheme.error)
            },
            shape = RoundedCornerShape(12.dp),
            colors = recipeFieldColors(),
            modifier = Modifier.weight(1f)
        )
        FilledIconButton(
            onClick = onAdd,
            shape = RoundedCornerShape(12.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun SaveRecipeButton(onClick: () -> Unit, enabled: Boolean) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Save Recipe", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SavedRecipeDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text("Recipe Saved!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Your recipe has been saved successfully.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("Great!") }
        }
    )
}
