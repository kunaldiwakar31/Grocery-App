package com.application.groceryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class FilterActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText searchText;
    private ImageButton searchButton;
    private GroceryAdapter groceryAdapter;
    private RecyclerView recyclerView;
    private List<GroceryDetails> groceryDetails;
    private LinearLayoutManager linearLayoutManager;
    private ProgressBar progressBar;
    private ProgressBar progressBar2;
    private Boolean isScrolling=false;
    int currentItems,totalItems,scrolledItems;
    int offset=20;
    private String text=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        FilterActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        toolbar = findViewById(R.id.BarLayout);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Grocery App");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar=findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        progressBar2=findViewById(R.id.progressBar3);
        progressBar2.setVisibility(View.GONE);

        recyclerView=findViewById(R.id.filterRecyclerView);
        groceryDetails=new ArrayList<>();
        linearLayoutManager=new LinearLayoutManager(this);
        groceryAdapter=new GroceryAdapter(FilterActivity.this,groceryDetails);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(groceryAdapter);

        searchText =findViewById(R.id.searchByDistrict);
        searchButton=findViewById(R.id.searchButton);

        searchText.requestFocus();
        try {
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressBar2.setVisibility(View.VISIBLE);
                    text = String.valueOf(searchText.getText());
                    offset=20;
                    DownloadTask task = new DownloadTask();
                    task.execute("https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070?api-key=579b464db66ec23bdd0000013f720af3e2ed4a866bc5aa28a1fd6225&format=json&offset=0&limit=20&filters[district]=" + text);
                }
            });

            loadData();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    public class DownloadTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {

            URL url;
            HttpURLConnection urlConnection=null;
            String result="";
            try {
                url=new URL(urls[0]);
                urlConnection=(HttpURLConnection) url.openConnection();
                InputStream inputStream=urlConnection.getInputStream();
                InputStreamReader reader=new InputStreamReader(inputStream);
                int data =reader.read();
                while(data!=-1) {
                    char current=(char) data;
                    result +=current;
                    data=reader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s!=null){
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String records="";
                    records=jsonObject.getString("records");
                    JSONArray arr=new JSONArray(records);
                    if(!groceryDetails.isEmpty() && offset==20){
                        groceryDetails.clear();
                    }
                    for(int i=0;i<arr.length();i++) {
                        JSONObject part=arr.getJSONObject(i);
                        GroceryDetails details=new GroceryDetails();
                        details.setGroceryName(part.getString("commodity"));
                        details.setGroceryPlace(part.getString("district")+","+part.getString("state"));
                        details.setGroceryPrice(part.getString("modal_price"));
                        details.setGroceryTime(Long.valueOf(part.getString("timestamp")));
                        //Log.i("INFO",District+" "+Commodity+" "+Modal_Price);
                        groceryDetails.add(details);
                    }
                    groceryAdapter =new GroceryAdapter(FilterActivity.this,groceryDetails);
                    groceryAdapter.notifyDataSetChanged();

                    recyclerView.setAdapter(groceryAdapter);
                    if(totalItems==(currentItems+scrolledItems)){
                        recyclerView.scrollToPosition(totalItems-currentItems+1);
                    }
                    progressBar.setVisibility(View.GONE);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else{
                Toast.makeText(FilterActivity.this,"Try Again!!",Toast.LENGTH_SHORT).show();
            }
            progressBar2.setVisibility(View.GONE);
            if(groceryDetails.isEmpty()){
                Toast.makeText(FilterActivity.this,"Data not Found!! Please Try Again and Remember to Keep first Letter Capital",Toast.LENGTH_LONG).show();
            }
        }

    }

    private void loadData() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling=true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentItems=linearLayoutManager.getChildCount();
                totalItems=linearLayoutManager.getItemCount();
                scrolledItems=linearLayoutManager.findFirstVisibleItemPosition();

                if(isScrolling && (currentItems+scrolledItems)==totalItems){
                    //data fetch
                    isScrolling=false;
                    progressBar.setVisibility(View.VISIBLE);
                    fetchData();
                }
            }
        });
    }

    private void fetchData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                offset+=20;
                DownloadTask task = new DownloadTask();
                task.execute("https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070?api-key=579b464db66ec23bdd0000013f720af3e2ed4a866bc5aa28a1fd6225&format=json&offset="+offset+"&limit=20&filters[district]="+text);
            }
        },5000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.filtermenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.sortByPrice){
            if(groceryDetails.isEmpty()){
                Toast.makeText(FilterActivity.this,"No data to sort!!",Toast.LENGTH_SHORT).show();
            }else{
                Collections.sort(groceryDetails, new Comparator<GroceryDetails>() {
                    public int compare(GroceryDetails g1,GroceryDetails g2){
                        return Integer.valueOf(g1.getGroceryPrice()).compareTo(Integer.valueOf(g2.getGroceryPrice()));
                    }
                });
                groceryAdapter.notifyDataSetChanged();
            }
        }
        else if(item.getItemId()==R.id.sortByDate){
            if(groceryDetails.isEmpty()){
                Toast.makeText(FilterActivity.this,"No data to sort!!",Toast.LENGTH_SHORT).show();
            }else{
                Collections.sort(groceryDetails, new Comparator<GroceryDetails>() {
                    public int compare(GroceryDetails g1,GroceryDetails g2){
                        return Integer.compare(Math.toIntExact(g1.getGroceryTime()), Math.toIntExact(g2.getGroceryTime()));
                    }
                });
                groceryAdapter.notifyDataSetChanged();
            }
        }
        else if(item.getItemId()==android.R.id.home){
            finish();
        }
        return true;
    }
}