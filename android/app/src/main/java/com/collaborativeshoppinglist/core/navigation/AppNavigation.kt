package com.collaborativeshoppinglist.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.collaborativeshoppinglist.feature.auth.AuthScreen
import com.collaborativeshoppinglist.feature.auth.AuthViewModel
import com.collaborativeshoppinglist.feature.invitations.CreateInvitationScreen
import com.collaborativeshoppinglist.feature.invitations.InvitationInboxScreen
import com.collaborativeshoppinglist.feature.lists.ListDetailScreen
import com.collaborativeshoppinglist.feature.lists.ListOverviewScreen

object AppRoute {
    const val AUTH = "auth"
    const val LISTS = "lists"
    const val INVITATIONS = "invitations"
    const val LIST_DETAIL = "lists/{listId}"
    const val CREATE_INVITATION = "lists/{listId}/invite"

    fun listDetail(listId: String): String = "lists/" + listId
    fun createInvitation(listId: String): String = "lists/" + listId + "/invite"
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val authState by authViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(authState.isAuthenticated) {
        val currentRoute = navController.currentDestination?.route
        if (authState.isAuthenticated && currentRoute == AppRoute.AUTH) {
            navController.navigate(AppRoute.LISTS) {
                popUpTo(AppRoute.AUTH) { inclusive = true }
            }
        } else if (!authState.isAuthenticated && currentRoute != AppRoute.AUTH) {
            navController.navigate(AppRoute.AUTH) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authState.isAuthenticated) AppRoute.LISTS else AppRoute.AUTH,
    ) {
        composable(AppRoute.AUTH) { AuthScreen() }
        composable(AppRoute.LISTS) {
            ListOverviewScreen(
                onListSelected = { navController.navigate(AppRoute.listDetail(it)) },
                onInvitations = { navController.navigate(AppRoute.INVITATIONS) },
            )
        }
        composable(AppRoute.INVITATIONS) {
            InvitationInboxScreen(
                onBack = { navController.popBackStack() },
                onAccepted = {
                    navController.navigate(AppRoute.listDetail(it)) {
                        popUpTo(AppRoute.LISTS)
                    }
                },
            )
        }
        composable(AppRoute.LIST_DETAIL) {
            ListDetailScreen(
                onBack = { navController.popBackStack() },
                onInvite = { navController.navigate(AppRoute.createInvitation(it)) },
            )
        }
        composable(AppRoute.CREATE_INVITATION) { backStackEntry ->
            CreateInvitationScreen(
                listId = backStackEntry.arguments?.getString("listId").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
