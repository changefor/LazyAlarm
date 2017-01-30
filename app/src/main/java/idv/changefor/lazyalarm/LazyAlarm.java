package idv.changefor.lazyalarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.AlarmClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

public class LazyAlarm extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "ALRMSetting";
    private TextView rtPickerResult;
    private EditText alrmTime;
    private Uri rtURI;
    private int ready;
    private int delay;
    private String tempUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lazy_alarm);

        alrmTime = (EditText) findViewById(R.id.alarmtime);

        Button rtPicker = (Button) findViewById(R.id.rtpicker);
        rtPicker.setOnClickListener(this);

        rtPickerResult = (TextView) findViewById(R.id.rtresult);

        Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(this);


        SharedPreferences settings = getSharedPreferences("LAZYALARM", 0);
        ready = settings.getInt("READY", -1);
        delay = settings.getInt("DELAY", -1);

        if(ready!=-1) {
            tempUri = settings.getString("URI", null);
            startLazyAlarm();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.rtpicker:
                getAlarm();
                break;
            case R.id.save:
                startLazyAlarm();

                this.finish();
                break;
        }
    }

    private void startLazyAlarm() {
        setAlarm();
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        goHome();
    }

    private void goHome() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(TAG,"go to home!!");
        this.startActivity(startMain);
    }

    private void setAlarm() {
        Intent alarm = new Intent(AlarmClock.ACTION_SET_ALARM);
        if(delay==-1) {
            delay = Integer.valueOf(alrmTime.getText().toString());
            if(delay>60) delay = 60;
        }
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        minute += delay;
        if(minute >= 60) {
            minute -= 60;
            hour++;
        }
        if(hour >=24) hour-=24;

        alarm.putExtra(AlarmClock.EXTRA_MESSAGE, "My Alarm");
        alarm.putExtra(AlarmClock.EXTRA_HOUR, hour);
        alarm.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        if(tempUri!=null) {
            rtURI = Uri.parse(tempUri);
            alarm.putExtra(AlarmClock.EXTRA_RINGTONE, rtURI);
        }
        alarm.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        Log.d(TAG,"set Alarm!!");
        this.startActivity(alarm);

        SharedPreferences settings = getSharedPreferences ("LAZYALARM", 0);
        SharedPreferences.Editor PE = settings.edit();
        PE.putInt("READY",1);
        PE.putInt("DELAY", delay);
        PE.putString("URI", rtURI.toString());
        PE.commit();
    }

    public void getAlarm(){
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        this.startActivityForResult(intent, 5);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "ok");
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                tempUri = uri.toString();
                rtURI = uri;
                Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                String title = ringtone.getTitle(this);
                rtPickerResult.setText(title);
                Log.d(TAG, "the new alarm sound is " + tempUri);
            }
        } else {
            Log.d(TAG, "Not ok");
        }
    }
}
