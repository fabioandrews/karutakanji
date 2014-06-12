package com.karutakanji;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver.PendingResult;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import bancodedados.ActivityQueEsperaAtePegarOsKanjis;
import bancodedados.ArmazenaKanjisPorCategoria;
import bancodedados.CategoriaDeKanjiParaListviewSelecionavel;
import bancodedados.DadosPartidaParaOLog;
import bancodedados.EnviarDadosDaPartidaParaLogTask;
import bancodedados.KanjiTreinar;
import bancodedados.MyCustomAdapter;
import bancodedados.SolicitaKanjisParaTreinoTask;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.multiplayer.realtime.*;
//import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.realtime.*;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import lojinha.ConcreteDAOAcessaDinheiroDoJogador;
import lojinha.DAOAcessaDinheiroDoJogador;
import lojinha.TransformaPontosEmCredito;

public class TelaInicialMultiplayer extends ActivityDoJogoComSom implements View.OnClickListener, RealTimeMessageReceivedListener,RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener, ActivityQueEsperaAtePegarOsKanjis,ActivityMultiplayerQueEsperaAtePegarOsKanjis 
{

/*
* API INTEGRATION SECTION. This section contains the code that integrates
* the game with the Google Play game services API.
*/

// Debug tag
final static boolean ENABLE_DEBUG = true;
final static String TAG = "ButtonClicker2000";

// Request codes for the UIs that we show with startActivityForResult:
final static int RC_SELECT_PLAYERS = 10000;
final static int RC_INVITATION_INBOX = 10001;
final static int RC_WAITING_ROOM = 10002;

// Room ID where the currently active game is taking place; null if we're
// not playing.
String mRoomId = null;
Room room;

private String emailUsuario; //email do usuario no google account que eh obtido assim que o usuario faz login

// Are we playing in multiplayer mode?
boolean mMultiplayer = false;

// The participants in the currently active game
ArrayList<Participant> mParticipants = null;

// My participant ID in the currently active game
String mMyId = null;

// If non-null, this is the id of the invitation we received via the
// invitation listener
String mIncomingInvitationId = null;

// Message buffer for sending messages
byte[] mMsgBuf; //1 e 2 byte eu nao uso, mas o 3 diz se o adversario acertou ou nao clicou na carta1('N','A')

private String quemEscolheACategoria;
private boolean finalizouDecisaoEscolheCategoria = false;

private int quantasRodadasHaverao; //possiveis valores: 1,2,3 e 99(para infinito)
private int rodadaAtual;

private int suaPontuacao; //sua pontuacao no modo multiplayer
private TimerTask timerTaskDecrementaTempoRestante;
private boolean tempoEstahParado; //quando o jogador usa o item que para o tempo, ele para

private LinkedList<KanjiTreinar> kanjisDasCartasNaTela; //os kanjis das cartas que aparecem na tela

private KanjiTreinar kanjiDaDica; //kanji da dica atual que mostra para ambos os jogadores
private LinkedList<KanjiTreinar> kanjisDasCartasNaTelaQueJaSeTornaramDicas; // quais kanjis ja viraram dicas? Essa lista deve se esvaziar no comeco de cada rodada   
private LinkedList<KanjiTreinar> kanjisQuePodemVirarCartas; //no comeco do jogo, eh igual a todos os kanjis das categorias escolhidas pelo usuario, mas com o passar das rodadas, vai-se tirando kanjis dessa lista 

private int pontuacaoDoAdversario; //precisaremos saber a pontuacao do adversario porque na hora de recebermos um item aleatorio, saberemos se devemos receber item do perdedor ou de quem estah ganhando 
private int quantasCartasJaSairamDoJogo; //de X em X cartas que saem, saberemos quando dar um item aleatorio p cada jogador
private String itemAtual;
private LinkedList<String> itensDoPerdedor; //essa lista e a de baixo sao preenchidas no comeco do jogo multiplayer
private LinkedList<String> itensDoGanhador;

private ImageView cartaASerTirada; //na hora que o item do trovao for usado, precisaremos armazenar algo nesse atributo so p passar p outra funcao
private boolean usouTrovaoTiraCarta; //booleano que diz se o usuario usou o item trovaotiracarta
private boolean usouReviveCarta; //booleano que diz se o usuario usou o item revivecarta

public boolean guestTerminouDeCarregarListaDeCategorias; //booleano para resolver problema de um dos jogadores nao estar recebendo a lista de categorias
private Handler mHandler = new Handler(); //handler para o chat do final do jogo
private ArrayList<String> mensagensChat; //arraylist com as mensagens do chat do fim de jogo

private LinkedList<KanjiTreinar> palavrasAcertadas;//toda palavra/kanji que o usuario acertar entra nessa lista(vai para o log)  
private LinkedList<KanjiTreinar> palavrasErradas;//toda palavra/kanji que o usuario errar entra nessa lista(vai para o log)
private LinkedList<KanjiTreinar> palavrasJogadas; //toda palavra/kanji que sair como dica serah armazenada (vai para o log)

private String emailAdversario;

private ProgressDialog loadingComecoDaPartida; //loading que comeca desde quando sao decididas as categorias do jogo ateh o primeiro kanji da dica ser escolhido

private boolean jogoAcabou;

//This array lists all the individual screens our game has.
final static int[] SCREENS = {
 R.id.screen_game, R.id.screen_main, R.id.screen_sign_in,
 R.id.screen_wait,R.id.decidindoQuemEscolheACategoria,R.id.tela_escolha_categoria, R.id.tela_jogo_multiplayer,R.id.tela_fim_de_jogo};

@Override
public void onCreate(Bundle savedInstanceState) 
{
	
	enableDebugLog(ENABLE_DEBUG, TAG);
	setContentView(R.layout.activity_tela_inicial_multiplayer);
	super.onCreate(savedInstanceState);
	// set up a click listener for everything we care about
	for (int id : CLICKABLES) 
	{
		findViewById(id).setOnClickListener(this);
	}
	
	findViewById(R.id.botao_menu_principal).setOnClickListener(this);
}

/**
* Called by the base class (BaseGameActivity) when sign-in has failed. For
* example, because the user hasn't authenticated yet. We react to this by
* showing the sign-in button.
*/
@Override
public void onSignInFailed() {
Log.d(TAG, "Sign-in failed.");
switchToScreen(R.id.screen_sign_in);
}

/**
* Called by the base class (BaseGameActivity) when sign-in succeeded. We
* react by going to our main screen.
*/
@Override
public void onSignInSucceeded() {
Log.d(TAG, "Sign-in succeeded.");

// register listener so we are notified if we receive an invitation to play
// while we are in the game
Games.Invitations.registerInvitationListener(getApiClient(), this);

// if we received an invite via notification, accept it; otherwise, go to main screen
if (getInvitationId() != null) {
    acceptInviteToRoom(getInvitationId());
    return;
}

//salvarei o email do usuario para adicionar o log dele no banco de dados
AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
Account[] list = manager.getAccounts();

for(Account account: list)
{
    if(account.type.equalsIgnoreCase("com.google"))
    {
        this.emailUsuario = account.name;
        break;
    }
}
switchToMainScreen();
}

@Override
public void onClick(View v) {
Intent intent;

switch (v.getId()) {
    case R.id.button_sign_in:
        // user wants to sign in
        if (!verifyPlaceholderIdsReplaced()) {
            showAlert("Error: sample not set up correctly. Please see README.");
            return;
        }
        beginUserInitiatedSignIn();
        break;
    case R.id.button_sign_out:
        // user wants to sign out
        signOut();
        switchToScreen(R.id.screen_sign_in);
        break;
    case R.id.button_invite_players:
        // show list of invitable players
        intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(getApiClient(), 1, 3);
        switchToScreen(R.id.screen_wait);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
        break;
    case R.id.button_see_invitations:
        // show list of pending invitations
        intent = Games.Invitations.getInvitationInboxIntent(getApiClient());
        switchToScreen(R.id.screen_wait);
        startActivityForResult(intent, RC_INVITATION_INBOX);
        break;
    case R.id.button_accept_popup_invitation:
        // user wants to accept the invitation shown on the invitation popup
        // (the one we got through the OnInvitationReceivedListener).
        acceptInviteToRoom(mIncomingInvitationId);
        mIncomingInvitationId = null;
        break;
    case R.id.button_quick_game:
        // user wants to play against a random opponent right now
        startQuickGame();
        break;
    case R.id.karuta1_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta1 = (TextView) findViewById(R.id.texto_karuta1);
        	String textoKaruta1 = textViewKaruta1.getText().toString();
        	String textoKanjiDaDica = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta1.compareTo(textoKanjiDaDica) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta1 = (ImageView) findViewById(R.id.karuta1_imageview);
        		imageViewKaruta1.setImageResource(R.drawable.karutax); //mudei a figura da carta
        		findViewById(R.id.karuta1).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta1.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta1");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        		
        	}
        	else
        	{
        		//errou
        		
        		//devemos executar o som que indica que ele errou uma carta
        		super.reproduzirSfx("errou_carta");
        		
        		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(0)); //colocar no log que ele errou este kanji
        		this.realizarProcedimentoUsuarioErrouCarta();
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=1");
    		this.realizarProcedimentoReviverCarta(1,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    		
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(0);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(0);
    		}
    	}
    	
    	break;
    case R.id.karuta2_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta2 = (TextView) findViewById(R.id.texto_karuta2);
        	String textoKaruta2 = textViewKaruta2.getText().toString();
        	String textoKanjiDaDica2 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta2.compareTo(textoKanjiDaDica2) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta2 = (ImageView) findViewById(R.id.karuta2_imageview);
        		imageViewKaruta2.setImageResource(R.drawable.karutax); //mudei a figura da carta
        		findViewById(R.id.karuta2).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta2.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta2");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou
        		
        		//devemos executar o som que indica que ele errou uma carta
        		super.reproduzirSfx("errou_carta");
        		
        		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(1)); //colocar no log que ele errou este kanji
        		this.realizarProcedimentoUsuarioErrouCarta();
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=2");
    		this.realizarProcedimentoReviverCarta(2,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(1);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(1);
    		}
    	}
    	break;
    case R.id.karuta3_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta3 = (TextView) findViewById(R.id.texto_karuta3);
        	String textoKaruta3 = textViewKaruta3.getText().toString();
        	String textoKanjiDaDica3 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta3.compareTo(textoKanjiDaDica3) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta3 = (ImageView) findViewById(R.id.karuta3_imageview);
        		imageViewKaruta3.setImageResource(R.drawable.karutax); //mudei a figura da carta
        		findViewById(R.id.karuta3).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta3.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta3");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou
        		
        		//devemos executar o som que indica que ele errou uma carta
        		super.reproduzirSfx("errou_carta");
        		
        		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(2)); //colocar no log que ele errou este kanji
        		this.realizarProcedimentoUsuarioErrouCarta();
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=3");
    		this.realizarProcedimentoReviverCarta(3,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(2);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(2);
    		}
    	}
    	break;
    case R.id.karuta4_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta4 = (TextView) findViewById(R.id.texto_karuta4);
        	String textoKaruta4 = textViewKaruta4.getText().toString();
        	String textoKanjiDaDica4 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta4.compareTo(textoKanjiDaDica4) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta4 = (ImageView) findViewById(R.id.karuta4_imageview);
        		imageViewKaruta4.setImageResource(R.drawable.karutax); //mudei a figura da carta
        		findViewById(R.id.karuta4).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta4.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta4");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou
        		
        		//devemos executar o som que indica que ele errou uma carta
        		super.reproduzirSfx("errou_carta");
        		
        		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(3)); //colocar no log que ele errou este kanji
        		this.realizarProcedimentoUsuarioErrouCarta();
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=4");
    		this.realizarProcedimentoReviverCarta(4,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(3);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(3);
    		}
    	}
    	break;
    case R.id.karuta5_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta5 = (TextView) findViewById(R.id.texto_karuta5);
        	String textoKaruta5 = textViewKaruta5.getText().toString();
        	String textoKanjiDaDica5 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta5.compareTo(textoKanjiDaDica5) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
        		imageViewKaruta5.setImageResource(R.drawable.karutax); //mudei a figura da carta
        		findViewById(R.id.karuta5).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta5.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta5");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou
        		
        		//devemos executar o som que indica que ele errou uma carta
        		super.reproduzirSfx("errou_carta");
        		
        		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(4)); //colocar no log que ele errou este kanji
        		this.realizarProcedimentoUsuarioErrouCarta();
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=5");
    		this.realizarProcedimentoReviverCarta(5,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(4);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(4);
    		}
    	}
    	break;
    case R.id.karuta6_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
        	String textoKaruta6 = textViewKaruta6.getText().toString();
        	String textoKanjiDaDica6 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta6.compareTo(textoKanjiDaDica6) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
        		imageViewKaruta6.setImageResource(R.drawable.karutax); //mudei a figura da carta
        		findViewById(R.id.karuta6).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta6.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta6");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou
        		
        		//devemos executar o som que indica que ele errou uma carta
        		super.reproduzirSfx("errou_carta");
        		
        		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(5)); //colocar no log que ele errou este kanji
        		this.realizarProcedimentoUsuarioErrouCarta();
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=6");
    		this.realizarProcedimentoReviverCarta(6,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(5);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(5);
    		}
    	}
    	break;
    case R.id.karuta7_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
        	String textoKaruta7 = textViewKaruta7.getText().toString();
        	String textoKanjiDaDica7 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta7.compareTo(textoKanjiDaDica7) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
        		imageViewKaruta7.setImageResource(R.drawable.karutax); //mudei a figura da carta
        		findViewById(R.id.karuta7).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta7.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta7");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou
        		
        		//devemos executar o som que indica que ele errou uma carta
        		super.reproduzirSfx("errou_carta");
        		
        		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(6)); //colocar no log que ele errou este kanji
        		this.realizarProcedimentoUsuarioErrouCarta();
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=7");
    		this.realizarProcedimentoReviverCarta(7,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(6);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(6);
    		}
    	}
    	break;
    case R.id.karuta8_imageview:
    	if(this.usouTrovaoTiraCarta == false && this.usouReviveCarta == false) //no caso do trovao e do revive, ele clica numa carta p elimina-la
    	{
    		TextView textViewKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
        	String textoKaruta8 = textViewKaruta8.getText().toString();
        	String textoKanjiDaDica8 = this.kanjiDaDica.getKanji();
        	
        	if(textoKaruta8.compareTo(textoKanjiDaDica8) == 0)
        	{
        		//usuario acertou o kanji. Temos de aumentar seus pontos e informar ao adversario que a dica mudou e que nao pode mais clicar nessa carta
        		this.aumentarPontuacaoComBaseNaDificuldadeDoKanji();
        		
        		//tb devemos executar o som que indica que ele acertou uma carta
        		super.reproduzirSfx("acertou_carta");
        		
        		this.palavrasAcertadas.add(this.kanjiDaDica); //ele acertou mais uma palavra para o log
        		ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
        		imageViewKaruta8.setImageResource(R.drawable.karutax); //mudei a figura da carta
        		findViewById(R.id.karuta8).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
        		textViewKaruta8.setText("");
        		this.alertarAoAdversarioQueACartaNaoEhMaisClicavel("karuta8");
        		this.gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo();
        		
        		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
        		this.verificarSeJogadorRecebeUmItemAleatorio();
        	}
        	else
        	{
        		//errou
        		
        		//devemos executar o som que indica que ele errou uma carta
        		super.reproduzirSfx("errou_carta");
        		
        		this.palavrasErradas.add(this.kanjisDasCartasNaTela.get(7)); //colocar no log que ele errou este kanji
        		this.realizarProcedimentoUsuarioErrouCarta();
        	}
    	}
    	else if(this.usouReviveCarta == true)
    	{
    		this.mandarMensagemMultiplayer("item revivercarta numeroCarta=8");
    		this.realizarProcedimentoReviverCarta(8,false);
    		this.usouReviveCarta = false; //nao esquecer que ele perde o item
    		ImageView imageViewItem = (ImageView)findViewById(R.id.item);
    		imageViewItem.setImageResource(R.drawable.nenhumitem);
    	}
    	else
    	{
    		KanjiTreinar kanjiCartaQueEleClicou = this.kanjisDasCartasNaTela.get(7);
    		ImageView imagemItem = (ImageView) findViewById(R.id.item);
   		 	imagemItem.setImageResource(R.drawable.nenhumitem);
   		 	
    		if((this.kanjiDaDica.getKanji().compareTo(kanjiCartaQueEleClicou.getKanji()) == 0)
    			&& (this.kanjiDaDica.getCategoriaAssociada().compareTo(kanjiCartaQueEleClicou.getCategoriaAssociada()) == 0))
    		{
    			//usuario quer tirar do jogo a carta com a dica. Nao pode
    			String mensagemErro = getResources().getString(R.string.mensagem_trovao_tira_carta_tira_dica);
    			Toast t = Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG);
    		    t.show();
    		    
    		    usouTrovaoTiraCarta = false; //nao esquecer que ele perde o item, entao o booleano eh falso
    		}
    		else
    		{
        		this.lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(7);
    		}
    	}
    	break;
    case R.id.item:
    	this.usarItemAtual();
    	break;
    case R.id.botao_menu_principal:
    	this.voltarAoMenuInicial(null);
    	break;
}
}

/*iremos alterar a pontuacao do usuario com base na dificuldade do kanji que ele acabou de acertar.
  A sua pontuacao cresce aqui, mas a do adversario cresce somente se ele acertar alguma carta e vc recebeu
  uma mensagem dizendo que a carta X nao estah mais clicavel*/
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

void startQuickGame() {
// quick-start a game with 1 randomly selected opponent
final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
        MAX_OPPONENTS, 0);
RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
rtmConfigBuilder.setMessageReceivedListener(this);
rtmConfigBuilder.setRoomStatusUpdateListener(this);
rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);

rtmConfigBuilder.setVariant(2); //somente dois usuarios com o mesmo variante podem jogar juntos no automatch. Usaremos o nivel do usuario como esse variante

switchToScreen(R.id.screen_wait);
keepScreenOn();
resetGameVars();
Games.RealTimeMultiplayer.create(getApiClient(), rtmConfigBuilder.build());
}

@Override
public void onActivityResult(int requestCode, int responseCode,
    Intent intent) {
super.onActivityResult(requestCode, responseCode, intent);

switch (requestCode) {
    case RC_SELECT_PLAYERS:
        // we got the result from the "select players" UI -- ready to create the room
        handleSelectPlayersResult(responseCode, intent);
        break;
    case RC_INVITATION_INBOX:
        // we got the result from the "select invitation" UI (invitation inbox). We're
        // ready to accept the selected invitation:
        handleInvitationInboxResult(responseCode, intent);
        break;
    case RC_WAITING_ROOM:
        // we got the result from the "waiting room" UI.
        if (responseCode == Activity.RESULT_OK) {
            // ready to start playing
            Log.d(TAG, "Starting game (waiting room returned OK).");
            startGame(true);
        } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
            // player indicated that they want to leave the room
            leaveRoom();
        } else if (responseCode == Activity.RESULT_CANCELED) {
            // Dialog was cancelled (user pressed back key, for instance). In our game,
            // this means leaving the room too. In more elaborate games, this could mean
            // something else (like minimizing the waiting room UI).
            leaveRoom();
        }
        break;
}
}

// Handle the result of the "Select players UI" we launched when the user clicked the
// "Invite friends" button. We react by creating a room with those players.
private void handleSelectPlayersResult(int response, Intent data) {
if (response != Activity.RESULT_OK) {
    Log.w(TAG, "*** select players UI cancelled, " + response);
    switchToMainScreen();
    return;
}

Log.d(TAG, "Select players UI succeeded.");

// get the invitee list
final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
Log.d(TAG, "Invitee count: " + invitees.size());

// get the automatch criteria
Bundle autoMatchCriteria = null;
int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
    autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
            minAutoMatchPlayers, maxAutoMatchPlayers, 0);
    Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
}

// create the room
Log.d(TAG, "Creating room...");
RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
rtmConfigBuilder.addPlayersToInvite(invitees);
rtmConfigBuilder.setMessageReceivedListener(this);
rtmConfigBuilder.setRoomStatusUpdateListener(this);
if (autoMatchCriteria != null) {
    rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
}
switchToScreen(R.id.screen_wait);
keepScreenOn();
resetGameVars();
Games.RealTimeMultiplayer.create(getApiClient(), rtmConfigBuilder.build());
Log.d(TAG, "Room created, waiting for it to be ready...");
}

// Handle the result of the invitation inbox UI, where the player can pick an invitation
// to accept. We react by accepting the selected invitation, if any.
private void handleInvitationInboxResult(int response, Intent data) {
if (response != Activity.RESULT_OK) {
    Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
    switchToMainScreen();
    return;
}

Log.d(TAG, "Invitation inbox UI succeeded.");
Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

// accept invitation
acceptInviteToRoom(inv.getInvitationId());
}

// Accept the given invitation.
void acceptInviteToRoom(String invId) {
// accept the invitation
Log.d(TAG, "Accepting invitation: " + invId);
RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
roomConfigBuilder.setInvitationIdToAccept(invId)
        .setMessageReceivedListener(this)
        .setRoomStatusUpdateListener(this);
switchToScreen(R.id.screen_wait);
keepScreenOn();
resetGameVars();
Games.RealTimeMultiplayer.join(getApiClient(), roomConfigBuilder.build());
}

// Activity is going to the background. We have to leave the current room.
@Override
public void onStop() {
Log.d(TAG, "**** got onStop");

// if we're in a room, leave it.
leaveRoom();

// stop trying to keep the screen on
stopKeepingScreenOn();

switchToScreen(R.id.screen_wait);
super.onStop();
}

// Activity just got to the foreground. We switch to the wait screen because we will now
// go through the sign-in flow (remember that, yes, every time the Activity comes back to the
// foreground we go through the sign-in flow -- but if the user is already authenticated,
// this flow simply succeeds and is imperceptible).
@Override
public void onStart() {
switchToScreen(R.id.screen_wait);
super.onStart();
}

// Handle back key to make sure we cleanly leave a game if we are in the middle of one
@Override
public boolean onKeyDown(int keyCode, KeyEvent e) {
if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game) {
    leaveRoom();
    return true;
}
return super.onKeyDown(keyCode, e);
}

// Leave the room.
void leaveRoom() {
Log.d(TAG, "Leaving room.");
tempoRestante = 0;
stopKeepingScreenOn();
if (mRoomId != null) {
    Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
    mRoomId = null;
    switchToScreen(R.id.screen_wait);
} else {
    switchToMainScreen();
}
}

// Show the waiting room UI to track the progress of other players as they enter the
// room and get connected.
void showWaitingRoom(Room room) {
// minimum number of players required for our game
// For simplicity, we require everyone to join the game before we start it
// (this is signaled by Integer.MAX_VALUE).
//final int MIN_PLAYERS = Integer.MAX_VALUE;
final int MIN_PLAYERS = 2;
Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(getApiClient(), room, MIN_PLAYERS);

// show waiting room UI
startActivityForResult(i, RC_WAITING_ROOM);
}

// Called when we get an invitation to play a game. We react by showing that to the user.
@Override
public void onInvitationReceived(Invitation invitation) {
// We got an invitation to play a game! So, store it in
// mIncomingInvitationId
// and show the popup on the screen.
mIncomingInvitationId = invitation.getInvitationId();
((TextView) findViewById(R.id.incoming_invitation_text)).setText(
        invitation.getInviter().getDisplayName() + " " +
                getString(R.string.is_inviting_you));
switchToScreen(mCurScreen); // This will show the invitation popup
}

@Override
public void onInvitationRemoved(String invitationId) {
if (mIncomingInvitationId.equals(invitationId)) {
    mIncomingInvitationId = null;
    switchToScreen(mCurScreen); // This will hide the invitation popup
}
}

/*
* CALLBACKS SECTION. This section shows how we implement the several games
* API callbacks.
*/

// Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
// is connected yet).
@Override
public void onConnectedToRoom(Room room) {
Log.d(TAG, "onConnectedToRoom.");

// get room ID, participants and my ID:
mRoomId = room.getRoomId();
this.room = room;
mParticipants = room.getParticipants();
mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(getApiClient()));

// print out the list of participants (for debug purposes)
Log.d(TAG, "Room ID: " + mRoomId);
Log.d(TAG, "My ID " + mMyId);
Log.d(TAG, "<< CONNECTED TO ROOM>>");
}

// Called when we've successfully left the room (this happens a result of voluntarily leaving
// via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
@Override
public void onLeftRoom(int statusCode, String roomId) {
// we have left the room; return to main screen.
Log.d(TAG, "onLeftRoom, code " + statusCode);
switchToMainScreen();
}

// Called when we get disconnected from the room. We return to the main screen.
@Override
public void onDisconnectedFromRoom(Room room) {
mRoomId = null;
showGameError();
}

// Show error message about game being cancelled and return to main screen.
void showGameError() {
showAlert(getString(R.string.game_problem));
switchToMainScreen();
}

// Called when room has been created
@Override
public void onRoomCreated(int statusCode, Room room) {
Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
if (statusCode != GamesStatusCodes.STATUS_OK) {
    Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
    showGameError();
    return;
}

// show the waiting room UI
showWaitingRoom(room);
}

// Called when room is fully connected.
@Override
public void onRoomConnected(int statusCode, Room room) {
Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
if (statusCode != GamesStatusCodes.STATUS_OK) {
    Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
    showGameError();
    return;
}
updateRoom(room);
}

@Override
public void onJoinedRoom(int statusCode, Room room) {
Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
if (statusCode != GamesStatusCodes.STATUS_OK) {
    Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
    showGameError();
    return;
}

// show the waiting room UI
showWaitingRoom(room);
}

// We treat most of the room update callbacks in the same way: we update our list of
// participants and update the display. In a real game we would also have to check if that
// change requires some action like removing the corresponding player avatar from the screen,
// etc.
@Override
public void onPeerDeclined(Room room, List<String> arg1) {
updateRoom(room);
}

@Override
public void onPeerInvitedToRoom(Room room, List<String> arg1) {
updateRoom(room);
}

@Override
public void onP2PDisconnected(String participant) {
}

@Override
public void onP2PConnected(String participant) {
}

@Override
public void onPeerJoined(Room room, List<String> arg1) {
updateRoom(room);
}

@Override
public void onPeerLeft(Room room, List<String> peersWhoLeft) {
updateRoom(room);
}

@Override
public void onRoomAutoMatching(Room room) {
updateRoom(room);
}

@Override
public void onRoomConnecting(Room room) {
updateRoom(room);
}

@Override
public void onPeersConnected(Room room, List<String> peers) {
updateRoom(room);
}

@Override
public void onPeersDisconnected(Room room, List<String> peers) {
updateRoom(room);
}

void updateRoom(Room room) {
if (room != null) {
    mParticipants = room.getParticipants();
}
if (mParticipants != null) {
}
}

/*
* GAME LOGIC SECTION. Methods that implement the game's rules.
*/

// Current state of the game:
int tempoRestante = -1; // how long until the game ends (seconds)
final static int GAME_DURATION = 90; // game duration, seconds.
int mScore = 0; // user's current score

// Reset game variables in preparation for a new game.
void resetGameVars() {
tempoRestante = GAME_DURATION;
mScore = 0;
}

// Start the gameplay phase of the game.
void startGame(boolean multiplayer) 
{
	mMultiplayer = multiplayer;
	
	this.enviarSeuEmailParaOAdversario();
	
	switchToScreen(R.id.decidindoQuemEscolheACategoria);
	this.decidirQuemEscolheACategoria();
}





/*
* UI SECTION. Methods that implement the game's UI.
*/

// This array lists everything that's clickable, so we can install click
// event handlers.
final static int[] CLICKABLES = {
    R.id.button_accept_popup_invitation, R.id.button_invite_players,
    R.id.button_quick_game, R.id.button_see_invitations, R.id.button_sign_in,
    R.id.button_sign_out,
};

int mCurScreen = -1;

void switchToScreen(int screenId) {
// make the requested screen visible; hide all others.
for (int id : SCREENS) {
    findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
}
mCurScreen = screenId;

// should we show the invitation popup?
boolean showInvPopup;
if (mIncomingInvitationId == null) {
    // no invitation, so no popup
    showInvPopup = false;
} else if (mMultiplayer) {
    // if in multiplayer, only show invitation on main screen
    showInvPopup = (mCurScreen == R.id.screen_main);
} else {
    // single-player: show on main screen and gameplay screen
    showInvPopup = (mCurScreen == R.id.screen_main || mCurScreen == R.id.screen_game);
}
findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
}

void switchToMainScreen() {
switchToScreen(isSignedIn() ? R.id.screen_main : R.id.screen_sign_in);
}




/*
* MISC SECTION. Miscellaneous methods.
*/

/**
* Checks that the developer (that's you!) read the instructions. IMPORTANT:
* a method like this SHOULD NOT EXIST in your production app! It merely
* exists here to check that anyone running THIS PARTICULAR SAMPLE did what
* they were supposed to in order for the sample to work.
*/
boolean verifyPlaceholderIdsReplaced() {
final boolean CHECK_PKGNAME = true; // set to false to disable check
                                    // (not recommended!)

// Did the developer forget to change the package name?
if (CHECK_PKGNAME && getPackageName().startsWith("com.google.example.")) {
    Log.e(TAG, "*** Sample setup problem: " +
        "package name cannot be com.google.example.*. Use your own " +
        "package name.");
    return false;
}

// Did the developer forget to replace a placeholder ID?
int res_ids[] = new int[] {
        R.string.app_id
};
for (int i : res_ids) {
    if (getString(i).equalsIgnoreCase("ReplaceMe")) {
        Log.e(TAG, "*** Sample setup problem: You must replace all " +
            "placeholder IDs in the ids.xml file by your project's IDs.");
        return false;
    }
}
return true;
}

// Sets the flag to keep this screen on. It's recommended to do that during
// the
// handshake when setting up a game, because if the screen turns off, the
// game will be
// cancelled.
void keepScreenOn() {
getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
}

// Clears the flag that keeps the screen on.
void stopKeepingScreenOn() {
getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
}

/*
* COMMUNICATIONS SECTION. Methods that implement the game's network
* protocol.
*/

// Called when we receive a real-time message from the network.
// Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
// indicating
// whether it's a final or interim score. The second byte is the score.
// There is also the
// 'S' message, which indicates that the game should start.
@Override
public void onRealTimeMessageReceived(RealTimeMessage rtm) 
{
	byte[] buf = rtm.getMessageData();
	String sender = rtm.getSenderParticipantId();

	String mensagem = "";
	try {
		mensagem = new String(buf, "UTF-8");
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	/*Toast t = Toast.makeText(this, "mensagem recebida:" + mensagem, Toast.LENGTH_LONG);
    t.show();*/
    
	if(mensagem.contains("acertou") == true)
	{
		//o adversario acertou uma das cartas
		if(mensagem.contains("karuta1") == true)
		{
			//adversario acertou a carta 1
			final View karuta1 = findViewById (R.id.karuta1);
			karuta1.setVisibility(View.GONE);
		}
	}
	else if(mensagem.contains("escolheCategoria") == true && finalizouDecisaoEscolheCategoria == false)
	{
		//o adversario e o jogador atual irao escolher aleatoriamente quem ira decidir a lista e essa escolha so
		//irah parar quando os dois decidirem na mesma pessoa
		String jogadorEscolhidoNaTelaDoAdversario = mensagem.replaceFirst("escolheCategoria ", ""); 
		
		if(this.quemEscolheACategoria != null && jogadorEscolhidoNaTelaDoAdversario.compareTo(this.quemEscolheACategoria) == 0)
		{
			//chegou-se a um consenso, basta alertar ao adversario
			this.finalizouDecisaoEscolheCategoria = true;
			mensagem = "escolheCategoria " + this.quemEscolheACategoria;
			this.mandarMensagemMultiplayer(mensagem);
			
			//agora vamos passar para a outra tela do jogo, a de escolha da categoria
			switchToScreen(R.id.tela_escolha_categoria);
			this.decidirCategoria();
			
			
		}
		else
		{
			//mais uma vez gerar o numero aleatorio
			this.decidirQuemEscolheACategoria();
		}
	}
	else if(mensagem.contains("categoria=") && mensagem.contains("enabled="))
	{
		//o usuario que escolheu uma categoria decidiu alertar ao outro user
		String[] stringSeparada = mensagem.split(";");
		
		String categoria = stringSeparada[0].replace("categoria=", "");
		boolean enabled = Boolean.valueOf(stringSeparada[1].replace("enabled=", ""));
		
		ListView listView = (ListView) findViewById(R.id.listaCategorias);
		
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		ArrayList<CategoriaDeKanjiParaListviewSelecionavel> categoriaDeKanjiList = this.dataAdapter.getCategoriaDeKanjiList();
		
		for(int i = 0; i < categoriaDeKanjiList.size(); i++)
		{
			CategoriaDeKanjiParaListviewSelecionavel umaCategoria = categoriaDeKanjiList.get(i);
			if(umaCategoria.getName().compareTo(categoria) == 0)
			{
				umaCategoria.setSelected(enabled);
				listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				listView.setItemChecked(i, enabled);
				
			}
		}

	}
	else if(mensagem.contains("quantasRodadasHaverao=") == true)
	{
		String quantasRodadasString = mensagem.replaceFirst("quantasRodadasHaverao=", "");
		this.quantasRodadasHaverao = Integer.valueOf(quantasRodadasString);
		
		if(this.quantasRodadasHaverao == 1)
		{
			findViewById(R.id.radioButton1).setEnabled(true);
			findViewById(R.id.radioButton2).setEnabled(false);
			findViewById(R.id.radioButton3).setEnabled(false);
			findViewById(R.id.radioButton4).setEnabled(false);
		}
		else if(this.quantasRodadasHaverao == 2)
		{
			findViewById(R.id.radioButton1).setEnabled(false);
			findViewById(R.id.radioButton2).setEnabled(true);
			findViewById(R.id.radioButton3).setEnabled(false);
			findViewById(R.id.radioButton4).setEnabled(false);
		}
		else if(this.quantasRodadasHaverao == 3)
		{
			findViewById(R.id.radioButton1).setEnabled(false);
			findViewById(R.id.radioButton2).setEnabled(false);
			findViewById(R.id.radioButton3).setEnabled(true);
			findViewById(R.id.radioButton4).setEnabled(false);
		}
		else
		{
			findViewById(R.id.radioButton1).setEnabled(false);
			findViewById(R.id.radioButton2).setEnabled(false);
			findViewById(R.id.radioButton3).setEnabled(false);
			findViewById(R.id.radioButton4).setEnabled(true);
		}
	}
	else if(mensagem.contains("mandar dados da partida para singleton") == true)
	{
		//o adversario pediu para o jogador armazenar os dados da partida no singleton(categorias escolhidas e quantas rodadas)
		
		//como eh o comeco da primeira partida do jogo, vamos fazer o usuario ver uma tela de espera pelo menos ate o kanji da dica ser escolhido
		this.comecarEsperaDoUsuarioParaComecoDaPartida();
		
		SingletonGuardaDadosDaPartida.getInstance().setQuantasRodadasHaverao(this.quantasRodadasHaverao);
		
		ArrayList<CategoriaDeKanjiParaListviewSelecionavel> categoriaDeKanjiList = this.dataAdapter.getCategoriaDeKanjiList();
		
		 ArmazenaKanjisPorCategoria conheceKanjisECategorias = ArmazenaKanjisPorCategoria.pegarInstancia();
			for(int i = 0; i < categoriaDeKanjiList.size(); i++)
			{
				CategoriaDeKanjiParaListviewSelecionavel umaCategoria = categoriaDeKanjiList.get(i);
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
			
		//vamos mudar a tela
		switchToScreen(R.id.tela_jogo_multiplayer);
		this.comecarJogoMultiplayer();
		
		//falta avisar ao jogador que escolhe as categorias p sortear os kanjis
		String mensagemParaODono = "pode comecar a escolher os kanjis";
		
		this.mandarMensagemMultiplayer(mensagemParaODono);
		
	}
	else if(mensagem.contains("pode comecar a escolher os kanjis") == true)
	{
		//mensagem enviada do jogador que nao escolhe a categoria para o que escolhe. Eh para que o jogador que escolhe a categoria tambem escolha os kanjis
		//essa mensagem so ocorre uma vez que eh no comeco da primeira partida do jogo
		
		this.pegarTodosOsKanjisQuePodemVirarCartas();
		this.escolher8KanjisParaARodada();
	}
	else if(mensagem.contains("kanjis=") == true && mensagem.contains("item misturarcartas kanjis=") == false)
	{
		//mensagem de quem escolhe categorias p/ dizer ao adversario quais os kanjis que estao na tela dele
		//ex: kanjis=au|cotidiano;me|corpo...
		//No final desse if, eh gerado um kanji da dica tb.
		mensagem = mensagem.replace("kanjis=", "");
		String[] kanjisSeparadosPorPontoEVirgula = mensagem.split(";");
		
		this.mudarCartasNaTela(kanjisSeparadosPorPontoEVirgula);
		
		//tenho de achar cada KanjiTreinar com base na mensagem mandada. Felizmente ja tenho categoria e texto do kanji
		
		if(this.kanjisDasCartasNaTela == null)
		{
			this.kanjisDasCartasNaTela = new LinkedList<KanjiTreinar>();
		}
		if(this.kanjisDasCartasNaTelaQueJaSeTornaramDicas == null)
		{
			this.kanjisDasCartasNaTelaQueJaSeTornaramDicas = new LinkedList<KanjiTreinar>();
		}
		
		this.kanjisDasCartasNaTela.clear(); //se foi de uma rodada para outra, eh bom limpar essa lista
		this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.clear(); //essa tb
		
		for(int i = 0; i < kanjisSeparadosPorPontoEVirgula.length; i++)
		{
			String umKanjiECategoria = kanjisSeparadosPorPontoEVirgula[i];
			String[] kanjiECategoria = umKanjiECategoria.split("\\|");
			String kanji = kanjiECategoria[0];
			String categoria = kanjiECategoria[1];
			
			KanjiTreinar umKanjiTreinar = 
					ArmazenaKanjisPorCategoria.pegarInstancia().acharKanji(categoria, kanji);
			
			this.kanjisDasCartasNaTela.add(umKanjiTreinar);
		}
		
		//serah que foram geradas 8 cartas na tela ou menos?
		
		if(this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() < 8)
		{
			//menos de 8 cartas, por isso algumas devem ficar vazias
			tornarORestoDasCartasNaTelaVazias();
		}
		
		//o jogador que nao escolheu a categoria e nem os 8 kanjis da tela eh o unico que pode criar a dica do kanji
		this.gerarKanjiDaDica();
		
		//apos gerar o kanji da dica e visto que as 8 cartas ja estao na tela, pode-se dispensar o laoding inicial que diz p o usuario que o jogo estah sendo iniciado
		if(this.rodadaAtual == 1)
		{
			this.loadingComecoDaPartida.dismiss();
		}
		
	}
	else if(mensagem.contains("kanjiDaDica=") == true && mensagem.contains("item") == false)
	{
		//alguem acertou algum kanji(ou eh o comeco de tudo) e eh necessairo mudar a dica do kanji
		//e nao eh o item p mudar a dica atual
		//formato: kanjiDaDica=asa|Cotidiano
		String kanjiECategoria = mensagem.replace("kanjiDaDica=", "");
		String[] kanjiECategoriaArray = kanjiECategoria.split("\\|");
		String kanji = kanjiECategoriaArray[0];
		String categoria = kanjiECategoriaArray[1];
		
		this.kanjiDaDica = ArmazenaKanjisPorCategoria.pegarInstancia().acharKanji(categoria, kanji);
		this.palavrasJogadas.add(kanjiDaDica);
		this.alterarTextoDicaComBaseNoKanjiDaDica();
		this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.add(this.kanjiDaDica);
		
		//caso essa seja a primeira rodada e ja foi gerado um kanji da dica inicial, pode-se dispensar o loading que diz que o jogo estah sendo iniciado 
		if(this.rodadaAtual == 1 && this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() == 1)
		{
			this.loadingComecoDaPartida.dismiss();
		}
	}
	else if(mensagem.contains("naoClicavel=") == true)
	{
		//alguem acertou uma carta e por isso essa carta nao deveria ser mais clicavel p ambos os jogadores
		//ex: naoClicavel=karuta1
		String karutaNaoClicavel = mensagem.replace("naoClicavel=", "");
		if(karutaNaoClicavel.compareTo("karuta1") == 0)
		{
			ImageView imageViewKaruta1 = (ImageView) findViewById(R.id.karuta1_imageview);
    		imageViewKaruta1.setImageResource(R.drawable.karutax); //mudei a figura da carta
    		findViewById(R.id.karuta1).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta1 = (TextView) findViewById(R.id.texto_karuta1);
    		textViewKaruta1.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta2") == 0)
		{
			ImageView imageViewKaruta2 = (ImageView) findViewById(R.id.karuta2_imageview);
    		imageViewKaruta2.setImageResource(R.drawable.karutax); //mudei a figura da carta
    		findViewById(R.id.karuta2).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta2 = (TextView) findViewById(R.id.texto_karuta2);
    		textViewKaruta2.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta3") == 0)
		{
			ImageView imageViewKaruta3 = (ImageView) findViewById(R.id.karuta3_imageview);
    		imageViewKaruta3.setImageResource(R.drawable.karutax); //mudei a figura da carta
    		findViewById(R.id.karuta3).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta3 = (TextView) findViewById(R.id.texto_karuta3);
    		textViewKaruta3.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta4") == 0)
		{
			ImageView imageViewKaruta4 = (ImageView) findViewById(R.id.karuta4_imageview);
    		imageViewKaruta4.setImageResource(R.drawable.karutax); //mudei a figura da carta
    		findViewById(R.id.karuta4).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta4 = (TextView) findViewById(R.id.texto_karuta4);
    		textViewKaruta4.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta5") == 0)
		{
			ImageView imageViewKaruta5 = (ImageView) findViewById(R.id.karuta5_imageview);
    		imageViewKaruta5.setImageResource(R.drawable.karutax); //mudei a figura da carta
    		findViewById(R.id.karuta5).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta5 = (TextView) findViewById(R.id.texto_karuta5);
    		textViewKaruta5.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta6") == 0)
		{
			ImageView imageViewKaruta6 = (ImageView) findViewById(R.id.karuta6_imageview);
    		imageViewKaruta6.setImageResource(R.drawable.karutax); //mudei a figura da carta
    		findViewById(R.id.karuta6).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
    		textViewKaruta6.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta7") == 0)
		{
			ImageView imageViewKaruta7 = (ImageView) findViewById(R.id.karuta7_imageview);
    		imageViewKaruta7.setImageResource(R.drawable.karutax); //mudei a figura da carta
    		findViewById(R.id.karuta7).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
    		textViewKaruta7.setText("");
		}
		else if(karutaNaoClicavel.compareTo("karuta8") == 0)
		{
			ImageView imageViewKaruta8 = (ImageView) findViewById(R.id.karuta8_imageview);
    		imageViewKaruta8.setImageResource(R.drawable.karutax); //mudei a figura da carta
    		findViewById(R.id.karuta8).setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
    		TextView textViewKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
    		textViewKaruta8.setText("");
		}
		
		//a cada X cartas que ja se foram do jogo, um item eh aleatoriamente gerado para cada um dos jogadores		
		if(this.kanjiDaDica.getDificuldadeDoKanji() == 1)
		{
			this.pontuacaoDoAdversario = this.pontuacaoDoAdversario + 10;
		}
		else if(this.kanjiDaDica.getDificuldadeDoKanji() == 2)
		{
			this.pontuacaoDoAdversario = this.pontuacaoDoAdversario + 20;
		}
		else
		{
			this.pontuacaoDoAdversario = this.pontuacaoDoAdversario + 30;
		}
		
		this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
		this.verificarSeJogadorRecebeUmItemAleatorio();
	}
	else if(mensagem.contains("rodadaMudou") == true)
	{
		this.rodadaAtual = this.rodadaAtual + 1;
		TextView textViewRodada = (TextView) findViewById(R.id.rodada);
		String rodada = getResources().getString(R.string.rodada);
		textViewRodada.setText(rodada + String.valueOf(this.rodadaAtual));
		
		this.tornarTodasAdCartasNaTelaClicaveisEVaziasNovamente();
	}
	else if(mensagem.contains("gerarMais8Cartas") == true)
	{
		/*somente quem escolhe as categorias recebe essa mensagem q significa que ele deve gerar novas 8 
		 *cartas p a rodada. A rodada acabou de mudar*/
		this.escolher8KanjisParaARodada();
	}
	else if(mensagem.contains("item trovaotiracartaaleatoria indiceCartaRemovida=") == true)
	{
		//o adversario lancou um trovaotiracartaaleatoria e o trovao deve cair em quem recebeu esta mensagem tb
		String indiceCartaRemovidaEmString = mensagem.replace("item trovaotiracartaaleatoria indiceCartaRemovida=", "");
		int indiceCartaRemovida = Integer.valueOf(indiceCartaRemovidaEmString);
		this.lancarTrovaoNaTelaNaCartaDeIndice(indiceCartaRemovida);
	}
	else if(mensagem.contains("item trovaotiracarta indiceCartaRemovida=") == true)
	{
		//o adversario lancou um trovaotiracarta e o trovao deve cair em quem recebeu esta mensagem tb
		String indiceCartaRemovidaEmString = mensagem.replace("item trovaotiracarta indiceCartaRemovida=", "");
		int indiceCartaRemovida = Integer.valueOf(indiceCartaRemovidaEmString);
		this.lancarTrovaoNaTelaNaCartaDeIndice(indiceCartaRemovida);
	}
	else if(mensagem.contains("item parartempo") == true)
	{
		this.realizarProcedimentoPararTempo();
	}
	else if(mensagem.contains("item misturarcartas kanjis=") == true)
	{
		/*os kanjis chegam assim: kanjis=au|cotidiano;kau|cotidiano...*/
		String mensagemKanjis = mensagem.replace("item misturarcartas kanjis=", "");
		String[] kanjisECategorias = mensagemKanjis.split(";");
		
		LinkedList<String> textoKanjisNovos = new LinkedList<String>();
		LinkedList<String> categoriasKanjisNovos = new LinkedList<String>();
		
		for(int i = 0; i < kanjisECategorias.length; i++)
		{
			String umKanjiECategoria = kanjisECategorias[i];
			String[] kanjiECategoriaArray = umKanjiECategoria.split("\\|");
			textoKanjisNovos.add(kanjiECategoriaArray[0]);
			categoriasKanjisNovos.add(kanjiECategoriaArray[1]);
		}
		
		this.misturarCartasRecebeuCartasOutroUsuario(textoKanjisNovos, categoriasKanjisNovos);
	}
	else if(mensagem.contains("item mudardica kanjiDaDica=") == true)
	{
		this.tirarKanjiDicaAtualDeCartasQueJaViraramDicasEPalavrasJogadas();
		
		String kanjiECategoria = mensagem.replace("item mudardica kanjiDaDica=", "");
		String[] kanjiECategoriaArray = kanjiECategoria.split("\\|");
		String kanji = kanjiECategoriaArray[0];
		String categoria = kanjiECategoriaArray[1];
		
		this.kanjiDaDica = ArmazenaKanjisPorCategoria.pegarInstancia().acharKanji(categoria, kanji);
		this.palavrasJogadas.add(kanjiDaDica);
		this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.add(this.kanjiDaDica);
		this.realizarProcedimentoMudandoDicaAtual();
	}
	else if(mensagem.contains("item revivercarta numeroCarta=") == true)
	{
		//adversario reviveu uma carta
		String stringNumeroCartaRevivida = mensagem.replace("item revivercarta numeroCarta=", "");
		int numeroCartaRevivida = Integer.valueOf(stringNumeroCartaRevivida);
		this.realizarProcedimentoReviverCarta(numeroCartaRevivida,true);
	}
	else if(mensagem.contains("termineiDeCarregarListaDeCategoria;") == true)
	{
		//guest manda pro host que jah terminou de carregar lista de categorias
		this.guestTerminouDeCarregarListaDeCategorias = true;
	}
	else if(mensagem.contains("fim de jogo") == true)
	{
		//algum jogador alcancou o fim de jogo e o outro tb deve fazer o mesmo
		switchToScreen(R.id.tela_fim_de_jogo);
		this.jogoAcabou = true;
		comecarFimDeJogo();
	}
	else if(mensagem.contains("oponente falou no chat="))
	{
		String mensagemAdicionarAoChat = mensagem.replaceFirst("oponente falou no chat=", "");
		this.adicionarMensagemNoChat(mensagemAdicionarAoChat, false);
	}
	else if(mensagem.contains("email=") == true)
	{
		this.emailAdversario = mensagem.replace("email=", "");
	}

}


private void decidirQuemEscolheACategoria()
{
	Random generaNumAleatorio = new Random(); 
	int indiceJogadorEscolhido = generaNumAleatorio.nextInt(2);
	
	this.mParticipants = this.room.getParticipants();
	
	
	Participant jogadorEscolhido = this.mParticipants.get(indiceJogadorEscolhido);
	this.quemEscolheACategoria = jogadorEscolhido.getParticipantId();
	
	String mensagem = "escolheCategoria " + this.quemEscolheACategoria;
	
	this.mandarMensagemMultiplayer(mensagem);
}

private void decidirCategoria()
{
	TextView tituloEscolhaCategoria = (TextView) findViewById (R.id.tituloEscolhaCategoria);
	tituloEscolhaCategoria.setVisibility(View.VISIBLE);
	
	findViewById(R.id.listaCategorias).setVisibility(View.VISIBLE);
	findViewById(R.id.imageView1).setVisibility(View.VISIBLE);
	findViewById(R.id.ok_button).setVisibility(View.VISIBLE);
	findViewById(R.id.radioGroup1).setVisibility(View.VISIBLE);
	findViewById(R.id.radioButton1).setVisibility(View.VISIBLE);
	findViewById(R.id.radioButton2).setVisibility(View.VISIBLE);
	findViewById(R.id.radioButton3).setVisibility(View.VISIBLE);
	findViewById(R.id.radioButton4).setVisibility(View.VISIBLE);
	
	if(mMyId.compareTo(quemEscolheACategoria) != 0)
	{
		tituloEscolhaCategoria.setText(R.string.esperandoDecidirCategoria);
		findViewById(R.id.radioButton1).setClickable(false);
		findViewById(R.id.radioButton2).setClickable(false);
		findViewById(R.id.radioButton3).setClickable(false);
		findViewById(R.id.radioButton4).setClickable(false);
		
		findViewById(R.id.ok_button).setVisibility(View.INVISIBLE);
	}
	else
	{
		adicionarListenerBotao();
	}
	
	this.solicitarPorKanjisPraTreino();
	
}


/*NOVO DA ACTIVITY REFERENTE A SELECIONAR CATEGORIAS */
private MyCustomAdapter dataAdapter = null;
private ProgressDialog loadingKanjisDoBd;
private static String jlptEnsinarNaFerramenta = "4";

private void solicitarPorKanjisPraTreino() {
	this.loadingKanjisDoBd = ProgressDialog.show(TelaInicialMultiplayer.this, getResources().getString(R.string.carregando_kanjis_remotamente), getResources().getString(R.string.por_favor_aguarde));
	  SolicitaKanjisParaTreinoTask armazenarMinhasFotos = new SolicitaKanjisParaTreinoTask(this.loadingKanjisDoBd, this);
	  armazenarMinhasFotos.execute("");
	 
}
  
 public void mostrarListaComKanjisAposCarregar() {
  
  //Array list of countries
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
 
  
  boolean possoEscolherCategorias;
  
  if(this.mMyId.compareTo(this.quemEscolheACategoria) == 0)
  {
	  possoEscolherCategorias = true;
  }
  else
  {
	  possoEscolherCategorias = false;
	  this.mandarMensagemMultiplayer("termineiDeCarregarListaDeCategoria;");
	  this.guestTerminouDeCarregarListaDeCategorias = true;
  }
  
  //create an ArrayAdaptar from the String Array
  dataAdapter = new MyCustomAdapter(this,
    R.layout.categoria_de_kanji_na_lista, listaDeCategorias,possoEscolherCategorias,this);
  ListView listView = (ListView) findViewById(R.id.listaCategorias);
  // Assign adapter to ListView
  listView.setAdapter(dataAdapter);
  
  listView.setOnItemClickListener(new OnItemClickListener() {
   public void onItemClick(AdapterView parent, View view,
     int position, long id) 
   {
		// When clicked, show a toast with the TextView text
		    CategoriaDeKanjiParaListviewSelecionavel categoriaDeKanji = (CategoriaDeKanjiParaListviewSelecionavel) parent.getItemAtPosition(position);
		    Toast.makeText(getApplicationContext(),
		      "Clicked on Row: " + categoriaDeKanji.getName(),
		      Toast.LENGTH_LONG).show();
		  
		 
   }
  });
  
 }
  

 private void adicionarListenerBotao() {
  
  
  Button myButton = (Button) findViewById(R.id.ok_button);
  myButton.setOnClickListener(new OnClickListener() {
  
   @Override
   public void onClick(View v) 
   {
	   quemEscolheCategoriasClicouNoBotaoOk();
	   comecarJogoMultiplayer();
	   
   }
  });
  
 }
 
 public void mandarMensagemMultiplayer(String mensagem)
 {
	 byte[] mensagemEmBytes = mensagem.getBytes();
	 for (Participant p : mParticipants) 
		{
			if (p.getParticipantId().equals(mMyId))
			{
				continue;
			}
		    else
		    {
		    	Games.RealTimeMultiplayer.sendReliableMessage(getApiClient(),null, mensagemEmBytes, mRoomId,
			            p.getParticipantId());
		    }
		}
 }
 
 public void onRadioButtonClicked(View view) 
 {
	    // O radioButton esta marcado?
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.radioButton1:
	            if (checked)
	                this.quantasRodadasHaverao = 1;
	            break;
	        case R.id.radioButton2:
	            if (checked)
	            	this.quantasRodadasHaverao = 2;
	            break;
	        case R.id.radioButton3:
	            if (checked)
	            	this.quantasRodadasHaverao = 3;
	            break;
	        case R.id.radioButton4:
	            if (checked)
	            	this.quantasRodadasHaverao = 99; //infinitas rodadas
	            break;
	    }
	    
	    String quantasRodadasHaveraoString = "quantasRodadasHaverao=" + this.quantasRodadasHaverao;
	    this.mandarMensagemMultiplayer(quantasRodadasHaveraoString);
}

 private void quemEscolheCategoriasClicouNoBotaoOk()
 {
	 //primeiro iremos armazenar no singleton todas as categorias escolhidas e kanjis delas
	 ArrayList<CategoriaDeKanjiParaListviewSelecionavel> categoriaDeKanjiList = this.dataAdapter.getCategoriaDeKanjiList();
	
	 if(categoriaDeKanjiList.size() == 0)
	 {
		 String mensagem = getResources().getString(R.string.erroEscolherCategorias);
		 Toast t = Toast.makeText(this, mensagem, Toast.LENGTH_LONG);
		 t.show();
	 }
	 else
	 {
		 SingletonGuardaDadosDaPartida.getInstance().limparCategoriasEKanjis();
		 
		 ArmazenaKanjisPorCategoria conheceKanjisECategorias = ArmazenaKanjisPorCategoria.pegarInstancia();
			for(int i = 0; i < categoriaDeKanjiList.size(); i++)
			{
				CategoriaDeKanjiParaListviewSelecionavel umaCategoria = categoriaDeKanjiList.get(i);
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
			
		//Agora vamos armazenar quantas rodadas o jogo terah no singleton
		SingletonGuardaDadosDaPartida.getInstance().setQuantasRodadasHaverao(quantasRodadasHaverao);
		
		//Agora falta alertar ao outro jogador que ele precisa mudar tb, mas nao se preocupe que o MyCustomAdapter ja mantem as categorias selecionadas tb e o quantasRodadasHaverao foi atualizado tb
		String stringAlertarJogador = "mandar dados da partida para singleton";
		this.mandarMensagemMultiplayer(stringAlertarJogador);
		 
		 //por fim, vamos mudar a tela
		 switchToScreen(R.id.tela_jogo_multiplayer);
	 }
	 
	//como eh o comeco da primeira partida do jogo, vamos fazer o usuario ver uma tela de espera pelo menos ate o kanji da dica ser escolhido
	this.comecarEsperaDoUsuarioParaComecoDaPartida();
		
 }
 
 private void comecarJogoMultiplayer()
 {
	 findViewById(R.id.pontuacao).setVisibility(View.VISIBLE);
	 findViewById(R.id.rodada).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta1).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta2).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta3).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta4).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta5).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta6).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta7).setVisibility(View.VISIBLE);
	 findViewById(R.id.karuta8).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta1).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta2).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta3).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta4).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta5).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta6).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta7).setVisibility(View.VISIBLE);
	 findViewById(R.id.texto_karuta8).setVisibility(View.VISIBLE);
	 
	 findViewById(R.id.item).setVisibility(View.VISIBLE);
	 findViewById(R.id.tempo).setVisibility(View.VISIBLE);
	 findViewById(R.id.label_item).setVisibility(View.VISIBLE);
	 findViewById(R.id.dica_kanji_layout).setVisibility(View.VISIBLE);
	 findViewById(R.id.dica_kanji).setVisibility(View.VISIBLE);
	 findViewById(R.id.balao_fala).setVisibility(View.VISIBLE);
	 findViewById(R.id.parartempopequeno).setVisibility(View.INVISIBLE);
	 
	 findViewById(R.id.karuta1_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta2_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta3_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta4_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta5_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta6_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta7_imageview).setOnClickListener(this);
	 findViewById(R.id.karuta8_imageview).setOnClickListener(this);
	 
	 findViewById(R.id.item).setOnClickListener(this);
	 findViewById(R.id.item).setClickable(false);
	 
	 TextView textViewPontuacao = (TextView) findViewById(R.id.pontuacao);
	 String pontuacao = getResources().getString(R.string.pontuacao);
	 textViewPontuacao.setText(pontuacao + "0");
	 
	 this.rodadaAtual = 1;
	 TextView textViewRodada = (TextView) findViewById(R.id.rodada);
	 String rodada = getResources().getString(R.string.rodada);
	 textViewRodada.setText(rodada + "1");
	 
	 this.quantasCartasJaSairamDoJogo = 0;
	 this.suaPontuacao = 0;
	 this.pontuacaoDoAdversario = 0;
	 this.palavrasAcertadas = new LinkedList<KanjiTreinar>();
	 this.palavrasErradas = new LinkedList<KanjiTreinar>();
	 this.palavrasJogadas = new LinkedList<KanjiTreinar>();
	 
	 this.itemAtual = "";
	 this.itensDoGanhador = new LinkedList<String>();
	 this.itensDoPerdedor = new LinkedList<String>();
	 //itensDoGanhador.add("trovaotiracartaaleatoria");
	 itensDoGanhador.add("parartempo");
	 //itensDoGanhador.add("misturarcartas");
	 //itensDoPerdedor.add("mudardica");
	 //itensDoGanhador.add("doisx");
	 //itensDoPerdedor.add("segundamao");
	 //itensDoPerdedor.add("areadica");
	 //itensDoPerdedor.add("trovaotiracarta");
	 itensDoPerdedor.add("revivecarta");
	 
	 this.usouTrovaoTiraCarta = false;
	 this.usouReviveCarta = false;
	 
	 TextView textoTempo = (TextView) findViewById(R.id.tempo);
	 String stringTempo = getResources().getString(R.string.tempo_restante);
	 textoTempo.setText(stringTempo + "1:30");
	 tempoEstahParado = false;
	 this.jogoAcabou = false;
	 
	 /*this.timerTaskDecrementaTempoRestante = new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        TelaInicialMultiplayer.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	if(tempoEstahParado == false)
		            	{
		            		passarUmSegundo();
		            	}
		            }
		        });
		    }
	 };
     new Timer().schedule(this.timerTaskDecrementaTempoRestante, 1000);*/
	 
	 final Handler h = new Handler();
     h.postDelayed(new Runnable() {
         @Override
         public void run() 
         {
             if (tempoEstahParado == false)
             {
            	 TelaInicialMultiplayer.this.runOnUiThread(new Runnable() 
 		        {
 		            @Override
 		            public void run() 
 		            {
 		            	passarUmSegundo();
 		            	if(tempoRestante <= 0)
 		            	{
 		            		//o jogo deve acabar
 		            		
 		            		if(jogoAcabou == false)
 		            		{
 		            			terminarJogoEEnviarMesagemAoAdversario();
 		            		}
 		            	}
 		            }
 		        });
             }
             
             h.postDelayed(this, 1000); 
             
         }
     }, 1000);
	 
     
     //falta iniciar a musica de fundo do jogo
     this.mudarMusicaDeFundo(R.raw.radiate);
	 
 }
 
 private void passarUmSegundo()
 {
	 this.tempoRestante = this.tempoRestante - 1;
	 String tempoParaMostrar = ""; //n irei mostrar 90s, irei mostrar 1:30,1:29,0:30...
	 if(tempoRestante < 60)
	 {
		 if(tempoRestante < 10)
		 {
			 tempoParaMostrar = "0:0" + String.valueOf(tempoRestante);
		 }
		 else
		 {
			 tempoParaMostrar = "0:" + String.valueOf(tempoRestante);
		 }
	 }
	 else
	 {
		 int segundosMenosUmMinuto = this.tempoRestante - 60;
		 if(segundosMenosUmMinuto < 10)
		 {
			 tempoParaMostrar = "1:0" + String.valueOf(segundosMenosUmMinuto);
		 }
		 else
		 {
			 tempoParaMostrar = "1:" + String.valueOf(segundosMenosUmMinuto);
		 }
	 }
	 
	 TextView textoTempo = (TextView) findViewById(R.id.tempo);
	 String stringTempo = getResources().getString(R.string.tempo_restante);
	 textoTempo.setText(stringTempo + tempoParaMostrar);
 }
 
 /*alem da funcao abaixo mudar cada uma das cartas na tela e decidir quais os 8 kanjis da rodada serao usados,
  * ela tb manda uma mensagem ao jogador que nao eh quem escolhe a categoria avisando quais kanjis entraram.
  * A funcao so eh executada pelo jogador que eh que escolhe as categorias*/
 private void escolher8KanjisParaARodada()
 {
	 if(this.mMyId.compareTo(this.quemEscolheACategoria) == 0)
	 {
		 //quem escolhe a categoria quem vai sortear os kanjis

		 if(kanjisDasCartasNaTela == null)
		 {
			 this.kanjisDasCartasNaTela = new LinkedList<KanjiTreinar>();
		 }
		 if(kanjisDasCartasNaTelaQueJaSeTornaramDicas == null)
		 {
			 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas = new LinkedList<KanjiTreinar>();
		 }
		 
		 this.kanjisDasCartasNaTela.clear();
		 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.clear();
		 
		 for(int i = 0; i < 8; i++)
		 {
			 KanjiTreinar kanjiTreinar = this.escolherUmKanjiParaTreinar();
			 
			 if(kanjiTreinar == null)
			 {
				 //acabaram-se os kanjis que posso usar na tela
				 this.tornarORestoDasCartasNaTelaVazias();
				 break;
				 
			 }
			 else
			 { 
				 this.kanjisDasCartasNaTela.add(kanjiTreinar);
				 
				 if(i == 0)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta1);
					 texto.setText(kanjiTreinar.getKanji());
					 findViewById(R.id.karuta1_imageview).setClickable(true);
				 }
				 else if(i == 1)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta2);
					 texto.setText(kanjiTreinar.getKanji());
					 findViewById(R.id.karuta2_imageview).setClickable(true);
				 }
				 else if(i == 2)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta3);
					 texto.setText(kanjiTreinar.getKanji());
					 findViewById(R.id.karuta3_imageview).setClickable(true);
				 }
				 else if(i == 3)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta4);
					 texto.setText(kanjiTreinar.getKanji());
					 findViewById(R.id.karuta4_imageview).setClickable(true);
				 }
				 else if(i == 4)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta5);
					 texto.setText(kanjiTreinar.getKanji());
					 findViewById(R.id.karuta5_imageview).setClickable(true);
				 }
				 else if(i == 5)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta6);
					 texto.setText(kanjiTreinar.getKanji());
					 findViewById(R.id.karuta6_imageview).setClickable(true);
				 }
				 else if(i == 6)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta7);
					 texto.setText(kanjiTreinar.getKanji());
					 findViewById(R.id.karuta7_imageview).setClickable(true);
				 }
				 else if(i == 7)
				 {
					 TextView texto = (TextView) findViewById(R.id.texto_karuta8);
					 texto.setText(kanjiTreinar.getKanji());
					 findViewById(R.id.karuta8_imageview).setClickable(true);
				 } 
				 
			 }
		 }
		 
		 //falta agora avisar ao outro jogador quais as cartas na tela
		 String kanjisString = "kanjis=";
		 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
		 {
			 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
			 if(i < this.kanjisDasCartasNaTela.size() - 1)
			 {
				 kanjisString = kanjisString + umKanji.getKanji() + "|" + umKanji.getCategoriaAssociada() + ";";
			 }
			 else
			 {
				 kanjisString = kanjisString + umKanji.getKanji() + "|" + umKanji.getCategoriaAssociada();
			 }
		 }
		 this.mandarMensagemMultiplayer(kanjisString);
		 
	 }
 }
 
 /*pode ser que em alguma rodada, nao seja possivel criar 8 kanjis p mostrar na tela. O resto dessas 8 cartas erao cartas vazias*/
 private void tornarORestoDasCartasNaTelaVazias()
 {
	 int quantasCartasNaTela = this.kanjisDasCartasNaTela.size();
	 
	 if(quantasCartasNaTela == 1)
	 {
		 TextView textView2 = (TextView) findViewById(R.id.texto_karuta2);
		 textView2.setText("");
		 ImageView carta2 = (ImageView) findViewById(R.id.karuta2_imageview);
		 carta2.setClickable(false);
		 
		 TextView textView3 = (TextView) findViewById(R.id.texto_karuta3);
		 textView3.setText("");
		 ImageView carta3 = (ImageView) findViewById(R.id.karuta3_imageview);
		 carta3.setClickable(false);
		 
		 TextView textView4 = (TextView) findViewById(R.id.texto_karuta4);
		 textView4.setText("");
		 ImageView carta4 = (ImageView) findViewById(R.id.karuta4_imageview);
		 carta4.setClickable(false);
		 
		 TextView textView5 = (TextView) findViewById(R.id.texto_karuta5);
		 textView5.setText("");
		 ImageView carta5 = (ImageView) findViewById(R.id.karuta5_imageview);
		 carta5.setClickable(false);
		 
		 TextView textView6 = (TextView) findViewById(R.id.texto_karuta6);
		 textView6.setText("");
		 ImageView carta6 = (ImageView) findViewById(R.id.karuta6_imageview);
		 carta6.setClickable(false);
		 
		 TextView textView7 = (TextView) findViewById(R.id.texto_karuta7);
		 textView7.setText("");
		 ImageView carta7 = (ImageView) findViewById(R.id.karuta7_imageview);
		 carta7.setClickable(false);
		 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
	 }
	 else if(quantasCartasNaTela == 2)
	 {
		 TextView textView3 = (TextView) findViewById(R.id.texto_karuta3);
		 textView3.setText("");
		 ImageView carta3 = (ImageView) findViewById(R.id.karuta3_imageview);
		 carta3.setClickable(false);
		 
		 TextView textView4 = (TextView) findViewById(R.id.texto_karuta4);
		 textView4.setText("");
		 ImageView carta4 = (ImageView) findViewById(R.id.karuta4_imageview);
		 carta4.setClickable(false);
		 
		 TextView textView5 = (TextView) findViewById(R.id.texto_karuta5);
		 textView5.setText("");
		 ImageView carta5 = (ImageView) findViewById(R.id.karuta5_imageview);
		 carta5.setClickable(false);
		 
		 TextView textView6 = (TextView) findViewById(R.id.texto_karuta6);
		 textView6.setText("");
		 ImageView carta6 = (ImageView) findViewById(R.id.karuta6_imageview);
		 carta6.setClickable(false);
		 
		 TextView textView7 = (TextView) findViewById(R.id.texto_karuta7);
		 textView7.setText("");
		 ImageView carta7 = (ImageView) findViewById(R.id.karuta7_imageview);
		 carta7.setClickable(false);
		 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
	 }
	 else if(quantasCartasNaTela == 3)
	 {
		 
		 TextView textView4 = (TextView) findViewById(R.id.texto_karuta4);
		 textView4.setText("");
		 ImageView carta4 = (ImageView) findViewById(R.id.karuta4_imageview);
		 carta4.setClickable(false);
		 
		 TextView textView5 = (TextView) findViewById(R.id.texto_karuta5);
		 textView5.setText("");
		 ImageView carta5 = (ImageView) findViewById(R.id.karuta5_imageview);
		 carta5.setClickable(false);
		 
		 TextView textView6 = (TextView) findViewById(R.id.texto_karuta6);
		 textView6.setText("");
		 ImageView carta6 = (ImageView) findViewById(R.id.karuta6_imageview);
		 carta6.setClickable(false);
		 
		 TextView textView7 = (TextView) findViewById(R.id.texto_karuta7);
		 textView7.setText("");
		 ImageView carta7 = (ImageView) findViewById(R.id.karuta7_imageview);
		 carta7.setClickable(false);
		 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
	 }
	 else if(quantasCartasNaTela == 4)
	 { 
		 TextView textView5 = (TextView) findViewById(R.id.texto_karuta5);
		 textView5.setText("");
		 ImageView carta5 = (ImageView) findViewById(R.id.karuta5_imageview);
		 carta5.setClickable(false);
		 
		 TextView textView6 = (TextView) findViewById(R.id.texto_karuta6);
		 textView6.setText("");
		 ImageView carta6 = (ImageView) findViewById(R.id.karuta6_imageview);
		 carta6.setClickable(false);
		 
		 TextView textView7 = (TextView) findViewById(R.id.texto_karuta7);
		 textView7.setText("");
		 ImageView carta7 = (ImageView) findViewById(R.id.karuta7_imageview);
		 carta7.setClickable(false);
		 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
	 }
	 else if(quantasCartasNaTela == 5)
	 { 
		 TextView textView6 = (TextView) findViewById(R.id.texto_karuta6);
		 textView6.setText("");
		 ImageView carta6 = (ImageView) findViewById(R.id.karuta6_imageview);
		 carta6.setClickable(false);
		 
		 TextView textView7 = (TextView) findViewById(R.id.texto_karuta7);
		 textView7.setText("");
		 ImageView carta7 = (ImageView) findViewById(R.id.karuta7_imageview);
		 carta7.setClickable(false);
		 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
	 }
	 else if(quantasCartasNaTela == 6)
	 { 
		 TextView textView7 = (TextView) findViewById(R.id.texto_karuta7);
		 textView7.setText("");
		 ImageView carta7 = (ImageView) findViewById(R.id.karuta7_imageview);
		 carta7.setClickable(false);
		 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
	 }
	 else if(quantasCartasNaTela == 7)
	 { 
		 TextView textView8 = (TextView) findViewById(R.id.texto_karuta8);
		 textView8.setText("");
		 ImageView carta8 = (ImageView) findViewById(R.id.karuta8_imageview);
		 carta8.setClickable(false);
	 }
	 
 }
 
 /*esse metodo retorna null caso todos os kanjis de kanjisQuePodemVirarCartas for usado*/
 private KanjiTreinar escolherUmKanjiParaTreinar()
 {
	 if(kanjisQuePodemVirarCartas.size() <= 0)
	 {
			return null;
	 }
	 else
	 {
		 Random geraNumAleatorio = new Random();
		 int posicaoKanjiEscolhido = geraNumAleatorio.nextInt(this.kanjisQuePodemVirarCartas.size());
		 
		 KanjiTreinar kanjiEscolhido = this.kanjisQuePodemVirarCartas.remove(posicaoKanjiEscolhido); 
		 
		 return kanjiEscolhido; 
	 }
 }
 
 /*muda todas as cartas da tela com base no atributo kanjisDasCartasNaTela. Entrada possivel: au|cotidiano,me|corpo,...*/
 private void mudarCartasNaTela(String[] kanjis)
 {
	 for(int i = 0; i < kanjis.length; i++)
	 {
		 String umKanjiECategoria = kanjis[i];
		 String[] kanjiECategoriaArray = umKanjiECategoria.split("\\|");
		 String umKanji = kanjiECategoriaArray[0];
		 String umaCategoria = kanjiECategoriaArray[1];
		 
		 boolean cartaDoKanjiDeveSerX = this.cartaDoKanjiDeveSerX(umaCategoria, umKanji);
		 //se o kanji ja tiver virado dica anteriormente e nao eh a dica atual, entao a carta dele ja deveria ter saido do jogo
		 
		 if(i == 0)
		 {
			 if(cartaDoKanjiDeveSerX == true)
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta1);
				 texto.setText(""); 
				 ImageView imageViewCarta1 = (ImageView) findViewById(R.id.karuta1_imageview);
				 imageViewCarta1.setClickable(false);
				 imageViewCarta1.setImageResource(R.drawable.karutax);
			 }
			 else
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta1);
				 texto.setText(umKanji);
				 ImageView imageViewCarta1 = (ImageView) findViewById(R.id.karuta1_imageview);
				 imageViewCarta1.setClickable(true);
				 imageViewCarta1.setImageResource(R.drawable.karutavazia);
				 
			 }
		 }
		 else if(i == 1)
		 {
			 if(cartaDoKanjiDeveSerX == true)
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta2);
				 texto.setText(""); 
				 ImageView imageViewCarta2 = (ImageView) findViewById(R.id.karuta2_imageview);
				 imageViewCarta2.setClickable(false);
				 imageViewCarta2.setImageResource(R.drawable.karutax);
			 }
			 else
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta2);
				 texto.setText(umKanji);
				 ImageView imageViewCarta2 = (ImageView) findViewById(R.id.karuta2_imageview);
				 imageViewCarta2.setClickable(true);
				 imageViewCarta2.setImageResource(R.drawable.karutavazia);
			 }
		 }
		 else if(i == 2)
		 {
			 if(cartaDoKanjiDeveSerX == true)
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta3);
				 texto.setText(""); 
				 ImageView imageViewCarta3 = (ImageView) findViewById(R.id.karuta3_imageview);
				 imageViewCarta3.setClickable(false);
				 imageViewCarta3.setImageResource(R.drawable.karutax);
			 }
			 else
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta3);
				 texto.setText(umKanji);
				 ImageView imageViewCarta3 = (ImageView) findViewById(R.id.karuta3_imageview);
				 imageViewCarta3.setClickable(true);
				 imageViewCarta3.setImageResource(R.drawable.karutavazia);
			 }
		 }
		 else if(i == 3)
		 {
			 if(cartaDoKanjiDeveSerX == true)
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta4);
				 texto.setText(""); 
				 ImageView imageViewCarta4 = (ImageView) findViewById(R.id.karuta4_imageview);
				 imageViewCarta4.setClickable(false);
				 imageViewCarta4.setImageResource(R.drawable.karutax);
			 }
			 else
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta4);
				 texto.setText(umKanji);
				 ImageView imageViewCarta4 = (ImageView) findViewById(R.id.karuta4_imageview);
				 imageViewCarta4.setClickable(true);
				 imageViewCarta4.setImageResource(R.drawable.karutavazia);
			 }
		 }
		 else if(i == 4)
		 {
			 if(cartaDoKanjiDeveSerX == true)
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta5);
				 texto.setText(""); 
				 ImageView imageViewCarta5 = (ImageView) findViewById(R.id.karuta5_imageview);
				 imageViewCarta5.setClickable(false);
				 imageViewCarta5.setImageResource(R.drawable.karutax);
			 }
			 else
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta5);
				 texto.setText(umKanji);
				 ImageView imageViewCarta5 = (ImageView) findViewById(R.id.karuta5_imageview);
				 imageViewCarta5.setClickable(true);
				 imageViewCarta5.setImageResource(R.drawable.karutavazia);
			 }
		 }
		 else if(i == 5)
		 {
			 if(cartaDoKanjiDeveSerX == true)
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta6);
				 texto.setText(""); 
				 ImageView imageViewCarta6 = (ImageView) findViewById(R.id.karuta6_imageview);
				 imageViewCarta6.setClickable(false);
				 imageViewCarta6.setImageResource(R.drawable.karutax);
			 }
			 else
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta6);
				 texto.setText(umKanji);
				 ImageView imageViewCarta6 = (ImageView) findViewById(R.id.karuta6_imageview);
				 imageViewCarta6.setClickable(true);
				 imageViewCarta6.setImageResource(R.drawable.karutavazia);
			 }
		 }
		 else if(i == 6)
		 {
			 if(cartaDoKanjiDeveSerX == true)
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta7);
				 texto.setText(""); 
				 ImageView imageViewCarta7 = (ImageView) findViewById(R.id.karuta7_imageview);
				 imageViewCarta7.setClickable(false);
				 imageViewCarta7.setImageResource(R.drawable.karutax);
			 }
			 else
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta7);
				 texto.setText(umKanji);
				 ImageView imageViewCarta7 = (ImageView) findViewById(R.id.karuta7_imageview);
				 imageViewCarta7.setClickable(true);
				 imageViewCarta7.setImageResource(R.drawable.karutavazia);
			 }
		 }
		 else if(i == 7)
		 {
			 if(cartaDoKanjiDeveSerX == true)
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta8);
				 texto.setText(""); 
				 ImageView imageViewCarta8 = (ImageView) findViewById(R.id.karuta8_imageview);
				 imageViewCarta8.setClickable(false);
				 imageViewCarta8.setImageResource(R.drawable.karutax);
			 }
			 else
			 {
				 TextView texto = (TextView) findViewById(R.id.texto_karuta8);
				 texto.setText(umKanji);
				 ImageView imageViewCarta8 = (ImageView) findViewById(R.id.karuta8_imageview);
				 imageViewCarta8.setClickable(true);
				 imageViewCarta8.setImageResource(R.drawable.karutavazia);
			 }
		 }
	 }
 }
 
 /*caso o kanji dessa categoria e texto nao seja o kanjidaDica e ele ja tenha saido como dica, entao a carta respetiva desse kanji deveria ser uma carta X*/
 private boolean cartaDoKanjiDeveSerX(String categoriaKanji, String textoKanji)
 {
	 if(this.kanjiDaDica == null)
	 {
		 //no primeiro turno, quando nao ha dica nenhuma, nenhum kanji eh X
		 return false;
	 }
	 
	 if((this.kanjiDaDica.getKanji().compareTo(textoKanji) == 0)
			 && (this.kanjiDaDica.getCategoriaAssociada().compareTo(categoriaKanji) == 0))
	 {
		 return false; //eh o kanji da dica
	 }
	 else
	 {
		 for(int i = 0; i < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); i++)
		 {
			 KanjiTreinar umKanjiJaVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(i);
			 
			 if((umKanjiJaVirouDica.getCategoriaAssociada().compareTo(categoriaKanji) == 0) 
					 && (umKanjiJaVirouDica.getKanji().compareTo(textoKanji) == 0))
			 {
				 //o kanji ja virou dica anteriormente e alguem ja o acertou e por isso deve ser um X
				 return true;
			 }
			 
		 }
		 
		 return false; //passamos por todos os kanjis que ja viraram dicas e nenhum deles era esse kanji como parametro. Entao ele ainda estah no jogo
	 }
 }
 
 /*funcao para gerar a dica que vai aparecer para ambos os usuarios e ainda enviar ao outro jogador essa dica*/
 private void gerarKanjiDaDica()
 {
	 LinkedList<KanjiTreinar> kanjisQueAindaNaoViraramDicas = new LinkedList<KanjiTreinar>();
	 
	 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
	 {
		 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
		 
		 boolean kanjiJaVirouDica = false;
		 for(int j = 0; j < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); j++)
		 {
			 KanjiTreinar umKanjiQueVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(j);
			 
			 if(umKanjiQueVirouDica.getKanji().compareTo(umKanji.getKanji()) == 0)
			 {
				 kanjiJaVirouDica = true;
				 break;
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
	 this.palavrasJogadas.add(kanjiDaDica);
	 
	 String mensagem = "kanjiDaDica=" + this.kanjiDaDica.getKanji() + "|" + this.kanjiDaDica.getCategoriaAssociada();
	 this.mandarMensagemMultiplayer(mensagem);
	 
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
 
 
 /*assim que o usuario acerta uma carta, ele avisa ao adversairo que aquela carta nao pode ser mais escolhida*/
 private void alertarAoAdversarioQueACartaNaoEhMaisClicavel(String qualCarta)
 {
	 String mensagem = "naoClicavel=" + qualCarta; //ex: naoClicavel=karuta1
	 this.mandarMensagemMultiplayer(mensagem);
 }
 
 /*funcao chamada assim que alguem acerta algum kanji na tela.*/
 private void gerarKanjiDaDicaOuIniciarNovaRodadaOuTerminarJogo()
 {
	 if(this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() == this.kanjisDasCartasNaTela.size())
	 {
		 //todos os kanjis da tela ja se tornaram dicas. Eh hora de mudar a rodada ou terminar o jogo
		 if(this.quantasRodadasHaverao == this.rodadaAtual || this.kanjisQuePodemVirarCartas.size() == 0)
		 {
			 //o jogo deve terminar. Ou a quantidade de rodadas foi alcancada ou nao existem mais cartas a serem geradas
			 this.terminarJogoEEnviarMesagemAoAdversario();
		 }
		 else
		 {
			 //deve-se passar para a proxima rodada
			 this.realizarProcedimentoPassarParaProximaRodada();
			 
		 }
	 }
	 else
	 {
		 this.gerarKanjiDaDica();
	 }
 }
 
 private void realizarProcedimentoPassarParaProximaRodada()
 {
	 this.rodadaAtual = this.rodadaAtual + 1;
	 TextView textViewRodada = (TextView) findViewById(R.id.rodada);
	 String rodada = getResources().getString(R.string.rodada);
	 textViewRodada.setText(rodada + String.valueOf(this.rodadaAtual));
	 
	 this.tornarTodasAdCartasNaTelaClicaveisEVaziasNovamente();
	 
	 String mensagemRodadaMudou = "rodadaMudou";
	 this.mandarMensagemMultiplayer(mensagemRodadaMudou);
	 
	 if(this.mMyId.compareTo(this.quemEscolheACategoria) == 0)
	 {
		 //jogador eh quem escolheu as categorias, entao eh ele quem sorteia os 8 kanjis da rodada
		 this.escolher8KanjisParaARodada();
	 }
	 else
	 {
		 //jogador nao eh quem escolheu as categorias, entao ele deve avisar ao adversario que eh necessario criar mais 8 cartas p a jogada
		 String mensagemGerarMais8Cartas = "gerarMais8Cartas";
		 this.mandarMensagemMultiplayer(mensagemGerarMais8Cartas);
	 }
 }
 
 /*as cartas voltam a ser clicaveis e o X no meio da carta desaparece*/
 private void tornarTodasAdCartasNaTelaClicaveisEVaziasNovamente()
 {
	 ImageView imageViewKaruta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 imageViewKaruta1.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
	 findViewById(R.id.karuta1).setClickable(true);
	 
	 ImageView imageViewKaruta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 imageViewKaruta2.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
	 findViewById(R.id.karuta2).setClickable(true);
	 
	 ImageView imageViewKaruta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 imageViewKaruta3.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
	 findViewById(R.id.karuta3).setClickable(true);
	 
	 ImageView imageViewKaruta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 imageViewKaruta4.setImageResource(R.drawable.karutavazia); //mudei a figura da carta
	 findViewById(R.id.karuta4).setClickable(true);
	 
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
 }
 
 private void pegarTodosOsKanjisQuePodemVirarCartas()
 {
	 this.kanjisQuePodemVirarCartas = new LinkedList<KanjiTreinar>();
	 HashMap<String,LinkedList<KanjiTreinar>> categoriasEscolhidasEKanjisDelas = SingletonGuardaDadosDaPartida.getInstance().getCategoriasEscolhidasEKanjisDelas();
	 
	 Iterator<String> iteradorCategoriasEKanjis = categoriasEscolhidasEKanjisDelas.keySet().iterator();
	 while(iteradorCategoriasEKanjis.hasNext() == true)
	 {
		 String umaCategoria = iteradorCategoriasEKanjis.next();
		 LinkedList<KanjiTreinar> kanjisDaCategoria = categoriasEscolhidasEKanjisDelas.get(umaCategoria);
		 
		 for(int i = 0; i < kanjisDaCategoria.size(); i++)
		 {
			 this.kanjisQuePodemVirarCartas.add(kanjisDaCategoria.get(i));
		 }
	 }
 }

 /*se o usuario errou, ele espera 5 segundos sem clicar em nada*/
 private void realizarProcedimentoUsuarioErrouCarta()
 {
	 String mensagemClicouCartaErrada = getResources().getString(R.string.mensagem_clicou_carta_errada);
	 Toast t = Toast.makeText(this, mensagemClicouCartaErrada, Toast.LENGTH_LONG);
	 t.show();
	 
	 ImageView karuta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 ImageView karuta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 ImageView karuta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 ImageView karuta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 ImageView karuta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 ImageView karuta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 ImageView karuta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 ImageView karuta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 
	 karuta1.setClickable(false);
	 karuta2.setClickable(false);
	 karuta3.setClickable(false);
	 karuta4.setClickable(false);
	 karuta5.setClickable(false);
	 karuta6.setClickable(false);
	 karuta7.setClickable(false);
	 karuta8.setClickable(false);
	 
	 karuta1.setAlpha(128);
	 karuta2.setAlpha(128);
	 karuta3.setAlpha(128);
	 karuta4.setAlpha(128);
	 karuta5.setAlpha(128);
	 karuta6.setAlpha(128);
	 karuta7.setAlpha(128);
	 karuta8.setAlpha(128);
	 
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        TelaInicialMultiplayer.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		                	terminouEsperaUsuarioErrouCarta();
		            }
		        });
		    }
		}, 5000);
 }
 
 private void terminouEsperaUsuarioErrouCarta()
 { 
	 ImageView karuta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 ImageView karuta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 ImageView karuta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 ImageView karuta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 ImageView karuta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 ImageView karuta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 ImageView karuta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 ImageView karuta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 
	 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
	 {
		 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
		 
		 if(kanjiNaoSeTornouDicaOuEhADica(umKanji) == false)
		 {
			 if(i == 0)
			 {
				 karuta1.setClickable(false);
			 }
			 else if(i == 1)
			 {
				 karuta2.setClickable(false);
			 }
			 else if(i == 2)
			 {
				 karuta3.setClickable(false);
			 }
			 else if(i == 3)
			 {
				 karuta4.setClickable(false);
			 }
			 else if(i == 4)
			 {
				 karuta5.setClickable(false);
			 }
			 else if(i == 5)
			 {
				 karuta6.setClickable(false);
			 }
			 else if(i == 6)
			 {
				 karuta7.setClickable(false);
			 }
			 else if(i == 7)
			 {
				 karuta8.setClickable(false);
			 }
		 }
		 else
		 {
			 if(i == 0)
			 {
				 karuta1.setClickable(true);
			 }
			 else if(i == 1)
			 {
				 karuta2.setClickable(true);
			 }
			 else if(i == 2)
			 {
				 karuta3.setClickable(true);
			 }
			 else if(i == 3)
			 {
				 karuta4.setClickable(true);
			 }
			 else if(i == 4)
			 {
				 karuta5.setClickable(true);
			 }
			 else if(i == 5)
			 {
				 karuta6.setClickable(true);
			 }
			 else if(i == 6)
			 {
				 karuta7.setClickable(true);
			 }
			 else if(i == 7)
			 {
				 karuta8.setClickable(true);
			 }
		 }
	 }
	 
	 karuta1.setAlpha(255);
	 karuta2.setAlpha(255);
	 karuta3.setAlpha(255);
	 karuta4.setAlpha(255);
	 karuta5.setAlpha(255);
	 karuta6.setAlpha(255);
	 karuta7.setAlpha(255);
	 karuta8.setAlpha(255);
	 
 }
 
 /*retorna true se o kanji nao se tornou dica ou se ele eh a propria dica*/
 private boolean kanjiNaoSeTornouDicaOuEhADica(KanjiTreinar kanji)
 {
	 String categoriaKanji = kanji.getCategoriaAssociada();
	 String nomeKanji = kanji.getKanji();
	 
	 if((this.kanjiDaDica.getCategoriaAssociada().compareTo(categoriaKanji) == 0) &&
		(this.kanjiDaDica.getKanji().compareTo(nomeKanji) == 0))
	 {
		 //eh o kanji da dica
		 return true;
	 }
	 else
	 {
		 boolean jaVirouDica = false;
		 
		 for(int i = 0; i < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); i++)
		 {
			 KanjiTreinar kanjiJaVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(i);
			 
			 if((kanjiJaVirouDica.getCategoriaAssociada().compareTo(categoriaKanji) == 0) &&
				(kanjiJaVirouDica.getKanji().compareTo(nomeKanji) == 0))
			{
				jaVirouDica = true;
				break;
			}
		 }
		 
		 if(jaVirouDica == false)
		 {
			 return true;
		 }
		 else
		 {
			 return false;
		 }
	 }
 }
 
 private void verificarSeJogadorRecebeUmItemAleatorio()
 {
	 if(this.quantasCartasJaSairamDoJogo % 3 == 0)
	 {
		 //de 3 em 3 cartas que saem, um item aleatorio eh gerado
		 if(this.itemAtual == null || this.itemAtual.compareTo("") == 0)
		 {
			 //o jogador estah sem itens. Vamos dar um p ele
			 if(this.pontuacaoDoAdversario > this.suaPontuacao)
			 {
				 //voce estah perdendo.
				 Random geraNumeroAleatorio = new Random();
				 int indiceSeuItem = geraNumeroAleatorio.nextInt(this.itensDoPerdedor.size());
				 this.itemAtual = this.itensDoPerdedor.get(indiceSeuItem);
			 }
			 else
			 {
				 //voce estah ganhando ou esta empatado
				 Random geraNumeroAleatorio = new Random();
				 int indiceSeuItem = geraNumeroAleatorio.nextInt(this.itensDoGanhador.size());
				 this.itemAtual = this.itensDoGanhador.get(indiceSeuItem);
			 }
			 
			 this.mudarImagemItemDeAcordoComOItemAtual();
		 }
	 }
 }
 
 private void mudarImagemItemDeAcordoComOItemAtual()
 { 
	 ImageView imagemItem = (ImageView) findViewById(R.id.item);
	 
	 if(this.itemAtual.compareTo("trovaotiracartaaleatoria") == 0)
	 {
		 imagemItem.setImageResource(R.drawable.trovaotiracartaaleatoria);
	 }
	 else if(this.itemAtual.compareTo("parartempo") == 0)
	 {
		 imagemItem.setImageResource(R.drawable.parartempo);
	 }
	 else if(this.itemAtual.compareTo("misturarcartas") == 0)
	 {
		 imagemItem.setImageResource(R.drawable.misturarcartas);
	 }
	 else if(this.itemAtual.compareTo("mudardica") == 0)
	 {
		 imagemItem.setImageResource(R.drawable.mudardica);
	 }
	 else if(this.itemAtual.compareTo("doisx") == 0)
	 {
		 imagemItem.setImageResource(R.drawable.doisx);
	 }
	 else if(this.itemAtual.compareTo("segundamao") == 0)
	 {
		 imagemItem.setImageResource(R.drawable.segundamao);
	 }
	 else if(this.itemAtual.compareTo("areadica") == 0)
	 {
		 imagemItem.setImageResource(R.drawable.areadica);
	 }
	 else if(this.itemAtual.compareTo("trovaotiracarta") == 0)
	 {
		 imagemItem.setImageResource(R.drawable.trovaotiracarta);
	 }
	 else if(this.itemAtual.compareTo("revivecarta") == 0)
	 {
		 imagemItem.setImageResource(R.drawable.revivecarta);
	 }
	 
	 imagemItem.setClickable(true); //agora que o usuario tem um item, a figura eh clicavel
 }
 
 private void usarItemAtual()
 {
	 if(this.itemAtual.compareTo("trovaotiracartaaleatoria") == 0)
	 {
		 if(this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() == this.kanjisDasCartasNaTela.size())
		 {
			 //nao existe nenhum kanji que o trovao possa atacar porque soh existe aquele kanji que eh a dica
			 String mensagemTrovaoAcertaNada = getResources().getString(R.string.mensagem_trovao_carta_aleatoria_nao_acerta_nada);
			 Toast t = Toast.makeText(this, mensagemTrovaoAcertaNada , Toast.LENGTH_LONG);
			 t.show();
		 }
		 else
		 {
			 Random geraNumeroAleatorio = new Random();
			 boolean achouUmKanjiParaTirar = false;
			 int posicaoKanjiTirarEmKanjisDasCartasNaTela = -1;
			 
			 while(achouUmKanjiParaTirar == false && posicaoKanjiTirarEmKanjisDasCartasNaTela == -1)
			 {
				 int posicaoCartaAleatoria = geraNumeroAleatorio.nextInt(this.kanjisDasCartasNaTela.size());
				 KanjiTreinar kanjiDessaPosicao = this.kanjisDasCartasNaTela.get(posicaoCartaAleatoria);
				 
				 //peguei um kanji qualquer dos da tela. Mas sera que ele ja tinha virado dica antes?Se sim, nao posso escolhe-lo porque a carta dele ja ter sido tirada do jogo
				 boolean kanjiJaVirouDica = false;
				 
				 for(int i = 0; i < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); i++)
				 {
					 KanjiTreinar umKanjiQueJaVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(i);
					 
					 if((kanjiDessaPosicao.getKanji().compareTo(umKanjiQueJaVirouDica.getKanji()) == 0) &&
					    (kanjiDessaPosicao.getCategoriaAssociada().compareTo(umKanjiQueJaVirouDica.getCategoriaAssociada()) == 0))
					 {
						 kanjiJaVirouDica = true;
						 break;
					 }
				 }
				 
				 if(kanjiJaVirouDica == false)
				 {
					 //achamos um kanji p remover
					 achouUmKanjiParaTirar = true;
					 posicaoKanjiTirarEmKanjisDasCartasNaTela = posicaoCartaAleatoria;
				 }
			 }
			 
			 this.lancarTrovaoNaTelaNaCartaDeIndice(posicaoKanjiTirarEmKanjisDasCartasNaTela);
			 
			 //falta avisar ao adversario p lancar um trovao na mesma carta
			 String mensagemLancarTrovao = "item trovaotiracartaaleatoria indiceCartaRemovida=" + posicaoKanjiTirarEmKanjisDasCartasNaTela;
			 this.mandarMensagemMultiplayer(mensagemLancarTrovao);
		 }
		 
	 }
	 else if(this.itemAtual.compareTo("parartempo") == 0)
	 {
		 this.realizarProcedimentoPararTempo();
		 
		 String mensagemUsouItemPararTempo = "item parartempo";
		 this.mandarMensagemMultiplayer(mensagemUsouItemPararTempo);
	 }
	 else if(this.itemAtual.compareTo("misturarcartas") == 0)
	 {
		 this.misturarCartasEAvisarAoAdversario();
	 }
	 else if(this.itemAtual.compareTo("mudardica") == 0)
	 {
		 this.usarItemMudarDica();
	 }
	 else if(this.itemAtual.compareTo("doisx") == 0)
	 {
		 
	 }
	 else if(this.itemAtual.compareTo("segundamao") == 0)
	 {
		 
	 }
	 else if(this.itemAtual.compareTo("areadica") == 0)
	 {
		 
	 }
	 else if(this.itemAtual.compareTo("trovaotiracarta") == 0)
	 {
		ImageView item = (ImageView) findViewById(R.id.item);
		item.setImageResource(R.drawable.escolhaumacartatrovao);
		this.usouTrovaoTiraCarta = true;
	 }
	 else if(this.itemAtual.compareTo("revivecarta") == 0)
	 {
		 ImageView item = (ImageView) findViewById(R.id.item);
		 item.setImageResource(R.drawable.escolhaumacartarevive);
		 this.usouReviveCarta = true;
		 
		 //falta tornar todas as cartas clicaveis novamente p o usuario escolher uma carta p reviver
		 findViewById(R.id.karuta1_imageview).setClickable(true);
		 findViewById(R.id.karuta2_imageview).setClickable(true);
		 findViewById(R.id.karuta3_imageview).setClickable(true);
		 findViewById(R.id.karuta4_imageview).setClickable(true);
		 findViewById(R.id.karuta5_imageview).setClickable(true);
		 findViewById(R.id.karuta6_imageview).setClickable(true);
		 findViewById(R.id.karuta7_imageview).setClickable(true);
		 findViewById(R.id.karuta8_imageview).setClickable(true);
	 }
	 
	 if(this.itemAtual.compareTo("trovaotiracarta") != 0 && this.itemAtual.compareTo("revivecarta") != 0)
	 {
		 ImageView imagemItem = (ImageView) findViewById(R.id.item);
		 imagemItem.setImageResource(R.drawable.nenhumitem);
		 
		 this.itemAtual = "";
		 imagemItem.setClickable(false); 
	 }
	 else if(this.itemAtual.compareTo("trovaotiracarta") == 0 || this.itemAtual.compareTo("revivecarta") == 0)
	 {
		 ImageView imagemItem = (ImageView) findViewById(R.id.item);
		 imagemItem.setClickable(false);
		 this.itemAtual = "";
	 }
 }
 
 /*lanca um trovao que torna uma das cartas na tela inclicavel. O indice vai de 0 a 7*/
 private void lancarTrovaoNaTelaNaCartaDeIndice(int indiceCartaTrovoada)
 {
	 TextView textoCartaTirada = null;
	 
	 if(indiceCartaTrovoada == 0)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta1_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta1);
	 }
	 else if(indiceCartaTrovoada == 1)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta2_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta2);
	 }
	 else if(indiceCartaTrovoada == 2)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta3_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta3);
	 }
	 else if(indiceCartaTrovoada == 3)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta4_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta4);
	 }
	 else if(indiceCartaTrovoada == 4)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta5_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta5);
	 }
	 else if(indiceCartaTrovoada == 5)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta6_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta6);
	 }
	 else if(indiceCartaTrovoada == 6)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta7_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta7);
	 }
	 else if(indiceCartaTrovoada == 7)
	 {
		 cartaASerTirada = (ImageView) findViewById(R.id.karuta8_imageview);
		 textoCartaTirada = (TextView) findViewById(R.id.texto_karuta8);
	 }
	 
	 
	 final AnimationDrawable animacaoTrovaoAcertaCarta = new AnimationDrawable();
	 int idImagemKarutaTrovao1 = getResources().getIdentifier("karutatrovao1", "drawable", getPackageName());
	 int idImagemKarutaTrovao2 = getResources().getIdentifier("karutatrovao2", "drawable", getPackageName());
	 int idImagemKarutaTrovao3 = getResources().getIdentifier("karutatrovao3", "drawable", getPackageName());
	 int idImagemKarutaTrovao4 = getResources().getIdentifier("karutatrovao4", "drawable", getPackageName());
	 int idImagemKarutaX = getResources().getIdentifier("karutax", "drawable", getPackageName());
	 
	 animacaoTrovaoAcertaCarta.addFrame(getResources().getDrawable(idImagemKarutaTrovao1), 200);
	 animacaoTrovaoAcertaCarta.addFrame(getResources().getDrawable(idImagemKarutaTrovao2), 200);
	 animacaoTrovaoAcertaCarta.addFrame(getResources().getDrawable(idImagemKarutaTrovao3), 200);
	 animacaoTrovaoAcertaCarta.addFrame(getResources().getDrawable(idImagemKarutaTrovao4), 200);
	 animacaoTrovaoAcertaCarta.addFrame(getResources().getDrawable(idImagemKarutaX), 200);
	 
	 animacaoTrovaoAcertaCarta.setOneShot(true);
	 cartaASerTirada.setImageDrawable(animacaoTrovaoAcertaCarta);
	 cartaASerTirada.post(new Runnable() {
		@Override
		public void run() {
			animacaoTrovaoAcertaCarta.start();
		}
	}); 
	 
	 super.reproduzirSfx("trovao");
	 
	cartaASerTirada.setClickable(false); //a carta nao esta mais clicavel ate o final da rodada
	textoCartaTirada.setText("");
	
	KanjiTreinar kanjiRemovido = this.kanjisDasCartasNaTela.get(indiceCartaTrovoada);
	this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.add(kanjiRemovido);
	
	this.quantasCartasJaSairamDoJogo = this.quantasCartasJaSairamDoJogo + 1;
	
	this.usouTrovaoTiraCarta = false;
	this.verificarSeJogadorRecebeUmItemAleatorio();
 }
 
 /*no caso de trovao carta nao aleatoria*/
 private void lancarTrovaoNaTelaNaCartaDeIndiceEAvisarAoAdversario(int indiceCarta)
 {
	 this.lancarTrovaoNaTelaNaCartaDeIndice(indiceCarta);
	 
	 //falta avisar ao adversario p lancar um trovao na mesma carta
	 String mensagemLancarTrovao = "item trovaotiracarta indiceCartaRemovida=" + indiceCarta;
	 this.mandarMensagemMultiplayer(mensagemLancarTrovao);
 }
 
 private void realizarProcedimentoPararTempo()
 {
	 TextView textViewTempo = (TextView) findViewById(R.id.tempo);
	 textViewTempo.setTextColor(Color.RED);
	 findViewById(R.id.parartempopequeno).setVisibility(View.VISIBLE);
	 
	 this.tempoEstahParado = true;
	 
	 //as cartas nao vao ficar clicaveis por um tempo assim como quando um jogador erra uma carta
	 ImageView karuta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 ImageView karuta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 ImageView karuta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 ImageView karuta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 ImageView karuta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 ImageView karuta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 ImageView karuta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 ImageView karuta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 
	 karuta1.setClickable(false);
	 karuta2.setClickable(false);
	 karuta3.setClickable(false);
	 karuta4.setClickable(false);
	 karuta5.setClickable(false);
	 karuta6.setClickable(false);
	 karuta7.setClickable(false);
	 karuta8.setClickable(false);
	 
	 karuta1.setAlpha(128);
	 karuta2.setAlpha(128);
	 karuta3.setAlpha(128);
	 karuta4.setAlpha(128);
	 karuta5.setAlpha(128);
	 karuta6.setAlpha(128);
	 karuta7.setAlpha(128);
	 karuta8.setAlpha(128);
	 
	 super.reproduzirSfx("parar_tempo");
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        TelaInicialMultiplayer.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	tempoEstahParado = false;
		            	TextView textViewTempo = (TextView) findViewById(R.id.tempo);
		           	 	textViewTempo.setTextColor(Color.BLACK);
		           	 	findViewById(R.id.parartempopequeno).setVisibility(View.INVISIBLE);
		                terminouEsperaUsuarioErrouCarta();
		            }
		        });
		    }
		}, 5000);
 }
 
 
 private void misturarCartasEAvisarAoAdversario()
 {
	 Collections.shuffle(this.kanjisDasCartasNaTela);
	 
	 final String[] kanjisEBarrasComCategoria = new String[this.kanjisDasCartasNaTela.size()]; //so preciso mudar as cartas na tela, mas a funcao recebe somente essa estrutura: au|cotidiano,me|corpo... 
	 
	 String mensagemParaOAdversario = "item misturarcartas kanjis="; //mensagem p alertar ao adversario a nova ordem dos kanjis
	 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
	 {
		 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
		 kanjisEBarrasComCategoria[i] = umKanji.getKanji() + "|" + umKanji.getCategoriaAssociada();
		 mensagemParaOAdversario = mensagemParaOAdversario + umKanji.getKanji() + "|" + umKanji.getCategoriaAssociada();
		 
	     mensagemParaOAdversario = mensagemParaOAdversario + ";"; //tanta faz se no item final tiver um ;, isso foi testado
	 }
	 
	 this.mandarMensagemMultiplayer(mensagemParaOAdversario);
	 
	 TextView textoKaruta1 = (TextView) findViewById(R.id.texto_karuta1);
	 textoKaruta1.setText("");
	 TextView textoKaruta2 = (TextView) findViewById(R.id.texto_karuta2);
	 textoKaruta2.setText("");
	 TextView textoKaruta3 = (TextView) findViewById(R.id.texto_karuta3);
	 textoKaruta3.setText("");
	 TextView textoKaruta4 = (TextView) findViewById(R.id.texto_karuta4);
	 textoKaruta4.setText("");
	 TextView textoKaruta5 = (TextView) findViewById(R.id.texto_karuta5);
	 textoKaruta5.setText("");
	 TextView textoKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
	 textoKaruta6.setText("");
	 TextView textoKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
	 textoKaruta7.setText("");
	 TextView textoKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
	 textoKaruta8.setText("");
	 
	 ImageView imageViewCarta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 ImageView imageViewCarta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 ImageView imageViewCarta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 ImageView imageViewCarta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 ImageView imageViewCarta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 ImageView imageViewCarta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 ImageView imageViewCarta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 ImageView imageViewCarta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 
	 
	 imageViewCarta1.setClickable(false);
	 imageViewCarta2.setClickable(false);
	 imageViewCarta3.setClickable(false);
	 imageViewCarta4.setClickable(false);
	 imageViewCarta5.setClickable(false);
	 imageViewCarta6.setClickable(false);
	 imageViewCarta7.setClickable(false);
	 imageViewCarta8.setClickable(false);
	 
	 final AnimationDrawable animacaoPoofCarta = new AnimationDrawable();
	 int idImagemKarutaPoof1 = getResources().getIdentifier("karutapoof1", "drawable", getPackageName());
	 int idImagemKarutaPoof2 = getResources().getIdentifier("karutapoof2", "drawable", getPackageName());
	 int idImagemKarutaPoof3 = getResources().getIdentifier("karutapoof3", "drawable", getPackageName());
	 int idImagemKarutaPoof4 = getResources().getIdentifier("karutapoof4", "drawable", getPackageName());
	 int idImagemKarutaVazia = getResources().getIdentifier("karutavazia", "drawable", getPackageName());
	 
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof1), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof2), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof3), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof4), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaVazia), 200);
	 
	 animacaoPoofCarta.setOneShot(true);
	 imageViewCarta1.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta2.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta3.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta4.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta5.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta6.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta7.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta8.setImageDrawable(animacaoPoofCarta);
	 
	 imageViewCarta1.post(new Runnable() {
		@Override
		public void run() {
			animacaoPoofCarta.start();
		}
	 	});
	 imageViewCarta2.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 imageViewCarta3.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 imageViewCarta4.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 imageViewCarta5.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 imageViewCarta6.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 imageViewCarta7.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 imageViewCarta8.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 
	 
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        TelaInicialMultiplayer.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	mudarCartasNaTela(kanjisEBarrasComCategoria); //so irei realizar essa funcao apos a animacao ter sido realizada em cada uma das cartas
		            	
		            	if(kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() < 8)
		        		{
		        			//menos de 8 cartas, por isso algumas devem ficar vazias
		        			tornarORestoDasCartasNaTelaVazias();
		        		}
		            	
		            }
		        });
		    }
		}, 1200);
	 
 }
 
 /*funcao do usuario que recebeu a mensagem p mudar as cartas na tela.
  * As linkedlists tem o mesmo tamanho*/
 private void misturarCartasRecebeuCartasOutroUsuario(LinkedList<String> textoKanjisNovos, LinkedList<String> categoriasKanjisNovos)
 {
	 //primeiro iremos pegar os kanjis com base nas categorias e texto
	 LinkedList<KanjiTreinar> novosKanjis = new LinkedList<KanjiTreinar>();
	 
	 for(int i = 0; i < textoKanjisNovos.size(); i++)
	 {
		 String umTextoKanji = textoKanjisNovos.get(i);
		 String umaCategoria = categoriasKanjisNovos.get(i);
		 
		 for(int j = 0; j < this.kanjisDasCartasNaTela.size(); j++)
		 {
			 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(j);
			 
			 if(umKanji.getKanji().compareTo(umTextoKanji) == 0 && umKanji.getCategoriaAssociada().compareTo(umaCategoria) == 0)
			 {
				 novosKanjis.add(umKanji);
				 break;
			 }
		 }
	 }
	 
	 //agora podemos tornar a lista de kanjis das cartas na tela essa nova lista
	 this.kanjisDasCartasNaTela.clear();
	 
	 for(int k = 0; k < novosKanjis.size(); k++)
	 {
		 KanjiTreinar umKanji = novosKanjis.get(k);
		 this.kanjisDasCartasNaTela.add(umKanji);
	 }
	 
	 
	 //agora podemos comecar a mostrar a mistura das cartas na tela
	 final String[] kanjisEBarrasComCategoria = new String[this.kanjisDasCartasNaTela.size()]; //so preciso mudar as cartas na tela, mas a funcao recebe somente essa estrutura: au|cotidiano,me|corpo... 
	 
	 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
	 {
		 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
		 kanjisEBarrasComCategoria[i] = umKanji.getKanji() + "|" + umKanji.getCategoriaAssociada();
		 
	 }
	 
	 
	 TextView textoKaruta1 = (TextView) findViewById(R.id.texto_karuta1);
	 textoKaruta1.setText("");
	 TextView textoKaruta2 = (TextView) findViewById(R.id.texto_karuta2);
	 textoKaruta2.setText("");
	 TextView textoKaruta3 = (TextView) findViewById(R.id.texto_karuta3);
	 textoKaruta3.setText("");
	 TextView textoKaruta4 = (TextView) findViewById(R.id.texto_karuta4);
	 textoKaruta4.setText("");
	 TextView textoKaruta5 = (TextView) findViewById(R.id.texto_karuta5);
	 textoKaruta5.setText("");
	 TextView textoKaruta6 = (TextView) findViewById(R.id.texto_karuta6);
	 textoKaruta6.setText("");
	 TextView textoKaruta7 = (TextView) findViewById(R.id.texto_karuta7);
	 textoKaruta7.setText("");
	 TextView textoKaruta8 = (TextView) findViewById(R.id.texto_karuta8);
	 textoKaruta8.setText("");
	 
	 ImageView imageViewCarta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 ImageView imageViewCarta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 ImageView imageViewCarta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 ImageView imageViewCarta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 ImageView imageViewCarta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 ImageView imageViewCarta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 ImageView imageViewCarta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 ImageView imageViewCarta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 
	 
	 imageViewCarta1.setClickable(false);
	 imageViewCarta2.setClickable(false);
	 imageViewCarta3.setClickable(false);
	 imageViewCarta4.setClickable(false);
	 imageViewCarta5.setClickable(false);
	 imageViewCarta6.setClickable(false);
	 imageViewCarta7.setClickable(false);
	 imageViewCarta8.setClickable(false);
	 
	 final AnimationDrawable animacaoPoofCarta = new AnimationDrawable();
	 int idImagemKarutaPoof1 = getResources().getIdentifier("karutapoof1", "drawable", getPackageName());
	 int idImagemKarutaPoof2 = getResources().getIdentifier("karutapoof2", "drawable", getPackageName());
	 int idImagemKarutaPoof3 = getResources().getIdentifier("karutapoof3", "drawable", getPackageName());
	 int idImagemKarutaPoof4 = getResources().getIdentifier("karutapoof4", "drawable", getPackageName());
	 int idImagemKarutaVazia = getResources().getIdentifier("karutavazia", "drawable", getPackageName());
	 
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof1), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof2), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof3), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaPoof4), 200);
	 animacaoPoofCarta.addFrame(getResources().getDrawable(idImagemKarutaVazia), 200);
	 
	 animacaoPoofCarta.setOneShot(true);
	 imageViewCarta1.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta2.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta3.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta4.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta5.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta6.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta7.setImageDrawable(animacaoPoofCarta);
	 imageViewCarta8.setImageDrawable(animacaoPoofCarta);
	 
	 imageViewCarta1.post(new Runnable() {
		@Override
		public void run() {
			animacaoPoofCarta.start();
		}
	 	});
	 imageViewCarta2.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 imageViewCarta3.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 imageViewCarta4.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 imageViewCarta5.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 imageViewCarta6.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		});
	 imageViewCarta7.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 imageViewCarta8.post(new Runnable() {
			@Override
			public void run() {
				animacaoPoofCarta.start();
			}
		}); 
	 
	 
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        TelaInicialMultiplayer.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	mudarCartasNaTela(kanjisEBarrasComCategoria); //so irei realizar essa funcao apos a animacao ter sido realizada em cada uma das cartas
		            	if(kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() < 8)
		        		{
		        			//menos de 8 cartas, por isso algumas devem ficar vazias
		        			tornarORestoDasCartasNaTelaVazias();
		        		}
		            	
		            }
		        });
		    }
		}, 1200);
 }
 
 /*caso uma nova dica possa ser criada, a dica atual mudarah. */
 private void usarItemMudarDica()
 {
	 if(kanjisDasCartasNaTela.size() == kanjisDasCartasNaTelaQueJaSeTornaramDicas.size())
	 {
		 //sinto muito, mas nao ha dicas diferentes que possam ser geradas. A ultima dica ja esta presente
		 String mensagemErroMudarDica = getResources().getString(R.string.erro_mudar_dica);
		 Toast.makeText(this, mensagemErroMudarDica, Toast.LENGTH_SHORT).show();
	 }
	 else
	 { 
		 //acharemos uma nova dica que seja diferente da anterior
		 //primeiro, precisamos remover o kanji da dica da linkedlist chamada kanjisDasCartasNaTelaQueJaSeTornaramDicas
		 
		 this.tirarKanjiDicaAtualDeCartasQueJaViraramDicasEPalavrasJogadas();
		 
		 //agora vamos gerar um novo kanji da dica diferente da anterior
		 
		 LinkedList<KanjiTreinar> kanjisQueAindaNaoViraramDicas = new LinkedList<KanjiTreinar>();
		 
		 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
		 {
			 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
			 
			 boolean kanjiJaVirouDica = false;
			 for(int j = 0; j < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); j++)
			 {
				 KanjiTreinar umKanjiQueVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(j);
				 
				 if(umKanjiQueVirouDica.getKanji().compareTo(umKanji.getKanji()) == 0)
				 {
					 kanjiJaVirouDica = true;
					 break;
				 }
			 }
			 
			 if(kanjiJaVirouDica == false 
					 && (this.ehMesmoKanji(umKanji, kanjiDaDica) == false))
			 {
				 //agora tb n queremos que esse kanji seja a antiga dica!!!!
				 kanjisQueAindaNaoViraramDicas.add(umKanji);
			 }
		 }
		 
		 Random geraNumAleatorio = new Random();
		 int tamanhoKanjisQueNaoViraramDicas = kanjisQueAindaNaoViraramDicas.size();
		 
		 int indiceKanjiDaDica = geraNumAleatorio.nextInt(tamanhoKanjisQueNaoViraramDicas);
		 
		 KanjiTreinar umKanji = kanjisQueAindaNaoViraramDicas.get(indiceKanjiDaDica);
		 this.kanjiDaDica = umKanji;
		 this.palavrasJogadas.add(kanjiDaDica);
		 
		 String mensagem = "item mudardica kanjiDaDica=" + this.kanjiDaDica.getKanji() + "|" + this.kanjiDaDica.getCategoriaAssociada();
		 this.mandarMensagemMultiplayer(mensagem);
		 
		 //this.alterarTextoDicaComBaseNoKanjiDaDica(); //vamos realizar um poof antes de mudar esse texto
		 
		 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.add(umKanji);
		 
		 this.realizarProcedimentoMudandoDicaAtual();
			 
	 }
 }
 
 private boolean ehMesmoKanji(KanjiTreinar k1, KanjiTreinar k2)
 {
	 if((k1.getKanji().compareTo(k2.getKanji()) == 0) && (k1.getCategoriaAssociada().compareTo(k2.getCategoriaAssociada()) == 0))
	 {
		 return true;
	 }
	 else
	 {
		 return false;
	 }
 }
 
 private void tirarKanjiDicaAtualDeCartasQueJaViraramDicasEPalavrasJogadas()
 {
	//primeiro, precisamos remover o kanji da dica da linkedlist chamada kanjisDasCartasNaTelaQueJaSeTornaramDicas
	 for(int g = 0; g < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); g++)
	 {
		 KanjiTreinar umKanjiJaVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(g);
		 
		 if((umKanjiJaVirouDica.getKanji().compareTo(kanjiDaDica.getKanji()) == 0) 
			 && (umKanjiJaVirouDica.getCategoriaAssociada().compareTo(kanjiDaDica.getCategoriaAssociada()) == 0))
		 {
			 //achamos o kanji da dica! Falta remove-lo
			 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.remove(g);
			 break;	 
		 } 
	 }
	 
	 //agora precisamos remove-lo das palavras jogadas tb
	 
	 for(int h = 0; h < this.palavrasJogadas.size(); h++)
	 {
		 KanjiTreinar umKanjiJaJogado = this.palavrasJogadas.get(h);
		 
		 if((umKanjiJaJogado.getKanji().compareTo(kanjiDaDica.getKanji()) == 0) 
			 && (umKanjiJaJogado.getCategoriaAssociada().compareTo(kanjiDaDica.getCategoriaAssociada()) == 0))
		 {
			 //achamos o kanji da dica! Falta remove-lo
			 this.palavrasJogadas.remove(h);
			 break;	 
		 } 
	 }
 }
 
 //a dica atual ficarah com "..." por alguns segundos ate mudar
 private void realizarProcedimentoMudandoDicaAtual()
 {
	 TextView textoDicaKanji = (TextView) findViewById(R.id.dica_kanji);
	 String mensagemDicaMudando = getResources().getString(R.string.palavra_esta_mudando);
	 textoDicaKanji.setText(mensagemDicaMudando);
	 
	 super.reproduzirSfx("mudar_dica");
	 
	 //durante essa espera, as cartas n devem ser clicaveis
	 ImageView imageViewCarta1 = (ImageView) findViewById(R.id.karuta1_imageview);
	 ImageView imageViewCarta2 = (ImageView) findViewById(R.id.karuta2_imageview);
	 ImageView imageViewCarta3 = (ImageView) findViewById(R.id.karuta3_imageview);
	 ImageView imageViewCarta4 = (ImageView) findViewById(R.id.karuta4_imageview);
	 ImageView imageViewCarta5 = (ImageView) findViewById(R.id.karuta5_imageview);
	 ImageView imageViewCarta6 = (ImageView) findViewById(R.id.karuta6_imageview);
	 ImageView imageViewCarta7 = (ImageView) findViewById(R.id.karuta7_imageview);
	 ImageView imageViewCarta8 = (ImageView) findViewById(R.id.karuta8_imageview);
	 
	 
	 imageViewCarta1.setClickable(false);
	 imageViewCarta2.setClickable(false);
	 imageViewCarta3.setClickable(false);
	 imageViewCarta4.setClickable(false);
	 imageViewCarta5.setClickable(false);
	 imageViewCarta6.setClickable(false);
	 imageViewCarta7.setClickable(false);
	 imageViewCarta8.setClickable(false);
	 
	 final String[] kanjisEBarrasComCategoria = new String[this.kanjisDasCartasNaTela.size()]; //so preciso disso p tornar as cartas clicaveis novamente e somente aquelas que deveriam ser clicaveis
	 for(int i = 0; i < this.kanjisDasCartasNaTela.size(); i++)
	 {
		 KanjiTreinar umKanji = this.kanjisDasCartasNaTela.get(i);
		 kanjisEBarrasComCategoria[i] = umKanji.getKanji() + "|" + umKanji.getCategoriaAssociada();
		 
	 }
	 
	 
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        TelaInicialMultiplayer.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	mudarCartasNaTela(kanjisEBarrasComCategoria); //so irei realizar essa funcao apos a animacao ter sido realizada
		            	if(kanjisDasCartasNaTelaQueJaSeTornaramDicas.size() < 8)
		        		{
		        			//menos de 8 cartas, por isso algumas devem ficar vazias
		        			tornarORestoDasCartasNaTelaVazias();
		        		}
		            	alterarTextoDicaComBaseNoKanjiDaDica();
		            }
		        });
		    }
		}, 3000);
 }
 
 /*o usuario usou o item para reviver uma carta. O numero da carta vai de 1 ate 8*/
 private void realizarProcedimentoReviverCarta(int numeroCarta, boolean ehUsuarioQueRecebeuMensagem)
 {
	 KanjiTreinar kanjiRevivido = this.kanjisDasCartasNaTela.get(numeroCarta - 1);
	 
	 //temos de tirar ele das cartas que ja viraram dicas
	 //mas o usuario pode dar uma de engracadinho e tentar reviver uma carta que ta viva!
	 //o usuario n pode reviver carta ja viva ou a da dicaatual
	 boolean cartaFoiRemovidaDaLinkedListTornaramDicas = false;
	 String textoCartaRevivida = "";
	 for(int i = 0; i < this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.size(); i++)
	 {
		 KanjiTreinar umKanjiQueJaVirouDica = this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.get(i);
		 
		 if(this.ehMesmoKanji(umKanjiQueJaVirouDica, kanjiRevivido) == true &&
				 this.ehMesmoKanji(this.kanjiDaDica, kanjiRevivido) == false)
		 {
			 //achamos o kanji que era p reviver! Ele n pode ser o kanji da dica
			 this.kanjisDasCartasNaTelaQueJaSeTornaramDicas.remove(i);
			 textoCartaRevivida = umKanjiQueJaVirouDica.getKanji();
			 cartaFoiRemovidaDaLinkedListTornaramDicas = true;
			 break;
		 }
	 }
	 
	 if(cartaFoiRemovidaDaLinkedListTornaramDicas == false)
	 {
		 //dizer ao usuario que essa carta nao pode ser revivida. O usuario q recebeu a mensagem da carta ser revivida n deve receber esse informativo
		 if(ehUsuarioQueRecebeuMensagem == false)
		 {
			 String mensagemErroReviverCarta = getResources().getString(R.string.erro_reviver_carta);
			 TextView textViewFalaMascote = (TextView) findViewById(R.id.dica_kanji);
			 String fraseAtualMascote = String.valueOf(textViewFalaMascote.getText());
			 mascoteFalaFrasePor4Segundos(mensagemErroReviverCarta, fraseAtualMascote);
		 } 
	 }
	 else
	 {
		 //Falta mudar a figura da carta para nao ser mais a imagem de carta removida
		 ImageView imageViewCartaRevivida;
		 TextView textViewCartaRevivida;
		 if(numeroCarta == 1)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta1_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta1);
		 }
		 else if(numeroCarta == 2)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta2_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta2);
		 }
		 else if(numeroCarta == 3)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta3_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta3);
		 }
		 else if(numeroCarta == 4)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta4_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta4);
		 }
		 else if(numeroCarta == 5)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta5_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta5);
		 }
		 else if(numeroCarta == 6)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta6_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta6);
		 }
		 else if(numeroCarta == 7)
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta7_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta7);
		 }
		 else
		 {
			 imageViewCartaRevivida = (ImageView)findViewById(R.id.karuta8_imageview);
			 textViewCartaRevivida = (TextView)findViewById(R.id.texto_karuta8);
		 }
		
		 //falta realizar a animacao de carta sendo revivida
		 this.realizarAnimacaoCartaRevivida(imageViewCartaRevivida, textViewCartaRevivida, textoCartaRevivida);
	 } 
 }
 
 private void realizarAnimacaoCartaRevivida(ImageView imageViewCartaRevivida, TextView textViewCartaRevivida, String textoCartaRevivida)
 {
	 final AnimationDrawable animacaoReviveCarta = new AnimationDrawable();
	 final TextView textViewCartaRevividaFinal = textViewCartaRevivida;
	 final String textoCartaRevividaFinal = textoCartaRevivida; 
	 int idImagemKarutaRevive1 = getResources().getIdentifier("karutarevive1", "drawable", getPackageName());
	 int idImagemKarutaRevive2 = getResources().getIdentifier("karutarevive2", "drawable", getPackageName());
	 int idImagemKarutaRevive3 = getResources().getIdentifier("karutarevive3", "drawable", getPackageName());
	 int idImagemKarutaVazia = getResources().getIdentifier("karutavazia", "drawable", getPackageName());
	 
	 animacaoReviveCarta.addFrame(getResources().getDrawable(idImagemKarutaRevive1), 200);
	 animacaoReviveCarta.addFrame(getResources().getDrawable(idImagemKarutaRevive2), 200);
	 animacaoReviveCarta.addFrame(getResources().getDrawable(idImagemKarutaRevive3), 200);
	 animacaoReviveCarta.addFrame(getResources().getDrawable(idImagemKarutaVazia), 200);
	 
	 animacaoReviveCarta.setOneShot(true);
	 imageViewCartaRevivida.setImageDrawable(animacaoReviveCarta);
	 
	 super.reproduzirSfx("reviver_carta");
	 
	 imageViewCartaRevivida.post(new Runnable() {
		@Override
		public void run() {
			animacaoReviveCarta.start();
		}
	 	});
	 
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        TelaInicialMultiplayer.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		       		  //FALTA TORNAR A CARTA CLICAVEL E AS OUTRAS NAO. posso fazer isso com a funcao abaixo
		       		  terminouEsperaUsuarioErrouCarta();
		       		  textViewCartaRevividaFinal.setText(textoCartaRevividaFinal);
		            }
		        });
		    }
		}, 1000);
 }
 
 /*a mascote fala a frase por 4 segundos e depois volta a fala anterior*/
 private void mascoteFalaFrasePor4Segundos(String fraseFalarPor4Segundos, String fraseAnterior)
 {
	 final TextView textViewFalaMascote = (TextView) findViewById(R.id.dica_kanji);
	 textViewFalaMascote.setText(fraseFalarPor4Segundos);
	 
	 final String fraseAnteriorFinal = fraseAnterior;
	 new Timer().schedule(new TimerTask() 
	 { 
		    @Override
		    public void run() 
		    {
		        //If you want to operate UI modifications, you must run ui stuff on UiThread.
		        TelaInicialMultiplayer.this.runOnUiThread(new Runnable() 
		        {
		            @Override
		            public void run() 
		            {
		            	textViewFalaMascote.setText(fraseAnteriorFinal);
		            }
		        });
		    }
		}, 4000);
 }
 
 @Override
 public boolean jogadorEhHost() {
 	if(mMyId.compareTo(quemEscolheACategoria) == 0)
 	{
 		return true;
 	}
 	else
 	{
 		return false;
 	}
 	
 }

 public boolean oGuestTerminouDeCarregarListaDeCategorias() {
 	return guestTerminouDeCarregarListaDeCategorias;
 }
 
 private void terminarJogoEEnviarMesagemAoAdversario()
 {
	 this.jogoAcabou = true;
	 
	 try
	 {
		 this.mandarMensagemMultiplayer("fim de jogo");
		 switchToScreen(R.id.tela_fim_de_jogo);
		 comecarFimDeJogo();
	 }
	 catch(IllegalStateException E)
	 {
		 voltarAoMenuInicial(null);
	 }
 }
 
 private void comecarFimDeJogo()
 {
	 findViewById(R.id.textoPontuacaoAdversario).setVisibility(View.VISIBLE);
	 findViewById(R.id.textoSuaPontuacao).setVisibility(View.VISIBLE);
	 findViewById(R.id.tituloTelaFimDeJogo).setVisibility(View.VISIBLE);
	 findViewById(R.id.mensagens_chat).setVisibility(View.VISIBLE);
	 findViewById(R.id.sendBtn).setVisibility(View.VISIBLE);
	 findViewById(R.id.botao_menu_principal).setVisibility(View.VISIBLE);
	 findViewById(R.id.botao_revanche).setVisibility(View.VISIBLE);
	 findViewById(R.id.chatET).setVisibility(View.VISIBLE);
	 
	 TextView textoPontuacaoAdversario = (TextView) findViewById(R.id.textoPontuacaoAdversario);
	 String pontuacaoDoAdversario = getResources().getString(R.string.pontuacaoDoAdversario);
	 textoPontuacaoAdversario.setText(pontuacaoDoAdversario + String.valueOf(this.pontuacaoDoAdversario));
	 
	 TextView textoSuaPontuacao = (TextView) findViewById(R.id.textoSuaPontuacao);
	 String stringSuaPontuacao = getResources().getString(R.string.suaPontuacao);
	 textoSuaPontuacao.setText(stringSuaPontuacao + String.valueOf(this.suaPontuacao));
	 
	 this.mensagensChat = new ArrayList<String>();
	 
	 this.enviarDadosDaPartidaParaOLogDoUsuarioNoBancoDeDados();
	 
	 //falta adicionar o dinheiro que o usuario ganhou na partida
	 
	 int creditosAdicionarAoJogador = TransformaPontosEmCredito.converterPontosEmCredito(this.suaPontuacao);
	 DAOAcessaDinheiroDoJogador daoDinheiroJogador = ConcreteDAOAcessaDinheiroDoJogador.getInstance();
	 daoDinheiroJogador.adicionarCredito(creditosAdicionarAoJogador, this);
	 String textoGanhouCreditoNaPartida = getResources().getString(R.string.texto_ganhou) + " " + 
	 											 creditosAdicionarAoJogador + " " + getResources().getString(R.string.moeda_do_jogo) + " " + 
	 											 getResources().getString(R.string.texto_na_partida); 
	 Toast.makeText(this, textoGanhouCreditoNaPartida, Toast.LENGTH_SHORT).show();
	 
	 this.mudarMusicaDeFundo(R.raw.time_to_unwind);
 }
 

 private void setListAdapter() { //arraylist<string>
     ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem, this.mensagensChat);
     ListView listViewChat = (ListView) findViewById(R.id.mensagens_chat);
     listViewChat.setAdapter(adapter);
   }

 /**
  * 
  * @param mensagem
  * @param adicionarNomeDoRemetente precisa complementar a mensagem com o nome do remetente ou nao...
  * @return a mensagem adicionada no chat.
  */
 private String adicionarMensagemNoChat(String mensagem, boolean adicionarNomeDoRemetente)
 {
 	String mensagemAdicionarNoChat = mensagem;
 	if(adicionarNomeDoRemetente == true)
 	{
 		//append na mensagem o nome do remetente
 		//String emailUsuario = this.emailUsuario.substring(0, 11);
 		mensagemAdicionarNoChat = this.emailUsuario + ":" + mensagem;
 	}
 	
 	this.mensagensChat.add(mensagemAdicionarNoChat);
 	setListAdapter();
 	return mensagemAdicionarNoChat;
 }

 private void avisarAoOponenteQueDigitouMensagem(String mensagemAdicionarNoChat)
 {
 	//mandar mensagem para oponente...
 	this.mandarMensagemMultiplayer("oponente falou no chat=" + mensagemAdicionarNoChat);
 }

 private void enviarSeuEmailParaOAdversario()
 {
	 this.mandarMensagemMultiplayer("email=" + this.emailUsuario);
 }
 
 private void enviarDadosDaPartidaParaOLogDoUsuarioNoBancoDeDados()
 {
	 //enviaremos as informacoes da partida num log que escreveremos para o usuário e salvaremos num servidor remoto
	 DadosPartidaParaOLog dadosPartida = new DadosPartidaParaOLog();
	 HashMap<String,LinkedList<KanjiTreinar>> categoriasEKanjis = SingletonGuardaDadosDaPartida.getInstance().getCategoriasEscolhidasEKanjisDelas();
	 String categoriasEmString = "";
	 Iterator<String> iteradorCategorias = categoriasEKanjis.keySet().iterator();
	 while(iteradorCategorias.hasNext() == true)
	 {
		 categoriasEmString = categoriasEmString + iteradorCategorias.next() + ";";
	 }
	 
	 dadosPartida.setCategoria(categoriasEmString);
	 
	 Calendar c = Calendar.getInstance();
	 SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
	 String formattedDate = df.format(c.getTime());
	 dadosPartida.setData(formattedDate);
	 
	 dadosPartida.setEmail(this.emailUsuario);
	 dadosPartida.setJogoAssociado("karuta kanji");
	 
	 dadosPartida.setPalavrasAcertadas(this.palavrasAcertadas);
	 dadosPartida.setPalavrasErradas(this.palavrasErradas);
	 dadosPartida.setPalavrasJogadas(this.palavrasJogadas);
	 dadosPartida.setPontuacao(suaPontuacao);
	 
	 if(this.suaPontuacao > this.pontuacaoDoAdversario)
	 {
		 String ganhou = getResources().getString(R.string.ganhou);
		 dadosPartida.setVoceGanhouOuPerdeu(ganhou);
	 }
	 else if(this.suaPontuacao < this.pontuacaoDoAdversario)
	 {
		 String perdeu = getResources().getString(R.string.perdeu);
		 dadosPartida.setVoceGanhouOuPerdeu(perdeu);
	 }
	 else
	 {
		 String empatou = getResources().getString(R.string.empatou);
		 dadosPartida.setVoceGanhouOuPerdeu(empatou); 
	 }
	 
	 dadosPartida.seteMailAdversario(this.emailAdversario);
	 
	 EnviarDadosDaPartidaParaLogTask armazenaNoLog = new EnviarDadosDaPartidaParaLogTask();
	 armazenaNoLog.execute(dadosPartida);
 }
 
 public void voltarAoMenuInicial(View v)
 {
		try
		{
			 Intent irMenuInicial =
						new Intent(TelaInicialMultiplayer.this, MainActivity.class);
			irMenuInicial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);	
			 startActivity(irMenuInicial);
		}
		catch(Exception e)
		{
			String mensagemerro = e.getMessage();
			mensagemerro = mensagemerro + "";
		}
	
 }
 
 public void mandarMensagemChat(View v)
 {
	 EditText textfieldMensagemDigitada = (EditText) findViewById(R.id.chatET);
 	String mensagemDigitada = textfieldMensagemDigitada.getText().toString();
 	textfieldMensagemDigitada.setText("");
 	String mensagemAdicionadaAoChat = this.adicionarMensagemNoChat(mensagemDigitada, true);
 	this.avisarAoOponenteQueDigitouMensagem(mensagemAdicionadaAoChat);
 }
 
 /*foi percebido um bug onde no comeco da primeira rodada de qualquer partida o usuario ve todas as cartas iguais e a dica do kanji nao eh a correta.
  * Irei fazer o usuario esperar um pouco antes de comecar a partida p o bug n acontecer. A espera acaba quando ambos os jogadores ja tem a dica do kanji atualizada*/
 private void comecarEsperaDoUsuarioParaComecoDaPartida()
 {
	 this.loadingComecoDaPartida = ProgressDialog.show(TelaInicialMultiplayer.this, getResources().getString(R.string.iniciandoJogo), getResources().getString(R.string.por_favor_aguarde));
 }
 
 
}
