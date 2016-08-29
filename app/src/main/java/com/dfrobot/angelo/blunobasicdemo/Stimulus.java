package com.dfrobot.angelo.blunobasicdemo;


import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by simonperrault on 10/8/16.
 */

public class Stimulus extends Thread implements SoundPool.OnLoadCompleteListener  {

    /* To remove a stimulus, remove R.raw.sX from the array soundStimuli
        Remove its number from the stimuli array as well
        Change the value of NB_STIMULI
     */

    public static final int[] soundStimuli = { R.raw.s1, R.raw.s2, R.raw.s3, R.raw.s4, R.raw.s5, R.raw.s6, R.raw.s7, R.raw.s8, R.raw.s9, R.raw.s10, R.raw.s12 };
    public static final int[] stimuli = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    // Make sure to match soundStimuli size and this value
    public static final int NB_STIMULI = 11;
    public static final int NB_REPETITIONS = 1;

    public static final int SLEEP_DELAY = 20000;
    public static final int INTERTRIAL_DELAY = 2500;

    private int lastStream = -1;

    public static List<Integer> trials = new ArrayList<Integer>();

    private static SoundPool soundPool;
    private MainActivity mContext;
    private MediaPlayer mp = null;

    private int currentStimulus = -1;
    private int stimRepeat = 0;


    private boolean unlock = false;
    private int selected = -1;
    private int trialNumber = 0;

    private boolean logFalsePositives = false;

    public Stimulus (MainActivity _context)
    {

        this.mContext = _context;
        for (int i = 0; i < NB_REPETITIONS; i++)
        {
            for (int j=0; j < stimuli.length; j++)
            {
                trials.add(stimuli[j]);
            }
        }
        Collections.shuffle(trials);
    }

    /*
    public void initializeStimulus()
    {
        this.mp = MediaPlayer.create(this.mContext, soundStimuli[this.stimulus]);
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
        for (int i = 0; i < soundStimuli.length; i++)
        {
            soundPool.load(this.mContext, soundStimuli[i], i);
        }
        soundPool.load(this.mContext, R.raw.correct, this.NB_STIMULI+1);
        soundPool.load(this.mContext, R.raw.incorrect, this.NB_STIMULI+2);
        soundPool.load(this.mContext, R.raw.end, this.NB_STIMULI+3);
    }

    public synchronized void playSound(int stimulus)
    {

        if (soundPool == null)
        {
            this.initSounds();
        }
        float volume = 1.0f;
        this.stopPreviousStream();
        this.lastStream = soundPool.play(stimuli[stimulus - 1], volume, volume, 1, 0, 1f);

    }

    public synchronized void playFeedback(boolean b)
    {
        int index = this.NB_STIMULI+1;
        index += (b?0:1);
        if (soundPool == null)
        {
            this.initSounds();
        }
        float volume = 1.0f;
        this.stopPreviousStream();
        this.lastStream = soundPool.play(index, volume, volume, 1, 0, 1f);

    }

    public void playEndExperiment()
    {
        if (soundPool == null)
        {
            this.initSounds();
        }
        float volume = 1.0f;
        this.stopPreviousStream();
        this.lastStream = soundPool.play(this.NB_STIMULI+3, volume, volume, 1, 0, 1f);
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int i, int i1) {
        System.out.println("Loaded: " + i + ", status: " + i1);
    }

    @Override
    public void run()
    {

        try {
            Thread.sleep(SLEEP_DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Start block
        if (!this.logFalsePositives) {
            for (Integer itg : trials) {
                this.mContext.setTrials(true);
                this.stimRepeat = 0;
                int i = itg.intValue();
                System.out.println("Stimulus:" + i);
                this.trialNumber++;
                this.currentStimulus = i;
                this.mContext.newTimeStamp();
                this.playSound(i);

                long now = System.currentTimeMillis();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (!this.unlock) {
                /*long delay = System.currentTimeMillis() - now;
                //If no answer within 3000 ms, repeat
                if (delay >= 3000)
                {
                    now = delay;
                    this.playSound(i);
                    this.stimRepeat++;
                }*/
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                this.mContext.setTrials(false);
                this.unlock = false;
                System.out.println("Recognized: " + this.selected);
                this.playFeedback(this.selected == i);
                try {
                    Thread.sleep(INTERTRIAL_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            this.mContext.setTrials(true);
            for (int i=0; i<3500; i++) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.mContext.setTrials(false);
        }
        this.playEndExperiment();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.mContext.endExperiment();


    }

    public void unlockThread(int _selected)
    {
        this.selected = _selected;
        this.unlock = true;

    }

    public int getCurrentStimulus()
    {
        return this.currentStimulus;
    }

    public int getStimRepeat()
    {
        return this.stimRepeat;
    }

    public int getTrialNumber()
    {
        return this.trialNumber;
    }

    public void stopPreviousStream()
    {
        if (this.lastStream != -1)
        {
            soundPool.stop(this.lastStream);
        }
    }

    public void setFalsePositives() {
        this.logFalsePositives = true;
    }
}
