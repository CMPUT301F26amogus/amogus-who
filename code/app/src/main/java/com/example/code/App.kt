package com.example.code

import Model
import View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.code.Model.Role
import com.example.code.Model.User
import com.example.code.Views.AddEditCampsite
import com.example.code.Views.AdminHomeView
import com.example.code.Views.CampsiteDetailsView
import com.example.code.Views.CampsiteOwnerHomeView
import com.example.code.Views.EntrantHomeView
import com.example.code.Views.LotteriesView
import com.example.code.Views.MapView
import com.example.code.Views.NotificationsView
import com.example.code.Views.OwnerCampsiteListView
import com.example.code.Views.OwnerLotteriesView
import com.example.code.Views.OwnerReservationsView
import com.example.code.Views.ProfileSettingsView
import com.example.code.Views.RegistrationView
import com.example.code.Views.ReservationsView
import com.example.code.Views.SearchResultsView
import com.example.code.Views.SearchView

/**
 * The names of every screen in the app.
 * Use these with navController.navigate(...) instead of typing the string by hand.
 */
object Routes {
    const val REGISTRATION = "registration"

    // home screens, one per role
    const val ENTRANT_HOME = "entrant_home"
    const val OWNER_HOME = "owner_home"
    const val ADMIN_HOME = "admin_home"

    // shared screens
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"
    const val CAMPSITE_DETAILS = "campsite_details"
    const val MAP = "map"

    // entrant screens
    const val RESERVATIONS = "reservations"
    const val LOTTERIES = "lotteries"
    const val SEARCH = "search"
    const val SEARCH_RESULTS = "search_results"

    // owner screens
    const val OWNER_CAMPSITES = "owner_campsites"
    const val OWNER_RESERVATIONS = "owner_reservations"
    const val OWNER_LOTTERIES = "owner_lotteries"
    const val ADD_EDIT_CAMPSITE = "add_edit_campsite"
}

/**
 * Picks the first screen to show for [user].
 * No user means they haven't registered yet. Otherwise admin wins over owner, and owner over entrant.
 */
fun startRouteFor(user: User?): String = when {
    user == null -> Routes.REGISTRATION
    user.hasRole(Role.ADMIN) -> Routes.ADMIN_HOME
    user.hasRole(Role.OWNER) -> Routes.OWNER_HOME
    else -> Routes.ENTRANT_HOME
}

/**
 * Goes back to the home screen at the bottom of the back stack, clearing everything above it.
 */
fun NavController.goHome() {
    while (previousBackStackEntry != null) {
        popBackStack()
    }
}

/**
 * Sets up navigation between all the views in the app.
 * Waits for [model] to load the current user, then starts on the screen for their role.
 *
 * @param id this device's id, the same one given to [model]
 * @param model the app's model, already set up with [id]
 */
@Composable
fun App(id: String, model: Model) {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        model.getUserObj { user -> startDestination = startRouteFor(user) }
    }

    // still waiting on the database
    val start = startDestination
    if (start == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    NavHost(navController = navController, startDestination = start) {
        composable(Routes.REGISTRATION) { remember { RegistrationView(model) }.ShowScreen(navController) }

        composable(Routes.ENTRANT_HOME) { remember { EntrantHomeView() }.ShowScreen(navController) }
        composable(Routes.OWNER_HOME) { remember { CampsiteOwnerHomeView() }.ShowScreen(navController) }
        composable(Routes.ADMIN_HOME) { remember { AdminHomeView() }.ShowScreen(navController) }

        composable(Routes.PROFILE) { remember { ProfileSettingsView(model) }.ShowScreen(navController) }
        composable(Routes.NOTIFICATIONS) { remember { NotificationsView(id) }.ShowScreen(navController) }
        composable(Routes.CAMPSITE_DETAILS) { remember { CampsiteDetailsView() }.ShowScreen(navController) }
        composable(Routes.MAP) { remember { MapView() }.ShowScreen(navController) }

        composable(Routes.RESERVATIONS) { remember { ReservationsView() }.ShowScreen(navController) }
        composable(Routes.LOTTERIES) { remember { LotteriesView(model) }.ShowScreen(navController) }
        composable(Routes.SEARCH) { remember { SearchView(model) }.ShowScreen(navController) }
        composable(Routes.SEARCH_RESULTS) { remember { SearchResultsView(model) }.ShowScreen(navController) }

        composable(Routes.OWNER_CAMPSITES) { remember { OwnerCampsiteListView() }.ShowScreen(navController) }
        composable(Routes.OWNER_RESERVATIONS) { remember { OwnerReservationsView() }.ShowScreen(navController) }
        composable(Routes.OWNER_LOTTERIES) { remember { OwnerLotteriesView() }.ShowScreen(navController) }
        composable(Routes.ADD_EDIT_CAMPSITE) { remember { AddEditCampsite() }.ShowScreen() }
    }
}
