package com.varshuai.a1swaara.cloud

import android.content.Context
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

import com.varshuai.a1swaara.BuildConfig

/**
 * Core configuration and low-level REST client for A1 Swaara Cloud backend
 * utilizing Firebase Firestore on project raaga-music-8e1f2.
 */
object SwaaraCloudConfig {
    private const val TAG = "SwaaraCloudConfig"

    val PROJECT_ID: String get() = BuildConfig.SWAARA_CLOUD_PROJECT_ID.ifBlank { "raaga-music-8e1f2" }
    val API_KEY: String get() = BuildConfig.SWAARA_CLOUD_API_KEY
    val BASE_FIRESTORE_URL: String get() = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Converts a flat Kotlin Map to Firestore document fields format.
     */
    fun mapToFirestoreFields(data: Map<String, Any?>): JSONObject {
        val fields = JSONObject()
        for ((key, value) in data) {
            val converted = valueToFirestore(value)
            if (converted != null) {
                fields.put(key, converted)
            }
        }
        return JSONObject().put("fields", fields)
    }

    private fun valueToFirestore(value: Any?): JSONObject? {
        if (value == null) return null
        val obj = JSONObject()
        when (value) {
            is String -> obj.put("stringValue", value)
            is Boolean -> obj.put("booleanValue", value)
            is Int, is Long, is Short, is Byte -> obj.put("integerValue", value.toString())
            is Float, is Double -> obj.put("doubleValue", (value as Number).toDouble())
            is List<*> -> {
                val arrayValues = JSONArray()
                for (item in value) {
                    val convertedItem = valueToFirestore(item)
                    if (convertedItem != null) {
                        arrayValues.put(convertedItem)
                    }
                }
                obj.put("arrayValue", JSONObject().put("values", arrayValues))
            }
            is Map<*, *> -> {
                val mapFields = JSONObject()
                for ((k, v) in value) {
                    if (k is String) {
                        val convertedV = valueToFirestore(v)
                        if (convertedV != null) {
                            mapFields.put(k, convertedV)
                        }
                    }
                }
                obj.put("mapValue", JSONObject().put("fields", mapFields))
            }
            else -> obj.put("stringValue", value.toString())
        }
        return obj
    }

    /**
     * Converts Firestore document fields back to a Kotlin Map.
     */
    fun firestoreFieldsToMap(fieldsObj: JSONObject): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        val keys = fieldsObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val valObj = fieldsObj.optJSONObject(key)
            if (valObj != null) {
                result[key] = firestoreValueToAny(valObj)
            }
        }
        return result
    }

    private fun firestoreValueToAny(valObj: JSONObject): Any? {
        return when {
            valObj.has("stringValue") -> valObj.optString("stringValue")
            valObj.has("booleanValue") -> valObj.optBoolean("booleanValue")
            valObj.has("integerValue") -> valObj.optString("integerValue").toLongOrNull() ?: 0L
            valObj.has("doubleValue") -> valObj.optDouble("doubleValue")
            valObj.has("arrayValue") -> {
                val arrayObj = valObj.optJSONObject("arrayValue")
                val values = arrayObj?.optJSONArray("values")
                val list = mutableListOf<Any?>()
                if (values != null) {
                    for (i in 0 until values.length()) {
                        val itemObj = values.optJSONObject(i)
                        if (itemObj != null) {
                            list.add(firestoreValueToAny(itemObj))
                        }
                    }
                }
                list
            }
            valObj.has("mapValue") -> {
                val mapObj = valObj.optJSONObject("mapValue")
                val fields = mapObj?.optJSONObject("fields")
                if (fields != null) firestoreFieldsToMap(fields) else emptyMap<String, Any?>()
            }
            else -> null
        }
    }

    /**
     * Fetches a single Firestore document by path.
     */
    fun getDocument(documentPath: String): Map<String, Any?>? {
        val cleanPath = documentPath.trimStart('/')
        val url = "$BASE_FIRESTORE_URL/$cleanPath?key=$API_KEY"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    if (response.code != 404) {
                        Log.w(TAG, "getDocument $cleanPath failed with code ${response.code}")
                    }
                    return null
                }
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                val fields = json.optJSONObject("fields") ?: return emptyMap()
                firestoreFieldsToMap(fields)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching document $cleanPath", e)
            null
        }
    }

    /**
     * Creates or updates a Firestore document using PATCH.
     */
    fun setDocument(documentPath: String, data: Map<String, Any?>): Boolean {
        val cleanPath = documentPath.trimStart('/')
        val url = "$BASE_FIRESTORE_URL/$cleanPath?key=$API_KEY"
        val payload = mapToFirestoreFields(data).toString()

        val request = Request.Builder()
            .url(url)
            .patch(payload.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "setDocument $cleanPath failed with code ${response.code}: ${response.body?.string()}")
                    false
                } else {
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing document $cleanPath", e)
            false
        }
    }

    /**
     * Deletes a Firestore document.
     */
    fun deleteDocument(documentPath: String): Boolean {
        val cleanPath = documentPath.trimStart('/')
        val url = "$BASE_FIRESTORE_URL/$cleanPath?key=$API_KEY"
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                response.isSuccessful || response.code == 404
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting document $cleanPath", e)
            false
        }
    }

    /**
     * Lists documents in a collection.
     */
    fun listDocuments(collectionPath: String, pageSize: Int = 30): List<Pair<String, Map<String, Any?>>> {
        val cleanPath = collectionPath.trimStart('/')
        val url = "$BASE_FIRESTORE_URL/$cleanPath?pageSize=$pageSize&key=$API_KEY"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val list = mutableListOf<Pair<String, Map<String, Any?>>>()
        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val json = JSONObject(body)
                val documents = json.optJSONArray("documents") ?: return emptyList()
                for (i in 0 until documents.length()) {
                    val doc = documents.optJSONObject(i) ?: continue
                    val docName = doc.optString("name", "")
                    val docId = docName.substringAfterLast('/')
                    val fields = doc.optJSONObject("fields") ?: JSONObject()
                    list.add(docId to firestoreFieldsToMap(fields))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing collection $cleanPath", e)
        }
        return list
    }
}
