package com.example.currencyconverter;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Spinner spinner1,Spinner2;
    TextView result;
    EditText values;
    MaterialButton resultBtn;
    String url = "https://api.getgeoapi.com/v2/currency/list?api_key=7b864316812ba1309ac1e5509b598105b5c4f8f3&format=json";
    ArrayList<String> arrayList = new ArrayList<>();
    String convertFrom,convertTo, fromVal, toVal;
    String key;
    String value;
    RelativeLayout layout;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner1 = findViewById(R.id.Spinner1);
        Spinner2 = findViewById(R.id.Spinner2);
        result = findViewById(R.id.Result);
        values = findViewById(R.id.Value);
        resultBtn = findViewById(R.id.ConvertBtn);
        layout = findViewById(R.id.layout);

//        creates new request queue using volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);

//        creates string Request for getting result from api
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    JSONObject object2 = object.getJSONObject("currencies");

                    Log.d("Objects ",object2.toString());

                    JSONArray array2 = object2.names();
                    Set<String> stringSet = new HashSet<>();

                    for (int i = 0; i < array2.length(); i++) {
                        key = array2.getString(i);
                        value = object2.getString(key);
                        arrayList.add(key+"-"+value);
                    }

                    Log.d("List",arrayList.toString());
                    stringSet.addAll(arrayList);

                    SharedPreferences preferences = getSharedPreferences("Set",MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putStringSet("StringSet",stringSet);
                    editor.apply();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Failure", error.getLocalizedMessage());
            }
        });

        // adds request in request queue
        requestQueue.add(stringRequest);

        SharedPreferences preferences = getSharedPreferences("Set", MODE_PRIVATE);
        Set<String> newSet = new HashSet<>();
        newSet = preferences.getStringSet("StringSet", new HashSet<>());
        arrayList.addAll(newSet);
        arrayList.sort(Comparator.naturalOrder());

        Log.d("Set",newSet.toString());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, arrayList);
        spinner1.setAdapter(arrayAdapter);
        Spinner2.setAdapter(arrayAdapter);


        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                convertFrom = parent.getItemAtPosition(position).toString();
                fromVal = convertFrom.substring(0,3);
                Log.d("Convert From ",fromVal);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                convertTo = parent.getItemAtPosition(position).toString();
                toVal = convertTo.substring(0,3);
                Log.d("Convert To ",toVal);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        resultBtn.setOnClickListener(view->{
            float edtValue = Float.parseFloat(values.getText().toString());
            getConvertedCurrency(fromVal, toVal, edtValue);

        });
    }

    // method for getting converted currency value from api request
    public void getConvertedCurrency(String from, String to, float value){
        try {
            RequestQueue requestQueue2 = Volley.newRequestQueue(this);
            StringRequest stringRequest2 = new StringRequest(Request.Method.GET, "https://api.getgeoapi.com/v2/currency/convert?api_key=7b864316812ba1309ac1e5509b598105b5c4f8f3&from="+from+"&to="+to+"&amount="+value+"&format=json", new Response.Listener<String>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(String response) {
                    Log.d("String Request", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject ratesJson = jsonObject.getJSONObject("rates");
                        JSONObject currencyJson = ratesJson.getJSONObject(to);
                        double rateValue = currencyJson.getDouble("rate_for_amount");
                        String baseCurrency = jsonObject.getString("base_currency_name");
                        String currencyToConvert = currencyJson.getString("currency_name");


                        layout.setVisibility(View.VISIBLE);
                        result.setText(value + " " + baseCurrency + " = " + rateValue + " " + currencyToConvert);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Error", error.getLocalizedMessage());
                }
            });

            requestQueue2.add(stringRequest2);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}

// 22e91ab924eb2aa6f9a4
// https://api.getgeoapi.com/v2/currency/convert?api_key=7b864316812ba1309ac1e5509b598105b5c4f8f3&from=USD&to=INR&amount=1&format=json