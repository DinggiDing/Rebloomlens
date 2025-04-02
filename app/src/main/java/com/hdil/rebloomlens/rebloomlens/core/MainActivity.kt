package com.hdil.rebloomlens.rebloomlens.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.hdil.rebloomlens.common.utils.Logger
import com.hdil.rebloomlens.rebloomlens.ui.theme.RebloomlensTheme

//ROLE  Start from here, load plugins, and configure UI

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.i("Main Activity Started")

        // init plugin
        PluginManager.initialize(this)

        setContent {
            RebloomlensTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PluginUI()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginUI() {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Rebloomlens") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
//            Text("Loaded Plugins:")
            PluginManager.PluginNavigation()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PluginUI()
}
