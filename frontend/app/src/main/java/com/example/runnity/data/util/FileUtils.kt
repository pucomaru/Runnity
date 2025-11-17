package com.example.runnity.data.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * 파일 관련 유틸리티 함수
 */
object FileUtils {

    /**
     * Uri를 MultipartBody.Part로 변환
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
            // ContentResolver로 파일 정보 가져오기
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null

            // 파일명 가져오기
            val fileName = getFileName(context, uri) ?: "image.jpg"

            // 임시 파일 생성
            val tempFile = File(context.cacheDir, fileName)
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            // MIME 타입 결정
            val mimeType = contentResolver.getType(uri) ?: "image/*"

            // MultipartBody.Part 생성
            val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(paramName, fileName, requestBody)
        } catch (e: Exception) {
            Timber.e(e, "URI를 MultipartBody.Part로 변환 실패")
            null
        }
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
