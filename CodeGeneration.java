import java.io.*;
import java.util.Stack;
import java.util.ArrayList;

class ControlStruct{
   String nombre;
   String EtX,EtY,EtA;
   int dir;
   //Valores utilizados en DESDE y DEPENDIENDO:
   String ident,incVal;
   boolean fueDEC;
   /*
   Uso de las etiquetas en DEPENDIENDO:
   EtX=LabelFD
   EtY=LabelSC
   EtA=LabelGI
   */
   
   public ControlStruct(String n){
      nombre=n;
      dir=0;
      EtX=EtY=EtA="";
      
      ident="";
      incVal="1";
      fueDEC=false;
   }
}

public class CodeGeneration{
   private Sintactico sint;
   private String ruta,archivo;
   private boolean abierto;
   private FileWriter fw;
   private BufferedWriter bw;
   private PrintWriter salida;
   public int contador,etCont;
   public Stack pilaParams;
   
   public CodeGeneration(Sintactico s,String r,String a){
      sint=s;
      contador=0;
      etCont=1;
      pilaParams=new Stack();
      
      ruta=r.substring(0,r.lastIndexOf("\\"));
      ruta=ruta+"\\";
      archivo=a.substring(0,a.lastIndexOf("."));
      //archivo=archivo+".pl0";
      /*
      System.out.println("\nGeneracion de Codigo: ");
      System.out.println("Ruta: \""+ruta+"\"");
      System.out.println("Archivo: \""+archivo+"\"");
      */
      abierto=false;
      creaPL0();
   }
   
   private void creaPL0(){
      String rutaPL0=ruta+archivo+".pl0";
      
      try{
         fw=new FileWriter(rutaPL0);
         
         bw=new BufferedWriter(fw);
         salida=new PrintWriter(bw);
         
         abierto=true;
         
         escribePL0("JMP  0,$LabelP");
      }catch(IOException ioex){
         System.out.println("Error al crear el archivo "+archivo+".pl0");
      }
   }
   
   public void cierraPL0(){
      if(abierto){
         salida.close();
         abierto=false;
      }
   }
   
   public void escribeEt(String codigo){
      if(abierto){
         contador++;
         salida.println(contador+"\t"+codigo+etCont);
         etCont++;
      }
   }
   
   public void escribePL0(String codigo){
      if(abierto){
         contador++;
         salida.println(contador+"\t"+codigo);
      }
   }
   
   public void codeOpers(String op){
      if(op.equals("+"))
         escribePL0("OPR  0,2");
      else if(op.equals("-"))
         escribePL0("OPR  0,3");
      else if(op.equals("*"))
         escribePL0("OPR  0,4");
      else if(op.equals("/"))
         escribePL0("OPR  0,5");
      else if(op.equals("DIV"))
         escribePL0("OPR  0,6");
      else if(op.equals("MOD"))
         escribePL0("OPR  0,7");
      else if(op.equals("POT"))
         escribePL0("OPR  0,8");
      else if(op.equals("- unario"))
         escribePL0("OPR  0,9");
      else if(op.equals(">"))
         escribePL0("OPR  0,10");
      else if(op.equals("<"))
         escribePL0("OPR  0,11");
      else if(op.equals(">="))
         escribePL0("OPR  0,12");
      else if(op.equals("<="))
         escribePL0("OPR  0,13");
      else if(op.equals("<>"))
         escribePL0("OPR  0,14");
      else if(op.equals("="))
         escribePL0("OPR  0,15");
      else if(op.equals("|"))
         escribePL0("OPR  0,16");
      else if(op.equals("&"))
         escribePL0("OPR  0,17");
      else if(op.equals("!"))
         escribePL0("OPR  0,18");
   }
   
   public void llenaArr(int d1,int d2,String val,String id,boolean bidim,boolean directo){
      String tId;
      int cPos=id.indexOf('@');
      if(cPos!=-1)
         tId=id.substring(0,cPos);
      else
         tId=id;
      
      if(directo){
         escribePL0("LIT  "+d1+",0");
         if(bidim)
            escribePL0("LIT  "+d2+",0");
         escribePL0("LIT  "+val+",0");
         escribePL0("STO  0,"+tId);
      }else{
         String codigo;
         codigo="LIT  "+d1+",0";
         sint.tempCode.add(codigo);
         if(bidim){
            codigo="LIT  "+d2+",0";
            sint.tempCode.add(codigo);
         }
         codigo="LIT  "+val+",0";
         sint.tempCode.add(codigo);
         codigo="STO  0,"+tId;
         sint.tempCode.add(codigo);
      }
   }
   
   public void paramArr(String id,int d1,int d2){
      for(int i=0;i<d1*d2;i++)
         pilaParams.push(id);
   }
   
   public void descargaPilaParams(){
      String param;
      while(!pilaParams.empty()){
         param=(String)pilaParams.pop();
         escribePL0("STO  0,"+param);
      }
   }
   
   public void haceInit(char c,Stack pila){
      String valor="",ident="";
      if(!pila.empty()){
         valor=(String)pila.pop();
         if(valor.equals("VERDADERO"))
            valor="T";
         else if(valor.equals("FALSO"))
            valor="F";
      }
      if(c=='L'){
         //Se escribe el codigo directamente en el archivo
         while(!pila.empty()){
            ident=(String)pila.pop();
            escribePL0("LIT  "+valor+",0");
            escribePL0("STO  0,"+ident);
         }
      }else{
         //Se pone el codigo en la lista de codigo temporal
         while(!pila.empty()){
            ident=(String)pila.pop();
            String codigo;
            codigo="LIT  "+valor+",0";
            sint.tempCode.add(codigo);
            codigo="STO  0,"+ident;
            sint.tempCode.add(codigo);
         }
      }
   }
   
   public void poneInic(){
      String codigo;
      for(int i=0;i<sint.tempCode.size();i++){
         codigo=(String)sint.tempCode.get(i);
         escribePL0(codigo);
      }
   }
   
   public void enviaArr(String id,int d1,int d2){
      if(d2==0){
         //Es unidim
         escribePL0("LIT  0,0");
         escribePL0("LIT  0,0");
         for(int i=1;i<d1;i++){
            escribePL0("LOD  "+id+",0");
            escribePL0("LIT  "+i+",0");
            escribePL0("LIT  "+i+",0");
         }
      }else{
         //Es bidim
         for(int i=0;i<4;i++)
            escribePL0("LIT  0,0");
         
         for(int i=1;i<d2;i++){
            escribePL0("LOD  "+id+",0");
            escribePL0("LIT  0,0");
            escribePL0("LIT  "+i+",0");
            escribePL0("LIT  0,0");
            escribePL0("LIT  "+i+",0");
         }
         for(int i=1;i<d1;i++){
            for(int j=0;j<d2;j++){
               escribePL0("LOD  "+id+",0");
               escribePL0("LIT  "+i+",0");
               escribePL0("LIT  "+j+",0");
               escribePL0("LIT  "+i+",0");
               escribePL0("LIT  "+j+",0");
            }
         }
      }
   }
   
   private String letraTipo(String tipo){
      if(tipo.equals("<Entero>"))
         return "E";
      else if(tipo.equals("<Real>"))
         return "R";
      else if(tipo.equals("<Cadena>"))
         return "C";
      else if(tipo.equals("<Logico>"))
         return "L";
      else 
         return "N";
   }
   
   public void escribeMVI(ArrayList tabla){
      String rutaMVI=ruta+archivo+".mvi";
      try{
         FileWriter fw2=new FileWriter(rutaMVI);
         BufferedWriter bw2=new BufferedWriter(fw2);
         PrintWriter salida2=new PrintWriter(bw2);
         
         for(int i=0;i<tabla.size();i++){
            Simbolo st=(Simbolo)tabla.get(i);
            if(st.nombre.startsWith("$")){
               //Es una etiqueta
               salida2.println(st.nombre+","+st.d1);
            }else
               if(st.clase!='C'){
                  salida2.println(st.nombre+","+letraTipo(st.tipo_dato)+","+st.d1+","+st.d2);
               }
         }
         salida2.println("#");
         
         //Lee el archivo PL0 y lo vacia en el MVI
         try{
            String rutaPL0=ruta+archivo+".pl0";
            FileReader fr=new FileReader(rutaPL0);
            BufferedReader entrada=new BufferedReader(fr);
            
            String s;
            while((s=entrada.readLine())!=null){
               salida2.println(s);
            }
            
            entrada.close();
         }catch(IOException ioex){
            System.out.println("No se pudo leer el archivo: "+archivo+".pl0");
         }
         
         salida2.close();
      }catch(IOException ioex){
         System.out.println("Error al crear el archivo "+archivo+".mvi");
      }
   }
}