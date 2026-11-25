package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.firebase.BabyRepository
import com.example.data.firebase.PhotoStorageHelper
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.screens.BabyProfileDialog
import com.example.ui.screens.GuidelineScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.PhotosScreen
import com.example.ui.screens.TodoScreen
import com.example.ui.theme.ShonamoniTheme
import com.example.ui.viewmodel.ShonamoniViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

enum class AppTab(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    TODOS(R.string.tab_todos, Icons.Rounded.Checklist, Icons.Outlined.Checklist, "nav_todos"),
    PHOTOS(R.string.tab_photos, Icons.Rounded.PhotoLibrary, Icons.Outlined.PhotoLibrary, "nav_photos"),
    NOTES(R.string.tab_notes, Icons.Rounded.EditNote, Icons.Outlined.EditNote, "nav_notes"),
    GUIDELINES(R.string.tab_guidelines, Icons.Rounded.AutoStories, Icons.Outlined.AutoStories, "nav_guidelines")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShonamoniTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = { currentUser = Firebase.auth.currentUser })
    } else {
        key(user.uid) {
            ShonamoniHomeShell(
                currentUserId = user.uid,
                userEmail = user.email
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShonamoniHomeShell(
    currentUserId: String,
    userEmail: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: ShonamoniViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                ShonamoniViewModel(
                    repository = BabyRepository(db),
                    photoStorage = PhotoStorageHelper(app),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val todosState by viewModel.todosState.collectAsStateWithLifecycle()
    val photosState by viewModel.photosState.collectAsStateWithLifecycle()
    val notesState by viewModel.notesState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    var currentTab by remember { mutableStateOf(AppTab.TODOS) }
    var showProfileDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = currentTab != AppTab.TODOS) {
        currentTab = AppTab.TODOS
    }

    val profile = (profileState as? UiState.Success)?.data
    val defaultAgeMonth = (profile?.babyAgeMonths ?: 0L).toInt().coerceIn(0, 12)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier.testTag("top_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChildCare,
                                contentDescription = stringResource(R.string.edit_baby_profile),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar {
                        AppTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        AppTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp)
                    ) {
                        Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                            when (tab) {
                                AppTab.TODOS -> TodoScreen(
                                    profileState = profileState,
                                    todosState = todosState,
                                    isSaving = isSaving,
                                    onOpenProfile = { showProfileDialog = true },
                                    onSeedStarter = viewModel::seedStarterChecklist,
                                    onAddTodo = viewModel::addTodo,
                                    onToggleTodo = viewModel::toggleTodo,
                                    onUpdateTodo = viewModel::updateTodo,
                                    onDeleteTodo = viewModel::deleteTodo
                                )

                                AppTab.PHOTOS -> PhotosScreen(
                                    photosState = photosState,
                                    defaultAgeMonth = defaultAgeMonth,
                                    isSaving = isSaving,
                                    resolvePhotoFile = viewModel::resolvePhotoFile,
                                    onAddPhoto = viewModel::addPhotoMemory,
                                    onUpdatePhoto = viewModel::updatePhotoMemory,
                                    onDeletePhoto = viewModel::deletePhotoMemory
                                )

                                AppTab.NOTES -> NotesScreen(
                                    notesState = notesState,
                                    defaultAgeMonth = defaultAgeMonth,
                                    isSaving = isSaving,
                                    resolvePhotoFile = viewModel::resolvePhotoFile,
                                    onAddNote = viewModel::addNote,
                                    onUpdateNote = viewModel::updateNote,
                                    onTogglePin = viewModel::toggleNotePin,
                                    onDeleteNote = viewModel::deleteNote
                                )

                                AppTab.GUIDELINES -> GuidelineScreen(
                                    onAddQuickTodo = { title, notes, stage, category ->
                                        viewModel.addTodo(
                                            title = title,
                                            notes = notes,
                                            ageStage = stage,
                                            category = category,
                                            successMessage = "গাইডলাইন থেকে করণীয় তালিকায় যোগ করা হয়েছে"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        BabyProfileDialog(
            profile = profile,
            userEmail = userEmail,
            isSaving = isSaving,
            onDismiss = { showProfileDialog = false },
            onSave = viewModel::saveBabyProfile,
            onSignOut = {
                showProfileDialog = false
                signOutUser(credentialManager = credentialManager, scope = scope)
            }
        )
    }
}
