package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.AppUtils
import com.example.data.KegiatanEntity
import com.example.data.SettingsEntity
import com.example.data.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class SIMPELViewModel(
    private val context: Context,
    private val repository: AppRepository
) : ViewModel() {

    // Active logged in user profile
    val activeUser: StateFlow<UserEntity?> = repository.activeUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Application branding & theme settings
    val appSettings: StateFlow<SettingsEntity> = repository.appSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsEntity()
    )

    // All activity reports
    val allKegiatan: StateFlow<List<KegiatanEntity>> = repository.allKegiatan.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Search queries & filters
    val searchQuery = MutableStateFlow("")
    val filterRt = MutableStateFlow("")
    val filterRw = MutableStateFlow("")
    val filterDate = MutableStateFlow("")

    // Synchronously/Reactively combine filters to form search results
    val filteredKegiatan: StateFlow<List<KegiatanEntity>> = combine(
        allKegiatan,
        searchQuery,
        filterRt,
        filterRw,
        filterDate
    ) { list, query, rt, rw, date ->
        list.filter { item ->
            val matchQuery = query.isEmpty() ||
                    item.judul.contains(query, ignoreCase = true) ||
                    item.uraian.contains(query, ignoreCase = true) ||
                    item.keterangan.contains(query, ignoreCase = true) ||
                    item.hari.contains(query, ignoreCase = true)

            val matchRt = rt.isEmpty() || item.rt == rt
            val matchRw = rw.isEmpty() || item.rw == rw
            val matchDate = date.isEmpty() || item.tanggal.contains(date, ignoreCase = true)

            matchQuery && matchRt && matchRw && matchDate
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Synchronization status
    val isSyncing = MutableStateFlow(false)

    // Active location coordinates string (Default GPS coords: Kelurahan Gedong Panjang)
    val gpsCoordinates = MutableStateFlow("-6.930112, 106.931524 (Kel. Gedong Panjang)")

    // Update GPS coordinates with mock variability to look real
    fun refreshGps() {
        val lat = -6.9300 + (Random.nextDouble() - 0.5) * 0.002
        val lng = 106.9315 + (Random.nextDouble() - 0.5) * 0.002
        val locStr = String.format(Locale.US, "%.6f, %.6f (Sukabumi Baru)", lat, lng)
        gpsCoordinates.value = locStr
    }

    // AUTH ACTIONS
    val loginError = MutableStateFlow<String?>(null)

    fun login(email: String, role: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            loginError.value = null
            if (email.isEmpty()) {
                loginError.value = "Email/Nomor HP tidak boleh kosong"
                return@launch
            }
            val success = repository.authenticate(email, role)
            if (success) {
                onSuccess()
            } else {
                loginError.value = "Gagal login. Periksa email atau role Anda."
            }
        }
    }

    fun register(user: UserEntity) {
        viewModelScope.launch {
            repository.registerUser(user)
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onSuccess()
        }
    }

    // KEGIATAN ACTIONS
    fun addKegiatan(
        judul: String,
        uraian: String,
        keterangan: String,
        tanggalMillis: Long,
        tempPhotoUris: List<Uri>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = activeUser.value ?: return@launch

            // Format date & day
            val calendar = Calendar.getInstance().apply { timeInMillis = tanggalMillis }
            val sdfTanggal = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
            val sdfHari = SimpleDateFormat("EEEE", Locale("id", "ID"))

            val tanggalStr = sdfTanggal.format(calendar.time)
            val hariStr = sdfHari.format(calendar.time)

            // Process & watermark each photo path
            val processedPaths = mutableListOf<String>()
            for (uri in tempPhotoUris) {
                val path = AppUtils.compressAndWatermarkImage(
                    context = context,
                    imageUri = uri,
                    rtName = user.nama,
                    rtNum = user.rt,
                    rwNum = user.rw,
                    locationGps = gpsCoordinates.value
                )
                if (path != null) {
                    processedPaths.add(path)
                }
            }

            val newKegiatan = KegiatanEntity(
                id = "keg_" + UUID.randomUUID().toString().take(8),
                tanggal = tanggalStr,
                hari = hariStr,
                judul = judul,
                uraian = uraian,
                keterangan = keterangan,
                lokasi = gpsCoordinates.value,
                createdBy = user.id,
                rt = user.rt,
                rw = user.rw,
                createdAt = System.currentTimeMillis(),
                isSynced = false,
                photoPaths = processedPaths
            )

            repository.saveKegiatan(newKegiatan)
            onSuccess()
        }
    }

    fun deleteKegiatan(id: String) {
        viewModelScope.launch {
            repository.deleteKegiatan(id)
        }
    }

    // SETTINGS & THEME ACTIONS
    fun updateSettings(settings: SettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(settings)
        }
    }

    // Force synchronization offline-online
    fun triggerSync(onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            isSyncing.value = true
            repository.forceSync()
            isSyncing.value = false
            onCompleted(true)
        }
    }

    // PDF GENERATION ACTION
    val generatedPdfFile = MutableStateFlow<File?>(null)

    fun generateReport(user: UserEntity, onCompleted: (File?) -> Unit) {
        viewModelScope.launch {
            // Filter user reports
            val userReports = allKegiatan.value.filter {
                if (user.role == "Admin Kelurahan") true
                else if (user.role == "RW") it.rw == user.rw
                else it.rt == user.rt && it.rw == user.rw
            }
            val file = AppUtils.generatePdfReport(context, user, userReports)
            generatedPdfFile.value = file
            onCompleted(file)
        }
    }

    // USER PROFILE UPDATE ACTION
    fun updateProfile(
        nama: String,
        nik: String,
        noHp: String,
        alamat: String,
        rt: String,
        rw: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = activeUser.value ?: return@launch
            val updatedUser = user.copy(
                nama = nama,
                nik = nik,
                noHp = noHp,
                alamat = alamat,
                rt = rt,
                rw = rw
            )
            repository.registerUser(updatedUser)
            onSuccess()
        }
    }
}

class SIMPELViewModelFactory(
    private val context: Context,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SIMPELViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SIMPELViewModel(context, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
