package com.example.nutriflex2.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import components.NFBottomBar
import components.TitleText
import theme.AppTheme


@Composable
fun HomeScreen(onNavigateToTreino: () -> Unit,
               onNavigateToDieta: () -> Unit,
               onAddCaloriesClick: () -> Unit, // botão +
               viewModel: HomeViewModel = hiltViewModel()){
    AppTheme() {
        Scaffold(
            bottomBar = { NFBottomBar(onTreinoClick = onNavigateToTreino,
            onPerfilClick = {},
            onDietaClick = onNavigateToDieta) }
        ) {
            innerPadding ->
            Column(modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Círculo/avatar à esquerda
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Perfil",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    }


                    Box(modifier = Modifier.weight(0.4f)){
                        TitleText(value = stringResource(id = com.example.nutriflex2.R.string.app_name))
                    }
                    // Círculo vazio/das definições à direita
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.outlineVariant,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Definições",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.width(200.dp),
                    color = colorScheme.onSurface,
                    thickness = 1.dp
                )


            }
        }
    }
}