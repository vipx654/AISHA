package com.aisha.presentation.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")
    data object ForgotPassword : Screen("forgot_password")
    data object Home : Screen("home")
    data object Chat : Screen("chat")
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
    data object Settings : Screen("settings")
    data object History : Screen("history")
    data object Voice : Screen("voice")
    data object Tasks : Screen("tasks")
    data object Mood : Screen("mood")
    data object Relationship : Screen("relationship")
    data object Export : Screen("export")
}

object NavRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
    const val CHAT = "chat"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val SETTINGS = "settings"
    const val HISTORY = "history"
    const val VOICE = "voice"
    const val TASKS = "tasks"
    const val MOOD = "mood"
    const val RELATIONSHIP = "relationship"
    const val EXPORT = "export"
}
