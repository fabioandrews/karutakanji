package com.karutakanji;

import java.util.HashMap;
import java.util.LinkedList;

import bancodedados.KanjiTreinar;

public class SingletonGuardaDadosDaPartida 
{
	private static SingletonGuardaDadosDaPartida instancia;
	private HashMap<String,LinkedList<KanjiTreinar>> categoriasEscolhidasEKanjisDelas;
	private int quantasRodadasHaverao; //possiveis valores: 1,2,3 e 99(para infinitas rodadas)
	
	private SingletonGuardaDadosDaPartida()
	{
		
	}
	
	public static SingletonGuardaDadosDaPartida getInstance()
	{
		if(instancia == null)
		{
			instancia = new SingletonGuardaDadosDaPartida();
		}
		
		return instancia;
	}

	public HashMap<String, LinkedList<KanjiTreinar>> getCategoriasEscolhidasEKanjisDelas() {
		return categoriasEscolhidasEKanjisDelas;
	}

	public void adicionarNovaCategoriaESeusKanjis(String categoria, LinkedList<KanjiTreinar> kanjis)
	{
		if(this.categoriasEscolhidasEKanjisDelas == null)
		{
			this.categoriasEscolhidasEKanjisDelas = new HashMap<String, LinkedList<KanjiTreinar>>();
		}
		this.categoriasEscolhidasEKanjisDelas.put(categoria, kanjis);
	}
	
	public void limparCategoriasEKanjis()
	{
		this.categoriasEscolhidasEKanjisDelas = null;
	}

	public int getQuantasRodadasHaverao() {
		return quantasRodadasHaverao;
	}

	public void setQuantasRodadasHaverao(int quantasRodadasHaverao) 
	{
		this.quantasRodadasHaverao = quantasRodadasHaverao;
	}

}
