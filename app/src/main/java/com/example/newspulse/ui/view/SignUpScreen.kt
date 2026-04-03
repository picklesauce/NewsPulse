package com.example.newspulse.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.newspulse.ui.viewmodel.SignUpViewModel

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = viewModel(factory = CompositionLocals.LocalViewModelFactory.current)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppBranding(subtitle = "Your personalized news experience")

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Create Account",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NewsPulseAuthColors.primaryText
                    )
                    Text(
                        text = "Sign up to get started",
                        fontSize = 13.sp,
                        color = NewsPulseAuthColors.secondaryText,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    AuthFieldLabel("Username")
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = { viewModel.updateUsername(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Choose a username", color = NewsPulseAuthColors.secondaryText) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = NewsPulseAuthColors.secondaryText)
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
                        placeholder = { Text("you@example.com", color = NewsPulseAuthColors.secondaryText) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = NewsPulseAuthColors.secondaryText)
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
                        placeholder = { Text("Create a password", color = NewsPulseAuthColors.secondaryText) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthFieldLabel("Confirm Password")
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.updateConfirmPassword(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Confirm your password", color = NewsPulseAuthColors.secondaryText) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NewsPulseAuthColors.secondaryText)
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleConfirmPasswordVisible() }) {
                                Icon(
                                    imageVector = if (state.confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (state.confirmPasswordVisible) "Hide password" else "Show password",
                                    tint = NewsPulseAuthColors.secondaryText
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

                    Button(
                        onClick = {
                            viewModel.signUp { success ->
                                if (success) {
                                    navController.navigate("topicSelection") {
                                        popUpTo("signup") { inclusive = true }
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
                            text = "Sign Up",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            fontSize = 14.sp,
                            color = NewsPulseAuthColors.primaryText
                        )
                        Text(
                            text = "Log In",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NewsPulseAuthColors.accent,
                            modifier = Modifier.clickable {
                                navController.navigate("login") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "By signing up, you agree to our Terms & Privacy",
                fontSize = 12.sp,
                color = NewsPulseAuthColors.secondaryText,
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
