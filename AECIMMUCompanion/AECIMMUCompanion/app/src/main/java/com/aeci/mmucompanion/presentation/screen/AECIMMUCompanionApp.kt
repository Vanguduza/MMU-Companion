@file:OptIn(ExperimentalMaterial3Api::class)

package com.aeci.mmucompanion.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aeci.mmucompanion.R
import com.aeci.mmucompanion.presentation.component.AECIIcons
import com.aeci.mmucompanion.presentation.viewmodel.AuthViewModel
import com.aeci.mmucompanion.presentation.screen.reports.ReportGenerationScreen
import com.aeci.mmucompanion.presentation.screen.blast.BlastReportScreen
import androidx.compose.material3.Divider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AECIMMUCompanionApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()
    
    if (authState.isAuthenticated) {
        MainScreen(navController = navController, authViewModel = authViewModel)
    } else {
        LoginScreen(
            navController = navController,
            authViewModel = authViewModel
        )
    }
}

@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.aeci_logo),
                            contentDescription = "AECI Logo",
                            modifier = Modifier.height(36.dp).padding(end = 8.dp)
                        )
                        Text("AECI MMU Companion")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Menu button
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    // Dropdown menu
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // User Profile submenu
                        DropdownMenuItem(
                            text = { Text("User Profile") },
                            onClick = { 
                                navController.navigate("profile")
                                showMenu = false 
                            },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                        )
                        
                        // Dashboard Options submenu
                        if (authState.currentUser?.role?.name in listOf("ADMIN", "SUPERVISOR")) {
                            DropdownMenuItem(
                                text = { Text("Admin Dashboard") },
                                onClick = { 
                                    navController.navigate("admin_dashboard")
                                    showMenu = false 
                                },
                                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) }
                            )
                        }
                        
                        if (authState.currentUser?.role?.name in listOf("MAINTENANCE", "SUPERVISOR")) {
                            DropdownMenuItem(
                                text = { Text("Millwright Dashboard") },
                                onClick = { 
                                    navController.navigate("millwright_dashboard")
                                    showMenu = false 
                                },
                                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) }
                            )
                        }
                        
                        Divider()
                        
                        // Export Data
                        DropdownMenuItem(
                            text = { Text("Export Data") },
                            onClick = { 
                                navController.navigate("export")
                                showMenu = false 
                            },
                            leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) }
                        )
                        
                        // Settings
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { 
                                navController.navigate("settings")
                                showMenu = false 
                            },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                        )
                        
                        // Logout
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = { 
                                authViewModel.logout()
                                showMenu = false 
                            },
                            leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null) }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Dashboard") },
                    selected = currentRoute == "dashboard",
                    onClick = { navController.navigate("dashboard") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Forms") },
                    selected = currentRoute?.startsWith("forms") == true,
                    onClick = { navController.navigate("forms") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    label = { Text("Technician") },
                    selected = currentRoute?.startsWith("technician") == true,
                    onClick = { navController.navigate("technician_dashboard") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Storage, contentDescription = null) },
                    label = { Text("Equipment") },
                    selected = currentRoute?.startsWith("equipment") == true,
                    onClick = { navController.navigate("equipment") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                    label = { Text("Reports") },
                    selected = currentRoute?.startsWith("reports") == true,
                    onClick = { navController.navigate("reports") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(navController = navController)
            }
            composable("forms") {
                FormsListScreen(navController = navController)
            }
            composable("forms/category/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category")
                FormsCategoryScreen(
                    navController = navController,
                    category = category ?: "production"
                )
            }
            composable("forms/create/{formType}") { backStackEntry ->
                val formType = backStackEntry.arguments?.getString("formType")
                WorkingFormCreationScreen(
                    navController = navController,
                    formType = formType
                )
            }
            composable("form_data_capture/{formType}") { backStackEntry ->
                val formType = backStackEntry.arguments?.getString("formType")
                FormDataEntryScreen(
                    formType = formType ?: "MMU_DAILY_LOG"
                )
            }
            composable("forms/edit/{formId}") { backStackEntry ->
                val formId = backStackEntry.arguments?.getString("formId")
                FormEditScreen(
                    navController = navController,
                    formId = formId
                )
            }
            composable("equipment") {
                EquipmentStatusScreen()
            }
            composable("equipment/list") {
                EquipmentListScreen(navController = navController)
            }
            composable("equipment/{equipmentId}") { backStackEntry ->
                val equipmentId = backStackEntry.arguments?.getString("equipmentId")
                EquipmentDetailsScreen(
                    navController = navController,
                    equipmentId = equipmentId
                )
            }
            composable("reports") {
                ReportsScreen(navController = navController)
            }
            composable("blast_reports") {
                BlastReportScreen(navController = navController)
            }
            composable("export") {
                ExportScreen(navController = navController)
            }
            composable("password_change") {
                PasswordChangeScreen(
                    navController = navController,
                    isFirstLogin = false
                )
            }
            composable("profile") {
                UserProfileScreen(navController = navController)
            }
            composable("admin_dashboard") {
                AdminDashboardScreen(navController = navController)
            }
            composable("millwright_dashboard") {
                MillwrightDashboardScreen(
                    onNavigateToEquipmentDetails = { equipmentId ->
                        navController.navigate("equipment/$equipmentId")
                    }
                )
            }
            composable("technician_dashboard") {
                SimplifiedTechnicianDashboardScreen(navController = navController)
            }
            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            composable("site_management") {
                SiteManagementScreen(navController = navController)
            }
            composable("todo_management") {
                TodoManagementScreen(navController = navController)
            }
            composable("safety_form") {
                WorkingFormCreationScreen(navController = navController, formType = "SAFETY_FORM")
            }
            composable("job_cards") {
                WorkingFormCreationScreen(navController = navController, formType = "JOB_CARD")
            }
            composable("maintenance_form") {
                WorkingFormCreationScreen(navController = navController, formType = "MAINTENANCE_FORM")
            }
            composable("inspection_form") {
                WorkingFormCreationScreen(navController = navController, formType = "INSPECTION_FORM")
            }
            composable("incident_form") {
                WorkingFormCreationScreen(navController = navController, formType = "INCIDENT_FORM")
            }
            composable("equipment_list") {
                EquipmentListScreen(navController = navController)
            }
            composable("my_reports") {
                ReportsScreen(navController = navController)
            }
            composable("admin_reports") {
                AdminReportsScreen(navController = navController)
            }
            composable("user_management") {
                UserManagementScreen(navController = navController)
            }
            composable("system_settings") {
                SystemSettingsScreen(navController = navController)
            }
            composable("audit_logs") {
                AuditLogsScreen(navController = navController)
            }
            composable("equipment_maintenance") {
                EquipmentMaintenanceScreen(navController = navController)
            }
            composable("job_card_management") {
                JobCardManagementScreen(
                    navController = navController,
                    viewModel = hiltViewModel()
                )
            }
            composable("task_list?mode=update_progress") {
                TaskProgressUpdateScreen(navController = navController)
            }
            composable("equipment_list?mode=update_status") {
                EquipmentStatusScreen()
            }
            composable("admin/reports") {
                AdminReportsScreen(navController = navController)
            }
            composable("reports/generate") {
                ReportGenerationScreen(navController = navController)
            }
            composable("admin/reports?filter=downloads") {
                AdminReportsScreen(navController = navController, initialFilter = "downloads")
            }
            composable("admin/reports?tab=statistics") {
                AdminReportsScreen(navController = navController, initialTab = "statistics")
            }
            
            // Technician Management Routes
            composable("technician_list?mode=edit") {
                TechnicianListScreen(navController = navController, mode = "edit")
            }
            composable("technician_list?mode=remove") {
                TechnicianListScreen(navController = navController, mode = "remove")
            }
            composable("technician_list?mode=reset_password") {
                TechnicianListScreen(navController = navController, mode = "reset_password")
            }
            
            // Equipment Management Routes
            composable("equipment_add") {
                EquipmentAddScreen(navController = navController)
            }
            composable("equipment_reports") {
                EquipmentReportsScreen(navController = navController)
            }
            
            // Task Management Routes
            composable("task_list?mode=update_progress") {
                TaskListScreen(navController = navController, mode = "update_progress")
            }
            composable("task_list?mode=remove") {
                TaskListScreen(navController = navController, mode = "remove")
            }
            
            // Report Generation Routes  
            composable("reports/generate") {
                ReportGenerationScreen(navController = navController)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$label Screen (Placeholder)", style = MaterialTheme.typography.headlineMedium)
    }
}
