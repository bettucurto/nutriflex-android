package components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable




@Composable
fun NFBottomBar(
        onTreinoClick: () -> Unit,
        onPerfilClick: () -> Unit,
        onDietaClick: () -> Unit
){
    NavigationBar {
        NavigationBarItem(
            selected = false, // depois vais ligar isto ao estado atual
            onClick = onTreinoClick,
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Treino") },
            label = { Text("Treino") }
        )
        NavigationBarItem(
            selected = true,
            onClick = onPerfilClick,
            icon = { Icon(Icons.Default.Person, contentDescription = "Área Pessoal") },
            label = { Text("Área Pessoal") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onDietaClick,
            icon = { Icon(Icons.Default.Restaurant, contentDescription = "Dieta") },
            label = { Text("Dieta") }
        )
    }
}