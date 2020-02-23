package com.example.shoppingcartnew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoppingcartnew.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    private EditText Username;
    private EditText Password;
    private Button Login;
    private TextView Registerpage;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseDatabase database = FirebaseDatabase.getInstance();


        Username = (EditText)findViewById(R.id.UsernameeditText); // define the username edit text
        Password = (EditText)findViewById(R.id.PasswordeditText); // define the password edit text
        Login = (Button)findViewById(R.id.LoginButton); // define the Login Button
        Registerpage = (TextView)findViewById(R.id.registerpage);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null){
            finish();
            startActivity(new Intent(MainActivity.this, MainCartActivity.class)); /// build applicactivity
        }

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((!Username.getText().toString().equals("")) && !Password.getText().toString().equals("")){
                validate(Username.getText().toString(), Password.getText().toString());
            }
                else {
                    Toast.makeText(MainActivity.this, "פרטים חסרים", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Registerpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }


        });


    }
    private void validate(String userName, String UserPassword){
        progressDialog.setMessage("אנו מכינים את רשימת הקניות בשבילך, אנא המתן...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(userName,UserPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();

                    Toast.makeText(MainActivity.this, "התחברות הושלמה", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, MainCartActivity.class));

                }else{
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "התחברות נכשלה", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }}

