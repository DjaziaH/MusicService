package com.example.musicservice;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class PlayMusicService extends Service {

    /* ***************************************************************************************************** */
    /* ******************************      Declaring variables    ****************************************** */
    /* ***************************************************************************************************** */

        private  MyReceiver myReceiver;
        static ArrayList<Song> listSong ;
        // Position for music when : Pause ;
        static int position = 0;
        MediaPlayer musicPlayer ;
        static int currrentMusicIndex = 0 ;
        static String currentAction = "play";


        Notification notification ;
        NotificationManager notificationManager;
        String channelId = "musicChannel";
        CharSequence channelName = "Music Channel";


        PendingIntent pendingIntent;
        PendingIntent play_pause_PendingIntent,nextPendingIntent,prevPendingIntent;

    /* ***************************************************************************************************** */


    public PlayMusicService() {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        listSong = new ArrayList<>();
        ReadMusic();

        myReceiver = new MyReceiver();
        registerReceiver(myReceiver,new IntentFilter("PlayPause"));
        registerReceiver(myReceiver,new IntentFilter("Next"));
        registerReceiver(myReceiver,new IntentFilter("Previous"));

        musicPlayer = new MediaPlayer();
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //intent du clique notif
        Intent notificationIntent = new Intent(this, MainActivity.class);

        pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        // intent du clique bouton PlayPause
        play_pause_PendingIntent = PendingIntent.getBroadcast(this, 0, new
                Intent("PlayPause"), FLAG_UPDATE_CURRENT);

        // intent du clique bouton next
        nextPendingIntent = PendingIntent.getBroadcast(this, 0, new
                Intent("Next"), FLAG_UPDATE_CURRENT);

        // intent du clique bouton Previous
        prevPendingIntent = PendingIntent.getBroadcast(this, 0, new
                Intent("Previous"), FLAG_UPDATE_CURRENT);


        // Notification Channel
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int importance = NotificationManager.IMPORTANCE_HIGH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new
                    NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notification =   new NotificationCompat.Builder(this, channelId)
                    .setContentTitle("Lecture en cours")
                    .setSmallIcon(R.drawable.music__)
                    .setContentText(listSong.get(currrentMusicIndex).getTitle())
                    .addAction(R.drawable.next, "Previous", nextPendingIntent)
                    .addAction(R.drawable.play, "Play/Pause", play_pause_PendingIntent)
                    .addAction(R.drawable.next, "Next", nextPendingIntent)
                    .setContentIntent(pendingIntent)
                    .build();
        startForeground(10, notification);

        switch (currentAction){
            case "Pause":
                PauseMusic();
                break;
            case "Play":
                PlayMusic();
                break;
            case "Next":
                PlayNextMusic();
                break;
            case "Previous":
                PlayPrevMusic();
                break;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(musicPlayer.isPlaying()) musicPlayer.stop();
        unregisterReceiver(myReceiver);
    }


    /* ***************************************************************************************************** */
    /* *************************************   MyReceiver Class :    *************************************** */
    /* ***************************************************************************************************** */

    public class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action){
                case "PlayPause":
                    if(musicPlayer.isPlaying())
                        PauseMusic();
                    else
                        PlayMusic();
                break;
                case "Next":
                    PlayNextMusic();
                break;
                case "Previous":
                    PlayPrevMusic();
                break;
            }

        }
    }

    /* ***************************************************************************************************** */


    /* ***************************************************************************************************** */
    /* ******************************      ReadMusic Method :     ****************************************** */
    /* ***************************************************************************************************** */

    public void ReadMusic(){
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor songCursor = contentResolver.query(songUri,null,null,null,null);

        if(songCursor!=null && songCursor.moveToFirst()){

            int indexTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int indexArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int indexPath = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do{
                String title = songCursor.getString(indexTitle);
                String artist = songCursor.getString(indexArtist);
                String path = songCursor.getString(indexPath);

                Song song = new Song(title,artist,path);

                listSong.add(song);

            }while (songCursor.moveToNext());
        }
     }

    /* ***************************************************************************************************** */


    /* ***************************************************************************************************** */
    /* ****************************   Play Pause Next Prev Methods :  ************************************** */
    /* ***************************************************************************************************** */

    public void PlayMusic(){
        try {
            UpdateNotif();
            musicPlayer = new MediaPlayer();
            musicPlayer.setDataSource(listSong.get(currrentMusicIndex).getPath());
            musicPlayer.prepare();
            musicPlayer.seekTo(position);
            musicPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void PauseMusic(){
        musicPlayer.pause();
        position = musicPlayer.getCurrentPosition();
    }

    public  void PlayNextMusic(){
        currrentMusicIndex++;
        musicPlayer.stop();
        position = 0 ;
        PlayMusic();
    }

    public  void PlayPrevMusic(){
        currrentMusicIndex--;
        musicPlayer.stop();
        position = 0 ;
        PlayMusic();
    }

    /* ***************************************************************************************************** */



    /* ***************************************************************************************************** */
    /* ******************************   UpdateNotif  Method  :  ******************************************** */
    /* ***************************************************************************************************** */

    public void UpdateNotif(){
        if(currrentMusicIndex == 0 ){
            notification =   new NotificationCompat.Builder(this, channelId)
                    .setContentTitle("Lecture en cours")
                    .setSmallIcon(R.drawable.music__)
                    .setContentText(listSong.get(currrentMusicIndex).getTitle())
                    .addAction(R.drawable.play, "Play/Pause", play_pause_PendingIntent)
                    .addAction(R.drawable.next, "Next", nextPendingIntent)
                    .setContentIntent(pendingIntent)
                    .build();
        }else{
            if(currrentMusicIndex == listSong.size()-1){
                notification =   new NotificationCompat.Builder(this, channelId)
                        .setContentTitle("Lecture en cours")
                        .setSmallIcon(R.drawable.music__)
                        .setContentText(listSong.get(currrentMusicIndex).getTitle())
                        .addAction(R.drawable.previous, "Previous", prevPendingIntent)
                        .addAction(R.drawable.play, "Play/Pause", play_pause_PendingIntent)
                        .setContentIntent(pendingIntent)
                        .build();
            }else{
                notification =   new NotificationCompat.Builder(this, channelId)
                        .setContentTitle("Lecture en cours")
                        .setSmallIcon(R.drawable.music__)
                        .setContentText(listSong.get(currrentMusicIndex).getTitle())
                        .addAction(R.drawable.previous, "Previous", prevPendingIntent)
                        .addAction(R.drawable.play, "Play/Pause", play_pause_PendingIntent)
                        .addAction(R.drawable.next, "Next", nextPendingIntent)
                        .setContentIntent(pendingIntent)
                        .build();
            }
        }

        notificationManager.notify(10, notification);
     }

    /* ***************************************************************************************************** */
}
