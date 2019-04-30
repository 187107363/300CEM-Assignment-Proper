package com.example.myapplication;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class listView_dropPoint extends Fragment {

    String baseUrl = "https://backend300cem.herokuapp.com";
    String getDropPoint = "/droppoint/all";
    RequestQueue requestQueue;
    ListView listView;
    private List<Map<String, Object>> datas = new ArrayList<>();
    SimpleAdapter simpleAdapter;
    public listView_dropPoint() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view_drop_point, container, false);
        // Inflate the layout for this fragment
        requestQueue = Volley.newRequestQueue(view.getContext());
        listView = view.findViewById(R.id.listView_dropPoint);

        getRESTfulAPI();
        Log.d("datas", String.valueOf(datas));
        return view;
    }

    private void getRESTfulAPI() {
        JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.GET, baseUrl + getDropPoint,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Check the length of our response
                            if (response.length() > 0) {
                                for (int i = 0; i < response.length(); i++) {
                                    try {
                                        JSONObject jsonObj = response.getJSONObject(i);
                                        String name = jsonObj.get("name").toString();
                                        String type = "";

                                        for (int j = 0; j < jsonObj.getJSONArray("recycleType").length(); j++) {
                                            if (getLocale().equals("zh-HK")) {
                                                type += jsonObj.getJSONArray("recycleType").getJSONObject(j).getJSONObject("type").getString("chineseType") + " ";
                                            } else {
                                                type += jsonObj.getJSONArray("recycleType").getJSONObject(j).getJSONObject("type").getString("engType") + " ";
                                            }
                                        }
                                        type = type.trim();
                                        Log.d("Volley", "type: " + type + ", name:" + name);
                                        String img = jsonObj.getJSONArray("img").get(0).toString();
                                        Log.d("Volley",  "img:" + img);

                                        byte[] decodedString = Base64.decode(img, Base64.DEFAULT);
                                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        Drawable image = new BitmapDrawable(getContext().getResources(), decodedByte);
                                        Map map = new HashMap();

                                        map.put("img", decodedByte);
                                        map.put("desc", name + "\n" + type);
                                        datas.add(map);
                                        Log.d("datas", String.valueOf(datas));
                                    } catch (JSONException e) {
                                        // If there is an error then output this to the logs.
                                        Log.e("Volley", "Invalid JSON Object: " + e);
                                    }
                                }
                                simpleAdapter = new SimpleAdapter(getActivity(),datas , R.layout.listview_droppoint, new String[]{"img", "desc"}, new int[]{R.id.img1, R.id.tv1});
                                simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                                    @Override
                                    public boolean setViewValue(View view, Object data,
                                                                String textRepresentation) {
                                        if( (view instanceof ImageView) & (data instanceof Bitmap) ) {
                                            ImageView iv = (ImageView) view;
                                            Bitmap bm = (Bitmap) data;
                                            iv.setImageBitmap(bm);
                                            return true;
                                        }
                                        return false;
                                    }
                                });
                                listView.setAdapter(simpleAdapter);
                            } else {
                                Snackbar.make(getView(), "Type JSON Not found.", Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            }
                        } catch (Throwable t) {
                            Log.e("Volley", "Could not parse malformed JSON: " + t);
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

    public String getLocale() {
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage() + "-" + locale.getCountry();
        return lang;
    }
}
