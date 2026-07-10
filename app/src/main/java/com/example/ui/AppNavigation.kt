package com.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(viewModel: SIMPELViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(viewModel = viewModel) {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }

        composable("login") {
            LoginScreen(viewModel = viewModel) {
                navController.navigate("dashboard") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                navController = navController,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("input_kegiatan") {
            InputKegiatanScreen(viewModel = viewModel) {
                navController.popBackStack()
            }
        }

        composable("laporan") {
            DaftarLaporanScreen(viewModel = viewModel) {
                navController.popBackStack()
            }
        }

        composable("galeri") {
            GaleriScreen(viewModel = viewModel) {
                navController.popBackStack()
            }
        }

        composable("statistik") {
            StatistikScreen(viewModel = viewModel) {
                navController.popBackStack()
            }
        }

        composable("pengaturan") {
            PengaturanScreen(viewModel = viewModel) {
                navController.popBackStack()
            }
        }

        composable("profil") {
            ProfilScreen(viewModel = viewModel) {
                navController.popBackStack()
            }
        }
    }
}
