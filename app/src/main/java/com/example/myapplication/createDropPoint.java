package com.example.myapplication;


import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Constraints;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class createDropPoint extends Fragment {
    TextView info;
    RequestQueue requestQueue;
    LinearLayout linearLayout;
    String baseUrl = "https://backend300cem.herokuapp.com/type/all";
    Map<String, Boolean> typeList = new HashMap<String, Boolean>();
    String typeId = "";
    Button submit;

    public createDropPoint() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_create_drop_point, container, false);
        requestQueue = Volley.newRequestQueue(v.getContext());
        linearLayout = v.findViewById(R.id.LinearLayout);
        info = v.findViewById(R.id.textView_CDP);
        submit = v.findViewById(R.id.button_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("List", "onClick: " + Arrays.asList(typeList).toString());
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
                    Snackbar.make(getView(), typeId, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                } else {
                    typeList.put(aSwitch.getTag().toString(), false);
                    Snackbar.make(getView(), typeId, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            }
        });
        linearLayout.addView(aSwitch);
    }

    private void getRESTfulAPI() {
        // Next, we create a new JsonArrayRequest. This will use Volley to make a HTTP request
        // that expects a JSON Array Response.
        JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, this.baseUrl,
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

    public String getLocale() {
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage() + "-" + locale.getCountry();
        return lang;
    }


}
