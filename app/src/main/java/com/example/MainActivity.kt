package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.MainViewModel
import com.example.ui.components.JarvisAuthDialog
import com.example.ui.components.JarvisDrawerContent
import com.example.ui.components.JarvisScreen
import com.example.ui.components.JarvisTopBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CommandCenterScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.JarvisBgVoid
import com.example.ui.theme.JarvisTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JarvisTheme {
                JarvisApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun JarvisApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val currentConversation by viewModel.currentConversation.collectAsState()
    val isAuthDialogVisible by viewModel.isAuthDialogVisible.collectAsState()

    val currentSettings = userSettings ?: com.example.data.entity.UserSettingsEntity()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Permission launcher for microphone recording
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    if (isAuthDialogVisible) {
        JarvisAuthDialog(
            onDismiss = { viewModel.hideAuthDialog() },
            onSignInGoogle = { name, email ->
                viewModel.signInWithGoogle(name, email)
            },
            onLoginEmail = { email, password, onResult ->
                viewModel.loginWithEmail(email, password, onResult)
            },
            onRegisterEmail = { email, password, name, onResult ->
                viewModel.registerWithEmail(email, password, name, onResult)
            },
            onSignInPhone = { phone, name, onResult ->
                viewModel.registerOrLoginWithPhone(phone, name, onResult)
            },
            currentUserName = currentSettings.userName,
            currentUserEmail = currentSettings.userEmail,
            currentUserPhone = currentSettings.userPhone,
            isLoggedIn = currentSettings.isLoggedIn,
            authProvider = currentSettings.authProvider,
            onSignOut = { viewModel.signOut() }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = JarvisBgVoid
            ) {
                JarvisDrawerContent(
                    conversations = conversations,
                    currentConversationId = currentConversation?.id,
                    onSelectConversation = { convId ->
                        viewModel.selectConversation(convId)
                        scope.launch { drawerState.close() }
                    },
                    onNewConversation = {
                        viewModel.startNewConversation()
                        scope.launch { drawerState.close() }
                    },
                    onDeleteConversation = { convId ->
                        viewModel.deleteConversation(convId)
                    },
                    onTogglePin = { conv ->
                        viewModel.togglePinConversation(conv)
                    },
                    userName = currentSettings.userName,
                    userEmail = currentSettings.userEmail,
                    userPhone = currentSettings.userPhone,
                    isLoggedIn = currentSettings.isLoggedIn,
                    authProvider = currentSettings.authProvider,
                    onOpenAuth = {
                        scope.launch { drawerState.close() }
                        viewModel.showAuthDialog()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                JarvisTopBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onNewChat = {
                        viewModel.startNewConversation()
                        viewModel.navigateTo(JarvisScreen.CHAT)
                    },
                    isDemoMode = currentSettings.isDemoMode,
                    userName = currentSettings.userName,
                    isLoggedIn = currentSettings.isLoggedIn,
                    authProvider = currentSettings.authProvider,
                    onOpenAuth = { viewModel.showAuthDialog() }
                )
            },
            containerColor = JarvisBgVoid,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(JarvisBgVoid)
            ) {
                when (currentScreen) {
                    JarvisScreen.CHAT -> ChatScreen(viewModel = viewModel)
                    JarvisScreen.COMMAND_CENTER -> CommandCenterScreen(viewModel = viewModel)
                    JarvisScreen.MEMORY -> MemoryScreen(viewModel = viewModel)
                    JarvisScreen.NOTES -> NotesScreen(viewModel = viewModel)
                    JarvisScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
