package com.example.musicservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /* ***************************************************************************************************** */
    /* ******************************      Declaring variables    ****************************************** */
    /* ***************************************************************************************************** */

    ImageView img,start,pause,next,prev ;
    final static int REQUEST_PERMISSION = 99 ;

    /* ***************************************************************************************************** */


    /* ***************************************************************************************************** */
    /* ***************************         onCreate Method :           ************************************* */
    /* ***************************************************************************************************** */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /* ***************************         Initializing var:           ************************************* */

        img = findViewById(R.id.img);
        img.setImageResource(R.drawable.music__);

        start = findViewById(R.id.start);
        pause = findViewById(R.id.pause);
        next  = findViewById(R.id.next);
        prev  = findViewById(R.id.prev);

        start.setImageResource(R.drawable.play);
        pause.setImageResource(R.drawable.pause);
        next.setImageResource(R.drawable.next);
        prev.setImageResource(R.drawable.previous);

        pause.setVisibility(View.GONE);
        next.setVisibility(View.GONE);
        prev.setVisibility(View.GONE);

        /* ***************************************************************************************************** */


        /* ***************************************************************************************************** */

        /* ***************************    GetReadMusicPermission :         ************************************ */
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_PERMISSION);
        }else{
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlayMusic();
                }
            });
        }
        /* ***************************************************************************************************** */

        /* ***************************    Set OnCliskListeners   :         ************************************ */

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PauseMusic();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayNextMusic();
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayPrevMusic();
            }
        });
    }

    /* ***************************************************************************************************** */


    /* ***************************************************************************************************** */
    /* ***************************   onRequestPermissionResult Method :    ********************************* */
    /* ***************************************************************************************************** */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PlayMusic();
                    }
                });
            }
        }
    }

    /* ***************************************************************************************************** */


    /* ***************************************************************************************************** */
    /* ******************************  Play Pause Next Prev Methods : ************************************** */
    /* ***************************************************************************************************** */

    public void PlayMusic(){

            start.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);
            prev.setVisibility(View.VISIBLE);


            PlayMusicService.currentAction = "Play";
            startService(new Intent(getApplicationContext(),PlayMusicService.class));

            /*
            if(PlayMusicService.currrentMusicIndex == 0 )
                    prev.setVisibility(View.GONE);

            if(PlayMusicService.currrentMusicIndex == PlayMusicService.listSong.size()-1)
                next.setVisibility(View.GONE);
            */
    }

    public void PauseMusic(){

        start.setVisibility(View.VISIBLE);
        pause.setVisibility(View.GONE);
        next.setVisibility(View.VISIBLE);
        prev.setVisibility(View.VISIBLE);

        stopService(new Intent(getApplicationContext(),PlayMusicService.class));
        PlayMusicService.currentAction = "Pause";
        startService(new Intent(getApplicationContext(),PlayMusicService.class));

        /*
        if(PlayMusicService.currrentMusicIndex == 0 )
            prev.setVisibility(View.GONE);
        if(PlayMusicService.currrentMusicIndex == PlayMusicService.listSong.size()-1)
            next.setVisibility(View.GONE);
        */

    }

    public  void PlayNextMusic(){
            int index = PlayMusicService.currrentMusicIndex++;
            stopService(new Intent(getApplicationContext(),PlayMusicService.class));
            PlayMusicService.currentAction = "Next" ;
            PlayMusicService.currrentMusicIndex = index;
            startService(new Intent(getApplicationContext(),PlayMusicService.class));

    }

    public  void PlayPrevMusic(){
            int index = PlayMusicService.currrentMusicIndex--;
            stopService(new Intent(getApplicationContext(),PlayMusicService.class));
            PlayMusicService.currentAction = "Previous";
            PlayMusicService.currrentMusicIndex = index ;
            startService(new Intent(getApplicationContext(),PlayMusicService.class));
    }

    /* ***************************************************************************************************** */

}