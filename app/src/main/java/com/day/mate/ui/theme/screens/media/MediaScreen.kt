package com.day.mate.ui.theme.screens.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items // ✅ هذا هو السطر الذي كان ينقصك
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.day.mate.data.local.media.VaultItem
import com.day.mate.data.local.media.VaultType
import com.day.mate.ui.theme.AppGold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VaultScreen(
    navController: NavController,
    viewModel: VaultViewModel = viewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isArabic = remember(configuration) { configuration.locales[0].language == "ar" }

    // Data from ViewModel
    val allItems by viewModel.items.collectAsState()
    val foldersList by viewModel.foldersOnly.collectAsState()
    val currentFolderId by viewModel.currentFolderId.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    // Handle Back Press inside Folder
    BackHandler(enabled = currentFolderId != null) {
        viewModel.goBack()
    }

    // Strings
    val strings = if (isArabic) VaultStringsAR else VaultStringsEN

    // UI States
    var showAddMenu by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    // Action States (Rename / Move)
    var itemToEdit by remember { mutableStateOf<VaultItem?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    // File Picker
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            val newItems = uris.mapNotNull { uri ->
                val mimeType = context.contentResolver.getType(uri)
                val type = when {
                    mimeType?.startsWith("image/") == true -> VaultType.PHOTO
                    mimeType?.startsWith("video/") == true -> VaultType.VIDEO
                    mimeType?.startsWith("audio/") == true -> VaultType.AUDIO
                    else -> VaultType.DOCUMENT
                }
                val name = getFileName(context, uri, "File")
                try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) {}

                VaultItem(uri = uri.toString(), type = type, name = name, isFolder = false)
            }
            viewModel.addItems(newItems)
        }
    )

    // Update Filter Logic
    LaunchedEffect(isArabic) {
        val newFilter = if (isArabic) "الكل" else "All"
        if (selectedFilter != newFilter && selectedFilter in listOf("All", "الكل")) {
            viewModel.selectFilter(newFilter)
        }
    }

    // 🔥 تطبيق الفلترة
    val displayedItems = remember(selectedFilter, allItems) {
        when (selectedFilter) {
            strings.filterPhotos -> allItems.filter { it.type == VaultType.PHOTO || it.isFolder }
            strings.filterVideos -> allItems.filter { it.type == VaultType.VIDEO || it.isFolder }
            strings.filterAudio -> allItems.filter { it.type == VaultType.AUDIO || it.isFolder }
            strings.filterFiles -> allItems.filter { it.type == VaultType.DOCUMENT || it.isFolder }
            strings.filterFolders -> allItems.filter { it.isFolder }
            else -> allItems
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = strings.title, fontWeight = FontWeight.Bold)
                        if (currentFolderId != null) {
                            Text(text = if(isArabic) "داخل مجلد" else "Inside Folder", style = MaterialTheme.typography.bodySmall, color = AppGold)
                        }
                    }
                },
                navigationIcon = {
                    if (currentFolderId != null) {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                actions = {
                    Box {
                        FilledIconButton(
                            onClick = { showAddMenu = true },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = AppGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 16.dp).size(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(28.dp))
                        }
                        DropdownMenu(
                            expanded = showAddMenu,
                            onDismissRequest = { showAddMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text(strings.optionCreateFolder) },
                                leadingIcon = { Icon(Icons.Default.CreateNewFolder, null, tint = AppGold) },
                                onClick = { showAddMenu = false; showCreateFolderDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text(strings.optionUploadFile) },
                                leadingIcon = { Icon(Icons.Default.UploadFile, null, tint = AppGold) },
                                onClick = { showAddMenu = false; picker.launch(arrayOf("*/*")) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 16.dp, start = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(100.dp).background(Brush.linearGradient(listOf(Color(0xFF81D4FA), Color(0xFF4DB6AC))), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = strings.lock, tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(text = strings.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Filters
            item(span = { GridItemSpan(maxLineSpan) }) {
                val filters = listOf(
                    strings.filterAll,
                    strings.filterFolders,
                    strings.filterFiles,
                    strings.filterPhotos,
                    strings.filterVideos,
                    strings.filterAudio
                )
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    filters.forEach { filter ->
                        val selected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.selectFilter(filter) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(text = filter, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Items
            items(displayedItems) { item ->
                VaultItemCard(
                    item = item,
                    onClick = {
                        if (item.isFolder) {
                            viewModel.openFolder(item.id)
                        } else {
                            if (item.type == VaultType.AUDIO || item.type == VaultType.DOCUMENT) {
                                openAudioExternally(context, Uri.parse(item.uri), strings.playAudio)
                            } else {
                                navController.navigate("viewer/${Uri.encode(item.uri)}/${item.type.name}")
                            }
                        }
                    },
                    onMenuAction = { action ->
                        itemToEdit = item
                        when (action) {
                            "rename" -> { renameText = item.name; showRenameDialog = true }
                            "move" -> { showMoveDialog = true }
                            "delete" -> { viewModel.removeItem(item) }
                        }
                    }
                )
            }
        }
    }

    // --- Dialogs ---

    // 1. Create Folder
    if (showCreateFolderDialog) {
        SimpleDialog(
            title = strings.dialogTitle,
            hint = strings.dialogHint,
            value = newFolderName,
            onValueChange = { if (it.length <= 50) newFolderName = it },
            maxLength = 50,
            onConfirm = {
                if (newFolderName.isNotBlank()) {
                    if (viewModel.isNameTaken(newFolderName)) {
                        Toast.makeText(context, if(isArabic) "الاسم موجود بالفعل!" else "Name exists!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.createFolder(newFolderName)
                        newFolderName = ""
                        showCreateFolderDialog = false
                    }
                }
            },
            onDismiss = { showCreateFolderDialog = false }
        )
    }

    // 2. Rename
    if (showRenameDialog && itemToEdit != null) {
        SimpleDialog(
            title = if(isArabic) "إعادة تسمية" else "Rename",
            hint = if(isArabic) "الاسم الجديد" else "New Name",
            value = renameText,
            onValueChange = { if (it.length <= 50) renameText = it },
            maxLength = 50,
            onConfirm = {
                if (renameText.isNotBlank()) {
                    if (renameText != itemToEdit!!.name && viewModel.isNameTaken(renameText)) {
                        Toast.makeText(context, if(isArabic) "الاسم موجود بالفعل!" else "Name exists!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.renameItem(itemToEdit!!, renameText)
                        showRenameDialog = false
                        itemToEdit = null
                    }
                }
            },
            onDismiss = { showRenameDialog = false; itemToEdit = null }
        )
    }

    // 3. Move Dialog (هنا كان الخطأ وتم إصلاحه)
    if (showMoveDialog && itemToEdit != null) {
        AlertDialog(
            onDismissRequest = { showMoveDialog = false; itemToEdit = null },
            title = { Text(if(isArabic) "نقل إلى..." else "Move to...") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    item {
                        TextButton(
                            onClick = { viewModel.moveItemToFolder(itemToEdit!!, null); showMoveDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if(isArabic) "🏠 الصفحة الرئيسية" else "🏠 Home") }
                    }

                    // ✅ تم إصلاح الخطأ هنا باستخدام import androidx.compose.foundation.lazy.items
                    items(foldersList) { folder ->
                        if (folder.id != itemToEdit!!.id) {
                            TextButton(
                                onClick = { viewModel.moveItemToFolder(itemToEdit!!, folder.id); showMoveDialog = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row {
                                    Icon(Icons.Default.Folder, null, tint = AppGold)
                                    Spacer(Modifier.width(8.dp))
                                    Text(folder.name)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showMoveDialog = false }) { Text(strings.dialogCancel) } }
        )
    }
}

// --- Components ---

@Composable
fun VaultItemCard(item: VaultItem, onClick: () -> Unit, onMenuAction: (String) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.aspectRatio(1f).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.isFolder) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Folder, null, tint = AppGold, modifier = Modifier.size(64.dp))
                }
            } else {
                when (item.type) {
                    VaultType.PHOTO -> AsyncImage(model = item.uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    VaultType.VIDEO -> Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) { Icon(Icons.Default.PlayCircle, null, tint = Color.White, modifier = Modifier.size(48.dp)) }
                    VaultType.AUDIO -> Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF6A1B9A), Color(0xFFAB47BC)))), contentAlignment = Alignment.Center) { Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(48.dp)) }
                    VaultType.DOCUMENT -> Box(Modifier.fillMaxSize().background(Color.Gray), contentAlignment = Alignment.Center) { Icon(Icons.Default.Description, null, tint = Color.White, modifier = Modifier.size(48.dp)) }
                }
            }
            Box(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color.Black.copy(0.6f)).padding(8.dp)
            ) {
                Text(text = item.name, color = Color.White, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(end = 24.dp))
            }
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.White, modifier = Modifier.background(Color.Black.copy(0.3f), CircleShape))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("تغيير الاسم") }, onClick = { showMenu = false; onMenuAction("rename") }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                    DropdownMenuItem(text = { Text("نقل") }, onClick = { showMenu = false; onMenuAction("move") }, leadingIcon = { Icon(Icons.Default.DriveFileMove, null) })
                    DropdownMenuItem(text = { Text("حذف", color = Color.Red) }, onClick = { showMenu = false; onMenuAction("delete") }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) })
                }
            }
        }
    }
}

@Composable
fun SimpleDialog(title: String, hint: String, value: String, onValueChange: (String) -> Unit, maxLength: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(hint) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppGold, focusedLabelColor = AppGold, cursorColor = AppGold),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${value.length}/$maxLength",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (value.length == maxLength) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = TextAlign.End
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("موافق", color = AppGold, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

data class VaultStrings(
    val title: String, val subtitle: String, val add: String, val delete: String, val lock: String, val playAudio: String,
    val defaultFileName: String, val filterAll: String,
    val filterFolders: String,
    val filterFiles: String,
    val filterPhotos: String, val filterVideos: String, val filterAudio: String, val filterDocuments: String,
    val optionCreateFolder: String, val optionUploadFile: String, val dialogTitle: String, val dialogHint: String, val dialogCreate: String, val dialogCancel: String
)

val VaultStringsAR = VaultStrings("مخزن الوسائط", "ملفاتك خاصة", "إضافة", "حذف", "قفل", "تشغيل", "ملف", "الكل", "مجلدات", "ملفات", "الصور", "الفيديوهات", "الصوتيات", "المستندات", "إنشاء مجلد", "رفع ملفات", "مجلد جديد", "اسم المجلد", "إنشاء", "إلغاء")
val VaultStringsEN = VaultStrings("Media Vault", "Private Files", "Add", "Delete", "Lock", "Play", "File", "All", "Folders", "Files", "Photos", "Videos", "Audio", "Docs", "Create Folder", "Upload Files", "New Folder", "Folder Name", "Create", "Cancel")

fun openAudioExternally(context: Context, uri: Uri, title: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, "application/*"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        context.startActivity(Intent.createChooser(intent, title))
    } catch (e: Exception) { Log.e("Vault", "Error", e) }
}

fun getFileName(context: Context, uri: Uri, defaultName: String): String {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) result = it.getString(index)
            }
        }
    }
    return result ?: uri.lastPathSegment ?: defaultName
}