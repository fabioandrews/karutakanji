package bancodedados;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

import com.karutakanji.MainActivity;

public class ChecaVersaoAtualDoSistemaTask extends AsyncTask<String, String, Void> 
{
	private String versaoDoSistema = "0.1.1-beta";
	private String versaoAtual; //aquela versao mais atual do jogo que iremos pegar no servidor remoto
	private MainActivity activityMain;
	private boolean usuarioEstaComVersaoMaisRecente;
	
	public ChecaVersaoAtualDoSistemaTask(MainActivity activity)
	{
		this.activityMain = activity;
	}
	
	public void pegarVersaoMaisRecenteDoJogo()
	{
		try
		{
			String url = "http://server.karutakanji.pairg.dimap.ufrn.br/app/pegarversaodosistemaatual.php";
			//String url = "http://192.168.0.110/amit/pegarversaodosistemaatual.php";
			HttpClient httpclient = new DefaultHttpClient();  
		    HttpPost httppost = new HttpPost(url);   
		    HttpResponse response = httpclient.execute(httppost); 
		    
		    InputStream inputStream = response.getEntity().getContent();
		    
		    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

		    String readData = "";
		    String line = "";

		    while((line = br.readLine()) != null){
		        readData += line;
		    }
		    
		    String todoOPhpRetornadoEmString = readData;
		    int indiceFinalDoPhp = todoOPhpRetornadoEmString.indexOf("/>");
		    this.versaoAtual = todoOPhpRetornadoEmString.substring(indiceFinalDoPhp + 2);
		    this.versaoAtual = this.versaoAtual.replaceAll(" ", "");
		}
		catch(Exception e)
		{
			//this.activityMain.mostrarErro(e.getMessage());
		}
	}
	
	public boolean usuarioEstaComAVersaoAtualDoSistema()
	{
		if(this.versaoAtual.compareTo(this.versaoDoSistema) == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	protected Void doInBackground(String... arg0) 
	{
		this.pegarVersaoMaisRecenteDoJogo();
		this.usuarioEstaComVersaoMaisRecente = this.usuarioEstaComAVersaoAtualDoSistema();
		
		return null;
	}
	
	protected void onPostExecute(Void v) 
	{
		if(this.usuarioEstaComVersaoMaisRecente == false)
		{
			this.activityMain.mudarParaTelaAtualizeOJogo(this.versaoAtual);
		}
	}
}
