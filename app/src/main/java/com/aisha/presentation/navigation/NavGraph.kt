package com.aisha.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aisha.presentation.auth.ForgotPasswordScreen
import com.aisha.presentation.auth.SignInScreen
import com.aisha.presentation.auth.SignUpScreen
import com.aisha.presentation.chat.ChatScreen
import com.aisha.presentation.home.HomeScreen
import com.aisha.presentation.profile.EditProfileScreen
import com.aisha.presentation.profile.ProfileScreen
import com.aisha.presentation.settings.SettingsScreen
import com.aisha.presentation.history.HistoryScreen
import com.aisha.presentation.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = NavRoutes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onNavigateToSignIn = {
                    navController.navigate(NavRoutes.SIGN_IN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.SIGN_IN) {
            SignInScreen(
                onNavigateToSignUp = {
                    navController.navigate(NavRoutes.SIGN_UP)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(NavRoutes.FORGOT_PASSWORD)
                },
                onSignInSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.SIGN_UP) {
            SignUpScreen(
                onNavigateToSignIn = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEmailSent = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.PROFILE)
                },
                onNavigateToChat = {
                    navController.navigate(NavRoutes.CHAT)
                },
                onSignOut = {
                    navController.navigate(NavRoutes.SIGN_IN) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.CHAT) {
            ChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditProfile = {
                    navController.navigate(NavRoutes.EDIT_PROFILE)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onSignOut = {
                    navController.navigate(NavRoutes.SIGN_IN) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.EDIT_PROFILE) {
            EditProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProfileUpdated = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHistory = {
                    navController.navigate(NavRoutes.HISTORY)
                },
                onSignOut = {
                    navController.navigate(NavRoutes.SIGN_IN) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.HISTORY) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
