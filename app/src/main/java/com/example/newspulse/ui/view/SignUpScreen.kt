package com.example.newspulse.ui.view

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.ui.CompositionLocals
import com.example.newspulse.ui.preview.createPreviewViewModelFactory
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.viewmodel.SignUpViewModel

private val SignUpDarkBg = Color(0xFF0F0F1A)
private val SignUpAccent = Color(0xFF6C63FF)
private val SignUpAccentEnd = Color(0xFFE040FB)
private val SignUpFieldBorder = Color(0xFFE0E0E0)
private val SignUpHint = Color(0xFF9E9E9E)
private val SignUpLabel = Color(0xFF1C1B1F)
private val SignUpLinkBlue = Color(0xFF2979FF)

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = viewModel(factory = CompositionLocals.LocalViewModelFactory.current)
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SignUpDarkBg)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppBranding(subtitle = "Your personalized news experience")

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Create Account",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignUpLabel
                    )
                    Text(
                        text = "Sign up to get started",
                        fontSize = 13.sp,
                        color = SignUpHint,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    AuthFieldLabel("Username")
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = { viewModel.updateUsername(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Choose a username", color = SignUpHint) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = SignUpHint)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = authFieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthFieldLabel("Email Address")
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("you@example.com", color = SignUpHint) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = SignUpHint)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = authFieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthFieldLabel("Password")
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Create a password", color = SignUpHint) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = SignUpHint)
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.togglePasswordVisible() }) {
                                Icon(
                                    imageVector = if (state.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (state.passwordVisible) "Hide password" else "Show password",
                                    tint = SignUpHint
                                )
                            }
                        },
                        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = authFieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthFieldLabel("Confirm Password")
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.updateConfirmPassword(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Confirm your password", color = SignUpHint) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = SignUpHint)
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleConfirmPasswordVisible() }) {
                                Icon(
                                    imageVector = if (state.confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (state.confirmPasswordVisible) "Hide password" else "Show password",
                                    tint = SignUpHint
                                )
                            }
                        },
                        visualTransformation = if (state.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

                    // Gradient "Sign Up" button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(SignUpAccent, SignUpAccentEnd)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                if (viewModel.signUp()) {
                                    navController.navigate("topicSelection") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            elevation = null
                        ) {
                            Text(
                                text = "Sign Up",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(SignUpFieldBorder)
                        )
                        Text(
                            text = "  or  ",
                            fontSize = 13.sp,
                            color = SignUpHint
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(SignUpFieldBorder)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = buildAnnotatedString {
                            append("Already have an account? ")
                            withStyle(SpanStyle(color = SignUpLinkBlue, fontWeight = FontWeight.SemiBold)) {
                                append("Log In")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = SignUpLabel
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "By signing up, you agree to our Terms & Privacy",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    NewsPulseTheme {
        CompositionLocalProvider(
            CompositionLocals.LocalViewModelFactory provides createPreviewViewModelFactory()
        ) {
            SignUpScreen(navController = rememberNavController())
        }
    }
}
