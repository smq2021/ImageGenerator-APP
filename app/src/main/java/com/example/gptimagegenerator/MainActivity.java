package com.example.gptimagegenerator;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    EditText inputext;
    MaterialButton generateBtn;
    ImageView imageView;
    ProgressBar progressBar;
    public static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient();

    String prompt = "A futuristic cityscape at sunset";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        inputext = findViewById(R.id.input_text);
        generateBtn = findViewById(R.id.genereate_btn);
        imageView = findViewById(R.id.image_view);
        progressBar = findViewById(R.id.progress_circular);

        generateBtn.setOnClickListener((v) -> {
            String text = inputext.getText().toString().trim();
            if (text.isEmpty()) {
                inputext.setError("Prompt cannot be empty");
                return;
            }
            callApi(text);

        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }




    void callApi (String question){
        JSONObject jsonBody = new JSONObject();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // Connection timeout
                .readTimeout(30, TimeUnit.SECONDS)     // Read timeout
                .writeTimeout(30, TimeUnit.SECONDS)    // Write timeout
                .build();

        String url = "https://api.openai.com/v1/images/generations";


        try {
            jsonBody.put("prompt", question);
            jsonBody.put("n", 1);  // Number of images to generate
            jsonBody.put("size", "1024x1024");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(
                jsonBody.toString(), MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "API_KEY")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {



            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("OpenAIImageGenerator", "Request failed", e);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String responseData = response.body().string();

                String imageUrl = null;
                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    imageUrl = jsonResponse.getJSONArray("data")
                            .getJSONObject(0)
                            .getString("url");
                    loadImage(imageUrl);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Generated Image URL: " + imageUrl);
            }
        });



    }

    void loadImage(String url) {
        runOnUiThread(()->{
            Picasso.get().load(url).into(imageView);
        });


    }










}