package com.karutakanji;



import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import bancodedados.ActivityQueEsperaAtePegarOsKanjis;
import bancodedados.ArmazenaKanjisPorCategoria;
import bancodedados.CategoriaDeKanjiParaListviewSelecionavel;
import bancodedados.KanjiTreinar;
import bancodedados.MyCustomAdapter;
import bancodedados.MyCustomAdapter1Jogador;
import bancodedados.SolicitaKanjisParaTreinoTask;


import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EscolherCategoriasModoTreinamento extends ActivityDoJogoComSom implements ActivityQueEsperaAtePegarOsKanjis
{
	private MyCustomAdapter1Jogador dataAdapter; 
	private String jlptEnsinarNaFerramenta = "4";
	private ProgressDialog loadingKanjisDoBd;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_escolher_categorias_modo_treinamento);
		
		this.loadingKanjisDoBd = ProgressDialog.show(EscolherCategoriasModoTreinamento.this, getResources().getString(R.string.carregando_kanjis_remotamente), getResources().getString(R.string.por_favor_aguarde));
		  SolicitaKanjisParaTreinoTask pegarKanjisTreino = new SolicitaKanjisParaTreinoTask(this.loadingKanjisDoBd, this);
		  pegarKanjisTreino.execute("");
		  
	}
	
	public void usuarioClicouBotaoOk(View v)
	{
		ArrayList<CategoriaDeKanjiParaListviewSelecionavel> categoriasSelecionadas = this.dataAdapter.getCategoriaDeKanjiList();
		
		if(categoriasSelecionadas.size() == 0)
		 {
			 String mensagem = getResources().getString(R.string.erroEscolherCategorias);
			 Toast t = Toast.makeText(this, mensagem, Toast.LENGTH_LONG);
			 t.show();
		 }
		else
		{
			SingletonGuardaDadosDaPartida.getInstance().limparCategoriasEKanjis();
			 
			 ArmazenaKanjisPorCategoria conheceKanjisECategorias = ArmazenaKanjisPorCategoria.pegarInstancia();
				for(int i = 0; i < categoriasSelecionadas.size(); i++)
				{
					CategoriaDeKanjiParaListviewSelecionavel umaCategoria = categoriasSelecionadas.get(i);
					if(umaCategoria.isSelected() == true)
					{
						String nomeCategoria = umaCategoria.getName();
						int posicaoParenteses = nomeCategoria.indexOf("(");
						String nomeCategoriaSemParenteses = nomeCategoria.substring(0, posicaoParenteses);
						LinkedList<KanjiTreinar> kanjisDaCategoria = 
								conheceKanjisECategorias.getListaKanjisTreinar(nomeCategoriaSemParenteses);
						SingletonGuardaDadosDaPartida.getInstance().adicionarNovaCategoriaESeusKanjis(nomeCategoriaSemParenteses, kanjisDaCategoria);
						
					}
				}
				
					Intent criaTelaModoTreinamento =
							new Intent(EscolherCategoriasModoTreinamento.this, ModoTreinamento.class);
					startActivity(criaTelaModoTreinamento);
		}
		
	}

	@Override
	public void mostrarListaComKanjisAposCarregar() 
	{
		ArrayList<CategoriaDeKanjiParaListviewSelecionavel> listaDeCategorias = new ArrayList<CategoriaDeKanjiParaListviewSelecionavel>();
		  
		  LinkedList<String> categoriasDosKanjis = 
				  ArmazenaKanjisPorCategoria.pegarInstancia().getCategoriasDeKanjiArmazenadas(jlptEnsinarNaFerramenta);
		  
		  for(int i = 0; i < categoriasDosKanjis.size(); i++)
		  {
			  String categoriaDeKanji = categoriasDosKanjis.get(i);
			  LinkedList<KanjiTreinar> kanjisDaCategoria = ArmazenaKanjisPorCategoria.pegarInstancia().getListaKanjisTreinar(categoriaDeKanji);
			  String labelCategoriaDeKanji = categoriaDeKanji + "(" + kanjisDaCategoria.size() + getResources().getString(R.string.contagem_kanjis) + ")";
			  CategoriaDeKanjiParaListviewSelecionavel novaCategoria = new CategoriaDeKanjiParaListviewSelecionavel(labelCategoriaDeKanji, false);
			  listaDeCategorias.add(novaCategoria);
		  }

		  
		  //create an ArrayAdaptar from the String Array
		  dataAdapter = new MyCustomAdapter1Jogador(this,
		    R.layout.categoria_de_kanji_na_lista, listaDeCategorias);
		  ListView listView = (ListView) findViewById(R.id.listaCategorias);
		  // Assign adapter to ListView
		  listView.setAdapter(dataAdapter);
		  
		  listView.setOnItemClickListener(new OnItemClickListener() {
		   public void onItemClick(AdapterView parent, View view,
		     int position, long id) 
		   {
			   //faz nada 
		   }
		  });
		
	}

	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}
}
