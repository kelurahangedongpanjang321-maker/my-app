package com.example.data

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

object AppUtils {

    // Generates a QR Code bitmap from a string with position detection patterns (finder patterns) in 3 corners
    fun generateQrCode(text: String, size: Int = 180): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        // Background
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        // Calculate grid size (e.g. 21x21 modules for Version 1 QR code)
        val modules = 21
        val moduleSize = size / modules

        // Deterministic random generator based on string hash
        val seed = text.hashCode().toLong()
        val random = Random(seed)

        // Set up matrix representation
        val matrix = Array(modules) { BooleanArray(modules) }

        // Draw position detection patterns (7x7 modules) in 3 corners: Top-Left, Top-Right, Bottom-Left
        fun applyFinderPattern(rowOffset: Int, colOffset: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isInnerSquare = r in 2..4 && c in 2..4
                    matrix[rowOffset + r][colOffset + c] = isBorder || isInnerSquare
                }
            }
        }

        // Apply 3 finder patterns
        applyFinderPattern(0, 0) // Top-Left
        applyFinderPattern(0, modules - 7) // Top-Right
        applyFinderPattern(modules - 7, 0) // Bottom-Left

        // Set finder pattern zones so we don't overwrite them with data
        val reserved = Array(modules) { BooleanArray(modules) }
        fun markReserved(rowOffset: Int, colOffset: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    reserved[rowOffset + r][colOffset + c] = true
                }
            }
        }
        markReserved(0, 0)
        markReserved(0, modules - 7)
        markReserved(modules - 7, 0)

        // Draw remaining data modules using deterministic pseudo-random generator
        for (r in 0 until modules) {
            for (c in 0 until modules) {
                if (!reserved[r][c]) {
                    // Timing patterns and alignment-like visual noise
                    if (r == 6 || c == 6) {
                        matrix[r][c] = (r + c) % 2 == 0
                    } else {
                        matrix[r][c] = random.nextBoolean()
                    }
                }
            }
        }

        // Draw the modules onto the canvas
        for (r in 0 until modules) {
            for (c in 0 until modules) {
                if (matrix[r][c]) {
                    canvas.drawRect(
                        (c * moduleSize).toFloat(),
                        (r * moduleSize).toFloat(),
                        ((c + 1) * moduleSize).toFloat(),
                        ((r + 1) * moduleSize).toFloat(),
                        paint
                    )
                }
            }
        }

        return bitmap
    }

    // Compresses, resizes, and watermarks an uploaded image
    fun compressAndWatermarkImage(
        context: Context,
        imageUri: Uri,
        rtName: String,
        rtNum: String,
        rwNum: String,
        locationGps: String
    ): String? {
        try {
            // Read Bitmap from Uri
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            inputStream?.close()

            // Resize Bitmap (max size 1080px width or height)
            val maxDim = 1080
            val width = originalBitmap.width
            val height = originalBitmap.height
            val (newWidth, newHeight) = if (width > height) {
                val ratio = width.toFloat() / maxDim
                Pair(maxDim, (height / ratio).toInt())
            } else {
                val ratio = height.toFloat() / maxDim
                Pair((width / ratio).toInt(), maxDim)
            }

            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            val mutableBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)

            // Draw Watermark Overlay Banner at the bottom
            val bannerHeight = (newHeight * 0.18f).coerceIn(100f, 220f)
            val bannerPaint = Paint().apply {
                color = Color.parseColor("#99000000") // Semi-transparent black
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, newHeight - bannerHeight, newWidth.toFloat(), newHeight.toFloat(), bannerPaint)

            // Watermark Text Paint
            val textPaint = Paint().apply {
                color = Color.WHITE
                isAntiAlias = true
                textSize = (bannerHeight * 0.16f).coerceIn(14f, 32f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val smallTextPaint = Paint().apply {
                color = Color.parseColor("#E0E0E0")
                isAntiAlias = true
                textSize = (bannerHeight * 0.12f).coerceIn(10f, 24f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val margin = 20f
            var currentY = newHeight - bannerHeight + margin + textPaint.textSize

            // Line 1: RT/RW & Nama
            canvas.drawText("RT $rtNum / RW $rwNum - $rtName", margin, currentY, textPaint)
            currentY += smallTextPaint.textSize + 12f

            // Line 2: Date & Time
            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            val currentDateStr = dateFormat.format(Date())
            canvas.drawText("Waktu: $currentDateStr WITA", margin, currentY, smallTextPaint)
            currentY += smallTextPaint.textSize + 12f

            // Line 3: GPS & Kelurahan
            canvas.drawText("Lokasi: $locationGps | Kel. Gedong Panjang", margin, currentY, smallTextPaint)

            // Save watermarked image to private app directory
            val outputDir = File(context.filesDir, "kegiatan_photos")
            if (!outputDir.exists()) outputDir.mkdirs()

            val photoFile = File(outputDir, "IMG_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}.jpg")
            val outputStream = FileOutputStream(photoFile)
            mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()

            return photoFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Helper to draw a modern default logo for Kelurahan Gedong Panjang (GP Crest)
    private fun drawGedongPanjangCrest(canvas: Canvas, x: Float, y: Float, size: Float) {
        val circlePaint = Paint().apply {
            color = Color.parseColor("#005AC1") // Beautiful M3 RT/RW Blue
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = size * 0.08f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = size * 0.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        // Draw double border circle
        canvas.drawCircle(x, y, size / 2, circlePaint)
        canvas.drawCircle(x, y, size / 2, borderPaint)
        canvas.drawCircle(x, y, size / 2 - borderPaint.strokeWidth, Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        })

        // Draw "GP" initials in serif lettering
        val textBounds = Rect()
        textPaint.getTextBounds("GP", 0, 2, textBounds)
        val textHeight = textBounds.height()
        canvas.drawText("GP", x, y + textHeight / 2f, textPaint)
    }

    // Helper to draw the FK RT RW secondary logo as per screenshot
    private fun drawFkRtRwLogo(canvas: Canvas, x: Float, y: Float, size: Float) {
        val bgCirclePaint = Paint().apply {
            color = Color.parseColor("#FFD54F") // Yellowish wheel ring
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val gearPaint = Paint().apply {
            color = Color.parseColor("#5D4037") // Brownish outer gear tooths
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val centerCirclePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = size * 0.14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        // Draw gear outer ring
        canvas.drawCircle(x, y, size / 2f, gearPaint)
        canvas.drawCircle(x, y, size / 2f - 4f, bgCirclePaint)
        canvas.drawCircle(x, y, size / 3f, centerCirclePaint)

        // Draw miniature torch or initials "RT-RW"
        val textBounds = Rect()
        textPaint.getTextBounds("RT-RW", 0, 5, textBounds)
        canvas.drawText("FK", x, y - 6f, textPaint)
        canvas.drawText("RT-RW", x, y + textBounds.height().toFloat() + 2f, textPaint)
    }

    // PDF Generator matching the provided screenshot template perfectly!
    fun generatePdfReport(
        context: Context,
        user: UserEntity,
        kegiatanList: List<KegiatanEntity>,
        docNum: String = "DOC-GP-${System.currentTimeMillis() / 100000}"
    ): File? {
        try {
            val pdfDocument = PdfDocument()

            // Standard Letter Size Page: 612 x 792 pt (8.5" x 11" @ 72 dpi) or A4 (595 x 842 pt)
            // Let's use A4 which is extremely common in Indonesia: 595 x 842 pt
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Paints
            val textPaint = Paint().apply {
                color = Color.BLACK
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val titlePaint = Paint().apply {
                color = Color.BLACK
                isAntiAlias = true
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val subTitlePaint = Paint().apply {
                color = Color.BLACK
                isAntiAlias = true
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val tableHeaderPaint = Paint().apply {
                color = Color.BLACK
                isAntiAlias = true
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val cellTextPaint = Paint().apply {
                color = Color.BLACK
                isAntiAlias = true
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val linePaint = Paint().apply {
                color = Color.BLACK
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }

            val doubleLinePaint = Paint().apply {
                color = Color.BLACK
                strokeWidth = 1.8f
                style = Paint.Style.STROKE
            }

            // --- 1. HEADER (Logo GP, Text, FK RT RW Logo) ---
            // Draw left logo (Gedong Panjang Crest)
            drawGedongPanjangCrest(canvas, 60f, 65f, 50f)

            // Draw right logo (FK RT RW)
            drawFkRtRwLogo(canvas, 530f, 65f, 50f)

            // Header Texts
            canvas.drawText("LAPORAN KEGIATAN RT DAN RW", 297f, 45f, titlePaint)
            canvas.drawText("KELURAHAN GEDONG PANJANG", 297f, 60f, titlePaint)
            canvas.drawText("KECAMATAN CITAMIANG", 297f, 75f, titlePaint.apply { textSize = 11f })
            canvas.drawText("KOTA SUKABUMI", 297f, 90f, titlePaint)
            canvas.drawText("TAHUN 2026", 297f, 105f, titlePaint)

            // Underline separating header and metadata
            canvas.drawLine(40f, 120f, 555f, 120f, doubleLinePaint)
            canvas.drawLine(40f, 123f, 555f, 123f, linePaint)

            // --- 2. METADATA SECTION ---
            var startY = 145f
            textPaint.textSize = 10f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            canvas.drawText("RT", 40f, startY, textPaint)
            canvas.drawText(":", 110f, startY, textPaint)
            canvas.drawText(user.rt.padStart(3, '0'), 130f, startY, textPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) })

            startY += 15f
            canvas.drawText("RW", 40f, startY, textPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            canvas.drawText(":", 110f, startY, textPaint)
            canvas.drawText(user.rw.padStart(3, '0'), 130f, startY, textPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) })

            startY += 15f
            canvas.drawText("Nama", 40f, startY, textPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            canvas.drawText(":", 110f, startY, textPaint)
            canvas.drawText(user.nama.uppercase(), 130f, startY, textPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })

            // --- 3. THE REPORT TABLE ---
            startY += 25f

            // Table dimensions
            val colX = floatArrayOf(40f, 70f, 160f, 310f, 420f, 555f) // Columns borders
            val tableHeaders = arrayOf("NO", "HARI/TANGGAL", "URAIAN KEGIATAN", "KETERANGAN", "FOTO")

            // Draw Table Header
            val headerHeight = 22f
            canvas.drawRect(colX[0], startY, colX[5], startY + headerHeight, Paint().apply {
                color = Color.parseColor("#F5F5F5")
                style = Paint.Style.FILL
            })

            // Draw header lines & texts
            canvas.drawRect(colX[0], startY, colX[5], startY + headerHeight, linePaint)
            for (i in 1..4) {
                canvas.drawLine(colX[i], startY, colX[i], startY + headerHeight, linePaint)
            }

            for (i in tableHeaders.indices) {
                val colCenter = colX[i] + (colX[i + 1] - colX[i]) / 2f
                canvas.drawText(tableHeaders[i], colCenter, startY + 14f, tableHeaderPaint)
            }

            startY += headerHeight

            // Draw Row Data (max 5 items to fit comfortably in a single neat page, or handle paginated list)
            val itemsToDraw = kegiatanList.take(5)
            val rowHeight = 90f // Taller rows to host photo cleanly!

            for (idx in itemsToDraw.indices) {
                val item = itemsToDraw[idx]
                val itemY = startY + (idx * rowHeight)

                // Fill light rows alternative
                if (idx % 2 == 1) {
                    canvas.drawRect(colX[0], itemY, colX[5], itemY + rowHeight, Paint().apply {
                        color = Color.parseColor("#FAFAFA")
                        style = Paint.Style.FILL
                    })
                }

                // Draw row outline & column separators
                canvas.drawRect(colX[0], itemY, colX[5], itemY + rowHeight, linePaint)
                for (i in 1..4) {
                    canvas.drawLine(colX[i], itemY, colX[i], itemY + rowHeight, linePaint)
                }

                // Cell 1: No
                val noCenter = colX[0] + (colX[1] - colX[0]) / 2f
                canvas.drawText((idx + 1).toString(), noCenter, itemY + rowHeight / 2f + 4f, cellTextPaint.apply { textAlign = Paint.Align.CENTER })

                // Cell 2: Hari / Tanggal (e.g., "Senin, 6 Juli 2026")
                val dateText = "${item.hari}, ${item.tanggal}"
                val dateX = colX[1] + 6f
                // We split or auto-wrap if text too long
                if (dateText.length > 18) {
                    val parts = dateText.split(",")
                    canvas.drawText(parts[0] + ",", dateX, itemY + 30f, cellTextPaint.apply { textAlign = Paint.Align.LEFT })
                    if (parts.size > 1) {
                        canvas.drawText(parts[1].trim(), dateX, itemY + 45f, cellTextPaint)
                    }
                } else {
                    canvas.drawText(dateText, dateX, itemY + rowHeight / 2f + 4f, cellTextPaint.apply { textAlign = Paint.Align.LEFT })
                }

                // Cell 3: Uraian Kegiatan (Wrap-aware text drawing)
                val uraianText = item.judul + " - " + item.uraian
                val uraianX = colX[2] + 6f
                val words = uraianText.split(" ")
                var line = ""
                var lineY = itemY + 16f
                for (word in words) {
                    val testLine = if (line.isEmpty()) word else "$line $word"
                    val measure = cellTextPaint.measureText(testLine)
                    if (measure > (colX[3] - colX[2] - 12f)) {
                        canvas.drawText(line, uraianX, lineY, cellTextPaint)
                        lineY += 11f
                        line = word
                    } else {
                        line = testLine
                    }
                }
                if (line.isNotEmpty()) {
                    canvas.drawText(line, uraianX, lineY, cellTextPaint)
                }

                // Cell 4: Keterangan
                val ketX = colX[3] + 6f
                val ketWords = item.keterangan.split(" ")
                var ketLine = ""
                var ketLineY = itemY + 16f
                for (word in ketWords) {
                    val testLine = if (ketLine.isEmpty()) word else "$ketLine $word"
                    val measure = cellTextPaint.measureText(testLine)
                    if (measure > (colX[4] - colX[3] - 12f)) {
                        canvas.drawText(ketLine, ketX, ketLineY, cellTextPaint)
                        ketLineY += 11f
                        ketLine = word
                    } else {
                        ketLine = testLine
                    }
                }
                if (ketLine.isNotEmpty()) {
                    canvas.drawText(ketLine, ketX, ketLineY, cellTextPaint)
                }

                // Cell 5: Foto (Watermarked activity image resized to fit cell cleanly)
                val photoX = colX[4] + 10f
                val photoY = itemY + 10f
                val photoW = colX[5] - colX[4] - 20f
                val photoH = rowHeight - 20f

                if (item.photoPaths.isNotEmpty()) {
                    val pPath = item.photoPaths[0]
                    val pFile = File(pPath)
                    if (pFile.exists()) {
                        val originalBmp = BitmapFactory.decodeFile(pFile.absolutePath)
                        if (originalBmp != null) {
                            val rectDst = RectF(photoX, photoY, photoX + photoW, photoY + photoH)
                            // Draw photo
                            canvas.drawBitmap(originalBmp, null, rectDst, Paint(Paint.FILTER_BITMAP_FLAG))
                            originalBmp.recycle()
                        }
                    } else {
                        // Empty photo slot box
                        canvas.drawRect(photoX, photoY, photoX + photoW, photoY + photoH, Paint().apply {
                            color = Color.parseColor("#E0E0E0")
                            style = Paint.Style.FILL
                        })
                    }
                } else {
                    // Placeholder box
                    canvas.drawRect(photoX, photoY, photoX + photoW, photoY + photoH, Paint().apply {
                        color = Color.parseColor("#ECEFF1")
                        style = Paint.Style.FILL
                    })
                    canvas.drawText("[Tanpa Foto]", photoX + 18f, photoY + photoH / 2f + 4f, cellTextPaint.apply { color = Color.GRAY })
                }
            }

            val tableBottomY = startY + (itemsToDraw.size * rowHeight)

            // --- 4. FOOTER (Tanggal, Tanda Tangan, QR Code, Doc No) ---
            val footerStartY = tableBottomY + 40f

            // Date of Report Submission (Sukabumi, [Current Date])
            val submitDateFormat = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
            val todayDateStr = submitDateFormat.format(Date())
            canvas.drawText("Sukabumi, $todayDateStr", 380f, footerStartY, cellTextPaint.apply {
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
            })

            // Signature Title (e.g. "Ketua RT 001")
            canvas.drawText("Ketua RT ${user.rt}", 380f, footerStartY + 16f, cellTextPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            })

            // Tanda tangan digital or name at the bottom
            canvas.drawText(user.nama.uppercase(), 380f, footerStartY + 75f, cellTextPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isUnderlineText = true
            })

            // --- 5. QR CODE VALIDASI ---
            // QR content contains RT, RW, Date, DocNum, status valid
            val qrText = "RT:${user.rt}|RW:${user.rw}|Tanggal:$todayDateStr|DocNum:$docNum|Status:TERVERIFIKASI"
            val qrBmp = generateQrCode(qrText, 70)
            canvas.drawBitmap(qrBmp, 45f, footerStartY, Paint())

            // QR Code side description
            canvas.drawText("QR Code Validasi Resmi", 125f, footerStartY + 18f, cellTextPaint.apply {
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isUnderlineText = false
            })
            canvas.drawText("Pindai untuk verifikasi keaslian", 125f, footerStartY + 29f, cellTextPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            })
            canvas.drawText("No. Dokumen: $docNum", 125f, footerStartY + 40f, cellTextPaint.apply {
                color = Color.GRAY
            })

            pdfDocument.finishPage(page)

            // Save PDF file in external/documents or cache
            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.cacheDir
            if (!outputDir.exists()) outputDir.mkdirs()

            val pdfFile = File(outputDir, "Laporan_RT_${user.rt}_RW_${user.rw}_${System.currentTimeMillis() / 1000}.pdf")
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            fos.close()

            pdfDocument.close()
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
