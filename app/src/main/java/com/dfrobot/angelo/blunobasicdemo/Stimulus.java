package com.dfrobot.angelo.blunobasicdemo;


import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.provider.MediaStore;

/**
 * Created by simonperrault on 10/8/16.
 */

public class Stimulus implements SoundPool.OnLoadCompleteListener {

    public static final int[] stimuli = { R.raw.s1, R.raw.s2, R.raw.s3 };
    private static SoundPool soundPool;
    private Context mContext;
    private MediaPlayer mp = null;

    // Make sure to match stimuli size and this value
    public static final int NB_STIMULI = 5;

    public Stimulus (Context _context)
    {
        this.mContext = _context;
    }

    /*
    public void initializeStimulus()
    {
        this.mp = MediaPlayer.create(this.mContext, stimuli[this.stimulus]);
    }
    */

    /*
    public void playStimulus()
    {
        if (mp != null)
        {
            this.mp.start();
            this.mp.release();
            this.mp = null;
        }
    }
    */

    public void initSounds()
    {
        SoundPool.Builder buildMe = new SoundPool.Builder();
        buildMe.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build());
        soundPool = buildMe.build();
        this.soundPool.setOnLoadCompleteListener(this);
        for (int i = 0; i < stimuli.length; i++)
        {
            soundPool.load(this.mContext, stimuli[i], i);
        }
    }

    public void playSound(int stimulus)
    {

        if (soundPool == null)
        {
            this.initSounds();
        }
        float volume = 1.0f;
        soundPool.play(stimulus+1, volume, volume, 1, 0, 1f);

    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int i, int i1) {
        System.out.println("Finished loading sounds biatch. Sample: " + i + ", status: " + i1);
    }
}
