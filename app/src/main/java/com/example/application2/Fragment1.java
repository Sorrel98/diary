package com.example.application2;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.android.volley.Request.*;
import static com.android.volley.Request.Method.*;
import static com.example.application2.MainActivity.phoneBooks;

public class Fragment1 extends Fragment {
    final List<String> LIST_MENU = MainActivity.names;
    List<PhoneBook> REF_MENU = phoneBooks;
    ListViewAdapter adapter;
    String[] permission_list = { Manifest.permission.WRITE_CONTACTS };

    public Fragment1() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment1, null);
        checkPermission();
        for(int i=0; i<REF_MENU.size();i++){
            request(String.valueOf( REF_MENU.get(i).getName()),String.valueOf(REF_MENU.get(i).getTel()));
            Log.d("ref", String.valueOf(REF_MENU.get(i).getName())+","+ String.valueOf(REF_MENU.get(i).getTel()));
        }
        adapter = new ListViewAdapter(getActivity(), R.layout.listview_btn_item, REF_MENU);
        final ListView listview = (ListView) view.findViewById(R.id.listview1);
        ImageButton add_button = (ImageButton) view.findViewById(R.id.add_btn);
//        final Button dbbtn = view.findViewById(R.id.getTel);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.phone_add, null);
                ad.setView(view);
                ad.setTitle("????????? ??????");       // ?????? ??????
                ad.setMessage("????????? ??????????????? ??????????????????");   // ?????? ??????
                // EditText ????????????

                final Button submit = (Button) view.findViewById(R.id.buttonSubmit);
                final EditText name = (EditText) view.findViewById(R.id.edittext_name);
                final EditText phone_num = (EditText) view.findViewById(R.id.edittext_phone);
                name.setHint("????????? ???????????????");
                phone_num.setHint("????????? ???????????????");
                final AlertDialog dialog = ad.create();
//                dbbtn.setOnClickListener(new View.OnClickListener(){
//
//                    @Override
//                    public void onClick(View v) {
////                        get();
//                    }
//                });
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        PhoneBook add_phone = new PhoneBook();
                        // Text ??? ????????? ?????? ?????????
                        String full_name = name.getText().toString();
                        String phone_number = phone_num.getText().toString();

                        request(name.getText().toString(),phone_num.getText().toString());
                        add_phone.setName(full_name);
                        add_phone.setTel(phone_number);

                        contactAdd(full_name, phone_number);
                        adapter.phoneBooks.add(add_phone);
                        dialog.dismiss();     //??????
                    }
                });
                ad.setNegativeButton("????????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //??????
                    }
                });
                dialog.show();
            }
        });
        adapter.notifyDataSetChanged();
        listview.setAdapter(adapter);
        REF_MENU = adapter.phoneBooks;

        return view;
    }
    public void contactAdd(final String name, final String phone_num){
        new Thread(){
            @Override
            public void run() {
                ArrayList<ContentProviderOperation> list = new ArrayList<>();
                try{
                    list.add(
                            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                    .build()
                    );
                    list.add(
                            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)   //??????
                                    .build()
                    );
                    list.add(
                            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone_num)           //????????????
                                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE  , ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)   //????????????(Type_Mobile : ?????????)
                                    .build()
                    );
                    checkPermission();
                    getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, list);  //???????????????
                    list.clear();   //????????? ?????????
                }catch(RemoteException e){
                    e.printStackTrace();
                }catch(OperationApplicationException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void checkPermission() {
        //?????? ??????????????? ????????? 6.0???????????? ???????????? ????????????.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        for (String permission : permission_list) {
            //?????? ?????? ????????? ????????????.
            int chk = getActivity().checkCallingOrSelfPermission(permission);

            if (chk == PackageManager.PERMISSION_DENIED) {
                //?????? ?????????????????? ???????????? ?????? ?????????
                requestPermissions(permission_list, 0);
            }
        }
   }

    public void request(String name, String tel){
        String url = "http://192.249.19.254:7980/tels";
        JSONObject testjson = new JSONObject();
        try{
            testjson.put("name",name);
            testjson.put("tel",tel);
            final String jsonString = testjson.toString();
            Log.d("body",jsonString);

            final  RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(POST, url, testjson, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
            try {
                Log.d("test","?????????????????????");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
        }});

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Log.d("test","first");
            requestQueue.add(jsonObjectRequest);
            Log.d("test", String.valueOf(requestQueue));

} catch (JSONException e) {
        e.printStackTrace();
        }
    }

}

