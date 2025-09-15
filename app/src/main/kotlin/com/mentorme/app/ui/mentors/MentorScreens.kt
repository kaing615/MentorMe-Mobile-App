package com.mentorme.app.ui.mentors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mentorme.app.data.mock.MockData
import com.mentorme.app.ui.common.MentorCard
import com.mentorme.app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorsScreen(navController: NavController) {
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Find Mentors",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { showFilters = !showFilters }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filters")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mentors list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(MockData.mockMentors) { mentor ->
                MentorCard(
                    mentor = mentor,
                    onClick = {
                        navController.navigate(Screen.MentorDetail.createRoute(mentor.id))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorDetailScreen(
    mentorId: String,
    navController: NavController
) {
    val mentor = MockData.mockMentors.find { it.id == mentorId }

    if (mentor == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Mentor not found")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MentorCard(
                mentor = mentor,
                onClick = { }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mentor.user.bio ?: "No bio available",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Expertise",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    mentor.expertise.forEach { skill ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = skill,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    // TODO: Navigate to booking screen
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Book Session")
            }
        }
    }
}
