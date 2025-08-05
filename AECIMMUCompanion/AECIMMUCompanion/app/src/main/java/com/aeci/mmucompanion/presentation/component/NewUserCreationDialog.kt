package com.aeci.mmucompanion.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aeci.mmucompanion.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewUserCreationDialog(
    onDismiss: () -> Unit,
    onUserCreated: (UserCreationData) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.OPERATOR) }
    var shiftPattern by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var shiftDropdownExpanded by remember { mutableStateOf(false) }
    var departmentDropdownExpanded by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    val departments = listOf(
        "Mining Operations",
        "Maintenance",
        "Safety & Environmental",
        "Quality Control",
        "Administration",
        "Technical Services"
    )
    
    val shiftPatterns = listOf(
        "Day Shift (7:00 - 15:00)",
        "Night Shift (15:00 - 23:00)",
        "Back Shift (23:00 - 7:00)",
        "Rotating 12-hour",
        "Fixed Day Schedule"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create New User",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                HorizontalDivider()
                
                // Profile Image Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Profile Picture",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileImagePicker(
                        currentImageUri = profileImageUri,
                        onImageSelected = { uri -> profileImageUri = uri },
                        size = 80
                    )
                }
                
                // Basic Information
                Text(
                    text = "Basic Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                
                // Role Selection
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRole.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("User Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedRole = role
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Department Selection
                ExposedDropdownMenuBox(
                    expanded = departmentDropdownExpanded,
                    onExpandedChange = { departmentDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Department") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = departmentDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = departmentDropdownExpanded,
                        onDismissRequest = { departmentDropdownExpanded = false }
                    ) {
                        departments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept) },
                                onClick = {
                                    department = dept
                                    departmentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Shift Pattern
                ExposedDropdownMenuBox(
                    expanded = shiftDropdownExpanded,
                    onExpandedChange = { shiftDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = shiftPattern,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Shift Pattern") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shiftDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = shiftDropdownExpanded,
                        onDismissRequest = { shiftDropdownExpanded = false }
                    ) {
                        shiftPatterns.forEach { pattern ->
                            DropdownMenuItem(
                                text = { Text(pattern) },
                                onClick = {
                                    shiftPattern = pattern
                                    shiftDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Security Section
                Text(
                    text = "Security",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword,
                    supportingText = if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                        { Text("Passwords do not match", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (isFormValid(fullName, username, email, department, shiftPattern, password, confirmPassword)) {
                                val userData = UserCreationData(
                                    fullName = fullName,
                                    username = username,
                                    email = email,
                                    department = department,
                                    role = selectedRole,
                                    shiftPattern = shiftPattern,
                                    password = password,
                                    profileImageUri = profileImageUri
                                )
                                onUserCreated(userData)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isFormValid(fullName, username, email, department, shiftPattern, password, confirmPassword)
                    ) {
                        Text("Create User")
                    }
                }
            }
        }
    }
}

data class UserCreationData(
    val fullName: String,
    val username: String,
    val email: String,
    val department: String,
    val role: UserRole,
    val shiftPattern: String,
    val password: String,
    val profileImageUri: String?
)

private fun isFormValid(
    fullName: String,
    username: String,
    email: String,
    department: String,
    shiftPattern: String,
    password: String,
    confirmPassword: String
): Boolean {
    return fullName.isNotBlank() &&
            username.isNotBlank() &&
            email.isNotBlank() &&
            department.isNotBlank() &&
            shiftPattern.isNotBlank() &&
            password.isNotBlank() &&
            password == confirmPassword &&
            password.length >= 6 &&
            email.contains("@")
}
