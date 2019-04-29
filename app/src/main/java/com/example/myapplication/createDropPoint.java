package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class createDropPoint extends Fragment {
    TextView info;
    RequestQueue requestQueue;
    LinearLayout linearLayout;

    String baseUrl = "https://backend300cem.herokuapp.com";
    String getType = "/type/all";
    String dropPoint = "/droppoint";

    Map<String, Boolean> typeList = new HashMap<>();
    String typeId = "";
    Button submit;
    EditText name;
    String Base64Img;
    String ItemId;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Log.d("imageBitmap", imageBitmap.toString());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            Base64Img = Base64.encodeToString(byteArray, Base64.DEFAULT);
            putImg(ItemId);
        }
    }

    public createDropPoint() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_create_drop_point, container, false);
        requestQueue = Volley.newRequestQueue(v.getContext());
        linearLayout = v.findViewById(R.id.LinearLayout);
        info = v.findViewById(R.id.textView_CDP);
        submit = v.findViewById(R.id.button_submit);
        name = v.findViewById(R.id.editText_CName);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("List", "onClick: " + Arrays.asList(typeList).toString());
                if (typeList.containsValue(true) && name.getText().toString().length() > 0) {
                    postRESTfulAPI();
                }
            }
        });
        getRESTfulAPI();
        return v;
    }

    private void addToList(JSONObject jsonObj) {
        String type = "";
        try {
            typeId = jsonObj.get("_id").toString();
            if (getLocale().equals("zh-HK")) {
                type = jsonObj.getJSONObject("type").get("chineseType").toString();
            } else {
                type = jsonObj.getJSONObject("type").get("engType").toString();
            }
        } catch (JSONException e) {
            // If there is an error then output this to the logs.
            Log.e("Volley", "Invalid JSON Object.");
        }

        final Switch aSwitch = new Switch(getContext());
        aSwitch.setText(type);
        aSwitch.setTag(typeId);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    typeList.put(aSwitch.getTag().toString(), true);
                } else {
                    typeList.put(aSwitch.getTag().toString(), false);
                }
            }
        });
        linearLayout.addView(aSwitch);
    }

    public String getLocale() {
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage() + "-" + locale.getCountry();
        return lang;
    }

    private void postRESTfulAPI() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl + dropPoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            Log.d("status", "onResponse: " + res);
                            if (res.get("status").equals("success")) {
                                ItemId = res.get("result").toString();
                                putType(ItemId);
                            } else {
                                Toast toast = Toast.makeText(getContext(), "Error: Invalid Input.", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        } catch (Throwable t) {
                            Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null) {
                            error.printStackTrace();
                        }
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                SharedPreferences pref = getActivity().getSharedPreferences("UserToken", MODE_PRIVATE);
                float Latitude = pref.getFloat("Latitude", 0);
                float Longitude = pref.getFloat("Longitude", 0);
                String postBy = pref.getString("User", null);
                Map<String, String> params = new HashMap<>();
                params.put("name", name.getText().toString());
                params.put("latitude", String.valueOf(Latitude));
                params.put("longitude", String.valueOf(Longitude));
                params.put("postBy", postBy);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                SharedPreferences pref = getActivity().getSharedPreferences("UserToken", MODE_PRIVATE);
                String token = pref.getString("Token", null);
                Log.d("Token", "getHeaders: " + token);
                params.put("x-access-token", token);
                Log.d("Params", params.get("x-access-token"));
                params.put("Content-Type", "application/x-www-form-urlencoded");

                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void getRESTfulAPI() {
        // Next, we create a new JsonArrayRequest. This will use Volley to make a HTTP request
        // that expects a JSON Array Response.
        JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, this.baseUrl + getType,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Check the length of our response
                        if (response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject jsonObj = response.getJSONObject(i);
                                    typeList.put(jsonObj.get("_id").toString(), false);
                                    addToList(jsonObj);
                                } catch (JSONException e) {
                                    // If there is an error then output this to the logs.
                                    Log.e("Volley", "Invalid JSON Object.");
                                }
                            }
                        } else {
                            Snackbar.make(getView(), "Type JSON Not found.", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // If there a HTTP error then add a note to our repo list.
                        Snackbar.make(getView(), "Error while calling REST API", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                        Log.e("Volley", error.toString());
                    }
                }
        );
        // Add the request we just defined to our request queue.
        // The request queue will automatically handle the request as soon as it can.
        requestQueue.add(arrReq);
    }

    private void putType(final String id) {
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, baseUrl + dropPoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject res = new JSONObject(response);
                            Log.d("status", "onResponse: " + res);
                            if (res.get("status").equals("success")) {
                                dispatchTakePictureIntent();

                            } else {
                                Toast toast = Toast.makeText(getContext(), "Error: Invalid Input.", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        } catch (Throwable t) {
                            Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null) {
                            error.printStackTrace();
                        }
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                String typeString = getTypeString(typeList);
                params.put("type", typeString);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                SharedPreferences pref = getActivity().getSharedPreferences("UserToken", MODE_PRIVATE);
                String token = pref.getString("Token", null);
                params.put("x-access-token", token);
                params.put("Content-Type", "application/x-www-form-urlencoded");

                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void putImg(final String id) {
        Log.d("Base64", "putImg: " + Base64Img);
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, baseUrl + dropPoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject res = new JSONObject(response);
                            Log.d("status", "onResponse: " + res);
                            if (res.get("status").equals("success")) {
                                droppointFragment cf = new droppointFragment();
                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                manager.beginTransaction().replace(R.id.mainLayout, cf).commit();
                            } else {
                                Toast toast = Toast.makeText(getContext(), "Error: Invalid Input.", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        } catch (Throwable t) {
                            Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null) {
                            error.printStackTrace();
                        }
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("img", Base64Img);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                SharedPreferences pref = getActivity().getSharedPreferences("UserToken", MODE_PRIVATE);
                String token = pref.getString("Token", null);
                params.put("x-access-token", token);
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public String getTypeString(Map<String, Boolean> typeList) {
        String TypeString = "";
        if (typeList.containsValue(true)) {
            // Iterate over each entry of map using entrySet
            Iterator iter = typeList.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object val = entry.getValue();
                if ((boolean) val == true) {
                    TypeString += key + ",";
                }
            }
            if (TypeString.charAt(TypeString.length() - 1) == ",".charAt(0)) {
                TypeString = TypeString.substring(0, TypeString.length() - 1);
            }

        }
        Log.d("TypeString", TypeString);
        return TypeString;
    }
}
