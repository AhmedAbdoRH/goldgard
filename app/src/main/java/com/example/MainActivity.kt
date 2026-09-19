package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ScreenType
import com.example.ui.components.GoldGuardBottomNav
import com.example.ui.screens.AntiFraudTipsScreen
import com.example.ui.screens.BuyGoldScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PricesScreen
import com.example.ui.screens.SellGoldScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.ZakatScreen
import com.example.ui.theme.GoldTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GoldViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: GoldViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(isDarkMode = isDarkMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    GoldGuardApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun GoldGuardApp(viewModel: GoldViewModel) {
    var showSplash by rememberSaveable { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(
            onSplashFinished = { showSplash = false }
        )
        return
    }

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect Toast/Snackbar events
    LaunchedEffect(viewModel) {
        viewModel.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle system back navigation to return to Home
    if (currentScreen != ScreenType.HOME) {
        BackHandler {
            viewModel.navigateTo(ScreenType.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = GoldTheme.colors.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            GoldGuardBottomNav(
                currentScreen = currentScreen,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                ScreenType.HOME -> HomeScreen(viewModel = viewModel)
                ScreenType.PRICES -> PricesScreen(viewModel = viewModel)
                ScreenType.BUY -> BuyGoldScreen(viewModel = viewModel)
                ScreenType.SELL -> SellGoldScreen(viewModel = viewModel)
                ScreenType.SETTINGS -> SettingsScreen(viewModel = viewModel)
                ScreenType.HISTORY -> HistoryScreen(viewModel = viewModel)
                ScreenType.ZAKAT -> ZakatScreen(viewModel = viewModel)
                ScreenType.TIPS -> AntiFraudTipsScreen(viewModel = viewModel)
            }
        }
    }
}

// Kept for screenshot test compatibility if needed
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "مرحباً بك في حارس الذهب! $name", modifier = modifier)
}
