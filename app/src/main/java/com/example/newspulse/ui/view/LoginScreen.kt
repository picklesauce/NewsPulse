package com.example.newspulse.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.ui.CompositionLocals
import com.example.newspulse.ui.preview.createPreviewViewModelFactory
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.viewmodel.LoginViewModel

/** Matches ArticleListScreen / Home palette for a consistent auth experience. */
internal object NewsPulseAuthColors {
    val screenBg = Color(0xFFF8F8FC)
    val primaryText = Color(0xFF1C1B1F)
    val secondaryText = Color(0xFF79747E)
    val labelText = Color(0xFF49454F)
    val border = Color(0xFFE7E0EC)
    val accent = Color(0xFF6750A4)
    val logoSurface = Color(0xFFE8DEF8)
}
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel(factory = CompositionLocals.LocalViewModelFactory.current)
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NewsPulseAuthColors.screenBg)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppBranding(subtitle = "Welcome back!")

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Log In",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NewsPulseAuthColors.primaryText
                    )
                    Text(
                        text = "Enter your credentials to continue",
                        fontSize = 13.sp,
                        color = NewsPulseAuthColors.secondaryText,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    LaunchedEffect(state.oauthNavigateHome) {
                        if (state.oauthNavigateHome) {
                            viewModel.consumeOAuthNavigation()
                            navController.navigate(
                                if (viewModel.isOnboardingComplete()) "home" else "topicSelection"
                            ) {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.continueWithGoogle() },
                        enabled = !state.isGoogleLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, NewsPulseAuthColors.border)
                    ) {
                        if (state.isGoogleLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = NewsPulseAuthColors.accent
                            )
                        } else {
                            Text(
                                text = "Continue with Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NewsPulseAuthColors.primaryText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(NewsPulseAuthColors.border)
                        )
                        Text(
                            text = "  or  ",
                            fontSize = 13.sp,
                            color = NewsPulseAuthColors.secondaryText
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(NewsPulseAuthColors.border)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    AuthFieldLabel("Email/Username")
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("you@example.com or yourname", color = NewsPulseAuthColors.secondaryText) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = NewsPulseAuthColors.secondaryText)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        shape = RoundedCornerShape(12.dp),
                        colors = authFieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AuthFieldLabel("Password")
                        Text(
                            text = "Forgot?",
                            fontSize = 13.sp,
                            color = NewsPulseAuthColors.accent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Enter your password", color = NewsPulseAuthColors.secondaryText) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NewsPulseAuthColors.secondaryText)
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.togglePasswordVisible() }) {
                                Icon(
                                    imageVector = if (state.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (state.passwordVisible) "Hide password" else "Show password",
                                    tint = NewsPulseAuthColors.secondaryText
                                )
                            }
                        },
                        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = authFieldColors()
                    )

                    if (state.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.errorMessage!!,
                            color = Color(0xFFD32F2F),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.logIn { success ->
                                if (success) {
                                    navController.navigate(
                                        if (viewModel.isOnboardingComplete()) "home" else "topicSelection"
                                    ) {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NewsPulseAuthColors.accent)
                    ) {
                        Text(
                            text = "Log In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { navController.navigate("signup") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, NewsPulseAuthColors.border)
                    ) {
                        Text(
                            text = "Sign up",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NewsPulseAuthColors.primaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Protected by industry-standard encryption",
                fontSize = 12.sp,
                color = NewsPulseAuthColors.secondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun AppBranding(subtitle: String) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NewsPulseAuthColors.logoSurface),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "📰", fontSize = 30.sp)
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "NewsPulse",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = NewsPulseAuthColors.primaryText
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = subtitle,
        fontSize = 14.sp,
        color = NewsPulseAuthColors.secondaryText
    )
}

@Composable
internal fun AuthFieldLabel(label: String) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = NewsPulseAuthColors.labelText,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
internal fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = NewsPulseAuthColors.border,
    focusedBorderColor = NewsPulseAuthColors.accent,
    unfocusedContainerColor = Color.White,
    focusedContainerColor = Color.White,
    cursorColor = NewsPulseAuthColors.accent,
    unfocusedLeadingIconColor = NewsPulseAuthColors.secondaryText,
    focusedLeadingIconColor = NewsPulseAuthColors.accent,
    unfocusedTrailingIconColor = NewsPulseAuthColors.secondaryText,
    focusedTrailingIconColor = NewsPulseAuthColors.accent
)

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    NewsPulseTheme {
        CompositionLocalProvider(
            CompositionLocals.LocalViewModelFactory provides createPreviewViewModelFactory()
        ) {
            LoginScreen(navController = rememberNavController())
        }
    }
}
