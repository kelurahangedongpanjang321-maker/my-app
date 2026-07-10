@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.AppUtils
import com.example.data.KegiatanEntity
import com.example.data.SettingsEntity
import com.example.data.UserEntity
import com.example.ui.theme.parseHexColor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

// ==========================================
// 1. SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(
    viewModel: SIMPELViewModel,
    onNavigateToLogin: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsState()
    val context = LocalContext.current

    // Animations
    var startAnims by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnims) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnims) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    LaunchedEffect(Unit) {
        startAnims = true
        // Delay 3 seconds then navigate
        kotlinx.coroutines.delay(3000)
        onNavigateToLogin()
    }

    // Set background color/image based on configurations
    val bgBrush = if (settings.backgroundBase64.isNotEmpty()) {
        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    } else {
        Brush.verticalGradient(
            listOf(
                parseHexColor(settings.warnaUtama, Color(0xFF005AC1)).copy(alpha = 0.15f),
                MaterialTheme.colorScheme.background
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(bgBrush)
            .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Logo GP (Gedong Panjang Logo)
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(scaleAnim)
                    .drawBehind {
                        // Dynamic glowing background pulse
                        val pulse = (sin(System.currentTimeMillis() / 300.0) + 1.0) / 2.0
                        drawCircle(
                            color = parseHexColor(settings.warnaUtama, Color(0xFF005AC1)).copy(alpha = 0.2f * pulse.toFloat()),
                            radius = size.width / 1.7f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Crest Drawing helper
                val customLogoBmp = remember(settings.logoBase64) {
                    try {
                        if (settings.logoBase64.isNotEmpty()) {
                            val decodedString = android.util.Base64.decode(settings.logoBase64, android.util.Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                if (customLogoBmp != null) {
                    Image(
                        bitmap = customLogoBmp.asImageBitmap(),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    // Render beautiful native visual of GP Logo on Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val sizeF = size.width
                        val circlePaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor(settings.warnaUtama)
                            style = android.graphics.Paint.Style.FILL
                            isAntiAlias = true
                        }
                        val borderPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = sizeF * 0.08f
                            isAntiAlias = true
                        }
                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = sizeF * 0.45f
                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }

                        drawCircle(
                            color = Color(circlePaint.color),
                            radius = sizeF / 2
                        )
                        drawContext.canvas.nativeCanvas.drawCircle(
                            sizeF / 2, sizeF / 2, sizeF / 2 - borderPaint.strokeWidth / 2, borderPaint
                        )
                        val textBounds = android.graphics.Rect()
                        textPaint.getTextBounds("GP", 0, 2, textBounds)
                        drawContext.canvas.nativeCanvas.drawText(
                            "GP", sizeF / 2, sizeF / 2 + textBounds.height() / 2f, textPaint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Text SIPAK RT RW with fade-in animation
            Text(
                text = settings.tulisanSplash,
                modifier = Modifier.alpha(alphaAnim),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    color = parseHexColor(settings.warnaUtama, Color(0xFF005AC1)),
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = settings.subTulisanSplash,
                modifier = Modifier.alpha(alphaAnim),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Tagline Divider
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(2.dp)
                    .background(parseHexColor(settings.warnaUtama, Color(0xFF005AC1)).copy(alpha = 0.4f))
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tagline
            Text(
                text = "\"${settings.taglineSplash}\"",
                modifier = Modifier
                    .alpha(alphaAnim)
                    .padding(horizontal = 24.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================
// 2. LOGIN SCREEN
// ==========================================
@Composable
fun LoginScreen(
    viewModel: SIMPELViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsState()
    val errorMsg by viewModel.loginError.collectAsState()

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("RT") } // Default

    val rolesList = listOf("RT", "RW", "Admin Kelurahan")
    var dropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic backgrounds
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            parseHexColor(settings.warnaUtama, Color(0xFF005AC1)).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .widthIn(max = 450.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Circular App Logo Banner
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(parseHexColor(settings.warnaUtama, Color(0xFF005AC1)).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = parseHexColor(settings.warnaUtama, Color(0xFF005AC1)),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = settings.namaAplikasi,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = parseHexColor(settings.warnaUtama, Color(0xFF005AC1))
            )

            Text(
                text = "Sistem Manajemen Pelaporan RT / RW",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Glassmorphic Login Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Masuk Akun",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gunakan email / No HP yang terdaftar",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Role Selection Dropdown Trigger
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedRole,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Jabatan (Role)") },
                            trailingIcon = {
                                IconButton(onClick = { dropdownExpanded = true }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dropdownExpanded = true }
                                .testTag("role_dropdown_trigger")
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            rolesList.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        selectedRole = role
                                        dropdownExpanded = false
                                        // Auto suggestion for quick dev bypass
                                        if (role == "RT") {
                                            emailInput = "rt001@gedongpanjang.id"
                                        } else if (role == "RW") {
                                            emailInput = "rw002@gedongpanjang.id"
                                        } else {
                                            emailInput = "admin@gedongpanjang.id"
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email/Phone input field
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email atau Nomor WhatsApp") },
                        placeholder = { Text("rt001@gedongpanjang.id") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Kata Sandi") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    // Error warning
                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMsg ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sign-In Submit button
                    Button(
                        onClick = {
                            viewModel.login(emailInput, selectedRole) {
                                onNavigateToDashboard()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = parseHexColor(settings.warnaTombol, Color(0xFF005AC1))
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_button")
                    ) {
                        Icon(imageVector = Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MASUK SEKARANG",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Google Login Button
                    OutlinedButton(
                        onClick = {
                            // Prepopulate with a mock email then bypass
                            emailInput = if (selectedRole == "RT") "rt001@gedongpanjang.id" else "admin@gedongpanjang.id"
                            viewModel.login(emailInput, selectedRole) {
                                onNavigateToDashboard()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = parseHexColor(settings.warnaUtama, Color(0xFF005AC1))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Masuk dengan Google",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Info credentials tip
            Text(
                text = "Demo Akun: Gunakan rt001@gedongpanjang.id (Role RT) atau admin@gedongpanjang.id (Role Admin)",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================
// 3. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: SIMPELViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsState()
    val user by viewModel.activeUser.collectAsState()
    val kegiatanList by viewModel.allKegiatan.collectAsState()
    val context = LocalContext.current

    val reportCount = remember(kegiatanList, user) {
        kegiatanList.filter {
            if (user?.role == "Admin Kelurahan") true
            else if (user?.role == "RW") it.rw == user?.rw
            else it.rt == user?.rt
        }.size
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Standard Navigation Bar M3
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Currently in dashboard */ },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("laporan") },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Reports") },
                    label = { Text("Laporan", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("pengaturan") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Pengaturan", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("profil") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profil", fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FF)) // Professional Polish light-blue tint background
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Header / App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Avatar
                val initials = if (user?.nama != null) {
                    val parts = user!!.nama.trim().split(" ")
                    if (parts.size >= 2) {
                        "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                    } else {
                        user!!.nama.take(2).uppercase()
                    }
                } else "GP"
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(parseHexColor(settings.warnaUtama, Color(0xFF005AC1)), CircleShape)
                        .border(4.dp, parseHexColor(settings.warnaUtama, Color(0xFF005AC1)).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SIMPEL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Gedong Panjang".uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                }

                // Logout button styled as a neat custom rounded button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFEFF1F8), RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.logout {
                                onLogout()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Log Out",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // User Info Gradient Card with Decorative circles
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                parseHexColor(settings.warnaUtama, Color(0xFF005AC1)),
                                parseHexColor(settings.warnaUtama, Color(0xFF005AC1)).copy(red = (parseHexColor(settings.warnaUtama, Color(0xFF005AC1)).red * 0.7f).coerceAtLeast(0f), blue = (parseHexColor(settings.warnaUtama, Color(0xFF005AC1)).blue * 0.7f).coerceAtLeast(0f)) // Darkened accent
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .drawBehind {
                        // Drawing decorative overlapping circles
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = 120.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(size.width + 40.dp.toPx(), size.height + 40.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.04f),
                            radius = 60.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(size.width - 20.dp.toPx(), 20.dp.toPx())
                        )
                    }
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Selamat Pagi,",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user?.nama ?: "Bpk. Ahmad Suherman",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val rtRwLabel = if (user?.role == "Admin Kelurahan") "Kelurahan"
                        else "RT ${user?.rt?.padStart(2, '0')} / RW ${user?.rw?.padStart(2, '0')}"
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = rtRwLabel,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val roleLabel = when (user?.role) {
                            "Admin Kelurahan" -> "Admin"
                            "RW" -> "Ketua RW"
                            else -> "Ketua RT"
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = roleLabel,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Statistics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFEFF1F8)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TOTAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = reportCount.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = parseHexColor(settings.warnaUtama, Color(0xFF005AC1))
                            )
                        }
                    }

                    // Pending/Draft Card
                    val pendingCount = kegiatanList.count { !it.isSynced }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFEFF1F8)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "PENDING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pendingCount.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B) // Amber
                            )
                        }
                    }

                    // Synced/Selesai Card
                    val syncedCount = kegiatanList.count { it.isSynced }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFEFF1F8)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SELESAI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = syncedCount.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981) // Emerald
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section Headers
                Text(
                    text = "MENU KEGIATAN",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Feature Grid (M3 Dashboard cards)
                val menuList = mutableListOf(
                    DashboardMenuItem("Input Laporan", Icons.Default.AddPhotoAlternate, "input_kegiatan"),
                    DashboardMenuItem("Daftar Laporan", Icons.AutoMirrored.Filled.List, "laporan"),
                    DashboardMenuItem("Galeri Foto", Icons.Default.PhotoLibrary, "galeri"),
                    DashboardMenuItem("Cetak PDF", Icons.Default.PictureAsPdf, "export_pdf"),
                    DashboardMenuItem("Statistik Kerja", Icons.Default.BarChart, "statistik")
                )
                if (user?.role == "Admin Kelurahan") {
                    menuList.add(DashboardMenuItem("Admin Panel", Icons.Default.AdminPanelSettings, "pengaturan"))
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .height(300.dp) // Adjusted slightly to give perfect spacing
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(menuList) { item ->
                        DashboardCard(item = item, colorHex = settings.warnaUtama) {
                            if (item.route == "export_pdf") {
                                // Trigger PDF generation instantly and display share
                                user?.let { usr ->
                                    viewModel.generateReport(usr) { file ->
                                        if (file != null) {
                                            Toast.makeText(context, "PDF Berhasil Dibuat!", Toast.LENGTH_SHORT).show()
                                            // Share file
                                            val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/pdf"
                                                putExtra(Intent.EXTRA_STREAM, fileUri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan PDF Laporan"))
                                        } else {
                                            Toast.makeText(context, "Gagal membuat PDF", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                navController.navigate(item.route)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bulletin Board / Announcement banner
                Text(
                    text = "PENGUMUMAN KELURAHAN",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = parseHexColor(settings.warnaUtama, Color(0xFF005AC1)).copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, parseHexColor(settings.warnaUtama, Color(0xFF005AC1)).copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(parseHexColor(settings.warnaUtama, Color(0xFF005AC1)).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = parseHexColor(settings.warnaUtama, Color(0xFF005AC1))
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Batas Pelaporan Bulanan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Diharapkan Ketua RT dan RW mengirimkan laporan sebelum tanggal 28 setiap bulannya untuk pencairan insentif.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class DashboardMenuItem(val name: String, val icon: ImageVector, val route: String)

@Composable
fun DashboardCard(item: DashboardMenuItem, colorHex: String, onClick: () -> Unit) {
    val visualTheme = remember(item.route, colorHex) {
        when (item.route) {
            "input_kegiatan" -> Pair(Color(0xFFE8F0FE), parseHexColor(colorHex, Color(0xFF005AC1)))
            "laporan" -> Pair(Color(0xFFE6F4EA), Color(0xFF10B981))
            "export_pdf" -> Pair(Color(0xFFF3E8FD), Color(0xFF8B5CF6))
            "galeri" -> Pair(Color(0xFFFEF7E0), Color(0xFFF59E0B))
            "statistik" -> Pair(Color(0xFFFCE8E6), Color(0xFFEF4444))
            else -> Pair(Color(0xFFF1F3F4), Color(0xFF64748B))
        }
    }
    val iconBgColor = visualTheme.first
    val iconColor = visualTheme.second

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("menu_card_${item.route}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFEFF1F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.name,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (item.route) {
                        "input_kegiatan" -> "Kegiatan Lingkungan"
                        "laporan" -> "Arsip Digital"
                        "export_pdf" -> "Ekspor Dokumen"
                        "galeri" -> "Dokumentasi Visual"
                        "statistik" -> "Indikator Kinerja"
                        else -> "Profil & Aplikasi"
                    },
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// ==========================================
// 4. INPUT KEGIATAN SCREEN
// ==========================================
@Composable
fun InputKegiatanScreen(
    viewModel: SIMPELViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsState()
    val gpsLoc by viewModel.gpsCoordinates.collectAsState()
    val context = LocalContext.current

    var judul by remember { mutableStateOf("") }
    var uraian by remember { mutableStateOf("") }
    var keterangan by remember { mutableStateOf("") }
    var tanggalMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    // Multi photos state (supports up to 10 photos)
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Multi-Image Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris != null) {
            val combined = (photoUris + uris).take(10)
            photoUris = combined
            if (combined.size < uris.size) {
                Toast.makeText(context, "Maksimum 10 Foto saja yang diperbolehkan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera Capture Launcher (Requires a temp private file Uri)
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val combined = (photoUris + tempCameraUri!!).take(10)
            photoUris = combined
        }
    }

    // Helper to generate temporary file uri for camera capture
    fun launchCamera() {
        try {
            val tempFile = File.createTempFile("TEMP_CAPTURE_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal meluncurkan kamera", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Input Pelaporan Kegiatan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshGps() }) {
                        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = "Refresh GPS")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // GPS Location Watermark Status Panel
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Watermark GPS Aktif", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(gpsLoc, color = Color.LightGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form Fields
            OutlinedTextField(
                value = judul,
                onValueChange = { judul = it },
                label = { Text("Judul Kegiatan") },
                placeholder = { Text("Contoh: Monitoring Posyandu Balita") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("judul_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uraian,
                onValueChange = { uraian = it },
                label = { Text("Uraian / Deskripsi Kegiatan") },
                placeholder = { Text("Jelaskan rincian aktivitas yang dilakukan...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = keterangan,
                onValueChange = { keterangan = it },
                label = { Text("Keterangan Tambahan") },
                placeholder = { Text("Contoh: Dihadiri 25 warga, situasi kondusif.") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Upload Section (max 10 images)
            Text(
                text = "FOTO KEGIATAN (${photoUris.size}/10)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Attachment Trigger Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Photo, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pilih Galeri", fontSize = 11.sp)
                }

                Button(
                    onClick = { launchCamera() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ambil Kamera", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Image Thumbnails list
            if (photoUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    items(photoUris) { uri ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Remove thumbnail button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Red, CircleShape)
                                    .clickable { photoUris = photoUris.filter { it != uri } },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada foto terpilih", color = Color.Gray, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Submit Button
            Button(
                onClick = {
                    if (judul.isEmpty() || uraian.isEmpty()) {
                        Toast.makeText(context, "Judul dan Uraian wajib diisi!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addKegiatan(judul, uraian, keterangan, tanggalMillis, photoUris) {
                            Toast.makeText(context, "Laporan berhasil disimpan offline & auto-sync!", Toast.LENGTH_LONG).show()
                            onBack()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = parseHexColor(settings.warnaTombol, Color(0xFF005AC1))),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_report_button")
            ) {
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("KIRIM LAPORAN", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ==========================================
// 5. DAFTAR LAPORAN (LIST SCREEN)
// ==========================================
@Composable
fun DaftarLaporanScreen(
    viewModel: SIMPELViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsState()
    val reports by viewModel.filteredKegiatan.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var textQuery by remember { mutableStateOf("") }
    var activeFilterRt by remember { mutableStateOf("") }
    var activeFilterRw by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Daftar Pelaporan Kegiatan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.triggerSync {
                                Toast.makeText(context, "Sinkronisasi Firebase Berhasil!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = if (isSyncing) parseHexColor(settings.warnaUtama, Color.Red) else LocalContentColor.current
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Search Filters Bar
            Card(
                shape = RoundedCornerShape(0.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Search Text Box
                    OutlinedTextField(
                        value = textQuery,
                        onValueChange = {
                            textQuery = it
                            viewModel.searchQuery.value = it
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        placeholder = { Text("Cari RT, RW, tanggal, judul...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_bar_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row of RT/RW chip filters
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = activeFilterRt,
                            onValueChange = {
                                activeFilterRt = it
                                viewModel.filterRt.value = it
                            },
                            placeholder = { Text("RT") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = activeFilterRw,
                            onValueChange = {
                                activeFilterRw = it
                                viewModel.filterRw.value = it
                            },
                            placeholder = { Text("RW") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }

            // Reports Lazy List
            if (reports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LayersClear,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Belum ada laporan kegiatan", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Gunakan menu Input Kegiatan untuk memulai laporan baru.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(reports) { item ->
                        ReportItemRow(item = item, colorHex = settings.warnaUtama) {
                            viewModel.deleteKegiatan(item.id)
                            Toast.makeText(context, "Laporan berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItemRow(item: KegiatanEntity, colorHex: String, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("report_item_row_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular Badge with RT/RW number representation
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(parseHexColor(colorHex, Color(0xFF005AC1)).copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.rt,
                        fontWeight = FontWeight.Black,
                        color = parseHexColor(colorHex, Color(0xFF005AC1)),
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(item.judul, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "${item.hari}, ${item.tanggal} | RT ${item.rt} RW ${item.rw}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Sync Icon Badge
                Icon(
                    imageVector = if (item.isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (item.isSynced) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expanded detail segment
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Rincian Kegiatan:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(item.uraian, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Keterangan:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(item.keterangan, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Titik GPS Lokasi:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(item.lokasi, fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Photos Row List
                    if (item.photoPaths.isNotEmpty()) {
                        Text("Foto Lampiran (${item.photoPaths.size} Foto):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        ) {
                            items(item.photoPaths) { path ->
                                val file = File(path)
                                if (file.exists()) {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = file,
                                            contentDescription = "Lampiran",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row of Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. GALERI FOTO SCREEN
// ==========================================
@Composable
fun GaleriScreen(
    viewModel: SIMPELViewModel,
    onBack: () -> Unit
) {
    val reports by viewModel.allKegiatan.collectAsState()
    val allPhotos = remember(reports) {
        reports.flatMap { item ->
            item.photoPaths.map { path -> Pair(path, item) }
        }
    }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Galeri Foto Kegiatan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (allPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Belum ada foto kegiatan diunggah", color = Color.Gray)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(allPhotos) { (path, kegiatan) ->
                    val file = File(path)
                    if (file.exists()) {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            AsyncImage(
                                model = file,
                                contentDescription = "Galeri",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Small overlay showing RT number
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("RT ${kegiatan.rt}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. STATISTIK SCREEN
// ==========================================
@Composable
fun StatistikScreen(
    viewModel: SIMPELViewModel,
    onBack: () -> Unit
) {
    val reports by viewModel.allKegiatan.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    // Aggregate statistics
    val totalReports = reports.size
    val syncedReports = reports.count { it.isSynced }
    val unsyncedReports = totalReports - syncedReports

    // Grouping by RT
    val reportsByRt = remember(reports) {
        reports.groupBy { it.rt }.mapValues { it.value.size }
    }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Statistik & Kinerja RT/RW", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("IKHTISAR LAPORAN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Synced
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Sudah Sync", fontSize = 11.sp, color = Color(0xFF2E7D32))
                        Text(syncedReports.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                    }
                }

                // Unsynced
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Pending Sync", fontSize = 11.sp, color = Color(0xFFE65100))
                        Text(unsyncedReports.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text("GRAFIK AKTIVITAS RT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(14.dp))

            // Custom Drawing Canvas Bar Chart for reports per RT
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                if (reportsByRt.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada data grafik", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    val maxVal = reportsByRt.values.maxOrNull() ?: 1
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        val width = size.width
                        val height = size.height

                        // Baseline
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, height),
                            end = Offset(width, height),
                            strokeWidth = 2f
                        )

                        val barWidth = 40.dp.toPx()
                        val spacing = (width - (barWidth * reportsByRt.size)) / (reportsByRt.size + 1)

                        var curX = spacing
                        val primaryColor = parseHexColor(settings.warnaUtama, Color.Red)

                        reportsByRt.forEach { (rtNum, count) ->
                            val barHeight = (count.toFloat() / maxVal) * (height - 40f)

                            // Draw Bar
                            drawRoundRect(
                                color = primaryColor,
                                topLeft = Offset(curX, height - barHeight),
                                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                            )

                            // Label count above bar
                            val textPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 10.dp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                            }
                            drawContext.canvas.nativeCanvas.drawText(
                                count.toString(),
                                curX + barWidth / 2f,
                                height - barHeight - 10f,
                                textPaint
                            )

                            // Label RT name below bar
                            drawContext.canvas.nativeCanvas.drawText(
                                "RT $rtNum",
                                curX + barWidth / 2f,
                                height + 15.dp.toPx(),
                                textPaint
                            )

                            curX += barWidth + spacing
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Map list representation
            Text("PETA TITIK KEGIATAN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))

            reports.take(3).forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.PinDrop, contentDescription = null, tint = parseHexColor(settings.warnaUtama, Color.Red))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(item.judul, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(item.lokasi, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. PENGATURAN SCREEN (SETTINGS & THEME)
// ==========================================
@Composable
fun PengaturanScreen(
    viewModel: SIMPELViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsState()
    val context = LocalContext.current

    var appNameInput by remember { mutableStateOf(settings.namaAplikasi) }
    var splashTextInput by remember { mutableStateOf(settings.tulisanSplash) }

    // Preset color themes matching Indonesian kelurahan branding
    val themePresets = listOf(
        Pair("Tema Biru (Professional Polish)", "#005AC1"),
        Pair("Tema Merah (Kelurahan)", "#E53935"),
        Pair("Tema Biru (Kota Sukabumi)", "#1E88E5"),
        Pair("Tema Hijau (Keasrian)", "#4CAF50"),
        Pair("Tema Hitam (Obsidian)", "#212121")
    )

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Pengaturan & Kustomisasi", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("IDENTITAS APLIKASI", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = appNameInput,
                onValueChange = { appNameInput = it },
                label = { Text("Nama Sistem Aplikasi") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_name_setting_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = splashTextInput,
                onValueChange = { splashTextInput = it },
                label = { Text("Tulisan Splash Screen") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("PILIHAN TEMA WARNA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // Theme chips selection
            themePresets.forEach { (name, hex) ->
                val isSelected = settings.warnaUtama == hex
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.updateSettings(
                                settings.copy(
                                    warnaUtama = hex,
                                    warnaTombol = hex
                                )
                            )
                        }
                        .padding(vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(parseHexColor(hex, Color.Red), CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = name,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) parseHexColor(hex, Color.Red) else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = parseHexColor(hex, Color.Red))
                    }
                }
                Divider(color = Color.LightGray.copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("TEMA MODE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // Light / Dark mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.updateSettings(settings.copy(temaMode = "Light Mode")) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (settings.temaMode == "Light Mode") parseHexColor(settings.warnaUtama, Color.Red) else Color.LightGray
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Light Mode", fontSize = 11.sp)
                }

                Button(
                    onClick = { viewModel.updateSettings(settings.copy(temaMode = "Dark Mode")) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (settings.temaMode == "Dark Mode") parseHexColor(settings.warnaUtama, Color.Red) else Color.LightGray
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dark Mode", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("PENGATURAN BAHASA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.updateSettings(settings.copy(bahasa = "Indonesia")) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (settings.bahasa == "Indonesia") parseHexColor(settings.warnaUtama, Color.Red) else Color.LightGray
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Bahasa Indonesia", fontSize = 11.sp)
                }

                Button(
                    onClick = { viewModel.updateSettings(settings.copy(bahasa = "Inggris")) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (settings.bahasa == "Inggris") parseHexColor(settings.warnaUtama, Color.Red) else Color.LightGray
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("English", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save settings action
            Button(
                onClick = {
                    viewModel.updateSettings(
                        settings.copy(
                            namaAplikasi = appNameInput,
                            tulisanSplash = splashTextInput
                        )
                    )
                    Toast.makeText(context, "Sistem Kustomisasi Disimpan!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = parseHexColor(settings.warnaTombol, Color.Red)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SIMPAN SEMUA PERUBAHAN", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ==========================================
// 9. PROFIL USER SCREEN
// ==========================================
@Composable
fun ProfilScreen(
    viewModel: SIMPELViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsState()
    val user by viewModel.activeUser.collectAsState()
    val context = LocalContext.current

    var namaInput by remember { mutableStateOf("") }
    var nikInput by remember { mutableStateOf("") }
    var noHpInput by remember { mutableStateOf("") }
    var alamatInput by remember { mutableStateOf("") }
    var rtInput by remember { mutableStateOf("") }
    var rwInput by remember { mutableStateOf("") }

    // Load initial values from logged-in user profile
    LaunchedEffect(user) {
        if (user != null) {
            namaInput = user!!.nama
            nikInput = user!!.nik
            noHpInput = user!!.noHp
            alamatInput = user!!.alamat
            rtInput = user!!.rt
            rwInput = user!!.rw
        }
    }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Profil Pengguna (RT/RW)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(parseHexColor(settings.warnaUtama, Color.Red).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = parseHexColor(settings.warnaUtama, Color.Red), modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(namaInput, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = if (user?.role == "Admin Kelurahan") "Lurah Kelurahan Gedong Panjang" else "Ketua RT $rtInput / RW $rwInput",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("DATA ADMINISTRASI", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = namaInput,
                onValueChange = { namaInput = it },
                label = { Text("Nama Lengkap Ketua") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nikInput,
                onValueChange = { nikInput = it },
                label = { Text("Nomor Induk Kependudukan (NIK)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = noHpInput,
                onValueChange = { noHpInput = it },
                label = { Text("Nomor HP (WhatsApp)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = alamatInput,
                onValueChange = { alamatInput = it },
                label = { Text("Alamat Tinggal Lengkap") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = rtInput,
                    onValueChange = { rtInput = it },
                    label = { Text("RT") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = rwInput,
                    onValueChange = { rwInput = it },
                    label = { Text("RW") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Save Action
            Button(
                onClick = {
                    viewModel.updateProfile(namaInput, nikInput, noHpInput, alamatInput, rtInput, rwInput) {
                        Toast.makeText(context, "Profil Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = parseHexColor(settings.warnaTombol, Color.Red)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button")
            ) {
                Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("PERBARUI PROFIL SAYA", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
