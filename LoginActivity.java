package com.example.olx.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.olx.MainActivity;
import com.example.olx.R;
import com.example.olx.ClientSocket;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    Button b1,b2;
    EditText ed1,ed2;
    public final static String User_Key = "user_name";
    public  static  String myUserName;
    ClientSocket connection;//conexiunea cu serverul
    Thread sendThread;
    Thread readThread;
    public static Handler loginHandler;//comunic intre thread principal-conexiune
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);
        connection = ClientSocket.getInstance();

        b1 = (Button)findViewById(R.id.button_login);
        ed1 = (EditText)findViewById(R.id.editText);
        ed2 = (EditText)findViewById(R.id.editText2);

        b2 = (Button)findViewById(R.id.button_createAccount);

        loginHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                String res = bundle.getString("LOGINRES");//ce am trimis prin handler
                if(res.equals("0")){ //daca e 0 datele sunt corecte
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    //intent.putExtra(LoginActivity.User_Key,ed1.getText().toString());
                    myUserName = ed1.getText().toString();
                    startActivity(intent);
                }
                else if(res.equals("5")){//click pe create account - si daca exista imi da valoarea 5 innapoi
                    Toast.makeText(LoginActivity.this, "User already exists with that name", Toast.LENGTH_SHORT).show();
                }
                else{//dau pe login si sunt incorecte datele se trimite codul 1
                    Toast.makeText(LoginActivity.this, "Incorrect data", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        readThread = new Thread(new Runnable() {
            @Override
            public void run() {//pornesc threadul din care citesc pe server
                connection.ReadFromServer();
                //readThread=null;
            }
        });
        readThread.start();
       b1.setOnClickListener(new View.OnClickListener() {

           @Override
           public void onClick(View v) {//login
               sendThread = new Thread(new Runnable() {
                   @Override
                   public void run() { //trimit user si parola
                       try {
                           connection.LoginServer(ed1.getText().toString(),ed2.getText().toString());
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                       sendThread = null;
                   }
               });

               sendThread.start();

           }
       });

        b2.setOnClickListener(new View.OnClickListener() {//create

            @Override
            public void onClick(View v) {//creez un cont pe server
                sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connection.CreateAccountServer(ed1.getText().toString(),ed2.getText().toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        sendThread = null;
                    }
                });
                sendThread.start();
            }
        });





    }

}