package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * General utility to call the Gemini API raw JSON endpoint securely.
     */
    private suspend fun callGemini(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or placeholder!")
            return@withContext "Error: Gemini API Key is not configured. Please use the Secrets Panel in AI Studio."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

        try {
            // Build direct request payload using org.json (extremely safe and robust in Android)
            val partsArray = JSONArray().put(JSONObject().put("text", prompt))
            val contentsArray = JSONArray().put(JSONObject().put("parts", partsArray))
            
            val payload = JSONObject().apply {
                put("contents", contentsArray)
                if (systemInstruction != null) {
                    put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
                }
            }

            val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "HTTP Error response code: ${response.code}, body: $bodyString")
                    return@withContext "Error details: HTTP ${response.code}"
                }

                val jsonResponse = JSONObject(bodyString)
                val candidatesArray = jsonResponse.optJSONArray("candidates")
                if (candidatesArray != null && candidatesArray.length() > 0) {
                    val candidate = candidatesArray.getJSONObject(0)
                    val contentObj = candidate.optJSONObject("content")
                    val partsArr = contentObj?.optJSONArray("parts")
                    if (partsArr != null && partsArr.length() > 0) {
                        return@withContext partsArr.getJSONObject(0).optString("text", "No text parsed.")
                    }
                }
                "No AI response candidates received."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API Call Exception", e)
            "Error: ${e.message}"
        }
    }

    /**
     * Translates and Polishes a Crop listing automatically.
     */
    suspend fun translateAndPolishListing(title: String, description: String, category: String, isKhmer: Boolean): String {
        val instruction = "You are a professional agricultural marketing expert in Cambodia."
        val prompt = if (isKhmer) {
            "Please translate this Cambodian crop listing to professional English. Output ONLY the translated title, followed by a double line break, followed by the polished English description.\n\nOriginal Title: $title\nCategory: $category\nOriginal Description: $description"
        } else {
            "Please translate this Cambodian crop listing to professional Khmer. Output ONLY the translated title, followed by a double line break, followed by the polished Khmer description.\n\nOriginal Title: $title\nCategory: $category\nOriginal Description: $description"
        }
        return callGemini(prompt = prompt, systemInstruction = instruction)
    }

    /**
     * Analyzes price intelligence and regional market forecasts.
     */
    suspend fun generateMarketIntelligenceReport(category: String, currentPrice: String, province: String): String {
        val instruction = "You are FarmJumnoy Crop price intelligence adviser (Cambodia Agricultural Intelligence Bureau)."
        val prompt = """
            Create a professional 3-paragraph Market Advisory report for farmers in $province province regarding the crop category: $category.
            The current listed price area is around: $currentPrice.
            Include:
            1. Price trend summary (increasing, stable, or decreasing and why)
            2. High demand areas (traders, local buyers, or exporters in Phnom Penh, etc.)
            3. Operational recommendations for farmers harvesting this crop (e.g. optimal drying, storage, or negotiation strategies).
            Keep it actionable, friendly, and concise. Format with bullet points if helpful.
        """.trimIndent()
        return callGemini(prompt = prompt, systemInstruction = instruction)
    }

    /**
     * Analyzes active negotiations and suggests counter-tactics.
     */
    suspend fun suggestBargainTactics(cropTitle: String, listedPrice: Double, quantity: Double, unit: String, bidPrice: Double, bidDelivery: String, notes: String): String {
        val instruction = "You are an AI Agronomist and smart bargaining assistant for Cambodian crop traders."
        val prompt = """
            We are negotiating a crop deal.
            - Crop name: $cropTitle
            - Farmer's regular listed price: $$listedPrice per $unit
            - Total quantity: $quantity $unit
            - Buyer's offered bid price: $$bidPrice per $unit
            - Buyer's proposed delivery: $bidDelivery
            - Buyer's personal offer notes: "$notes"
            
            Identify whether the buyer is offering a fair price.
            Provide:
            1. Brief analysis of the offer (e.g., discount rate)
            2. Three smart counter-strategy suggestions in a friendly bullet list (e.g., offer free logistics if they match regular price, or counter-price at halfway). Keep it professional.
        """.trimIndent()
        return callGemini(prompt = prompt, systemInstruction = instruction)
    }
}
