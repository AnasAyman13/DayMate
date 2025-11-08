package com.day.mate.ui.theme.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.day.mate.data.VaultItem
import com.day.mate.data.VaultType
import com.day.mate.viewmodel.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VaultScreen(
    navController: NavController,
    viewModel: VaultViewModel = viewModel()
) {
    val context = LocalContext.current

    // جمع الـ StateFlow مباشرة كمراقبة
    val allItems by viewModel.items.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    // تطبيق الفلترة مباشرة
    val items = when (selectedFilter) {
        "Photos" -> allItems.filter { it.type == VaultType.PHOTO }
        "Videos" -> allItems.filter { it.type == VaultType.VIDEO }
        "Documents" -> allItems.filter { it.type == VaultType.DOCUMENT }
        else -> allItems
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris: List<Uri> ->
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val newItems = uris.map { uri ->
                val type = when {
                    uri.toString().endsWith(".mp4") -> VaultType.VIDEO
                    uri.toString().endsWith(".pdf") -> VaultType.DOCUMENT
                    else -> VaultType.PHOTO
                }
                VaultItem(id = uri.hashCode(), uri = uri.toString(), type = type)
            }
            viewModel.addItems(newItems)
        }
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { picker.launch(arrayOf("image/*", "video/*", "application/pdf")) },
                containerColor = Color(0xFFFFD700),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Media Storage ", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(Color(0xFF81D4FA), Color(0xFF4DB6AC))),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Your files are encrypted and private.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // الفلاتر
            val filters = listOf("All", "Photos", "Videos", "Documents")
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val selected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) Color(0xFF4DB6AC) else Color.Black.copy(alpha = 0.05f))
                            .clickable { viewModel.selectFilter(filter) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onBackground,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // عرض الملفات
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    VaultItemCard(
                        item = item,
                        onClick = {
                            navController.navigate("viewer/${Uri.encode(item.uri)}/${item.type.name}")
                        },
                        overlayContent = {
                            IconButton(
                                onClick = { viewModel.removeItem(item) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                            }
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun VaultItemCard(
    item: VaultItem,
    onClick: () -> Unit,
    overlayContent: (@Composable BoxScope.() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (item.type) {
                VaultType.PHOTO -> {
                    AsyncImage(
                        model = item.uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                VaultType.VIDEO -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                VaultType.DOCUMENT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFEEEEEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            overlayContent?.let { it() }
        }
    }
}
