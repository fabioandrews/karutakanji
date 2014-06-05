package com.karutakanji;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import bancodedados.KanjiTreinar;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ModoTreinamento extends ActivityDoJogoComSom implements OnClickListener
{
	private int nivel; 
	private int vidas; //jogador comeca com 4 vidas e vai diminuindo
	private int suaPontuacao;
	private LinkedList<KanjiTreinar> kanjisDasCartasNaTela;
	private LinkedList<KanjiTreinar> kanjisDasCartasNaTelaQueJaSeTornaramDicas;
	private KanjiTreinar kanjiDaDica;
	
	private int quantosNiveisPassaram; //numero que so vai de 0 a 2. De 2 em 2 niveis, o usuario perceberah uma mudanca nas cartas ou nos kanjis 
	private LinkedList<KanjiTreinar> kanjisQuePodemVirarCartasNovas;
	private LinkedList<KanjiTreinar> kanjisQueJaViraramCartas; //no jogo inteiro
	private LinkedList<KanjiTreinar> ultimosKanjis; //os kanjis serao ensinados de 4 em 4. Esses 4 ensinados deveriam aparecer com maior frequencia na tela p o usuario memoriza-los
	
	private int quantasCartasHaveraoNaTela; //no nivel 5,9,13 e 17 esse numero aumenta
	private boolean naoHaMaisNovosKanjisParaSeCriar;
	
	final static int[] SCREENS = {R.id.tela_modo_treinamento,R.id.tela_observacao_novos_kanjis,R.id.tela_fim_do_modo_treinamento};
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modo_treinamento);
		this.comecarJogoPelaPrimeiraVez();
		this.mostrarTelaObservacaoNovosKanjis();
	}
	
	private void comecarJogoPelaPrimeiraVez()
	{	
		this.nivel = 1;
		this.vidas = 4;
		this.suaPontuacao = 0;
		this.quantosNiveisPassaram = 0;
		quantasCartasHaveraoNaTela = 4;
		kanjisQueJaViraramCartas = new LinkedList<KanjiTreinar>();
		ultimosKanjis = new LinkedList<KanjiTreinar>();
		this.kanjisDasCartasNaTela = new LinkedList<KanjiTreinar>();
		this.kanjisDasCartasNaTelaQueJaSeTornaramDicas = new LinkedList<KanjiTreinar>();
		
		naoHaMaisNovosKanjisParaSeCriar = false;
		
		TextView textNivel = (TextView) findViewById(R.id.nivel);
		String stringNivel = getResources().getString(R.string.nivel);
		stringNivel = stringNivel + this.nivel;
		textNivel.setText(stringNivel);
		
		pegarTodosOsKanjisQuePodemVirarCartas();
		
		findViewById(R.id.karuta1_imageview).setOnClickListener(this);
		findViewById(R.id.karuta2_imageview).setOnClickListener(this);
		findViewById(R.id.karuta3_imageview).setOnClickListener(this);
		findViewById(R.id.karuta4_imageview).setOnClickListener(this);
		findViewById(R.id.karuta5_imageview).setOnClickListener(this);
		findViewById(R.id.karuta6_imageview).setOnClickListener(this);
		findViewById(R.id.karuta7_imageview).setOnClickListener(this);
		findViewById(R.id.karuta8_imageview).setOnClickListener(this);
		
		TextView textoPontuacao = (TextView) findViewById(R.id.pontuacao);
		String pontuacao = getResources().getString(R.string.pontuacao);
		textoPontuacao.setText(pontuacao + String.valueOf(this.suaPontuacao));
		
		this.tornarCartasNaTelaClicaveisEVaziasNovamente();
		this.escolherKanjisParaONivel();
		this.gerarKanjiDaDica();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.modo_treinamento, menu);
		return true;
	}
	
	
	private void escolherNovosKanjis()
	{
		this.ultimosKanjis.clear();
		
		for(int i = 0; i < 4; i++)
		{
			KanjiTreinar kanjiTreinar = this.escolherUmNovoKanjiParaTreinar();
			 
			 if(kanjiTreinar == null)
			 {
				 //acabaram-se os kanjis que posso usar na tela
				 this.naoHaMaisNovosKanjisParaSeCriar = true;
				 break;
				 
			 }
			 else
			 {
				 this.ultimosKanjis.add(kanjiTreinar);
			 }
		}
	}
	
	public void mostrarTelaModoTreinamento(View v)
	{
		this.switchToScreen(R.id.tela_modo_treinamento);
		
		if(this.nivel == 1)
		{
			//no primeiro nivel, iremos iniciar a musica de fundo
			this.mudarMusicaDeFundo(R.raw.radiate);
		}
	}
	
	public void mostrarTelaObservacaoNovosKanjis()
	{
		this.switchToScreen(R.id.tela_observacao_novos_kanjis);
		
		TextView textoUltimoKanji1 = (TextView) findViewById(R.id.ultimokanji1);
		TextView textoUltimoKanji2 = (TextView) findViewById(R.id.ultimokanji2);
		TextView textoUltimoKanji3 = (TextView) findViewById(R.id.ultimokanji3);
		TextView textoUltimoKanji4 = (TextView) findViewById(R.id.ultimokanji4);
		
		String queSignifica = getResources().getString(R.string.que_significa);
		
		if(this.ultimosKanjis.size() == 0)
		{
			mostrarTelaModoTreinamento(null);
		}
		else if(this.ultimosKanjis.size() == 1)
		{
			KanjiTreinar ultimoKanji1 = this.ultimosKanjis.get(0); 
			String textoDicaKanji1 = ultimoKanji1.getKanji() + " " + queSignifica + " " +
						ultimoKanji1.getHiraganaDoKanji() + "(" + ultimoKanji1.getTraducaoEmPortugues() + ")";  
			textoUltimoKanji1.setText(textoDicaKanji1);
			textoUltimoKanji2.setText("");
			textoUltimoKanji3.setText("");
			textoUltimoKanji4.setText("");
		}
		else if(this.ultimosKanjis.size() == 2)
		{
			KanjiTreinar ultimoKanji1 = this.ultimosKanjis.get(0); 
			String textoDicaKanji1 = ultimoKanji1.getKanji() + " " + queSignifica + " " +
						ultimoKanji1.getHiraganaDoKanji() + "(" + ultimoKanji1.getTraducaoEmPortugues() + ")";  
			textoUltimoKanji1.setText(textoDicaKanji1);
			
			KanjiTreinar ultimoKanji2 = this.ultimosKanjis.get(1); 
			String textoDicaKanji2 = ultimoKanji2.getKanji() + " " + queSignifica + " " +
						ultimoKanji2.getHiraganaDoKanji() + "(" + ultimoKanji2.getTraducaoEmPortugues() + ")";  
			textoUltimoKanji2.setText(textoDicaKanji2);
			textoUltimoKanji3.setText("");
			textoUltimoKanji4.setText("");
		}
		else if(this.ultimosKanjis.size() == 3)
		{
			KanjiTreinar ultimoKanji1 = this.ultimosKanjis.get(0); 
			String textoDicaKanji1 = ultimoKanji1.getKanji() + " " + queSignifica + " " +
						ultimoKanji1.getHiraganaDoKanji() + "(" + ultimoKanji1.getTraducaoEmPortugues() + ")";  
			textoUltimoKanji1.setText(textoDicaKanji1);
			
			KanjiTreinar ultimoKanji2 = this.ultimosKanjis.get(1); 
			String textoDicaKanji2 = ultimoKanji2.getKanji() + " " + queSignifica + " " +
						ultimoKanji2.getHiraganaDoKanji() + "(" + ultimoKanji2.getTraducaoEmPortugues() + ")";  
			textoUltimoKanji2.setText(textoDicaKanji2);
			
			KanjiTreinar ultimoKanji3 = this.ultimosKanjis.get(2); 
			String textoDicaKanji3 = ultimoKanji3.getKanji() + " " + queSignifica + " " +
						ultimoKanji3.getHiraganaDoKanji() + "(" + ultimoKanji3.getTraducaoEmPortugues() + ")";  
			textoUltimoKanji3.setText(textoDicaKanji3);
			textoUltimoKanji4.setText("");
		}
		else if(this.ultimosKanjis.size() == 4)
		{
			KanjiTreinar ultimoKanji1 = this.ultimosKanjis.get(0); 
			String textoDicaKanji1 = ultimoKanji1.getKanji() + " " + queSignifica + " " +
						ultimoKanji1.getHiraganaDoKanji() + "(" + ultimoKanji1.getTraducaoEmPortugues() + ")";  
			textoUltimoKanji1.setText(textoDicaKanji1);
			
			KanjiTreinar ultimoKanji2 = this.ultimosKanjis.get(1); 
			String textoDicaKanji2 = ultimoKanji2.getKanji() + " " + queSignifica + " " +
						ultimoKanji2.getHiraganaDoKanji() + "(" + ultimoKanji2.getTraducaoEmPortugues() + ")";  
			textoUltimoKanji2.setText(textoDicaKanji2);
			
			KanjiTreinar ultimoKanji3 = this.ultimosKanjis.get(2); 
			String textoDicaKanji3 = ultimoKanji3.getKanji() + " " + queSignifica + " " +
						ultimoKanji3.getHiraganaDoKanji() + "(" + ultimoKanji3.getTraducaoEmPortugues() + ")";  
			textoUltimoKanji3.setText(textoDicaKanji3);
			
			KanjiTreinar ultimoKanji4 = this.ultimosKanjis.get(3); 
			String textoDicaKanji4 = ultimoKanji4.getKanji() + " " + queSignifica + " " +
						ultimoKanji4.getHiraganaDoKanji() + "(" + ultimoKanji4.getTraducaoEmPortugues() + ")";  
			textoUltimoKanji4.setText(textoDicaKanji4);
		}
		
		TextView textoObservacao = (TextView) findViewById(R.id.observacao);
		if(this.nivel == 5 || this.nivel == 9 || this.nivel == 13 || this.nivel == 17)
		{
			textoObservacao.setVisibility(View.VISIBLE);
		}
		else
		{
			textoObservacao.setVisibility(View.INVISIBLE);
		}
		
	}
	
	private void escolherKanjisParaONivel()
	{	 
		 this.kanjisDasCartasNaTela.clear();
		 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.clear();
		 
		 
		 if(this.quantosNiveisPassaram >= 2 || nivel == 1)
		 {
			 //hora de mais novos kanjis
			 this.escolherNovosKanjis();
			 this.mostrarTelaObservacaoNovosKanjis(); //aquela tela que o usuario visualiza os novos kanjis
			 this.quantosNiveisPassaram = 0;
		 }
		  
		 if(naoHaMaisNovosKanjisParaSeCriar == true && this.quantosNiveisPassaram >= 2)
		 {
			 //nao tem mais utilidade esses ultimos kanjis
			 this.ultimosKanjis.clear();
		 }
		 
		//escolher kanjis velhos
		 LinkedList<KanjiTreinar> velhosKanjis = this.escolherKanjisNaoNovos(this.quantasCartasHaveraoNaTela - this.ultimosKanjis.size());
		 
		 LinkedList<KanjiTreinar> kanjisDaTela = new LinkedList<KanjiTreinar>();
		 
		 for(int g = 0; g < ultimosKanjis.size(); g++)
		 {
			 kanjisDaTela.add(ultimosKanjis.get(g));
		 }
		 for(int h = 0; h < velhosKanjis.size(); h++)
		 {
			 kanjisDaTela.add(velhosKanjis.get(h));
		 }
		 
		 Collections.shuffle(kanjisDaTela);
		 
		 
		 for(int i = 0; i < this.quantasCartasHaveraoNaTela; i++)
		 {
			 KanjiTreinar kanjiParaUmaCarta = kanjisDaTela.get(i);
			 
			 this.kanjisDasCartasNaTela.add(kanjiParaUmaCarta);
				 
				 if(i == 0)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta1);
					 texto.setText(kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 1)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta2);
					 texto.setText(kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 2)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta3);
					 texto.setText(kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 3)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta4);
					 texto.setText(kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 4)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta5);
					 texto.setText(kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 5)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta6);
					 texto.setText(kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 6)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta7);
					 texto.setText(kanjiParaUmaCarta.getKanji());
				 }
				 else if(i == 7)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta8);
					 texto.setText(kanjiParaUmaCarta.getKanji());
				 } 
				 
			 }
		 
	 }
	
	private KanjiTreinar escolherUmNovoKanjiParaTreinar()
	 {
		if(kanjisQuePodemVirarCartasNovas.size() <= 0)
		{
			return null;
		}
		else
		{
			Random geraNumAleatorio = new Random();
			 int posicaoKanjiEscolhido = geraNumAleatorio.nextInt(this.kanjisQuePodemVirarCartasNovas.size());
			 
			 KanjiTreinar kanjiEscolhido = this.kanjisQuePodemVirarCartasNovas.remove(posicaoKanjiEscolhido); 
			 
			 this.kanjisQueJaViraramCartas.add(kanjiEscolhido);
			 
			 return kanjiEscolhido;
		} 
	 }
	
	private void pegarTodosOsKanjisQuePodemVirarCartas()
	 {
		 this.kanjisQuePodemVirarCartasNovas = new LinkedList<KanjiTreinar>();
		 HashMap<String,LinkedList<KanjiTreinar>> categoriasEscolhidasEKanjisDelas = SingletonGuardaDadosDaPartida.getInstance().getCategoriasEscolhidasEKanjisDelas();
		 
		 Iterator<String> iteradorCategoriasEKanjis = categoriasEscolhidasEKanjisDelas.keySet().iterator();
		 while(iteradorCategoriasEKanjis.hasNext() == true)
		 {
			 String umaCategoria = iteradorCategoriasEKanjis.next();
			 LinkedList<KanjiTreinar> kanjisDaCategoria = categoriasEscolhidasEKanjisDelas.get(umaCategoria);
			 
			 for(int i = 0; i < kanjisDaCategoria.size(); i++)
			 {
				 this.kanjisQuePodemVirarCartasNovas.add(kanjisDaCategoria.get(i));
			 }
		 }
	 }
	
	public void gerarKanjiDaDica()
	{
		LinkedList<KanjiTreinar> kanjisQueAindaNaoViraramDicas = new LinkedList<KanjiTreinar>();
		 
		 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
		 {
			 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
			 
			 boolean kanjiJaVirouDica = false;
			 for(int j = 0; j < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); j++)
			 {
				 KanjiTreinar umKanjiQueVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(j);
				 
				 if((umKanjiQueVirouDica.getKanji().compareTo(umKanji.getKanji()) == 0) 
						 && (umKanjiQueVirouDica.getCategoriaAssociada().compareTo(umKanji.getCategoriaAssociada()) == 0))
				 {
					 kanjiJaVirouDica = true;
				 }
			 }
			 
			 if(kanjiJaVirouDica == false)
			 {
				 kanjisQueAindaNaoViraramDicas.add(umKanji);
			 }
		 }
		 
		 
		 Random geraNumAleatorio = new Random(); 
		 int indiceKanjiDaDica = geraNumAleatorio.nextInt(kanjisQueAindaNaoViraramDicas.size());
		 
		 KanjiTreinar umKanji = kanjisQueAindaNaoViraramDicas.get(indiceKanjiDaDica);
		 this.kanjiDaDica = umKanji;
		 
		 this.alterarTextoDicaComBaseNoKanjiDaDica();
		 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.add(umKanji);
	}
	
	private void alterarTextoDicaComBaseNoKanjiDaDica()
	{
		 TextView textoDica = (TextView) findViewById(R.id.dica_kanji);
		 String hiraganaDoKanji = this.kanjiDaDica.getHiraganaDoKanji();
		 String traducaoDoKanji = this.kanjiDaDica.getTraducaoEmPortugues();
		 textoDica.setText(hiraganaDoKanji + "(" + traducaoDoKanji + ")");
	}
	
	private void gerarKanjiDaDicaOuIniciarNovoNivel()
	{
		 if(this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() == this.kanjisDasCartasNaTela.size())
		 {
			//deve-se passar para o proximo nivel
		    this.realizarProcedimentoPassarParaProximoNivel();
		    this.gerarKanjiDaDica();
		 }
		 else
		 {
			 this.gerarKanjiDaDica();
		 }
	}
	 
	private void realizarProcedimentoPassarParaProximoNivel()
	{
		this.nivel = this.nivel + 1;
		if(nivel == 5)
		{
			this.quantasCartasHaveraoNaTela = this.quantasCartasHaveraoNaTela + 1;
		}
		else if(nivel == 9)
		{
			this.quantasCartasHaveraoNaTela = this.quantasCartasHaveraoNaTela + 1;
		}
		else if(nivel == 13)
		{
			this.quantasCartasHaveraoNaTela = this.quantasCartasHaveraoNaTela + 1;
		}
		else if(nivel == 17)
		{
			this.quantasCartasHaveraoNaTela = this.quantasCartasHaveraoNaTela + 1;
		}

		this.quantosNiveisPassaram = this.quantosNiveisPassaram + 1;
		 TextView textViewNivel = (TextView) findViewById(R.id.nivel);
		 String nivel = getResources().getString(R.string.nivel);
		 textViewNivel.setText(nivel + String.valueOf(this.nivel));
		 
		 this.tornarCartasNaTelaClicaveisEVaziasNovamente();
	     this.escolherKanjisParaONivel();
	     
	}
	
	private void tornarCartasNaTelaClicaveisEVaziasNovamente()
	{
		 ImageView imageViewKaruta1 = (ImageView) findViewById(R.id.karuta1_imageview);
		 imageViewKaruta1.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
		 findViewById(R.id.karuta1).setClickable(true);
		 imageViewKaruta1.setVisibility(View.VISIBLE);
		 
		 ImageView imageViewKaruta2 = (ImageView) findViewById(R.id.karuta2_imageview);
		 imageViewKaruta2.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
		 findViewById(R.id.karuta2).setClickable(true);
		 imageViewKaruta2.setVisibility(View.VISIBLE);
		 
		 ImageView imageViewKaruta3 = (ImageView) findViewById(R.id.karuta3_imageview);
		 imageViewKaruta3.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
		 findViewById(R.id.karuta3).setClickable(true);
		 imageViewKaruta3.setVisibility(View.VISIBLE);
		 
		 ImageView imageViewKaruta4 = (ImageView) findViewById(R.id.karuta4_imageview);
		 imageViewKaruta4.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
		 findViewById(R.id.karuta4).setClickable(true);
		 imageViewKaruta4.setVisibility(View.VISIBLE);
		 
		 if(quantasCartasHaveraoNaTela < 5)
		 { 
			 TextView textoKaruta5 = (TextView) findViewById(R.id.texto_karuta5);
			 textoKaruta5.setText("");
			 TextView textoKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
			 textoKaruta6.setText("");
			 TextView textoKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
			 textoKaruta7.setText("");
			 TextView textoKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
			 textoKaruta8.setText("");
			 
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setVisibility(View.INVISIBLE);
		 }
		 else if(this.quantasCartasHaveraoNaTela == 5)
		 {
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
			 findViewById(R.id.karuta5).setClickable(true);
			 imageViewKaruta5.setVisibility(View.VISIBLE);
			 
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setVisibility(View.INVISIBLE);
			 
			 TextView textoKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
			 textoKaruta6.setText("");
			 TextView textoKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
			 textoKaruta7.setText("");
			 TextView textoKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
			 textoKaruta8.setText("");
		 }
		 else if(this.quantasCartasHaveraoNaTela == 6)
		 {
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
			 findViewById(R.id.karuta5).setClickable(true);
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
			 findViewById(R.id.karuta6).setClickable(true);
			 imageViewKaruta5.setVisibility(View.VISIBLE);
			 imageViewKaruta6.setVisibility(View.VISIBLE);
			 
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setVisibility(View.INVISIBLE);
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setVisibility(View.INVISIBLE);
			 
			 TextView textoKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
			 textoKaruta7.setText("");
			 TextView textoKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
			 textoKaruta8.setText("");
		 }
		 else if(this.quantasCartasHaveraoNaTela == 7)
		 {
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
			 findViewById(R.id.karuta5).setClickable(true);
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
			 findViewById(R.id.karuta6).setClickable(true);
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
			 findViewById(R.id.karuta7).setClickable(true);
			 imageViewKaruta5.setVisibility(View.VISIBLE);
			 imageViewKaruta6.setVisibility(View.VISIBLE);
			 imageViewKaruta7.setVisibility(View.VISIBLE);
			 
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setVisibility(View.INVISIBLE);
			 TextView textoKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
			 textoKaruta8.setText("");
			 
			 
		 }
		 else
		 {
			 ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
			 imageViewKaruta5.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
			 findViewById(R.id.karuta5).setClickable(true);
			 ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
			 imageViewKaruta6.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
			 findViewById(R.id.karuta6).setClickable(true);
			 ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
			 imageViewKaruta7.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
			 findViewById(R.id.karuta7).setClickable(true);
			 ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
			 imageViewKaruta8.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
			 findViewById(R.id.karuta8).setClickable(true);
			 imageViewKaruta5.setVisibility(View.VISIBLE);
			 imageViewKaruta6.setVisibility(View.VISIBLE);
			 imageViewKaruta7.setVisibility(View.VISIBLE);
			 imageViewKaruta8.setVisibility(View.VISIBLE);
		 }
		 
	 }
	
	private LinkedList<KanjiTreinar> escolherKanjisNaoNovos(int quantosKanjis)
	{
		LinkedList<KanjiTreinar> kanjisVelhos = new LinkedList<KanjiTreinar>();
		
		for(int i= 0; i < quantosKanjis; i++)
		{	
			Random geraNumAleatorio = new Random();
			boolean kanjiVelhoEhRepetido = true;
			
			KanjiTreinar kanjiNaoNovoEscolhido = null;
			
			while(kanjiVelhoEhRepetido == true)
			{
				int posicaoNovoKanjiNosKanjisJaViraramCartas = geraNumAleatorio.nextInt(this.kanjisQueJaViraramCartas.size());
				KanjiTreinar umKanji = this.kanjisQueJaViraramCartas.get(posicaoNovoKanjiNosKanjisJaViraramCartas);
				
				boolean umKanjiJaExisteNosKanjisVelhos = false;
				for(int j = 0; j < kanjisVelhos.size(); j++)
				{
					KanjiTreinar umKanjiVelho = kanjisVelhos.get(j);
					if(umKanjiVelho.getKanji().compareTo(umKanji.getKanji()) == 0 &&
							umKanjiVelho.getCategoriaAssociada().compareTo(umKanji.getCategoriaAssociada()) == 0)
					{
						//umKanji ja existe nos velhos
						umKanjiJaExisteNosKanjisVelhos = true;
					}
					
				}
				
				//esse kanji velho nao pode pertencer aos ultimos kanjis
				for(int k = 0; k < this.ultimosKanjis.size(); k++)
				{
					KanjiTreinar umDosUltimos = this.ultimosKanjis.get(k);
					if((umDosUltimos.getKanji().compareTo(umKanji.getKanji()) == 0)
							&& (umDosUltimos.getCategoriaAssociada().compareTo(umKanji.getCategoriaAssociada()) == 0))
					{
						//o kanjivelho eh um dos ultimos! nao pode! Devemos escolher outro kanji velho, por isso vamos obrigar que outro kanji seja escolhido
						umKanjiJaExisteNosKanjisVelhos = true;
					}
				}
				
				
				if(umKanjiJaExisteNosKanjisVelhos == true)
				{
					kanjiVelhoEhRepetido = true;
				}
				else
				{
					kanjiVelhoEhRepetido = false;
					kanjiNaoNovoEscolhido = umKanji;
				}
			}
			
			kanjisVelhos.add(kanjiNaoNovoEscolhido);
			
		}
		
		return kanjisVelhos;
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.karuta1_imageview:
	    		TextView textViewKaruta1 = (TextView) findViewById(R.id.texto_karuta1);
	        	String textoKaruta1 = textViewKaruta1.getText().toString();
	        	String textoKanjiDaDica = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta1.compareTo(textoKanjiDaDica) == 0)
	        	{
	        		//usuario acertou o kanji.
	        		super.reproduzirSfx("acertou_carta");
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	        		imageViewKaruta1.setImageResource(R.drawable.karutax); //mudei a figura da carta
	        		findViewById(R.id.karuta1).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta1.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        		
	        	}
	        	else
	        	{
	        		//errou
	        		this.realizarProcedimentoUsuarioErrouCarta();
	        	}
	    	
	    	break;
	    case R.id.karuta2_imageview:
	    		TextView textViewKaruta2 = (TextView) findViewById(R.id.texto_karuta2);
	        	String textoKaruta2 = textViewKaruta2.getText().toString();
	        	String textoKanjiDaDica2 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta2.compareTo(textoKanjiDaDica2) == 0)
	        	{
	        		//usuario acertou o kanji
	        		super.reproduzirSfx("acertou_carta");
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	        		imageViewKaruta2.setImageResource(R.drawable.karutax); //mudei a figura da carta
	        		findViewById(R.id.karuta2).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta2.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		this.realizarProcedimentoUsuarioErrouCarta();
	        	}
	    	break;
	    case R.id.karuta3_imageview:
	    		TextView textViewKaruta3 = (TextView) findViewById(R.id.texto_karuta3);
	        	String textoKaruta3 = textViewKaruta3.getText().toString();
	        	String textoKanjiDaDica3 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta3.compareTo(textoKanjiDaDica3) == 0)
	        	{
	        		//usuario acertou o kanji.
	        		super.reproduzirSfx("acertou_carta");
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	        		imageViewKaruta3.setImageResource(R.drawable.karutax); //mudei a figura da carta
	        		findViewById(R.id.karuta3).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta3.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		this.realizarProcedimentoUsuarioErrouCarta();
	        	}
	    	break;
	    case R.id.karuta4_imageview:
	    		TextView textViewKaruta4 = (TextView) findViewById(R.id.texto_karuta4);
	        	String textoKaruta4 = textViewKaruta4.getText().toString();
	        	String textoKanjiDaDica4 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta4.compareTo(textoKanjiDaDica4) == 0)
	        	{
	        		//usuario acertou o kanji. 
	        		super.reproduzirSfx("acertou_carta");
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	        		imageViewKaruta4.setImageResource(R.drawable.karutax); //mudei a figura da carta
	        		findViewById(R.id.karuta4).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta4.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		this.realizarProcedimentoUsuarioErrouCarta();
	        	}
	    	break;
	    case R.id.karuta5_imageview:
	    		TextView textViewKaruta5 = (TextView) findViewById(R.id.texto_karuta5);
	        	String textoKaruta5 = textViewKaruta5.getText().toString();
	        	String textoKanjiDaDica5 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta5.compareTo(textoKanjiDaDica5) == 0)
	        	{
	        		//usuario acertou o kanji.
	        		super.reproduzirSfx("acertou_carta");
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	        		imageViewKaruta5.setImageResource(R.drawable.karutax); //mudei a figura da carta
	        		findViewById(R.id.karuta5).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta5.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		this.realizarProcedimentoUsuarioErrouCarta();
	        	}
	    	break;
	    case R.id.karuta6_imageview:
	    		TextView textViewKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
	        	String textoKaruta6 = textViewKaruta6.getText().toString();
	        	String textoKanjiDaDica6 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta6.compareTo(textoKanjiDaDica6) == 0)
	        	{
	        		//usuario acertou o kanji.
	        		super.reproduzirSfx("acertou_carta");
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	        		imageViewKaruta6.setImageResource(R.drawable.karutax); //mudei a figura da carta
	        		findViewById(R.id.karuta6).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta6.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		this.realizarProcedimentoUsuarioErrouCarta();
	        	}
	    	break;
	    case R.id.karuta7_imageview:
	    		TextView textViewKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
	        	String textoKaruta7 = textViewKaruta7.getText().toString();
	        	String textoKanjiDaDica7 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta7.compareTo(textoKanjiDaDica7) == 0)
	        	{
	        		//usuario acertou o kanji.
	        		super.reproduzirSfx("acertou_carta");
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	        		imageViewKaruta7.setImageResource(R.drawable.karutax); //mudei a figura da carta
	        		findViewById(R.id.karuta7).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta7.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		this.realizarProcedimentoUsuarioErrouCarta();
	        	}
	    	break;
	    case R.id.karuta8_imageview:
	    		TextView textViewKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
	        	String textoKaruta8 = textViewKaruta8.getText().toString();
	        	String textoKanjiDaDica8 = this.kanjiDaDica.getKanji();
	        	
	        	if(textoKaruta8.compareTo(textoKanjiDaDica8) == 0)
	        	{
	        		//usuario acertou o kanji.
	        		super.reproduzirSfx("acertou_carta");
	        		aumentarPontuacaoComBaseNaDificuldadeDoKanji();
	        		ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	        		imageViewKaruta8.setImageResource(R.drawable.karutax); //mudei a figura da carta
	        		findViewById(R.id.karuta8).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	        		textViewKaruta8.setText("");
	        		this.gerarKanjiDaDicaOuIniciarNovoNivel();
	        	}
	        	else
	        	{
	        		//errou
	        		this.realizarProcedimentoUsuarioErrouCarta();
	        	}
	    	break;
		}
	}
	
	public void realizarProcedimentoUsuarioErrouCarta()
	{
		super.reproduzirSfx("errou_carta");
		
		this.vidas = this.vidas - 1;
		
		if(vidas == 3)
		{
			findViewById(R.id.coracao1).setVisibility(View.INVISIBLE);
		}
		else if(vidas == 2)
		{
			findViewById(R.id.coracao2).setVisibility(View.INVISIBLE);
		}
		else if(vidas == 1)
		{
			findViewById(R.id.coracao3).setVisibility(View.INVISIBLE);
		}
		else if(vidas == 0)
		{
			findViewById(R.id.coracao4).setVisibility(View.INVISIBLE);
			this.terminarModoTreinamento();
		}
	}
	
	
	void switchToScreen(int screenId) 
	{
		// make the requested screen visible; hide all others.
		for (int id : SCREENS) {
		    findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
		}
	}
	
	private void aumentarPontuacaoComBaseNaDificuldadeDoKanji()
	{
		int dificuldade = this.kanjiDaDica.getDificuldadeDoKanji();
		
		if(dificuldade == 1)
		{
			this.suaPontuacao = this.suaPontuacao + 10;
		}
		else if(dificuldade == 2)
		{
			this.suaPontuacao = this.suaPontuacao + 20;
		}
		else
		{
			this.suaPontuacao = this.suaPontuacao + 30;
		}
		
		TextView textoPontuacao = (TextView) findViewById(R.id.pontuacao);
		String pontuacao = getResources().getString(R.string.pontuacao);
		textoPontuacao.setText(pontuacao + String.valueOf(this.suaPontuacao));
	}

	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}
	
	private void terminarModoTreinamento()
	{
		this.mudarMusicaDeFundo(R.raw.time_to_unwind);
		
		switchToScreen(R.id.tela_fim_do_modo_treinamento);
		TextView textViewPontuacao = (TextView) findViewById(R.id.pontuacaoFimTreinamento);
		String textoPontuacao = getResources().getString(R.string.pontuacao);
		textViewPontuacao.setText(textoPontuacao + " " + this.suaPontuacao);
		
		TextView textViewNivel = (TextView) findViewById(R.id.chegouAteQueNivel);
		String textoNivel = getResources().getString(R.string.chegouAteQueNivel);
		textViewNivel.setText(textoNivel + " " + this.nivel);
		
	}
	
	public void voltarAoMenuPrincipal(View v)
	{
		Intent irMenuInicial =
				new Intent(ModoTreinamento.this, MainActivity.class);
		irMenuInicial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);	
		startActivity(irMenuInicial);
	}
	
	public void treinarNovamente(View v)
	{
		Intent comecarModoTreinamento =
				new Intent(ModoTreinamento.this, ModoTreinamento.class);
		comecarModoTreinamento.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);	
		startActivity(comecarModoTreinamento);
	}
	

}
