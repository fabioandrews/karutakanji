package com.karutakanji;



import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

import lojinha.ConcreteDAOAcessaDinheiroDoJogador;
import lojinha.DAOAcessaDinheiroDoJogador;

import bancodedados.ChecaVersaoAtualDoSistemaTask;
import bancodedados.DadosPartidaParaOLog;
import bancodedados.EnviarDadosDaPartidaParaLogTask;
import bancodedados.KanjiTreinar;
import bancodedados.SolicitaKanjisParaTreinoTask;

import android.os.Bundle;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.PorterDuff;

public class MainActivity extends ActivityDoJogoComSom implements View.OnClickListener
{
	private ChecaVersaoAtualDoSistemaTask checaVersaoAtual;
	final static int[] SCREENS = {R.id.telaatualizeojogo, R.id.telainicialnormal};

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		switchToScreen(R.id.telainicialnormal);
		this.checaVersaoAtual = new ChecaVersaoAtualDoSistemaTask(this);
		this.checaVersaoAtual.execute("");
		
		ImageView imageViewCarta = (ImageView) findViewById(R.id.imageView2);
		imageViewCarta.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void irAoModoMultiplayer(View view)
	{
		try
		{
			Intent criaTelaModoMultiplayer =
					new Intent(MainActivity.this, TelaInicialMultiplayer.class);
			startActivity(criaTelaModoMultiplayer);
		}
		catch(Exception e)
		{
			Writer writer = new StringWriter();
	    	PrintWriter printWriter = new PrintWriter(writer);
	    	e.printStackTrace(printWriter);
	    	String s = writer.toString();
	    	Context context = getApplicationContext();
	        Toast t = Toast.makeText(context, s, Toast.LENGTH_LONG);
	        t.show();
		}
		
	}
	
	public void irAEscolhaDeCategoriasModoTreinamento(View view)
	{
		try
		{
			Intent criaTelaModoTreinamento =
					new Intent(MainActivity.this, EscolherCategoriasModoTreinamento.class);
			startActivity(criaTelaModoTreinamento);
		}
		catch(Exception e)
		{
			Writer writer = new StringWriter();
	    	PrintWriter printWriter = new PrintWriter(writer);
	    	e.printStackTrace(printWriter);
	    	String s = writer.toString();
	    	Context context = getApplicationContext();
	        Toast t = Toast.makeText(context, s, Toast.LENGTH_LONG);
	        t.show();
		}
		
	}
	
	 public void fazerToast(String mensagem)
	 {
		 Toast t = Toast.makeText(this, mensagem, Toast.LENGTH_LONG);
		  t.show();
	 }
	 
	 public void irADadosPartidasAnteriores(View v)
	 {
		 Intent criaTelaDadosAnteriores =
					new Intent(MainActivity.this, DadosPartidasAnteriores.class);
			startActivity(criaTelaDadosAnteriores);
	 }
	 
	 public void irALojinha(View v)
	 {
		 Intent criaLojinha =
					new Intent(MainActivity.this, LojinhaMaceteKanjiActivity.class);
			startActivity(criaLojinha);
	 }
	 
	 public void adicionarDinheirinho(View v)
	 {
			
			DAOAcessaDinheiroDoJogador acessaDinheiroDoJogador = ConcreteDAOAcessaDinheiroDoJogador.getInstance();
			acessaDinheiroDoJogador.adicionarCredito(1500, this);
			int creditoAtual = acessaDinheiroDoJogador.getCreditoQuePossui(this);
			
	 }
	 
	 @Override
		protected void onPause()
		{
			
			//TocadorMusicaBackground.getInstance().pausarTocadorMusica();
			if(this.isFinishing())
			{
				Toast.makeText(MainActivity.this, "is finishing will stop service", Toast.LENGTH_SHORT).show();
				Intent iniciaMusicaFundo = new Intent(MainActivity.this, BackgroundSoundService.class);
				stopService(iniciaMusicaFundo);
			}
			super.onPause();
			
		}

	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}
	
	void switchToScreen(int screenId) {
		// make the requested screen visible; hide all others.
		for (int id : SCREENS) {
		    findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
		}
	}
	
	public void mudarParaTelaAtualizeOJogo(String versaoMaisAtual)
	{
		switchToScreen(R.id.telaatualizeojogo);
		
		String stringMensagemAtualizeJogo = getResources().getString(R.string.mensagem_por_favor_atualize_o_jogo);
		stringMensagemAtualizeJogo = stringMensagemAtualizeJogo + versaoMaisAtual;
		
		TextView textViewAtualize = (TextView) findViewById(R.id.mensagemAtualizeOJogo);
		textViewAtualize.setText(stringMensagemAtualizeJogo);
	}
	
	public void mostrarErro(String erro)
	{
		Toast t = Toast.makeText(this, erro, Toast.LENGTH_LONG);
	    t.show();
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
	    	case R.id.imageView2:
	    		ImageView imageViewCarta = (ImageView) findViewById(R.id.imageView2);
	    		imageViewCarta.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
	    	break;
		}
	}
}
