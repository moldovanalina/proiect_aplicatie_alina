package com.example.olx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.olx.login.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class DetaliiAnunt extends AppCompatActivity {

    TextView titluA;
    TextView categorieA;
    TextView ownerA;
    TextView infoA;
    TextView telefonA;
    Button dezactivareaBtn;
    ClientSocket connection;
    Thread sendThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anunt_main);
        //initializari
        connection =ClientSocket.getInstance();
        String owner = getIntent().getStringExtra(MainActivity.OWNER_ANUNT_KEY);
        String categorie = getIntent().getStringExtra(MainActivity.CATEGORIE_ANUNT_KEY);
        final String titlu = getIntent().getStringExtra(MainActivity.TITLU_ANUNT_KEY);
        String telefon = getIntent().getStringExtra(MainActivity.TELEFON_ANUNT_KEY);
        String info = getIntent().getStringExtra(MainActivity.INFO_ANUNT_KEY);
        titluA = findViewById(R.id.textView_Anunturi);
        categorieA = findViewById(R.id.textView_categorie);
        infoA = findViewById(R.id.textView_informatii);
        telefonA = findViewById(R.id.textView_telefon);
        ownerA = findViewById(R.id.textView_owner_anunt);
        dezactivareaBtn = findViewById(R.id.button_dezactivare);
        Button backBtn = findViewById(R.id.button_back);


        titluA.setText(titlu);//setez textul
        categorieA.setText(categorie);
        ownerA.setText(owner);
        telefonA.setText(telefon);
        infoA.setText(info);

        if (LoginActivity.myUserName.equals(owner)) {//verific daca anuntul e al meu
            dezactivareaBtn.setVisibility(View.VISIBLE);
            dezactivareaBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                connection.RemoveAnuntServer(titlu);//trimit catre server ca vreau sa il sterg
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sendThread = null;
                        }
                    });

                    sendThread.start();
                    Toast.makeText(getApplicationContext(),"Anunt dezactivat!",Toast.LENGTH_SHORT).show();
                    onBackPressed();//
                }
            });
        } else
            dezactivareaBtn.setVisibility(View.INVISIBLE);



        FloatingActionButton button_meniu = findViewById(R.id.button_meniu);//meniul cont
        button_meniu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetaliiAnunt.this,MeniuCont.class);
                startActivity(intent);

            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
