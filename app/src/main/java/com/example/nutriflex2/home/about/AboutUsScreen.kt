package com.example.nutriflex2.home.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nutriflex2.R
import com.example.nutriflex2.R.drawable.axfitnesslogo
import components.LeftTitleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(
    navController: NavController? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { LeftTitleText("About Us") },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Top)
        ) {

            // Imagem Principal (Logo da Nutriflex)
            Image(
                painter = painterResource(id = R.drawable.nutrilogo),
                contentDescription = "Nutriflex Logo",
                modifier = Modifier
                    .size(120.dp) // Podes ajustar o tamanho conforme necessário
                    .padding(top = 16.dp)
            )

            // Texto descritivo
            Text(
                text = "Nutriflex is a project developed as part of a PAP (Projeto de Avaliação Profissional). It aims to provide users with a comprehensive tool for managing their fitness and nutrition.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Secção de Parcerias
            Text(
                text = "In Partnership With:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Image(
                painter = painterResource(id = axfitnesslogo),
                contentDescription = "AX Fitness Logo",
                modifier = Modifier
                    .size(200.dp) // Podes ajustar o tamanho para bater certo com a primeira imagem
            )



            // Adding a spacer to push content towards the top if the screen height is large.
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Version 1.0.3", // Placeholder for version information.
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}