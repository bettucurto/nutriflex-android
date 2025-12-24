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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.nutriflex2.home.ui.HomeScreen
import kotlinx.coroutines.delay
import ui.WelcomeScreen
import ui.login.LoginScreen
import ui.registration.RegisterViewModel
import ui.registration.RegistrationScreen1
import ui.registration.RegistrationScreen2
import ui.registration.RegistrationScreen3
import ui.registration.RegistrationScreen4
import ui.registration.RegistrationScreen5

private fun screenOrder(route: String?): Int = when (route) {
    "welcomeScreen" -> 0
    "loginScreen" -> 1
    "registrationScreen1" -> 2
    "registrationScreen2" -> 3
    "registrationScreen3" -> 4
    "registrationScreen4" -> 5
    "registrationScreen5" -> 6
    else -> -1   // splash ou desconhecido
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
        composable("homeScreen") {HomeScreen(
            onNavigateToTreino = { /* navController.navigate(...) */ },
            onNavigateToDieta = { /* ... */ }
        )}

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

    // splash → welcome: podes pôr um fade se quiseres
    if (initial.destination.route == "splashScreen" && target.destination.route == "welcomeScreen") {
        return fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.9f)
    }

    val forward = targetOrder > initialOrder

    return if (forward) {
        // Navegação para a frente: entra da direita
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(500)
        )
    } else {
        // Navegação para trás: entra da esquerda
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
        // Navegação para a frente: sai para a esquerda
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(500)
        )
    } else {
        // Navegação para trás: sai para a direita
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

    // Quando o destino ficar pronto, espera um pouco e navega
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