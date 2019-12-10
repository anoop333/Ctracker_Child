package in.inetinfotech.ctracker_child;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class Official extends AppCompatActivity {
ImageView imageView;
EditText num1,num2;
SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_official);
        imageView=findViewById(R.id.imageView);
        num1=findViewById(R.id.editText);
        num2=findViewById(R.id.editText2);
        sharedPreferences=getSharedPreferences("Official",MODE_PRIVATE);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               SharedPreferences.Editor editor=sharedPreferences.edit();
               editor.putString("num1",num1.getText().toString());
               editor.putString("num2",num2.getText().toString());
               editor.apply();
               Toast.makeText(Official.this,"saved",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
