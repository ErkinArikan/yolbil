package com.example.yolbil_jetpack_sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.basarsoft.yolbil.ui.MapView
import com.example.yolbil_jetpack_sample.ui.theme.YolbiljetpacksampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YolbiljetpacksampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    YolbilMapScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun YolbilMapScreen(
    modifier: Modifier = Modifier,
    viewModel: YolbilViewModel = hiltViewModel()
) {
    // Define two locations in Ankara


    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // MapView Display
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        viewModel.initializeMapView(this)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )

            // Button to trigger route creation
            Button(
                onClick = {
                    viewModel.createRoute()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Create Route")
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YolbiljetpacksampleTheme {
        YolbilMapScreen()
    }
}