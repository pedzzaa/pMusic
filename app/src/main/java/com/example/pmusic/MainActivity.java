package com.example.pmusic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements MusicUI {

    // Deklaracija varijabli
    private static final int PERMISSION_REQUEST_CODE = 1;
    RecyclerView recyclerView;
    TextView noMusicTextView, songTitle;
    ImageButton playPrevious, playNext, pausePlay;
    MyMediaPlayer myMediaPlayer;
    //Button recent, albums, favorites, online;
    SongModel currentSong;
    SQLDatabase myDB;
    Cursor cursor;
    ArrayList<SongModel> songsList;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicijalizacija variabli
        recyclerView = findViewById(R.id.recycler_view);
        noMusicTextView = findViewById(R.id.no_songs_txt);
        songTitle = findViewById(R.id.song_name);

        /*
         * recent = findViewById(R.id.recent_page);
         * albums = findViewById(R.id.albums_page);
         * favorites = findViewById(R.id.favorites_page);
         * online = findViewById(R.id.online_page);
        */

        playPrevious = findViewById(R.id.play_previous);
        playNext = findViewById(R.id.play_next);
        pausePlay = findViewById(R.id.pause);

        myMediaPlayer = MyMediaPlayer.getInstance(this);
        myDB = new SQLDatabase(MainActivity.this);
        songTitle.setSelected(true);
        songsList = new ArrayList<>();

        // Ako nemamo permission pitamo usera opet za nju
        // a ako imamo permission učitavamo pjesme
        if (!checkPermission()) {
            requestPermissions(); // Tražimo permission
        } else {
            loadSongs(""); // Učitavamo pjesme
        }
    }

    /*
        Razlog zašto su naredne dvije funkcije dio MusicUI interfejsa jeste taj jer ih koristimo i u MainActivity
        i u MusicPlayer. One u oba activity-a rade različite stvari
    */

    // Ova funkcija je zadužna za update-ovanje UI elemenata
    // Takođe nakon što sinhronizuje sve elemente UI sa selektovanom pjesmom ona je pušta tako što pozove playMusic finkciju
    @Override
    public void setResourcesWithMusic(int songID) {
        currentSong = myDB.getSong(songID); // Nađemo trenutnu pjesmu uz pomoć ID-a i stavimo je u currentSong varijablu

        if(currentSong != null){ // Ako varijabla currentSong nije 'null' radimo sledeće
            songTitle.setText(currentSong.getTitle()); // Postavimo ime pjesme

            // Ovo je click listener za dugme pause/play
            // Ovo dugme pauzira ili pusti pjesmu da nastavi
            pausePlay.setOnClickListener(v -> myMediaPlayer.pausePlay(pausePlay));

            // Ovo je click listener za dugme playNext
            // sa njim puštamo sledeću pjesmu
            playNext.setOnClickListener(v -> {
                // Sledeće radimo na osnovu boolean vrijednosti
                // Ako je shuffle upaljen zovemo shuffleSongs funkciju ako nije zovemo playNext funkciju
                // vrijednost koju dobijemo stavljamo u currentSong varijablu
                currentSong = myMediaPlayer.shuffleState() ? myMediaPlayer.shuffleSongs(myDB) : myMediaPlayer.playNext(myDB);
                setResourcesWithMusic(currentSong.getId()); // Recursive call funkcije
                onResume(); // Pozovemo onResume funkciju da update-uje UI
            });

            // Ovo je click listener za playPrevious dugme
            // Sa njim puštamo prethodnu pjesmu
            playPrevious.setOnClickListener(v -> {
                // Ovdje je logika ista kao kod playNext click listener-a
                currentSong = myMediaPlayer.shuffleState() ? myMediaPlayer.shuffleSongs(myDB) : myMediaPlayer.playPrevious(myDB);
                setResourcesWithMusic(currentSong.getId());
                onResume();
            });

            // Ako media player ne svira, zovemo playMusic funkciju da krene da svira
            if(!myMediaPlayer.getPlayer().isPlaying()){
                playMusic();
            }
        } else {
            // Ako je currentSong 'null' prikazaćemo grešku u obliku Toast poruke
            UtilsMain.showToast(MainActivity.this, "Couldn't play the song :(");
        }
    }

    // Ova funkcija je zaslužna za puštanje pjesama
    @Override
    public void playMusic() {
        myMediaPlayer.getPlayer().reset(); // Resetujemo media player

        // Šta god da se desi pogrešno ovaj try catch block će to uhvatiti i pokazaće
        // poruku u obliku Toast-a
        try {
            myMediaPlayer.getPlayer().setDataSource(currentSong.getPath()); // Dobijamo path mp3 fajla u memoriji telefona
            myMediaPlayer.getPlayer().prepare(); // Pripremimo media player za pjesmu
            myMediaPlayer.getPlayer().start(); // pustimo pjesmu

            // Sa ovim listener-om pratimo da li je pjesma gotova
            // ako jeste puštamo sledeću na osnovu boolean vrijednosti
            myMediaPlayer.getPlayer().setOnCompletionListener(mp -> {
                // Ako je shuffle upaljen zovemo shuffleSongs funkciju ako nije zovemo playNext funkciju
                // vrijednost koju dobijemo stavljamo u currentSong varijablu
                currentSong = myMediaPlayer.shuffleState() ? myMediaPlayer.shuffleSongs(myDB) : myMediaPlayer.playNext(myDB);
                setResourcesWithMusic(currentSong.getId()); // Zovemo setResourcesWithMusic fuknciju
                onResume(); // Update-ujemo UI
            });

        } catch (IOException e) {
            // Prikazujemo poruku da nešto nije uredu
            UtilsMain.showToast(MainActivity.this, "Something went wrong :(");
        }
    }


    // Ova funkcija je zaslužna za Menu bar na vrhu aplikacije
    // Sa njim prikazujemo search dugme i prikazujemo dugme za refresh-ovanje databaze pjesama
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu); // Tražimo menu layout xml file

        // Sa ovom varijablom pravimo dugme za refresh-ovanje databaze
        MenuItem recreateDB = menu.findItem(R.id.recreate_db);
        recreateDB.setOnMenuItemClickListener(item -> {
            displayReloadDialog(); // Zovemo displayReloadDialog fuknciju
            return true; // Vraćamo true zato što ova funkcija onCreateOptionsMenu vraća boolean vrijednost
        });

        // Sa ovom varijablom pravimo dugme za search opciju
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView(); // Sa ovom varijablom dobijamo polje u koje možemo da kucamo

        // Ovdje pravimo listener za search query
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadSongs(newText); // Ako dobijemo neki search query iz searchView varijable, zovemo loadSongs funkciju
                return true;
            }
        });
        return true;
    }

    // Kao što joj ime kaže. Ova funkcija nam služi za ušitavanje pjesama
    private void loadSongs(String searchQuery) {

        // Na osnovu boolean vrijednosti ušitavamo sve pjesme iz databaze i stavljamo ih u recycle view
        // ili radimo search i onda prikazujemo pjesme koje su slične imenom kao one što je user tražio
        cursor = (searchQuery.isEmpty()) ? myDB.readAllMainTableData() : myDB.searchSongs(searchQuery);
        storeDataInArray(cursor); // Zovemo storeDataInArray funkciju da učita pjesme i prikaže ih u recycle view

        // Provjeravamo dali je databaza prazna
        // Ako jeste tražimo sve mp3 fajlove na memoriji telefona
        if (UtilsMain.isDatabaseEmpty(MainActivity.this)) {
            UtilsMain.storeSongs(MainActivity.this); // Ova funkcija čuva sve fajlove koje je našla u databazi
            cursor = (searchQuery.isEmpty()) ? myDB.readAllMainTableData() : myDB.searchSongs(searchQuery);
            storeDataInArray(cursor);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this)); // Posravljamo linear layout za recycle view
        recyclerView.setAdapter(new MainAdapter(MainActivity.this, cursor, myMediaPlayer)); // Postavljamo adapter
        recyclerView.getAdapter().notifyDataSetChanged(); // Update-ujemo adapter ako je došlo do nekih promjena
    }


    // Ova fukcija prikazuje reload database dialog
    // Ako kliknete na dugme za refresh-ovanje databaze dobijate ovaj UI elemenat
    private void displayReloadDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // Postavljamo pitanje
        builder.setTitle("Reload songs?");
        builder.setPositiveButton("OK", (dialog, which) -> reloadSongs()); // Ako je pritisnuto OK zovemo reloadSongs
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel()); // Ako nije zatvaramo dialog
        builder.show(); // Prikazujemo dijalog
    }

    // Ova funkcija je zaslužna za ponovno učitavanje pjesama sa memorije i njihovo smještanje u databazu
    private void reloadSongs() {
        // Pravimo dialog za prikazivanje poruke dok se databaza reload-uje
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Please wait while songs are reloading..."); // Prikazivanje poruke
        AlertDialog progressDialog = builder.show(); // prikazivanje dialoga

        // Za sve ovo pravimo Handler koji će ca odradi ove stvari
        new Handler().post(() -> {
            Executor executor = Executors.newSingleThreadExecutor(); // Pravimo novi thread za multithreading

            // Ove svari run-uju na background thread
            executor.execute(() -> {
                myDB.resetTable(); // Izbriše i ponovo napravi SQL tabelu
                UtilsMain.storeSongs(MainActivity.this); // Traži mp3 falove i stavlja ih u databazu
                myMediaPlayer.getPlayer().stop(); // Zaustavi media player ako je svirao
                myMediaPlayer.getPlayer().reset(); // Resetuje media player
                cursor.close(); // Cursor koji učitava pjesme i stavlja ih u recycle view zatvaramo ovjde
                                // da bi se poslije napravio novi cursor u loadSongs funkciji i takođe da ne bi došlo do
                                // nekih čudnih bug-ova

                // Ove run-uju na UI thread
                runOnUiThread(() -> {
                    loadSongs(""); // Zovemo fukciju za učitavanje pjesama i njihovo prikazivanje u recycle view
                    progressDialog.dismiss(); // Zatvaramo dialog koji nam je prikazivao poruku da sačekamo
                });
            });
        });
    }

    // Ova funkcija nam služi za učitavanje pjesama i njihovo prikazivanje u recycle view
    // Ona učitava informacije iz databaze i stavlja ih u ArrayList koji je recycle view-u da bi ga populizovali sa pjesmama
    void storeDataInArray(Cursor cursor) {
        Executor executor = Executors.newSingleThreadExecutor(); // Pravimo novi thread

        // Ove stvari run-uju na background thread
        executor.execute(() -> {
            // Ako nema pjesama u databazi prikaži tekst da nema pjesama
            // U suprotnom ga makni
            if (cursor.getCount() == 0) {
                runOnUiThread(() -> noMusicTextView.setVisibility(View.VISIBLE));
            } else {
                runOnUiThread(() -> noMusicTextView.setVisibility(View.GONE));

                // Ako ima pjesama u databazi, učitavamo jednu po jednu uz pomoć while loop-a
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry._ID)); // Učitavamo id pjesme
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry.COLUMN_TITLE)); // Učitavamo ime pjesme
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry.COLUMN_DATA)); // Učitavamo path pjesme u memoriji telefona
                    String duration = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry.COLUMN_DURATION)); // Učitavamo trajanje pjesme

                    // Sve stvari koje smo našli stavljamo u jedan SongModel,
                    // a taj somg model dodajemo u ArrayList koji se zove songsList
                    songsList.add(new SongModel(id, path, title, duration));
                }
            }

            // Ove stvari run-uju na UI thread
            runOnUiThread(() -> recyclerView.getAdapter().notifyDataSetChanged()); // Update-ijemo adapter ako je došlo do promjena
        });
    }


    // Naredne tri fukncije su zaslužne za permissions
    // Starije verzije android-a imaju drugačiji permission za čitanje memorije
    // Starije verzije koriste READ_EXTERNAL_STORAGE a novije READ_MEDIA_AUDIO
    // Mogućnost starijeg i novijeg permission-a omogućava da aplikacija radi na mnogo veći broj uređaja
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    boolean checkPermission() {

        // Provjeravamo dali je jedan od ovih permission-a odobren
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Tražimo permission
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    void requestPermissions() {

        // Zavisno od verzije android-a tražimo permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }


    // Ova funkcija gleda dali je permission odobren i na osnovu toga obavještava user-a
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Provjeravamo request code
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean permissionsGranted = true;

            // Provjeravamo dali je jedan od permission-a odobren. Jer različite verzije android-a imaju
            // različite permission request-ove
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted = false;
                    break;
                }
            }

            // Ako nijedan permission nije zadovoljen primazujemo poruku user-u i tražimo permission opet
            if (!permissionsGranted) {
                UtilsMain.showToast(MainActivity.this, "Permissions required!");
                requestPermissions();
            } else {
                // Ako je permission zadovoljen pokazujemo jedan dialog koji govori user-u da treba da resetuje aplikaciju
                new Handler().postDelayed(() -> UtilsMain.showAlertDialog(MainActivity.this, MainActivity.this), 100);
            }
        }
    }


    // Ova funkcija update-uje UI na nastavak (on resume) MainActivity-ja
    // ili je možemo pozvati kada nam treba UI update MainActivity-ja
    @Override
    protected void onResume() {
        super.onResume();

        // Sa ove dvije varijable postavljamo recycle view na mjesto posljednje puštane pjesme
        SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        int selectedPosition = preferences.getInt("selectedPosition", -1);

        // Ako pozicija nije (-1) radimo sledeće
        if (selectedPosition != -1) {
            recyclerView.scrollToPosition(selectedPosition); // Scroll-ujemo do pozicije zadnje puštane pjesme
            runOnUiThread(() -> recyclerView.setAdapter(new MainAdapter(MainActivity.this, cursor, myMediaPlayer))); // Postavimo novi adapter
            setResourcesWithMusic(myMediaPlayer.getCurrentSongId()); // Zovemo setResourcesWithMusic funkciju zbog update-ovanja mini music player-a
        }
    }
}