package com.teka.aicalculatorappwithkoog

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teka.aicalculatorappwithkoog.agents.calculator.CalculatorAgentProvider
import com.teka.aicalculatorappwithkoog.screens.CalcAgentScreen
import com.teka.aicalculatorappwithkoog.screens.CalcAgentViewModel
import com.teka.aicalculatorappwithkoog.ui.theme.AICalculatorAppWithKoogTheme






class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()


        setContent {
            AICalculatorAppWithKoogTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val provider = CalculatorAgentProvider

                    val viewModel = viewModel<CalcAgentViewModel>(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                return CalcAgentViewModel(
                                    application = context.applicationContext as Application,
                                    agentProvider = provider
                                ) as T
                            }
                        }
                    )



                    CalcAgentScreen(
                        viewModel = viewModel,
                    )

                }
            }
        }
    }
}


