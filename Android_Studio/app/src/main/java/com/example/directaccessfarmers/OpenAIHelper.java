package com.example.directaccessfarmers;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Simple OpenAI client using OkHttp + Gson.
 * Usage:
 *   OpenAIHelper helper = new OpenAIHelper(BuildConfig.OPENAI_API_KEY);
 *   helper.getAIResponse("Hello", new OpenAIHelper.AIResponseCallback() { ... });
 */
public class OpenAIHelper {
    private static final String TAG = "OpenAIHelper";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String apiKey;
    private final OkHttpClient client;
    private final Gson gson;

    public OpenAIHelper(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public interface AIResponseCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    /**
     * Sends a prompt to the OpenAI Chat Completions endpoint using gpt-3.5-turbo (or change model).
     * The callback is invoked on background thread; if you need UI updates, wrap calls in runOnUiThread.
     */
    public void getAIResponse(String prompt, AIResponseCallback callback) {
        try {
            // Build messages array with single user message
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);

            JsonArray messages = new JsonArray();
            messages.add(userMessage);

            // Build request payload
            JsonObject payload = new JsonObject();
            // Change model to "gpt-4o" or "gpt-4o-mini" if you have access and prefer it
            payload.addProperty("model", "gpt-3.5-turbo");
            payload.add("messages", messages);
            payload.addProperty("temperature", 0.7);

            String bodyString = gson.toJson(payload);
            RequestBody body = RequestBody.create(bodyString, JSON);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "OpenAI request failed", e);
                    if (callback != null) callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String msg = "OpenAI API error: " + response.code() + " " + response.message();
                        Log.e(TAG, msg);
                        if (callback != null) callback.onError(msg);
                        return;
                    }

                    String respStr = response.body() != null ? response.body().string() : "";
                    try {
                        JsonObject json = gson.fromJson(respStr, JsonObject.class);
                        if (json == null) {
                            if (callback != null) callback.onError("Empty OpenAI response");
                            return;
                        }
                        // Most responses include choices[].message.content
                        if (json.has("choices") && json.getAsJsonArray("choices").size() > 0) {
                            JsonObject first = json.getAsJsonArray("choices").get(0).getAsJsonObject();
                            // Chat completions: message.content
                            if (first.has("message") && first.get("message").getAsJsonObject().has("content")) {
                                String content = first.get("message").getAsJsonObject().get("content").getAsString();
                                if (callback != null) callback.onSuccess(content.trim());
                                return;
                            }
                            // fallback: text (older completions)
                            if (first.has("text")) {
                                String content = first.get("text").getAsString();
                                if (callback != null) callback.onSuccess(content.trim());
                                return;
                            }
                        }
                        // If we reach here, no usable content found
                        if (callback != null) callback.onError("OpenAI: unexpected response format");
                    } catch (Exception ex) {
                        Log.e(TAG, "Error parsing OpenAI response", ex);
                        if (callback != null) callback.onError("Parse error: " + ex.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "getAIResponse error", e);
            if (callback != null) callback.onError(e.getMessage());
        }
    }
}
