package com.example.olx;


import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.example.olx.login.LoginActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;


public class ClientSocket {

    private static ClientSocket instance = null;
    public boolean isConnectedToServer = false;
    public static Context mContext;
    private Socket mySocket = null;
    private static String SERVER_IP = "192.168.0.103";
    private static final int SERVER_PORT = 33333;

    private DataOutputStream outStream = null;//scriu pe server
    private DataInputStream inStream = null;// citesc de pe server


    public static ClientSocket getInstance() {//parte de singlenton
        if (ClientSocket.instance == null) {
            ClientSocket.instance = new ClientSocket();
            return ClientSocket.instance;
        } else
            return ClientSocket.instance;

    }



    private synchronized void connectToServer() { //se executa pe un fir pt a nu fi intrerupt
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            mySocket = new Socket(serverAddr, SERVER_PORT);//initializare variabile
            outStream = new DataOutputStream(mySocket.getOutputStream());
            inStream = new DataInputStream(mySocket.getInputStream());
            isConnectedToServer = true;
            Log.w("error", "connected to server!");
        } catch (IOException e) {
            closeStuff();
            Log.w("error", "unable to connect to server!" + e);

        }
    }


    public void ReadFromServer() {//citesc de pe server


        if (isConnectedToServer) {
            int len;//dimensiunea/lungimea  cat am citit de pe server
            byte[] buffer = new byte[16384];
            Message message;//comunic intre thread-uri
            Bundle bundle;
            try {
                while ((len = inStream.read(buffer)) != -1) {//citesc in buffer pana serverul nu mai trimite
                    int cod = buffer[0]-48; //48 e cod din buffer
                    String data = new String(buffer, 1, len - 1);//date primite de la server
                    switch (cod) {
                        case 1://create result
                        case 0://login result
                            //if (data == "0")

                            message = Message.obtain();
                            bundle = new Bundle();
                            bundle.putString("LOGINRES", String.valueOf(cod));
                            message.setData(bundle);
                            LoginActivity.loginHandler.sendMessage(message); //loginHandler- trimit mesajul static

                            break;
                        case 5://userul exista deja

                            message = Message.obtain();
                            bundle = new Bundle();
                            bundle.putString("LOGINRES", String.valueOf(cod));
                            message.setData(bundle);
                            LoginActivity.loginHandler.sendMessage(message);
                            break;
                        case 2://primesc anunturi si le trimit in MainActivity
                            //Show toast
                           // HandleAnunturi.mAnunturi = HandleAnunturi(data);
                            message = Message.obtain();
                            bundle = new Bundle();

                            bundle.putString("ANUNTURI", data);
                            message.setData(bundle);
                            MainActivity.mainHandler.sendMessage(message);
                            //List<Anunt> anunturi = HandleAnunturi(data);
                            break;
                        case 7://primesc anunturile mele si le trimit in MeniuCont
                            //Show toast
                            // HandleAnunturi.mAnunturi = HandleAnunturi(data);
                            message = Message.obtain();
                            bundle = new Bundle();

                            bundle.putString("ANUNTURIUSER", data);
                            message.setData(bundle);
                            MeniuCont.anunturiUserHandler.sendMessage(message);
                            //List<Anunt> anunturi = HandleAnunturi(data);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + cod);
                    }


                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            closeStuff();
        } else {
            connectToServer();
            if (isConnectedToServer)
                ReadFromServer();
            else
                closeStuff();
        }
        return;

    }


    private List<Anunt> HandleAnunturi(String anunturiPack) {

        List<Anunt> anunturiList = new ArrayList<Anunt>();
        String[] anunturi = anunturiPack.split("`");// separ un anunt de altul prin acest caracter `
        for (String anunt : anunturi) {
            String[] anuntData = anunt.split("\\|");//separ campurile din fiecare anunt |
            Anunt a = new Anunt();//creez un nou anunt
            a.owner = anuntData[0];//OWNER- user
            a.categorie = anuntData[1];//categorie
            a.titlu = anuntData[2];//Titlu
            a.info = anuntData[3];//informatii
            a.telefon = anuntData[4];//telefon
            anunturiList.add(a);//adaug in lista de anunturi
        }

        return anunturiList;//returnez o lista de anunturi unde sunt stocate toate anunturile


    }

    private void SendToServer(byte[] bytes, int size) {//trimit pachetul de bytes spre server
        if (isConnectedToServer) {
            try {
                outStream.write(bytes, 0, size);
                Log.i("info", "SENT TO SERVER: " + size);
                outStream.flush();
            } catch (SocketException se) {
                closeStuff();
            } catch (IOException e) {
                Log.w("network Connection", "Not able to send to server, disconnecting...");
                closeStuff();
            }
        } else {
            connectToServer();
            if (isConnectedToServer)
                SendToServer(bytes, size);
            else
                closeStuff();
        }
    }


    public void LoginServer(String username, String password) throws IOException {
        try {
            String data = username + "~" + password;
            byte code = 0;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();//concatenez codul cu data.getBytes
            outputStream.write(code);
            outputStream.write(data.getBytes());

            byte pack[] = outputStream.toByteArray();
            SendToServer(pack, pack.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void CreateAccountServer(String username, String password) throws IOException {
        try {
            String data = username + "~" + password;
            byte code = 1;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(code);
            outputStream.write(data.getBytes());

            byte pack[] = outputStream.toByteArray();
            SendToServer(pack, pack.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void GetAnunturiServer(String categorie) throws IOException {//categorie
        try {
            String data = categorie;
            byte code = 2;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(code);
            outputStream.write(data.getBytes());

            byte pack[] = outputStream.toByteArray();
            SendToServer(pack, pack.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void GetAnunturiUserServer(String user) throws IOException {//returnez anunturile pentru un user
        try {
            String data = user;
            byte code = 7;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(code);
            outputStream.write(data.getBytes());

            byte pack[] = outputStream.toByteArray();
            SendToServer(pack, pack.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void AdaugareAnuntServer(String owner, String categorie, String titlu, String info, String telefon) throws IOException {
        try {
            String data = owner + "~" + categorie + "~" + titlu + "~" + info + "~" + telefon;
            byte code = 3;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(code);
            outputStream.write(data.getBytes());

            byte pack[] = outputStream.toByteArray();
            SendToServer(pack, pack.length);//trimit anuntul pe server
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void RemoveAnuntServer(String titlu) throws IOException {//sterg anuntul dupa titlu, fara titlu unic
        try {
            String data = titlu;
            byte code = 4;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(code);
            outputStream.write(data.getBytes());

            byte pack[] = outputStream.toByteArray();
            SendToServer(pack, pack.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void closeStuff() {//inchid conexiunile
        Log.i("info", "Connection closed by someone");
        isConnectedToServer = false;
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        inStream = null;
        outStream = null;
        try {
            if (mySocket != null)
                mySocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mySocket = null;
    }
}
