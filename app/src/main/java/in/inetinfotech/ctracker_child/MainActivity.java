package in.inetinfotech.ctracker_child;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class MainActivity extends AppCompatActivity {
    Button sos;
    Bitmap bitmap;
    Intent intent;
    private int PICK_IMAGE_REQUEST = 1;
    FloatingActionButton fab;
    private boolean loggedIn = false;
    boolean doubleBackToExitPressedOnce = false;
    String address,city,state,country,postalCode,knownName;
    public static final String LOGIN_URL ="https://anoopsuvarnan1.000webhostapp.com/Locationapi.php";
    SharedPreferences sharedPreferences,sh;
    public static final int RequestPermissionCode = 1;
    String phonenumber;
    private Uri filePath;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EnableRuntimePermission();
        sh=getSharedPreferences("Official",MODE_PRIVATE);
        sharedPreferences=getSharedPreferences("phone",MODE_PRIVATE);
        sos = findViewById(R.id.soss);
        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loggedIn = sharedPreferences.getBoolean("phone", false);
                if (!loggedIn) {
                    // Snackbar.make(v,"Enter emergency number",Snackbar.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, "Enter Emergency Number", Toast.LENGTH_SHORT).show();
                }
                try {

                    phonenumber = sharedPreferences.getString("pnumber", null);
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phonenumber, null, address+city+country+postalCode+knownName, null, null);
                    Toast.makeText(getApplicationContext(), "SMS Sent!",
                            Toast.LENGTH_LONG).show();
                    StringRequest stringRequest = new StringRequest(Request.Method.POST,"https://dress-metal.000webhostapp.com/sms.php",
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {


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
                            params.put("num", phonenumber);
                            params.put("loc",address+city+country+postalCode+knownName);
                            //returning parameter
                            return params;
                        }
                    };

                    //Adding the string request to the queue
                    RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                    requestQueue.add(stringRequest);
official();
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phonenumber));
                    startActivity(intent);
                } catch (Exception e) {
                   /* Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again later!",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();*/
                }

            }
        });
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Keep track of user location.
        // Use callback/listener since requesting immediately may return null location.
        // IMPORTANT: TO GET GPS TO WORK, MAKE SURE THE LOCATION SERVICES ON YOUR PHONE ARE ON.
        // FOR ME, THIS WAS LOCATED IN SETTINGS > LOCATION.
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, new Listener());
        // Have another for GPS provider just in case.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, new Listener());
        // Try to request the location immediately
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (location != null) {
            handleLatLng(location.getLatitude(), location.getLongitude());
        }
               /* Toast.makeText(getApplicationContext(),
                        "Trying to obtain GPS coordinates. Make sure you have location services on.",
                        Toast.LENGTH_SHORT).show();*/
    }

    /**
     * Handle lat lng.
     */
    private void handleLatLng(final double latitude, final double longitude) {
        Log.v("TAG", "(" + latitude + "," + longitude + ")");
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        city = addresses.get(0).getLocality();
        state = addresses.get(0).getAdminArea();
        country = addresses.get(0).getCountryName();
        postalCode = addresses.get(0).getPostalCode();
        knownName = addresses.get(0).getFeatureName();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        if (response.equals("success")) {

                            Toast.makeText(MainActivity.this, "success upload", Toast.LENGTH_SHORT).show();
                            //Creating a shared preference

                        } else {
                            Toast.makeText(MainActivity.this, "success failed", Toast.LENGTH_SHORT).show();
                            //If the server response is not success
                            //Displaying an error message on toast
                            // Toast.makeText(VerificationActivity.this,"success upload fail",Toast.LENGTH_SHORT).show();
                        }
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
                params.put("latitude",address);
              //  params.put("longitude",String.valueOf(longitude));


                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }


    class Listener implements LocationListener {
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            handleLatLng(latitude, longitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
        @Override
        public void onProviderEnabled(String provider) {

        }
        @Override
        public void onProviderDisabled(String provider) {

        }
        /**
         * Listener for changing gps coords.
         */


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu2, menu);
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
            Intent intent=new Intent(this,Phone.class);
            startActivity(intent);
            return true;
        }
        if (id==R.id.prealert)
        {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phonenumber, null, "I think i am in a panic situation", null, null);
            Toast.makeText(getApplicationContext(), "Prealert sent", Toast.LENGTH_LONG).show();
        }
        if(id==R.id.official)
        {
            Intent intent=new Intent(this,Official.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
    public void EnableRuntimePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.CALL_PHONE)) {

            // Toast.makeText(Cpature_image.this,"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CALL_PHONE,Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.CAMERA}, RequestPermissionCode);


        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    //  Toast.makeText(Cpature_image.this,"Permission Granted, Now your application can access CAMERA.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(this, "Permission Canceled, Now your application cannot access sms.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }
    public void official()
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,"https://dress-metal.000webhostapp.com/sms1.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


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
String n=sh.getString("num1",null);
String n1=sh.getString("num2",null);
                //Adding parameters to request
                params.put("num",n);
                params.put("num2",n1);
                params.put("loc",address);

                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
        Toast.makeText(MainActivity.this,"sent",Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {

            //getting the image Uri
           // Uri imageUri = data.getData();
          //  bitmap = (Bitmap) data.getExtras().get("data");
            //getting bitmap object from uri
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            //displaying selected image to imageview
            //imageView.setImageBitmap(bitmap);

            //calling the method uploadBitmap to upload image
            uploadBitmap(bitmap);
        }
    }

    /*
     * The method is taking Bitmap as an argument
     * then it will return the byte[] array for the given bitmap
     * and we will send this array to the server
     * here we are using PNG Compression with 80% quality
     * you can give quality between 0 to 100
     * 0 means worse quality
     * 100 means best quality
     * */
    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadBitmap(final Bitmap bitmap) {

        //getting the tag from the edittext


        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, EndPoints.UPLOAD_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                // params.put("tags", tags);
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }
    public void cam(View V)
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
       intent.putExtra("android.intent.extra.quickCapture",true);
        startActivityForResult(intent, 100);
    }
    }




