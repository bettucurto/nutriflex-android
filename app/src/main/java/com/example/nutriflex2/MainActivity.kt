package com.example.nutriflex2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.nutriflex2.ui.theme.Nutriflex2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column {
                Text(text = "Hello Galhofa!")
                Text(text = "Hello David!")
                Button(onClick = {
                    Toast.makeText(applicationContext,"Obrigado Twin", Toast.LENGTH_SHORT).show()
                }) {
                    Text(text = "Clica-me twin")
                }
            }

        }
    }
}
