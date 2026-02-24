package com.example.nutriflex2

import android.os.Build
import android.view.animation.OvershootInterpolator
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.nutriflex2.diet.search.SearchMealsScreen
import com.example.nutriflex2.home.account.AccountScreen
import com.example.nutriflex2.home.ui.HomeScreen
import com.example.nutriflex2.home.ui.tabs.diet.favorites.FavoriteMealEditorScreen
import com.example.nutriflex2.home.ui.tabs.diet.favorites.FavoriteMealEditorViewModel
import com.example.nutriflex2.home.ui.tabs.diet.info.meals.MealInfoScreen
import com.example.nutriflex2.home.ui.tabs.diet.info.recipes.RecipeInfoScreen
import com.example.nutriflex2.home.ui.tabs.diet.search.recipes.SearchRecipeScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.WelcomeScreen
import ui.login.LoginScreen
import ui.registration.RegisterViewModel
import ui.registration.RegistrationScreen1
import ui.registration.RegistrationScreen2
import ui.registration.RegistrationScreen3
import ui.registration.RegistrationScreen4
import ui.registration.RegistrationScreen5
import ui.registration.RegistrationScreen6

private fun screenOrder(route: String?): Int = when (route) {
    "welcomeScreen" -> 0
    "loginScreen" -> 1
    "registrationScreen1" -> 2
    "registrationScreen6" -> 3
    "registrationScreen2" -> 4
    "registrationScreen3" -> 5
    "registrationScreen4" -> 6
    "registrationScreen5" -> 7
    else -> -1
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splashScreen",
        enterTransition = { defaultEnterTransition(initialState, targetState) },
        exitTransition = { defaultExitTransition(initialState, targetState) }
    ) {
        composable("splashScreen") { SplashScreen(navController) }
        composable("welcomeScreen") { WelcomeScreen(navController) }
        composable("loginScreen") { LoginScreen(navController) }

        composable("homeScreen") {
            HomeScreen(
                onNavigateToTreino = { /* ... */ },
                onNavigateToSearchMeals = { navController.navigate("searchMealsScreen") },
                onNavigateToSearchRecipes = { navController.navigate("searchRecipeScreen") },
                onNavigateToAccount = { navController.navigate("accountScreen") }
            )
        }

        // --- Fluxo de Pesquisa Diária (Log normal) ---
        composable("searchMealsScreen") {
            SearchMealsScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                onOpenFavorites = { /* TODO */ },

                // MUDANÇA 1: Ao clicar em Create Meal, iniciamos o FLUXO ANINHADO
                onOpenCreateMeal = { navController.navigate("create_meal_flow") },

                // Comportamento normal: vai para detalhes para logar no dia
                onFoodClick = { foodId ->
                    navController.navigate("foodDetail/$foodId")
                }
            )
        }

        composable("searchRecipeScreen") {
            SearchRecipeScreen(
                onBack = { navController.popBackStack() },
                onOpenFavorites = { /* TODO */ },
                onOpenPhoto = { /* TODO */ },
                navController = navController,
            )
        }

        // --- Detalhes do Alimento (Modo: Log Diário) ---
        composable(
            route = "foodDetail/{foodId}",
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: ""

            // Aqui mantemos a lógica original para logar no diário
            MealInfoScreen(
                foodId = foodId,
                onBack = { navController.popBackStack() },
                onReturnIngredient = null, // NULL = Modo Log Diário
                onAddToMealCompleted = {
                    // Volta para a home
                    navController.navigate("homeScreen") {
                        popUpTo("homeScreen") { inclusive = true }
                    }
                }
            )
        }

        // --- Detalhes da Receita ---
        composable(
            route = "recipeDetail/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            val scope = rememberCoroutineScope()

            RecipeInfoScreen(
                recipeId = recipeId,
                onBack = { navController.popBackStack() },
                onAddToMeal = {
                    scope.launch {
                        navController.navigate("homeScreen") {
                            popUpTo("homeScreen") { inclusive = true }
                        }
                    }
                },
                onIngredientClick = { foodId ->
                    navController.navigate("foodDetail/$foodId")
                }
            )
        }

        // === NOVO FLUXO ANINHADO: CRIAR REFEIÇÃO FAVORITA ===
        // Todos os ecrãs aqui dentro partilham o mesmo FavoriteMealViewModel
        navigation(
            startDestination = "favoriteMealEditor",
            route = "create_meal_flow"
        ) {

            // 1. O Editor (Lista de ingredientes, Nome, Botão Salvar)
            composable("favoriteMealEditor") { backStackEntry ->
                // Obtém o ViewModel associado ao GRAFO, não ao ecrã
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("create_meal_flow")
                }
                val sharedViewModel: FavoriteMealEditorViewModel = hiltViewModel(parentEntry)

                FavoriteMealEditorScreen(
                    viewModel = sharedViewModel,
                    onBack = { navController.popBackStack() }, // Sai do fluxo
                    onAddFoodClick = {
                        // Navega para a pesquisa DENTRO deste fluxo
                        navController.navigate("meal_creation_search")
                    },
                    onMealSaved = {
                        // Sucesso: volta para a Home
                        navController.navigate("homeScreen") {
                            popUpTo("homeScreen") { inclusive = true }
                        }
                    }
                )
            }

            // 2. A Pesquisa (para adicionar ao rascunho)
            composable("meal_creation_search") {
                SearchMealsScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    onOpenFavorites = { }, // Desativado neste modo
                    onOpenCreateMeal = { }, // Desativado neste modo

                    // IMPORTANTE: Navega para a versão "creation" do detalhe
                    onFoodClick = { foodId ->
                        navController.navigate("meal_creation_detail/$foodId")
                    }
                )
            }

            // 3. O Detalhe (Modo: Retornar Ingrediente)
            composable(
                route = "meal_creation_detail/{foodId}",
                arguments = listOf(navArgument("foodId") { type = NavType.StringType })
            ) { backStackEntry ->
                val foodId = backStackEntry.arguments?.getString("foodId") ?: ""

                // Precisamos do SharedViewModel para chamar o método addIngredient
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("create_meal_flow")
                }
                val sharedViewModel: FavoriteMealEditorViewModel = hiltViewModel(parentEntry)

                MealInfoScreen(
                    foodId = foodId,
                    onBack = { navController.popBackStack() },

                    // MODO RASCUNHO ATIVADO: Passamos a lambda
                    onReturnIngredient = { food, serving, qty ->
                        sharedViewModel.addIngredient(food, serving, qty)
                    },

                    onAddToMealCompleted = {
                        // Volta direto para o Editor, removendo a pesquisa e o detalhe da stack
                        navController.navigate("favoriteMealEditor") {
                            popUpTo("favoriteMealEditor") { inclusive = true }
                        }
                    }
                )
            }
        }
        // === FIM DO FLUXO ANINHADO ===

        composable("accountScreen") {
            AccountScreen(
                onBack = { navController.navigate("homeScreen")},
                onLogout = {
                    navController.navigate("welcomeScreen") {
                        popUpTo("homeScreen") { inclusive = true }
                    }
                }
            )
        }

        navigation(
            startDestination = "registrationScreen1",
            route = "registrationFlow"
        ) {
            // ... (Telas de registo mantêm-se iguais) ...
            composable("registrationScreen1") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("registrationFlow")
                }
                val viewModel: RegisterViewModel = hiltViewModel(parentEntry)
                RegistrationScreen1(navController, viewModel)
            }
            composable("registrationScreen6") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("registrationFlow")
                }
                val viewModel: RegisterViewModel = hiltViewModel(parentEntry)
                RegistrationScreen6(navController, viewModel)
            }
            composable("registrationScreen2") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("registrationFlow")
                }
                val viewModel: RegisterViewModel = hiltViewModel(parentEntry)
                RegistrationScreen2(navController, viewModel)
            }
            composable("registrationScreen3") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("registrationFlow")
                }
                val viewModel: RegisterViewModel = hiltViewModel(parentEntry)
                RegistrationScreen3(navController, viewModel)
            }
            composable("registrationScreen4") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("registrationFlow")
                }
                val viewModel: RegisterViewModel = hiltViewModel(parentEntry)
                RegistrationScreen4(navController, viewModel)
            }
            composable("registrationScreen5") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("registrationFlow")
                }
                val viewModel: RegisterViewModel = hiltViewModel(parentEntry)
                RegistrationScreen5(navController, viewModel)
            }
        }
    }
}


private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultEnterTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry
): EnterTransition {
    val initialOrder = screenOrder(initial.destination.route)
    val targetOrder = screenOrder(target.destination.route)

    if (initial.destination.route == "splashScreen" && target.destination.route == "welcomeScreen") {
        return fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.9f)
    }

    val forward = targetOrder > initialOrder

    return if (forward) {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(500)
        )
    } else {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(500)
        )
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultExitTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry
): ExitTransition {
    val initialOrder = screenOrder(initial.destination.route)
    val targetOrder = screenOrder(target.destination.route)
    val forward = targetOrder > initialOrder

    return if (forward) {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(500)
        )
    } else {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(500)
        )
    }
}


@Composable
fun SplashScreen(navController: NavController, viewModel: SplashViewModel = hiltViewModel()) {
    val scale = remember { Animatable(0f) }
    val destination by viewModel.destination.collectAsState()

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 0.9f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = { OvershootInterpolator(3f).getInterpolation(it) }
            )
        )
    }

    LaunchedEffect(destination) {
        val dest = destination ?: return@LaunchedEffect
        delay(300L)
        when (dest) {
            SplashDestination.ToWelcome -> {
                navController.navigate("welcomeScreen") {
                    popUpTo("splashScreen") { inclusive = true }
                }
            }
            SplashDestination.ToHome -> {
                navController.navigate("homeScreen") {
                    popUpTo("splashScreen") { inclusive = true }
                }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = painterResource(id = R.drawable.nutrilogo),
            contentDescription = "Logo",
            modifier = Modifier.scale(scale.value)
        )
    }
}