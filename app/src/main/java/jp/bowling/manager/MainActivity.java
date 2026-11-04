package jp.bowling.manager;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import android.text.*;
import org.json.*;
import java.util.*;

public class MainActivity extends Activity {
  final int NAVY=Color.rgb(18,39,65), BLUE=Color.rgb(22,101,180);
  LinearLayout root, list; EditText search; Spinner makerFilter, materialFilter, sort;
  JSONArray balls=new JSONArray();
  final String[] makers={"すべて","STORM","ROTO GRIP","HAMMER","MOTIV","BRUNSWICK","DV8","EBONITE","900 GLOBAL","TRACK","COLUMBIA 300","その他"};
  final String[] materials={"すべて","ソリッド","パール","ハイブリッド","ウレタン","その他"};
  final String[] sorts={"登録が新しい順","ボール名順","メーカー順","素材順","RG 小さい順","ΔRG 大きい順"};
  String[] fields={"ボール名 *","メーカー","素材","重さ (lb)","RG","ΔRG","INT Diff","コア名","カバー名","表面仕上げ","レイアウト","メモ","公式製品URL"};
  String[] keys={"name","maker","material","weight","rg","diff","intdiff","core","cover","finish","layout","memo","url"};
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(NAVY);load();draw();}
  void load(){try{balls=new JSONArray(getPreferences(0).getString("balls","[]"));}catch(Exception e){balls=new JSONArray();}}
  void save(){getPreferences(0).edit().putString("balls",balls.toString()).apply();}
  TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(10,9,10,9);return t;}
  LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  Button button(String label){Button b=new Button(this);b.setText(label);b.setAllCaps(false);return b;}
  Spinner spinner(String[] data){Spinner s=new Spinner(this);ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,data);s.setAdapter(a);return s;}
  void draw(){
    root=vertical();root.setBackgroundColor(Color.rgb(246,248,251));setContentView(root);
    TextView header=text("🎳 ボウリング管理",23,Color.WHITE);header.setBackgroundColor(NAVY);header.setPadding(20,25,15,25);root.addView(header);
    Button add=button("＋ ボールを登録");root.addView(add);add.setOnClickListener(v->edit(-1));
    search=new EditText(this);search.setSingleLine(true);search.setHint("ボール名で検索");root.addView(search);
    makerFilter=spinner(makers);materialFilter=spinner(materials);sort=spinner(sorts);
    root.addView(text("メーカーで絞り込み",12,NAVY));root.addView(makerFilter);
    root.addView(text("素材で絞り込み",12,NAVY));root.addView(materialFilter);
    root.addView(text("並び替え",12,NAVY));root.addView(sort);
    ScrollView sc=new ScrollView(this);list=vertical();sc.addView(list);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
    search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int before,int count){refresh();}public void afterTextChanged(Editable e){}});
    AdapterView.OnItemSelectedListener listener=new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int pos,long id){refresh();}};
    makerFilter.setOnItemSelectedListener(listener);materialFilter.setOnItemSelectedListener(listener);sort.setOnItemSelectedListener(listener);
    refresh();
  }
  String get(JSONObject o,String k){return o.optString(k,"");}
  double num(JSONObject o,String k,double fallback){try{return Double.parseDouble(get(o,k));}catch(Exception e){return fallback;}}
  void refresh(){
    if(list==null)return;list.removeAllViews();
    ArrayList<Integer> ids=new ArrayList<>();
    String q=search.getText().toString().toLowerCase(Locale.ROOT);
    String m=makers[makerFilter.getSelectedItemPosition()], mat=materials[materialFilter.getSelectedItemPosition()];
    for(int i=0;i<balls.length();i++){JSONObject o=balls.optJSONObject(i);if(o==null)continue;
      if(!get(o,"name").toLowerCase(Locale.ROOT).contains(q))continue;
      if(!m.equals("すべて")&&!get(o,"maker").equals(m))continue;
      if(!mat.equals("すべて")&&!get(o,"material").equals(mat))continue;
      ids.add(i);
    }
    int s=sort.getSelectedItemPosition();
    Collections.sort(ids,(a,b)->{
      JSONObject x=balls.optJSONObject(a),y=balls.optJSONObject(b);
      if(s==0)return Integer.compare(b,a);
      if(s==1)return get(x,"name").compareToIgnoreCase(get(y,"name"));
      if(s==2)return get(x,"maker").compareToIgnoreCase(get(y,"maker"));
      if(s==3)return get(x,"material").compareToIgnoreCase(get(y,"material"));
      if(s==4)return Double.compare(num(x,"rg",999),num(y,"rg",999));
      return Double.compare(num(y,"diff",-1),num(x,"diff",-1));
    });
    list.addView(text("表示 "+ids.size()+" 球 / 登録 "+balls.length()+" 球",14,NAVY));
    if(ids.isEmpty())list.addView(text("ボールがありません。「＋ ボールを登録」から追加してください。",15,Color.DKGRAY));
    for(int id:ids){
      JSONObject o=balls.optJSONObject(id);
      LinearLayout card=vertical();card.setPadding(12,10,12,12);card.setBackgroundColor(Color.WHITE);
      LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(10,5,10,5);list.addView(card,cp);
      card.addView(text("● "+get(o,"name"),19,NAVY));
      card.addView(text(get(o,"maker")+"  /  "+get(o,"material")+"  /  "+get(o,"weight")+" lb",13,Color.DKGRAY));
      card.addView(text("RG "+get(o,"rg")+"     ΔRG "+get(o,"diff"),13,BLUE));
      Button details=button("詳細・編集");card.addView(details);details.setOnClickListener(v->details(id));
    }
  }
  void details(int id){
    JSONObject o=balls.optJSONObject(id);if(o==null)return;
    StringBuilder sb=new StringBuilder();
    for(int i=0;i<keys.length;i++)sb.append(fields[i]).append("：").append(get(o,keys[i])).append("\n");
    AlertDialog.Builder d=new AlertDialog.Builder(this).setTitle(get(o,"name")).setMessage(sb.toString())
      .setPositiveButton("編集",(x,w)->edit(id))
      .setNeutralButton("公式サイト検索",(x,w)->official(o));
    d.setNegativeButton("その他",(x,w)->new AlertDialog.Builder(this).setItems(new String[]{"削除","閉じる"},(dd,choice)->{if(choice==0)confirmDelete(id);}).show()).show();
  }
  void confirmDelete(int id){new AlertDialog.Builder(this).setTitle("削除しますか？").setMessage(get(balls.optJSONObject(id),"name"))
    .setPositiveButton("削除",(d,w)->{JSONArray next=new JSONArray();for(int i=0;i<balls.length();i++)if(i!=id)next.put(balls.optJSONObject(i));balls=next;save();refresh();})
    .setNegativeButton("キャンセル",null).show();}
  void official(JSONObject o){
    String url=get(o,"url").trim();
    if(!url.isEmpty()&&(url.startsWith("https://")||url.startsWith("http://"))){open(url);return;}
    String maker=get(o,"maker").toUpperCase(Locale.ROOT), domain="";
    if(maker.equals("STORM"))domain="stormbowling.com";
    else if(maker.equals("ROTO GRIP"))domain="rotogrip.com";
    else if(maker.equals("HAMMER"))domain="hammerbowling.com";
    else if(maker.equals("MOTIV"))domain="motivbowling.com";
    else if(maker.equals("BRUNSWICK"))domain="brunswickbowling.com";
    String q=get(o,"maker")+" "+get(o,"name")+" bowling ball specifications RG differential"+(domain.isEmpty()?"":" site:"+domain);
    open("https://www.google.com/search?q="+Uri.encode(q));
  }
  void open(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception e){Toast.makeText(this,"ブラウザを開けません",Toast.LENGTH_SHORT).show();}}
  void edit(int id){
    JSONObject old=id>=0?balls.optJSONObject(id):new JSONObject();
    ScrollView sc=new ScrollView(this);LinearLayout form=vertical();form.setPadding(20,10,20,10);sc.addView(form);
    EditText[] inputs=new EditText[keys.length];
    for(int i=0;i<keys.length;i++){
      form.addView(text(fields[i],13,NAVY));EditText e=new EditText(this);e.setSingleLine(i!=11);e.setText(get(old,keys[i]));
      if(i==3||i==4||i==5||i==6)e.setInputType(8194);
      e.setHint(fields[i]);form.addView(e);inputs[i]=e;
    }
    TextView hint=text("素材は「ソリッド」「パール」「ハイブリッド」「ウレタン」「その他」から入力してください。メーカー名は大文字で統一すると絞り込みやすくなります。",12,Color.DKGRAY);form.addView(hint);
    new AlertDialog.Builder(this).setTitle(id<0?"ボールを追加":"ボールを編集").setView(sc)
      .setPositiveButton("保存",(d,w)->{
        if(inputs[0].getText().toString().trim().isEmpty()){Toast.makeText(this,"ボール名が必要です",Toast.LENGTH_LONG).show();return;}
        JSONObject o=new JSONObject();try{for(int i=0;i<keys.length;i++)o.put(keys[i],inputs[i].getText().toString().trim());}catch(Exception ignored){}
        if(id<0)balls.put(o);else try{balls.put(id,o);}catch(Exception ignored){}
        save();refresh();
      }).setNegativeButton("キャンセル",null).show();
  }
}
