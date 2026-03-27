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
import com.example.nutriflex2.home.about.AboutUsScreen
import com.example.nutriflex2.home.settings.SettingsScreen
import com.example.nutriflex2.home.ui.HomeScreen
import com.example.nutriflex2.home.ui.SyncViewModel
import com.example.nutriflex2.home.ui.tabs.diet.search.scan.ScanConfirmScreen
import com.example.nutriflex2.home.ui.tabs.diet.search.scan.ScanMealScreen
import com.example.nutriflex2.home.ui.tabs.diet.favorites.FavoriteMealEditorScreen
import com.example.nutriflex2.home.ui.tabs.diet.favorites.FavoriteMealEditorViewModel
import com.example.nutriflex2.home.ui.tabs.diet.info.favorites.FavoriteMealInfoScreen
import com.example.nutriflex2.home.ui.tabs.diet.info.meals.MealInfoScreen
import com.example.nutriflex2.home.ui.tabs.diet.info.recipes.RecipeInfoScreen
import com.example.nutriflex2.home.ui.tabs.diet.search.meals.SearchMealsScreen
import com.example.nutriflex2.home.ui.tabs.diet.search.recipes.SearchRecipeScreen
import com.example.nutriflex2.home.ui.tabs.training.active.ActiveWorkoutScreen
import com.example.nutriflex2.home.ui.tabs.training.edit.CreateSessionScreen
import com.example.nutriflex2.home.ui.tabs.training.edit.EditSessionScreen
import com.example.nutriflex2.home.ui.tabs.training.info.ExerciseInfoScreen
import com.example.nutriflex2.home.ui.tabs.training.search.exercise.SearchExerciseScreen
import com.example.nutriflex2.home.ui.tabs.training.search.workout.SearchWorkoutScreen
import com.example.nutriflex2.home.ui.tabs.training.active.WorkoutSummary
import com.example.nutriflex2.home.ui.tabs.training.active.WorkoutSummaryScreen
import com.example.treino.domain.models.Exercicio
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
fun AppNavGraph(
    navController: NavHostController,
    syncViewModel: SyncViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = "splashScreen",
        enterTransition = { defaultEnterTransition(initialState, targetState) },
        exitTransition = { defaultExitTransition(initialState, targetState) }
    ) {
        composable("splashScreen") { SplashScreen(navController) }
        composable("welcomeScreen") { WelcomeScreen(navController) }
        composable("loginScreen") {
            LoginScreen(navController)
            
            // Observar se o utilizador acabou de fazer login com sucesso para disparar o sync
            // Nota: Como o LoginScreen navega para homeScreen ao ter sucesso, 
            // podemos detetar a mudança de destino ou simplesmente confiar no fluxo de navegação.
        }

        composable("homeScreen") {
            // Verificar se viemos do Login ou Registo
            val previousBackStackEntry = navController.previousBackStackEntry
            val fromAuth = previousBackStackEntry?.destination?.route?.let { 
                it == "loginScreen" || it.contains("registration") 
            } ?: false
            
            LaunchedEffect(Unit) {
                if (fromAuth) {
                    android.util.Log.d("AppNavGraph", "Navigated from Auth, triggering sync...")
                    syncViewModel.performInitialSync()
                }
            }

            HomeScreen(
                navController = navController,
                onNavigateToTraining = { navController.navigate("training_flow") },
                onNavigateToSearchMeals = { navController.navigate("searchMealsScreen") },
                onNavigateToSearchRecipes = { navController.navigate("searchRecipeScreen") },
                onNavigateToAccount = { navController.navigate("accountScreen") },
                onNavigateToAbout = { navController.navigate("aboutScreen") }
            )
        }

        // --- Fluxo de Treino (Training) ---
        navigation(
            startDestination = "trainingTab",
            route = "training_flow"
        ) {
            composable("trainingTab") {
                // TrainingTabScreen já é instanciado dentro do HomeScreen Pager
            }
            
            composable(
                route = "create_session/{folderId}",
                arguments = listOf(navArgument("folderId") { type = NavType.IntType })
            ) {
                CreateSessionScreen(
                    onBack = { navController.popBackStack() },
                    navController = navController
                )
            }

            composable(
                route = "search_exercise/{exIndex}?isReplacement={isReplacement}",
                arguments = listOf(
                    navArgument("exIndex") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("isReplacement") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val exIndex = backStackEntry.arguments?.getInt("exIndex") ?: -1
                val isReplacement = backStackEntry.arguments?.getBoolean("isReplacement") ?: false
                SearchExerciseScreen(navController = navController, exIndex = exIndex, isReplacement = isReplacement)
            }

            composable(
                route = "exercise_info/{exerciseId}?isAddingMode={isAddingMode}&isReplacement={isReplacement}&exIndex={exIndex}",
                arguments = listOf(
                    navArgument("exerciseId") { type = NavType.StringType },
                    navArgument("isAddingMode") { 
                        type = NavType.BoolType
                        defaultValue = false
                    },
                    navArgument("isReplacement") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                    navArgument("exIndex") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val isAddingMode = backStackEntry.arguments?.getBoolean("isAddingMode") ?: false
                val isReplacement = backStackEntry.arguments?.getBoolean("isReplacement") ?: false
                val exIndex = backStackEntry.arguments?.getInt("exIndex") ?: -1

                ExerciseInfoScreen(
                    onBack = { navController.popBackStack() },
                    isAddingMode = isAddingMode || isReplacement,
                    isReplacement = isReplacement,
                    exIndex = exIndex,
                    navController = navController,
                    onAddExercise = { data ->
                        if (isReplacement && exIndex != -1) {
                            val parts = data.split("|")
                            if (parts.size >= 4) {
                                val replacedEx = Exercicio(
                                    id = 0,
                                    exercicioApiId = parts[0],
                                    nome = parts[1],
                                    notas = "",
                                    idSessao = 0,
                                    ordem = 0,
                                    imagem = parts[3],
                                    bodypart = parts[2]
                                )
                                // Tenta encontrar a entrada do ActiveWorkout para colocar o resultado
                                try {
                                    val activeWorkoutEntry = navController.getBackStackEntry("active_workout/{sessionId}")
                                    activeWorkoutEntry.savedStateHandle.set("replaced_exercise", replacedEx)
                                    activeWorkoutEntry.savedStateHandle.set("replace_index", exIndex)
                                    navController.popBackStack("active_workout/{sessionId}", inclusive = false)
                                } catch (e: Exception) {
                                    navController.previousBackStackEntry?.savedStateHandle?.set("replaced_exercise", replacedEx)
                                    navController.previousBackStackEntry?.savedStateHandle?.set("replace_index", exIndex)
                                    navController.popBackStack()
                                }
                            }
                        } else {
                            val routes = listOf("create_session/{folderId}", "edit_session/{sessionId}")
                            var foundEntry: NavBackStackEntry? = null
                            for (route in routes) {
                                try {
                                    foundEntry = navController.getBackStackEntry(route)
                                    break
                                } catch (e: Exception) { }
                            }

                            if (foundEntry != null) {
                                foundEntry.savedStateHandle.set("selected_exercise_data", data)
                                navController.popBackStack(foundEntry.destination.id, inclusive = false)
                            } else {
                                navController.previousBackStackEntry?.savedStateHandle?.set("selected_exercise_data", data)
                                navController.popBackStack()
                            }
                        }
                    }
                )
            }

            composable(
                route = "edit_session/{sessionId}",
                arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
            ) {
                EditSessionScreen(
                    onBack = { navController.popBackStack() },
                    navController = navController
                )
            }

            composable(
                route = "active_workout/{sessionId}",
                arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
            ) {
                ActiveWorkoutScreen(
                    onBack = { navController.popBackStack() },
                    onFinish = { summary ->
                        navController.navigate("workout_summary/${summary.duration}/${summary.totalVolume}/${summary.completedSets}") {
                            popUpTo("trainingTab") { inclusive = false }
                        }
                    },
                    navController = navController
                )
            }

            composable(
                route = "workout_summary/{duration}/{volume}/{sets}",
                arguments = listOf(
                    navArgument("duration") { type = NavType.StringType },
                    navArgument("volume") { type = NavType.FloatType },
                    navArgument("sets") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val duration = backStackEntry.arguments?.getString("duration") ?: ""
                val volume = backStackEntry.arguments?.getFloat("volume")?.toDouble() ?: 0.0
                val sets = backStackEntry.arguments?.getInt("sets") ?: 0
                
                WorkoutSummaryScreen(
                    summary = WorkoutSummary(duration, volume, sets),
                    navController = navController
                )
            }
            
            composable("workout_browser") {
                SearchWorkoutScreen(navController = navController)
            }
        }

        // --- Fluxo de Pesquisa Diária (Log normal) ---
        composable("searchMealsScreen") {
            SearchMealsScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                onOpenPhoto = { navController.navigate("scan_meal") },

                // Ao clicar em Create Meal, iniciamos o FLUXO ANINHADO
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
                onOpenPhoto = { navController.navigate("scan_meal") },
                navController = navController,
            )
        }

        // --- Detalhes do Alimento (Modo: Log Diário) ---
        composable(
            route = "foodDetail/{foodId}?servingId={servingId}&portion={portion}&servingDesc={servingDesc}",
            arguments = listOf(
                navArgument("foodId") { type = NavType.StringType },
                navArgument("servingId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("portion") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("servingDesc") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
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
                onIngredientClick = { foodId, servingId, portion ->
                    navController.navigate("foodDetail/$foodId?servingId=$servingId&portion=$portion")
                }
            )
        }

        // --- Detalhes da Refeição Favorita (Personalizada) ---
        composable(
            route = "favoriteMealDetail/{mealId}",
            arguments = listOf(navArgument("mealId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString("mealId") ?: ""
            FavoriteMealInfoScreen(
                mealId = mealId,
                onBack = { navController.popBackStack() },
                onLogMeal = {
                    navController.navigate("homeScreen") {
                        popUpTo("homeScreen") { inclusive = true }
                    }
                },
                onEditClick = { id ->
                    navController.navigate("favoriteMealEditor?mealId=$id")
                },
                onIngredientClick = { foodId, servingDesc, portion ->
                    navController.navigate("foodDetail/$foodId?servingDesc=$servingDesc&portion=$portion")
                }
            )
        }

        // === NOVO FLUXO ANINHADO: CRIAR/EDITAR REFEIÇÃO FAVORITA ===
        navigation(
            startDestination = "favoriteMealEditor",
            route = "create_meal_flow"
        ) {
            composable(
                route = "favoriteMealEditor?mealId={mealId}",
                arguments = listOf(navArgument("mealId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val mealIdStr = backStackEntry.arguments?.getString("mealId")
                val mealId = mealIdStr?.toIntOrNull()

                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("create_meal_flow")
                }
                val sharedViewModel: FavoriteMealEditorViewModel = hiltViewModel(parentEntry)

                FavoriteMealEditorScreen(
                    mealId = mealId,
                    viewModel = sharedViewModel,
                    onBack = { navController.popBackStack() },
                    onAddFoodClick = {
                        navController.navigate("meal_creation_search")
                    },
                    onMealSaved = {
                        navController.navigate("homeScreen") {
                            popUpTo("homeScreen") { inclusive = true }
                        }
                    }
                )
            }

            composable("meal_creation_search") {
                SearchMealsScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    onOpenPhoto = { },
                    onOpenCreateMeal = { },
                    onFoodClick = { foodId ->
                        navController.navigate("meal_creation_detail/$foodId")
                    }
                )
            }

            composable(
                route = "meal_creation_detail/{foodId}?servingId={servingId}&portion={portion}&servingDesc={servingDesc}",
                arguments = listOf(
                    navArgument("foodId") { type = NavType.StringType },
                    navArgument("servingId") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("portion") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("servingDesc") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("create_meal_flow")
                }
                val sharedViewModel: FavoriteMealEditorViewModel = hiltViewModel(parentEntry)

                MealInfoScreen(
                    foodId = foodId,
                    onBack = { navController.popBackStack() },
                    onReturnIngredient = { food, serving, qty ->
                        sharedViewModel.addIngredient(food, serving, qty)
                    },
                    onAddToMealCompleted = {
                        navController.navigate("favoriteMealEditor") {
                            popUpTo("favoriteMealEditor") { inclusive = true }
                        }
                    }
                )
            }
        }



        composable("accountScreen") {
            SettingsScreen(
                onBack = { navController.navigate("homeScreen")},
                onLogout = {
                    navController.navigate("welcomeScreen") {
                        popUpTo("homeScreen") { inclusive = true }
                    }
                }
            )
        }

        composable("aboutScreen"){
            AboutUsScreen(navController)
        }

        composable("scan_meal") {
            ScanMealScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "scanConfirm?imageUri={imageUri}",
            arguments = listOf(
                navArgument("imageUri") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val ingredients = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<List<com.example.dieta.domain.FoodRecognitionUseCase.ProcessedIngredient>>("detected_ingredients") 
                ?: emptyList()

            ScanConfirmScreen(
                initialIngredients = ingredients,
                onBack = { navController.popBackStack() },
                onAddMore = { navController.navigate("scan_confirm_search") },
                onCompleted = {
                    navController.navigate("homeScreen") {
                        popUpTo("homeScreen") { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable("scan_confirm_search") {
            SearchMealsScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                onOpenPhoto = { },
                onOpenCreateMeal = { },
                onFoodClick = { foodId ->
                    navController.navigate("scan_confirm_detail/$foodId")
                }
            )
        }

        composable(
            route = "scan_confirm_detail/{foodId}",
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
            MealInfoScreen(
                foodId = foodId,
                onBack = { navController.popBackStack() },
                onReturnIngredient = { food, serving, qty ->
                    val selection = com.example.dieta.domain.FatSecretIngredientSelection(food, serving, qty)
                    // Procuramos scanConfirm na pilha (pode ter argumentos, por isso usamos contains)
                    val targetEntry = navController.currentBackStack.value.lastOrNull { it.destination.route?.contains("scanConfirm") == true }
                    targetEntry?.savedStateHandle?.set("added_food_selection", selection)
                },
                onAddToMealCompleted = {
                    navController.popBackStack("scanConfirm", inclusive = false)
                }
            )
        }

        navigation(
            startDestination = "registrationScreen1",
            route = "registrationFlow"
        ) {

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

