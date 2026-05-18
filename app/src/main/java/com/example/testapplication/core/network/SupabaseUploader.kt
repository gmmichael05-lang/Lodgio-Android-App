package com.example.testapplication.core.network

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Utility object for uploading images to Supabase Storage.
 * Uses the same bucket ("avatars") and URL patterns as the web app.
 */
object SupabaseUploader {

    private const val SUPABASE_URL = "https://dzigcwfyyfezvhdffprk.supabase.co"
    private const val BUCKET = "avatars"

    /**
     * Upload an image from a content:// URI to Supabase Storage.
     *
     * @param context     Android context for content resolver
     * @param imageUri    The gallery content:// URI
     * @param storagePath Path within the bucket
     * @param onSuccess   Callback with the public URL of the uploaded image
     * @param onError     Callback with error message
     */
    fun uploadImage(
        context: Context,
        imageUri: Uri,
        storagePath: String,
        onSuccess: (publicUrl: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        try {
            // Read the image bytes from the content URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return onError("Cannot read the selected image.")
            val imageBytes = inputStream.readBytes()
            inputStream.close()

            // Determine MIME type
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val mediaType = mimeType.toMediaTypeOrNull()
            val requestBody = imageBytes.toRequestBody(mediaType)

            // Upload to Supabase
            RetrofitClient.supabaseStorageApi.uploadFile(
                apiKey = RetrofitClient.SUPABASE_KEY,
                authorization = "Bearer ${RetrofitClient.SUPABASE_KEY}",
                contentType = mimeType,
                bucket = BUCKET,
                path = storagePath,
                body = requestBody
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val publicUrl = "$SUPABASE_URL/storage/v1/object/public/$BUCKET/$storagePath"
                        onSuccess(publicUrl)
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        onError("Upload failed (${response.code()}): $errorBody")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onError("Network error: ${t.message}")
                }
            })
        } catch (e: Exception) {
            onError("Error: ${e.message}")
        }
    }
}
