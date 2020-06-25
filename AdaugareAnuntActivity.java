package com.example.olx;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.olx.login.LoginActivity;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;

public class AdaugareAnuntActivity extends AppCompatActivity {
    String username;//userul
    ClientSocket connection;
    Thread sendThread;
    EditText titlu;
    Spinner categorie;
    EditText informatii;
    EditText telefon;
    Button add_anuntBtn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_anunt_layout);
        username= LoginActivity.myUserName;
        connection = ClientSocket.getInstance();
        titlu = findViewById(R.id.titlu_add);
        categorie = findViewById(R.id.categorie_spinner_add);
        informatii = findViewById(R.id.informatii_add);
        telefon = findViewById(R.id.telefon_add);
        add_anuntBtn = findViewById(R.id.add_anunt);

        add_anuntBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!titlu.getText().toString().equals("") && !informatii.getText().toString().equals("") && telefon.getText().toString().length()==10 ){//verific daca a scris ceva userul
                    sendThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                connection.AdaugareAnuntServer(username,categorie.getSelectedItem().toString(),titlu.getText().toString(),informatii.getText().toString(),telefon.getText().toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sendThread=null;
                        }
                    });
                    sendThread.start();
                    Toast.makeText(getApplicationContext(),"Anunt adaugat!",Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }else{
                    Toast.makeText(getApplicationContext(), "Please fill all the details", Toast.LENGTH_SHORT).show();//userul nu a complectat toate campuriile
                }
            }
        });

    }
}
