package com.example.shoppingcartnew;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainCartActivity extends AppCompatActivity {
    NewProduct newProduct;
    private FirebaseAuth firebaseAuth;
    private Button Add;
    private CheckBox checkBox;
    private EditText addText;
    private EditText QuantityPlainText;
    private Button Uncheck_all;
    LinearLayout linearMain;
    private ProgressDialog mProgressDialog;
    List<CheckBox> CheckBoxList = new ArrayList<CheckBox>();
    List<ImageButton> DeleteButtonList = new ArrayList<ImageButton>();
    List<LinearLayout> LinearLayoutList = new ArrayList<LinearLayout>();
    List<String> QuantityList = new ArrayList<String>();

    private DatabaseReference current_user_id;
    private DatabaseReference instance_user_id;
    private String User_ID;
    boolean stop = false;
    boolean first_time = true;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_cart);
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        linearMain = (LinearLayout) findViewById(R.id.linear_main);
        firebaseAuth = FirebaseAuth.getInstance();
        String User_ID = firebaseAuth.getCurrentUser().getUid();
        current_user_id = FirebaseDatabase.getInstance().getReference().child("Users").child(User_ID).child("Products");
        instance_user_id = FirebaseDatabase.getInstance().getReference().child("Users").child(User_ID);
        Add = (Button) findViewById(R.id.AddBtn);
        Uncheck_all = (Button)findViewById(R.id.Uncheck_all);
        addText = (EditText) findViewById(R.id.addeditText);
        QuantityPlainText = (EditText) findViewById(R.id.Quantity);
        QuantityPlainText.setInputType(InputType.TYPE_CLASS_NUMBER);
        newProduct = new NewProduct();
        Load_all();
        Add_data();
        clear_all();
    }

    public void Add_data() {

        Add.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                TextView r = new TextView(MainCartActivity.this);
                final LinearLayout n = new LinearLayout(MainCartActivity.this);
                n.setOrientation(LinearLayout.HORIZONTAL);
                String newtext = addText.getText().toString().trim();
                String FullText = newtext;
                newtext = encode_firebase(newtext);
                String newquantity = (QuantityPlainText.getText().toString().trim());
                if (!newtext.trim().equals("")) {
                    if (newquantity.trim().equals("")) {
                        newquantity = "1";
                    }
                    for (int p=0;p<CheckBoxList.size();p++){
                        final int finalP = p;
                        if (CheckBoxList.get(finalP).getText().toString().trim().equals(newtext))
                        {
                        stop = true;
                        Toast.makeText(MainCartActivity.this, "מוצר זה קיים ברשימה", Toast.LENGTH_LONG).show();
                        }
                    }
                    if (stop == false) {
                        newProduct.setProductName(newtext);
                        newProduct.setQuantity(newquantity);
                        newProduct.setIsCheck("false");
                        current_user_id.child(newtext).setValue(newProduct);

                        Toast.makeText(MainCartActivity.this, "Data Inserted", Toast.LENGTH_LONG).show();
                        checkBox = new CheckBox(MainCartActivity.this);
                        checkBox.setText(FullText);
                        TextView space = new TextView(MainCartActivity.this);
                        space.setText("X    ");
                        TextView TextViewQuantity = new TextView(MainCartActivity.this);
                        TextViewQuantity.setText(newquantity);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            checkBox.setButtonTintList(getColorStateList(R.color.colorPrimary));
                        }
                        checkBox.setScaleX(1.1f);
                        checkBox.setScaleY(1.1f);
                        checkBox.setTypeface(checkBox.getTypeface(), Typeface.BOLD);
                        r.setText("     ");
                        CheckBoxList.add(checkBox);
                        ImageButton imgbtn = new ImageButton(MainCartActivity.this);
                        imgbtn.setImageResource(R.drawable.ic_del);
                        DeleteButtonList.add(imgbtn);
                        space.setTextColor(Color.parseColor("#000000"));
                        space.setTypeface(space.getTypeface(),Typeface.BOLD);
                        TextViewQuantity.setTextColor(Color.parseColor("#000000"));
                        TextViewQuantity.setTypeface(TextViewQuantity.getTypeface(),Typeface.BOLD);
                        LinearLayoutList.add(n);
                        checkBox.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.0f;
                        params.gravity = Gravity.RIGHT;
                        n.setLayoutParams(params);
                        n.addView(checkBox);
                        n.addView(space);
                        n.addView(TextViewQuantity);
                        n.addView(r);
                        n.addView(imgbtn);
                        linearMain.addView(n);
                        addText.getText().clear();
                        QuantityPlainText.getText().clear();
                        // control Delete Buttons
                        for (int j = 0; j < DeleteButtonList.size(); j++) {
                            final int finalJ = j;
                            DeleteButtonList.get(j).setOnClickListener(new View.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onClick(View view) {

                                    String Delete_It = CheckBoxList.get(finalJ).getText().toString().trim()  ;
                                    Delete_It = encode_firebase(Delete_It);
                                    LinearLayoutList.get(finalJ).setVisibility(View.GONE);
                                    current_user_id.child(Delete_It).removeValue();

                                }
                            });
                        }
                        // control Check Value
                        for (int k=0;k<CheckBoxList.size();k++){
                            final int finalK = k;
                            CheckBoxList.get(k).setOnClickListener(new View.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onClick(View view) {
                                    String NewConvertString = CheckBoxList.get(finalK).getText().toString().trim();
                                    NewConvertString = encode_firebase(NewConvertString);
                                    if (String.valueOf(CheckBoxList.get(finalK).isChecked()).equals("true")){
                                        current_user_id.child(NewConvertString).child("isCheck").setValue("true");
                                    }
                                    if (String.valueOf(CheckBoxList.get(finalK).isChecked()).equals("false")){
                                        current_user_id.child(NewConvertString).child("isCheck").setValue("false");
                                    }

                                }});}


                        //from here
                        // control edit quantity
                        for (int curr = 0; curr<CheckBoxList.size();curr++){
                            final int finalCurr = curr;
                            CheckBoxList.get(curr).setOnLongClickListener(new View.OnLongClickListener() {
                                public boolean onLongClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainCartActivity.this);
                                    builder.setTitle("ערוך את הכמות ל" + CheckBoxList.get(finalCurr).getText().toString().trim());
                                    final EditText EditInput = new EditText(MainCartActivity.this);
                                    EditInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                                    int maxLength = 5;
                                    EditInput.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.MATCH_PARENT);
                                    EditInput.setLayoutParams(lp);
                                    builder.setView(EditInput);
                                    builder.setPositiveButton("ערוך", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            final TextView newonespace = new TextView(MainCartActivity.this);
                                            newonespace.setText("X    ");

                                            String NewChildString = CheckBoxList.get(finalCurr).getText().toString().trim();
                                            NewChildString = encode_firebase(NewChildString);
                                            current_user_id.child(NewChildString).child("quantity").setValue(EditInput.getText().toString().trim());
                                            //QuantityList.set( finalCurr, input.getText().toString().trim());
                                            TextView Newquantity = new TextView(MainCartActivity.this);
                                            Newquantity.setText(EditInput.getText().toString().trim());
                                            if (Newquantity.getText().toString().trim().equals("")){Newquantity.setText("1");}
                                            Newquantity.setTextColor(Color.parseColor("#000000"));
                                            Newquantity.setTypeface(Newquantity.getTypeface(),Typeface.BOLD);
                                            newonespace.setTextColor(Color.parseColor("#000000"));
                                            newonespace.setTypeface(newonespace.getTypeface(),Typeface.BOLD);
                                            LinearLayoutList.get(finalCurr).removeAllViews();
                                            LinearLayoutList.get(finalCurr).addView(CheckBoxList.get(finalCurr));
                                            LinearLayoutList.get(finalCurr).addView(newonespace);
                                            LinearLayoutList.get(finalCurr).addView(Newquantity);
                                            LinearLayoutList.get(finalCurr).addView(DeleteButtonList.get(finalCurr));
                                        }
                                    });
                                    builder.show();
                                    return true;
                                }
                            });}
                        //to here
                    }
                    stop = false;
                } else {
                    Toast.makeText(MainCartActivity.this, "אנא בחר שם למוצר", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void Load_all() {
        current_user_id.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String productName = snapshot.child("productName").getValue().toString();
                    String Quantity = snapshot.child("quantity").getValue().toString();
                    final String isCheck = snapshot.child("isCheck").getValue().toString();
                    checkBox = new CheckBox(MainCartActivity.this);
                    checkBox.setScaleX(1.1f);
                    checkBox.setScaleY(1.1f);
                    checkBox.setTypeface(checkBox.getTypeface(), Typeface.BOLD);
                    TextView t = new TextView(MainCartActivity.this);
                    final TextView space = new TextView(MainCartActivity.this);
                    space.setText("X    ");
                    space.setTextColor(Color.parseColor("#000000"));
                    space.setTypeface(space.getTypeface(), Typeface.BOLD);
                    final TextView qunatitytext = new TextView(MainCartActivity.this);
                    String newtext1 = productName;
                    final String newquantity1 = Quantity;
                    newtext1 = decode_firebase(newtext1);
                    checkBox.setText(newtext1);
                    checkBox.setChecked(Boolean.parseBoolean(isCheck));
                    t.setText("     ");
                    qunatitytext.setText(newquantity1);
                    qunatitytext.setTextColor(Color.parseColor("#000000"));
                    qunatitytext.setTypeface(qunatitytext.getTypeface(), Typeface.BOLD);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        checkBox.setButtonTintList(getColorStateList(R.color.colorPrimary));
                    }
                    CheckBoxList.add(checkBox);
                    final ImageButton imgbtn = new ImageButton(MainCartActivity.this);
                    imgbtn.setImageResource(R.drawable.ic_del);
                    DeleteButtonList.add(imgbtn);
                    final LinearLayout a = new LinearLayout(MainCartActivity.this);
                    a.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayoutList.add(a);
                    checkBox.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params.weight = 1.0f;
                    QuantityList.add(newquantity1);
                    params.gravity = Gravity.RIGHT;
                    a.setLayoutParams(params);
                    a.addView(checkBox);
                    a.addView(space);
                    a.addView(qunatitytext);
                    a.addView(t);
                    a.addView(imgbtn);
                    linearMain.addView(a);
                    first_time = false;
                    for (int k = 0; k < CheckBoxList.size(); k++) {
                        final int finalK = k;
                        CheckBoxList.get(k).setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onClick(View view) {
                                String ConvertString = CheckBoxList.get(finalK).getText().toString().trim();
                                ConvertString = encode_firebase(ConvertString);
                                if (String.valueOf(CheckBoxList.get(finalK).isChecked()).equals("true")) {
                                    current_user_id.child(ConvertString).child("isCheck").setValue("true");
                                }
                                if (String.valueOf(CheckBoxList.get(finalK).isChecked()).equals("false")) {
                                    current_user_id.child(ConvertString).child("isCheck").setValue("false");
                                }
                            }
                        });
                    }
                    imgbtn.setOnClickListener(new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onClick(View view) {
                            a.setVisibility(View.GONE);
                            current_user_id.child(productName).removeValue();
                        }
                    });
                    //from here
                    for (int curr = 0; curr < CheckBoxList.size(); curr++) {
                        final int finalCurr = curr;
                        CheckBoxList.get(curr).setOnLongClickListener(new View.OnLongClickListener() {
                            public boolean onLongClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainCartActivity.this);
                                builder.setTitle("ערוך את הכמות ל" + CheckBoxList.get(finalCurr).getText().toString().trim());
                                final EditText input = new EditText(MainCartActivity.this);
                                int maxLength = 5;
                                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
                                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                input.setLayoutParams(lp);
                                builder.setView(input);
                                builder.setPositiveButton("ערוך", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        final TextView newspace = new TextView(MainCartActivity.this);
                                        newspace.setText("X    ");
                                        String ConvertStringAgain = CheckBoxList.get(finalCurr).getText().toString().trim();
                                        ConvertStringAgain = encode_firebase(ConvertStringAgain);
                                        current_user_id.child(ConvertStringAgain).child("quantity").setValue(input.getText().toString().trim());
                                        //QuantityList.set( finalCurr, input.getText().toString().trim());
                                        TextView Newquantity = new TextView(MainCartActivity.this);
                                        Newquantity.setText(input.getText().toString().trim());
                                        Newquantity.setTextColor(Color.parseColor("#000000"));
                                        Newquantity.setTypeface(Newquantity.getTypeface(), Typeface.BOLD);
                                        newspace.setTextColor(Color.parseColor("#000000"));
                                        newspace.setTypeface(newspace.getTypeface(), Typeface.BOLD);
                                        if (Newquantity.getText().toString().trim().equals("")) {
                                            Newquantity.setText("1");
                                        }
                                        LinearLayoutList.get(finalCurr).removeAllViews();
                                        LinearLayoutList.get(finalCurr).addView(CheckBoxList.get(finalCurr));
                                        LinearLayoutList.get(finalCurr).addView(newspace);
                                        LinearLayoutList.get(finalCurr).addView(Newquantity);
                                        LinearLayoutList.get(finalCurr).addView(DeleteButtonList.get(finalCurr));
                                    }
                                });
                                builder.show();
                                return true;
                            }
                        });
                    }
                    //to here
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void Logout() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(MainCartActivity.this, MainActivity.class));
    }

    public void dialogbox(){
        final List<AlertDialog> AlertDialogList = new ArrayList<AlertDialog>();

        current_user_id.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String isCheck = snapshot.child("isCheck").getValue().toString();
                    if (isCheck.equals("true")) {
                        count += 1;
                    }}

            if (count>0){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainCartActivity.this,R.style.MyDialogTheme);
                builder.setTitle("שים לב ! פעולה זו תגרום למחיקה של העגלת הקניות הקודמת שלך !");
                builder.setMessage("מסכים ?");

                builder.setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        count = 0;
                        dialog.dismiss();
                        createnewinstance();

                    }
                });
                builder.setNegativeButton("לא", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        count = 0;

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                AlertDialogList.add(alert);

            }
            else
            {
                Toast.makeText(MainCartActivity.this, "לא נבחרו מוצרים", Toast.LENGTH_LONG).show();
                for (int j = 0; j < AlertDialogList.size(); j++) {
                    AlertDialogList.get(j).dismiss(); // close the dialog after backpress button ! ! ! ! ! !
                }}
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }});}

            private void createnewinstance() {
                    /// clear all
                    instance_user_id.child("CurrentlyCart").removeValue();
                    current_user_id.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                final String productName = snapshot.child("productName").getValue().toString();
                                String Quantity = snapshot.child("quantity").getValue().toString();
                                final String isCheck = snapshot.child("isCheck").getValue().toString();
                                if (isCheck.equals("true")){
                                    newProduct.setIsCheck("false");
                                    newProduct.setQuantity(Quantity);
                                    newProduct.setProductName(productName);
                                    instance_user_id.child("CurrentlyCart").child(newProduct.getProductName()).setValue(newProduct);
                    }

                }
                startActivity(new Intent(MainCartActivity.this, CurrentlyCart.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                        }});
    }


    public void clear_all(){
        Uncheck_all.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                for (int j=0;j<CheckBoxList.size();j++){
                    CheckBoxList.get(j).setChecked(false);
                    String ConvertString = CheckBoxList.get(j).getText().toString().trim();
                    ConvertString = encode_firebase(ConvertString);
                    current_user_id.child(ConvertString).child("isCheck").setValue("false");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.resumeCart: {
                startActivity(new Intent(MainCartActivity.this, CurrentlyCart.class));
                return true;
            }
            case R.id.createinstance: {
                dialogbox();
                return true;
            }

            case R.id.logoutMenu: {
                Logout();
                return true;
            }


        }

        return super.onOptionsItemSelected(item);
    }
}































