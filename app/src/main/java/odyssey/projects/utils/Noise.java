package odyssey.projects.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

import odyssey.projects.sav.driver.R;

public final class Noise {

    private Noise(){}

    /* Проиграть предустановленный аудиофайл. */
    public static void playSound(Context context){
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        final SoundPool sSoundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();

        final int mSoundId = sSoundPool.load(context, R.raw.bossdeath, 1);

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        final float leftVolume = curVolume / maxVolume;
        final float rightVolume = curVolume / maxVolume;

        sSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (mSoundId > 0){
                    sSoundPool.play(
                            mSoundId,     // идентификатор звука.
                            leftVolume,   // громкость левого динамика.
                            rightVolume,  // громкость правого динамика.
                            1,    // приоритет.
                            1,      // без повторов.
                            1f);    // скорость воспроизведения.
                }
            }
        });
    }

    // Делает непродолжительную фибрацию в качестве уведомления об успешной отметке.
    public static void doVibro(Context context){
        //                       V         V         V         V         V
        long[] pattern = { 500, 800, 300, 800, 300, 300, 300, 300, 300, 1000 };

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(pattern, -1); // -1 - без повторений.
        }
    }
}
