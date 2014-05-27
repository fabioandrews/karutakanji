package bancodedados;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import com.karutakanji.R;
import com.karutakanji.TelaInicialMultiplayer;

public class MyCustomAdapter1Jogador extends ArrayAdapter<CategoriaDeKanjiParaListviewSelecionavel> {
	  
	  private ArrayList<CategoriaDeKanjiParaListviewSelecionavel> categoriaDeKanjiList;
	  private Context contexto;
	  
	  public MyCustomAdapter1Jogador(Context context, int textViewResourceId,
	    ArrayList<CategoriaDeKanjiParaListviewSelecionavel> categoriaDeKanjiList) {
	   super(context, textViewResourceId, categoriaDeKanjiList);
	   this.categoriaDeKanjiList = new ArrayList<CategoriaDeKanjiParaListviewSelecionavel>();
	   this.categoriaDeKanjiList.addAll(categoriaDeKanjiList);
	   this.contexto = context;
	  }
	  
	  
	  
	  public ArrayList<CategoriaDeKanjiParaListviewSelecionavel> getCategoriaDeKanjiList() {
		return categoriaDeKanjiList;
	}



	private class ViewHolder {
	   CheckBox name;
	  }
	  
	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	  
	   ViewHolder holder = null;
	   Log.v("ConvertView", String.valueOf(position));
	  
	   if (convertView == null) {
	   LayoutInflater vi = (LayoutInflater)contexto.getSystemService(
	     Context.LAYOUT_INFLATER_SERVICE);
	   convertView = vi.inflate(R.layout.categoria_de_kanji_na_lista, null);
	  
	   holder = new ViewHolder();
	   holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
	   convertView.setTag(holder);
	   
	    holder.name.setOnClickListener( new View.OnClickListener() { 
	     public void onClick(View v) 
	     { 
	    	
	    		 CheckBox cb = (CheckBox) v ; 
	   	      CategoriaDeKanjiParaListviewSelecionavel categoriaDeKanji = (CategoriaDeKanjiParaListviewSelecionavel) cb.getTag(); 
	   	      /*Toast.makeText(contexto.getApplicationContext(),
	   	       "Clicked on Checkbox: " + cb.getText() +
	   	       " is " + cb.isChecked(),
	   	       Toast.LENGTH_LONG).show();*/
	   	      categoriaDeKanji.setSelected(cb.isChecked());
	   	      
	   	      
	    	 }
	     }); 
	   }
	   else {
	    holder = (ViewHolder) convertView.getTag();
	   }
	  
	   CategoriaDeKanjiParaListviewSelecionavel categoriaDeKanji = categoriaDeKanjiList.get(position);
	   holder.name.setText(categoriaDeKanji.getName());
	   holder.name.setChecked(categoriaDeKanji.isSelected());
	   holder.name.setTag(categoriaDeKanji);
	  
	   return convertView;
	  
	  }



	public void setCategoriaDeKanjiList(
			ArrayList<CategoriaDeKanjiParaListviewSelecionavel> categoriaDeKanjiList) {
		this.categoriaDeKanjiList = categoriaDeKanjiList;
	}
	  
	 }