// dieta/src/main/java/com/example/dieta/domain/DietaModels.kt
package com.example.dieta.domain

/**
 * Modelos de domínio usados pela UI / use cases da feature de dieta.
 * Não expõem entidades Room nem DTOs de rede diretamente.
 */

// --------- Refeições favoritas ---------

data class Refeicao(
    val id: Int,
    val nome: String,
    val userId: Int,
)

// Ingrediente que compõe uma refeição favorita
data class IngredienteRefeicao(
    val id: Int,
    val alimentoApiId: String,
    val porcaoGramas: Double,
    val quantidadePorcoes: Double,
    val refeicaoId: Int,
)

// Ligação simples de receita FatSecret favoritada pelo utilizador
data class ReceitaFavorita(
    val id: Int,
    val userId: Int,
    val receitaApiId: String,
)

// --------- FatSecret: alimentos ---------

data class FatSecretFood(
    val id: String,
    val nomeEn: String,
    val descricaoEn: String,
    val tipo: String,
    val url: String,
    val calories: Int?,
    val carbsGrams: Double?,
    val proteinGrams: Double?,
    val fatGrams: Double?,
    val carbsPct: Int?,
    val proteinPct: Int?,
    val fatPct: Int?,
)

// Detalhes de um alimento específico
data class FatSecretFoodDetails(
    val id: String,
    val nomeEn: String,
    val porcao: String,
    val calorias: String,
    val proteina: String,
    val gordura: String,
    val carboidratos: String,
    val image: String?,        // novo
)



// --------- FatSecret: receitas (lista) ---------

data class FatSecretRecipeNutrition(
    val calories: String,
    val carbohydrate: String,
    val fat: String,
    val protein: String,
)

data class FatSecretRecipeSummary(
    val id: String,
    val nomeEn: String,
    val descricaoEn: String,
    val image: String?,
    val nutrition: FatSecretRecipeNutrition,
    val calories: String,
    val types: List<String>,
)

// Resultado da pesquisa de receitas FatSecret
data class FatSecretRecipeSearchResult(
    val sucesso: Boolean,
    val encontrados: Int,
    val receitas: List<FatSecretRecipeSummary>,
)

// --------- FatSecret: receita detalhada ---------

data class FatSecretRecipeCategory(
    val name: String,
    val url: String,
)

data class FatSecretRecipeServing(
    val calcium: String,
    val calories: String,
    val carbohydrate: String,
    val cholesterol: String,
    val fat: String,
    val fiber: String,
    val iron: String,
    val monounsaturatedFat: String,
    val polyunsaturatedFat: String,
    val potassium: String,
    val protein: String,
    val saturatedFat: String,
    val servingSize: String,
    val sodium: String,
    val sugar: String,
    val transFat: String,
    val vitaminA: String,
    val vitaminC: String,
)

data class FatSecretRecipeIngredient(
    val foodId: String,
    val foodName: String,
    val ingredientDescription: String,
    val ingredientUrl: String,
    val measurementDescription: String,
    val numberOfUnits: String,
    val servingId: String,
)

data class FatSecretRecipeDirection(
    val description: String,
    val number: String,
)

data class FatSecretRecipe(
    val id: String,
    val name: String,
    val url: String,
    val description: String,
    val numberOfServings: Int,
    val gramsPerPortion: Double,
    val preparationTimeMin: Int,
    val cookingTimeMin: Int,
    val rating: Int,
    val images: List<String>,
    val types: List<String>,
    val categories: List<FatSecretRecipeCategory>,
    val servings: List<FatSecretRecipeServing>,
    val ingredients: List<FatSecretRecipeIngredient>,
    val directions: List<FatSecretRecipeDirection>,
)
