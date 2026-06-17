package com.cardsnap.domain.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import java.io.File
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

object ImageCropper {
    private const val TAG = "ImageCropper"

    private const val CARD_ASPECT_RATIO = 1.75f
    private const val EDGE_DETECT_SIZE = 400

    fun cropToCardGuide(bitmap: Bitmap): Bitmap {
        val perspectiveResult = detectAndCorrectPerspective(bitmap)
        if (perspectiveResult != null) {
            Log.d(TAG, "Perspective correction applied successfully")
            return perspectiveResult
        }
        Log.d(TAG, "Edge detection failed, falling back to center crop")
        return centerCropToCardAspect(bitmap)
    }

    private fun centerCropToCardAspect(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val cropHeight = (min(width, height) * 0.85f).toInt()
        val cropWidth = (cropHeight * CARD_ASPECT_RATIO).toInt()
        val cropX = ((width - cropWidth) / 2).coerceAtLeast(0)
        val cropY = ((height - cropHeight) / 2).coerceAtLeast(0)
        val actualCropWidth = cropWidth.coerceAtMost(width - cropX)
        val actualCropHeight = cropHeight.coerceAtMost(height - cropY)
        return Bitmap.createBitmap(bitmap, cropX, cropY, actualCropWidth, actualCropHeight)
    }

    private fun detectAndCorrectPerspective(bitmap: Bitmap): Bitmap? {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 60 || h < 60) return null

        // ── 1. Downscale for edge-detection performance ──
        val scale = min(1f, EDGE_DETECT_SIZE.toFloat() / min(w, h))
        val sw = (w * scale).toInt().coerceAtLeast(60)
        val sh = (h * scale).toInt().coerceAtLeast(60)

        val scaled = Bitmap.createScaledBitmap(bitmap, sw, sh, true)
        val pixels = IntArray(sw * sh)
        scaled.getPixels(pixels, 0, sw, 0, 0, sw, sh)
        scaled.recycle()

        // ── 2. Convert to grayscale ──
        val gray = IntArray(sw * sh) { idx ->
            val p = pixels[idx]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)
        }

        // ── 3. Sobel edge detection ──
        val mag = FloatArray(sw * sh)
        var maxMag = 0f

        for (y in 1 until sh - 1) {
            for (x in 1 until sw - 1) {
                val idx = y * sw + x
                val gx = (-1 * gray[idx - sw - 1] + 1 * gray[idx - sw + 1]
                        - 2 * gray[idx - 1] + 2 * gray[idx + 1]
                        - 1 * gray[idx + sw - 1] + 1 * gray[idx + sw + 1]).toFloat()
                val gy = (-1 * gray[idx - sw - 1] - 2 * gray[idx - sw] - 1 * gray[idx - sw + 1]
                        + 1 * gray[idx + sw - 1] + 2 * gray[idx + sw] + 1 * gray[idx + sw + 1]).toFloat()
                val m = sqrt(gx * gx + gy * gy)
                mag[idx] = m
                if (m > maxMag) maxMag = m
            }
        }

        if (maxMag < 1f) {
            Log.d(TAG, "No significant edges detected")
            return null
        }

        val threshold = maxMag * 0.12f
        for (i in mag.indices) {
            if (mag[i] < threshold) mag[i] = 0f
        }

        // ── 4. Scan from each side for card-boundary edge points ──
        val topPoints = mutableListOf<Pair<Int, Int>>()
        val bottomPoints = mutableListOf<Pair<Int, Int>>()
        val leftPoints = mutableListOf<Pair<Int, Int>>()
        val rightPoints = mutableListOf<Pair<Int, Int>>()

        val scanStep = (sw / 40).coerceIn(1, 6)

        // Top edge: scan each column from top toward centre
        for (x in 0 until sw step scanStep) {
            for (y in 0 until sh / 2) {
                if (mag[y * sw + x] > 0f) {
                    topPoints.add(Pair(x, y))
                    break
                }
            }
        }

        // Bottom edge: scan each column from bottom toward centre
        for (x in 0 until sw step scanStep) {
            for (y in (sh - 1) downTo sh / 2) {
                if (mag[y * sw + x] > 0f) {
                    bottomPoints.add(Pair(x, y))
                    break
                }
            }
        }

        // Left edge: scan each row from left toward centre
        for (y in 0 until sh step scanStep) {
            for (x in 0 until sw / 2) {
                if (mag[y * sw + x] > 0f) {
                    leftPoints.add(Pair(x, y))
                    break
                }
            }
        }

        // Right edge: scan each row from right toward centre
        for (y in 0 until sh step scanStep) {
            for (x in (sw - 1) downTo sw / 2) {
                if (mag[y * sw + x] > 0f) {
                    rightPoints.add(Pair(x, y))
                    break
                }
            }
        }

        Log.d(TAG, "Edge-point counts  top=${topPoints.size}  bottom=${bottomPoints.size}  " +
                "left=${leftPoints.size}  right=${rightPoints.size}")

        if (topPoints.size < 3 || bottomPoints.size < 3 ||
            leftPoints.size < 3 || rightPoints.size < 3
        ) {
            Log.d(TAG, "Insufficient edge points to fit lines")
            return null
        }

        // ── 5. Fit lines through edge points ──
        val topLine = fitLineY(topPoints) ?: return null
        val bottomLine = fitLineY(bottomPoints) ?: return null
        val leftLine = fitLineX(leftPoints) ?: return null
        val rightLine = fitLineX(rightPoints) ?: return null

        // ── 6. Compute quadrilateral corners ──
        val topLeft = intersectLines(topLine, leftLine) ?: return null
        val topRight = intersectLines(topLine, rightLine) ?: return null
        val bottomRight = intersectLines(bottomLine, rightLine) ?: return null
        val bottomLeft = intersectLines(bottomLine, leftLine) ?: return null

        val corners = listOf(topLeft, topRight, bottomRight, bottomLeft)

        if (!isValidCardQuad(corners, sw, sh)) {
            Log.d(TAG, "Quadrilateral validation failed")
            return null
        }

        Log.d(TAG, "Detected card corners (scaled)  $corners")

        // ── 7. Map corners back to original bitmap coordinates ──
        val srcPoints = floatArrayOf(
            topLeft.first / scale, topLeft.second / scale,
            topRight.first / scale, topRight.second / scale,
            bottomRight.first / scale, bottomRight.second / scale,
            bottomLeft.first / scale, bottomLeft.second / scale,
        )

        // ── 8. Apply perspective warp ──
        return warpPerspective(bitmap, srcPoints)
    }

    private data class Line(
        val slope: Double,
        val intercept: Double,
        val horizontal: Boolean,
    )

    private fun fitLineY(points: List<Pair<Int, Int>>): Line? {
        val n = points.size
        if (n < 2) return null
        var sumX = 0.0
        var sumY = 0.0
        var sumXX = 0.0
        var sumXY = 0.0
        for ((x, y) in points) {
            sumX += x.toDouble()
            sumY += y.toDouble()
            sumXX += x.toDouble() * x.toDouble()
            sumXY += x.toDouble() * y.toDouble()
        }
        val denom = n * sumXX - sumX * sumX
        if (abs(denom) < 1e-10) return null
        val slope = (n * sumXY - sumX * sumY) / denom
        val intercept = (sumY - slope * sumX) / n
        return Line(slope, intercept, horizontal = true)
    }

    private fun fitLineX(points: List<Pair<Int, Int>>): Line? {
        val n = points.size
        if (n < 2) return null
        var sumY = 0.0
        var sumX = 0.0
        var sumYY = 0.0
        var sumXY = 0.0
        for ((x, y) in points) {
            sumY += y.toDouble()
            sumX += x.toDouble()
            sumYY += y.toDouble() * y.toDouble()
            sumXY += x.toDouble() * y.toDouble()
        }
        val denom = n * sumYY - sumY * sumY
        if (abs(denom) < 1e-10) return null
        val slope = (n * sumXY - sumY * sumX) / denom
        val intercept = (sumX - slope * sumY) / n
        return Line(slope, intercept, horizontal = false)
    }

    private fun intersectLines(hLine: Line, vLine: Line): Pair<Float, Float>? {
        val denom = 1.0 - vLine.slope * hLine.slope
        if (abs(denom) < 1e-6) return null
        val x = (vLine.slope * hLine.intercept + vLine.intercept) / denom
        val y = hLine.slope * x + hLine.intercept
        return Pair(x.toFloat(), y.toFloat())
    }

    private fun isValidCardQuad(
        corners: List<Pair<Float, Float>>,
        imageW: Int,
        imageH: Int,
    ): Boolean {
        if (corners.any { (x, y) -> x.isNaN() || y.isNaN() || x.isInfinite() || y.isInfinite() }) {
            return false
        }

        val area = polygonArea(corners)
        val imageArea = (imageW * imageH).toFloat()
        if (area < imageArea * 0.05f) return false

        val edges = (0 until 4).map { i ->
            val (x1, y1) = corners[i]
            val (x2, y2) = corners[(i + 1) % 4]
            sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
        }

        val avgWidth = (edges[0] + edges[2]) / 2f
        val avgHeight = (edges[1] + edges[3]) / 2f
        if (avgHeight < 1f) return false

        val aspect = avgWidth / avgHeight
        if (aspect < 0.8f || aspect > 3.0f) {
            Log.d(TAG, "Quad aspect $aspect outside [0.8, 3.0]")
            return false
        }

        return true
    }

    private fun polygonArea(corners: List<Pair<Float, Float>>): Float {
        var area = 0f
        for (i in corners.indices) {
            val (x1, y1) = corners[i]
            val (x2, y2) = corners[(i + 1) % corners.size]
            area += x1 * y2 - x2 * y1
        }
        return abs(area) / 2f
    }

    private fun warpPerspective(bitmap: Bitmap, srcPoints: FloatArray): Bitmap? {
        val p0x = srcPoints[0]; val p0y = srcPoints[1]
        val p1x = srcPoints[2]; val p1y = srcPoints[3]
        val p2x = srcPoints[4]; val p2y = srcPoints[5]
        val p3x = srcPoints[6]; val p3y = srcPoints[7]

        val topW = sqrt((p1x - p0x) * (p1x - p0x) + (p1y - p0y) * (p1y - p0y))
        val bottomW = sqrt((p2x - p3x) * (p2x - p3x) + (p2y - p3y) * (p2y - p3y))
        val leftH = sqrt((p3x - p0x) * (p3x - p0x) + (p3y - p0y) * (p3y - p0y))
        val rightH = sqrt((p2x - p1x) * (p2x - p1x) + (p2y - p1y) * (p2y - p1y))

        val cardW = maxOf(topW, bottomW).toInt().coerceAtLeast(10)
        val cardH = maxOf(leftH, rightH).toInt().coerceAtLeast(10)

        val (outW, outH) = fitToCardAspect(cardW, cardH)

        val dstPoints = floatArrayOf(
            0f, 0f,
            outW.toFloat(), 0f,
            outW.toFloat(), outH.toFloat(),
            0f, outH.toFloat(),
        )

        val matrix = Matrix()
        if (!matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)) {
            Log.d(TAG, "setPolyToPoly returned false")
            return null
        }

        return try {
            val result = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            canvas.concat(matrix)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            result
        } catch (e: Exception) {
            Log.d(TAG, "Perspective warp failed: ${e.message}")
            null
        }
    }

    private fun fitToCardAspect(detectedW: Int, detectedH: Int): Pair<Int, Int> {
        val detectedAspect = detectedW.toFloat() / detectedH.toFloat()
        return if (abs(detectedAspect - CARD_ASPECT_RATIO) < 0.5f) {
            Pair(detectedW, detectedH)
        } else if (detectedAspect > CARD_ASPECT_RATIO) {
            val h = (detectedW / CARD_ASPECT_RATIO).toInt()
            Pair(detectedW, h)
        } else {
            val w = (detectedH * CARD_ASPECT_RATIO).toInt()
            Pair(w, detectedH)
        }
    }

    fun decodeBitmapWithRotation(uri: String): Bitmap? {
        val path = uri.removePrefix("file://")
        val file = File(path)
        if (!file.exists()) return null
        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(this, path, 1200)
        }
        val bitmap = BitmapFactory.decodeFile(path, options) ?: return null
        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        return if (rotation != 0f) {
            val matrix = Matrix()
            matrix.postRotate(rotation)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        path: String,
        targetSize: Int,
    ): Int {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var inSampleSize = 1
        if (bounds.outHeight > targetSize || bounds.outWidth > targetSize) {
            val halfHeight = bounds.outHeight / 2
            val halfWidth = bounds.outWidth / 2
            while ((halfHeight / inSampleSize) >= targetSize &&
                (halfWidth / inSampleSize) >= targetSize
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
