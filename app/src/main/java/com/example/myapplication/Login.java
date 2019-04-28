package com.example.myapplication;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class Login extends Fragment {
    RequestQueue requestQueue;
    Button button;
    EditText email;
    EditText password;
    String baseUrl = "https://backend300cem.herokuapp.com/users/authenticate";

    public Login() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        requestQueue = Volley.newRequestQueue(view.getContext());
        button = (Button) view.findViewById(R.id.button_login_submit);
        email = (EditText) view.findViewById(R.id.editText_email);
        password = (EditText) view.findViewById(R.id.editText_password);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (email.getText().toString().length() > 0 && password.getText().toString().length() > 0) {

                    Log.d("Login", email.getText().toString() + ", " + password.getText().toString());
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject res = new JSONObject(response);
                                        Log.d("status", "onResponse: " + res.get("status"));
                                        if (res.get("status").equals("success")){
                                            SharedPreferences pref = getActivity().getSharedPreferences("UserToken", MODE_PRIVATE);
                                            pref.edit()
                                                    .putString("Token", res.getJSONObject("data").getString("token"))
                                                    .putString("User",res.getJSONObject("data").getJSONObject("user").getString("_id"))
                                                    .commit();

                                            droppointFragment droppointfragment = new droppointFragment();
                                            FragmentManager manager = getFragmentManager();
                                            manager.beginTransaction().replace(R.id.mainLayout, droppointfragment).commit();
                                        }else{
                                            Toast toast = Toast.makeText(getContext(),"Error: Invalid Email/Password.", Toast.LENGTH_LONG);
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
                            params.put("email", email.getText().toString());
                            params.put("password", password.getText().toString());
                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("Content-Type", "application/x-www-form-urlencoded");
                            return super.getHeaders();
                        }
                    };

                    requestQueue.add(stringRequest);
                }

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

}
