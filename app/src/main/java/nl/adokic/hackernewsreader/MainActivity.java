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
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final String URL_NEW_STORIES = "https://hacker-news.firebaseio.com/v0/newstories";
    final String URL_ITEM = "https://hacker-news.firebaseio.com/v0/item/";

    //firebase
    Firebase ref;

    ListView listview;
    ArrayAdapter<String> adapter;
    ArrayList<String> news_URL_list;
    ArrayList<String> news_title_list;
    ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Firebase.setAndroidContext(this);


        listview = (ListView) findViewById(R.id.listView);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        ref = new Firebase(URL_NEW_STORIES);
        listener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                news_title_list.clear();
                news_URL_list.clear();
                for (DataSnapshot idSnapShot : dataSnapshot.getChildren()){
                    Long id = (Long) idSnapShot.getValue();
                    Firebase artikel = new Firebase(URL_ITEM + id);
                    artikel.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("title").exists() && dataSnapshot.child("url").exists()) {
                                String title = (String) dataSnapshot.child("title").getValue();
                                String url = (String) dataSnapshot.child("url").getValue();
                                news_title_list.add(title);
                                news_URL_list.add(url);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
                Toast.makeText(getApplicationContext(), "Lijst is ververst", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        ref.removeEventListener(listener);
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

