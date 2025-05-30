package com.hdil.rebloomlens.rebloomlens.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
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
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab.title) },
                        icon = { Icon(imageVector = tab.icon, contentDescription = null) }
                    )
                }
            }

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