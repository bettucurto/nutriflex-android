package components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.R

@Composable
fun DividerTextComponent(){
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically){
        HorizontalDivider(modifier = Modifier.fillMaxWidth()
            .weight(1f),
            color = colorScheme.onSurface,
            thickness = 1.dp
        )

        Text(modifier = Modifier.padding(8.dp),
            text = stringResource(id = R.string.divider),
            fontSize = 18.sp,
            color = colorScheme.onSurface
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth()
            .weight(1f),
            color = colorScheme.onSurface,
            thickness = 1.dp
        )
    }
}