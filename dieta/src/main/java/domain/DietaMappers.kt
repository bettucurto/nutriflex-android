// dieta/src/main/java/com/example/dieta/domain/DietaMappers.kt
package com.example.dieta.domain

import com.example.dieta.remote.FatSecretFoodDetailsDto
import com.example.dieta.remote.FatSecretFoodDto
import com.example.dieta.remote.FatSecretRecipeCategoryDto
import com.example.dieta.remote.FatSecretRecipeDetailsDto
import com.example.dieta.remote.FatSecretRecipeDirectionDto
import com.example.dieta.remote.FatSecretRecipeIngredientDto
import com.example.dieta.remote.FatSecretRecipeNutritionDto
import com.example.dieta.remote.FatSecretRecipeSearchResponse
import com.example.dieta.remote.FatSecretRecipeServingDto
import com.example.dieta.remote.FatSecretRecipeSummaryDto
import com.example.dieta.remote.IngredienteDto
import com.example.dieta.remote.ReceitaFavoritaDto
import com.example.dieta.remote.RefeicaoDto
import data.local.tables.IngredienteRefeicaoLocal
import data.local.tables.ReceitaFavoritaLocal
import data.local.tables.RefeicaoFavoritaLocal

// --------- Refeições favoritas ---------

fun RefeicaoFavoritaLocal.toDomain(): Refeicao =
    Refeicao(
        id = id,
        nome = nome,
        userId = idUser,
    )

fun RefeicaoDto.toDomain(): Refeicao =
    Refeicao(
        id = id,
        nome = nome,
        userId = iduser,
    )

fun Refeicao.toLocal(): RefeicaoFavoritaLocal =
    RefeicaoFavoritaLocal(
        id = id,
        nome = nome,
        idUser = userId,
    )

// --------- Ingredientes ---------

fun IngredienteRefeicaoLocal.toDomain(): IngredienteRefeicao =
    IngredienteRefeicao(
        id = id,
        alimentoApiId = alimentoApiId,
        porcaoGramas = porcaoGramas,
        quantidadePorcoes = quantidadePorcoes,
        refeicaoId = idRefeicao,
    )

fun IngredienteDto.toDomain(): IngredienteRefeicao =
    IngredienteRefeicao(
        id = id,
        alimentoApiId = alimentoapiid,
        porcaoGramas = porcaogramas,
        quantidadePorcoes = quantidadeporcoes,
        refeicaoId = idrefeicao ?: 0,
    )

fun IngredienteRefeicao.toLocal(): IngredienteRefeicaoLocal =
    IngredienteRefeicaoLocal(
        id = id,
        alimentoApiId = alimentoApiId,
        porcaoGramas = porcaoGramas,
        quantidadePorcoes = quantidadePorcoes,
        idRefeicao = refeicaoId,
    )

// --------- Receitas favoritas ---------

fun ReceitaFavoritaLocal.toDomain(): ReceitaFavorita =
    ReceitaFavorita(
        id = id,
        userId = idUser,
        receitaApiId = idReceitaApi,
    )

fun ReceitaFavoritaDto.toDomain(): ReceitaFavorita =
    ReceitaFavorita(
        id = id,
        userId = iduser,
        receitaApiId = idreceitaapi,
    )

fun ReceitaFavorita.toLocal(): ReceitaFavoritaLocal =
    ReceitaFavoritaLocal(
        id = id,
        idUser = userId,
        idReceitaApi = receitaApiId,
    )

// --------- FatSecret alimentos ---------

fun FatSecretFoodDto.toDomain(): FatSecretFood =
    FatSecretFood(
        id = id,
        nomeEn = nome_en,
        descricaoEn = descricao_en,
        tipo = tipo,
        url = url,
        calories = calories?.toIntOrNull(),
        carbsGrams = carbs_grams?.toDoubleOrNull(),
        proteinGrams = protein_grams?.toDoubleOrNull(),
        fatGrams = fat_grams?.toDoubleOrNull(),
        carbsPct = macro_split?.carbs,
        proteinPct = macro_split?.protein,
        fatPct = macro_split?.fat,
    )




fun FatSecretFoodDetailsDto.toDomain(): FatSecretFoodDetails =
    FatSecretFoodDetails(
        id = id,
        nomeEn = nome_en,
        porcao = porcao,
        calorias = calorias,
        proteina = proteina,
        gordura = gordura,
        carboidratos = carboidratos,
        image = image,          // novo
    )


// --------- FatSecret receitas (lista) ---------

fun FatSecretRecipeNutritionDto.toDomain(): FatSecretRecipeNutrition =
    FatSecretRecipeNutrition(
        calories = calories,
        carbohydrate = carbohydrate,
        fat = fat,
        protein = protein,
    )

fun FatSecretRecipeSummaryDto.toDomain(): FatSecretRecipeSummary =
    FatSecretRecipeSummary(
        id = id,
        nomeEn = nome_en,
        descricaoEn = descricao_en,
        image = image,
        nutrition = nutrition.toDomain(),
        calories = calories,
        types = types,
    )

fun FatSecretRecipeSearchResponse.toDomain(): FatSecretRecipeSearchResult =
    FatSecretRecipeSearchResult(
        sucesso = sucesso,
        encontrados = encontrados,
        receitas = receitas.map { it.toDomain() },
    )

// --------- FatSecret receita detalhada ---------

fun FatSecretRecipeCategoryDto.toDomain(): FatSecretRecipeCategory =
    FatSecretRecipeCategory(
        name = name,
        url = url,
    )

fun FatSecretRecipeServingDto.toDomain(): FatSecretRecipeServing =
    FatSecretRecipeServing(
        calcium = calcium,
        calories = calories,
        carbohydrate = carbohydrate,
        cholesterol = cholesterol,
        fat = fat,
        fiber = fiber,
        iron = iron,
        monounsaturatedFat = monounsaturated_fat,
        polyunsaturatedFat = polyunsaturated_fat,
        potassium = potassium,
        protein = protein,
        saturatedFat = saturated_fat,
        servingSize = serving_size,
        sodium = sodium,
        sugar = sugar,
        transFat = trans_fat,
        vitaminA = vitamin_a,
        vitaminC = vitamin_c,
    )

fun FatSecretRecipeIngredientDto.toDomain(): FatSecretRecipeIngredient =
    FatSecretRecipeIngredient(
        foodId = food_id,
        foodName = food_name,
        ingredientDescription = ingredient_description,
        ingredientUrl = ingredient_url,
        measurementDescription = measurement_description,
        numberOfUnits = number_of_units,
        servingId = serving_id,
    )

fun FatSecretRecipeDirectionDto.toDomain(): FatSecretRecipeDirection =
    FatSecretRecipeDirection(
        description = direction_description,
        number = direction_number,
    )

fun FatSecretRecipeDetailsDto.toDomain(): FatSecretRecipe =
    FatSecretRecipe(
        id = id,
        name = name,
        url = url,
        description = description,
        numberOfServings = number_of_servings,
        gramsPerPortion = grams_per_portion,
        preparationTimeMin = preparation_time_min,
        cookingTimeMin = cooking_time_min,
        rating = rating,
        images = images,
        types = types,
        categories = categories.map { it.toDomain() },
        servings = servings.map { it.toDomain() },
        ingredients = ingredients.map { it.toDomain() },
        directions = directions.map { it.toDomain() },
    )
