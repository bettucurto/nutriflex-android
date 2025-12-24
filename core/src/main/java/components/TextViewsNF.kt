package components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.R


@Composable
fun TitleText(value: String){
    Text(
        text = value,
        modifier = Modifier.fillMaxWidth().heightIn(),
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.audiowide)),
            textAlign = TextAlign.Center,
            color = colorScheme.secondary
        )
    )
}

@Composable
fun LeftTitleText(value: String){
    Text(
        text = value,
        modifier = Modifier.fillMaxWidth().heightIn(),
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.audiowide)),
            color = colorScheme.secondary
        )
    )
}

@Composable
fun HeadingTextComponent(value: String) {
    Text(
        text = value,
        modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily(Font(R.font.audiowide)),
            textAlign = TextAlign.Center,
            color = colorScheme.secondary
        )
    )
}

@Composable
fun LeftHeadingTextComponent(value: String) {
    Text(
        text = value,
        modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily(Font(R.font.audiowide)),
            color = colorScheme.secondary
        )
    )
}


@Composable
fun ClickableTextComponent(onClick: () -> Unit){
    val initialText = stringResource(R.string.clickable_text1)
    val clickableText = stringResource(R.string.clickable_text2)

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = colorScheme.onBackground, fontSize = 16.sp)){
            append(initialText)
        }
        append(" ")
        withStyle(style = SpanStyle(color = colorScheme.secondary, fontSize = 16.sp)){
            pushStringAnnotation(tag = clickableText, annotation = clickableText)
            append(clickableText)
        }
    }
    ClickableText(
        modifier = Modifier.fillMaxWidth(),
        style = TextStyle(
            textAlign = TextAlign.Center,

        ),
        text = annotatedString,
        onClick ={ offset ->
            annotatedString.getStringAnnotations(offset,offset)
                .firstOrNull()?.
                also { span->
                    Log.d("ClickableTextComponent", "{${span.item}}")

                    if (span.item == clickableText){
                        onClick()
                    }
                }
        }
    )


}

