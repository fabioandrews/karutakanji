package com.karutakanji;


import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


import com.google.example.games.basegameutils.BaseGameActivity;
import com.karutakanji.BackgroundSoundService.BackgroundSoundServiceBinder;

public abstract class ActivityDoJogoComSom extends BaseGameActivity{

	private SoundPool soundPool;
	private int soundIds[] = new int[10];//array com todos os SFXs da tela, limitado a 10 SFXs
	private boolean soundPoolLoaded;
	private boolean activityEstahAmarradaComServiceMusicaDeFundo = false;//a activity estah conectada com o service que roda musica de fundo ou nao
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			
			soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId,
						int status) {
					soundPoolLoaded = true;
				}
			});
			soundIds = new int[10];
			//adicionar os sfxs do seu jogo
			soundIds[0] = soundPool.load(getApplicationContext(), R.raw.correct_sound, 1);
			soundIds[1] = soundPool.load(getApplicationContext(), R.raw.cartoon_hop, 1);
			//soundIds[2] = soundPool.load(getApplicationContext(), R.raw.small_group_of_american_children_shout_hooray_, 1);
			soundIds[2] = soundPool.load(getApplicationContext(), R.raw.single_lightning_bolt, 1);
			soundIds[3] =soundPool.load(getApplicationContext(), R.raw.tick_tock_sound_effect_from_www_soundbyter_com, 1);
			soundIds[4] = soundPool.load(getApplicationContext(), R.raw.magic_spell_trick_sound_001, 1);		
			soundIds[5] = soundPool.load(getApplicationContext(), R.raw.nice_playing_harp, 1);
			soundIds[6] = soundPool.load(getApplicationContext(), R.raw.glass_ping, 1);
			soundIds[7] = soundPool.load(getApplicationContext(), R.raw.quick_buzzer_like_a_game_show_wrong_answer_, 1);
			soundIds[8] = soundPool.load(getApplicationContext(), R.raw.science_fiction_laser_gun_or_beam_fire_version_4, 1);
			
			
			//embaixo, bote os seus botoes q vc quer que o usuario possa clicar e lancar o sfx
			//LEMBRAR: CHAMAR SUPER.ONCREATE() SÓ DEPOIS DE SETCONTENTVIEW NA SUBCLASSE!
			
		}
		catch(Exception exc)
		{
			//Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
		}
		
	}
	
	
	 
	public void reproduzirSfx(String qualEfeitoSonoro) 
	{
		if(qualEfeitoSonoro.compareTo("acertou_carta") == 0)
		{
			//usuario acertou uma carta
			this.tocarSom(soundIds[0]);
			
		}
		else if(qualEfeitoSonoro.compareTo("errou_carta") == 0)
		{
			//usuario errou uma carta
			this.tocarSom(soundIds[1]);
		}
		else if(qualEfeitoSonoro.compareTo("trovao") == 0)
		{
			//algum usuario usou o item do trovao aleatorio ou trovao acerta uma carta
			this.tocarSom(soundIds[2]);
		}
		else if(qualEfeitoSonoro.compareTo("parar_tempo") == 0)
		{
			this.tocarSom(soundIds[3]);
		}
		else if(qualEfeitoSonoro.compareTo("mudar_dica") == 0)
		{
			this.tocarSom(soundIds[4]);
		}
		else if(qualEfeitoSonoro.compareTo("reviver_carta") == 0)
		{
			this.tocarSom(soundIds[5]);
		}
		else if(qualEfeitoSonoro.compareTo("dois_x") == 0)
		{
			this.tocarSom(soundIds[6]);
		}
		else if(qualEfeitoSonoro.compareTo("nao_espere_mais") == 0)
		{
			this.tocarSom(soundIds[7]);
		}
		else if(qualEfeitoSonoro.compareTo("cartas_douradas") == 0)
		{
			this.tocarSom(soundIds[8]);
		}
	}
	
	
	
	private void tocarSom(int idSom)
	{
		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		float actualVolume = (float) audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxVolume = (float) audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = actualVolume / maxVolume;
		// Is the sound loaded already?
		if (this.soundPoolLoaded == true) {
			
			soundPool.play(idSom, 1, 1, 9, 0, 1f);
			Log.e("Test", "Played sound");
		}
	}
	
	
	@Override
	public abstract void onSignInFailed();

	@Override
	public abstract void onSignInSucceeded();
	
	
	/**
	 * REFERENTE A BACKGROUND MUSIC
	 */
	
	private BackgroundSoundService servicoFazMusicaDeFundo;//servico que faz a musica de fundo do jogo
	
	
	public void mudarMusicaDeFundo(int idMusicaDeFundo)
	{
		 if (activityEstahAmarradaComServiceMusicaDeFundo == true && servicoFazMusicaDeFundo != null) {
			 
			 this.servicoFazMusicaDeFundo.mudarMusica(idMusicaDeFundo);
		 }
	}
	
	@Override
	protected void onPause()
	{
		//TocadorMusicaBackground.getInstance().pausarTocadorMusica();
		
		
		Context context = getApplicationContext();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        if (!taskInfo.isEmpty()) {
          ComponentName topActivity = taskInfo.get(0).topActivity; 
          if (!topActivity.getPackageName().equals(context.getPackageName())) {
        	
        	Intent iniciaMusicaFundo = new Intent(ActivityDoJogoComSom.this, BackgroundSoundService.class);
  			//stopService(iniciaMusicaFundo);
            Toast.makeText(ActivityDoJogoComSom.this, "YOU LEFT YOUR APP", Toast.LENGTH_SHORT).show();
            // Unbind from the service
	       
          }
        }
        
        super.onPause();
		
	}
	
	@Override
	protected void onResume()
	{
		if(isMyServiceRunning() == false)
		{
			Intent iniciaMusicaFundo = new Intent(ActivityDoJogoComSom.this, BackgroundSoundService.class);
			//startService(iniciaMusicaFundo);
			
		}
		super.onResume();
		//TocadorMusicaBackground.getInstance().resumirTocadorMusica();
		
	}
	
	 @Override
	    protected void onStart() {
	        super.onStart();
	        // Bind to the service
	        bindService(new Intent(this, BackgroundSoundService.class), mConnection,
	            Context.BIND_AUTO_CREATE);
	    }

	    @Override
	    protected void onStop() {
	        super.onStop();
	        // Unbind from the service
	        if (activityEstahAmarradaComServiceMusicaDeFundo) {
	            unbindService(mConnection);
	            activityEstahAmarradaComServiceMusicaDeFundo = false;
	        }
	    }
	
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (BackgroundSoundService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	//REFERENTE AO BINDIND DA ACTIVITY AO SERVICO DE MUSICA DE FUNDO PARA MUDAR MUSICA
	  /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BackgroundSoundServiceBinder binder = (BackgroundSoundServiceBinder) service;
            servicoFazMusicaDeFundo = binder.getService();
            activityEstahAmarradaComServiceMusicaDeFundo = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            activityEstahAmarradaComServiceMusicaDeFundo = false;
        }
    };
    

}
