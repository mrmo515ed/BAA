package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.repository.AnimeRepository
import com.example.ui.AnimeMainApp
import com.example.ui.AuthUiState
import com.example.ui.AuthViewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.AnimeBlackBg
import com.example.ui.theme.AnimeCrimson
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AnimeDarkSurface
import com.example.ui.theme.AnimeTextSecondary
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "Edge to edge styling fallback: ${e.message}")
        }
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AnimeBlackBg
                ) {
                    BlackAnimeApp()
                }
            }
        }
    }
}

@Composable
fun BlackAnimeApp(
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUserFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        authViewModel.trySilentSignIn(context)
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            uiState = authState,
            onSignInClick = { ctx ->
                authViewModel.signInWithGoogle(ctx)
            }
        )
    } else {
        // Authenticated Session: Inject repository with custom database ID with fallback to default instance
        val mainViewModel: MainViewModel = viewModel(
            key = user.uid,
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[APPLICATION_KEY])
                    val db = try {
                        val databaseId = app.getString(R.string.firestore_database_id)
                        if (databaseId.isNotBlank() && databaseId != "(default)") {
                            FirebaseFirestore.getInstance(databaseId)
                        } else {
                            FirebaseFirestore.getInstance()
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("MainActivity", "Falling back to default Firestore database: ${e.message}")
                        FirebaseFirestore.getInstance()
                    }
                    val repository = AnimeRepository(db)
                    MainViewModel(repository, user.uid, user)
                }
            }
        )

        val userProfile by mainViewModel.userProfile.collectAsStateWithLifecycle()

        if (userProfile?.isBanned == true) {
            BannedUserScreen(
                banReason = userProfile?.banReason?.ifBlank { "مخالفة شروط وقواعد مجتمع بلاك انمي" } ?: "مخالفة شروط مجتمع بلاك انمي",
                onSignOutClick = { authViewModel.signOut() }
            )
        } else {
            AnimeMainApp(
                viewModel = mainViewModel,
                onSignOutClick = { authViewModel.signOut() }
            )
        }
    }
}

@Composable
private fun BannedUserScreen(
    banReason: String,
    onSignOutClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AnimeBlackBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AnimeDarkSurface),
            border = BorderStroke(1.5.dp, AnimeCrimson),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(AnimeCrimson.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "محظور",
                        tint = AnimeCrimson,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "الحساب محظور",
                    color = AnimeCrimson,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "تم حظر حسابك من قبل إدارة بلاك انمي بسبب:\n$banReason",
                    color = AnimeTextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Button(
                    onClick = onSignOutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AnimeCrimson),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("تسجيل الخروج", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

