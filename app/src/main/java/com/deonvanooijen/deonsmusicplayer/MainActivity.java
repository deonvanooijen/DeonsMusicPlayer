package com.deonvanooijen.deonsmusicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    RecyclerView recyclerView;
    TextView noMusicTextView;
    ArrayList<AudioModel> songsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        noMusicTextView = findViewById(R.id.no_songs_text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!checkPermission()) {
                requestPermission();
            } else {
                loadAudioFiles();
            }
        }
    }

    private void loadAudioFiles() {
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
        while (true) {
            assert cursor != null;
            if (!cursor.moveToNext()) break;
            AudioModel songData = new AudioModel(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            if (new File(songData.path).exists())
                songsList.add(songData);
        }

        if (songsList.size() == 0) {
            noMusicTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            MusicListAdapter adapter = new MusicListAdapter(songsList, getApplicationContext());
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new MusicListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    startMusicPlayerActivity(position);
                }
            });
        }
    }

    private void startMusicPlayerActivity(int position) {
        Intent intent = new Intent(MainActivity.this, MusicPlayerActivity.class);
        intent.putExtra("LIST", songsList);
        intent.putExtra("POSITION", position);
        intent.putExtra("CURRENT_INDEX", MyMediaPlayer.currentIndex);

        startActivity(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_MEDIA_AUDIO)) {
            Toast.makeText(MainActivity.this, "STORAGE PERMISSION IS REQUIRED, PLEASE ALLOW FROM SETTINGS.", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAudioFiles();
            } else {
                Toast.makeText(this, "Permission denied. You won't be able to access audio files.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}