package com.aeci.mmucompanion.presentation.screen.blast

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aeci.mmucompanion.data.model.WeighbridgeTicket
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BlastReportGenerationDialog(
    fromDate: LocalDate,
    toDate: LocalDate,
    bcmBlasted: Double,
    weighbridgeTickets: List<WeighbridgeTicket>,
    fallbackToBlastHoleLog: Boolean,
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onFromDateChange: (LocalDate) -> Unit,
    onToDateChange: (LocalDate) -> Unit,
    onBcmChange: (String) -> Unit,
    onAddWeighbridgeTicket: (String, String, LocalDate) -> Unit,
    onRemoveWeighbridgeTicket: (Int) -> Unit,
    onToggleFallback: () -> Unit,
    onGenerate: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Generate Blast Report",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Date Selection
                item {
                    DateRangeSelector(
                        fromDate = fromDate,
                        toDate = toDate,
                        onFromDateChange = onFromDateChange,
                        onToDateChange = onToDateChange
                    )
                }

                // BCM Input
                item {
                    var bcmText by remember { mutableStateOf(if (bcmBlasted > 0) bcmBlasted.toString() else "") }
                    
                    OutlinedTextField(
                        value = bcmText,
                        onValueChange = { 
                            bcmText = it
                            onBcmChange(it)
                        },
                        label = { Text("BCM of Material Blasted") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Enter the total cubic meters of material blasted") }
                    )
                }

                // Weighbridge Tickets Section
                item {
                    WeighbridgeTicketsSection(
                        tickets = weighbridgeTickets,
                        onAddTicket = onAddWeighbridgeTicket,
                        onRemoveTicket = onRemoveWeighbridgeTicket
                    )
                }

                // Fallback Option
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = fallbackToBlastHoleLog,
                            onCheckedChange = { onToggleFallback() }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Use Blast Hole Log emulsion data if no weighbridge tickets",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = onGenerate,
                            enabled = !isGenerating && bcmBlasted > 0
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Generate Report")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateRangeSelector(
    fromDate: LocalDate,
    toDate: LocalDate,
    onFromDateChange: (LocalDate) -> Unit,
    onToDateChange: (LocalDate) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    Column {
        Text(
            text = "Date Range",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // From Date
            OutlinedTextField(
                value = fromDate.format(dateFormatter),
                onValueChange = { /* Handle date picker */ },
                label = { Text("From Date") },
                readOnly = true,
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    IconButton(onClick = { /* Open date picker */ }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select from date")
                    }
                }
            )
            
            // To Date
            OutlinedTextField(
                value = toDate.format(dateFormatter),
                onValueChange = { /* Handle date picker */ },
                label = { Text("To Date") },
                readOnly = true,
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    IconButton(onClick = { /* Open date picker */ }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select to date")
                    }
                }
            )
        }
    }
}

@Composable
private fun WeighbridgeTicketsSection(
    tickets: List<WeighbridgeTicket>,
    onAddTicket: (String, String, LocalDate) -> Unit,
    onRemoveTicket: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weighbridge Tickets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            TextButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Ticket")
            }
        }
        
        if (tickets.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "No weighbridge tickets added. Add tickets or enable fallback to blast hole log.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            tickets.forEachIndexed { index, ticket ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ticket: ${ticket.ticketNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Weight: ${ticket.weight} kg",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Date: ${ticket.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        IconButton(onClick = { onRemoveTicket(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove ticket")
                        }
                    }
                }
                
                if (index < tickets.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Show total
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Total Weight: ${tickets.sumOf { it.weight }} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
    
    if (showAddDialog) {
        AddWeighbridgeTicketDialog(
            onDismiss = { showAddDialog = false },
            onAddTicket = { ticketNumber, weight, date ->
                onAddTicket(ticketNumber, weight, date)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddWeighbridgeTicketDialog(
    onDismiss: () -> Unit,
    onAddTicket: (String, String, LocalDate) -> Unit
) {
    var ticketNumber by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Weighbridge Ticket",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = ticketNumber,
                    onValueChange = { ticketNumber = it },
                    label = { Text("Ticket Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    onValueChange = { /* Handle date picker */ },
                    label = { Text("Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { /* Open date picker */ }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select date")
                        }
                    }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { onAddTicket(ticketNumber, weight, date) },
                        enabled = ticketNumber.isNotBlank() && weight.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
