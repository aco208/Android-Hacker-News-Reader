package nl.adokic.hackernewsreader;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final String URL_TOP = "https://hacker-news.firebaseio.com/v0/topstories.json";
    final String URL_start = "https://hacker-news.firebaseio.com/v0/item/";
    final String URL_end = ".json";
    ListView listview;
    TextView downloadView;
    ArrayAdapter<String> adapter;
    ArrayList<String> news_URL_list;
    ArrayList<String> news_title_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listview = (ListView) findViewById(R.id.listView);
        downloadView = (TextView) findViewById(R.id.downloadView);
        news_URL_list = new ArrayList<>();
        news_title_list = new ArrayList<>();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, news_title_list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news_URL_list.get(position)));
                startActivity(browserIntent);
            }
        });

        new DownloadTask().execute();
    }

    class DownloadTask extends AsyncTask<Void, String, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            String resArray = "";
            HttpURLConnection connection = null;
            InputStream in = null;
            InputStreamReader reader = null;

            try {
                URL urlIDS = new URL(URL_TOP);
                connection = (HttpURLConnection) urlIDS.openConnection();
                in = connection.getInputStream();
                reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1){
                    char c = (char) data;
                    resArray += c;
                    data = reader.read();
                }
                Log.i("Hackrnews", resArray);
                JSONArray jsonArray = new JSONArray(resArray);
                int index = 0;
                for (int i = 0; i < jsonArray.length(); i++){
                    String id = jsonArray.getString(i);
                    URL wholeURL = new URL(URL_start + id + URL_end);
                    connection = (HttpURLConnection) wholeURL.openConnection();
                    in = connection.getInputStream();
                    reader = new InputStreamReader(in);
                    String jsonObjectString = "";
                    data = reader.read();
                    while (data != -1){
                        char c = (char) data;
                        jsonObjectString += c;
                        data = reader.read();
                    }
                    JSONObject jsonObject = new JSONObject(jsonObjectString);
                    if (jsonObject.has("title") && jsonObject.has("url")){
                        news_title_list.add(jsonObject.getString("title"));
                        news_URL_list.add(jsonObject.getString("url"));
                        publishProgress("Downloaded: " + index);
                        index++;
                    }
                }

            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if (connection != null){
                    connection.disconnect();
                }
                if (in != null){
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String update = values[0];
            downloadView.setText(update);
            downloadView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
            downloadView.setVisibility(View.GONE);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

