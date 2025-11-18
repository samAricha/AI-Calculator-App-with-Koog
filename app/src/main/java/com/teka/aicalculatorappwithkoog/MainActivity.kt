package com.teka.aicalculatorappwithkoog

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.teka.aicalculatorappwithkoog.agent.CalculatorAgentProvider
import com.teka.aicalculatorappwithkoog.screens.AgentDemoScreen
import com.teka.aicalculatorappwithkoog.screens.AgentDemoViewModel
import com.teka.aicalculatorappwithkoog.ui.theme.AICalculatorAppWithKoogTheme
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AICalculatorAppWithKoogTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    CalcScreen()
                }
            }
        }
    }
}

@Composable
fun CalcScreen() {
        val context = LocalContext.current
        val provider = CalculatorAgentProvider
        val viewModel = viewModel<AgentDemoViewModel>(
            factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return AgentDemoViewModel(
                        application = context.applicationContext as Application,
                        agentProvider = provider
                    ) as T
                }
            }
        )

        AgentDemoScreen(
            viewModel = viewModel,
            onNavigateBack = {},
        )

}
