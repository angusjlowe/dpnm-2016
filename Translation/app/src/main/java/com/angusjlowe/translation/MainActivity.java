package com.angusjlowe.translation;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    EditText translateEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onTranslateClick(View v) {
        EditText translateEditText = (EditText) findViewById(R.id.translationEditText);
        if(!isEmpty(translateEditText)) {
            Toast.makeText(this, "Getting translations", Toast.LENGTH_LONG).show();
            new SavetheFeed().execute();
        }
        else {
            Toast.makeText(this, "Enter words to translate", Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    class SavetheFeed extends AsyncTask<Void, Void, Void> {
        String jsonString = "";
        String result = "";
        EditText translateEditText = (EditText) findViewById(R.id.translationEditText);
        String wordsToTranslate = translateEditText.getText().toString();


        @Override
        protected Void doInBackground(Void... voids) {
            wordsToTranslate = wordsToTranslate.replace(" ", "+");
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost("http://newjustin.com/translateit.php?action=translations&english_words="+ wordsToTranslate);
            httpPost.setHeader("Content-type", "application/json");
            InputStream inputStream = null;
            try {
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                jsonString = sb.toString();

                JSONObject jObject = new JSONObject(jsonString);
                JSONArray jArray = jObject.getJSONArray("translations");

                outPutTranslations(jArray);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            TextView translationTextView = (TextView) findViewById(R.id.translationTextView);
            translationTextView.setText(result);
        }

        protected void outPutTranslations(JSONArray jsonArray) {
            String[] languages = {"arabic", "chinese", "danish", "dutch", "french", "german",
                    "italian", "portuguese", "russian", "spanish"};
            try{
                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject translationObject = jsonArray.getJSONObject(i);
                    result = result + languages[i] + " : " + translationObject.getString(languages[i]) + "\n";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
