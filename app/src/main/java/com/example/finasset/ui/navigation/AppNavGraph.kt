package com.example.finasset.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.finasset.ui.screens.home.DashboardScreen
import com.example.finasset.ui.screens.stock.*
import com.example.finasset.ui.screens.fund.*
import com.example.finasset.ui.screens.transaction.*
import com.example.finasset.ui.screens.report.*
import com.example.finasset.ui.screens.market.*
import com.example.finasset.ui.screens.settings.*

data class BottomNavItem(
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    BottomNavItem(Screen.StockList, Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp),
    BottomNavItem(Screen.FundList, Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.screen.title
                                )
                            },
                            label = { Text(item.screen.title, style = MaterialTheme.typography.labelSmall) },
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { DashboardScreen(navController) }
            composable(Screen.StockList.route) { StockListScreen(navController) }
            composable(
                Screen.StockDetail.route,
                arguments = listOf(navArgument("stockId") { type = NavType.LongType })
            ) { backStackEntry ->
                val stockId = backStackEntry.arguments?.getLong("stockId") ?: 0L
                StockDetailScreen(navController, stockId)
            }
            composable(Screen.FundList.route) { FundListScreen(navController) }
            composable(
                Screen.FundDetail.route,
                arguments = listOf(navArgument("fundId") { type = NavType.LongType })
            ) { backStackEntry ->
                val fundId = backStackEntry.arguments?.getLong("fundId") ?: 0L
                FundDetailScreen(navController, fundId)
            }
            composable(Screen.Transaction.route) { TransactionScreen(navController) }
            composable(Screen.Report.route) { ReportScreen(navController) }
            composable(Screen.Market.route) { MarketScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen(navController) }

            composable(Screen.AddStock.route) { AddStockScreen(navController) }
            composable(Screen.AddFund.route) { AddFundScreen(navController) }
            composable(
                Screen.AddTransaction.route,
                arguments = listOf(navArgument("assetType") { type = NavType.StringType })
            ) { backStackEntry ->
                val assetType = backStackEntry.arguments?.getString("assetType") ?: "STOCK"
                AddTransactionScreen(navController, assetType = assetType)
            }
            composable(
                Screen.AddTransactionForAsset.route,
                arguments = listOf(
                    navArgument("assetType") { type = NavType.StringType },
                    navArgument("assetId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val assetType = backStackEntry.arguments?.getString("assetType") ?: "STOCK"
                val assetId = backStackEntry.arguments?.getLong("assetId") ?: 0L
                AddTransactionScreen(navController, assetType = assetType, assetId = assetId)
            }
        }
    }
}
