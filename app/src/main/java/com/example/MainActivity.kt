package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.firebase.attemptAutoSignIn
import com.example.core.firebase.authStateFlow
import com.example.core.firebase.configureAppCheck
import com.example.ui.components.FloatingLiquidBottomBar
import com.example.ui.components.LiquidGlassBackdrop
import com.example.ui.components.RedtorTab
import com.example.ui.screens.AiStudioScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DownloaderScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SocialHubScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.RedtorTheme
import com.example.ui.viewmodel.RedtorViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            RedtorTheme {
                RedtorRootApp()
            }
        }
    }
}

@Composable
private fun RedtorRootApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var showSplash by remember { mutableStateOf(true) }
    val currentUser by Firebase.auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onUnauthenticated = {},
            scope = scope
        )
    }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
    } else {
        val user = currentUser
        if (user == null) {
            AuthScreen(onAuthSuccess = {})
        } else {
            key(user.uid) {
                RedtorMainShell(firebaseUser = user)
            }
        }
    }
}

@Composable
private fun RedtorMainShell(firebaseUser: FirebaseUser) {
    val context = LocalContext.current
    val databaseId = remember(context) { context.getString(R.string.firestore_database_id) }

    val viewModel: RedtorViewModel = viewModel(
        key = firebaseUser.uid,
        factory = RedtorViewModel.provideFactory(
            databaseId = databaseId,
            currentUserId = firebaseUser.uid,
            currentUser = firebaseUser
        )
    )

    var selectedTab by remember { mutableStateOf(RedtorTab.STUDIO) }

    LiquidGlassBackdrop {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Crossfade(
                targetState = selectedTab,
                label = "tab_crossfade",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                    RedtorTab.STUDIO -> AiStudioScreen(viewModel = viewModel)
                    RedtorTab.DOWNLOADER -> DownloaderScreen(viewModel = viewModel)
                    RedtorTab.HUB -> SocialHubScreen(viewModel = viewModel)
                    RedtorTab.PROFILE -> ProfileScreen(firebaseUser = firebaseUser, viewModel = viewModel)
                }
            }

            FloatingLiquidBottomBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
