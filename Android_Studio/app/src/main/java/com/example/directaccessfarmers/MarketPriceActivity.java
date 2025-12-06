package com.example.directaccessfarmers;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.*;

public class MarketPriceActivity extends AppCompatActivity {

    private static final int VOICE_REQUEST_CODE = 1001;
    private static final int AUDIO_PERMISSION_CODE = 101;

    private EditText etCropSearch;
    private TextView tvMarketResult;
    private ProgressBar progressMarket;
    private Button btnFetchPrice;
    private ImageButton btnVoiceSearch;
    private Spinner spinnerLanguage;

    private TextToSpeech tts;
    private ProgressDialog listeningDialog;
    private final OkHttpClient client = new OkHttpClient();


    private static final String API_KEY = "579b464db66ec23bdd0000011e76f9cf794841a342352c109e64349c";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_price);

        etCropSearch = findViewById(R.id.etCropSearch);
        tvMarketResult = findViewById(R.id.tvMarketResult);
        progressMarket = findViewById(R.id.progressMarket);
        btnFetchPrice = findViewById(R.id.btnFetchPrice);
        btnVoiceSearch = findViewById(R.id.btnVoiceSearch);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_CODE
            );
        }


        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });


        String[] languages = {"English", "Kannada", "Hindi"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, languages
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        // Fetch button
        btnFetchPrice.setOnClickListener(v -> {
            String crop = etCropSearch.getText().toString().trim();
            if (crop.isEmpty()) {
                Toast.makeText(this, "Enter crop name", Toast.LENGTH_SHORT).show();
            } else {
                String normalized = normalizeCropName(crop);
                fetchRealMarketPrice(normalized);
            }
        });

        btnVoiceSearch.setOnClickListener(v -> startVoiceInput());

        listeningDialog = new ProgressDialog(this);
        listeningDialog.setMessage("🎤 Listening... Speak now");
        listeningDialog.setCancelable(false);
    }


    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the crop name...");

        try {
            listeningDialog.show();
            startActivityForResult(intent, VOICE_REQUEST_CODE);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Voice input not supported", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (listeningDialog.isShowing()) listeningDialog.dismiss();

        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (results != null && !results.isEmpty()) {
                String crop = results.get(0);
                etCropSearch.setText(crop);
                fetchRealMarketPrice(normalizeCropName(crop));
            }
        }
    }


    private String normalizeCropName(String crop) {

        crop = crop.toLowerCase().trim();

        switch (crop) {

            case "rice":
            case "paddy":
            case "akki":
                return "Paddy";

            case "groundnut":
            case "peanut":
            case "kadlekai":
                return "Groundnut Pods";

            case "chilli":
            case "green chilli":
            case "green chili":
            case "chili":
            case "menasinakai":
                return "Chili Green";

            case "tur dal":
            case "toor dal":
            case "arhar":
            case "togari":
                return "Redgram";

            case "urad":
            case "urad dal":
            case "uddina":
                return "Blackgram";

            case "maize":
            case "corn":
            case "jola":
                return "Maize";

            case "ragi":
            case "finger millet":
                return "Finger Millet";

            case "sugarcane":
            case "kabbu":
                return "Sugarcane";

            case "onion":
            case "eerulli":
                return "Onion";

            case "potato":
            case "alugadde":
                return "Potato";

            case "tomato":
            case "tommato":
                return "Tomato";

            default:
                return crop.substring(0, 1).toUpperCase() + crop.substring(1);
        }
    }


    private void fetchRealMarketPrice(String crop) {

        progressMarket.setVisibility(View.VISIBLE);
        tvMarketResult.setText("Fetching market price...");

        String url = "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070"
                + "?api-key=" + API_KEY
                + "&format=json"
                + "&filters[commodity]=" + crop
                + "&filters[state]=Karnataka";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressMarket.setVisibility(View.GONE);
                    tvMarketResult.setText("❌ Network Error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> {
                        progressMarket.setVisibility(View.GONE);
                        tvMarketResult.setText("❌ Failed to load data.");
                    });
                    return;
                }

                String json = response.body().string();

                try {
                    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                    JsonArray records = obj.getAsJsonArray("records");

                    if (records.size() == 0) {
                        runOnUiThread(() -> {
                            progressMarket.setVisibility(View.GONE);
                            tvMarketResult.setText("❗ No market price found for " + crop);
                        });
                        return;
                    }

                    JsonObject data = records.get(0).getAsJsonObject();

                    String market = data.get("market").getAsString();
                    String variety = data.get("variety").getAsString();
                    String modalPriceStr = data.get("modal_price").getAsString();

                    double modalPriceQuintal = Double.parseDouble(modalPriceStr);

                    // Convert per 100kg to per kg
                    double pricePerKg = modalPriceQuintal / 100.0;
                    String priceKgFormatted = String.format("%.2f", pricePerKg);

                    String output = "🌾 " + crop.toUpperCase() + "\n"
                            + "📍 Market: " + market + "\n"
                            + "🍃 Variety: " + variety + "\n"
                            + "💰 Price: ₹" + priceKgFormatted + " / kg";

                    runOnUiThread(() -> translateAndDisplay(output));

                } catch (Exception e) {
                    runOnUiThread(() ->
                            tvMarketResult.setText("⚠ Parsing error"));
                }
            }
        });
    }


    private void translateAndDisplay(String text) {

        String selectedLang = spinnerLanguage.getSelectedItem().toString();

        if (selectedLang.equals("English")) {
            progressMarket.setVisibility(View.GONE);
            tvMarketResult.setText(text);
            speakText(text, "en");
            return;
        }

        String langCode = selectedLang.equals("Kannada") ? "kn" : "hi";

        String url = "https://translate.googleapis.com/translate_a/single?client=gtx"
                + "&sl=en&tl=" + langCode + "&dt=t&q=" + text.replace(" ", "%20");

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        tvMarketResult.setText("Translation failed"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String json = response.body().string();

                try {
                    JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
                    JsonArray tArr = arr.get(0).getAsJsonArray();

                    StringBuilder trans = new StringBuilder();

                    for (int i = 0; i < tArr.size(); i++) {
                        trans.append(tArr.get(i).getAsJsonArray().get(0).getAsString());
                    }

                    runOnUiThread(() -> {
                        progressMarket.setVisibility(View.GONE);
                        tvMarketResult.setText(trans.toString());
                        speakText(trans.toString(), langCode);
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            tvMarketResult.setText("Translation error"));
                }
            }
        });
    }


    private void speakText(String text, String langCode) {

        Locale locale;

        switch (langCode) {
            case "kn":
                locale = new Locale("kn", "IN");
                break;
            case "hi":
                locale = new Locale("hi", "IN");
                break;
            default:
                locale = Locale.ENGLISH;
        }

        tts.setLanguage(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ID");
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
