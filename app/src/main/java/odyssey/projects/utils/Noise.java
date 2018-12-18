package odyssey.projects.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

import odyssey.projects.sav.driver.R;

public final class Noise {

    private Context context;
    private SoundPool mSoundPool;
    private int mSoundId;

    public Noise(Context context){
        initNoises(context);
    }

    private void initNoises(Context context){
        this.context = context;
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
        mSoundId = mSoundPool.load(context, R.raw.bossdeath, 1);
    }

    /* Проиграть предустановленный аудиофайл. */
    public void playSound(){

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        /*
        float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        float leftVolume = curVolume / maxVolume;
        float rightVolume = curVolume / maxVolume;
        */

        float leftVolume  = 1; // максимальная громкость.
        float rightVolume = 1;

        if (mSoundId > 0 && mSoundPool != null){
            mSoundPool.play(
                    mSoundId,     // идентификатор звука.
                    leftVolume,   // громкость левого динамика.
                    rightVolume,  // громкость правого динамика.
                    1,    // приоритет.
                    1,      // без повторов.
                    1f);    // скорость воспроизведения.
        }
    }

    // Делает непродолжительную фибрацию в качестве уведомления об успешной отметке.
    public void doVibro(){
        //                       V         V         V         V         V
        long[] pattern = { 500, 800, 300, 800, 300, 300, 300, 300, 300, 1000 };

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(pattern, -1); // -1 - без повторений.
        }
    }
}
