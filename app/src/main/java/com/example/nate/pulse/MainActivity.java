package com.example.nate.pulse;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static android.app.PendingIntent.getActivity;


public class MainActivity extends AppCompatActivity {

    static final int INTERPERSONAL = 1;
    static final int EMOTION = 2;
    static final int DISTRESS = 3;

    public Double heartRate = 300d;
    public Double heartSpeed = 300d;
    public Double slowFactor = 2d;
    public boolean question1Answered = true;
    public int module = 0;

    private Timer timer;
    private Timer timer2;
    public TextSwitcher topText;
    public TextSwitcher bottomText;
    public ScaleAnimation animation;
    public ScaleAnimation animationReturn;
    public TimerTask fadeQuestion1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        setContentView(R.layout.activity_main);
        ImageView imgView=(ImageView) findViewById(R.id.heart);
        Drawable drawable  = getResources().getDrawable(R.drawable.pink_heart);
        imgView.setImageDrawable(drawable);
        ImageView progressButton=(ImageView) findViewById(R.id.progress_button);
        Drawable progressDrawable  = getResources().getDrawable(R.mipmap.ic_assessment_white_24dp);
        progressButton.setImageDrawable(progressDrawable);
        topText = findViewById(R.id.topText);
        topText.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView myText = new TextView(MainActivity.this);
                myText.setGravity(Gravity.CENTER);
                return myText;
            }
        });
        topText.setInAnimation(context, android.R.anim.fade_in);
        topText.setOutAnimation(context, android.R.anim.fade_out);
        topText.setText("Start cooling down with some deep breathing.");

        bottomText = findViewById(R.id.bottomText);
        bottomText.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView myText = new TextView(MainActivity.this);
                myText.setGravity(Gravity.CENTER);
                return myText;
            }
        });
        bottomText.setInAnimation(context, android.R.anim.fade_in);
        bottomText.setOutAnimation(context, android.R.anim.fade_out);

        fadeQuestion1 = new TimerTask() {
            final LinearLayout question1 = findViewById(R.id.q1buttons);
            @Override
            public void run(){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        question1.animate().alpha(0);
                    }
                });
            }
        };

        fadeQuestion1 = new TimerTask() {
            final LinearLayout question1 = findViewById(R.id.q1buttons);
            @Override
            public void run(){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        question1.animate().alpha(0);
                    }
                });
            }
        };
    }

    public void growHeart(View view) {
        timer = new Timer();

        animation = new ScaleAnimation(1, 1.5f, 1, 1.5f,
                view.getWidth() / 2,view.getHeight() / 2 );
        animation.setFillEnabled(true);
        animationReturn = new ScaleAnimation(1.5f, 1f, 1.5f, 1,
                view.getWidth() / 2,view.getHeight() / 2 );
        animationReturn.setFillEnabled(true);

        final Runnable actuallyAnimate = new Runnable() {
            @Override
            public void run() {
                final ImageView heart = findViewById(R.id.heart);
                heart.clearAnimation();
                animation.setDuration(heartSpeed.longValue());
                heart.startAnimation(animation);
            }
        };

        final Runnable animateBack = new Runnable() {
            @Override
            public void run() {
                final ImageView heart = findViewById(R.id.heart);
                heart.clearAnimation();
                animationReturn.setDuration(heartSpeed.longValue());
                heart.startAnimation(animationReturn);
            }
        };

        final TimerTask animateTask = new TimerTask(){
            @Override
            public void run(){
                runOnUiThread(actuallyAnimate);
            }
        };

        final TimerTask animateBackwards = new TimerTask(){
            @Override
            public void run(){
                runOnUiThread(animateBack);
            }
        };


        timer.scheduleAtFixedRate(animateTask, 0, heartRate.longValue() * 2);
        timer.scheduleAtFixedRate(animateBackwards, heartRate.longValue(), heartRate.longValue() * 2);
        final Timer metaTimer = new Timer();

        class slowDownTask extends TimerTask {
            private Runnable runnable;
            slowDownTask(Runnable command){
                this.runnable = command;
            }
            @Override
            public void run(){
                this.runnable.run();
            }
        }

        Runnable slowDown = new Runnable() {
            @Override
            public void run() {
                if (timer != null){
                    timer.cancel();
                    timer.purge();
                }

                timer = new Timer();

                heartSpeed = heartSpeed * slowFactor;
                heartRate = heartRate * slowFactor;
                TimerTask subAnimateTask = new TimerTask(){
                    @Override
                    public void run(){
                        runOnUiThread(actuallyAnimate);
                    }
                };
                timer.scheduleAtFixedRate(subAnimateTask, 0, heartRate.longValue() * 2);
            }
        };

        Runnable slowDownBack = new Runnable() {
            @Override
            public void run() {
                if (timer2 != null){
                    timer2.cancel();
                    timer2.purge();
                }

                timer2 = new Timer();

                TimerTask subAnimateTaskBack = new TimerTask(){
                    @Override
                    public void run(){
                        runOnUiThread(animateBack);
                    }
                };
                timer2.scheduleAtFixedRate(subAnimateTaskBack, heartRate.longValue(), heartRate.longValue() * 2);
            }
        };

        metaTimer.schedule(new slowDownTask(slowDown), 4000);
        metaTimer.schedule(new slowDownTask(slowDownBack), 4000);

        TimerTask audioPlay = new TimerTask() {
            @Override
            public void run() {
                final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.synthswellextend);
                mp.start();
            }
        };
        metaTimer.schedule(audioPlay, 4000);
        metaTimer.schedule(new slowDownTask(slowDown), 10000);
        metaTimer.schedule(new slowDownTask(slowDownBack), 10000);

    }


    public void onHeartBegin(View view) {
        if (timer != null){
//            Only needs to run one time.
            return;
        }
        growHeart(view);
        Timer timer = new Timer();

        TimerTask question1Appear = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        question1Answered = false;
                        topText.setText("What seems to be bothering you today?");
                        Button button1 = findViewById(R.id.q1button1);
                        button1.animate().alpha(1.0f);
                        Button button2 = findViewById(R.id.q1button2);
                        button2.animate().alpha(1.0f);
                        Button button3 = findViewById(R.id.q1button3);
                        button3.animate().alpha(1.0f);
                    }
                });
            }
        };
        timer.schedule(question1Appear, 5000);
    }

    public void startTraining(){
        switch (module){
            case INTERPERSONAL:
                topText.setText(getResources().getText(R.string.interpersonalTop));
                bottomText.setVisibility(View.VISIBLE);
                bottomText.setText(getResources().getText(R.string.interpersonalBottom));
                break;

            case EMOTION:
                topText.setText(getResources().getText(R.string.emotionTop));
                bottomText.setVisibility(View.VISIBLE);
                bottomText.setText(getResources().getText(R.string.emotionBottom));
                break;

            case DISTRESS:
                topText.setText(getResources().getText(R.string.distressTop));
                bottomText.setVisibility(View.VISIBLE);
                bottomText.setText(getResources().getText(R.string.distressBottom));
                break;

            case 0:
                topText.setText("Oops, you shouldn't be seeing this!");
                break;
        }
        Timer finalTimer = new Timer();
        TimerTask finalStatement = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (module){
                            case INTERPERSONAL:
                                topText.setText(getResources().getText(R.string.interpersonal2));
                                bottomText.setText(getResources().getText(R.string.interpersonal3));
                                break;

                            case EMOTION:
                                topText.setText(getResources().getText(R.string.emotion2));
                                bottomText.setText(getResources().getText(R.string.emotion3));
                                break;

                            case DISTRESS:
                                topText.setText(getResources().getText(R.string.distress2));
                                bottomText.setText(getResources().getText(R.string.distress3));
                                break;
                        }
                    }
                });
            }
        };
        TimerTask finishButton = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Button finishButton = findViewById(R.id.finishButton);
                        finishButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        };

        finalTimer.schedule(finalStatement, 10000);
        finalTimer.schedule(finishButton, 15000);

    }

    public void interpersonal(View view) {
        // Only one button is clickable.
        if (question1Answered){
            return;
        }
        question1Answered = true;
        ((Button) view).setBackground(getDrawable(R.drawable.mybutton1dark));
        Button button2 = findViewById(R.id.q1button2);
        button2.animate().alpha(0f);
        Button button3 = findViewById(R.id.q1button3);
        button3.animate().alpha(0f);
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 100);
        animation.setDuration(600);
        animation.setFillAfter(true);
        view.startAnimation(animation);

        Timer timerNew = new Timer();
        timerNew.schedule(fadeQuestion1, 700);
        module = INTERPERSONAL;
        startTraining();
    }

    public void emotions(View view) {
        if (question1Answered){
            return;
        }
        question1Answered = true;
        ((Button) view).setBackground(getDrawable(R.drawable.mybutton2dark));
        Button button1 = findViewById(R.id.q1button1);
        button1.animate().alpha(0f);
        Button button3 = findViewById(R.id.q1button3);
        button3.animate().alpha(0f);
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -50);
        animation.setDuration(300);
        animation.setFillAfter(true);
        view.startAnimation(animation);

        Timer timerNew = new Timer();
        timerNew.schedule(fadeQuestion1, 400);
        module = EMOTION;
        startTraining();
    }

    public void distress(View view) {
        if (question1Answered){
            return;
        }
        question1Answered = true;
        ((Button) view).setBackground(getDrawable(R.drawable.mybutton3dark));
        Button button1 = findViewById(R.id.q1button1);
        button1.animate().alpha(0f);
        Button button2 = findViewById(R.id.q1button2);
        button2.animate().alpha(0f);
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -200);
        animation.setDuration(800);
        animation.setFillAfter(true);
        view.startAnimation(animation);

        Timer timerNew = new Timer();
        timerNew.schedule(fadeQuestion1, 900);
        module = DISTRESS;
        startTraining();
    }

    public void finishApp(View view) {
        finishAndRemoveTask();
    }

    public void progressClicked(View view) {
        Intent intent = new Intent(this, ProgressActivity.class);
        startActivity(intent);
    }
}
