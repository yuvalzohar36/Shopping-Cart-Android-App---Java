package com.example.shoppingcartnew;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CurrentlyCart extends AppCompatActivity {
    private DatabaseReference instance_user_id;
    private String User_ID;
    private FirebaseAuth firebaseAuth;
    private CheckBox checkBox;
    List<CheckBox> CheckBoxList = new ArrayList<CheckBox>();
    List<LinearLayout> LinearLayoutList = new ArrayList<LinearLayout>();
    LinearLayout linearMain;
    private Button Uncheck_all;
    private Boolean Share_IT = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currently_cart);
        firebaseAuth = FirebaseAuth.getInstance();
        String User_ID = firebaseAuth.getCurrentUser().getUid();
        instance_user_id = FirebaseDatabase.getInstance().getReference().child("Users").child(User_ID).child("CurrentlyCart");
        linearMain = (LinearLayout)findViewById(R.id.linear_main_new);
        Uncheck_all = (Button)findViewById(R.id.Uncheck_all_Btn2);
        Load_From_Main_Cart();
        clear_all();

    }
    public void Load_From_Main_Cart(){

        instance_user_id.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String productName = snapshot.child("productName").getValue().toString();
                    String Quantity = snapshot.child("quantity").getValue().toString();
                    final String isCheck = snapshot.child("isCheck").getValue().toString();
                    checkBox = new CheckBox(CurrentlyCart.this);
                    checkBox.setScaleX(1.1f);
                    checkBox.setScaleY(1.1f);
                    checkBox.setTypeface(checkBox.getTypeface(), Typeface.BOLD);
                    TextView t = new TextView(CurrentlyCart.this);
                    TextView space = new TextView(CurrentlyCart.this);
                    space.setText("X    ");
                    TextView qunatitytext = new TextView(CurrentlyCart.this);
                    String newtext1 = productName;
                    newtext1 = decode_firebase(newtext1);
                    String newquantity1 = Quantity;
                    checkBox.setText(newtext1);
                    checkBox.setChecked(Boolean.parseBoolean(isCheck));
                    space.setTextColor(Color.parseColor("#000000"));
                    space.setTypeface(space.getTypeface(),Typeface.BOLD);
                    qunatitytext.setTextColor(Color.parseColor("#000000"));
                    qunatitytext.setTypeface(qunatitytext.getTypeface(),Typeface.BOLD);
                    t.setText("     ");
                    qunatitytext.setText(newquantity1);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        checkBox.setButtonTintList(getColorStateList(R.color.colorPrimary));
                    }
                    CheckBoxList.add(checkBox);

                    final LinearLayout a = new LinearLayout(CurrentlyCart.this);
                    a.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayoutList.add(a);
                    checkBox.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                    a.addView(checkBox);
                    a.addView(space);
                    a.addView(qunatitytext);
                    a.addView(t);
                    linearMain.addView(a);
                    for (int k=0;k<CheckBoxList.size();k++){
                        final int finalK = k;
                        CheckBoxList.get(k).setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onClick(View view) {

                                String ConvertString = CheckBoxList.get(finalK).getText().toString().trim();
                                ConvertString = encode_firebase(ConvertString);
                                if (String.valueOf(CheckBoxList.get(finalK).isChecked()).equals("true")){
                                    instance_user_id.child(ConvertString).child("isCheck").setValue("true");
                                }
                                if (String.valueOf(CheckBoxList.get(finalK).isChecked()).equals("false")){
                                    instance_user_id.child(ConvertString).child("isCheck").setValue("false");
                                }

                            }});}
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void Sharing(){
        final List<String> NameList = new ArrayList<String>();
        instance_user_id.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String productName = snapshot.child("productName").getValue().toString();
                    String Quantity = snapshot.child("quantity").getValue().toString();
                    if (snapshot.getValue() == null){
                        Toast.makeText(CurrentlyCart.this,"לא נמצאו מוצרים ברשימה לשיתוף",Toast.LENGTH_LONG).show();
                        Share_IT=false;
                    }
                    else{
                        Share_IT = true;
                        String quantity = Quantity;
                        String product_name = productName;
                        product_name = decode_firebase(product_name);
                        NameList.add(product_name+"X "+quantity);

                    }
                    }
                String text = "";
                for (int i=0; i<NameList.size();i++){
                    text = text + NameList.get(i) +"\n";
                }
                if (Share_IT){
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "My Shopping Cart is:");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                    sendIntent.setType("text/plain");
                    Intent shareIntent = Intent.createChooser(sendIntent, "Sharing My Shopping cart");
                    startActivity(shareIntent);
                }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
});}
    public void clear_all(){
        Uncheck_all.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                for (int j=0;j<CheckBoxList.size();j++) {
                    CheckBoxList.get(j).setChecked(false);
                    String ConvertString = CheckBoxList.get(j).getText().toString().trim();
                    ConvertString = encode_firebase(ConvertString);
                    instance_user_id.child(ConvertString).child("isCheck").setValue("false");
                }
            }
        });}

        public String decode_firebase(String decoding_string){

            decoding_string = decoding_string.replace("_P%ë5nN*", ".")
                    .replace("_D%5nNë*", "$")
                    .replace("_H%ë5Nn*", "#")
                    .replace("_Oë5n%N*", "[")
                    .replace("_5nN*C%ë", "]")
                    .replace("*_S%ë5nN", "/");
            return decoding_string;
        }

        public String encode_firebase(String encoding_string){
            encoding_string = encoding_string.replace(".", "_P%ë5nN*")
                    .replace("$", "_D%5nNë*")
                    .replace("#", "_H%ë5Nn*")
                    .replace("[", "_Oë5n%N*")
                    .replace("]", "_5nN*C%ë")
                    .replace("/", "*_S%ë5nN");
            return encoding_string;
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu2) {
        getMenuInflater().inflate(R.menu.menu2, menu2);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.Shareit: {
                Sharing();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }}









