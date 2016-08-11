package com.dfrobot.angelo.blunobasicdemo;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class MainActivity  extends BlunoLibrary {
	private Button buttonScan;
    private Button launchExperiment;
    private Button stopExperiment;
	private String receivedString;
	private String participant;
	private PrintWriter pw_biodata = null;
	private PrintWriter pw_stimuli = null;

    private boolean experimentOn = false;
    private boolean connectionOK = false;
    private boolean filesOK = false;

    private Stimulus soundStimulus;

    private long timestamp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        this.soundStimulus = new Stimulus(this);
        this.soundStimulus.initSounds();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        onCreateProcess();														//onCreate Process by BlunoLibrary


        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200

	// Send text to arduino
		// 			serialSend(serialSendText.getText().toString());				//send the data to the BLUNO

        buttonScan = (Button) findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
        this.launchExperiment = (Button) findViewById(R.id.launchExperiment);
        this.launchExperiment.setVisibility(View.INVISIBLE);
        this.stopExperiment = (Button) findViewById(R.id.endExperiment);
        this.stopExperiment.setVisibility(View.INVISIBLE);


	}

	protected void onResume(){
		super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess();														//onResume Process by BlunoLibrary
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }
	
	protected void onStop() {
		super.onStop();
		onStopProcess();														//onStop Process by BlunoLibrary
	}
    
	@Override
    protected void onDestroy() {
        super.onDestroy();	
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

	@Override
	public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
		switch (theConnectionState) {											//Four connection state
		case isConnected:
			buttonScan.setText("Connected");
            this.updateLaunchExperiment(true, this.filesOK);
			break;
		case isConnecting:
			buttonScan.setText("Connecting");
            this.updateLaunchExperiment(false, this.filesOK);
			break;
		case isToScan:
			buttonScan.setText("Scan");
            this.updateLaunchExperiment(false, this.filesOK);
			break;
		case isScanning:
			buttonScan.setText("Scanning");
            this.updateLaunchExperiment(false, this.filesOK);
			break;
		case isDisconnecting:
			buttonScan.setText("isDisconnecting");
            this.updateLaunchExperiment(false, this.filesOK);
			break;
		default:
			break;
		}
	}

	@Override
	public void onSerialReceived(String theString) {                            //Once connection data received, this function will be called
        // TODO Auto-generated method stub
        receivedString = theString;
        this.logArduino(theString);
    }


	public void buttonScanClick(View view)
	{
		buttonScanOnClickProcess();
	}

	public void createLogFiles(View view)
	{
        EditText participant_id = (EditText) findViewById(R.id.participantID);
        this.participant = participant_id.getText().toString();
        if (!this.participant.isEmpty()) {
            String path = Environment.getExternalStorageDirectory().getPath();

            File file = new File(path, this.participant + "_stimuli.csv");
            File file2 = new File(path, this.participant + "_biodata.csv");
            if (file.exists()) {
                file.delete();
                System.out.println("Deleted stimulus file");
            }
            if (file2.exists()) {
                file2.delete();
                System.out.println("Deleted biodata file");
            }

            try {
                this.pw_stimuli = new PrintWriter(new FileOutputStream(file));

                this.pw_stimuli.println("Time,Stimulus,Recognized,Correct");
                this.pw_stimuli.flush();
                System.out.println("Created new stimulus file");
                this.pw_biodata = new PrintWriter(new FileOutputStream(file2));
                this.pw_biodata.println("Time,Sensor,Value");
                this.pw_biodata.flush();
                System.out.println("Created new biodata file");
                this.updateLaunchExperiment(this.connectionOK, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

    public void updateLaunchExperiment(boolean b1, boolean b2)
    {
        this.connectionOK = b1;
        this.filesOK = b2;
        if (this.filesOK && this.connectionOK)
        {
            this.launchExperiment.setVisibility(View.VISIBLE);
        }
        else
        {
            this.launchExperiment.setVisibility(View.INVISIBLE);
        }

    }

    public void launchExperiment(View view)
    {
        System.out.println("Let's launch it!");
        this.experimentOn = true;
        this.timestamp = System.currentTimeMillis();
        this.stopExperiment.setVisibility(View.VISIBLE);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.soundStimulus.playSound(0);
    }

    public void logArduino (String theString)
    {
        long now = System.currentTimeMillis();
        if (this.experimentOn) {
            double tstp = (now - this.timestamp) / 1000d;
            this.pw_stimuli.println(tstp + "," + theString);
            this.pw_stimuli.flush();
        }
    }

    public void endExperiment(View view)
    {
        this.pw_stimuli.println("End of experiment!");
        this.pw_stimuli.flush();
        this.pw_stimuli.close();
        this.pw_biodata.close();
        System.exit(0);
    }

}