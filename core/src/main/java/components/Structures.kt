package components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector


@Composable
fun NFBottomBar(
    selectedIndex: Int,
    onTreinoClick: () -> Unit,
    onPerfilClick: () -> Unit,
    onDietaClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedIndex == 0, // Treino
            onClick = onTreinoClick,
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Exercise") },
        )
        NavigationBarItem(
            selected = selectedIndex == 1, // Home / Área Pessoal
            onClick = onPerfilClick,
            icon = { Icon(Icons.Default.Person, contentDescription = "Personal Area") },
        )
        NavigationBarItem(
            selected = selectedIndex == 2, // Dieta
            onClick = onDietaClick,
            icon = { Icon(Icons.Default.Restaurant, contentDescription = "Diet") },
        )
    }
}


data class TabItem(
    val text: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector
)