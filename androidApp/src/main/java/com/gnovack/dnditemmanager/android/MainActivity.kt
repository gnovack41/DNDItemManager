package com.gnovack.dnditemmanager.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gnovack.dnditemmanager.android.viewmodels.DNDApiViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel by viewModels<DNDApiViewModel>()

        setContent {
            MyApplicationTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    DNDNavHost(
                        viewModel = viewModel,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp),
                    )
                }
            }
        }
    }

    override fun onStart() {
        val viewModel by viewModels<DNDApiViewModel>()
        viewModel.loadCharacterList(applicationContext)

        super.onStart()
    }

    override fun onStop() {
        val viewModel by viewModels<DNDApiViewModel>()
        viewModel.saveCharacterList(applicationContext)

        super.onStop()
    }
}

