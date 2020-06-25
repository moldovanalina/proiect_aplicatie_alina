package com.example.olx;

import android.content.Intent;
import android.os.Bundle;

import com.example.olx.login.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.olx.ui.main.SectionsPagerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    //apar anunturile pe categorii

    ClientSocket connection;
    Thread sendThread;
    Thread readThread;
    ListView anunturiListView;
    SwipeRefreshLayout refresh;
    public static String TITLU_ANUNT_KEY="titlu_key";
    public static String INFO_ANUNT_KEY="info_key";
    public static String CATEGORIE_ANUNT_KEY="categorie_key";
    public static String TELEFON_ANUNT_KEY="telefon_key";
    public static String OWNER_ANUNT_KEY="owner_key";


    public static Handler mainHandler;//se trimit messajele
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connection = ClientSocket.getInstance();

        anunturiListView = findViewById(R.id.anunturiListView);
        refresh = findViewById(R.id.swipe_refresh);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        final TabLayout tabs = findViewById(R.id.categorii);
        tabs.setupWithViewPager(viewPager);

        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                connection.ReadFromServer();
                //readThread=null;
            }
        });
        readThread.start();

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {// refresh la pagina

                sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connection.GetAnunturiServer((tabs.getTabAt(tabs.getSelectedTabPosition())).getText().toString());//trimit numele la categoria in care sunt
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        sendThread = null;
                    }
                });
                sendThread.start();

                refresh.setRefreshing(false);
            }
        });
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {//cand schimb categoria
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getText().toString()){
                    case "Masini":
                        sendThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    connection.GetAnunturiServer("Masini");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                sendThread = null;
                            }
                        });
                        sendThread.start();
                        break;
                    case "Animale":
                        sendThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    connection.GetAnunturiServer("Animale");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                sendThread = null;
                            }
                        });
                        sendThread.start();
                        break;
                    default: break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);
        FloatingActionButton button_meniu = findViewById(R.id.button_meniu);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,AdaugareAnuntActivity.class);
                startActivity(intent);

            }
        });
        button_meniu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//daca apas pe butonul de meniu, merg in MeniuCont
                Intent intent = new Intent(MainActivity.this,MeniuCont.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mainHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                String res = bundle.getString("ANUNTURI");
                assert res != null;
                List<Anunt> anunturi = HandleAnunturi(res);
                if (anunturi!=null)

                    FillWithAnunturi(anunturi);
                else{
                    List<String> titles = new ArrayList<String>();

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.anunt_list_item, titles);

                    anunturiListView.setAdapter(arrayAdapter);
                }
                return false;
            }
        });
    }

    public static List<Anunt> HandleAnunturi(String anunturiPack) {//separarea anunturilor

        try {
            List<Anunt> anunturiList = new ArrayList<Anunt>();
            String[] anunturi = anunturiPack.split("`");
            for (String anunt : anunturi) {
                String[] anuntData = anunt.split("\\|");
                Anunt a = new Anunt();
                a.owner = anuntData[0];//OWNER
                a.categorie = anuntData[1];//categorie
                a.titlu = anuntData[2];//Titlu
                a.info = anuntData[3];//informatii
                a.telefon = anuntData[4];//telefon
                anunturiList.add(a);
            }

            return anunturiList;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }


    }

    private void FillWithAnunturi(final List<Anunt> anunturi){
        anunturiListView.setOnItemClickListener(null);
        List<String> titles = new ArrayList<String>();
        for(Anunt a:anunturi){
            titles.add(a.titlu);
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, R.layout.anunt_list_item, titles);

        anunturiListView.setAdapter(arrayAdapter);//setez adaptor
        anunturiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//merg la detalii
                Anunt a= getAnuntByTitle(((TextView)view).getText().toString(),anunturi);
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

    public static Anunt getAnuntByTitle(String titlu,List<Anunt> anunturi){//vreau sa aflu pe ce anunt am dat click
        for(Anunt a:anunturi){
            if (a.titlu.equals(titlu))//verific anuntul dupa titlu
                return a;
        }
        return null;
    }
}