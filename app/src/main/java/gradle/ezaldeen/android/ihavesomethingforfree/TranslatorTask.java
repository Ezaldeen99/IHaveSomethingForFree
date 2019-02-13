package gradle.ezaldeen.android.ihavesomethingforfree;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class TranslatorTask extends AsyncTask<String, Void, String> {
    private WeakReference<MainActivity> mainActivity;
    private MainActivity.PostsViewHolder viewHolder;

    //Set Context
    TranslatorTask(MainActivity mainActivity, MainActivity.PostsViewHolder holder) {
        this.mainActivity = new WeakReference<>(mainActivity);
        viewHolder = holder;
    }

    @Override
    protected String doInBackground(String... params) {
        String text = params[0];
        String languagePair = params[1];

        String jsonString;

        try {
            String ApiKey = "trnsl.1.1.20181006T171749Z.2ddb91c5de12fc5d.d7ac67d14fe71caed75579d92a1e41783a3ff0b7";
            String url = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + ApiKey
                    + "&text=" + text + "&lang=" + languagePair;
            URL yandexURL = new URL(url);

            HttpURLConnection httpConnection = (HttpURLConnection) yandexURL.openConnection();
            InputStream inputStream = httpConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder jsonStringBuilder = new StringBuilder();
            while ((jsonString = bufferedReader.readLine()) != null) {
                jsonStringBuilder.append(jsonString).append("\n");
            }
            bufferedReader.close();
            inputStream.close();
            httpConnection.disconnect();

            String resultString = jsonStringBuilder.toString().trim();
            resultString = resultString.substring(resultString.indexOf('[') + 1);
            resultString = resultString.substring(0, resultString.indexOf("]"));
            resultString = resultString.substring(resultString.indexOf("\"") + 1);
            resultString = resultString.substring(0, resultString.indexOf("\""));

            Log.e("Translation Result:", resultString);
            return resultString;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (mainActivity.get() != null) {
            viewHolder.setDescription(s);
        }

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}