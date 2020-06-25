package com.example.olx;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.olx.login.LoginActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.olx.MainActivity.CATEGORIE_ANUNT_KEY;
import static com.example.olx.MainActivity.INFO_ANUNT_KEY;
import static com.example.olx.MainActivity.OWNER_ANUNT_KEY;
import static com.example.olx.MainActivity.TELEFON_ANUNT_KEY;
import static com.example.olx.MainActivity.TITLU_ANUNT_KEY;


public class MeniuCont extends AppCompatActivity {

    public static Handler anunturiUserHandler;
    ListView anunturiListView;
    ClientSocket connection;
    Thread readThread;
    Thread sendThread;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_menu_layout);
        TextView user = findViewById(R.id.usename_user);
        user.setText(LoginActivity.myUserName);

        Button anunturile_mele = findViewById(R.id.anunturile_meleBtn);
        Button deconectare = findViewById(R.id.disc_button);
        Button anunturiOlx = findViewById(R.id.anunturiOlxBtn);

        connection = ClientSocket.getInstance();

        anunturiUserHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                String res = bundle.getString("ANUNTURIUSER");
                assert res != null;
                List<Anunt> anunturi = MainActivity.HandleAnunturi(res);
                if (anunturi!=null)

                    FillWithAnunturi(anunturi);
                else{
                    List<String> titles = new ArrayList<String>();//daca nu sunt anunturi, list view gol

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.anunt_list_item, titles);

                    anunturiListView.setAdapter(arrayAdapter);
                }
                return false;
            }
        });

        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                connection.ReadFromServer();
                //readThread=null;
            }
        });
        readThread.start();

        anunturile_mele.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connection.GetAnunturiUserServer(LoginActivity.myUserName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        sendThread = null;
                    }
                });
                sendThread.start();
                showDialogAnunturi();
            }
        });

        deconectare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeniuCont.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        anunturiOlx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeniuCont.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }



    private void FillWithAnunturi(final List<Anunt> anunturi){
        anunturiListView.setOnItemClickListener(null);
        List<String> titles = new ArrayList<String>();
        for(Anunt a:anunturi){
            if(a.owner.equals(LoginActivity.myUserName))//verific daca sunt ale mele
                titles.add(a.titlu);
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, R.layout.anunt_list_item, titles);

        anunturiListView.setAdapter(arrayAdapter);
        anunturiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Anunt a= MainActivity.getAnuntByTitle(((TextView)view).getText().toString(),anunturi);
                Intent intent = new Intent(getApplicationContext(),DetaliiAnunt.class);
                intent.putExtra(OWNER_ANUNT_KEY,a.owner);
                intent.putExtra(TITLU_ANUNT_KEY,a.titlu);
                intent.putExtra(INFO_ANUNT_KEY,a.info);
                intent.putExtra(TELEFON_ANUNT_KEY,a.telefon);
                intent.putExtra(CATEGORIE_ANUNT_KEY,a.categorie);
                startActivity(intent);
                // Toast.makeText(getApplicationContext(),((TextView)view).getText().toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    private void showDialogAnunturi(){
        final AlertDialog.Builder alertDialog = new
                AlertDialog.Builder(this);

        List<String> titles = new ArrayList<String>();
        View rowList = getLayoutInflater().inflate(R.layout.rows_anunturi_user, null);
        anunturiListView = rowList.findViewById(R.id.listView);
        Button ok = rowList.findViewById(R.id.buttonOK);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, R.layout.anunt_list_item, titles);
        anunturiListView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        alertDialog.setView(rowList);//setez layout pe dialog
        final AlertDialog dialog = alertDialog.create();
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
