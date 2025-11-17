package com.example.runnity.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import androidx.exifinterface.media.ExifInterface
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * 파일 관련 유틸리티 함수
 */
object FileUtils {

    private const val MAX_FILE_SIZE_BYTES = 1 * 1024 * 1024 // 1MB - 이 크기 이하면 압축 안 함
    private const val MAX_IMAGE_DIMENSION = 1024 // 최대 이미지 가로/세로 크기
    private const val COMPRESSION_QUALITY = 85 // JPEG 압축 품질 (화질 유지)

    /**
     * Uri를 MultipartBody.Part로 변환 (필요시 이미지 압축)
     * @param context Context
     * @param uri 이미지 URI
     * @param paramName 파라미터 이름 (예: "profileImage")
     * @return MultipartBody.Part 또는 null
     */
    fun createMultipartFromUri(
        context: Context,
        uri: Uri,
        paramName: String
    ): MultipartBody.Part? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = getFileName(context, uri) ?: "image.jpg"

            // 1. 원본 파일 크기 확인
            val originalSize = getFileSize(context, uri)
            Timber.d("원본 파일 크기: ${originalSize / 1024}KB")

            // 2. 파일 크기가 1MB 이하면 압축 없이 그대로 사용
            if (originalSize <= MAX_FILE_SIZE_BYTES) {
                Timber.d("파일 크기가 충분히 작아 압축하지 않음")
                val inputStream = contentResolver.openInputStream(uri) ?: return null
                val tempFile = File(context.cacheDir, fileName)
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()

                val mimeType = contentResolver.getType(uri) ?: "image/*"
                val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                return MultipartBody.Part.createFormData(paramName, fileName, requestBody)
            }

            // 3. 1MB 초과 시 압축
            Timber.d("파일 크기가 커서 압축 시작")
            val compressedFile = compressImage(context, uri, "compressed_$fileName")

            if (compressedFile == null || !compressedFile.exists()) {
                Timber.e("이미지 압축 실패")
                return null
            }

            Timber.d("압축 완료: ${compressedFile.length() / 1024}KB")

            val requestBody = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(paramName, "compressed_$fileName", requestBody)
        } catch (e: Exception) {
            Timber.e(e, "URI를 MultipartBody.Part로 변환 실패")
            null
        }
    }

    /**
     * 파일 크기 가져오기
     */
    private fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        return@use cursor.getLong(sizeIndex)
                    }
                }
                0L
            } ?: 0L
        } catch (e: Exception) {
            Timber.e(e, "파일 크기 가져오기 실패")
            0L
        }
    }

    /**
     * 이미지 압축
     */
    private fun compressImage(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null

            // Bitmap으로 로드
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) {
                Timber.e("Bitmap 디코딩 실패")
                return null
            }

            // EXIF 정보로 회전 처리
            val rotatedBitmap = rotateImageIfRequired(context, uri, originalBitmap)

            // 해상도 조정 (1024x1024 이하로)
            val resizedBitmap = resizeBitmap(rotatedBitmap, MAX_IMAGE_DIMENSION)

            // 압축된 이미지 저장
            val compressedFile = File(context.cacheDir, fileName)
            var quality = COMPRESSION_QUALITY

            // 목표 크기 이하가 될 때까지 품질 조정 (최소 60까지)
            do {
                FileOutputStream(compressedFile).use { outputStream ->
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                }

                val fileSize = compressedFile.length()
                if (fileSize > MAX_FILE_SIZE_BYTES && quality > 60) {
                    quality -= 5
                    Timber.d("파일 크기 ${fileSize / 1024}KB, 품질을 ${quality}로 낮춤")
                } else {
                    break
                }
            } while (quality > 60)

            // Bitmap 메모리 해제
            if (rotatedBitmap != originalBitmap) {
                rotatedBitmap.recycle()
            }
            originalBitmap.recycle()
            resizedBitmap.recycle()

            compressedFile
        } catch (e: Exception) {
            Timber.e(e, "이미지 압축 중 오류")
            null
        }
    }

    /**
     * Bitmap 크기 조정
     */
    private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val scale = min(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * EXIF 정보에 따라 이미지 회전
     */
    private fun rotateImageIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            inputStream.close()

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            Timber.e(e, "EXIF 정보 읽기 실패")
            bitmap
        }
    }

    /**
     * Bitmap 회전
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Uri에서 파일명 추출
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}
