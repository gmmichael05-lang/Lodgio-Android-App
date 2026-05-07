package com.example.testapplication.core.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Retrofit interface for Supabase Storage REST API.
 * Uploads files directly to Supabase Storage buckets.
 *
 * Bucket: "avatars" — used for both profile pictures and listing images
 * (matching the web app's bucket usage).
 */
interface SupabaseStorageApi {

    /**
     * Upload a file to a Supabase Storage bucket.
     *
     * @param apiKey    The Supabase anon key (passed as both apikey header and Bearer token)
     * @param bucket    The storage bucket name (e.g., "avatars")
     * @param path      The file path within the bucket (e.g., "listing-images/userId/timestamp.jpg")
     * @param contentType  MIME type of the file (e.g., "image/jpeg")
     * @param body      The raw file bytes as a RequestBody
     */
    @POST("storage/v1/object/{bucket}/{path}")
    fun uploadFile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String,
        @Header("x-upsert") upsert: String = "true",
        @Path("bucket") bucket: String,
        @Path("path", encoded = true) path: String,
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * Get the public URL for a file.
     * This is a simple string construction — no API call needed.
     * Provided here as a utility reference:
     *   https://{project}.supabase.co/storage/v1/object/public/{bucket}/{path}
     */
}
