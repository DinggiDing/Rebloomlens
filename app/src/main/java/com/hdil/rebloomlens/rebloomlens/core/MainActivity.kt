package com.hdil.rebloomlens.rebloomlens.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hdil.rebloomlens.common.utils.Logger
import com.hdil.rebloomlens.rebloomlens.R
import com.hdil.rebloomlens.rebloomlens.ui.theme.AppTheme

//ROLE  Start from here, load plugins, and configure UI

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.i("Main Activity Started")

        // init plugin
        PluginManager.initialize(this)

        setContent {
            AppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PluginUI()
                }
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PluginUI() {
//    Scaffold(
//        topBar = {
//            TopAppBar(title = { Text(text = "Rebloomlens") })
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier.padding(paddingValues)
//        ) {
////            Text("Loaded Plugins:")
//            PluginManager.PluginNavigation()
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginUI() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        TabItem("Tracking", ImageVector.vectorResource(id = R.drawable.dashboard_24px)) { PluginManager.PluginNavigation() },
        TabItem("Visualization", ImageVector.vectorResource(id = R.drawable.monitoring_24px)) { Text("Visualization") }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Rebloomlens") })
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                NavigationBar(
                    modifier = Modifier.height(108.dp),
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    tab.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            tabs[selectedTabIndex].content()
        }
    }
}

data class TabItem(
    val title: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PluginUI()
}