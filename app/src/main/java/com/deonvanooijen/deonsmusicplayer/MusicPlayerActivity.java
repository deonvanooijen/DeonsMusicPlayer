package com.deonvanooijen.deonsmusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MusicPlayerActivity extends AppCompatActivity {

    TextView titleTv, currentTimeTv, totalTimeTv;
    SeekBar seekBar;
    ImageView pausePlay, nextBtn, previousBtn, musicIcon, repeatButton, list_button;
    ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    int x = 0;
    private static final int PERMISSION_REQUEST_CODE = 123;

    private boolean isRepeatOn = false;

    private boolean isSongPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        titleTv = findViewById(R.id.song_title);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_big);
        repeatButton = findViewById(R.id.repeatButton);
        list_button = findViewById(R.id.list_button);

        titleTv.setSelected(true);

        songsList = (ArrayList<AudioModel>) getIntent().getSerializableExtra("LIST");
        int position = getIntent().getIntExtra("POSITION", -1);
        int currentIndex = getIntent().getIntExtra("CURRENT_INDEX", -1);

        setResourcesWithMusic(position, currentIndex);

        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTv.setText(convertToMMSS(mediaPlayer.getCurrentPosition() + ""));

                    if (mediaPlayer.isPlaying()) {
                        pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                        musicIcon.setRotation(x++);
                    } else {
                        pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                        musicIcon.setRotation(0);
                    }
                }
                new Handler().postDelayed(this, 100);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        repeatButton.setOnClickListener(v -> toggleRepeat());

        list_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicPlayerActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        pausePlay.setOnClickListener(v -> pausePlay());
        nextBtn.setOnClickListener(v -> playNextSong());
        previousBtn.setOnClickListener(v -> playPreviousSong());
    }

    void setResourcesWithMusic(int position, int currentIndex) {
        if (position != -1) {

            if (position == currentIndex && mediaPlayer.isPlaying()) {
                currentSong = songsList.get(currentIndex);
                updateUI();
            } else {
                currentSong = songsList.get(position);
                playMusic(currentSong);
            }
        } else {
            currentSong = songsList.get(currentIndex);
            updateUI();

            if (!isSongPlaying) {
                playMusic(currentSong);
                isSongPlaying = true;
            }
        }
    }

    private void playMusic(AudioModel song) {
        MyMediaPlayer.currentIndex = songsList.indexOf(song);

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(song.path);
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
            updateUI();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playNextSong() {
        if (MyMediaPlayer.currentIndex == songsList.size() - 1) {
            if (isRepeatOn) {
                MyMediaPlayer.currentIndex = 0;
            } else {
                Toast.makeText(this, "End of the playlist", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            MyMediaPlayer.currentIndex += 1;
        }
        currentSong = songsList.get(MyMediaPlayer.currentIndex);
        mediaPlayer.reset();
        setResourcesWithMusic(-1, MyMediaPlayer.currentIndex);
    }

    private void playPreviousSong() {
        if (MyMediaPlayer.currentIndex == 0) {
            if (isRepeatOn) {
                MyMediaPlayer.currentIndex = songsList.size() - 1;
            } else {
                Toast.makeText(this, "Beginning of the playlist", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            MyMediaPlayer.currentIndex -= 1;
        }
        currentSong = songsList.get(MyMediaPlayer.currentIndex);
        mediaPlayer.reset();
        setResourcesWithMusic(-1, MyMediaPlayer.currentIndex);
    }

    private void pausePlay() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();
        updateUI();
    }

    private void toggleRepeat() {
        isRepeatOn = !isRepeatOn;
        if (isRepeatOn) {
            repeatButton.setImageResource(R.drawable.button_repeat_on);
        } else {
            repeatButton.setImageResource(R.drawable.button_repeat);
        }
        mediaPlayer.setLooping(isRepeatOn);
    }

    private void updateUI() {
        titleTv.setText(currentSong.title);
        totalTimeTv.setText(convertToMMSS(currentSong.duration));
    }

    public static String convertToMMSS(String duration) {
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }
}