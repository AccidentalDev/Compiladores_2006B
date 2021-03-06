import java.io.*;
import javax.swing.text.*;
import java.util.Vector;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;

class Simbolo{
   String nombre;
   public char clase;
   public String tipo_dato;
   public int d1,d2;
   public String cspcfyp;
   
   public Simbolo(String n,char c,String td,String s){
      nombre=n;
      clase=c;
      tipo_dato=td;
      cspcfyp=s;
      d1=d2=0;
   }
}
class Proto{
   String nombre;
   boolean esta;
   ArrayList parametros;
   String cspcfyp;
   String tipo_dato;
   char clase;
   int endPos,linea;
   String dir;
   
   public Proto(String n,char c,String d,int end,int l){
      nombre=n;
      clase=c;
      esta=false;
      cspcfyp="";
      tipo_dato="<Ninguno>";
      parametros=new ArrayList(5);
      endPos=end;
      linea=l;
      dir=d;
   }
}
class Padre{
   String idKey;
   char tipo;//Puede ser: A:Arreglo, M:Modulo, E:Escribir, C:Estructura de control, S:Asignacion
   ArrayList params;
   public Padre(String id,char t,ArrayList p){
      idKey=id;
      tipo=t;
      params=p;
   }
}
class Operador{
   String op;
   Padre padre;
   public Operador(String o,Padre p){
      op=o;
      padre=p;
   }
}
public class Sintactico{
   private Editor editor;
   private Ventana win;
   public String lexema,token,fuente,reserved[];
   protected int estado,tipoChar;
   private char c;
   protected Vector librerias;
   public int lineCounter,i,etapa,tipoElem,estate,nivel;
   public Stack infoStates,padresState;
   protected Hashtable Reglas,ReglasE,tabSim,tabProtos;
   private ErrorHandler mError;
   public boolean reuse,modulosBan;
   //Temporales para el analisis sintactico:
   private String idKey,tipo,cspcfyp,modulo,idAsign;
   private char clase;
   private int d1,d2,d1c,d2c;
   private int ProtOMod,argC[]={0,0,0,0,0,0,0,0,0,0},arrAC=0;
   private boolean tieneProto,esLocal,enArreglo,enModulo,proceder,hayReturn;
   private ArrayList params,modulos;
   private Stack pilaPadres;
   public Stack pilaTipos,pilaOp;
   private VerificacionTipos VT;
   //Para revisar que las dimensiones est?n dentro del rango
   private String dimVal;
   private boolean solo;
   //Para la generacion de codigo:
   public CodeGeneration codGen;
   public Stack pilaStructs,pilaDirs,pilaInit;
   public ArrayList tempCode;
   public boolean enSiNo;
   
   public Sintactico(Ventana v,Editor e,Hashtable tabla,Hashtable protos,ArrayList tC,CodeGeneration cg){
      editor=e;
      win=v;
      i=-1;
      lineCounter=0;
      lexema="";
      estado=0;
      etapa=0;//1=Archivos, 2=Constantes, 3=Variables, 4=Arreglos, 5=Registros, 6=Modulos, 7=Principal
      estate=0;
      modulosBan=false;
      reserved=new String[37];
      getReserved();
      
      librerias=new Vector(5,2);
      infoStates=new Stack();
      padresState=new Stack();
      Reglas=new Hashtable(33);
      ReglasE=new Hashtable(33);
      llenaReglas();
      if(tabla!=null){
         tabSim=tabla;
         tabProtos=protos;
         codGen=cg;
         tempCode=tC;
      }else{
         tabSim=new Hashtable(60);
         tabProtos=new Hashtable(6);
         codGen=new CodeGeneration(this,win.ruta,win.nombre);
         tempCode=new ArrayList(2);
      }
      idKey="";//La llave en la tabla de simbolos sobre la que se esta trabajando
      clase='G';//La clase del identificador sobre el que se esta trabajando
      tipo="<Ninguno>";//El tipo de dato
      d1=d2=2;//Las dimensiones
      d1c=d2c=0;//Los contadores de elementos en la inicializacion de arreglos
      cspcfyp="";//Para irlo llenando antes de meterlo al objeto simbolo sobre el que se este trabajando
      modulo="";//Para saber dentro de que modulo esta el identificador que se esta declarando (incluyendo dentro de Principal)
      ProtOMod=0;//Indica si se est? dentro de Prototipos (1) o de Modulos (2)
      tieneProto=false;//Para ver si el modulo tiene un prototipo y asi hacer las comparaciones correspondientes
      //argC=0;//Va contando los argumentos en el encabezado de un modulo
      arrAC=0;//Si hay llamadas a modulos anidadas, para moverse dentro del arreglo argC
      esLocal=false;//Cuando busc? un identificador y lo encontr? como local, para que las siguentes revisiones sean sobre ese
      params=new ArrayList();//Para desglosar lo que tenga cspcfyp en elementos identificables
      pilaPadres=new Stack();//Para que expresion sepa para quien esta haciendo el analisis
      enModulo=enArreglo=false;//Para que expresion sepa como hacer el analisis semantico
      hayReturn=false;//Para saber si se encontr? un REGRESAR dentro del modulo
      proceder=false;//Cuando se encontr? la palabra PROCEDIMIENTO o FUNCION
      dimVal="";//Para revisar que la dimension est? dentro del rango
      idAsign="";//Para guardar el identificador sobre el que se le est? haciendo la asignacion
      solo=false;//Para asegurarse de que s?lo se meti? un valor por dimension
      enSiNo=false;//Para ver si est? en Si o en SiNoSi
      modulos=new ArrayList();//Para verificar que no se repitan modulos
      pilaTipos=new Stack();
      pilaOp=new Stack();
      pilaStructs=new Stack();//Para las estructuras de control, sus etiquetas y todo eso
      pilaDirs=new Stack();//Para las llamadas a modulos, las etiquetas para las direcciones de regreso
      pilaInit=new Stack();//Para las inicializaciones encadenadas y simples.
      
      VT=new VerificacionTipos(this);
      mError=new ErrorHandler(this);
      
      
      if(win.areaTexto!=null){
         try{
            fuente=win.areaTexto.getDocument().getText(0,win.areaTexto.getDocument().getLength());
         }catch(BadLocationException ble){
            System.out.println("Error al capturar el texto para analizarlo");
            fuente=" ";
         }
      }else{
         //Abre directamente el archivo
         try{
            FileReader fr=new FileReader(win.ruta);
            BufferedReader entrada=new BufferedReader(fr);
            
            int pos=win.ruta.lastIndexOf('\\');
            String tmpName=win.ruta.substring(pos+1);
            
            String s=entrada.readLine();
            if(s!=null){
               fuente=s.toString();
               while((s=entrada.readLine())!=null){
                  fuente=fuente+"\n"+s.toString();
               }
            }
            entrada.close();
            
         }catch(IOException ioex){
            System.out.println("No se pudo abrir el archivo: "+win.ruta);
         }
      }
   }
   
   private void getReserved(){
      String s;
      int a=0;
      try{
         FileReader fr=new FileReader("Palabras Reservadas.txt");
         BufferedReader entrada=new BufferedReader(fr);
         while((s=entrada.readLine())!=null){
            reserved[a]=s;
            a++;
         }
         entrada.close();
      }catch(IOException ioex){
         System.out.println("No se pudo abrir el archivo de palabras reservadas");
      }
   }
   private int findReserved(String cadena){
      boolean si=false;
      int n=-1;
      
      for(int a=0;a<reserved.length&&!si;a++)
         if(reserved[a].equals(cadena)){
            si=true;
            n=a;
         }
      
      return n;
   }
   
   public int tipoLexema(){
      if(lexema.equals("ARCHIVOS")){
         return 1;
      }else if(lexema.equals("[")){
         return 2;
      }else if(lexema.equals("]")){
         return 3;
      }else if(lexema.equals(",")){
         return 4;
      }else if(token.equals("<Cadena>")){
         return 5;
      }else if(lexema.equals("CONSTANTES")){
         return 6;
      }else if(token.equals("<Identificador>")){
         return 7;
      }else if(token.equals("<Op_Asign>")){
         return 8;
      }else if(token.equals("<Entero>")){
         return 9;
      }else if(token.equals("<Real>")){
         return 10;
      }else if(token.equals("<Logico>")){
         return 11;
      }else if(lexema.equals("VARIABLES")){
         return 12;
      }else if(lexema.equals(";")){
         return 13;
      }else if(lexema.equals("ENTERO")){
         return 14;
      }else if(lexema.equals("REAL")){
         return 15;
      }else if(lexema.equals("CADENA")){
         return 16;
      }else if(lexema.equals("LOGICO")){
         return 17;
      }else if(lexema.equals("ARREGLOS")){
         return 18;
      }else if(lexema.equals("{")){
         return 19;
      }else if(lexema.equals("}")){
         return 20;
      }else if(lexema.equals("REGISTROS")){
         return 21;
      }else if(lexema.equals("MODULOS")){
         return 22;
      }else if(lexema.equals("PROTOTIPOS")){
         return 23;
      }else if(lexema.equals("PRINCIPAL")){
         return 24;
      }else if(lexema.equals("(")){
         return 25;
      }else if(lexema.equals(")")){
         return 26;
      }
      
      return 0;
   }
   public void Programa(){
      //estate=0;
      nivel=0;
      reuse=false;
      tipoElem=0;
      
      token=dameToken();
      
      while(token!="\0"){
         
         //for(int n=0;n<nivel;n++)
         //   System.out.print("   ");
         
         if(!reuse){
            tipoElem=tipoLexema();
            //System.out.print(lexema);
         }else
            reuse=false;
         
         //System.out.println(" "+estate+" ");
         
         switch(estate){
            case 0:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  idKey="";
                  clase='G';
                  tipo="<Ninguno>";
                  d1=d2=2;
                  d1c=d2c=0;
                  modulo="";
                  ProtOMod=0;
                  tieneProto=false;
                  argC[0]=0;
                  esLocal=false;
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==1){
                     estate=1;
                     if(etapa==0){
                        etapa=1;
                     }else
                        if(etapa==1){
                           String err="No se puede repetir la declaracion de archivos: "+lexema;
                           poneError(2,err);
                        }else{
                           String err="Los archivos se declaran solo al principio del programa: "+lexema;
                           poneError(2,err);
                        }
                  }else
                     if(tipoElem==6){
                        infoStates.push(new Integer(0));
                        padresState.push(new Integer(0));
                        estate=5;
                        nivel++;
                        if(etapa<=1){
                           etapa=2;
                        }else{
                           String err="Las constantes se deben declarar una sola vez y solo al principio del programa despues de los archivos si los hay.";
                           poneError(2,err);
                        }
                     }else
                        if(tipoElem==12){
                           infoStates.push(new Integer(0));
                           padresState.push(new Integer(0));
                           estate=11;
                           nivel++;
                           if(etapa<=2){
                              etapa=3;
                           }else{
                              String err="Las variables se deben declarar una sola vez y solo despues de los archivos y las constantes si los hay.";
                              poneError(2,err);
                           }
                        }else
                           if(tipoElem==18){
                              infoStates.push(new Integer(0));
                              padresState.push(new Integer(0));
                              estate=19;
                              nivel++;
                              if(etapa<=3){
                                 etapa=4;
                              }else{
                                 String err="Los arreglos se deben declarar una sola vez y solo despues de los archivos, las constantes y/o las variables si los hay.";
                                 poneError(2,err);
                              }
                           }else
                              if(tipoElem==21){
                                 infoStates.push(new Integer(0));
                                 padresState.push(new Integer(0));
                                 estate=40;
                                 nivel++;
                                 if(etapa<=4){
                                    etapa=5;
                                 }else{
                                    String err="Los registros se deben declarar una sola vez y solo despues de los archivos, las constantes, las variables y/o los arreglos, si los hay.";
                                    poneError(2,err);
                                 }
                              }else
                                 if(tipoElem==23){
                                    infoStates.push(new Integer(0));
                                    padresState.push(new Integer(0));
                                    estate=53;
                                    nivel++;
                                    if(etapa<=5){
                                       etapa=6;
                                    }else{
                                       String err="Los prototipos se deben declarar una sola vez y solo despues de los archivos, las constantes, las variables, los arreglos y/o los registros, si los hay.";
                                       poneError(2,err);
                                    }
                                 }else
                                    if(tipoElem==22){
                                       infoStates.push(new Integer(0));
                                       padresState.push(new Integer(0));
                                       estate=57;
                                       nivel++;
                                       if(etapa<=6){
                                          etapa=6;
                                       }else{
                                          String err="Los modulos se deben declarar una sola vez y solo despues de los archivos, las constantes, las variables, los arreglos y/o los registros, si los hay.";
                                          poneError(2,err);
                                       }
                                    }else
                                       if(tipoElem==24){
                                          estate=62;
                                          if(etapa<=6){
                                             etapa=7;
                                          }else{
                                             String err="El bloque Principal del programa solo se debe declarar una vez y solo al final de todas las otras declaraciones.";
                                             poneError(2,err);
                                          }
                                       }else{
                                          String err="C?digo no previsto: "+lexema;
                                          poneError(2,err);
                                          
                                          estate=mError.recuperaPrograma();
                                       }
            }break;
            //-------------------------------- ARCHIVOS --------------------------------
            case 1:{
                  if(tipoElem==2)
                     estate=2;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     estate=mError.recuperaArchivos();
                  }
            }break;
            case 2:{
                  if(tipoElem==5){
                     estate=3;
                     existeLibreria();
                  }else{
                     String err="Se esperaba una cadena y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     estate=mError.recuperaArchivos();
                  }
            }break;
            case 3:{
                  if(tipoElem==3)
                     estate=4;
                  else
                     if(tipoElem==4)
                        estate=2;
                     else{
                        String err="Se esperaba una coma o un corchete de cierre y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        estate=mError.recuperaArchivos();
                     }
            }break;
            case 4:{
                  estate=0;
                  reuse=true;
            }break;
            //----------------------------- Fin de ARCHIVOS ----------------------------
            
            //------------------------------- CONSTANTES -------------------------------
            case 5:{
                  if(tipoElem==2)
                     estate=6;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaConstantes(tmp);
                  }
            }break;
            case 6:{
                  if(tipoElem==7){
                     estate=7;
                     
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Agrega el simbolo en la tabla y guarda su llave en una variable
                     //temporal para cuando se le pone su valor
                     if(!tabSim.containsKey(lexema)){
                        tabSim.put(lexema,new Simbolo(lexema,'C',"<Ninguno>",""));
                        idKey=lexema;
                     }else{
                        String err="Ya est? definida una Constante con ese nombre: "+lexema;
                        poneError(3,err);
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaConstantes(tmp);
                  }
            }break;
            case 7:{
                  if(tipoElem==8)
                     estate=8;
                  else{
                     String err="Se esperaba el operador de asignacion (<-) y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaConstantes(tmp);
                  }
            }break;
            case 8:{
                  if(tipoElem==5||tipoElem==9||tipoElem==10||tipoElem==11){
                     estate=9;
                     
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     if(idKey!=""){
                        Simbolo simb=(Simbolo)tabSim.get(idKey);
                        if(simb!=null){
                           simb.tipo_dato=token;
                           simb.cspcfyp=lexema;
                        }
                        idKey="";
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     idKey="";
                     
                     String err="Se esperaba un valor y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaConstantes(tmp);
                  }
            }break;
            case 9:{
                  if(tipoElem==3)
                     estate=10;
                  else
                     if(tipoElem==4)
                        estate=6;
                     else{
                        String err="Se esperaba una coma o un corchete de cierre y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaConstantes(tmp);
                     }
            }break;
            case 10:{
                  /*estate=0;
                  reuse=true;
                  */
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  if(nivel>0)
                     nivel--;
            }break;
            //--------------------------- Fin de CONSTANTES ----------------------------
            
            //------------------------------- VARIABLES --------------------------------
            case 11:{
                  if(tipoElem==2)
                     estate=12;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaVariables(0,tmp);
                  }
            }break;
            case 12:{
                  if(tipoElem==14||tipoElem==15||tipoElem==16||tipoElem==17){
                     estate=13;
                     
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     revisaTipo();
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     tipo="<Ninguno>";
                     
                     String err="Se esperaba un tipo de dato encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaVariables(0,tmp);
                  }
            }break;
            case 13:{
                  if(tipoElem==7){
                     estate=14;
                     //Generacion de codigo:
                     pilaInit.push(lexema);
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa si se esta en global o en local:
                     String err;
                     int tmp=((Integer)infoStates.peek()).intValue();
                     if(tmp!=0&&tmp!=250){
                        //Esta en local:
                        if(!modulo.equals(""))
                           idKey=lexema+"@"+modulo;
                        else
                           idKey="";
                     }else{
                        //Esta en global:
                        idKey=lexema;
                        //Generacion de codigo:
                        if(tempCode.size()<30)
                           tempCode.ensureCapacity(30);
                        else
                           tempCode.ensureCapacity(tempCode.size()+8);
                     }
                     //Agrega el simbolo en la tabla
                     if(!tabSim.containsKey(idKey)){
                        boolean error=false;
                        if(tmp!=0&&tmp!=250){
                           clase='L';
                           //Revisa si no existe una constante o un modulo con ese nombre:
                           Simbolo sm=(Simbolo)tabSim.get(lexema);
                           if(sm!=null){
                              if(sm.clase=='C'){
                                 error=true;
                                 err="Ya existe una constante con ese nombre: "+lexema;
                                 poneError(3,err);
                              }else if(sm.clase=='P'||sm.clase=='F'){
                                 error=true;
                                 err="Ya existe un modulo con ese nombre: "+lexema;
                                 poneError(3,err);
                              }
                           }
                        }else
                           clase='G';
                        
                        if(idKey!=""&&!error)
                           tabSim.put(idKey,new Simbolo(idKey,clase,tipo,""));
                     }else{
                        if(tmp!=0&&tmp!=250&&modulo!="")
                           err="Ya existe un identificador dentro de "+modulo+" con ese nombre: "+lexema;
                        else
                           err="Ya est? definido un identificador con ese nombre: "+lexema;
                        poneError(3,err);
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaVariables(0,tmp);
                  }
            }break;
            case 14:{
                  //Generacion de codigo:
                  if(tipoElem!=8&&!pilaInit.empty())
                     pilaInit.pop();
                  
                  if(tipoElem==3){
                     estate=15;
                     tipo="<Ninguno>";
                  }else
                     if(tipoElem==4){
                        estate=13;
                     }else
                        if(tipoElem==8){
                           estate=16;
                        }else
                           if(tipoElem==13){
                              estate=12;
                           }else{
                              String err="Se esperaba: \",\" ? \"]\" ? \";\" ? \"<-\" y se encontr?: "+lexema;
                              poneError(2,err);
                              
                              int tmp=((Integer)infoStates.peek()).intValue();
                              estate=mError.recuperaVariables(1,tmp);
                           }
            }break;
            case 15:{
                  //Generacion de codigo:
                  pilaInit.clear();
                  
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  if(nivel>0)
                     nivel--;
            }break;
            case 16:{
                  if(tipoElem==7){
                     estate=18;
                     //Generacion de codigo:
                     pilaInit.push(lexema);
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     int tmp=((Integer)infoStates.peek()).intValue();
                     if(tmp!=0&&tmp!=250){
                        clase='L';
                        if(!modulo.equals(""))
                           idKey=lexema+"@"+modulo;
                        else
                           idKey="";
                     }else{
                        idKey=lexema;
                        clase='G';
                     }
                     //Agrega el simbolo en la tabla
                     if(!tabSim.containsKey(idKey)&&!tabSim.containsKey(lexema)){
                        tabSim.put(idKey,new Simbolo(idKey,clase,tipo,""));
                     }else{
                        //Revisa si es una constante:
                        Simbolo st=(Simbolo)tabSim.get(lexema);
                        if(st!=null&&st.clase=='C'){
                           estate=17;
                           
                           //Generacion de codigo
                           pilaInit.pop();
                           pilaInit.push(st.cspcfyp);
                           
                           if(!st.tipo_dato.equals(tipo)){
                              //System.out.print("  >>Los tipos no concuerdan: "+tipo+", "+st.tipo_dato+"<<  ");
                              if(!st.tipo_dato.equals("<Ninguno>")&&!tipo.equals("<Ninguno>")){
                                 //System.out.print("  >>Ninguno es <Ninguno><<  ");
                                 if(tipo.equals("<Real>")){
                                    if(!st.tipo_dato.equals("<Entero>")){
                                       String err="Incongruencia en tipos de dato: "+lexema;
                                       poneError(3,err);
                                    }
                                 }else{
                                    String err="Incongruencia en tipos de dato: "+lexema;
                                    poneError(3,err);
                                 }
                              }
                           }
                        }else
                           if(!tabSim.containsKey(idKey)){
                              if(tmp!=0&&tmp!=250){
                                 if(st!=null&&(st.clase=='P'||st.clase=='F')){
                                    String err="Ya existe un modulo con ese nombre: "+lexema;
                                    poneError(3,err);
                                 }else
                                    tabSim.put(idKey,new Simbolo(idKey,clase,tipo,""));
                              }else
                                 tabSim.put(idKey,new Simbolo(idKey,clase,tipo,""));
                           }else{
                              String err="Ya est? definido un identificador con ese nombre: "+lexema;
                              poneError(3,err);
                           }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==5||tipoElem==9||tipoElem==10||tipoElem==11){
                        estate=17;
                        //Generacion de codigo:
                        pilaInit.push(lexema);
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        if(!tipo.equals(token)){
                           if(!tipo.equals("<Ninguno>")&&!token.equals("<Ninguno>")){
                              if(tipo.equals("<Real>")){
                                 if(!token.equals("<Entero>")){
                                    String err="Incongruencia en tipos de dato: "+lexema;
                                    poneError(3,err);
                                 }
                              }else{
                                 String err="Incongruencia en tipos de dato: "+lexema;
                                 poneError(3,err);
                              }
                           }
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        String err="Se esperaba un identificador o un valor y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaVariables(1,tmp);
                     }
            }break;
            case 17:{
                  //Generacion de codigo:
                  codGen.haceInit(clase,pilaInit);
                  
                  if(tipoElem==3){
                     estate=15;
                     tipo="<Ninguno>";
                  }else
                     if(tipoElem==4)
                        estate=13;
                     else
                        if(tipoElem==13)
                           estate=12;
                        else{
                           String err="Se esperaba: \",\" ? \"]\" ? \";\" y se encontr?: "+lexema;
                           poneError(2,err);
                           
                           int tmp=((Integer)infoStates.peek()).intValue();
                           estate=mError.recuperaVariables(1,tmp);
                        }
            }break;
            case 18:{
                  if(tipoElem==8)
                     estate=16;
                  else{
                     String err="Se esperaba el operador de asignacion (<-) y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaVariables(1,tmp);
                  }
            }break;
            //---------------------------- Fin de VARIABLES ----------------------------
            
            //-------------------------------- ARREGLOS --------------------------------
            case 19:{
                  if(tipoElem==2)
                     estate=20;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaArreglos(0,tmp);
                  }
            }break;
            case 20:{
                  if(tipoElem==14||tipoElem==15||tipoElem==16||tipoElem==17){
                     estate=21;
                     
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     revisaTipo();
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     tipo="<Ninguno>";
                     
                     String err="Se esperaba un tipo de dato y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaArreglos(0,tmp);
                  }
            }break;
            case 21:{
                  if(tipoElem==7){
                     estate=22;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa si se esta en global o en local:
                     int tmp=((Integer)infoStates.peek()).intValue();
                     if(tmp!=0&&tmp!=250){
                        clase='L';
                        if(!modulo.equals(""))
                           idKey=lexema+"@"+modulo;
                        else
                           idKey="";
                     }else{
                        idKey=lexema;
                        clase='G';
                     }
                     //Agrega el simbolo en la tabla
                     if(!tabSim.containsKey(idKey)){
                        if(idKey!=""){
                           boolean error=false;
                           String err;
                           if(idKey.indexOf('@')!=-1){
                              //Revisa si no existe una constante o un modulo con ese nombre:
                              Simbolo sm=(Simbolo)tabSim.get(lexema);
                              if(sm!=null){
                                 if(sm.clase=='C'){
                                    error=true;
                                    err="Ya existe una constante con ese nombre: "+lexema;
                                    poneError(3,err);
                                 }else if(sm.clase=='P'||sm.clase=='F'){
                                    error=true;
                                    err="Ya existe un modulo con ese nombre: "+lexema;
                                    poneError(3,err);
                                 }
                              }
                           }
                           
                           if(!error)
                              tabSim.put(idKey,new Simbolo(idKey,clase,tipo,""));
                           else
                              idKey="";
                        }
                     }else{
                        String err;
                        if(tmp!=0&&tmp!=250&&modulo!="")
                           err="Ya existe un identificador dentro de "+modulo+" con ese nombre: "+lexema;
                        else
                           err="Ya est? definido un identificador con ese nombre: "+lexema;
                        poneError(3,err);
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     idKey="";
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaArreglos(0,tmp);
                  }
            }break;
            case 22:{
                  if(tipoElem==19)
                     estate=23;
                  else{
                     String err="Se esperaba una llave de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaArreglos(0,tmp);
                  }
            }break;
            case 23:{
                  if(tipoElem==9){
                     estate=24;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa que este dentro del rango:
                     d1=Integer.parseInt(lexema);
                     if(d1<2){
                        d1=2;
                        String err="Los arreglos deben tener un tama?o minimo de 2: "+lexema;
                        poneError(3,err);
                     }else
                        if(d1>32768){
                           d1=32768;
                           String err="Los arreglos deben tener un tama?o maximo de 32768: "+lexema;
                           poneError(3,err);
                        }
                     Simbolo simb=(Simbolo)tabSim.get(idKey);
                     if(simb!=null){
                        simb.d1=d1;
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     if(tipoElem==7){
                        estate=24;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        //Revisa que sea una constante
                        Simbolo sim=(Simbolo)tabSim.get(lexema);
                        if(sim!=null){
                           if(sim.clase=='C'){
                              if(sim.tipo_dato.equals("<Entero>")){
                                 //Revisa que este dentro del rango:
                                 d1=Integer.parseInt(sim.cspcfyp);
                                 if(d1<2){
                                    d1=2;
                                    String err="Los arreglos deben tener un tama?o minimo de 2: "+lexema;
                                    poneError(3,err);
                                 }else
                                    if(d1>32768){
                                       d1=32768;
                                       String err="Los arreglos deben tener un tama?o maximo de 32768: "+lexema;
                                       poneError(3,err);
                                    }
                                 Simbolo simb=(Simbolo)tabSim.get(idKey);
                                 if(simb!=null){
                                    simb.d1=d1;
                                 }
                              }else{
                                 Simbolo simb=(Simbolo)tabSim.get(idKey);
                                 if(simb!=null){
                                    simb.d1=2;
                                 }
                                 String err="La constante que define el tama?o del arreglo debe tener valor entero: "+lexema;
                                 poneError(3,err);
                              }
                           }else{
                              Simbolo simb=(Simbolo)tabSim.get(idKey);
                              if(simb!=null){
                                 simb.d1=2;
                              }
                              String err="Se esperaba una constante con valor entero y se encontr?: "+lexema;
                              poneError(3,err);
                           }
                        }else{
                           Simbolo simb=(Simbolo)tabSim.get(idKey);
                           if(simb!=null){
                              simb.d1=2;
                           }
                           String err="Se esperaba un identificador constante existente con valor entero y se encontr?: "+lexema;
                           poneError(3,err);
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        Simbolo simb=(Simbolo)tabSim.get(idKey);
                        if(simb!=null){
                           simb.d1=2;
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        String err="Se esperaba un valor entero y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaArreglos(0,tmp);
                     }
                  }
            }break;
            case 24:{
                  if(tipoElem==20)
                     estate=25;
                  else
                     if(tipoElem==4)
                        estate=26;
                     else{
                        String err="Se esperaba una coma o una llave de cierre y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaArreglos(0,tmp);
                     }
            }break;
            case 25:{
                  if(tipoElem==3){
                     estate=27;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     idKey="";
                     tipo="<Ninguno>";
                     d1=2;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==4){
                        estate=21;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        idKey="";
                        d1=2;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else
                        if(tipoElem==8)
                           estate=28;
                        else
                           if(tipoElem==13){
                              estate=20;
                              //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                              idKey="";
                              tipo="<Ninguno>";
                              d1=2;
                              //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                           }else{
                              String err="Se esperaba: \",\" ? \"]\" ? \";\" ? \"<-\" y se encontr?: "+lexema;
                              poneError(2,err);
                              
                              int tmp=((Integer)infoStates.peek()).intValue();
                              estate=mError.recuperaArreglos(2,tmp);
                           }
            }break;
            case 26:{/*
                  if(tipoElem==9)
                     estate=32;
                  else{
                     String err="Se esperaba un numero entero y se encontr?: "+lexema;
                     editor.errCount++;
                     editor.errores(estado,2,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaArreglos(17,tmp);
                  }*/
                  if(tipoElem==9){
                     estate=32;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa que este dentro del rango:
                     d2=Integer.parseInt(lexema);
                     if(d2<2){
                        d2=2;
                        String err="Los arreglos deben tener un tama?o minimo de 2: "+lexema;
                        poneError(3,err);
                     }else
                        if(d2>32768){
                           d2=32768;
                           String err="Los arreglos deben tener un tama?o maximo de 32768: "+lexema;
                           poneError(3,err);
                        }
                     Simbolo simb=(Simbolo)tabSim.get(idKey);
                     if(simb!=null){
                        simb.d2=d2;
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     if(tipoElem==7){
                        estate=32;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        //Revisa que sea una constante
                        Simbolo sim=(Simbolo)tabSim.get(lexema);
                        if(sim!=null){
                           if(sim.clase=='C'){
                              if(sim.tipo_dato.equals("<Entero>")){
                                 //Revisa que este dentro del rango:
                                 d2=Integer.parseInt(sim.cspcfyp);
                                 if(d2<2){
                                    d2=2;
                                    String err="Los arreglos deben tener un tama?o minimo de 2: "+lexema;
                                    poneError(3,err);
                                 }else
                                    if(d2>32768){
                                       d2=32768;
                                       String err="Los arreglos deben tener un tama?o maximo de 32768: "+lexema;
                                       poneError(3,err);
                                    }
                                 Simbolo simb=(Simbolo)tabSim.get(idKey);
                                 if(simb!=null){
                                    simb.d2=d2;
                                 }
                              }else{
                                 Simbolo simb=(Simbolo)tabSim.get(idKey);
                                 if(simb!=null){
                                    simb.d2=2;
                                 }
                                 String err="La constante que define el tama?o del arreglo debe tener valor entero: "+lexema;
                                 poneError(3,err);
                              }
                           }else{
                              Simbolo simb=(Simbolo)tabSim.get(idKey);
                              if(simb!=null){
                                 simb.d2=2;
                              }
                              String err="Se esperaba una constante con valor entero y se encontr?: "+lexema;
                              poneError(3,err);
                           }
                        }else{
                           Simbolo simb=(Simbolo)tabSim.get(idKey);
                           if(simb!=null){
                              simb.d2=2;
                           }
                           String err="Se esperaba un identificador constante existente con valor entero y se encontr?: "+lexema;
                           poneError(3,err);
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        Simbolo simb=(Simbolo)tabSim.get(idKey);
                        if(simb!=null){
                           simb.d2=2;
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        String err="Se esperaba un valor entero y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaArreglos(17,tmp);
                     }
                  }
            }break;
            case 27:{
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  if(nivel>0)
                     nivel--;
            }break;
            case 28:{
                  if(tipoElem==2)
                     estate=29;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaArreglos(2,tmp);
                  }
            }break;
            case 29:{
                  if(tipoElem==5||tipoElem==9||tipoElem==10||tipoElem==11){
                     estate=30;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     if(idKey!=""){
                        if(clase=='L')
                           valorArreglo(true,false);
                        else
                           valorArreglo(true,true);
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     if(tipoElem==7){
                        estate=30;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        if(clase=='L')
                           constArreglo(true,false);
                        else
                           constArreglo(true,true);
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        String err="Se esperaba un valor o una constante y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaArreglos(2,tmp);
                     }
                  }
            }break;
            case 30:{
                  if(tipoElem==3){
                     estate=31;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     Simbolo simb=(Simbolo)tabSim.get(idKey);
                     if(simb!=null){
                        if(d1c<simb.d1){
                           String err="El arreglo se defini? para contener "+simb.d1+" elementos y solo se encontraron "+d1c+": "+idKey;
                           poneError(3,err);
                        }
                        idKey="";
                     }
                     d1c=0;
                     d1=2;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==4)
                        estate=29;
                     else{
                        String err="Se esperaba una coma o un corchete de cierre y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaArreglos(2,tmp);
                     }
            }break;
            case 31:{
                  if(tipoElem==3){
                     estate=27;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     tipo="<Ninguno>";
                     d2=2;
                     d2c=0;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==4)
                        estate=21;
                     else
                        if(tipoElem==13){
                           estate=20;
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                           tipo="<Ninguno>";
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        }else{
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                           tipo="<Ninguno>";
                           d2=2;
                           d2c=0;
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                           String err="Se esperaba: \",\" ? \";\" ? \"]\" y se encontr?: "+lexema;
                           poneError(2,err);
                           
                           int tmp=((Integer)infoStates.peek()).intValue();
                           estate=mError.recuperaArreglos(2,tmp);
                        }
            }break;
            case 32:{
                  if(tipoElem==20)
                     estate=33;
                  else{
                     String err="Se esperaba una llave de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaArreglos(17,tmp);
                  }
            }break;
            case 33:{
                  if(tipoElem==3){
                     estate=27;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     idKey="";
                     tipo="<Ninguno>";
                     d1=d2=2;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==4){
                        estate=21;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        idKey="";
                        d1=d2=2;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else
                        if(tipoElem==8)
                           estate=34;
                        else
                           if(tipoElem==13){
                              estate=20;
                              //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                              idKey="";
                              tipo="<Ninguno>";
                              d1=d2=2;
                              //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                           }else{
                              //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                              idKey="";
                              tipo="<Ninguno>";
                              d1=d2=2;
                              //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                              String err="Se esperaba: \",\" ? \"]\" ? \";\" ? \"<-\" y se encontr?: "+lexema;
                              poneError(2,err);
                              
                              int tmp=((Integer)infoStates.peek()).intValue();
                              estate=mError.recuperaArreglos(17,tmp);
                           }
            }break;
            case 34:{
                  if(tipoElem==2)
                     estate=35;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaArreglos(17,tmp);
                  }
            }break;
            case 35:{
                  if(tipoElem==2){
                     estate=36;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     d1c++;
                     Simbolo simb=(Simbolo)tabSim.get(idKey);
                     if(simb!=null){
                        if(d1c==simb.d1+1){
                           String err="El arreglo se defini? para contener solo "+simb.d1+" subarreglos: "+lexema;
                           poneError(3,err);
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaArreglos(17,tmp);
                  }
            }break;
            case 36:{/*
                  if(tipoElem==5||tipoElem==9||tipoElem==10||tipoElem==11)
                     estate=37;
                  else{
                     String err="Se esperaba un valor y se encontr?: "+lexema;
                     editor.errCount++;
                     editor.errores(estado,2,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaArreglos(17,tmp);
                  }*/
                  if(tipoElem==5||tipoElem==9||tipoElem==10||tipoElem==11){
                     estate=37;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     if(clase=='L')
                        valorArreglo(false,false);
                     else
                        valorArreglo(false,true);
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     if(tipoElem==7){
                        estate=37;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        if(clase=='L')
                           constArreglo(false,false);
                        else
                           constArreglo(false,true);
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        String err="Se esperaba un valor o una constante y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaArreglos(17,tmp);
                     }
                  }
            }break;
            case 37:{
                  if(tipoElem==3){
                     estate=38;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     Simbolo simb=(Simbolo)tabSim.get(idKey);
                     if(simb!=null){
                        if(d2c<simb.d2){
                           String err="El subarreglo se defini? para contener "+simb.d2+" elementos y solo se encontraron "+d2c;
                           poneError(3,err);
                        }
                     }
                     d2c=0;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==4)
                        estate=36;
                     else{
                        String err="Se esperaba una coma o un corchete de cierre y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaArreglos(17,tmp);
                     }
            }break;
            case 38:{
                  if(tipoElem==3){
                     estate=39;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     Simbolo simb=(Simbolo)tabSim.get(idKey);
                     if(simb!=null){
                        if(d1c<simb.d1){
                           String err="El arreglo se defini? para contener "+simb.d1+" subarreglos y solo se encontraron "+d1c;
                           poneError(3,err);
                        }
                     }
                     idKey="";
                     d1c=d2c=0;
                     d1=d2=2;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==4)
                        estate=35;
                     else{
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        idKey="";
                        d1c=d2c=0;
                        d1=d2=2;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        String err="Se esperaba una coma o un corchete de cierre y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaArreglos(17,tmp);
                     }
            }break;
            case 39:{
                  if(tipoElem==3){
                     estate=27;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     tipo="<Ninguno>";
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==4)
                        estate=21;
                     else
                        if(tipoElem==13){
                           estate=20;
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                           tipo="<Ninguno>";
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        }else{
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                           tipo="<Ninguno>";
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                           String err="Se esperaba: \",\" ? \";\" ? \"]\" y se encontr?: "+lexema;
                           poneError(2,err);
                           
                           int tmp=((Integer)infoStates.peek()).intValue();
                           estate=mError.recuperaArreglos(17,tmp);
                        }
            }break;
            //----------------------------- Fin de ARREGLOS ----------------------------
            
            //-------------------------------- REGISTROS -------------------------------
            case 40:{
                  if(tipoElem==2)
                     estate=41;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaRegistros(0,tmp);
                  }
            }break;
            case 41:{
                  if(tipoElem==7){
                     estate=42;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa si se esta en global o en local:
                     int tmp=((Integer)infoStates.peek()).intValue();
                     if(tmp!=0&&tmp!=250){
                        //********************************************************************
                        //*************   Cuando se esta en registros locales:   *************
                        clase='L';
                        //Revisa que no este el simbolo en la tabla
                        if(modulo!=""){
                           idKey=lexema+"@"+modulo;
                           if(!tabSim.containsKey(idKey)){
                              //Busca que no exista otro registro con el mismo nombre
                              boolean esta=false;
                              for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                 String keyTmp=(String)e.nextElement();
                                 String posfijo="@"+modulo;
                                 if(keyTmp.endsWith(posfijo)){
                                    String prefijo=lexema+".";
                                    if(keyTmp.startsWith(prefijo))
                                       esta=true;
                                 }
                              }
                              if(!esta){
                                 //Revisa si no existe una constante o un modulo con ese nombre:
                                 String err;
                                 Simbolo sm=(Simbolo)tabSim.get(lexema);
                                 if(sm!=null){
                                    if(sm.clase=='C'){
                                       err="Ya existe una constante con ese nombre: "+lexema;
                                       poneError(3,err);
                                    }else if(sm.clase=='P'||sm.clase=='F'){
                                       err="Ya existe un modulo con ese nombre: "+lexema;
                                       poneError(3,err);
                                    }else
                                       idKey=lexema;
                                 }else
                                    idKey=lexema;
                              }else{
                                 //idKey="";
                                 String err="Ya est? definido un registro dentro de \""+modulo+"\" con ese nombre: "+lexema;
                                 poneError(3,err);
                              }
                           }else{
                              //idKey="";
                              String err="Ya est? definido un identificador dentro de \""+modulo+"\" con ese nombre: "+lexema;
                              poneError(3,err);
                           }
                           idKey=lexema;
                        }
                     }else{
                        //*********************************************************************
                        //*************   Cuando se esta en registros globales:   *************
                        clase='G';
                        //Revisa que no este el simbolo en la tabla
                        if(!tabSim.containsKey(lexema)){
                           //Busca que no exista otro registro con el mismo nombre
                           boolean esta=false;
                           for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                              String keyTmp=(String)e.nextElement();
                              String prefijo=lexema+".";
                              if(keyTmp.startsWith(prefijo))
                                 esta=true;
                           }
                           if(!esta)
                              idKey=lexema;
                           else{
                              idKey="";
                              String err="Ya est? definido un registro con ese nombre: "+lexema;
                              poneError(3,err);
                           }
                        }else{
                           idKey="";
                           String err="Ya est? definido un identificador con ese nombre: "+lexema;
                           poneError(3,err);
                        }
                        idKey=lexema;
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     idKey="";
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaRegistros(0,tmp);
                  }
            }break;
            case 42:{
                  if(tipoElem==2)
                     estate=43;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaRegistros(2,tmp);
                  }
            }break;
            case 43:{
                  if(tipoElem==14||tipoElem==15||tipoElem==16||tipoElem==17){
                     estate=44;
                     revisaTipo();
                  }else{
                     tipo="<Ninguno>";
                     String err="Se esperaba un tipo de dato encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaRegistros(2,tmp);
                  }
            }break;
            case 44:{
                  if(tipoElem==7){
                     estate=45;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     if(!idKey.equals("")){
                        //Agrega el simbolo en la tabla
                        String regElem=idKey+"."+lexema;
                        if(clase=='L')
                           regElem=regElem+"@"+modulo;
                        if(!tabSim.containsKey(regElem)){
                           tabSim.put(regElem,new Simbolo(regElem,clase,tipo,""));
                        }else{
                           String err="Ya un elemento del registro \""+idKey+"\" con ese nombre: "+lexema;
                           poneError(3,err);
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaRegistros(2,tmp);
                  }
            }break;
            case 45:{
                  if(tipoElem==3){
                     estate=46;
                     tipo="<Ninguno>";
                  }else
                     if(tipoElem==4)
                        estate=44;
                     else
                        if(tipoElem==13){
                           estate=43;
                           tipo="<Ninguno>";
                        }else{
                           tipo="<Ninguno>";
                           String err="Se esperaba: \",\" ? \";\" ? \"]\" y se encontr?: "+lexema;
                           poneError(2,err);
                           
                           int tmp=((Integer)infoStates.peek()).intValue();
                           estate=mError.recuperaRegistros(2,tmp);
                        }
            }break;
            case 46:{
                  if(tipoElem==3)
                     estate=52;
                  else
                     if(tipoElem==19)
                        estate=47;
                     else
                        if(tipoElem==13){
                           estate=41;
                           idKey="";
                           tipo="<Ninguno>";
                        }else{
                           idKey="";
                           tipo="<Ninguno>";
                           String err="Se esperaba: \";\" ? \"{\" ? \"]\" y se encontr?: "+lexema;
                           poneError(2,err);
                           
                           int tmp=((Integer)infoStates.peek()).intValue();
                           estate=mError.recuperaRegistros(7,tmp);
                        }
            }break;
            case 47:{/*
                  if(tipoElem==9)
                     estate=48;
                  else{
                     String err="Se esperaba un numero entero y se encontr?: "+lexema;
                     editor.errCount++;
                     editor.errores(estado,2,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaRegistros(7,tmp);
                  }*/
                  if(tipoElem==9){
                     estate=48;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa que este dentro del rango:
                     d1=Integer.parseInt(lexema);
                     if(d1<2){
                        d1=2;
                        String err="Los arreglos deben tener un tama?o minimo de 2: "+lexema;
                        poneError(3,err);
                     }else
                        if(d1>32768){
                           d1=32768;
                           String err="Los arreglos deben tener un tama?o maximo de 32768: "+lexema;
                           poneError(3,err);
                        }
                     /*Simbolo simb=(Simbolo)tabSim.get(idKey);
                     if(simb!=null){
                        simb.d1=d1;
                     }*/
                     if(clase=='L')
                        dimRegistroL(idKey,d1,false);
                     else
                        dimRegistro(idKey,d1,false);
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     if(tipoElem==7){
                        estate=48;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        //Revisa que sea una constante
                        Simbolo sim=(Simbolo)tabSim.get(lexema);
                        if(sim!=null){
                           if(sim.clase=='C'){
                              if(sim.tipo_dato.equals("<Entero>")){
                                 //Revisa que este dentro del rango:
                                 d1=Integer.parseInt(sim.cspcfyp);
                                 if(d1<2){
                                    d1=2;
                                    String err="Los arreglos deben tener un tama?o minimo de 2: "+lexema;
                                    poneError(3,err);
                                 }else
                                    if(d1>32768){
                                       d1=32768;
                                       String err="Los arreglos deben tener un tama?o maximo de 32768: "+lexema;
                                       poneError(3,err);
                                    }
                                 /*Simbolo simb=(Simbolo)tabSim.get(idKey);
                                 if(simb!=null){
                                    simb.d1=d1;
                                 }*/
                                 if(clase=='L')
                                    dimRegistroL(idKey,d1,false);
                                 else
                                    dimRegistro(idKey,d1,false);
                              }else{
                                 /*Simbolo simb=(Simbolo)tabSim.get(idKey);
                                 if(simb!=null){
                                    simb.d1=2;
                                 }*/
                                 if(clase=='L')
                                    dimRegistroL(idKey,2,false);
                                 else
                                    dimRegistro(idKey,2,false);
                                 String err="La constante que define el tama?o del arreglo debe tener valor entero: "+lexema;
                                 poneError(3,err);
                              }
                           }else{/*
                              Simbolo simb=(Simbolo)tabSim.get(idKey);
                              if(simb!=null){
                                 simb.d1=2;
                              }*/
                              if(clase=='L')
                                 dimRegistroL(idKey,2,false);
                              else
                                 dimRegistro(idKey,2,false);
                              String err="Se esperaba una constante con valor entero y se encontr?: "+lexema;
                              poneError(3,err);
                           }
                        }else{/*
                           Simbolo simb=(Simbolo)tabSim.get(idKey);
                           if(simb!=null){
                              simb.d1=2;
                           }*/
                           if(clase=='L')
                              dimRegistroL(idKey,2,false);
                           else
                              dimRegistro(idKey,2,false);
                           String err="Se esperaba un identificador constante existente con valor entero y se encontr?: "+lexema;
                           poneError(3,err);
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{/*
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        Simbolo simb=(Simbolo)tabSim.get(idKey);
                        if(simb!=null){
                           simb.d1=2;
                        }*/
                        if(clase=='L')
                           dimRegistroL(idKey,2,false);
                        else
                           dimRegistro(idKey,2,false);
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        String err="Se esperaba un valor entero y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaRegistros(7,tmp);
                     }
                  }
            }break;
            case 48:{
                  if(tipoElem==20)
                     estate=51;
                  else
                     if(tipoElem==4)
                        estate=49;
                     else{
                        String err="Se esperaba una coma o una llave de cierre y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaRegistros(11,tmp);
                     }
            }break;
            case 49:{/*
                  if(tipoElem==9)
                     estate=50;
                  else{
                     String err="Se esperaba un numero entero y se encontr?: "+lexema;
                     editor.errCount++;
                     editor.errores(estado,2,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaRegistros(11,tmp);
                  }*/
                  if(tipoElem==9){
                     estate=50;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa que este dentro del rango:
                     d2=Integer.parseInt(lexema);
                     if(d2<2){
                        d2=2;
                        String err="Los arreglos deben tener un tama?o minimo de 2: "+lexema;
                        poneError(3,err);
                     }else
                        if(d2>32768){
                           d2=32768;
                           String err="Los arreglos deben tener un tama?o maximo de 32768: "+lexema;
                           poneError(3,err);
                        }
                     /*Simbolo simb=(Simbolo)tabSim.get(idKey);
                     if(simb!=null){
                        simb.d2=d2;
                     }*/
                     if(clase=='L')
                        dimRegistroL(idKey,d2,true);
                     else
                        dimRegistro(idKey,d2,true);
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     if(tipoElem==7){
                        estate=50;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        //Revisa que sea una constante
                        Simbolo sim=(Simbolo)tabSim.get(lexema);
                        if(sim!=null){
                           if(sim.clase=='C'){
                              if(sim.tipo_dato.equals("<Entero>")){
                                 //Revisa que este dentro del rango:
                                 d2=Integer.parseInt(sim.cspcfyp);
                                 if(d2<2){
                                    d2=2;
                                    String err="Los arreglos deben tener un tama?o minimo de 2: "+lexema;
                                    poneError(3,err);
                                 }else
                                    if(d2>32768){
                                       d2=32768;
                                       String err="Los arreglos deben tener un tama?o maximo de 32768: "+lexema;
                                       poneError(3,err);
                                    }
                                 /*Simbolo simb=(Simbolo)tabSim.get(idKey);
                                 if(simb!=null){
                                    simb.d2=d2;
                                 }*/
                                 if(clase=='L')
                                    dimRegistroL(idKey,d2,true);
                                 else
                                    dimRegistro(idKey,d2,true);
                              }else{/*
                                 Simbolo simb=(Simbolo)tabSim.get(idKey);
                                 if(simb!=null){
                                    simb.d2=2;
                                 }*/
                                 if(clase=='L')
                                    dimRegistroL(idKey,2,true);
                                 else
                                    dimRegistro(idKey,2,true);
                                 String err="La constante que define el tama?o del arreglo debe tener valor entero: "+lexema;
                                 poneError(3,err);
                              }
                           }else{/*
                              Simbolo simb=(Simbolo)tabSim.get(idKey);
                              if(simb!=null){
                                 simb.d2=2;
                              }*/
                              if(clase=='L')
                                 dimRegistroL(idKey,2,true);
                              else
                                 dimRegistro(idKey,2,true);
                              String err="Se esperaba una constante con valor entero y se encontr?: "+lexema;
                              poneError(3,err);
                           }
                        }else{/*
                           Simbolo simb=(Simbolo)tabSim.get(idKey);
                           if(simb!=null){
                              simb.d2=2;
                           }*/
                           if(clase=='L')
                              dimRegistroL(idKey,2,true);
                           else
                              dimRegistro(idKey,2,true);
                           String err="Se esperaba un identificador constante existente con valor entero y se encontr?: "+lexema;
                           poneError(3,err);
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{/*
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        Simbolo simb=(Simbolo)tabSim.get(idKey);
                        if(simb!=null){
                           simb.d2=2;
                        }*/
                        if(clase=='L')
                           dimRegistroL(idKey,2,true);
                        else
                           dimRegistro(idKey,2,true);
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        String err="Se esperaba un valor entero y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaRegistros(11,tmp);
                     }
                  }
            }break;
            case 50:{
                  if(tipoElem==20)
                     estate=51;
                  else{
                     String err="Se esperaba un corchete de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaRegistros(1,tmp);
                  }
            }break;
            case 51:{
                  if(tipoElem==3)
                     estate=52;
                  else
                     if(tipoElem==13){
                        estate=41;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        idKey="";
                        tipo="<Ninguno>";
                        d1=d2=2;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        idKey="";
                        tipo="<Ninguno>";
                        d1=d2=2;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        String err="Se esperaba un punto y coma o un corchete de cierre y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaRegistros(1,tmp);
                     }
            }break;
            case 52:{
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  if(nivel>0)
                     nivel--;
            }break;
            //---------------------------- Fin de REGISTROS ----------------------------
            
            //------------------------------- PROTOTIPOS -------------------------------
            case 53:{
                  ProtOMod=1;
                  if(tipoElem==2)
                     estate=54;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaPrototipos(tmp);
                  }
            }break;
            case 54:{
                  if(lexema.equals("PROCEDIMIENTO")){
                     infoStates.push(new Integer(55));
                     padresState.push(new Integer(55));
                     //EncabezadoP
                     estate=63;
                     nivel++;
                     proceder=true;
                  }else
                     if(lexema.equals("FUNCION")){
                        infoStates.push(new Integer(55));
                        padresState.push(new Integer(55));
                        //EncabezadoF
                        estate=67;
                        nivel++;
                        proceder=true;
                     }else{
                        proceder=false;
                        String err="Se esperaba \"PROCEDIMIENTO\" o \"FUNCION\" y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaPrototipos(tmp);
                     }
            }break;
            case 55:{
                  if(tipoElem==3)
                     estate=56;
                  else
                     if(tipoElem==13)
                        estate=54;
                     else{
                        String err="Se esperaba un corchete de cierre o un punto y coma y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaPrototipos(tmp);
                     }
            }break;
            case 56:{
                  ProtOMod=0;
                  if(tipoElem==22){
                     estate=57;
                     nivel=1;
                     modulosBan=true;
                  }else{
                     String err="Despues de los prototipos deben estar los Modulos.";
                     poneError(2,err);
                     /*estate=0;
                     reuse=true;
                     */
                     int next=((Integer)infoStates.pop()).intValue();
                     padresState.pop();
                     estate=next;
                     reuse=true;
                     if(nivel>0)
                        nivel--;
                  }
            }break;
            //---------------------------- Fin de PROTOTIPOS ---------------------------
            
            //--------------------------------- MODULOS --------------------------------
            case 57:{
                  ProtOMod=2;
                  if(tipoElem==2)
                     estate=58;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaModulos(tmp);
                  }
            }break;
            case 58:{
                  hayReturn=false;
                  if(lexema.equals("PROCEDIMIENTO")){
                     infoStates.push(new Integer(59));
                     padresState.push(new Integer(59));
                     //EncabezadoP
                     estate=63;
                     nivel++;
                     proceder=true;
                  }else
                     if(lexema.equals("FUNCION")){
                        infoStates.push(new Integer(59));
                        padresState.push(new Integer(59));
                        //EncabezadoF
                        estate=67;
                        nivel++;
                        proceder=true;
                     }else{
                        proceder=false;
                        String err="Se esperaba \"PROCEDIMIENTO\" o \"FUNCION\" y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaModulos(tmp);
                     }
            }break;
            case 59:{
                  //Generacion de codigo:
                  codGen.descargaPilaParams();
                  
                  nivel++;
                  //Cuerpo
                  infoStates.push(new Integer(60));
                  padresState.push(new Integer(59));
                  estate=80;
                  reuse=true;
            }break;
            case 60:{
                  //Generacion de codigo:
                  codGen.escribePL0("OPR  0,1");
                  
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Revisa que el modulo haya sido una funcion y que se haya encontrado un REGRESAR
                  Simbolo modSim=(Simbolo)tabSim.get(modulo);
                  if(modSim!=null){
                     if(modSim.clase=='F'){
                        if(!hayReturn){
                           String err="No se encontr? un \"REGRESAR\" dentro de esta Funci?n: \""+modulo+"\"";
                           poneError2(3,err);
                        }
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  modulo="";
                  tieneProto=false;
                  if(tipoElem==3){
                     estate=61;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa que ya se hayan definido todos los prototipos:
                     ArrayList prots=new ArrayList(tabProtos.values());
                     for(int a=0;a<prots.size();a++){
                        Proto pt=(Proto)prots.get(a);
                        if(!pt.esta){
                           pt.esta=true;
                           String err="No se defini? el modulo de este prototipo: "+pt.nombre;
                           editor.errCount++;
                           editor.errores(estado,3,err,pt.linea,pt.endPos,pt.nombre,win,win.nombre,win.ruta);
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==13)
                        estate=58;
                     else{
                        String err="Se esperaba un corchete de cierre o un punto y coma y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaModulos(tmp);
                     }
            }break;
            case 61:{
                  ProtOMod=0;
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  if(nivel>0)
                     nivel--;
            }break;
            //------------------------------ Fin de MODULOS ----------------------------
            
            //-------------------------------- PRINCIPAL -------------------------------
            case 62:{
                  //*** Generacion de codigo ***
                  //Resuelve la etiqueta P:
                  Simbolo etP=new Simbolo("$LabelP",'N',"<Ninguno>","");
                  etP.d1=codGen.contador+1;
                  tabSim.put("$LabelP",etP);
                  //Pone el codigo de las inicializaciones globales:
                  codGen.poneInic();
                  //Genera y resuelve a $PRINCIPAL:
                  Simbolo PrinSim=new Simbolo("$PRINCIPAL",'N',"<Ninguno>","");
                  PrinSim.d1=codGen.contador+1;
                  tabSim.put("$PRINCIPAL",PrinSim);
                  // Fin de generacion de codigo
                  
                  modulo="PRINCIPAL";
                  etapa=7;
                  //Cuerpo
                  infoStates.push(new Integer(0));
                  padresState.push(new Integer(0));
                  estate=80;
                  reuse=true;
            }break;
            //----------------------------- Fin de PRINCIPAL ---------------------------
            
            //------------------------------- EncabezadoP ------------------------------
            case 211:{
                  if(lexema.equals("PROCEDIMIENTO")){
                     estate=63;
                  }else{
                     System.out.println(" *** Error: Elemento inesperado en la recuperacion de Encabezado *** ");
                     estate=63;
                  }
            }break;
            case 63:{
                  if(tipoElem==7){
                     estate=64;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     if(proceder){
                        proceder=false;
                        if(ProtOMod==1){
                           if(!tabProtos.containsKey(lexema)){
                              String et="$Label"+codGen.etCont;
                              codGen.etCont++;
                              tabProtos.put(lexema,new Proto(lexema,'P',et,i,lineCounter));
                              idKey=lexema;
                              //Revisa que no este el simbolo en la tabla
                              if(!tabSim.containsKey(lexema)){
                                 //Busca que no exista un registro con el mismo nombre
                                 boolean esta=false;
                                 for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                    String keyTmp=(String)e.nextElement();
                                    String prefijo=lexema+".";
                                    if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1)
                                       esta=true;
                                 }
                                 if(!esta){
                                    idKey=lexema;
                                    modulo=lexema;
                                    Simbolo simt=new Simbolo(lexema,'P',"<Ninguno>","");
                                    simt.d1=-1;
                                    tabSim.put(lexema,simt);
                                 }else{
                                    //Error, se resetea el analisis semantico
                                    idKey=modulo="";
                                    String err="Ya est? definido un identificador con ese nombre: "+lexema;
                                    poneError(3,err);
                                 }
                              }else{
                                 //Error, se resetea el analisis semantico y se indica si era un
                                 //modulo o un identificador
                                 idKey=modulo="";
                                 String claseErr;
                                 Simbolo smb=(Simbolo)tabSim.get(lexema);
                                 if(smb!=null){
                                    if(smb.clase=='P'||smb.clase=='F')
                                       claseErr="modulo";
                                    else
                                       claseErr="identificador";
                                 }else
                                    claseErr="identificador";
                                 
                                 String err="Ya est? definido un "+claseErr+" con ese nombre: "+lexema;
                                 poneError(3,err);
                              }
                           }else{
                              idKey="";
                              String err="Ya est? definido un prototipo con ese nombre: "+lexema;
                              poneError(3,err);
                           }
                        }else if(ProtOMod==2){
                           //Busca un prototipo con ese nombre
                           if(tabProtos.containsKey(lexema)){
                              Proto pt=(Proto)tabProtos.get(lexema);
                              if(!pt.esta){
                                 pt.esta=true;
                                 tieneProto=true;
                              }
                              idKey=lexema;
                              modulo=lexema;
                              //Quita la bandera de prototipo al simbolo:
                              Simbolo smb=(Simbolo)tabSim.get(lexema);
                              smb.d1=0;
                              if(modulos.contains(lexema)){
                                 String err="Ya est? definido un modulo con ese nombre: "+lexema;
                                 poneError(3,err);
                              }else
                                 modulos.add(lexema);
                              
                              //Resuelve la etiqueta:
                              Simbolo etX=new Simbolo(pt.dir,'N',"<Ninguno>","");
                              etX.d1=codGen.contador+1;
                              tabSim.put(pt.dir,etX);
                           }else{
                              idKey=lexema;
                              modulo=lexema;
                              tieneProto=false;
                              
                              //Mete el modulo en la tabla de prototipos:
                              String d=Integer.toString(codGen.contador+1);
                              Proto mod=new Proto(lexema,'P',d,i,lineCounter);
                              mod.esta=true;
                              tabProtos.put(lexema,mod);
                              
                              //Revisa que no est? el simbolo en la tabla
                              idKey=lexema;
                              if(!tabSim.containsKey(lexema)){
                                 //Busca que no exista un registro con el mismo nombre
                                 boolean esta=false;
                                 for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                    String keyTmp=(String)e.nextElement();
                                    String prefijo=lexema+".";
                                    if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1)
                                       esta=true;
                                 }
                                 if(!esta){
                                    idKey=lexema;
                                    modulo=lexema;
                                    tabSim.put(lexema,new Simbolo(lexema,'P',"<Ninguno>",""));
                                 }else{
                                    //idKey=modulo="";
                                    String err="Ya est? definido un identificador con ese nombre: "+lexema;
                                    poneError(3,err);
                                 }
                              }else{
                                 //idKey=modulo="";
                                 Simbolo smb=(Simbolo)tabSim.get(lexema);
                                 //Revisa que no sea un m?dulo fantasma (prototipo de otro archivo que nunca se defini?)
                                 if(smb.d1==-1){
                                    tabSim.remove(lexema);
                                    idKey=lexema;
                                    modulo=lexema;
                                    tabSim.put(lexema,new Simbolo(lexema,'P',"<Ninguno>",""));
                                 }else{
                                    String claseErr;
                                    if(smb.clase=='P'||smb.clase=='F')
                                       claseErr="modulo";
                                    else
                                       claseErr="identificador";
                                    
                                    String err="Ya est? definido un "+claseErr+" con ese nombre: "+lexema;
                                    poneError(3,err);
                                 }
                              }
                           }
                        }
                     }else
                        idKey=modulo="";
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     idKey="";
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaEncabezadoP(tmp);
                  }
            }break;
            case 64:{
                  if(tipoElem==19)
                     estate=65;
                  else{
                     idKey="";
                     String err="Se esperaba una llave de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaEncabezadoP(tmp);
                  }
            }break;
            case 65:{
                  if(tipoElem==20){
                     int next=((Integer)infoStates.pop()).intValue();
                     padresState.pop();
                     estate=next;
                     nivel--;
                     idKey="";
                  }else
                     if(tipoElem==14||tipoElem==15||tipoElem==16||tipoElem==17){
                        infoStates.push(new Integer(66));
                        padresState.push(new Integer(66));
                        //Parametros
                        estate=72;
                        nivel++;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        revisaTipo();
                        argC[0]=0;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        idKey="";
                        String err="Se esperaba un tipo de dato o una llave de cierre y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaEncabezadoP(tmp);
                     }
            }break;
            case 66:{
                  if(tipoElem==20){
                     int next=((Integer)infoStates.pop()).intValue();
                     padresState.pop();
                     estate=next;
                     nivel--;
                     idKey="";
                  }else{
                     idKey="";
                     String err="Se esperaba una llave de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaEncabezadoP(tmp);
                  }
            }break;
            //---------------------------- Fin de EncabezadoP --------------------------
            
            //------------------------------- EncabezadoF ------------------------------
            case 212:{
                  if(lexema.equals("FUNCION")){
                     estate=67;
                  }else{
                     System.out.println(" *** Error: Elemento inesperado en la recuperacion de Encabezado *** ");
                     estate=67;
                  }
            }break;
            case 67:{
                  if(tipoElem==7){
                     estate=68;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     if(proceder){
                        proceder=false;
                        if(ProtOMod==1){
                           if(!tabProtos.containsKey(lexema)){
                              String et="$Label"+codGen.etCont;
                              codGen.etCont++;
                              tabProtos.put(lexema,new Proto(lexema,'F',et,i,lineCounter));
                              idKey=lexema;
                              //Revisa que no este el simbolo en la tabla
                              if(!tabSim.containsKey(lexema)){
                                 //Busca que no exista un registro con el mismo nombre
                                 boolean esta=false;
                                 for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                    String keyTmp=(String)e.nextElement();
                                    String prefijo=lexema+".";
                                    if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1)
                                       esta=true;
                                 }
                                 if(!esta){
                                    idKey=lexema;
                                    modulo=lexema;
                                    Simbolo simt=new Simbolo(lexema,'F',"<Ninguno>","");
                                    simt.d1=-1;
                                    tabSim.put(lexema,simt);
                                 }else{
                                    //Error, se resetea el analisis semantico
                                    idKey=modulo="";
                                    String err="Ya est? definido un identificador con ese nombre: "+lexema;
                                    poneError(3,err);
                                 }
                              }else{
                                 //Error, se resetea el analisis semantico y se indica si era un
                                 //modulo o un identificador
                                 idKey=modulo="";
                                 String claseErr;
                                 Simbolo smb=(Simbolo)tabSim.get(lexema);
                                 if(smb!=null){
                                    if(smb.clase=='P'||smb.clase=='F')
                                       claseErr="modulo";
                                    else
                                       claseErr="identificador";
                                 }else
                                    claseErr="identificador";
                                 
                                 String err="Ya est? definido un "+claseErr+" con ese nombre: "+lexema;
                                 poneError(3,err);
                              }
                           }else{
                              idKey="";
                              String err="Ya est? definido un prototipo con ese nombre: "+lexema;
                              poneError(3,err);
                           }
                        }else if(ProtOMod==2){
                           //Busca un prototipo con ese nombre
                           if(tabProtos.containsKey(lexema)){
                              Proto pt=(Proto)tabProtos.get(lexema);
                              if(!pt.esta){
                                 pt.esta=true;
                                 tieneProto=true;
                              }
                              idKey=lexema;
                              modulo=lexema;
                              //Quita la bandera de prototipo al simbolo:
                              Simbolo smb=(Simbolo)tabSim.get(lexema);
                              smb.d1=0;
                              if(modulos.contains(lexema)){
                                 String err="Ya est? definido un modulo con ese nombre: "+lexema;
                                 poneError(3,err);
                              }else
                                 modulos.add(lexema);
                              
                              //Resuelve la etiqueta:
                              Simbolo etX=new Simbolo(pt.dir,'N',"<Ninguno>","");
                              etX.d1=codGen.contador+1;
                              tabSim.put(pt.dir,etX);
                           }else{
                              idKey=lexema;
                              modulo=lexema;
                              tieneProto=false;
                              
                              //Mete el modulo en la tabla de prototipos:
                              String d=Integer.toString(codGen.contador+1);
                              Proto mod=new Proto(lexema,'F',d,i,lineCounter);
                              mod.esta=true;
                              tabProtos.put(lexema,mod);
                              
                              //Revisa que no este el simbolo en la tabla
                              idKey=lexema;
                              if(!tabSim.containsKey(lexema)){
                                 //Busca que no exista un registro con el mismo nombre
                                 boolean esta=false;
                                 for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                    String keyTmp=(String)e.nextElement();
                                    String prefijo=lexema+".";
                                    if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1)
                                       esta=true;
                                 }
                                 if(!esta){
                                    idKey=lexema;
                                    modulo=lexema;
                                    tabSim.put(lexema,new Simbolo(lexema,'F',"<Ninguno>",""));
                                 }else{
                                    //idKey=modulo="";
                                    String err="Ya est? definido un identificador con ese nombre: "+lexema;
                                    poneError(3,err);
                                 }
                              }else{
                                 //idKey=modulo="";
                                 Simbolo smb=(Simbolo)tabSim.get(lexema);
                                 //Revisa que no sea un m?dulo fantasma (prototipo de otro archivo que nunca se defini?)
                                 if(smb.d1==-1){
                                    tabSim.remove(lexema);
                                    idKey=lexema;
                                    modulo=lexema;
                                    tabSim.put(lexema,new Simbolo(lexema,'F',"<Ninguno>",""));
                                 }else{
                                    String claseErr;
                                    if(smb.clase=='P'||smb.clase=='F')
                                       claseErr="modulo";
                                    else
                                       claseErr="identificador";
                                    
                                    String err="Ya est? definido un "+claseErr+" con ese nombre: "+lexema;
                                    poneError(3,err);
                                 }
                              }
                           }
                        }
                     }else
                        idKey=modulo="";
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     idKey="";
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaEncabezadoF(tmp,0);
                  }
            }break;
            case 68:{
                  if(tipoElem==19)
                     estate=69;
                  else{
                     idKey="";
                     String err="Se esperaba una llave de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaEncabezadoF(tmp,0);
                  }
            }break;
            case 69:{
                  if(tipoElem==20){
                     estate=71;
                  }else
                     if(tipoElem==14||tipoElem==15||tipoElem==16||tipoElem==17){
                        infoStates.push(new Integer(70));
                        padresState.push(new Integer(70));
                        //Parametros
                        estate=72;
                        nivel++;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        revisaTipo();
                        argC[0]=0;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        idKey="";
                        String err="Se esperaba un tipo de dato o una llave de cierre y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaEncabezadoF(tmp,0);
                     }
            }break;
            case 70:{
                  if(tipoElem==20){
                     estate=71;
                  }else{
                     idKey="";
                     String err="Se esperaba una llave de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaEncabezadoF(tmp,4);
                  }
            }break;
            case 71:{
                  if(tipoElem==14||tipoElem==15||tipoElem==16||tipoElem==17){
                     int next=((Integer)infoStates.pop()).intValue();
                     padresState.pop();
                     estate=next;
                     nivel--;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     if(modulo!=""){
                        revisaTipo();
                        Proto pt=(Proto)tabProtos.get(modulo);
                        if(ProtOMod==1){
                           pt.tipo_dato=tipo;
                        }else{
                           if(tieneProto){
                              if(!pt.tipo_dato.equals(tipo)){
                                 String err="El prototipo de esta funci?n se declaro como "+pt.tipo_dato+" y se encontr? el tipo: "+tipo;
                                 poneError(3,err);
                              }
                           }
                        }
                        Simbolo sim=(Simbolo)tabSim.get(modulo);
                        if(sim!=null)
                           sim.tipo_dato=tipo;
                        idKey="";
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     if(idKey!=""){
                        if(ProtOMod==1){
                           Proto pt=(Proto)tabProtos.get(modulo);
                           pt.tipo_dato="<Ninguno>";
                        }
                        idKey="";
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     String err="Se esperaba un tipo de dato y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaEncabezadoF(tmp,4);
                  }
            }break;
            //---------------------------- Fin de EncabezadoF --------------------------
            
            //-------------------------------- Parametros ------------------------------
            case 72:{
                  if(tipoElem==7){
                     estate=73;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     if(idKey!=""&&modulo!=""){
                        if(ProtOMod==1){
                           Proto pt=(Proto)tabProtos.get(modulo);
                           if(pt.parametros.contains(lexema)){
                              idKey="";
                              String err="Ya existe un argumento con ese nombre: "+lexema;
                              poneError(3,err);
                           }else{
                              //Agrega el parametro en la tabla de simbolos:
                              //Revisa si no existe una constante o un modulo con ese nombre:
                              Simbolo sm=(Simbolo)tabSim.get(lexema);
                              boolean error=false;
                              if(sm!=null){
                                 if(sm.clase=='C'){
                                    error=true;
                                    String err="Ya existe una constante con ese nombre: "+lexema;
                                    poneError(3,err);
                                 }else if(sm.clase=='P'||sm.clase=='F'){
                                    error=true;
                                    String err="Ya existe un modulo con ese nombre: "+lexema;
                                    poneError(3,err);
                                 }
                              }
                              if(!error){
                                 String param=lexema+"@"+modulo;
                                 tabSim.put(param,new Simbolo(param,'A',tipo,""));
                                 idKey=param;
                              }
                           }
                           pt.parametros.add(lexema);
                        }else if(ProtOMod==2){
                           if(tieneProto){
                              Proto pt=(Proto)tabProtos.get(modulo);
                              if(argC[0]==pt.parametros.size()){
                                 String err="El numero de parametros excede al de los parametros definidos en el prototipo.";
                                 poneError(3,err);
                                 
                                 revisaParam();
                              }else
                                 if(argC[0]<pt.parametros.size()&&!pt.parametros.get(argC[0]).equals(lexema)){
                                    String err="El argumento no concuerda con lo definido en el prototipo de este modulo: "+modulo;
                                    poneError(3,err);
                                    
                                    revisaParam();
                                 }else{
                                    String param=lexema+"@"+modulo;
                                    idKey=param;
                                 }
                              argC[0]++;
                           }else{
                              revisaParam();
                           }
                        }
                        cspcfyp=cspcfyp+letraTipo();
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaParametros(tmp,0);
                  }
            }break;
            case 73:{
                  //*** Generacion de codigo ***
                  if(tipoElem!=19&&ProtOMod==2){
                     //Mete el parametro en la pila de parametros:
                     idAsign=idKey;
                     int cPos=idAsign.indexOf('@');
                     if(cPos!=-1)
                        idAsign=idAsign.substring(0,cPos);
                     codGen.pilaParams.push(idAsign);
                  }
                  // Fin de generacion de codigo
                  
                  if(tipoElem==4){
                     estate=72;
                     cspcfyp=cspcfyp+"|";
                  }else
                     if(tipoElem==13){
                        estate=79;
                        cspcfyp=cspcfyp+"|";
                     }else
                        if(tipoElem==19){
                           estate=74;
                           cspcfyp=cspcfyp+",";
                        }else{
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                           if(idKey!=""&&!modulo.equals("")){
                              if(ProtOMod==1){
                                 Proto pt=(Proto)tabProtos.get(modulo);
                                 pt.cspcfyp=cspcfyp;
                              }else if(ProtOMod==2){
                                 //Revisa que no se hayan definido menos parametros que en su prototipo:
                                 if(tieneProto){
                                    Proto pt=(Proto)tabProtos.get(modulo);
                                    if(pt.parametros.size()>argC[0]){
                                       String err="El numero de parametros es menor al de los definidos en el prototipo.";
                                       editor.errCount++;
                                       editor.errores(estado,3,err,lineCounter,i,"",win,win.nombre,win.ruta);
                                    }
                                 }
                              }
                              Simbolo sm=(Simbolo)tabSim.get(modulo);
                              if(sm!=null)
                                 sm.cspcfyp=cspcfyp;
                           }
                           d1=d2=2;
                           tipo="<Ninguno>";
                           cspcfyp="";
                           argC[0]=0;
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                           int next=((Integer)infoStates.pop()).intValue();
                           padresState.pop();
                           estate=next;
                           reuse=true;
                           nivel--;
                        }
            }break;
            case 74:{/*
                  if(tipoElem==9)
                     estate=75;
                  else{
                     String err="Se esperaba un numero entero y se encontr?: "+lexema;
                     editor.errCount++;
                     editor.errores(estado,2,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaParametros(tmp,0);
                  }*/
                  if(tipoElem==9){
                     estate=75;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa que este dentro del rango:
                     d1=Integer.parseInt(lexema);
                     if(d1<2){
                        d1=2;
                        String err="Los arreglos deben tener un tama?o minimo de 2: "+lexema;
                        poneError(3,err);
                     }else
                        if(d1>32768){
                           d1=32768;
                           String err="Los arreglos deben tener un tama?o maximo de 32768: "+lexema;
                           poneError(3,err);
                        }
                     
                     cspcfyp=cspcfyp+d1;
                     //Pone el tama?o del arreglo en el argumento:
                     Simbolo sim=(Simbolo)tabSim.get(idKey);
                     if(sim!=null){
                        sim.d1=d1;
                     }/*
                     if(ProtOMod==2&&modulo!=""){
                        if(tieneProto){
                           Proto pt=(Proto)tabProtos.get(modulo);
                           if(!pt.cspcfyp.startsWith(cspcfyp)){
                              String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                              poneError(3,err);
                           }
                        }
                     }*/
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     if(tipoElem==7){
                        estate=75;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        //Revisa que sea una constante
                        Simbolo sim=(Simbolo)tabSim.get(lexema);
                        if(sim!=null){
                           if(sim.clase=='C'){
                              if(sim.tipo_dato.equals("<Entero>")){
                                 //Revisa que este dentro del rango:
                                 d1=Integer.parseInt(sim.cspcfyp);
                                 if(d1<2){
                                    d1=2;
                                    String err="Los arreglos deben tener un tama?o minimo de 2: "+lexema;
                                    poneError(3,err);
                                 }else
                                    if(d1>32768){
                                       d1=32768;
                                       String err="Los arreglos deben tener un tama?o maximo de 32768: "+lexema;
                                       poneError(3,err);
                                    }
                                 
                                 cspcfyp=cspcfyp+d1;
                                 //Pone la dimension en el argumento:
                                 Simbolo sm=(Simbolo)tabSim.get(idKey);
                                 if(sm!=null){
                                    sm.d1=d1;
                                 }/*
                                 if(ProtOMod==2&&modulo!=""){
                                    if(tieneProto){
                                       Proto pt=(Proto)tabProtos.get(modulo);
                                       if(!pt.cspcfyp.startsWith(cspcfyp)){
                                          String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                                          poneError(3,err);
                                       }
                                    }
                                 }*/
                              }else{
                                 cspcfyp=cspcfyp+"2";
                                 //Pone la dimension en el argumento:
                                 Simbolo sm=(Simbolo)tabSim.get(idKey);
                                 if(sm!=null){
                                    sm.d1=2;
                                 }/*
                                 if(ProtOMod==2&&modulo!=""){
                                    if(tieneProto){
                                       Proto pt=(Proto)tabProtos.get(modulo);
                                       if(!pt.cspcfyp.startsWith(cspcfyp)){
                                          String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                                          poneError(3,err);
                                       }
                                    }
                                 }
                                 */
                                 String err="La constante que define el tama?o del arreglo debe tener valor entero: "+lexema;
                                 poneError(3,err);
                              }
                           }else{
                              cspcfyp=cspcfyp+"2";
                              //Pone la dimension en el argumento:
                              Simbolo sm=(Simbolo)tabSim.get(idKey);
                              if(sm!=null){
                                 sm.d1=2;
                              }/*
                              if(ProtOMod==2&&modulo!=""){
                                 if(tieneProto){
                                    Proto pt=(Proto)tabProtos.get(modulo);
                                    if(!pt.cspcfyp.startsWith(cspcfyp)){
                                       String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                                       poneError(3,err);
                                    }
                                 }
                              }*/
                              String err="Se esperaba una constante con valor entero y se encontr?: "+lexema;
                              poneError(3,err);
                           }
                        }else{
                           cspcfyp=cspcfyp+"2";
                           //Pone la dimension en el argumento:
                           Simbolo sm=(Simbolo)tabSim.get(idKey);
                           if(sm!=null){
                              sm.d1=2;
                           }/*
                           if(ProtOMod==2&&modulo!=""){
                              if(tieneProto){
                                 Proto pt=(Proto)tabProtos.get(modulo);
                                 if(!pt.cspcfyp.startsWith(cspcfyp)){
                                    String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                                    poneError(3,err);
                                 }
                              }
                           }
                           */
                           String err="Se esperaba un identificador constante existente con valor entero y se encontr?: "+lexema;
                           poneError(3,err);
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        cspcfyp=cspcfyp+"2";
                        //Pone la dimension en el argumento:
                        Simbolo sim=(Simbolo)tabSim.get(idKey);
                        if(sim!=null){
                           sim.d1=2;
                        }/*
                        if(ProtOMod==2&&modulo!=""){
                           if(tieneProto){
                              Proto pt=(Proto)tabProtos.get(modulo);
                              if(!pt.cspcfyp.startsWith(cspcfyp)){
                                 String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                                 poneError(3,err);
                              }
                           }
                        }*/
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        String err="Se esperaba un valor entero y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaParametros(tmp,0);
                     }
                  }
            }break;
            case 75:{
                  if(tipoElem==4){
                     estate=77;
                     cspcfyp=cspcfyp+",";
                  }else
                     if(tipoElem==20){
                        estate=76;
                        d2=1;
                     }else{
                        idKey="";
                        String err="Se esperaba una coma o una llave de cierre y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaParametros(tmp,1);
                     }
            }break;
            case 76:{
                  //*** Generacion de codigo ***
                  if(idKey!=""&&ProtOMod==2){
                     //Mete el parametro en la pila de parametros:
                     idAsign=idKey;
                     int cPos=idAsign.indexOf('@');
                     if(cPos!=-1)
                        idAsign=idAsign.substring(0,cPos);
                     codGen.paramArr(idAsign,d1,d2);
                  }
                  d2=2;
                  // Fin de generacion de codigo
                  
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(ProtOMod==2&&modulo!=""){
                     if(tieneProto){
                        Proto pt=(Proto)tabProtos.get(modulo);
                        if(!pt.cspcfyp.startsWith(cspcfyp)||(cspcfyp.length()<pt.cspcfyp.length()&&pt.cspcfyp.charAt(cspcfyp.length())==',')){
                           String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                           poneError2(3,err);
                        }
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==4){
                     estate=72;
                     cspcfyp=cspcfyp+"|";
                  }else
                     if(tipoElem==13){
                        estate=79;
                        cspcfyp=cspcfyp+"|";
                     }else{
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        if(idKey!=""&&!modulo.equals("")){
                           if(ProtOMod==1){
                              Proto pt=(Proto)tabProtos.get(modulo);
                              pt.cspcfyp=cspcfyp;
                           }else if(ProtOMod==2){
                              //Revisa que no se hayan definido menos parametros que en su prototipo:
                              if(tieneProto){
                                 Proto pt=(Proto)tabProtos.get(modulo);
                                 if(pt.parametros.size()>argC[0]){
                                    String err="El numero de parametros es menor al de los definidos en el prototipo.";
                                    editor.errCount++;
                                    editor.errores(estado,3,err,lineCounter,i,"",win,win.nombre,win.ruta);
                                 }
                              }
                           }
                           Simbolo sm=(Simbolo)tabSim.get(modulo);
                           if(sm!=null)
                              sm.cspcfyp=cspcfyp;
                        }
                        d1=2;
                        tipo="<Ninguno>";
                        cspcfyp="";
                        argC[0]=0;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        int next=((Integer)infoStates.pop()).intValue();
                        padresState.pop();
                        estate=next;
                        reuse=true;
                        nivel--;
                     }
            }break;
            case 77:{/*
                  if(tipoElem==9)
                     estate=78;
                  else{
                     String err="Se esperaba un numero entero y se encontr?: "+lexema;
                     editor.errCount++;
                     editor.errores(estado,2,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaParametros(tmp,1);
                  }*/
                  if(tipoElem==9){
                     estate=78;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa que este dentro del rango:
                     d2=Integer.parseInt(lexema);
                     if(d2<2){
                        d2=2;
                        String err="Los arreglos deben tener un tama?o minimo de 2: "+lexema;
                        poneError(3,err);
                     }else
                        if(d2>32768){
                           d2=32768;
                           String err="Los arreglos deben tener un tama?o maximo de 32768: "+lexema;
                           poneError(3,err);
                        }
                     
                     cspcfyp=cspcfyp+d2;
                     //Pone la dimension en el argumento:
                     Simbolo sim=(Simbolo)tabSim.get(idKey);
                     if(sim!=null){
                        sim.d2=d2;
                     }/*
                     if(ProtOMod==2&&modulo!=""){
                        if(tieneProto){
                           Proto pt=(Proto)tabProtos.get(modulo);
                           if(!pt.cspcfyp.startsWith(cspcfyp)){
                              String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                              poneError(3,err);
                           }
                        }
                     }*/
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     if(tipoElem==7){
                        estate=78;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        //Revisa que sea una constante
                        Simbolo sim=(Simbolo)tabSim.get(lexema);
                        if(sim!=null){
                           if(sim.clase=='C'){
                              if(sim.tipo_dato.equals("<Entero>")){
                                 //Revisa que este dentro del rango:
                                 d2=Integer.parseInt(sim.cspcfyp);
                                 if(d2<2){
                                    d2=2;
                                    String err="Los arreglos deben tener un tama?o minimo de 2: "+lexema;
                                    poneError(3,err);
                                 }else
                                    if(d2>32768){
                                       d2=32768;
                                       String err="Los arreglos deben tener un tama?o maximo de 32768: "+lexema;
                                       poneError(3,err);
                                    }
                                 
                                 cspcfyp=cspcfyp+d2;
                                 //Pone la dimension en el argumento:
                                 Simbolo sm=(Simbolo)tabSim.get(idKey);
                                 if(sm!=null){
                                    sm.d2=d2;
                                 }/*
                                 if(ProtOMod==2&&modulo!=""){
                                    if(tieneProto){
                                       Proto pt=(Proto)tabProtos.get(modulo);
                                       if(!pt.cspcfyp.startsWith(cspcfyp)){
                                          String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                                          poneError(3,err);
                                       }
                                    }
                                 }*/
                              }else{
                                 cspcfyp=cspcfyp+"2";
                                 //Pone la dimension en el argumento:
                                 Simbolo sm=(Simbolo)tabSim.get(idKey);
                                 if(sm!=null){
                                    sm.d2=2;
                                 }/*
                                 if(ProtOMod==2&&modulo!=""){
                                    if(tieneProto){
                                       Proto pt=(Proto)tabProtos.get(modulo);
                                       if(!pt.cspcfyp.startsWith(cspcfyp)){
                                          String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                                          poneError(3,err);
                                       }
                                    }
                                 }
                                 */
                                 String err="La constante que define el tama?o del arreglo debe tener valor entero: "+lexema;
                                 poneError(3,err);
                              }
                           }else{
                              cspcfyp=cspcfyp+"2";
                              //Pone la dimension en el argumento:
                              Simbolo sm=(Simbolo)tabSim.get(idKey);
                              if(sm!=null){
                                 sm.d2=2;
                              }/*
                              if(ProtOMod==2&&modulo!=""){
                                 if(tieneProto){
                                    Proto pt=(Proto)tabProtos.get(modulo);
                                    if(!pt.cspcfyp.startsWith(cspcfyp)){
                                       String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                                       poneError(3,err);
                                    }
                                 }
                              }*/
                              String err="Se esperaba una constante con valor entero y se encontr?: "+lexema;
                              poneError(3,err);
                           }
                        }else{
                           cspcfyp=cspcfyp+"2";
                           //Pone la dimension en el argumento:
                           Simbolo sm=(Simbolo)tabSim.get(idKey);
                           if(sm!=null){
                              sm.d2=2;
                           }/*
                           if(ProtOMod==2&&modulo!=""){
                              if(tieneProto){
                                 Proto pt=(Proto)tabProtos.get(modulo);
                                 if(!pt.cspcfyp.startsWith(cspcfyp)){
                                    String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                                    poneError(3,err);
                                 }
                              }
                           }
                           */
                           String err="Se esperaba un identificador constante existente con valor entero y se encontr?: "+lexema;
                           poneError(3,err);
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        cspcfyp=cspcfyp+"2";
                        //Pone la dimension en el argumento:
                        Simbolo sim=(Simbolo)tabSim.get(idKey);
                        if(sim!=null){
                           sim.d2=2;
                        }/*
                        if(ProtOMod==2&&modulo!=""){
                           if(tieneProto){
                              Proto pt=(Proto)tabProtos.get(modulo);
                              if(!pt.cspcfyp.startsWith(cspcfyp)){
                                 String err="El argumento se defini? de forma diferente en el prototipo de este modulo: "+modulo;
                                 poneError(3,err);
                              }
                           }
                        }*/
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        String err="Se esperaba un valor entero y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int tmp=((Integer)infoStates.peek()).intValue();
                        estate=mError.recuperaParametros(tmp,1);
                     }
                  }
            }break;
            case 78:{
                  if(tipoElem==20)
                     estate=76;
                  else{
                     String err="Se esperaba una llave de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaParametros(tmp,0);
                  }
            }break;
            case 79:{
                  if(tipoElem==14||tipoElem==15||tipoElem==16||tipoElem==17){
                     estate=72;
                     revisaTipo();
                  }else{
                     tipo="<Ninguno>";
                     String err="Se esperaba un tipo de dato y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int tmp=((Integer)infoStates.peek()).intValue();
                     estate=mError.recuperaParametros(tmp,0);
                  }
            }break;
            //----------------------------- Fin de Parametros --------------------------
            
            //---------------------------------- Cuerpo --------------------------------
            case 80:{
                  if(tipoElem==12){
                     infoStates.push(new Integer(81));
                     padresState.push(new Integer(84));
                     estate=11;
                  }else
                     if(tipoElem==18){
                        infoStates.push(new Integer(82));
                        padresState.push(new Integer(84));
                        estate=19;
                     }else
                        if(tipoElem==21){
                           infoStates.push(new Integer(83));
                           padresState.push(new Integer(84));
                           estate=40;
                        }else{
                           //Bloque
                           infoStates.push(new Integer(84));
                           padresState.push(new Integer(84));
                           estate=85;
                           reuse=true;
                           nivel++;
                        }
            }break;
            case 81:{
                  if(tipoElem==12){
                     infoStates.push(new Integer(81));
                     padresState.push(new Integer(84));
                     estate=11;
                     
                     String err="Declaraci?n repetida de variables.";
                     poneError(2,err);
                  }else
                     if(tipoElem==18){
                        infoStates.push(new Integer(82));
                        padresState.push(new Integer(84));
                        estate=19;
                     }else
                        if(tipoElem==21){
                           infoStates.push(new Integer(83));
                           padresState.push(new Integer(84));
                           estate=40;
                        }else{
                           //Bloque
                           infoStates.push(new Integer(84));
                           padresState.push(new Integer(84));
                           estate=85;
                           reuse=true;
                           nivel++;
                        }
            }break;
            case 82:{
                  if(tipoElem==12){
                     infoStates.push(new Integer(81));
                     padresState.push(new Integer(84));
                     estate=11;
                     
                     String err="Las variables se deben declarar antes que los arreglos.";
                     poneError(2,err);
                  }else
                     if(tipoElem==18){
                        infoStates.push(new Integer(82));
                        padresState.push(new Integer(84));
                        estate=19;
                        
                        String err="Declaraci?n repetida de arreglos.";
                        poneError(2,err);
                     }else
                        if(tipoElem==21){
                           infoStates.push(new Integer(83));
                           padresState.push(new Integer(84));
                           estate=40;
                        }else{
                           //Bloque
                           infoStates.push(new Integer(84));
                           padresState.push(new Integer(84));
                           estate=85;
                           reuse=true;
                           nivel++;
                        }
            }break;
            case 83:{
                  if(tipoElem==12){
                     infoStates.push(new Integer(81));
                     padresState.push(new Integer(84));
                     estate=11;
                     
                     String err="Las variables se deben declarar antes que los registros.";
                     poneError(2,err);
                  }else
                     if(tipoElem==18){
                        infoStates.push(new Integer(82));
                        padresState.push(new Integer(84));
                        estate=19;
                        
                        String err="Los arreglos se deben declarar antes que los registros.";
                        poneError(2,err);
                     }else
                        if(tipoElem==21){
                           infoStates.push(new Integer(83));
                           padresState.push(new Integer(84));
                           estate=40;
                           
                           String err="Declaraci?n repetida de registros.";
                           poneError(2,err);
                        }else{
                           //Bloque
                           infoStates.push(new Integer(84));
                           padresState.push(new Integer(84));
                           estate=85;
                           reuse=true;
                           nivel++;
                        }
            }break;
            case 84:{
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
            }break;
            //------------------------------- Fin de Cuerpo ----------------------------
            
            //---------------------------------- Bloque --------------------------------
            case 85:{
                  //System.out.println("Sintaxis: Dentro de \"Bloque\": "+lexema);
                  if(lexema.equals("INICIO"))
                     estate=86;
                  else{
                     //Instruccion
                     /*infoStates.push(new Integer(92));
                     padresState.push(new Integer(92));*/
                     estate=93;
                     reuse=true;
                     //nivel++;
                  }
            }break;
            case 86:{
                  if(tipoElem==2)
                     estate=87;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     //Posibles padres: Cuerpo,Si,Mientras,Hacer,Desde,Dependiendo
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaBloque(father);
                     
                     /*String father=damePadre(??);
                     String arrThis=damePadreStrs(92);
                     String arrPadre=damePadreStrs(??);*/
                     //estate=mError.PanicoMayor("Bloque",);
                  }
            }break;
            case 87:{
                  //Instruccion
                  infoStates.push(new Integer(88));
                  padresState.push(new Integer(92));
                  estate=93;
                  reuse=true;
                  nivel++;
            }break;
            case 88:{
                  if(tipoElem==13)
                     estate=89;
                  else{
                     String err="Se esperaba un punto y coma y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaBloque(father);
                  }
            }break;
            case 89:{
                  //Instruccion
                  infoStates.push(new Integer(90));
                  padresState.push(new Integer(92));
                  estate=93;
                  reuse=true;
                  nivel++;
            }break;
            case 90:{
                  if(tipoElem==3)
                     estate=91;
                  else
                     if(tipoElem==13)
                        estate=89;
                     else{
                        String err="Se esperaba un corchete de cierre o un punto y coma y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int father=((Integer)padresState.peek()).intValue();
                        estate=mError.recuperaBloque(father);
                     }
            }break;
            case 91:{
                  if(lexema.equals("FIN")){
                     estate=92;
                  }else{
                     String err="Se esperaba la palabra \"FIN\" y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaBloque(father);
                  }
            }break;
            case 92:{
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
            }break;
            //------------------------------- Fin de Bloque ----------------------------
            
            //-------------------------------- Instruccion -----------------------------
            case 93:{
                  /*for(int n=0;n<nivel;n++)
                     System.out.print("\t");
                  /*System.out.print("Pila:");
                  for(int n=0;n<infoStates.size();n++){
                     int t=((Integer)infoStates.get(n)).intValue();
                     System.out.print(" "+t);
                  }*/
                  //System.out.println(" ");
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  idKey="";
                  d1=d2=2;
                  d1c=d2c=0;
                  tipo="<Ninguno>";
                  cspcfyp="";
                  arrAC=0;
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==7){
                     estate=94;
                     idKey=lexema;
                  }else if(lexema.equals("SI"))
                     estate=102;
                  else if(lexema.equals("MIENTRAS"))
                     estate=103;
                  else if(lexema.equals("HACER"))
                     estate=104;
                  else if(lexema.equals("DESDE"))
                     estate=105;
                  else if(lexema.equals("DEPENDIENDO"))
                     estate=106;
                  else if(lexema.equals("REGRESAR")){
                     estate=107;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa que est? en una Funci?n:
                     if(modulo!=""){
                        if(modulo.equals("PRINCIPAL")){
                           String err="No puede existir un \"REGRESAR\" dentro de \"PRINCIPAL\"";
                           poneError(3,err);
                        }else{
                           Simbolo modSim=(Simbolo)tabSim.get(modulo);
                           if(modSim!=null){
                              if(modSim.clase!='F'){
                                 String err="No puede existir un \"REGRESAR\" dentro de un Procedimiento: \""+modulo+"\"";
                                 poneError(3,err);
                              }
                           }
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else if(lexema.equals("LEER"))
                     estate=111;
                  else if(lexema.equals("ESCRIBIR"))
                     estate=112;
                  else if(lexema.equals("ESCRIBIRCSL"))
                     estate=116;
                  else if(lexema.equals("DTPT")||lexema.equals("LPPT")){
                     estate=120;
                     //Generacion de codigo
                     if(lexema.equals("DTPT"))
                        codGen.escribePL0("OPR  0,22");
                     else
                        codGen.escribePL0("OPR  0,23");
                  }else{
                     String err="Elemento invalido para iniciar una instrucci?n: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaInstruccion(father);
                  }
            }break;
            case 94:{
                  if(tipoElem==25){
                     estate=95;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisar que sea un procedimiento:
                     if(idKey!=""){
                        Simbolo stmp=(Simbolo)tabSim.get(idKey);
                        if(stmp!=null){
                           if(stmp.clase!='P'&&stmp.clase!='F'){
                              String err="Este identificador no es un modulo: "+idKey;
                              editor.errCount++;
                              editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                              idKey="";
                           }else{
                              if(stmp.clase=='F'){
                                 String err="Este identificador es una funcion, s?lo se puede llamar directamente a los procedimientos: "+idKey;
                                 editor.errCount++;
                                 editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                              }
                              //Crea una lista temporal con los parametros que se esperan:
                              if(stmp.cspcfyp!=""){
                                 llenaParam(stmp);
                              }
                           }
                        }else{
                           String err="No existe un modulo con ese nombre: "+idKey;
                           editor.errCount++;
                           editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                           idKey="";
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==8){
                        estate=98;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        //El identificador esta solo, buscar que exista:
                        if(idKey!=""&&modulo!=""){
                           String localKey=idKey+"@"+modulo;
                           if(!tabSim.containsKey(localKey)){
                              //Busca que no sea un registro local:
                              boolean esta=false;
                              String prefijo=idKey+".";
                              for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                 String keyTmp=(String)e.nextElement();
                                 String posfijo="@"+modulo;
                                 if(keyTmp.endsWith(posfijo)){
                                    if(keyTmp.startsWith(prefijo))
                                       esta=true;
                                 }
                              }
                              if(esta){
                                 pilaTipos.push("<Ninguno>");
                                 String err="\""+idKey+"\" es un registro local, falt? definir el elemento dentro del registro a usar.";
                                 editor.errCount++;
                                 editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                              }else{
                                 //Busca en globales:
                                 if(!tabSim.containsKey(idKey)){
                                    pilaTipos.push("<Ninguno>");
                                    //Busca que no sea un registro:
                                    esta=false;
                                    for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                       String keyTmp=(String)e.nextElement();
                                       if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1){
                                          esta=true;
                                       }
                                    }
                                    if(esta){
                                       String err="\""+idKey+"\" es un registro, falt? definir el elemento dentro del registro a usar.";
                                       editor.errCount++;
                                       editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                    }else{
                                       String err="No est? definido un identificador con ese nombre: "+idKey;
                                       editor.errCount++;
                                       editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                    }
                                 }else{
                                    Simbolo stmp=(Simbolo)tabSim.get(idKey);
                                    if(stmp!=null){
                                       if(stmp.d1!=0){
                                          pilaTipos.push(stmp.tipo_dato);
                                          String err="Este identificador es un arreglo, falto definir la posicion en el arreglo: "+idKey;
                                          editor.errCount++;
                                          editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                       }else
                                          if(stmp.clase=='P'||stmp.clase=='F'){
                                             pilaTipos.push("<Ninguno>");
                                             String err="Este identificador es un modulo, no se pueden realizar asignaciones a modulos: "+idKey;
                                             editor.errCount++;
                                             editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                          }else
                                             if(stmp.clase=='C'){
                                                pilaTipos.push("<Ninguno>");
                                                String err="Este identificador es una constante, no se pueden realizar asignaciones a constantes: "+idKey;
                                                editor.errCount++;
                                                editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                             }else{
                                                //Todo salio bien, mete el tipo en la pila
                                                pilaTipos.push(stmp.tipo_dato);
                                             }
                                    }
                                 }
                              }
                           }else{
                              //Busca en locales:
                              Simbolo stmp=(Simbolo)tabSim.get(localKey);
                              if(stmp!=null){
                                 if(stmp.d1!=0){
                                    pilaTipos.push("<Ninguno>");
                                    String err="Este identificador es un arreglo, falto definir la posicion en el arreglo: "+idKey;
                                    editor.errCount++;
                                    editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                 }else
                                    if(stmp.clase=='P'||stmp.clase=='F'){
                                       pilaTipos.push("<Ninguno>");
                                       String err="Este identificador es un modulo, no se pueden realizar asignaciones a modulos: "+idKey;
                                       editor.errCount++;
                                       editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                    }else
                                       if(stmp.clase=='C'){
                                          pilaTipos.push("<Ninguno>");
                                          String err="Este identificador es una constante, no se pueden realizar asignaciones a constantes: "+idKey;
                                          editor.errCount++;
                                          editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                       }else{
                                          //Todo salio bien, mete el tipo en la pila
                                          pilaTipos.push(stmp.tipo_dato);
                                       }
                              }
                           }
                        }else
                           pilaTipos.push("<Ninguno>");
                        
                        Padre p=new Padre(idKey,'S',null);
                        pilaPadres.push(p);
                        pilaOp.push(new Operador(lexema,p));
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else
                        if(tipoElem==2){
                           estate=99;
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                           if(idKey!=""&&modulo!=""){
                              //Busca si hay algun registro con ese nombre:
                              boolean esta=false;
                              String prefijo=idKey+".";
                              //Busca primero en registros locales:
                              for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                 String keyTmp=(String)e.nextElement();
                                 String posfijo="@"+modulo;
                                 if(keyTmp.endsWith(posfijo)){
                                    if(keyTmp.startsWith(prefijo)){
                                       esta=true;
                                       esLocal=true;
                                    }
                                 }
                              }
                              if(!esta){
                                 //Busca luego en registros globales:
                                 for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                    String keyTmp=(String)e.nextElement();
                                    if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1){
                                       esta=true;
                                       esLocal=false;
                                    }
                                 }
                                 if(!esta){
                                    String err="No existe un registro con ese nombre: "+idKey;
                                    editor.errCount++;
                                    editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                    idKey="";
                                 }
                              }
                           }
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        }else
                           if(tipoElem==19){
                              estate=100;
                              //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                              //Busca el simbolo en la tabla y revisa que sea un arreglo
                              if(idKey!=""&&modulo!=""){
                                 //Busca primero en arreglos locales:
                                 String localKey=idKey+"@"+modulo;
                                 if(!tabSim.containsKey(localKey)){
                                    if(!tabSim.containsKey(idKey)){
                                       String err="No est? definido un arreglo con ese nombre: "+idKey;
                                       editor.errCount++;
                                       editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                       idKey="";
                                    }else{
                                       Simbolo stmp=(Simbolo)tabSim.get(idKey);
                                       if(stmp!=null)
                                          if(stmp.d1==0){
                                             String err="Este identificador no es un arreglo: "+idKey;
                                             editor.errCount++;
                                             editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                             idKey="";
                                          }
                                    }
                                 }else{
                                    Simbolo stmp=(Simbolo)tabSim.get(localKey);
                                    if(stmp!=null)
                                       if(stmp.d1==0){
                                          String err="Este identificador no es un arreglo: "+idKey;
                                          editor.errCount++;
                                          editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                          idKey="";
                                       }else
                                          idKey=localKey;
                                 }
                              }
                              /*if(idKey!=""){
                                 pilaPadres.push(new Padre(idKey,'A'));
                              }*/
                              //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                           }else{
                              idKey="";
                              String err="Se esperaba \"(\" ? \"[\" ? \"{\" ? \"<-\" y se encontr?: "+lexema;
                              poneError(2,err);
                              
                              int father=((Integer)padresState.peek()).intValue();
                              estate=mError.recuperaInstruccionIdent(father);
                           }
            }break;
            case 95:{
                  //*** Generacion de codigo ***
                  String tmpLabel="$Label"+codGen.etCont;
                  codGen.etCont++;
                  codGen.escribePL0("LOD  "+tmpLabel+",0");
                  pilaDirs.push(tmpLabel);
                  // Fin de generacion de codigo
                  
                  if(tipoElem==26){
                     estate=101;
                     
                     //*** Generacion de codigo ***
                     Proto pt=(Proto)tabProtos.get(idKey);
                     if(pt!=null){
                        codGen.escribePL0("CAL  "+idKey+","+pt.dir);
                        //Resuelve la etiqueta:
                        String tlbl=(String)pilaDirs.pop();
                        Simbolo et=new Simbolo(tlbl,'N',"<Ninguno>","");
                        et.d1=codGen.contador+1;
                        tabSim.put(tlbl,et);
                     }
                     // Fin de generacion de codigo
                     
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Verifica que el procedimiento no recibe parametros:
                     Simbolo stmp=(Simbolo)tabSim.get(idKey);
                     if(stmp!=null){
                        if(stmp.cspcfyp!=""){
                           String err="El procedimiento \""+idKey+"\" debe recibir "+params.size()+" parametros.";
                           editor.errCount++;
                           editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     //Parametros_Identificador
                     infoStates.push(new Integer(96));
                     padresState.push(new Integer(95));
                     estate=220;
                     reuse=true;
                     nivel++;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Verifica que la funci?n si recibe parametros:
                     Simbolo stmp=(Simbolo)tabSim.get(idKey);
                     if(stmp!=null){
                        if(stmp.cspcfyp.equals("")){
                           String err="El procedimiento \""+idKey+"\" no recibe parametros.";
                           poneError(3,err);
                           idKey="";
                        }
                        arrAC++;
                        argC[arrAC]=1;
                        
                        ArrayList lt=new ArrayList();
                        for(int a=0;a<params.size();a++){
                           lt.add(a,params.get(a));
                        }
                        pilaPadres.push(new Padre(idKey,'M',lt));
                        enModulo=true;
                     }else{
                        arrAC++;
                        argC[arrAC]=1;
                        pilaPadres.push(new Padre(idKey,'E',null));
                        enModulo=true;
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }
            }break;
            case 96:{
                  //*** Generacion de codigo ***
                  Proto pt=(Proto)tabProtos.get(idKey);
                  if(pt!=null){
                     codGen.escribePL0("CAL  "+idKey+","+pt.dir);
                     //Resuelve la etiqueta:
                     String tlbl="";
                     if(!pilaDirs.empty())
                        tlbl=(String)pilaDirs.pop();
                     Simbolo et=new Simbolo(tlbl,'N',"<Ninguno>","");
                     et.d1=codGen.contador+1;
                     tabSim.put(tlbl,et);
                  }
                  // Fin de generacion de codigo
                  
                  if(tipoElem==26)
                     estate=101;
                  else{
                     String err="Se esperaba un parentesis de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaInstruccionIdent(father);
                  }
            }break;
            /*case 97:{
                  //Fue reemplazado por el caso de aceptaci?n 101
            }break;*/
            case 98:{
                  //Asignacion Simple
                  infoStates.push(new Integer(101));
                  padresState.push(new Integer(95));
                  estate=155;
                  reuse=true;
                  nivel++;
            }break;
            case 99:{
                  //Asignacion Registro
                  infoStates.push(new Integer(101));
                  padresState.push(new Integer(95));
                  estate=157;
                  reuse=true;
                  nivel++;
            }break;
            case 100:{
                  //Asignacion Arreglo
                  infoStates.push(new Integer(101));
                  padresState.push(new Integer(95));
                  estate=167;
                  reuse=true;
                  nivel++;
            }break;
            case 101:{
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
            }break;
            case 102:{
                  //SI
                  infoStates.push(new Integer(101));
                  padresState.push(new Integer(111));
                  estate=122;
                  reuse=true;
                  nivel++;
            }break;
            case 103:{
                  //Mientras
                  infoStates.push(new Integer(101));
                  padresState.push(new Integer(111));
                  estate=127;
                  reuse=true;
                  nivel++;
            }break;
            case 104:{
                  //Hacer
                  infoStates.push(new Integer(101));
                  padresState.push(new Integer(111));
                  estate=130;
                  reuse=true;
                  nivel++;
            }break;
            case 105:{
                  //Desde
                  infoStates.push(new Integer(101));
                  padresState.push(new Integer(111));
                  estate=134;
                  reuse=true;
                  nivel++;
            }break;
            case 106:{
                  //Dependiendo
                  infoStates.push(new Integer(101));
                  padresState.push(new Integer(111));
                  estate=144;
                  reuse=true;
                  nivel++;
            }break;
            case 107:{
                  //Regresar
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Revisa que regresar no est? dentro de una estructura de control
                  //para poner a la banderaRegresar en true:
                  int pos=padresState.search(new Integer(59));
                  //System.out.println("Posici?n de MODULOS en la pila de padres: "+pos+", en la linea: "+lineCounter);
                  if(pos==2||pos==3)
                     hayReturn=true;
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==25)
                     estate=108;
                  else{
                     String err="Se esperaba un parentesis de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaInstruccionReturn(father);
                  }
            }break;
            case 108:{
                  if(tipoElem==26){
                     estate=109;
                     reuse=true;
                     proceder=false;
                     String err="REGRESAR debe tener algo dentro de los parentesis.";
                     poneError(2,err);
                  }else{
                     proceder=true;
                     //Regla_OR
                     infoStates.push(new Integer(109));
                     padresState.push(new Integer(109));
                     estate=240;
                     reuse=true;
                     nivel++;
                     
                     //Generacion de codigo:
                     pilaPadres.push(new Padre("REGRESAR",'R',null));
                  }
            }break;
            case 109:{
                  //Generacion de codigo:
                  codGen.escribePL0("STO  0,"+modulo);
                  codGen.escribePL0("OPR  0,1");
                  if(!pilaPadres.empty())
                     pilaPadres.pop();
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(proceder)
                     tipo=(String)pilaTipos.pop();
                  else
                     tipo="<Ninguno>";
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==26){
                     estate=101;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     Simbolo modSim=(Simbolo)tabSim.get(modulo);
                     if(modSim!=null){
                        if(modSim.clase=='F'){
                           //Revisa que el tipo sea el mismo que lo que sali? de Regla_OR
                           if(!modSim.tipo_dato.equals(tipo)){
                              if(modSim.tipo_dato.equals("<Real>")){
                                 if(!tipo.equals("<Entero>")){
                                    String err="Se esperaba regresar un "+modSim.tipo_dato+" y se encontro un: "+tipo;
                                    poneError2(3,err);
                                 }
                              }else{
                                 String err="Se esperaba regresar un "+modSim.tipo_dato+" y se encontro un: "+tipo;
                                 poneError2(3,err);
                              }
                           }
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     String err="Se esperaba un parentesis de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaInstruccionReturn(father);
                  }
            }break;
            /*case 110:{
                  //Fue reemplazado por el caso de aceptaci?n 101
            }break;*/
            case 111:{
                  //Leer
                  infoStates.push(new Integer(101));
                  padresState.push(new Integer(111));
                  estate=183;
                  reuse=true;
                  nivel++;
            }break;
            case 112:{
                  //Escribir
                  if(tipoElem==25)
                     estate=113;
                  else{
                     String err="Se esperaba un parentesis de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaInstruccionWrite(father);
                  }
            }break;
            case 113:{
                  if(tipoElem==26){
                     estate=101;
                     
                     String err="La instruccion \"ESCRIBIR\" debe tener por lo menos un parametro: "+lexema;
                     poneError(2,err);
                  }else{
                     //Parametros_Identificador
                     infoStates.push(new Integer(114));
                     padresState.push(new Integer(114));
                     estate=220;
                     reuse=true;
                     nivel++;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     pilaPadres.push(new Padre("ESCRIBIR",'E',null));
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }
            }break;
            case 114:{
                  if(tipoElem==26)
                     estate=101;
                  else{
                     String err="Se esperaba un parentesis de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaInstruccionWrite(father);
                  }
            }break;
            /*case 115:{
                  //Fue reemplazado por el caso de aceptaci?n 101
            }break;*/
            case 116:{
                  //EscribirCSL
                  if(tipoElem==25)
                     estate=117;
                  else{
                     String err="Se esperaba un parentesis de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaInstruccionWriteLN(father);
                  }
            }break;
            case 117:{
                  if(tipoElem==26){
                     estate=101;
                     //Generacion de codigo:
                     codGen.escribePL0("LIT  \"\",0");
                     codGen.escribePL0("OPR  0,21");
                  }else{
                     //Parametros_Identificador
                     infoStates.push(new Integer(118));
                     padresState.push(new Integer(118));
                     estate=220;
                     reuse=true;
                     nivel++;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     pilaPadres.push(new Padre("ESCRIBIRCSL",'E',null));
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }
            }break;
            case 118:{
                  if(tipoElem==26){
                     estate=101;
                     //Generacion de codigo:
                     codGen.escribePL0("OPR  0,21");
                  }else{
                     String err="Se esperaba un parentesis de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaInstruccionWriteLN(father);
                  }
            }break;
            /*case 119:{
                  //Fue reemplazado por el caso de aceptaci?n 101
            }break;*/
            case 120:{
                  if(tipoElem==25)
                     estate=121;
                  else{
                     String err="Se esperaba un parentesis de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaInstruccionPT(father);
                  }
            }break;
            case 121:{
                  if(tipoElem==26)
                     estate=101;
                  else{
                     String err="Se esperaba un parentesis de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaInstruccionPT(father);
                  }
            }break;
            //----------------------------- Fin de Instruccion -------------------------
            
            //------------------------------------ SI ----------------------------------
            case 213:{
                  if(lexema.equals("SI"))
                     estate=122;
                  /*else{
                     //vacio
                  }*/
            }break;
            case 122:{
                  //Generacion de codigo:
                  pilaPadres.push(new Padre("SI",'C',null));
                  
                  //Regla_OR
                  infoStates.push(new Integer(123));
                  padresState.push(new Integer(123));
                  estate=240;
                  reuse=true;
                  nivel++;
            }break;
            case 123:{
                  if(!pilaPadres.empty())
                     pilaPadres.pop();
                  //*** Generacion de codigo ***
                  ControlStruct si;
                  if(!enSiNo){
                     si=new ControlStruct("Si");
                     si.EtX="$Label"+codGen.etCont;
                     pilaStructs.push(si);
                  }else{
                     if(!pilaStructs.empty()){
                        si=(ControlStruct)pilaStructs.peek();
                        if(si.nombre.equals("Si")){
                           si.EtA="$Label"+codGen.etCont;
                        }else
                           System.out.println("Error en la pila de esctructuras: Si, en la linea: "+lineCounter);
                     }else
                        System.out.println("Error en la pila de esctructuras: Si, en la linea: "+lineCounter);
                  }
                  
                  codGen.escribeEt("JMC  F,$Label");
                  // Fin de generacion de codigo
                  
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Revisa que el tipo de dato al final de la pila sea logico:
                  if(!pilaTipos.isEmpty()){
                     String tipoTmp=(String)pilaTipos.pop();
                     if(!tipoTmp.equals("<Logico>")){
                        String err="La estructura \"SI\" s?lo acepta operaciones de tipo <Logico> y se encontr? un tipo: "+tipoTmp;
                        poneError2(3,err);
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Bloque
                  infoStates.push(new Integer(124));
                  padresState.push(new Integer(123));
                  estate=85;
                  reuse=true;
                  nivel++;
            }break;
            case 124:{
                  if(lexema.equals("SINO")){
                     estate=125;
                  }else
                     if(lexema.equals("SINOSI")){
                        estate=122;
                        enSiNo=true;
                     }else{
                        //*** Generacion de codigo ***
                        if(!pilaStructs.empty()){
                           ControlStruct si=(ControlStruct)pilaStructs.pop();
                           if(si.nombre.equals("Si")){
                              //Resuelve la etiqueta (obtiene el contador y guarda la etiqueta en la tabSimb):
                              if(!enSiNo){
                                 Simbolo etX=new Simbolo(si.EtX,'N',"<Ninguno>","");
                                 etX.d1=codGen.contador+1;
                                 tabSim.put(si.EtX,etX);
                              }else{
                                 Simbolo etY=new Simbolo(si.EtY,'N',"<Ninguno>","");
                                 etY.d1=codGen.contador+1;
                                 tabSim.put(si.EtY,etY);
                              }
                           }else
                              System.out.println("ERROR en la pila de estructuras: Si, en la linea: "+lineCounter);
                        }else{
                           System.out.println("ERROR en la pila de estructuras en la linea: "+lineCounter);
                        }
                        enSiNo=false;
                        // Fin de generacion de codigo
                        
                        int next=((Integer)infoStates.pop()).intValue();
                        padresState.pop();
                        estate=next;
                        reuse=true;
                        nivel--;
                     }
                  
                  //*** Generacion de codigo ***
                  //Revisa si se va a entrar en opcional para hacer lo necesario:
                  if(estate==125||estate==122){
                     if(!pilaStructs.empty()){
                        ControlStruct si=(ControlStruct)pilaStructs.peek();
                        if(si.nombre.equals("Si")){
                           if(si.EtY==""){
                              si.EtY="$Label"+codGen.etCont;
                              codGen.escribeEt("JMP  0,$Label");
                           }else
                              codGen.escribePL0("JMP  0,"+si.EtY);
                           //Resuelve la etiqueta (obtiene el contador y guarda la etiqueta en la tabSimb):
                           if(si.EtA==""){
                              Simbolo etX=new Simbolo(si.EtX,'N',"<Ninguno>","");
                              etX.d1=codGen.contador+1;
                              tabSim.put(si.EtX,etX);
                           }else{
                              Simbolo etA=new Simbolo(si.EtA,'N',"<Ninguno>","");
                              etA.d1=codGen.contador+1;
                              tabSim.put(si.EtA,etA);
                              si.EtA="";
                           }
                        }else
                           System.out.println("ERROR en la pila de estructuras: Si, en la linea: "+lineCounter);
                     }else{
                        System.out.println("ERROR en la pila de estructuras: Si, en la linea: "+lineCounter);
                     }
                  }
                  // Fin de generacion de codigo
            }break;
            case 125:{
                  //Bloque
                  infoStates.push(new Integer(126));
                  padresState.push(new Integer(123));
                  estate=85;
                  reuse=true;
                  nivel++;
            }break;
            case 126:{
                  //*** Generacion de codigo ***
                  /*ControlStruct si=(ControlStruct)pilaStructs.pop();
                  if(si!=null){
                     if(si.nombre.equals("Si")||si.nombre.equals("SiNoSi")){
                        //Resuelve la etiqueta (obtiene el contador y guarda la etiqueta en la tabSimb):
                        Simbolo etY=new Simbolo(si.EtY,'N',"<Ninguno>","");
                        etY.d1=codGen.contador+1;
                        tabSim.put(si.EtY,etY);
                     }else
                        System.out.println("ERROR en la pila de estructuras: SiNo");
                  }else{
                     System.out.println("ERROR en la pila de estructuras");
                  }
                  enSiNo=false;*/
                  // Fin de generacion de codigo
                  //*** Generacion de codigo ***
                  if(!pilaStructs.empty()){
                     ControlStruct si=(ControlStruct)pilaStructs.pop();
                     if(si.nombre.equals("Si")){
                        //Resuelve la etiqueta (obtiene el contador y guarda la etiqueta en la tabSimb):
                        Simbolo etY=new Simbolo(si.EtY,'N',"<Ninguno>","");
                        etY.d1=codGen.contador+1;
                        tabSim.put(si.EtY,etY);
                     }else
                        System.out.println("ERROR en la pila de estructuras: Si, en la linea: "+lineCounter);
                  }else{
                     System.out.println("ERROR en la pila de estructuras en la linea: "+lineCounter);
                  }
                  enSiNo=false;
                  // Fin de generacion de codigo
                  
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
            }break;
            //--------------------------------- Fin de SI ------------------------------
            
            //--------------------------------- Mientras -------------------------------
            case 214:{
                  if(lexema.equals("MIENTRAS"))
                     estate=127;
                  /*else{
                     //vacio
                  }*/
            }break;
            case 127:{
                  //*** Generacion de codigo ***
                  ControlStruct mientras=new ControlStruct("Mientras");
                  mientras.dir=codGen.contador+1;
                  pilaStructs.push(mientras);
                  // Fin de generacion de codigo
                  
                  pilaPadres.push(new Padre("MIENTRAS",'C',null));
                  
                  //Regla_OR
                  infoStates.push(new Integer(128));
                  padresState.push(new Integer(128));
                  estate=240;
                  reuse=true;
                  nivel++;
            }break;
            case 128:{
                  if(!pilaPadres.empty())
                     pilaPadres.pop();
                  //*** Generacion de codigo ***
                  if(!pilaStructs.empty()){
                     ControlStruct mientras=(ControlStruct)pilaStructs.peek();
                     if(mientras.nombre.equals("Mientras"))
                        mientras.EtX="$Label"+codGen.etCont;
                     else
                        System.out.println("ERROR: Se esperaba un Mientras en la pila de estructuras y se encontro: \""+mientras.nombre+"\"");
                  }
                  codGen.escribeEt("JMC  F,$Label");
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Revisa que el tipo de dato al final de la pila sea logico:
                  if(!pilaTipos.isEmpty()){
                     String tipoTmp=(String)pilaTipos.pop();
                     if(!tipoTmp.equals("<Logico>")){
                        String err="La estructura \"MIENTRAS\" s?lo acepta operaciones de tipo <Logico> y se encontr? un tipo: "+tipoTmp;
                        poneError2(3,err);
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Bloque
                  infoStates.push(new Integer(129));
                  padresState.push(new Integer(128));
                  estate=85;
                  reuse=true;
                  nivel++;
            }break;
            case 129:{
                  //*** Generacion de codigo ***
                  ControlStruct mientras=(ControlStruct)pilaStructs.pop();
                  if(mientras!=null&&mientras.nombre.equals("Mientras")){
                     codGen.escribePL0("JMP  0,"+mientras.dir);
                     //Resuelve la etiqueta (obtiene el contador y guarda la etiqueta en la tabSimb):
                     Simbolo etX=new Simbolo(mientras.EtX,'N',"<Ninguno>","");
                     etX.d1=codGen.contador+1;
                     tabSim.put(mientras.EtX,etX);
                  }else{
                     System.out.println("ERROR en la pila de estructuras: Mientras");
                  }
                  // Fin de generacion de codigo
                  
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
            }break;
            //------------------------------ Fin de Mientras ---------------------------
            
            //----------------------------------- Hacer --------------------------------
            case 215:{
                  if(lexema.equals("HACER"))
                     estate=130;
                  /*else{
                     //vacio
                  }*/
            }break;
            case 130:{
                  //*** Generacion de codigo ***
                  ControlStruct hacer=new ControlStruct("Hacer");
                  hacer.dir=codGen.contador+1;
                  pilaStructs.push(hacer);
                  // Fin de generacion de codigo
                  
                  //Bloque
                  infoStates.push(new Integer(131));
                  padresState.push(new Integer(131));
                  estate=85;
                  reuse=true;
                  nivel++;
            }break;
            case 131:{
                  if(lexema.equals("HASTA"))
                     estate=132;
                  else{
                     String err="Se esperaba la palabra \"HASTA\" y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaHacer(father);
                  }
            }break;
            case 132:{
                  //Generacion de codigo:
                  pilaPadres.push(new Padre("HACER",'C',null));
                  
                  //Regla_OR
                  infoStates.push(new Integer(133));
                  padresState.push(new Integer(131));
                  estate=240;
                  reuse=true;
                  nivel++;
            }break;
            case 133:{
                  if(!pilaPadres.empty())
                     pilaPadres.pop();
                  
                  //*** Generacion de codigo ***
                  ControlStruct hacer=(ControlStruct)pilaStructs.pop();
                  if(hacer!=null&&hacer.nombre.equals("Hacer")){
                     codGen.escribePL0("JMC  F,"+hacer.dir);
                  }else{
                     System.out.println("ERROR en la pila de estructuras");
                  }
                  // Fin de generacion de codigo
                  
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Revisa que el tipo de dato al final de la pila sea logico:
                  if(!pilaTipos.isEmpty()){
                     String tipoTmp=(String)pilaTipos.pop();
                     if(!tipoTmp.equals("<Logico>")){
                        String err="La estructura \"HACER-HASTA\" s?lo acepta operaciones de tipo <Logico> y se encontr? un tipo: "+tipoTmp;
                        poneError2(3,err);
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
            }break;
            //-------------------------------- Fin de Hacer ----------------------------
            
            //----------------------------------- Desde --------------------------------
            case 216:{
                  if(lexema.equals("DESDE"))
                     estate=134;
                  /*else{
                     //vacio
                  }*/
            }break;
            case 134:{
                  if(tipoElem==7){
                     estate=135;
                     //*** Generacion de codigo ***
                     ControlStruct desde=new ControlStruct("Desde");
                     desde.ident=lexema;
                     pilaStructs.push(desde);
                     // Fin de generacion de codigo
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisar que el identificador exista:
                     idKey=lexema;
                     if(idKey!=""&&modulo!=""){
                        String localKey=idKey+"@"+modulo;
                        if(!tabSim.containsKey(localKey)){
                           //Busca que no sea un registro local:
                           boolean esta=false;
                           String prefijo=idKey+".";
                           for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                              String keyTmp=(String)e.nextElement();
                              String posfijo="@"+modulo;
                              if(keyTmp.endsWith(posfijo)){
                                 if(keyTmp.startsWith(prefijo))
                                    esta=true;
                              }
                           }
                           if(esta){
                              String err="\""+idKey+"\" es un registro, s?lo puede haber una variable simple.";
                              poneError(3,err);
                           }else{
                              //Busca en globales:
                              if(!tabSim.containsKey(idKey)){
                                 //Busca que no sea un registro:
                                 esta=false;
                                 for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                    String keyTmp=(String)e.nextElement();
                                    if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1){
                                       esta=true;
                                    }
                                 }
                                 if(esta){
                                    String err="\""+idKey+"\" es un registro, s?lo puede haber una variable simple.";
                                    poneError(3,err);
                                 }else{
                                    String err="No est? definido un identificador con ese nombre: "+idKey;
                                    poneError(3,err);
                                 }
                              }else{
                                 Simbolo stmp=(Simbolo)tabSim.get(idKey);
                                 if(stmp!=null){
                                    if(stmp.d1!=0){
                                       String err="\""+idKey+"\" es un arreglo, s?lo puede haber una variable simple.";
                                       poneError(3,err);
                                    }else
                                       if(stmp.clase=='P'||stmp.clase=='F'){
                                          String err="\""+idKey+"\" es un modulo, s?lo puede haber una variable simple.";
                                          poneError(3,err);
                                       }else
                                          if(stmp.clase=='C'){
                                             String err="Este identificador es una constante, no se puede usar en la estructura \"DESDE\": "+idKey;
                                             editor.errCount++;
                                             editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                          }else
                                             if(!stmp.tipo_dato.equals("<Entero>")){
                                                String err="En la estructura \"DESDE\" debe haber un entero y se encontr? un "+stmp.tipo_dato;
                                                poneError(3,err);
                                             }
                                 }
                              }
                           }
                        }else{
                           //Busca en locales:
                           Simbolo stmp=(Simbolo)tabSim.get(localKey);
                           if(stmp!=null){
                              if(stmp.d1!=0){
                                 String err="\""+idKey+"\" es un arreglo, s?lo puede haber una variable simple.";
                                 poneError(3,err);
                              }else
                                 if(stmp.clase=='P'||stmp.clase=='F'){
                                    String err="\""+idKey+"\" es un modulo, s?lo puede haber una variable simple.";
                                    poneError(3,err);
                                 }else
                                    if(stmp.clase=='C'){
                                       String err="Este identificador es una constante, no se puede usar en la estructura \"DESDE\": "+idKey;
                                       editor.errCount++;
                                       editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                    }else
                                       if(!stmp.tipo_dato.equals("<Entero>")){
                                          String err="En la estructura \"DESDE\" debe haber un entero y se encontr? un "+stmp.tipo_dato;
                                          poneError(3,err);
                                       }
                           }
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDesde(father,0);
                  }
            }break;
            case 135:{
                  if(tipoElem==19)
                     estate=136;
                  else{
                     String err="Se esperaba una llave de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDesde(father,0);
                  }
            }break;
            case 136:{
                  //Generacion de codigo:
                  pilaPadres.push(new Padre("DESDE",'C',null));
                  
                  //Expresion
                  infoStates.push(new Integer(137));
                  padresState.push(new Integer(137));
                  estate=233;
                  reuse=true;
                  nivel++;
            }break;
            case 137:{
                  //*** Generacion de codigo ***
                  if(!pilaStructs.empty()){
                     ControlStruct desde=(ControlStruct)pilaStructs.peek();
                     if(desde.nombre.equals("Desde")){
                        codGen.escribePL0("STO  0,"+desde.ident);
                        desde.dir=codGen.contador+1;
                        codGen.escribePL0("LOD  "+desde.ident+",0");
                     }else
                        System.out.println("ERROR en la pila de estructuras: Desde, en la linea: "+lineCounter);
                  }else
                     System.out.println("ERROR en la pila de estructuras: Desde, en la linea: "+lineCounter);
                  // Fin de generacion de codigo
                  
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Revisa que el tipo de dato al final de la pila sea logico:
                  if(!pilaTipos.isEmpty()){
                     String tipoTmp=(String)pilaTipos.pop();
                     if(!tipoTmp.equals("<Entero>")){
                        String err="La estructura \"DESDE\" s?lo acepta expresiones que den como resultado un <Entero> y se encontr? un: "+tipoTmp;
                        poneError2(3,err);
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==4)
                     estate=138;
                  else{
                     String err="Se esperaba una coma y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDesde(father,11);
                  }
            }break;
            case 138:{
                  //Expresion
                  infoStates.push(new Integer(139));
                  padresState.push(new Integer(137));
                  estate=233;
                  reuse=true;
                  nivel++;
            }break;
            case 139:{
                  if(!pilaPadres.empty())
                     pilaPadres.pop();
                  
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Revisa que el tipo de dato al final de la pila sea logico:
                  if(!pilaTipos.isEmpty()){
                     String tipoTmp=(String)pilaTipos.pop();
                     if(!tipoTmp.equals("<Entero>")){
                        String err="La estructura \"DESDE\" s?lo acepta expresiones que den como resultado un <Entero> y se encontr? un: "+tipoTmp;
                        poneError2(3,err);
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==20)
                     estate=140;
                  else{
                     String err="Se esperaba una llave de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDesde(father,11);
                  }
            }break;
            case 140:{
                  if(lexema.equals("INC")||lexema.equals("DEC")){
                     estate=141;
                     //*** Generacion de codigo ***
                     if(lexema.equals("INC")){
                        codGen.escribePL0("OPR  0,13");
                     }else{
                        if(!pilaStructs.empty()){
                           ControlStruct desde=(ControlStruct)pilaStructs.peek();
                           if(desde.nombre.equals("Desde")){
                              codGen.escribePL0("OPR  0,12");
                              desde.fueDEC=true;
                           }else
                              System.out.println("ERROR en la pila de estructuras: Desde, en la linea: "+lineCounter);
                        }else
                           System.out.println("ERROR en la pila de estructuras: Desde, en la linea: "+lineCounter);
                     }
                     // Fin de generacion de codigo
                  }else{
                     //*** Generacion de codigo ***
                     codGen.escribePL0("OPR  0,13");
                     if(!pilaStructs.empty()){
                        ControlStruct desde=(ControlStruct)pilaStructs.peek();
                        if(desde.nombre.equals("Desde"))
                           desde.EtX="$Label"+codGen.etCont;
                        else
                           System.out.println("ERROR en la pila de estructuras: Desde, en la linea: "+lineCounter);
                     }else
                        System.out.println("ERROR en la pila de estructuras: Desde, en la linea: "+lineCounter);
                     codGen.escribeEt("JMC  F,$Label");
                     // Fin de generacion de codigo
                     
                     //Bloque
                     infoStates.push(new Integer(143));
                     padresState.push(new Integer(137));
                     estate=85;
                     reuse=true;
                     nivel++;
                  }
            }break;
            case 141:{
                  if(tipoElem==9){
                     estate=142;
                     //*** Generacion de codigo ***
                     if(!pilaStructs.empty()){
                        ControlStruct desde=(ControlStruct)pilaStructs.peek();
                        if(desde.nombre.equals("Desde")){
                           desde.incVal=lexema;
                        }else
                           System.out.println("ERROR en la pila de estructuras: Desde, en la linea: "+lineCounter);
                     }else
                        System.out.println("ERROR en la pila de estructuras: Desde, en la linea: "+lineCounter);
                     // Fin de generacion de codigo
                  }else{
                     String err="Se esperaba un entero y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDesde(father,11);
                  }
            }break;
            case 142:{
                  //*** Generacion de codigo ***
                  if(!pilaStructs.empty()){
                     ControlStruct desde=(ControlStruct)pilaStructs.peek();
                     if(desde.nombre.equals("Desde"))
                        desde.EtX="$Label"+codGen.etCont;
                     else
                        System.out.println("ERROR en la pila de estructuras: Desde, en la linea: "+lineCounter);
                  }else
                     System.out.println("ERROR en la pila de estructuras: Desde, en la linea: "+lineCounter);
                  codGen.escribeEt("JMC  F,$Label");
                  // Fin de generacion de codigo
                  
                  //Bloque
                  infoStates.push(new Integer(143));
                  padresState.push(new Integer(137));
                  estate=85;
                  reuse=true;
                  nivel++;
            }break;
            case 143:{
                  //*** Generacion de codigo ***
                  if(!pilaStructs.empty()){
                     ControlStruct desde=(ControlStruct)pilaStructs.pop();
                     if(desde.nombre.equals("Desde")){
                        codGen.escribePL0("LIT  "+desde.incVal+",0");
                        if(desde.fueDEC)
                           codGen.escribePL0("OPR  0,9");
                        codGen.escribePL0("LOD  "+desde.ident+",0");
                        codGen.escribePL0("OPR  0,2");
                        codGen.escribePL0("STO  0,"+desde.ident);
                        codGen.escribePL0("JMP  0,"+desde.dir);
                        //Resuelve la etiqueta:
                        Simbolo etX=new Simbolo(desde.EtX,'N',"<Ninguno>","");
                        etX.d1=codGen.contador+1;
                        tabSim.put(desde.EtX,etX);
                     }else
                        System.out.println("ERROR en la pila de estructuras: Desde, en la linea: "+lineCounter);
                  }else
                     System.out.println("ERROR en la pila de estructuras: Desde, en la linea: "+lineCounter);
                  // Fin de generacion de codigo
                  
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
            }break;
            //-------------------------------- Fin de Desde ----------------------------
            
            //-------------------------------- Dependiendo -----------------------------
            case 217:{
                  if(lexema.equals("DEPENDIENDO"))
                     estate=144;
                  /*else{
                     //vacio
                  }*/
            }break;
            case 144:{
                  if(tipoElem==7){
                     estate=145;
                     
                     //*** Generacion de codigo ***
                     ControlStruct dependiendo=new ControlStruct("Dependiendo");
                     dependiendo.ident=lexema;
                     pilaStructs.push(dependiendo);
                     // Fin de generacion de codigo
                     
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     idKey=lexema;
                     pilaTipos.push("<Ninguno>");
                     //El identificador esta solo, buscar que exista:
                     if(modulo!=""){
                        String localKey=idKey+"@"+modulo;
                        if(!tabSim.containsKey(localKey)){
                           //Busca que no sea un registro local:
                           boolean esta=false;
                           String prefijo=idKey+".";
                           for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                              String keyTmp=(String)e.nextElement();
                              String posfijo="@"+modulo;
                              if(keyTmp.endsWith(posfijo)){
                                 if(keyTmp.startsWith(prefijo))
                                    esta=true;
                              }
                           }
                           if(esta){
                              String err="\""+idKey+"\" es un registro local, solo se aceptan variables simples.";
                              poneError(3,err);
                           }else{
                              //Busca en globales:
                              if(!tabSim.containsKey(idKey)){
                                 //Busca que no sea un registro:
                                 esta=false;
                                 for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                    String keyTmp=(String)e.nextElement();
                                    if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1){
                                       esta=true;
                                    }
                                 }
                                 if(esta){
                                    String err="\""+idKey+"\" es un registro, solo se aceptan variables simples.";
                                    poneError(3,err);
                                 }else{
                                    String err="No est? definido un identificador con ese nombre: "+idKey;
                                    poneError(3,err);
                                 }
                              }else{
                                 Simbolo stmp=(Simbolo)tabSim.get(idKey);
                                 if(stmp!=null){
                                    if(stmp.d1!=0){
                                       String err="Este identificador es un arreglo, solo se aceptan variables simples: "+idKey;
                                       poneError(3,err);
                                    }else
                                       if(stmp.clase=='P'||stmp.clase=='F'){
                                          String err="Este identificador es un modulo, solo se aceptan variables simples: "+idKey;
                                          poneError(3,err);
                                       }else{
                                          pilaTipos.pop();
                                          pilaTipos.push(stmp.tipo_dato);
                                       }
                                 }
                              }
                           }
                        }else{
                           //Busca en locales:
                           Simbolo stmp=(Simbolo)tabSim.get(localKey);
                           if(stmp!=null){
                              if(stmp.d1!=0){
                                 String err="Este identificador es un arreglo, solo se aceptan variables simples: "+idKey;
                                 poneError(3,err);
                              }else
                                 if(stmp.clase=='P'||stmp.clase=='F'){
                                    String err="Este identificador es un modulo, solo se aceptan variables simples: "+idKey;
                                    poneError(3,err);
                                 }else{
                                    pilaTipos.pop();
                                    pilaTipos.push(stmp.tipo_dato);
                                 }
                           }
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     idKey="";
                     tipo="<Ninguno>";
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDependiendo(father,0);
                  }
            }break;
            case 145:{
                  if(tipoElem==2)
                     estate=146;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDependiendo(father,2);
                  }
            }break;
            case 146:{
                  if(lexema.equals("CASO"))
                     estate=147;
                  else{
                     String err="Se esperaba la palabra \"CASO\" y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDependiendo(father,2);
                  }
            }break;
            case 147:{
                  //*** Generacion de codigo ***
                  ControlStruct dependiendo=null;
                  if(!pilaStructs.empty()){
                     dependiendo=(ControlStruct)pilaStructs.peek();
                     if(dependiendo.nombre.equals("Dependiendo")){
                        codGen.escribePL0("LOD  "+dependiendo.ident+",0");
                     }else{
                        System.out.println("ERROR en la pila de estructuras: Dependiendo, en la linea: "+lineCounter);
                        dependiendo=null;
                     }
                  }else
                     System.out.println("ERROR en la pila de estructuras: Dependiendo, en la linea: "+lineCounter);
                  // Fin de generacion de codigo
                  
                  if(tipoElem==7||tipoElem==5||tipoElem==9||tipoElem==10||tipoElem==11){
                     estate=148;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     tipo=(String)pilaTipos.peek();
                     if(tipoElem==7){
                        //Es un identificador, revisar que sea una constante:
                        if(modulo!=""){
                           String localKey=lexema+"@"+modulo;
                           if(!tabSim.containsKey(localKey)){
                              //Busca en globales:
                              if(!tabSim.containsKey(lexema)){
                                 String err="No est? definida una constante con ese nombre: "+lexema;
                                 poneError(3,err);
                              }else{
                                 Simbolo stmp=(Simbolo)tabSim.get(lexema);
                                 if(stmp.clase!='C'){
                                    String err="Este identificador no es una constante: "+lexema;
                                    poneError(3,err);
                                 }else{
                                    //Generacion de codigo:
                                    codGen.escribePL0("LIT  "+stmp.cspcfyp+",0");
                                    
                                    //Revisa que sea del mismo tipo:
                                    if(!stmp.tipo_dato.equals(tipo)){
                                       if(tipo.equals("<Real>")){
                                          if(!stmp.tipo_dato.equals("<Entero>")){
                                             String err="Se esperaba un "+tipo+" y se encontro un: "+stmp.tipo_dato;
                                             poneError(3,err);
                                          }
                                       }else{
                                          String err="Se esperaba un "+tipo+" y se encontro un: "+stmp.tipo_dato;
                                          poneError(3,err);
                                       }
                                    }
                                 }
                              }
                           }else{
                              String err="Este identificador no es una constante: "+lexema;
                              poneError(3,err);
                           }
                        }
                     }else{
                        //Generacion de codigo:
                        codGen.escribePL0("LIT  "+lexema+",0");
                        
                        //Es un valor, revisar que sea del mismo tipo:
                        if(!token.equals(tipo)){
                           if(tipo.equals("<Real>")){
                              if(!token.equals("<Entero>")){
                                 String err="Se esperaba un "+tipo+" y se encontro un: "+token;
                                 poneError(3,err);
                              }
                           }else{
                              String err="Se esperaba un "+tipo+" y se encontro un: "+token;
                              poneError(3,err);
                           }
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     String err="Se esperaba un identificador o un valor y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDependiendo(father,2);
                  }
                  
                  //*** Generacion de codigo ***
                  codGen.escribePL0("OPR  0,15");
                  if(dependiendo!=null){
                     dependiendo.EtY="$Label"+codGen.etCont;
                  }
                  codGen.escribeEt("JMC  F,$Label");
                  // Fin de generacion de codigo
            }break;
            case 148:{
                  //*** Generacion de codigo ***
                  ControlStruct dependiendo=null;
                  if(!pilaStructs.empty()){
                     dependiendo=(ControlStruct)pilaStructs.peek();
                     if(!dependiendo.nombre.equals("Dependiendo")){
                        System.out.println("ERROR en la pila de estructuras: Dependiendo, en la linea: "+lineCounter);
                        dependiendo=null;
                     }
                  }else
                     System.out.println("ERROR en la pila de estructuras: Dependiendo, en la linea: "+lineCounter);
                  // Fin de generacion de codigo
                  
                  if(lexema.equals("|")){
                     estate=147;
                     //*** Generacion de codigo ***
                     if(dependiendo!=null){
                        if(dependiendo.EtA==""){
                           dependiendo.EtA="$Label"+codGen.etCont;
                           codGen.escribeEt("JMP  0,$Label");
                        }else
                           codGen.escribePL0("JMP  0,"+dependiendo.EtA);
                        //Resuelve la etiqueta:
                        Simbolo etY=new Simbolo(dependiendo.EtY,'N',"<Ninguno>","");
                        etY.d1=codGen.contador+1;
                        tabSim.put(dependiendo.EtY,etY);
                     }
                     // Fin de generacion de codigo
                  }else{
                     //*** Generacion de codigo ***
                     if(dependiendo!=null){
                        //Resuelve la etiqueta:
                        if(dependiendo.EtA!=""){
                           Simbolo etA=new Simbolo(dependiendo.EtA,'N',"<Ninguno>","");
                           etA.d1=codGen.contador+1;
                           tabSim.put(dependiendo.EtA,etA);
                           dependiendo.EtA="";
                        }
                     }
                     // Fin de generacion de codigo
                     
                     //Bloque
                     infoStates.push(new Integer(149));
                     padresState.push(new Integer(149));
                     estate=85;
                     reuse=true;
                     nivel++;
                  }
            }break;
            case 149:{
                  //*** Generacion de codigo ***
                  if(!pilaStructs.empty()){
                     ControlStruct dependiendo=(ControlStruct)pilaStructs.peek();
                     if(dependiendo.nombre.equals("Dependiendo")){
                        if(dependiendo.EtX==""){
                           dependiendo.EtX="$Label"+codGen.etCont;
                           codGen.escribeEt("JMP  0,$Label");
                        }else
                           codGen.escribePL0("JMP  0,"+dependiendo.EtX);
                        //Resuelve la etiqueta:
                        Simbolo etY=new Simbolo(dependiendo.EtY,'N',"<Ninguno>","");
                        etY.d1=codGen.contador+1;
                        tabSim.put(dependiendo.EtY,etY);
                     }else
                        System.out.println("ERROR en la pila de estructuras: Dependiendo, en la linea: "+lineCounter);
                  }else
                     System.out.println("ERROR en la pila de estructuras: Dependiendo, en la linea: "+lineCounter);
                  // Fin de generacion de codigo
                  
                  if(tipoElem==13)
                     estate=150;
                  else
                     if(tipoElem==3)
                        estate=154;
                     else{
                        String err="Se esperaba \";\" ? \"]\" y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int father=((Integer)padresState.peek()).intValue();
                        estate=mError.recuperaDependiendo(father,2);
                     }
            }break;
            case 150:{
                  if(lexema.equals("CASO"))
                     estate=147;
                  else
                     if(lexema.equals("CUALQUIER"))
                        estate=151;
                     else{
                        String err="Se esperaba la palabra \"CASO\" ? \"CUALQUIER\" y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int father=((Integer)padresState.peek()).intValue();
                        estate=mError.recuperaDependiendo(father,2);
                     }
            }break;
            case 151:{
                  if(lexema.equals("OTRO"))
                     estate=152;
                  else{
                     String err="Se esperaba la palabra \"OTRO\" y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDependiendo(father,2);
                  }
            }break;
            case 152:{
                  //Bloque
                  infoStates.push(new Integer(153));
                  padresState.push(new Integer(149));
                  estate=85;
                  reuse=true;
                  nivel++;
            }break;
            case 153:{
                  if(tipoElem==3)
                     estate=154;
                  else{
                     String err="Se esperaba un corchete de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDependiendo(father,2);
                  }
            }break;
            case 154:{
                  pilaTipos.pop();
                  
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
                  
                  //*** Generacion de codigo ***
                  if(!pilaStructs.empty()){
                     ControlStruct dependiendo=(ControlStruct)pilaStructs.pop();
                     if(dependiendo.nombre.equals("Dependiendo")){
                        //Resuelve la etiqueta:
                        Simbolo etX=new Simbolo(dependiendo.EtX,'N',"<Ninguno>","");
                        etX.d1=codGen.contador+1;
                        tabSim.put(dependiendo.EtX,etX);
                     }else
                        System.out.println("ERROR en la pila de estructuras: Dependiendo, en la linea: "+lineCounter);
                  }else
                     System.out.println("ERROR en la pila de estructuras: Dependiendo, en la linea: "+lineCounter);
                  // Fin de generacion de codigo
            }break;
            //----------------------------- Fin de Dependiendo -------------------------
            
            //------------------------------ Asignacion Simple -------------------------
            case 230:{
                  if(lexema.equals("<-")){
                     estate=155;
                     pilaTipos.push("<Ninguno>");
                     pilaOp.push(new Operador(lexema,null));
                  }/*else{
                     //vacio
                  }*/
            }break;
            case 155:{
                  //Generacion de codigo:
                  idAsign=idKey;
                  //Regla_OR
                  infoStates.push(new Integer(156));
                  padresState.push(new Integer(156));
                  estate=240;
                  reuse=true;
                  nivel++;
            }break;
            case 156:{
                  if(!pilaPadres.empty())
                     pilaPadres.pop();
                  
                  //Generacion de codigo:
                  int cPos=idAsign.indexOf('@');
                  if(cPos!=-1)
                     idAsign=idAsign.substring(0,cPos);
                  codGen.escribePL0("STO  0,"+idAsign);
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(!pilaOp.empty()){
                     //String opr=(String)pilaOp.peek();
                     Operador opr=(Operador)pilaOp.peek();
                     if(opr.op.equals("<-")){
                        //System.out.println("Entrando a verificacion de asignacion en la linea: "+lineCounter);
                        VT.verAsign();
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
            }break;
            //--------------------------- Fin de Asignacion Simple ---------------------
            
            //----------------------------- Asignacion Registro ------------------------
            case 231:{
                  if(lexema.equals("["))
                     estate=157;
                  /*else{
                     //vacio
                  }*/
            }break;
            case 157:{
                  if(tipoElem==7){
                     estate=158;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Busca el elemento del registro en la tabla
                     if(idKey!=""&&modulo!=""){
                        String llave=idKey+"."+lexema;
                        if(esLocal){
                           llave=llave+"@"+modulo;
                        }
                        if(!tabSim.containsKey(llave)){
                           String local="";
                           if(esLocal)
                              local="local";
                           String err="No existe el elemento \""+lexema+"\" dentro del registro "+local+": \""+idKey+"\"";
                           poneError(3,err);
                           idKey="";
                        }else{
                           idKey=llave;
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Generacion de codigo:
                     idAsign=idKey;
                  }else{
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaAsignacionReg(father,0);
                  }
            }break;
            case 158:{
                  if(tipoElem==3)
                     estate=159;
                  else{
                     String err="Se esperaba un corchete de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaAsignacionReg(father,0);
                  }
            }break;
            case 159:{
                  //Generacion de codigo:
                  Padre P=new Padre(idKey,'S',null);
                  pilaPadres.push(P);
                  
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  Simbolo sim=(Simbolo)tabSim.get(idKey);
                  if(sim!=null){
                     pilaTipos.push(sim.tipo_dato);
                  }else
                     pilaTipos.push("<Ninguno>");
                  pilaOp.push(new Operador("<-",P));
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==19){
                     estate=160;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Busca el simbolo en la tabla y revisa que sea un arreglo
                     Simbolo stmp=(Simbolo)tabSim.get(idKey);
                     if(stmp!=null){
                        if(stmp.d1==0){
                           String err="Este registro no esta dimensionado: "+idKey;
                           editor.errCount++;
                           editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                           idKey="";
                        }
                     }
                     pilaPadres.push(new Padre(idKey,'A',null));
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==8){
                        estate=165;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        //Busca el simbolo en la tabla y revisa que no sea un arreglo
                        Simbolo stmp=(Simbolo)tabSim.get(idKey);
                        if(stmp!=null){
                           if(stmp.d1!=0){
                              String err="Este registro esta dimensionado, falto definir la posicion: "+idKey;
                              editor.errCount++;
                              editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                              idKey="";
                           }
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        idKey="";
                        String err="Se esperaba una llave de apertura ? \"<-\" y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int father=((Integer)padresState.peek()).intValue();
                        estate=mError.recuperaAsignacionReg(father,0);
                     }
            }break;
            case 160:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  solo=true;
                  dimVal="";
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Expresion
                  infoStates.push(new Integer(161));
                  padresState.push(new Integer(166));
                  estate=233;
                  reuse=true;
                  nivel++;
                  
                  /*if(tipoElem==9)
                     estate=161;
                  else{
                     String err="Se esperaba un entero y se encontr?: "+lexema;
                     editor.errCount++;
                     editor.errores(estado,2,err,lineCounter,i,lexema,win);
                  }*/
            }break;
            case 161:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  Padre tmp;
                  if(!pilaPadres.empty())
                     tmp=(Padre)pilaPadres.peek();
                  else
                     tmp=new Padre("@@@",'X',null);
                  //Revisa que el tipo de dato al final de la pila sea entero:
                  if(!pilaTipos.isEmpty()){
                     String tipoTmp=(String)pilaTipos.pop();
                     if(!tipoTmp.equals("<Entero>")){
                        String err="Se esperaba un valor de tipo <Entero> y se encontro uno de tipo "+tipoTmp+" al definir la posicion en el arreglo \""+tmp.idKey+"\".";
                        poneError2(3,err);
                     }else{
                        //Revisa que el valor est? en el rango:
                        if(solo){
                           //System.out.println("\nDimension de arreglo en la linea "+lineCounter);
                           try{
                              int valTmp=Integer.parseInt(dimVal);
                              Simbolo stmp=(Simbolo)tabSim.get(tmp.idKey);
                              //System.out.println("Valor: "+valTmp);
                              if(stmp!=null){
                                 if(valTmp<0||valTmp>stmp.d1-1){
                                    String err="Valor fuera de rango al definir la posicion en el arreglo \""+tmp.idKey+"\": "+valTmp;
                                    poneError2(3,err);
                                 }
                              }
                           }catch(NumberFormatException nfe){}
                        }
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==20){
                     estate=164;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa que no sea un arreglo de dos dimensiones
                     String tmpKey=tmp.idKey;
                     Simbolo stmp=(Simbolo)tabSim.get(tmpKey);
                     if(stmp!=null){
                        if(stmp.d2!=0){
                           String err="Este arreglo es de dos dimensiones, falto definir la posicion en la segunda dimension: "+tmpKey;
                           editor.errCount++;
                           editor.errores(estado,3,err,lineCounter,i,"",win,win.nombre,win.ruta);
                           idKey="";
                        }
                     }
                     if(!pilaPadres.empty())
                        pilaPadres.pop();
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==4){
                        estate=162;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        //Revisa que sea un arreglo de dos dimensiones
                        String tmpKey=tmp.idKey;
                        Simbolo stmp=(Simbolo)tabSim.get(tmpKey);
                        if(stmp!=null){
                           if(stmp.d2==0){
                              String err="Este arreglo es de una dimension, no se puede definir una posicion en una segunda dimension: "+tmpKey;
                              editor.errCount++;
                              editor.errores(estado,3,err,lineCounter,i,"",win,win.nombre,win.ruta);
                              idKey="";
                           }
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        String err="Se esperaba una llave de cierre o una coma y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int father=((Integer)padresState.peek()).intValue();
                        estate=mError.recuperaAsignacionReg(father,8);
                     }
            }break;
            case 162:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  solo=true;
                  dimVal="";
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Expresion
                  infoStates.push(new Integer(163));
                  padresState.push(new Integer(166));
                  estate=233;
                  reuse=true;
                  nivel++;
                  
                  /*if(tipoElem==9)
                     estate=163;
                  else{
                     String err="Se esperaba un entero y se encontr?: "+lexema;
                     editor.errCount++;
                     editor.errores(estado,2,err,lineCounter,i,lexema,win);
                  }*/
            }break;
            case 163:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Revisa que el tipo de dato al final de la pila sea entero:
                  if(!pilaPadres.empty()){
                     Padre tmp=(Padre)pilaPadres.peek();
                     if(!pilaTipos.isEmpty()){
                        String tipoTmp=(String)pilaTipos.pop();
                        if(!tipoTmp.equals("<Entero>")){
                           String err="Se esperaba un valor de tipo <Entero> y se encontro uno de tipo "+tipoTmp+" al definir la posicion en el arreglo \""+tmp.idKey+"\".";
                           poneError2(3,err);
                        }else{
                           //Revisa que el valor est? en el rango:
                           if(solo){
                              try{
                                 int valTmp=Integer.parseInt(dimVal);
                                 Simbolo stmp=(Simbolo)tabSim.get(tmp.idKey);
                                 if(stmp!=null){
                                    if(valTmp<0||valTmp>stmp.d2-1){
                                       String err="Valor fuera de rango al definir la posicion en el arreglo \""+tmp.idKey+"\": "+valTmp;
                                       poneError2(3,err);
                                    }
                                 }
                              }catch(NumberFormatException nfe){}
                           }
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     pilaPadres.pop();
                  }
                  if(tipoElem==20)
                     estate=164;
                  else{
                     String err="Se esperaba una llave de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaAsignacionReg(father,8);
                  }
            }break;
            case 164:{
                  if(tipoElem==8)
                     estate=165;
                  else{
                     String err="Se esperaba el operador de asignaci?n y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaAsignacionReg(father,8);
                  }
            }break;
            case 165:{
                  //Regla_OR
                  infoStates.push(new Integer(166));
                  padresState.push(new Integer(166));
                  estate=240;
                  reuse=true;
                  nivel++;
            }break;
            case 166:{
                  if(!pilaPadres.empty())
                     pilaPadres.pop();
                  
                  //Generacion de codigo:
                  int cPos=idAsign.indexOf('@');
                  if(cPos!=-1)
                     idAsign=idAsign.substring(0,cPos);
                  codGen.escribePL0("STO  0,"+idAsign);
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(!pilaOp.empty()){
                     //String opr=(String)pilaOp.peek();
                     Operador opr=(Operador)pilaOp.peek();
                     if(opr.op.equals("<-")){
                        //System.out.println("Entrando a verificacion de asignacion en la linea: "+lineCounter);
                        VT.verAsign();
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
            }break;
            //-------------------------- Fin de Asignacion Registro --------------------
            
            //----------------------------- Asignacion Arreglo -------------------------
            case 232:{
                  if(lexema.equals("{"))
                     estate=167;
                  /*else{
                     //vacio
                  }*/
            }break;
            case 167:{
                  //Generacion de codigo:
                  Padre P=new Padre(idKey,'S',null);
                  pilaPadres.push(P);
                  
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  Simbolo sim=(Simbolo)tabSim.get(idKey);
                  if(sim!=null){
                     pilaTipos.push(sim.tipo_dato);
                  }else
                     pilaTipos.push("<Ninguno>");
                  pilaOp.push(new Operador("<-",P));
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==20)
                     estate=174;
                  else{
                     //Generacion de codigo:
                     idAsign=idKey;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     solo=true;
                     dimVal="";
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Expresion
                     infoStates.push(new Integer(168));
                     padresState.push(new Integer(173));
                     estate=233;
                     reuse=true;
                     nivel++;
                     
                     pilaPadres.push(new Padre(idKey,'A',null));
                  }
            }break;
            case 168:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  Padre tmp=(Padre)pilaPadres.peek();
                  //Revisa que el tipo de dato al final de la pila sea entero:
                  if(!pilaTipos.isEmpty()){
                     String tipoTmp=(String)pilaTipos.pop();
                     if(!tipoTmp.equals("<Entero>")){
                        String err="Se esperaba un valor de tipo <Entero> y se encontro uno de tipo "+tipoTmp+" al definir la posicion en el arreglo \""+tmp.idKey+"\".";
                        poneError2(3,err);
                     }else{
                        //Revisa que el valor est? en el rango:
                        if(solo){
                           //System.out.println("\nDimension de arreglo en la linea "+lineCounter);
                           try{
                              int valTmp=Integer.parseInt(dimVal);
                              Simbolo stmp=(Simbolo)tabSim.get(tmp.idKey);
                              //System.out.println("Valor: "+valTmp);
                              if(stmp!=null){
                                 if(valTmp<0||valTmp>stmp.d1-1){
                                    String err="Valor fuera de rango al definir la posicion en el arreglo \""+tmp.idKey+"\": "+valTmp;
                                    poneError2(3,err);
                                 }
                              }
                           }catch(NumberFormatException nfe){}
                        }
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==20){
                     estate=171;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa que no sea un arreglo de dos dimensiones
                     String tmpKey=tmp.idKey;
                     Simbolo stmp=(Simbolo)tabSim.get(tmpKey);
                     if(stmp!=null){
                        if(stmp.d2!=0){
                           String err="Este arreglo es de dos dimensiones, falto definir la posicion en la segunda dimension: "+tmpKey;
                           editor.errCount++;
                           editor.errores(estado,3,err,lineCounter,i,"",win,win.nombre,win.ruta);
                           idKey="";
                        }
                     }
                     if(!pilaPadres.empty())
                        pilaPadres.pop();
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==4){
                        estate=169;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        //Revisa que sea un arreglo de dos dimensiones
                        String tmpKey=tmp.idKey;
                        Simbolo stmp=(Simbolo)tabSim.get(tmpKey);
                        if(stmp!=null){
                           if(stmp.d2==0){
                              String err="Este arreglo es de una dimension, no se puede definir una posicion en una segunda dimension: "+tmpKey;
                              editor.errCount++;
                              editor.errores(estado,3,err,lineCounter,i,"",win,win.nombre,win.ruta);
                              idKey="";
                           }
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        String err="Se esperaba una llave de cierre o una coma y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int father=((Integer)padresState.peek()).intValue();
                        estate=mError.recuperaAsignacionArr(father,8);
                     }
            }break;
            case 169:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  solo=true;
                  dimVal="";
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Expresion
                  infoStates.push(new Integer(170));
                  padresState.push(new Integer(173));
                  estate=233;
                  reuse=true;
                  nivel++;
                  
                  /*if(tipoElem==9)
                     estate=170;
                  else{
                     String err="Se esperaba un entero y se encontr?: "+lexema;
                     editor.errCount++;
                     editor.errores(estado,2,err,lineCounter,i,lexema,win);
                  }*/
            }break;
            case 170:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Revisa que el tipo de dato al final de la pila sea entero:
                  Padre tmp=(Padre)pilaPadres.peek();
                  if(!pilaTipos.isEmpty()){
                     String tipoTmp=(String)pilaTipos.pop();
                     if(!tipoTmp.equals("<Entero>")){
                        String err="Se esperaba un valor de tipo <Entero> y se encontro uno de tipo "+tipoTmp+" al definir la posicion en el arreglo \""+tmp.idKey+"\".";
                        poneError2(3,err);
                     }else{
                        //Revisa que el valor est? en el rango:
                        if(solo){
                           //System.out.println("\nDimension de arreglo en la linea "+lineCounter);
                           try{
                              int valTmp=Integer.parseInt(dimVal);
                              Simbolo stmp=(Simbolo)tabSim.get(tmp.idKey);
                              //System.out.println("Valor: "+valTmp);
                              if(stmp!=null){
                                 if(valTmp<0||valTmp>stmp.d2-1){
                                    String err="Valor fuera de rango al definir la posicion en el arreglo \""+tmp.idKey+"\": "+valTmp;
                                    poneError2(3,err);
                                 }
                              }
                           }catch(NumberFormatException nfe){}
                        }
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(!pilaPadres.empty())
                     pilaPadres.pop();
                  
                  if(tipoElem==20)
                     estate=171;
                  else{
                     String err="Se esperaba una llave de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaAsignacionArr(father,8);
                  }
            }break;
            case 171:{
                  if(tipoElem==8)
                     estate=172;
                  else{
                     String err="Se esperaba el operador de asignaci?n y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaAsignacionArr(father,8);
                  }
            }break;
            case 172:{
                  //Regla_OR
                  infoStates.push(new Integer(173));
                  padresState.push(new Integer(173));
                  estate=240;
                  reuse=true;
                  nivel++;
            }break;
            case 173:{
                  if(!pilaPadres.empty())
                     pilaPadres.pop();
                  
                  //Generacion de codigo:
                  int cPos=idAsign.indexOf('@');
                  if(cPos!=-1)
                     idAsign=idAsign.substring(0,cPos);
                  codGen.escribePL0("STO  0,"+idAsign);
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(!pilaOp.empty()){
                     //String opr=(String)pilaOp.peek();
                     Operador opr=(Operador)pilaOp.peek();
                     if(opr.op.equals("<-")){
                        //System.out.println("Entrando a verificacion de asignacion en la linea: "+lineCounter);
                        VT.verAsign();
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
            }break;
            case 174:{
                  if(tipoElem==8)
                     estate=175;
                  else{
                     String err="Se esperaba el operador de asignaci?n y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaAsignacionArr(father,29);
                  }
            }break;
            case 175:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  d1c=d2c=0;
                  if(!pilaTipos.isEmpty())
                     tipo=(String)pilaTipos.peek();
                  else
                     tipo="<Ninguno>";
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==2)
                     estate=176;
                  else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaAsignacionArr(father,19);
                  }
            }break;
            case 176:{
                  if(tipoElem==5||tipoElem==9||tipoElem==10||tipoElem==11||tipoElem==7||tipoElem==25||lexema.equals("+")||lexema.equals("-")){
                     //estate=177;
                     //Expresi?n
                     infoStates.push(new Integer(177));
                     padresState.push(new Integer(173));
                     estate=233;
                     reuse=true;
                     nivel++;
                     
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa que sea de una dimension
                     Simbolo sim=(Simbolo)tabSim.get(idKey);
                     if(sim!=null){
                        if(sim.d2!=0){
                           d1c=d2c=500;
                           String err="Este arreglo es de dos dimensiones, falt? definir los subarreglos: "+idKey;
                           editor.errCount++;
                           editor.errores(estado,3,err,lineCounter,i,"",win,win.nombre,win.ruta);
                        }
                        
                        //*** Generacion de codigo ***
                        codGen.escribePL0("LIT  "+d1c+",0");
                        // Fin de generacion de codigo
                        
                        d1c++;
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==2){
                        estate=178;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        //Revisa que sea de dos dimensiones
                        d1c++;
                        Simbolo sim=(Simbolo)tabSim.get(idKey);
                        if(sim!=null){
                           if(sim.d2==0){
                              d1c=d2c=500;
                              String err="Este arreglo es de una dimension, no se pueden definir subarreglos: "+idKey;
                              editor.errCount++;
                              editor.errores(estado,3,err,lineCounter,i,"",win,win.nombre,win.ruta);
                           }
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        String err="Se esperaba un corchete de apertura o una expresi?n y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int father=((Integer)padresState.peek()).intValue();
                        estate=mError.recuperaAsignacionArr(father,19);//8
                     }
            }break;
            case 177:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Revisa que el tipo de dato al final de la pila sea del mismo tipo del arreglo:
                  if(!pilaTipos.isEmpty()&&!pilaPadres.isEmpty()){
                     Padre tmp=(Padre)pilaPadres.peek();
                     tipo=(String)pilaTipos.pop();
                     
                     //Generacion de codigo:
                     String tId;
                     int cPos=tmp.idKey.indexOf('@');
                     if(cPos!=-1)
                        tId=tmp.idKey.substring(0,cPos);
                     else
                        tId=tmp.idKey;
                     codGen.escribePL0("STO  0,"+tId);
                     
                     if(!tipo.equals("<Ninguno>")){
                        Simbolo stmp=(Simbolo)tabSim.get(tmp.idKey);
                        if(stmp!=null){
                           if(!tipo.equals(stmp.tipo_dato)){
                              if(stmp.tipo_dato.equals("<Real>")){
                                 if(!tipo.equals("<Entero>")){
                                    String err="Se esperaba un "+stmp.tipo_dato+" y se encontr? un "+tipo+" al definir el elemento dentro del arreglo: \""+tmp.idKey+"\"";
                                    poneError2(3,err);
                                 }
                              }else{
                                 String err="Se esperaba un "+stmp.tipo_dato+" y se encontr? un "+tipo+" al definir el elemento dentro del arreglo: \""+tmp.idKey+"\"";
                                 poneError2(3,err);
                              }
                           }
                        }
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  
                  if(tipoElem==4)
                     estate=209;
                  else
                     if(tipoElem==3){
                        estate=182;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        if(!pilaPadres.isEmpty()){
                           Padre tmp=(Padre)pilaPadres.peek();
                        
                           Simbolo simb=(Simbolo)tabSim.get(tmp.idKey);
                           if(simb!=null){
                              if(d1c<simb.d1){
                                 String err="El arreglo se defini? para contener "+simb.d1+" elementos y solo se encontraron "+d1c+": "+tmp.idKey;
                                 poneError(3,err);
                              }
                              tmp.idKey="";
                           }
                        }
                        d1c=0;
                        d1=2;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        String err="Se esperaba un corchete de cierre o una coma y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int father=((Integer)padresState.peek()).intValue();
                        estate=mError.recuperaAsignacionArr(father,19);
                     }
            }break;
            case 209:{
                  if(tipoElem==5||tipoElem==9||tipoElem==10||tipoElem==11||tipoElem==7||tipoElem==25||lexema.equals("+")||lexema.equals("-")){
                     //estate=177;
                     //Expresi?n
                     infoStates.push(new Integer(177));
                     padresState.push(new Integer(173));
                     estate=233;
                     reuse=true;
                     nivel++;
                     
                     //*** Generacion de codigo ***
                     codGen.escribePL0("LIT  "+d1c+",0");
                     // Fin de generacion de codigo
                     
                     d1c++;
                     if(!pilaPadres.isEmpty()){
                        Padre tmp=(Padre)pilaPadres.peek();
                        Simbolo simb=(Simbolo)tabSim.get(tmp.idKey);
                        if(simb!=null){
                           if(d1c==simb.d1+1){
                              String err="El arreglo se defini? para contener solo "+simb.d1+" elementos: "+tmp.idKey;
                              poneError2(3,err);
                           }
                        }
                     }
                  }else{
                     String err="Se esperaba una expresi?n y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaAsignacionArr(father,19);
                  }
            }break;
            case 178:{
                  if(tipoElem==5||tipoElem==9||tipoElem==10||tipoElem==11||tipoElem==7||tipoElem==25||lexema.equals("+")||lexema.equals("-")){
                     //estate=179;
                     //Expresi?n
                     infoStates.push(new Integer(179));
                     padresState.push(new Integer(173));
                     estate=233;
                     reuse=true;
                     nivel++;
                     
                     //*** Generacion de codigo ***
                     int t1=d1c;
                     t1--;
                     codGen.escribePL0("LIT  "+t1+",0");
                     codGen.escribePL0("LIT  "+d2c+",0");
                     // Fin de generacion de codigo
                     
                     d2c++;
                     if(!pilaPadres.isEmpty()){
                        Padre tmp=(Padre)pilaPadres.peek();
                        Simbolo simb=(Simbolo)tabSim.get(tmp.idKey);
                        if(simb!=null){
                           if(d2c==simb.d2+1){
                              String err="El arreglo se defini? para contener solo "+simb.d2+" elementos por subarreglo: "+tmp.idKey;
                              poneError(3,err);
                           }
                        }
                     }
                  }else{
                     String err="Se esperaba una expresi?n y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaAsignacionArr(father,29);
                  }
            }break;
            case 179:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Revisa que el tipo de dato al final de la pila sea del mismo tipo del arreglo:
                  if(!pilaTipos.isEmpty()&&!pilaPadres.isEmpty()){
                     Padre tmp=(Padre)pilaPadres.peek();
                     tipo=(String)pilaTipos.pop();
                     
                     //Generacion de codigo:
                     String tId;
                     int cPos=tmp.idKey.indexOf('@');
                     if(cPos!=-1)
                        tId=tmp.idKey.substring(0,cPos);
                     else
                        tId=tmp.idKey;
                     codGen.escribePL0("STO  0,"+tId);
                     
                     if(!tipo.equals("<Ninguno>")){
                        Simbolo stmp=(Simbolo)tabSim.get(tmp.idKey);
                        if(stmp!=null){
                           if(!tipo.equals(stmp.tipo_dato)){
                              if(stmp.tipo_dato.equals("<Real>")){
                                 if(!tipo.equals("<Entero>")){
                                    String err="Se esperaba un "+stmp.tipo_dato+" y se encontr? un "+tipo+" al definir el elemento dentro del arreglo: \""+tmp.idKey+"\"";
                                    poneError2(3,err);
                                 }
                              }else{
                                 String err="Se esperaba un "+stmp.tipo_dato+" y se encontr? un "+tipo+" al definir el elemento dentro del arreglo: \""+tmp.idKey+"\"";
                                 poneError2(3,err);
                              }
                           }
                        }
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  
                  if(tipoElem==4)
                     estate=178;
                  else
                     if(tipoElem==3){
                        estate=181;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        if(!pilaPadres.isEmpty()){
                           Padre tmp=(Padre)pilaPadres.peek();
                           Simbolo simb=(Simbolo)tabSim.get(tmp.idKey);
                           if(simb!=null){
                              if(d2c<simb.d2){
                                 String err="El subarreglo se defini? para contener "+simb.d2+" elementos y solo se encontraron "+d2c;
                                 poneError(3,err);
                              }
                           }
                        }
                        d2c=0;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        String err="Se esperaba un corchete de cierre o una coma y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int father=((Integer)padresState.peek()).intValue();
                        estate=mError.recuperaAsignacionArr(father,29);
                     }
            }break;
            case 180:{
                  if(tipoElem==2){
                     estate=178;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     d1c++;
                     if(!pilaPadres.isEmpty()){
                        Padre tmp=(Padre)pilaPadres.peek();
                        Simbolo simb=(Simbolo)tabSim.get(tmp.idKey);
                        if(simb!=null){
                           if(d1c==simb.d1+1){
                              String err="El arreglo se defini? para contener solo "+simb.d1+" subarreglos: "+tmp.idKey;
                              poneError(3,err);
                           }
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     String err="Se esperaba un corchete de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaAsignacionArr(father,29);
                  }
            }break;
            case 181:{
                  if(tipoElem==3){
                     estate=182;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     if(!pilaPadres.isEmpty()){
                        Padre tmp=(Padre)pilaPadres.peek();
                        Simbolo simb=(Simbolo)tabSim.get(tmp.idKey);
                        if(simb!=null){
                           if(d1c<simb.d1){
                              String err="El arreglo se defini? para contener "+simb.d1+" subarreglos y solo se encontraron "+d1c;
                              poneError(3,err);
                           }
                        }
                     }
                     idKey="";
                     d1c=d2c=0;
                     d1=d2=2;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==4)
                        estate=180;
                     else{
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        idKey="";
                        d1c=d2c=0;
                        d1=d2=2;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        String err="Se esperaba un corchete de cierre o una coma y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int father=((Integer)padresState.peek()).intValue();
                        estate=mError.recuperaAsignacionArr(father,29);
                     }
            }break;
            case 182:{
                  if(!pilaPadres.empty())
                     pilaPadres.pop();
                  
                  pilaTipos.pop();
                  pilaOp.pop();
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
            }break;
            //-------------------------- Fin de Asignacion Arreglo ---------------------
            
            //----------------------------------- Leer ---------------------------------
            case 183:{
                  if(tipoElem==25)
                     estate=184;
                  else{
                     String err="Se esperaba un parentesis de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaLeer(father,0);
                  }
            }break;
            case 184:{
                  if(tipoElem==7){
                     estate=185;
                     idKey=lexema;
                  }else{
                     idKey="";
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaLeer(father,0);
                  }
            }break;
            case 185:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==4||tipoElem==26){
                     //Generacion de codigo:
                     //System.out.println("Generacion de c?digo en la linea "+lineCounter);
                     codGen.escribePL0("OPR  "+idKey+",19");
                     
                     //Busca el simbolo en la tabla
                     if(idKey!=""){
                        String localKey=idKey+"@"+modulo;
                        if(!tabSim.containsKey(localKey)){
                           //Busca que no sea un registro local:
                           boolean esta=false;
                           String prefijo=idKey+".";
                           for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                              String keyTmp=(String)e.nextElement();
                              String posfijo="@"+modulo;
                              if(keyTmp.endsWith(posfijo)){
                                 if(keyTmp.startsWith(prefijo))
                                    esta=true;
                              }
                           }
                           if(esta){
                              String err="\""+idKey+"\" es un registro local, falt? definir el elemento dentro del registro a usar.";
                              editor.errCount++;
                              editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                           }else{
                              //Busca en globales:
                              if(!tabSim.containsKey(idKey)){
                                 //Busca que no sea un registro:
                                 esta=false;
                                 for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                    String keyTmp=(String)e.nextElement();
                                    if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1){
                                       esta=true;
                                    }
                                 }
                                 if(esta){
                                    String err="\""+idKey+"\" es un registro, falt? definir el elemento dentro del registro a usar.";
                                    editor.errCount++;
                                    editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                 }else{
                                    String err="No est? definido un identificador con ese nombre: "+idKey;
                                    editor.errCount++;
                                    editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                 }
                              }else{
                                 Simbolo stmp=(Simbolo)tabSim.get(idKey);
                                 if(stmp.clase=='C'){
                                    String err="Las constantes no se pueden usar para leer: "+idKey;
                                    editor.errCount++;
                                    editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                 }else{
                                    if(stmp.d1!=0){
                                       String err="Este identificador es un arreglo, falto definir la posicion en el arreglo: "+idKey;
                                       editor.errCount++;
                                       editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                    }else
                                       if(stmp.clase=='P'||stmp.clase=='F'){
                                          String err="Este identificador es un modulo, no se puede usar para leer: "+idKey;
                                          editor.errCount++;
                                          editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                       }
                                 }
                              }
                           }
                        }else{
                           //Busca en locales:
                           Simbolo stmp=(Simbolo)tabSim.get(localKey);
                           if(stmp.d1!=0){
                              String err="Este identificador es un arreglo, falto definir la posicion en el arreglo: "+idKey;
                              editor.errCount++;
                              editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                           }else
                              if(stmp.clase=='P'||stmp.clase=='F'){
                                 String err="Este identificador es un modulo, no se puede usar para leer: "+idKey;
                                 editor.errCount++;
                                 editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                              }
                        }
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  
                  if(tipoElem==2){
                     estate=186;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Busca si hay algun registro con ese nombre:
                     boolean esta=false;
                     String prefijo=idKey+".";
                     //Busca primero en registros locales:
                     for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                        String keyTmp=(String)e.nextElement();
                        String posfijo="@"+modulo;
                        if(keyTmp.endsWith(posfijo)){
                           if(keyTmp.startsWith(prefijo)){
                              esta=true;
                              esLocal=true;
                           }
                        }
                     }
                     if(!esta&&idKey!=""){
                        //Busca luego en registros globales:
                        for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                           String keyTmp=(String)e.nextElement();
                           if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1){
                              esta=true;
                              esLocal=false;
                           }
                        }
                        if(!esta&&idKey!=""){
                           String err="No existe un registro con ese nombre: "+idKey;
                           editor.errCount++;
                           editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                           idKey="";
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==4){
                        estate=184;
                     }else
                        if(tipoElem==26){
                           estate=190;
                        }else
                           if(tipoElem==19){
                              //Dimensiones_Identificador
                              infoStates.push(new Integer(189));
                              padresState.push(new Integer(189));
                              estate=191;
                              reuse=true;
                              nivel++;
                              
                              //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                              //Busca el simbolo en la tabla y revisa que sea un arreglo
                              if(idKey!=""){
                                 //Busca primero en arreglos locales:
                                 String localKey=idKey+"@"+modulo;
                                 if(!tabSim.containsKey(localKey)){
                                    if(!tabSim.containsKey(idKey)){
                                       String err="No est? definido un arreglo con ese nombre: "+idKey;
                                       editor.errCount++;
                                       editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                       idKey="";
                                    }else{
                                       Simbolo stmp=(Simbolo)tabSim.get(idKey);
                                       if(stmp.d1==0){
                                          String err="Este identificador no es un arreglo: "+idKey;
                                          editor.errCount++;
                                          editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                          idKey="";
                                       }
                                    }
                                 }else{
                                    Simbolo stmp=(Simbolo)tabSim.get(localKey);
                                    if(stmp.d1==0){
                                       String err="Este identificador no es un arreglo: "+idKey;
                                       editor.errCount++;
                                       editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                       idKey="";
                                    }else
                                       idKey=localKey;
                                 }
                              }
                              pilaPadres.push(new Padre(idKey,'A',null));
                              //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                           }else{
                              idKey="";
                              String err="Se esperaba \"[\" ? \",\" ? \"{\" ? \")\" y se encontr?: "+lexema;
                              poneError(2,err);
                              
                              int father=((Integer)padresState.peek()).intValue();
                              estate=mError.recuperaLeer(father,0);
                           }
            }break;
            case 186:{
                  if(tipoElem==7){
                     estate=187;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Busca el elemento del registro en la tabla en la tabla
                     if(idKey!=""){
                        String llave=idKey+"."+lexema;
                        if(esLocal){
                           llave=llave+"@"+modulo;
                        }
                        if(!tabSim.containsKey(llave)){
                           String local="";
                           if(esLocal)
                              local="local";
                           String err="No existe el elemento \""+lexema+"\" dentro del registro "+local+": \""+idKey+"\"";
                           poneError(3,err);
                           idKey="";
                        }else{
                           idKey=llave;
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     idKey="";
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaLeer(father,0);
                  }
            }break;
            case 187:{
                  if(tipoElem==3)
                     estate=188;
                  else{
                     String err="Se esperaba un corchete de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaLeer(father,0);
                  }
            }break;
            case 188:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==4||tipoElem==26){
                     //Generacion de codigo:
                     String idTmp;
                     if(idKey.indexOf('@')!=-1)
                        idTmp=idKey.substring(0,idKey.indexOf('@'));
                     else
                        idTmp=idKey;
                     codGen.escribePL0("OPR  "+idTmp+",19");
                     
                     //Busca el simbolo en la tabla y revisa que no sea un arreglo
                     Simbolo stmp=(Simbolo)tabSim.get(idKey);
                     if(stmp!=null){
                        if(stmp.d1!=0){
                           String err="Este registro esta dimensionado, falto definir la posicion: "+idKey;
                           editor.errCount++;
                           editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                           idKey="";
                        }
                     }
                     idKey="";
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==4){
                     estate=184;
                  }else
                     if(tipoElem==26){
                        estate=190;
                     }else
                        if(tipoElem==19){
                           //Dimensiones_Identificador
                           infoStates.push(new Integer(189));
                           padresState.push(new Integer(189));
                           estate=191;
                           reuse=true;
                           nivel++;
                           
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                           //Busca el simbolo en la tabla y revisa que sea un arreglo
                           Simbolo stmp=(Simbolo)tabSim.get(idKey);
                           if(stmp!=null){
                              if(stmp.d1==0){
                                 String err="Este registro no esta dimensionado: "+idKey;
                                 editor.errCount++;
                                 editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                 idKey="";
                              }
                           }
                           pilaPadres.push(new Padre(idKey,'A',null));
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        }else{
                           idKey="";
                           String err="Se esperaba \",\" ? \"{\" ? \")\" y se encontr?: "+lexema;
                           poneError(2,err);
                           
                           int father=((Integer)padresState.peek()).intValue();
                           estate=mError.recuperaLeer(father,0);
                        }
            }break;
            case 189:{
                  //*** Generacion de codigo:
                  String idTmp;
                  if(idKey.indexOf('@')!=-1)
                     idTmp=idKey.substring(0,idKey.indexOf('@'));
                  else
                     idTmp=idKey;
                  codGen.escribePL0("OPR  "+idTmp+",19");
                  //Fin Generacion de codigo
                  
                  idKey="";
                  if(tipoElem==4)
                     estate=184;
                  else
                     if(tipoElem==26)
                        estate=190;
                     else{
                        String err="Se esperaba \",\" ? \")\" y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int father=((Integer)padresState.peek()).intValue();
                        estate=mError.recuperaLeer(father,0);
                     }
            }break;
            case 190:{
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
            }break;
            //-------------------------------- Fin de Leer -----------------------------
            
            //-------------------------- Dimensiones Identificador ---------------------
            case 191:{
                  if(tipoElem==19)
                     estate=192;
                  else{
                     String err="Se esperaba una llave de apertura y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDimensiones_Ident(father,0);
                  }
            }break;
            case 192:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  solo=true;
                  dimVal="";
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Expresion
                  infoStates.push(new Integer(193));
                  padresState.push(new Integer(193));
                  estate=233;
                  reuse=true;
                  nivel++;
            }break;
            case 193:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  Padre tmp=null;
                  if(!pilaPadres.isEmpty()){
                     tmp=(Padre)pilaPadres.peek();
                     //Revisa que el tipo de dato al final de la pila sea entero:
                     if(!pilaTipos.isEmpty()){
                        String tipoTmp=(String)pilaTipos.pop();
                        if(!tipoTmp.equals("<Entero>")){
                           String err="Se esperaba un valor de tipo <Entero> y se encontro uno de tipo "+tipoTmp+" al definir la posicion en el arreglo \""+tmp.idKey+"\".";
                           poneError2(3,err);
                        }else{
                           //Revisa que el valor est? en el rango:
                           if(solo){
                              try{
                                 int valTmp=Integer.parseInt(dimVal);
                                 Simbolo stmp=(Simbolo)tabSim.get(tmp.idKey);
                                 if(stmp!=null){
                                    if(valTmp<0||valTmp>stmp.d1-1){
                                       String err="Valor fuera de rango al definir la posicion en el arreglo \""+tmp.idKey+"\": "+valTmp;
                                       poneError2(3,err);
                                    }
                                 }
                              }catch(NumberFormatException nfe){}
                           }
                        }
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==20){
                     estate=196;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Revisa que no sea un arreglo de dos dimensiones
                     /*if(idKey!=""){
                        Simbolo stmp=(Simbolo)tabSim.get(idKey);
                        if(stmp.d2!=0){
                           String err="Este arreglo es de dos dimensiones, falto definir la posicion en la segunda dimension: "+idKey;
                           editor.errCount++;
                           editor.errores(estado,3,err,lineCounter,i,"",win,win.nombre,win.ruta);
                           idKey="";
                        }
                     }*/
                     if(!pilaPadres.isEmpty()){
                        String tmpKey=tmp.idKey;
                        Simbolo stmp=(Simbolo)tabSim.get(tmpKey);
                        if(stmp!=null){
                           idKey=stmp.nombre;
                           if(stmp.d2!=0){
                              String err="Este arreglo es de dos dimensiones, falto definir la posicion en la segunda dimension: "+idKey;
                              editor.errCount++;
                              editor.errores(estado,3,err,lineCounter,i,"",win,win.nombre,win.ruta);
                              idKey="";
                           }
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==4){
                        estate=194;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        //Revisa que sea un arreglo de dos dimensiones
                        /*if(idKey!=""){
                           Simbolo stmp=(Simbolo)tabSim.get(idKey);
                           if(stmp.d2==0){
                              String err="Este arreglo es de una dimension, no se puede definir una posicion en una segunda dimension: "+idKey;
                              editor.errCount++;
                              editor.errores(estado,3,err,lineCounter,i,"",win,win.nombre,win.ruta);
                              idKey="";
                           }
                        }*/
                        if(!pilaPadres.isEmpty()){
                           String tmpKey=tmp.idKey;
                           Simbolo stmp=(Simbolo)tabSim.get(tmpKey);
                           if(stmp!=null){
                              idKey=stmp.nombre;
                              if(stmp.d2==0){
                                 String err="Este arreglo es de una dimension, no se puede definir una posicion en una segunda dimension: "+idKey;
                                 editor.errCount++;
                                 editor.errores(estado,3,err,lineCounter,i,"",win,win.nombre,win.ruta);
                                 idKey="";
                              }
                           }
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else{
                        String err="Se esperaba una llave de cierre o una coma y se encontr?: "+lexema;
                        poneError(2,err);
                        
                        int father=((Integer)padresState.peek()).intValue();
                        estate=mError.recuperaDimensiones_Ident(father,9);
                     }
            }break;
            case 194:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  solo=true;
                  dimVal="";
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Expresion
                  infoStates.push(new Integer(195));
                  padresState.push(new Integer(193));
                  estate=233;
                  reuse=true;
                  nivel++;
            }break;
            case 195:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //Revisa que el tipo de dato al final de la pila sea entero:
                  if(!pilaPadres.empty()){
                     Padre tmp=(Padre)pilaPadres.peek();
                     if(!pilaTipos.isEmpty()){
                        String tipoTmp=(String)pilaTipos.pop();
                        if(!tipoTmp.equals("<Entero>")){
                           String err="Se esperaba un valor de tipo <Entero> y se encontro uno de tipo "+tipoTmp+" al definir la posicion en el arreglo \""+tmp.idKey+"\".";
                           poneError2(3,err);
                        }else{
                           //Revisa que el valor est? en el rango:
                           if(solo){
                              try{
                                 int valTmp=Integer.parseInt(dimVal);
                                 Simbolo stmp=(Simbolo)tabSim.get(tmp.idKey);
                                 if(stmp!=null){
                                    if(valTmp<0||valTmp>stmp.d2-1){
                                       String err="Valor fuera de rango al definir la posicion en el arreglo \""+tmp.idKey+"\": "+valTmp;
                                       poneError2(3,err);
                                    }
                                 }
                              }catch(NumberFormatException nfe){}
                           }
                        }
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==20)
                     estate=196;
                  else{
                     String err="Se esperaba una llave de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaDimensiones_Ident(father,9);
                  }
            }break;
            case 196:{
                  int next=((Integer)infoStates.pop()).intValue();
                  padresState.pop();
                  estate=next;
                  reuse=true;
                  nivel--;
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  Padre tmp;
                  /*if(!pilaPadres.empty())
                     pilaPadres.pop();*/
                  if(!pilaPadres.empty()){
                     tmp=(Padre)pilaPadres.pop();
                     idKey=tmp.idKey;
                  }
                  
                  //Revisa lo que hay en la pila por si es otro modulo:
                  if(!pilaPadres.isEmpty()){
                     tmp=(Padre)pilaPadres.peek();
                     if(tmp.params!=null)
                        params=tmp.params;
                     else
                        params.clear();
                     if(tmp.tipo=='M'){
                        enModulo=true;
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
            }break;
            //----------------------- Fin de Dimensiones Identificador -----------------
            
            //--------------------------------- Factor ------------------------------
            case 197:{
                  if(lexema.equals("+")||lexema.equals("-")){
                     estate=198;
                     //pilaOp.push(lexema+" unario");
                     Padre P=null;
                     if(!pilaPadres.empty())
                        P=(Padre)pilaPadres.peek();
                     pilaOp.push(new Operador(lexema+" unario",P));
                  }else
                     if(tipoElem==5||tipoElem==9||tipoElem==10||tipoElem==11){
                        estate=199;
                        //Generacion de codigo:
                        //System.out.println("Generacion de c?digo en la linea "+lineCounter);
                        if(tipoElem==11){
                           if(lexema.equals("VERDADERO"))
                              codGen.escribePL0("LIT  T,0");
                           else
                              codGen.escribePL0("LIT  F,0");
                        }else{
                           codGen.escribePL0("LIT  "+lexema+",0");
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        pilaTipos.push(token);
                        if(tipoElem==9)
                           dimVal=lexema;
                        
                        //Revisa que no se est? haciendo una divisi?n entre cero
                        if(!pilaOp.empty()&&!pilaPadres.empty()){
                           Operador tmpOp=(Operador)pilaOp.peek();
                           Padre P=(Padre)pilaPadres.peek();
                           if((tipoElem==9||tipoElem==10)&&P.equals(tmpOp.padre))
                              if(tmpOp.op.equals("/")||tmpOp.op.equals("DIV")||tmpOp.op.equals("MOD")){
                                 try{
                                    float numTmp=Float.parseFloat(lexema);
                                    if(numTmp==0.0f){
                                       String err="No se deben realizar divisiones entre cero.";
                                       poneError(3,err);
                                    }
                                 }catch(NumberFormatException nfe){}
                              }
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else
                        if(tipoElem==25){
                           estate=200;
                        }else
                           if(tipoElem==7){
                              estate=202;
                              //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                              idKey=lexema;
                              //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                           }else{
                              idKey="";
                              String err="Se esperaba \"+\" ? \"-\" ? \"(\" o un valor o un identificador y se encontr?: "+lexema;
                              poneError(2,err);
                              
                              int father=((Integer)padresState.peek()).intValue();
                              estate=mError.recuperaExpresion(father,0);
                           }
            }break;
            case 198:{
                  if(tipoElem==5||tipoElem==9||tipoElem==10||tipoElem==11){
                     estate=199;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     pilaTipos.push(token);
                     
                     //Revisa que no se est? haciendo una divisi?n entre cero
                     if(!pilaOp.empty()){
                        Operador opTmp=(Operador)pilaOp.pop();
                        if(!pilaOp.empty()&&!pilaPadres.empty()){
                           Operador tmpOp=(Operador)pilaOp.peek();
                           Padre P=(Padre)pilaPadres.peek();
                           if((tipoElem==9||tipoElem==10)&&P.equals(tmpOp.padre))
                              if(tmpOp.op.equals("/")||tmpOp.op.equals("DIV")||tmpOp.op.equals("MOD")){
                                 try{
                                    float numTmp=Float.parseFloat(lexema);
                                    if(numTmp==0.0f){
                                       String err="No se deben realizar divisiones entre cero.";
                                       poneError(3,err);
                                    }
                                 }catch(NumberFormatException nfe){}
                              }
                        }
                        pilaOp.push(opTmp);
                        
                        //aplica el signo si es necesario
                        if(tipoElem==9){
                           if(opTmp.op.equals("- unario"))
                              dimVal="-"+lexema;
                           else
                              dimVal=lexema;
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Generacion de codigo:
                     //System.out.println("Generacion de c?digo en la linea "+lineCounter);
                     if(tipoElem==11){
                        if(lexema.equals("VERDADERO"))
                           codGen.escribePL0("LIT  T,0");
                        else
                           codGen.escribePL0("LIT  F,0");
                     }else{
                        codGen.escribePL0("LIT  "+lexema+",0");
                     }
                  }else
                     if(tipoElem==7){
                        estate=202;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        idKey=lexema;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else
                        if(tipoElem==25)
                           estate=200;
                        else{
                           String err="Se esperaba \"(\" o un valor o un identificador y se encontr?: "+lexema;
                           poneError(2,err);
                           
                           int father=((Integer)padresState.peek()).intValue();
                           estate=mError.recuperaExpresion(father,3);
                        }
            }break;
            case 199:{
                  /*if(lexema.equals("+")||lexema.equals("-")||lexema.equals("*")||lexema.equals("/")||lexema.equals("DIV")||lexema.equals("MOD")||lexema.equals("POT"))
                     estate=197;
                  else{
                     int next=((Integer)infoStates.pop()).intValue();
                     padresState.pop();
                     estate=next;
                     reuse=true;
                     nivel--;
                  }*/
                  estate=238;
                  reuse=true;
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  //idKey="";
                  if(!pilaOp.empty()&&!pilaPadres.empty()){
                     Operador opr=(Operador)pilaOp.peek();
                     Padre P=(Padre)pilaPadres.peek();
                     if((opr.op.equals("+ unario")||opr.op.equals("- unario"))&&P.equals(opr.padre)){
                        VT.verFact();
                        
                        //Generacion de codigo
                        codGen.codeOpers(opr.op);
                     }
                  }
                  //Revisa si hay un operador para poner a solo en false
                  if(lexema.equals("+")||lexema.equals("-")||lexema.equals("*")||lexema.equals("/")||lexema.equals("DIV")||lexema.equals("MOD")||lexema.equals("POT"))
                     solo=false;
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
            }break;
            case 200:{
                  //Regla_OR
                  infoStates.push(new Integer(201));
                  padresState.push(new Integer(201));
                  estate=240;
                  reuse=true;
                  nivel++;
            }break;
            case 201:{
                  if(tipoElem==26)
                     estate=199;
                  else{
                     String err="Se esperaba un parentesis de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaExpresion(father,15);
                  }
            }break;
            case 202:{
                  if(tipoElem==25){
                     estate=203;
                     
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     params.clear();
                     //Revisar que sea una funci?n:
                     if(idKey!=""){
                        Simbolo stmp=(Simbolo)tabSim.get(idKey);
                        if(stmp!=null){
                           if(stmp.clase!='F'&&stmp.clase!='P'){
                              String err="Este identificador no es un modulo: "+idKey;
                              editor.errCount++;
                              editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                              idKey="";
                           }else{
                              if(stmp.clase=='P'){
                                 String err="Este identificador es un procedimiento, solo se pueden usar funciones: "+idKey;
                                 editor.errCount++;
                                 editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                              }
                              //Crea una lista temporal con los parametros que se esperan:
                              if(stmp.cspcfyp!=""){
                                 llenaParam(stmp);
                                 /*//Imprime:
                                 System.out.println("\nParametros:");
                                 for(int x=0;x<params.size();x++){
                                    Simbolo sTmp=(Simbolo)params.get(x);
                                    System.out.println(sTmp.tipo_dato+","+sTmp.d1+","+sTmp.d2);
                                 }*/
                              }
                           }
                        }else{
                           String err="No existe un modulo con ese nombre: "+idKey;
                           editor.errCount++;
                           editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                           idKey="";
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else
                     if(tipoElem==2){
                        estate=206;
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        if(idKey!=""&&modulo!=""){
                           //Busca si hay algun registro con ese nombre:
                           boolean esta=false;
                           String prefijo=idKey+".";
                           //Busca primero en registros locales:
                           for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                              String keyTmp=(String)e.nextElement();
                              String posfijo="@"+modulo;
                              if(keyTmp.endsWith(posfijo)){
                                 if(keyTmp.startsWith(prefijo)){
                                    esta=true;
                                    esLocal=true;
                                 }
                              }
                           }
                           if(!esta){
                              //Busca luego en registros globales:
                              for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                 String keyTmp=(String)e.nextElement();
                                 if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1){
                                    esta=true;
                                    esLocal=false;
                                 }
                              }
                              if(!esta){
                                 String err="No existe un registro con ese nombre: "+idKey;
                                 editor.errCount++;
                                 editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                 idKey="";
                              }
                           }
                        }
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                     }else
                        if(tipoElem==19){
                           //Dimensiones_Identificador
                           infoStates.push(new Integer(204));
                           padresState.push(new Integer(201));
                           estate=191;
                           reuse=true;
                           nivel++;
                           
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                           //Busca el simbolo en la tabla y revisa que sea un arreglo
                           if(idKey!=""&&modulo!=""){
                              //Busca primero en arreglos locales:
                              String localKey=idKey+"@"+modulo;
                              if(!tabSim.containsKey(localKey)){
                                 if(!tabSim.containsKey(idKey)){
                                    String err="No est? definido un arreglo con ese nombre: "+idKey;
                                    editor.errCount++;
                                    editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                    idKey="";
                                 }else{
                                    Simbolo stmp=(Simbolo)tabSim.get(idKey);
                                    if(stmp.d1==0){
                                       String err="Este identificador no es un arreglo: "+idKey;
                                       editor.errCount++;
                                       editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                       idKey="";
                                    }else{
                                       //Revisa que est? dentro de parametros y que las dimensiones sean las correctas
                                       revisaDims();
                                    }
                                 }
                              }else{
                                 Simbolo stmp=(Simbolo)tabSim.get(localKey);
                                 if(stmp.d1==0){
                                    String err="Este identificador no es un arreglo: "+idKey;
                                    editor.errCount++;
                                    editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                    idKey="";
                                 }else{
                                    idKey=localKey;
                                    //Revisa que est? dentro de parametros y que las dimensiones sean las correctas
                                    revisaDims();
                                 }
                              }
                           }
                           pilaPadres.push(new Padre(idKey,'A',null));
                           enModulo=false;
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        }else{
                           /*if(lexema.equals("+")||lexema.equals("-")||lexema.equals("*")||lexema.equals("/")||lexema.equals("DIV")||lexema.equals("MOD")||lexema.equals("POT"))
                              estate=197;
                           else{
                              int next=((Integer)infoStates.pop()).intValue();
                              padresState.pop();
                              estate=next;
                              reuse=true;
                              nivel--;
                           }*/
                           estate=238;
                           reuse=true;
                           
                           //Generacion de codigo:
                           String strCod="LOD  "+idKey+",0";
                           
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                           //El identificador esta solo, buscar que exista:
                           if(idKey!=""&&modulo!=""){
                              String localKey=idKey+"@"+modulo;
                              if(!tabSim.containsKey(localKey)){
                                 //Busca que no sea un registro local:
                                 boolean esta=false;
                                 String prefijo=idKey+".";
                                 for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                    String keyTmp=(String)e.nextElement();
                                    String posfijo="@"+modulo;
                                    if(keyTmp.endsWith(posfijo)){
                                       if(keyTmp.startsWith(prefijo))
                                          esta=true;
                                    }
                                 }
                                 if(esta){
                                    pilaTipos.push("<Ninguno>");
                                    String err="\""+idKey+"\" es un registro local, falt? definir el elemento dentro del registro a usar.";
                                    editor.errCount++;
                                    editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                 }else{
                                    //Busca en globales:
                                    if(!tabSim.containsKey(idKey)){
                                       //Busca que no sea un registro:
                                       esta=false;
                                       for(Enumeration e=tabSim.keys();e.hasMoreElements()&&!esta;){
                                          String keyTmp=(String)e.nextElement();
                                          if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1){
                                             esta=true;
                                          }
                                       }
                                       if(esta){
                                          pilaTipos.push("<Ninguno>");
                                          String err="\""+idKey+"\" es un registro, falt? definir el elemento dentro del registro a usar.";
                                          editor.errCount++;
                                          editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                       }else{
                                          pilaTipos.push("<Ninguno>");
                                          String err="No est? definido un identificador con ese nombre: "+idKey;
                                          editor.errCount++;
                                          editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                       }
                                    }else{
                                       Simbolo stmp=(Simbolo)tabSim.get(idKey);
                                       if(stmp.d1>0){
                                          //Revisa que est? dentro de parametros y que las dimensiones sean las correctas
                                          revisaDimensiones(stmp);
                                       }else{
                                          //Revisa que est? dentro de parametros y que las dimensiones sean las correctas
                                          if(!pilaPadres.isEmpty()&&enModulo&&argC[arrAC]<=params.size()){
                                             Simbolo sTmp=(Simbolo)params.get(argC[arrAC]-1);
                                             if(sTmp!=null){
                                                if(sTmp.d1!=stmp.d1||sTmp.d2!=stmp.d2){
                                                   Padre tmp=(Padre)pilaPadres.peek();
                                                   String err="El parametro dentro de \""+tmp.idKey+"\" debe ser un arreglo de las siguientes dimensiones: "+sTmp.d1+","+sTmp.d2;
                                                   editor.errCount++;
                                                   editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                                }else{
                                                   //Generacion de codigo:
                                                   if(sTmp.d1>0)
                                                      codGen.enviaArr(idKey,sTmp.d1,sTmp.d2);
                                                }
                                             }
                                          }
                                          if(stmp.clase=='P'||stmp.clase=='F'){
                                             pilaTipos.push("<Ninguno>");
                                             String err="Este identificador es un modulo, faltaron parentesis y que sea una funcion solamente: "+idKey;
                                             editor.errCount++;
                                             editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                          }else{
                                             //Todo salio bien, mete el tipo en la pila
                                             pilaTipos.push(stmp.tipo_dato);
                                             
                                             //Revisa si es una constante y mete su valor en un temporal:
                                             if(stmp.clase=='C'&&stmp.tipo_dato.equals("<Entero>")){
                                                dimVal=stmp.cspcfyp;
                                                if(!pilaOp.empty()&&!pilaPadres.empty()){
                                                   Operador opTmp=(Operador)pilaOp.peek();
                                                   Padre P=(Padre)pilaPadres.peek();
                                                   if(opTmp.op.equals("- unario")&&P.equals(opTmp.padre))
                                                      dimVal="-"+dimVal;
                                                }
                                             }
                                             //Revisa que no se est? haciendo una divisi?n entre cero
                                             if(stmp.clase=='C'){
                                                if(!pilaOp.empty()&&!pilaPadres.empty()){
                                                   Operador tmpOp=(Operador)pilaOp.peek();
                                                   Padre P=(Padre)pilaPadres.peek();
                                                   if((stmp.tipo_dato.equals("<Entero>")||stmp.tipo_dato.equals("<Real>"))&&P.equals(tmpOp.padre))
                                                      if(tmpOp.op.equals("/")||tmpOp.op.equals("DIV")||tmpOp.op.equals("MOD")){
                                                         try{
                                                            float numTmp=Float.parseFloat(stmp.cspcfyp);
                                                            if(numTmp==0.0f){
                                                               String err="No se deben realizar divisiones entre cero.";
                                                               poneError2(3,err);
                                                            }
                                                         }catch(NumberFormatException nfe){}
                                                      }
                                                }
                                             }
                                             //Generacion de codigo
                                             if(stmp.clase=='C'){
                                                if(stmp.cspcfyp.equals("VERDADERO"))
                                                   strCod="LIT  T,0";
                                                else if(stmp.cspcfyp.equals("FALSO"))
                                                   strCod="LIT  F,0";
                                                else
                                                   strCod="LIT  "+stmp.cspcfyp+",0";
                                             }
                                          }
                                       }
                                    }
                                 }
                              }else{
                                 //Busca en locales:
                                 Simbolo stmp=(Simbolo)tabSim.get(localKey);
                                 if(stmp.d1!=0){
                                    //Revisa que est? dentro de parametros y que las dimensiones sean las correctas
                                    revisaDimensiones(stmp);
                                 }else{
                                    //Revisa que est? dentro de parametros y que las dimensiones sean las correctas
                                    if(!pilaPadres.isEmpty()&&enModulo&&argC[arrAC]<=params.size()){
                                       Simbolo sTmp=(Simbolo)params.get(argC[arrAC]-1);
                                       if(sTmp!=null){
                                          if(sTmp.d1!=stmp.d1||sTmp.d2!=stmp.d2){
                                             Padre tmp=(Padre)pilaPadres.peek();
                                             String err="El parametro dentro de \""+tmp.idKey+"\" debe ser de las siguientes dimensiones: "+sTmp.d1+","+sTmp.d2;
                                             editor.errCount++;
                                             editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                          }else{
                                             //Generacion de codigo:
                                             if(sTmp.d1>0)
                                                codGen.enviaArr(idKey,sTmp.d1,sTmp.d2);
                                          }
                                       }
                                    }
                                    if(stmp.clase=='P'||stmp.clase=='F'){
                                       pilaTipos.push("<Ninguno>");
                                       String err="Este identificador es un modulo, faltaron parentesis y que sea una funcion solamente: "+idKey;
                                       editor.errCount++;
                                       editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                                    }else{
                                       //Todo salio bien, mete el tipo en la pila
                                       pilaTipos.push(stmp.tipo_dato);
                                    }
                                 }
                              }
                           }else
                              pilaTipos.push("<Ninguno>");
                           //idKey="";
                           
                           //Generacion de codigo:
                           //System.out.println("Generacion de c?digo en la linea "+lineCounter);
                           codGen.escribePL0(strCod);
                           
                           if(!pilaOp.empty()&&!pilaPadres.empty()){
                              Operador opr=(Operador)pilaOp.peek();
                              Padre P=(Padre)pilaPadres.peek();
                              if((opr.op.equals("+ unario")||opr.op.equals("- unario"))&&P.equals(opr.padre)){
                                 VT.verFact();
                                 
                                 //Generacion de codigo
                                 codGen.codeOpers(opr.op);
                              }
                           }
                           //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                        }
            }break;
            case 203:{
                  //*** Generacion de codigo ***
                  String tmpLabel="$Label"+codGen.etCont;
                  codGen.etCont++;
                  codGen.escribePL0("LOD  "+tmpLabel+",0");
                  pilaDirs.push(tmpLabel);
                  // Fin de generacion de codigo
                  
                  if(tipoElem==26){
                     estate=204;
                     
                     //*** Generacion de codigo ***
                     Proto pt=(Proto)tabProtos.get(idKey);
                     if(pt!=null){
                        codGen.escribePL0("CAL  "+idKey+","+pt.dir);
                        //Resuelve la etiqueta:
                        String tlbl="";
                        tlbl=(String)pilaDirs.pop();
                        Simbolo et=new Simbolo(tlbl,'N',"<Ninguno>","");
                        et.d1=codGen.contador+1;
                        tabSim.put(tlbl,et);
                     }
                     // Fin de generacion de codigo
                     
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Verifica que la funci?n no recibe parametros:
                     Simbolo stmp=(Simbolo)tabSim.get(idKey);
                     if(stmp!=null){
                        if(stmp.cspcfyp!=""){
                           String err="La funcion \""+idKey+"\" debe recibir "+params.size()+" parametros.";
                           editor.errCount++;
                           editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     //Parametros_Identificador
                     infoStates.push(new Integer(205));
                     padresState.push(new Integer(201));
                     estate=220;
                     reuse=true;
                     nivel++;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Verifica que la funci?n si recibe parametros:
                     Simbolo stmp=(Simbolo)tabSim.get(idKey);
                     if(stmp!=null){
                        if(stmp.cspcfyp.equals("")){
                           String err="La funcion \""+idKey+"\" no recibe parametros.";
                           poneError(3,err);
                           idKey="";
                        }
                        arrAC++;
                        argC[arrAC]=1;
                        
                        ArrayList lt=new ArrayList();
                        for(int a=0;a<params.size();a++){
                           lt.add(a,params.get(a));
                        }
                        pilaPadres.push(new Padre(idKey,'M',lt));
                        enModulo=true;
                     }else{
                        arrAC++;
                        argC[arrAC]=1;
                        pilaPadres.push(new Padre(idKey,'E',null));
                        enModulo=true;
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }
            }break;
            case 204:{
                  /*if(lexema.equals("+")||lexema.equals("-")||lexema.equals("*")||lexema.equals("/")||lexema.equals("DIV")||lexema.equals("MOD")||lexema.equals("POT"))
                     estate=197;
                  else{
                     int next=((Integer)infoStates.pop()).intValue();
                     padresState.pop();
                     estate=next;
                     reuse=true;
                     nivel--;
                  }*/
                  estate=238;
                  reuse=true;
                  
                  //Generacion de codigo:
                  String tId;
                  int cPos=idKey.indexOf('@');
                  if(cPos!=-1)
                     tId=idKey.substring(0,cPos);
                  else
                     tId=idKey;
                  codGen.escribePL0("LOD  "+tId+",0");
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  Simbolo stmp=(Simbolo)tabSim.get(idKey);
                  if(idKey!=""&&stmp!=null){
                     //Todo salio bien, mete el tipo en la pila
                     pilaTipos.push(stmp.tipo_dato);
                  }else
                     pilaTipos.push("<Ninguno>");
                  //idKey="";
                  
                  if(!pilaOp.empty()&&!pilaPadres.empty()){
                     Operador opr=(Operador)pilaOp.peek();
                     Padre P=(Padre)pilaPadres.peek();
                     if((opr.op.equals("+ unario")||opr.op.equals("- unario"))&&P.equals(opr.padre)){
                        VT.verFact();
                        
                        //Generacion de codigo
                        codGen.codeOpers(opr.op);
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
            }break;
            case 205:{
                  //*** Generacion de codigo ***
                  Proto pt=(Proto)tabProtos.get(idKey);
                  if(pt!=null){
                     codGen.escribePL0("CAL  "+idKey+","+pt.dir);
                     //Resuelve la etiqueta:
                     String tlbl="";
                     if(!pilaDirs.empty())
                        tlbl=(String)pilaDirs.pop();
                     Simbolo et=new Simbolo(tlbl,'N',"<Ninguno>","");
                     et.d1=codGen.contador+1;
                     tabSim.put(tlbl,et);
                  }
                  // Fin de generacion de codigo
                  
                  if(tipoElem==26){
                     estate=204;
                  }else{
                     String err="Se esperaba un parentesis de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaExpresion(father,15);
                  }
            }break;
            case 206:{
                  if(tipoElem==7){
                     estate=207;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Busca el elemento del registro en la tabla
                     if(idKey!=""){
                        String llave=idKey+"."+lexema;
                        if(esLocal){
                           llave=llave+"@"+modulo;
                        }
                        if(!tabSim.containsKey(llave)){
                           String local="";
                           if(esLocal)
                              local="local";
                           String err="No existe el elemento \""+lexema+"\" dentro del registro "+local+": \""+idKey+"\"";
                           poneError(3,err);
                           idKey="";
                        }else{
                           idKey=llave;
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     String err="Se esperaba un identificador y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaExpresion(father,12);
                  }
            }break;
            case 207:{
                  if(tipoElem==3)
                     estate=208;
                  else{
                     String err="Se esperaba un corchete de cierre y se encontr?: "+lexema;
                     poneError(2,err);
                     
                     int father=((Integer)padresState.peek()).intValue();
                     estate=mError.recuperaExpresion(father,12);
                  }
            }break;
            case 208:{
                  if(tipoElem==19){
                     //Dimensiones_Identificador
                     infoStates.push(new Integer(204));
                     padresState.push(new Integer(201));
                     estate=191;
                     reuse=true;
                     nivel++;
                     
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Busca el simbolo en la tabla y revisa que sea un arreglo
                     Simbolo stmp=(Simbolo)tabSim.get(idKey);
                     if(stmp!=null){
                        if(stmp.d1==0){
                           String err="Este registro no esta dimensionado: "+idKey;
                           editor.errCount++;
                           editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                           idKey="";
                        }else{
                           //Revisa que est? dentro de parametros y que las dimensiones sean las correctas
                           revisaDims();
                        }
                     }
                     pilaPadres.push(new Padre(idKey,'A',null));
                     enModulo=false;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }else{
                     /*if(lexema.equals("+")||lexema.equals("-")||lexema.equals("*")||lexema.equals("/")||lexema.equals("DIV")||lexema.equals("MOD")||lexema.equals("POT"))
                        estate=197;
                     else{
                        int next=((Integer)infoStates.pop()).intValue();
                        padresState.pop();
                        estate=next;
                        reuse=true;
                        nivel--;
                     }*/
                     estate=238;
                     reuse=true;
                     
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     //Busca el simbolo en la tabla y revisa que no sea un arreglo
                     Simbolo stmp=(Simbolo)tabSim.get(idKey);
                     if(stmp!=null){
                        //Revisa que est? dentro de parametros y que las dimensiones sean las correctas
                        if(!pilaPadres.isEmpty()&&enModulo&&argC[arrAC]<=params.size()){
                           Simbolo sTmp=(Simbolo)params.get(argC[arrAC]-1);
                           if(sTmp!=null){
                              if(sTmp.d1!=stmp.d1||sTmp.d2!=stmp.d2){
                                 Padre tmp=(Padre)pilaPadres.peek();
                                 String err="El parametro dentro de \""+tmp.idKey+"\" debe ser de las siguientes dimensiones: "+sTmp.d1+","+sTmp.d2;
                                 editor.errCount++;
                                 editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                              }else{
                                 //Generacion de codigo:
                                 if(sTmp.d1>0)
                                    codGen.enviaArr(idKey,sTmp.d1,sTmp.d2);
                              }
                           }
                        }else
                           if(stmp.d1!=0){
                              String err="Este registro esta dimensionado, falt? definir la posicion en el arreglo: "+idKey;
                              editor.errCount++;
                              editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                           }
                     }
                     /*if(idKey!=""){
                        pilaPadres.push(new Padre(idKey,'A'));
                     }*/
                     //Generacion de codigo:
                     codGen.escribePL0("LOD  "+idKey+",0");
                     
                     //Simbolo stmp=(Simbolo)tabSim.get(idKey);
                     if(idKey!=""&&stmp!=null){
                        //Todo salio bien, mete el tipo en la pila
                        pilaTipos.push(stmp.tipo_dato);
                     }else
                        pilaTipos.push("<Ninguno>");
                     //idKey="";
                     
                     if(!pilaOp.empty()&&!pilaPadres.empty()){
                        Operador opr=(Operador)pilaOp.peek();
                        Padre P=(Padre)pilaPadres.peek();
                        if((opr.op.equals("+ unario")||opr.op.equals("- unario"))&&P.equals(opr.padre)){
                           VT.verFact();
                           
                           //Generacion de codigo
                           codGen.codeOpers(opr.op);
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }
            }break;
            //-------------------------------- Fin de Factor ---------------------------
            
            //---------------------------------- Expresion -----------------------------
            case 233:{
                  //Termino
                  estate=235;
                  reuse=true;
            }break;
            case 234:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(!pilaOp.empty()&&!pilaPadres.empty()){
                     Operador opr=(Operador)pilaOp.peek();
                     Padre P=(Padre)pilaPadres.peek();
                     if((opr.op.equals("+")||opr.op.equals("-"))&&P.equals(opr.padre)){
                        VT.verExp();
                        
                        //Generacion de codigo:
                        codGen.codeOpers(opr.op);
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(lexema.equals("+")||lexema.equals("-")){
                     estate=233;
                     if(!pilaPadres.empty()){
                        pilaOp.push(new Operador(lexema,(Padre)pilaPadres.peek()));
                     }
                  }else{
                     int next=((Integer)infoStates.pop()).intValue();
                     padresState.pop();
                     estate=next;
                     reuse=true;
                     nivel--;
                  }
            }break;
            //------------------------------- Fin de Expresion -------------------------
            
            //----------------------------------- Termino ------------------------------
            case 235:{
                  //Potencia
                  estate=237;
                  reuse=true;
            }break;
            case 236:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(!pilaOp.empty()&&!pilaPadres.empty()){
                     Operador opr=(Operador)pilaOp.peek();
                     Padre P=(Padre)pilaPadres.peek();
                     if((opr.op.equals("*")||opr.op.equals("/")||opr.op.equals("DIV")||opr.op.equals("MOD"))&&P.equals(opr.padre)){
                        VT.verTerm();
                        
                        //Generacion de codigo:
                        codGen.codeOpers(opr.op);
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(lexema.equals("*")||lexema.equals("/")||lexema.equals("DIV")||lexema.equals("MOD")){
                     estate=235;
                     if(!pilaPadres.empty()){
                        pilaOp.push(new Operador(lexema,(Padre)pilaPadres.peek()));
                     }
                  }else{
                     estate=234;
                     reuse=true;
                  }
            }break;
            //-------------------------------- Fin de Termino --------------------------
            
            //----------------------------------- Potencia -----------------------------
            case 237:{
                  //Factor
                  estate=197;
                  reuse=true;
            }break;
            case 238:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(!pilaOp.empty()&&!pilaPadres.empty()){
                     Operador opr=(Operador)pilaOp.peek();
                     Padre P=(Padre)pilaPadres.peek();
                     if(opr.op.equals("POT")&&P.equals(opr.padre)){
                        VT.verPot();
                        
                        //Generacion de codigo:
                        codGen.codeOpers(opr.op);
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(lexema.equals("POT")){
                     estate=237;
                     if(!pilaPadres.empty()){
                        pilaOp.push(new Operador(lexema,(Padre)pilaPadres.peek()));
                     }
                  }else{
                     estate=236;
                     reuse=true;
                  }
            }break;
            //------------------------------- Fin de Potencia --------------------------
            /*case 210:{
                  //El caso 209 se usa en el bloque de Asignacion Arreglo
                  //Los casos 211 y 212 se usan en EncabezadoP y EncabezadoF
                  //Los casos 213 al 217 se usan en las estructuras de control
                  //Los casos 230 al 232 se usan en las asignaciones
                  //Los casos 233 al 238 se usan en Expresion y derivados
                  //Los casos 240 al 243 se usan en Regla_OR y derivados
                  /*
                   * Los casos 210,218,219,239 y del 244 al 249 se dejaran sin usarse para tener
                   * casos libres por si se modifica la regla Expresion o si otra regla 
                   * los necesita
                  *
            }break;*/
            //--------------------------- Parametros Identificador ---------------------
            case 220:{
                  //Regla_OR
                  infoStates.push(new Integer(221));
                  padresState.push(new Integer(221));
                  estate=240;
                  reuse=true;
                  nivel++;
                  /*//Impresion Semantico:
                  if(!pilaPadres.isEmpty()){
                     Padre tmp=(Padre)pilaPadres.peek();
                     int tam=0;
                     if(params!=null)
                        tam=params.size();
                     System.out.print("   *Padre: "+tmp.idKey+", Params: "+tam+"*   ");
                  }*/
            }break;
            case 221:{
                  /*//Impresion Semantico:
                  if(!pilaPadres.isEmpty()){
                     Padre pt=(Padre)pilaPadres.peek();
                     int tam=0;
                     if(params!=null)
                        tam=params.size();
                     System.out.print("   *Padre: "+pt.idKey+", Params: "+tam+"*   ");
                  }*/
                  
                  //Generacion de codigo:
                  if(!pilaPadres.isEmpty()){
                     Padre p=(Padre)pilaPadres.peek();
                     if(p.idKey.equals("ESCRIBIR")){
                        //System.out.println("Generando c?digo para ESCRIBIR en la linea: "+lineCounter);
                        codGen.escribePL0("OPR  0,20");
                     }else if(p.idKey.equals("ESCRIBIRCSL")){
                        if(tipoElem==4){
                           //System.out.println("Generando c?digo para ESCRIBIRCSL en la linea: "+lineCounter);
                           codGen.escribePL0("OPR  0,20");
                        }
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(!pilaPadres.isEmpty()&&enModulo&&argC[arrAC]<=params.size()){
                     Simbolo sTmp=(Simbolo)params.get(argC[arrAC]-1);
                     if(sTmp!=null){
                        String tipoTmp=(String)pilaTipos.peek();
                        if(!sTmp.tipo_dato.equals(tipoTmp)){
                           Padre p=(Padre)pilaPadres.peek();
                           if(sTmp.tipo_dato.equals("<Real>")){
                              if(!tipoTmp.equals("<Entero>")){
                                 String err="Se esperaba un "+sTmp.tipo_dato+" y se encontr? un "+tipoTmp+": <parametro "+argC[arrAC]+" de: \""+p.idKey+"\">";
                                 editor.errCount++;
                                 editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                              }
                           }else{
                              String err="Se esperaba un "+sTmp.tipo_dato+" y se encontr? un "+tipoTmp+": <parametro "+argC[arrAC]+" de: \""+p.idKey+"\">";
                              editor.errCount++;
                              editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                           }
                        }
                     }
                  }
                  pilaTipos.pop();
                  idKey="";
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(tipoElem==4){
                     estate=220;
                     argC[arrAC]++;
                  }else{
                     int next=((Integer)infoStates.pop()).intValue();
                     padresState.pop();
                     estate=next;
                     reuse=true;
                     nivel--;
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     Padre tmp=null;
                     if(!pilaPadres.isEmpty())
                        tmp=(Padre)pilaPadres.peek();
                     
                     if(tmp!=null&&enModulo){
                        String tmpKey=tmp.idKey;
                        Simbolo stmp=(Simbolo)tabSim.get(tmpKey);
                        if(stmp!=null){
                           idKey=stmp.nombre;
                           //Verifica que el numero de parametros sea el correcto:
                           if(stmp.clase=='F'||stmp.clase=='P'){
                              if(params.size()!=argC[arrAC]){
                                 String err="El modulo \""+tmpKey+"\" debe recibir "+params.size()+" parametros.";
                                 editor.errCount++;
                                 editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
                              }
                           }
                        }
                        arrAC--;
                        enModulo=false;
                     }/*else
                        if(tmp!=null)
                           idKey=tmp.idKey;*/
                     
                     if(!pilaPadres.empty())
                        pilaPadres.pop();
                     //Revisa lo que hay en la pila por si es otro modulo:
                     if(!pilaPadres.isEmpty()){
                        tmp=(Padre)pilaPadres.peek();
                        if(tmp.params!=null)
                           params=tmp.params;
                        else
                           params.clear();
                        if(tmp.tipo=='M'){
                           enModulo=true;
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }
            }break;
            //------------------------ Fin de Parametros Identificador -----------------
            
            //---------------------------------- Regla_OR ------------------------------
            case 240:{
                  //Regla_AND
                  estate=242;
                  reuse=true;
            }break;
            case 241:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(!pilaOp.empty()&&!pilaPadres.empty()){
                     Operador opr=(Operador)pilaOp.peek();
                     Padre P=(Padre)pilaPadres.peek();
                     if(opr.op.equals("|")&&P.equals(opr.padre)){
                        VT.verOR();
                        
                        //Generacion de codigo:
                        codGen.codeOpers(opr.op);
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(lexema.equals("|")){
                     estate=240;
                     if(!pilaPadres.empty()){
                        pilaOp.push(new Operador(lexema,(Padre)pilaPadres.peek()));
                     }
                  }else{
                     int next=((Integer)infoStates.pop()).intValue();
                     padresState.pop();
                     estate=next;
                     reuse=true;
                     nivel--;
                  }
            }break;
            //------------------------------ Fin de Regla_OR ---------------------------
            
            //--------------------------------- Regla_AND ------------------------------
            case 242:{
                  //Regla_NOT
                  estate=222;
                  reuse=true;
            }break;
            case 243:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(!pilaOp.empty()&&!pilaPadres.empty()){
                     Operador opr=(Operador)pilaOp.peek();
                     Padre P=(Padre)pilaPadres.peek();
                     if(opr.op.equals("&")&&P.equals(opr.padre)){
                        VT.verOR();
                        
                        //Generacion de codigo:
                        codGen.codeOpers(opr.op);
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(lexema.equals("&")){
                     estate=242;
                     if(!pilaPadres.empty()){
                        pilaOp.push(new Operador(lexema,(Padre)pilaPadres.peek()));
                     }
                  }else{
                     estate=241;
                     reuse=true;
                  }
            }break;
            //----------------------------- Fin de Regla_AND ---------------------------
            
            //--------------------------------- Regla_NOT ------------------------------
            case 222:{
                  if(lexema.equals("!")){
                     estate=223;
                     if(!pilaPadres.empty()){
                        pilaOp.push(new Operador(lexema,(Padre)pilaPadres.peek()));
                     }
                  }else{
                     //Expresion
                     infoStates.push(new Integer(224));
                     padresState.push(new Integer(224));
                     estate=233;
                     reuse=true;
                     nivel++;
                  }
            }break;
            case 223:{
                  //Expresion
                  infoStates.push(new Integer(224));
                  padresState.push(new Integer(224));
                  estate=233;
                  reuse=true;
                  nivel++;
            }break;
            case 224:{
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(!pilaOp.empty()&&!pilaPadres.empty()){
                     Operador opr=(Operador)pilaOp.peek();
                     Padre P=(Padre)pilaPadres.peek();
                     if((opr.op.equals("<")||opr.op.equals(">")||opr.op.equals("<=")||opr.op.equals(">=")||opr.op.equals("<>")||opr.op.equals("="))&&P.equals(opr.padre)){
                        VT.verREL();
                        
                        //Generacion de codigo:
                        codGen.codeOpers(opr.op);
                     }
                  }
                  //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  if(token.equals("<Op_Rel>")){
                     estate=223;
                     if(!pilaPadres.empty()){
                        pilaOp.push(new Operador(lexema,(Padre)pilaPadres.peek()));
                     }
                  }else{
                     /*
                     int next=((Integer)infoStates.pop()).intValue();
                     padresState.pop();
                     estate=next;
                     reuse=true;
                     nivel--;
                     */
                     estate=243;
                     reuse=true;
                     
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                     if(!pilaOp.empty()){
                        Operador opr=(Operador)pilaOp.peek();
                        if(opr.op.equals("!")){
                           VT.verNOT();
                           
                           //Generacion de codigo:
                           codGen.codeOpers(opr.op);
                        }
                     }
                     if(!pilaOp.empty()&&!pilaPadres.empty()){
                        Operador opr=(Operador)pilaOp.peek();
                        Padre P=(Padre)pilaPadres.peek();
                        if(opr.op.equals("!")&&P.equals(opr.padre)){
                           VT.verNOT();
                           
                           //Generacion de codigo:
                           codGen.codeOpers(opr.op);
                        }
                     }
                     //>>>>>>>>>>>>>>>>>>>>>>>>>>>   Fin de Semantica   <<<<<<<<<<<<<<<<<<<<<<<<<<
                  }
            }break;
            //----------------------------- Fin de Regla_NOT ---------------------------
            
            
            //-------------------------------- Archivos_H ------------------------------
            case 250:{
                  if(tipoElem==6){
                     infoStates.push(new Integer(250));
                     padresState.push(new Integer(250));
                     estate=5;
                     nivel++;
                     if(etapa<=1){
                        etapa=2;
                     }else{
                        String err="Las constantes se deben declarar una sola vez y solo al principio del programa despues de los archivos si los hay.";
                        poneError(2,err);
                     }
                  }else
                     if(tipoElem==12){
                        infoStates.push(new Integer(250));
                        padresState.push(new Integer(250));
                        estate=11;
                        nivel++;
                        if(etapa<=2){
                           etapa=3;
                        }else{
                           String err="Las variables se deben declarar una sola vez y solo despues de los archivos y las constantes si los hay.";
                           poneError(2,err);
                        }
                     }else
                        if(tipoElem==18){
                           infoStates.push(new Integer(250));
                           padresState.push(new Integer(250));
                           estate=19;
                           nivel++;
                           if(etapa<=3){
                              etapa=4;
                           }else{
                              String err="Los arreglos se deben declarar una sola vez y solo despues de los archivos, las constantes y/o las variables si los hay.";
                              poneError(2,err);
                           }
                        }else
                           if(tipoElem==21){
                              infoStates.push(new Integer(250));
                              padresState.push(new Integer(250));
                              estate=40;
                              nivel++;
                              if(etapa<=4){
                                 etapa=5;
                              }else{
                                 String err="Los registros se deben declarar una sola vez y solo despues de los archivos, las constantes, las variables y/o los arreglos, si los hay.";
                                 poneError(2,err);
                              }
                           }else
                              if(tipoElem==23){
                                 infoStates.push(new Integer(250));
                                 padresState.push(new Integer(250));
                                 estate=53;
                                 nivel++;
                                 if(etapa<=5){
                                    etapa=6;
                                 }else{
                                    String err="Los prototipos se deben declarar una sola vez y solo despues de los archivos, las constantes, las variables, los arreglos y/o los registros, si los hay.";
                                    poneError(2,err);
                                 }
                              }else
                                 if(tipoElem==22){
                                    infoStates.push(new Integer(250));
                                    padresState.push(new Integer(250));
                                    modulosBan=true;
                                    estate=57;
                                    nivel++;
                                    if(etapa<=6){
                                       etapa=6;
                                    }else{
                                       String err="Los modulos se deben declarar una sola vez y solo despues de los archivos, las constantes, las variables, los arreglos y/o los registros, si los hay.";
                                       poneError(2,err);
                                    }
                                 }else{
                                    if(lexema.equals("ARCHIVOS")){
                                    	String err="No se pueden declarar Archivos en un archivo de inclusi?n.";
                                       poneError(2,err);
                                    }else if(lexema.equals("PRINCIPAL")){
                                    	String err="Los archivos de inclusi?n no pueden tener un bloque Principal.";
                                       poneError(2,err);
                                    }else{
                                    	String err="C?digo no previsto: "+lexema;
                                       poneError(2,err);
                                    }
                                    estate=mError.recuperaArchivosH();
                                 }
            }break;
            //---------------------------- Fin de Archivos_H ---------------------------
         }
         /*if(!mError.tokenBuffer.isEmpty()){
            
            System.out.print("*Buffer:");
            for(int i=0;i<mError.lexemaBuffer.size();i++)
               System.out.print(" "+mError.lexemaBuffer.get(i));
            System.out.print("*");
            
            token=(String)mError.tokenBuffer.remove(0);
            lexema=(String)mError.lexemaBuffer.remove(0);
            tipoElem=((Integer)mError.tipoBuffer.remove(0)).intValue();
            mError.posBuffer.remove(0);
            reuse=true;
         }*/
         
         if(!reuse)
            token=dameToken();
      }
      analisisFinal();
   }
   
   private void revisaDimensiones(Simbolo stmp){
      pilaTipos.push(stmp.tipo_dato);
      //Revisa que est? dentro de parametros y que las dimensiones sean las correctas
      if(!pilaPadres.isEmpty()&&enModulo&&argC[arrAC]<=params.size()){
         Simbolo sTmp=(Simbolo)params.get(argC[arrAC]-1);
         if(sTmp!=null){
            if(sTmp.d1!=stmp.d1||sTmp.d2!=stmp.d2){
               Padre tmp=(Padre)pilaPadres.peek();
               String err="El parametro dentro de \""+tmp.idKey+"\" debe ser de las siguientes dimensiones: "+sTmp.d1+","+sTmp.d2;
               editor.errCount++;
               editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
            }else{
               //Generacion de codigo:
               if(sTmp.d1>0)
                  codGen.enviaArr(idKey,sTmp.d1,sTmp.d2);
            }
         }
      }else{
         String err="Este identificador es un arreglo, falto definir la posicion en el arreglo: "+idKey;
         editor.errCount++;
         editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
      }
   }
   
   private void revisaDims(){
      //Revisa que est? dentro de parametros y que las dimensiones sean las correctas
      if(!pilaPadres.isEmpty()&&enModulo&&argC[arrAC]<=params.size()){
         Simbolo sTmp=(Simbolo)params.get(argC[arrAC]-1);
         if(sTmp!=null){
            if(sTmp.d1>0){
               Padre tmp=(Padre)pilaPadres.peek();
               String err="El parametro dentro de \""+tmp.idKey+"\" se debe enviar completo (todo el arreglo) y debe ser de las siguientes dimensiones: "+sTmp.d1+","+sTmp.d2;
               editor.errCount++;
               editor.errores(estado,3,err,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
            }
         }
      }
   }
   
   private void revisaParam(){
      //Este procedimiento s?lo es usado por el bloque Parametros
      //Agrega el parametro en la tabla de simbolos:
      String param=lexema+"@"+modulo;
      String err;
      if(!tabSim.containsKey(param)){
         //Revisa si no existe una constante o un modulo con ese nombre:
         Simbolo sm=(Simbolo)tabSim.get(lexema);
         boolean error=false;
         if(sm!=null){
            if(sm.clase=='C'){
               error=true;
               err="Ya existe una constante con ese nombre: "+lexema;
               poneError(3,err);
            }else if(sm.clase=='P'||sm.clase=='F'){
               error=true;
               err="Ya existe un modulo con ese nombre: "+lexema;
               poneError(3,err);
            }
         }
         
         if(!error){
            tabSim.put(param,new Simbolo(param,'A',tipo,""));
            idKey=param;
         }
      }else{
         idKey="";
         err="Ya existe un argumento con ese nombre: "+lexema;
         poneError(3,err);
      }
   }
   private void valorArreglo(boolean unidim,boolean init){
      //*** Generacion de codigo ***
      int t1=d1c;
      if(!unidim)
         t1--;
      if(!init){
         //Se va a escribir directamente en el archivo:
         codGen.llenaArr(t1,d2c,lexema,idKey,!unidim,true);
      }else{
         //Se va a escribir en la lista de codigo temporal:
         codGen.llenaArr(t1,d2c,lexema,idKey,!unidim,false);
      }
      // Fin de generacion de codigo
      
      if(unidim)
         d1c++;
      else
         d2c++;
      Simbolo simb=(Simbolo)tabSim.get(idKey);
      if(simb!=null){
         if(unidim){
            if(d1c==simb.d1+1){
               String err="El arreglo se defini? para contener solo "+simb.d1+" elementos: "+idKey;
               poneError(3,err);
            }
         }else
            if(d2c==simb.d2+1){
               String err="El arreglo se defini? para contener solo "+simb.d2+" elementos por subarreglo: "+idKey;
               poneError(3,err);
            }
      }
      if(!tipo.equals("<Ninguno>")){
         if(!tipo.equals(token)){
            if(tipo.equals("<Real>")){
               if(!token.equals("<Entero>")){
                  String err="Incongruencia en tipos de dato: "+lexema;
                  poneError(3,err);
               }
            }else{
               String err="Incongruencia en tipos de dato: "+lexema;
               poneError(3,err);
            }
         }
      }
   }
   private void constArreglo(boolean unidim,boolean init){
      if(unidim)
         d1c++;
      else
         d2c++;
      Simbolo simb=(Simbolo)tabSim.get(idKey);
      if(simb!=null){
         if(unidim){
            if(d1c==simb.d1+1){
               String err="El arreglo se defini? para contener solo "+simb.d1+" elementos: "+lexema;
               poneError(3,err);
            }
         }else{
            if(d2c==simb.d2+1){
               String err="El arreglo se defini? para contener solo "+simb.d2+" elementos por subarreglo: "+lexema;
               poneError(3,err);
            }
         }
      }
      //Revisa que sea una constante
      Simbolo sim=(Simbolo)tabSim.get(lexema);
      if(sim!=null){
         if(sim.clase=='C'){
            //*** Generacion de codigo ***
            int t1=d1c,t2=d2c;
            if(!unidim)
               t2--;
            /*if(unidim)
               t1--;
            else
               t2--;*/
            if(!init){
               //Se va a escribir directamente en el archivo:
               codGen.llenaArr(t1-1,t2,sim.cspcfyp,idKey,!unidim,true);
            }else
               codGen.llenaArr(t1-1,t2,sim.cspcfyp,idKey,!unidim,false);
            // Fin de generacion de codigo
            
            if(!tipo.equals("<Ninguno>")){
               if(!tipo.equals(sim.tipo_dato)){
                  if(tipo.equals("<Real>")){
                     if(!sim.tipo_dato.equals("<Entero>")){
                        String err="Incongruencia en tipos de dato: "+lexema;
                        poneError(3,err);
                     }
                  }else{
                     String err="Incongruencia en tipos de dato: "+lexema;
                     poneError(3,err);
                  }
               }
            }
         }else{
            String err="Se esperaba una constante y se encontr?: "+lexema;
            poneError(3,err);
         }
      }else{
         String err="Se esperaba un identificador constante existente y se encontr?: "+lexema;
         poneError(3,err);
      }
   }
   private void llenaParam(Simbolo simbol){
      /*
      System.out.println("\nLinea: "+lineCounter);
      System.out.println("CSPCFYP: "+simbol.cspcfyp);
      */
      params.clear();
      char tmp;
      int dimC=0;
      Simbolo simTmp=null;
      String num="";
      for(int x=0;x<simbol.cspcfyp.length();x++){
         tmp=simbol.cspcfyp.charAt(x);//E|E,3,2
         if(tmp=='|'){
            if(dimC==1){
               simTmp.d1=Integer.parseInt(num);
               num="";
            }else if(dimC==2){
               simTmp.d2=Integer.parseInt(num);
               num="";
            }
            dimC=0;
         }else if(tmp==','){
            if(dimC==1){
               simTmp.d1=Integer.parseInt(num);
               num="";
            }
            dimC++;
         }else if(dimC==0){
            String tipoD=tipoLetra(tmp);
            simTmp=new Simbolo(null,'A',tipoD,"");
            params.add(simTmp);
         }else if(dimC==1||dimC==2)
            num=num+tmp;
      }
      
      if(dimC==1)
         simTmp.d1=Integer.parseInt(num);
      else if(dimC==2)
         simTmp.d2=Integer.parseInt(num);
      /*
      for(int c=0;c<params.size();c++){
         simTmp=(Simbolo)params.get(c);
         System.out.println("Parametro "+c+": Tipo: "+simTmp.tipo_dato+", D1: "+simTmp.d1+", D2: "+simTmp.d2);
      }*/
   }
   
   private void analisisFinal(){
      System.out.println("\nFin del Analisis!!");
      if(etapa!=7&&(win.nombre.endsWith(".i")||win.nombre.endsWith(".I"))){
         String err="Falt? el bloque Principal del programa.";
         editor.errCount++;
         editor.errores(estado,2,err,lineCounter,i-1,lexema,win,win.nombre,win.ruta);
      }else
         if(etapa==7&&estate==62){
            String err="No hay c?digo dentro de Principal.";
            editor.errCount++;
            editor.errores(estado,2,err,lineCounter,i-1,lexema,win,win.nombre,win.ruta);
         }else
            if(!modulosBan&&(win.nombre.endsWith(".h")||win.nombre.endsWith(".H"))){
               String err="El bloque de Modulos es necesario en los archivos de inclusi?n.";
               editor.errCount++;
               editor.errores(estado,2,err,lineCounter,i-1,lexema,win,win.nombre,win.ruta);
            }
      
      //Revisa si quedaron etiquetas por resolver y las resuelve
      if(pilaStructs.size()>0)
         resuelveFinal();
      
      //Imprime la cantidad de elementos que quedaron en cada pila:
      System.out.print("\nElementos en pilaTipos: "+pilaTipos.size());
      for(int j=0;j<pilaTipos.size();j++){
         String t=(String)pilaTipos.elementAt(j);
         System.out.print(","+t);
      }
      System.out.print("\nElementos en pilaOp: "+pilaOp.size());
      for(int j=0;j<pilaOp.size();j++){
         Operador op=(Operador)pilaOp.elementAt(j);
         System.out.print(","+op.op);
      }
      System.out.print("\nElementos en pilaStructs: "+pilaStructs.size());
      for(int j=0;j<pilaStructs.size();j++){
         ControlStruct cs=(ControlStruct)pilaStructs.elementAt(j);
         System.out.print(","+cs.nombre);
      }
      System.out.print("\nElementos en pilaDirs: "+pilaDirs.size());
      for(int j=0;j<pilaDirs.size();j++){
         String t=(String)pilaDirs.elementAt(j);
         System.out.print(","+t);
      }
      System.out.println("");
      
      //Generacion de codigo:
      if(modulo.equals("PRINCIPAL")){
         codGen.escribePL0("OPR  0,0");
         
         //Genera y resuelve a $LabelT
         Simbolo etT=new Simbolo("$LabelT",'N',"<Ninguno>","");
         etT.d1=codGen.contador;
         tabSim.put("$LabelT",etT);
      }
      if(win.areaTexto!=null)
         codGen.cierraPL0();
      
      //Imprime el contenido de la tabla de simbolos:
      ArrayList simbs=new ArrayList(tabSim.values());
      ListaBurbuja(simbs);
      System.out.println("\nNOMBRE\t\tCLASE\tTIPO\t\tD1\tD2\tCSPCFYP");
      for(int a=0;a<simbs.size();a++){
         Simbolo st=(Simbolo)simbs.get(a);
         System.out.print(st.nombre);
         if(st.nombre.length()<8)
            System.out.print("\t");
         System.out.print("\t"+st.clase+"\t"+st.tipo_dato+"\t");
         if(st.tipo_dato.length()<8)
            System.out.print("\t");
         System.out.println(st.d1+"\t"+st.d2+"\t"+st.cspcfyp);
      }
      
      //Imprime el contenido de la tabla de Prototipos:
      ArrayList prots=new ArrayList(tabProtos.values());
      System.out.println("\nNOMBRE\tCLASE\tTIPO\t\tDIR\t\tCSPCFYP \tPARAMETROS");
      for(int a=0;a<prots.size();a++){
         Proto pt=(Proto)prots.get(a);
         System.out.print(pt.nombre+"\t"+pt.clase+"\t"+pt.tipo_dato+"\t");
         if(pt.tipo_dato.length()<8)
            System.out.print("\t");
         System.out.print(pt.dir+"\t");
         if(pt.dir.length()<8)
            System.out.print("\t");
         System.out.print(pt.cspcfyp+"\t");
         //Imprime los parametros:
         if(pt.cspcfyp.length()<8)
            System.out.print("\t");
         for(int c1=0;c1<pt.parametros.size();c1++){
            String sp=(String)pt.parametros.get(c1);
            System.out.print(sp+" ");
         }
         System.out.println("");
      }
      System.out.println("--------------------------------------------------------------------\n");
      
      if(editor.errCount==-1&&win.areaTexto!=null){
         //No hubieron errores
         codGen.escribeMVI(simbs);
      }
   }
   private void resuelveFinal(){
      for(int j=0;j<pilaStructs.size();j++){
         ControlStruct cs=(ControlStruct)pilaStructs.elementAt(j);
         if(!tabSim.containsKey(cs.EtX)&&cs.EtX!=""){
            //Resuelve a EtX
            Simbolo etX=new Simbolo(cs.EtX,'N',"<Ninguno>","");
            etX.d1=codGen.contador+1;
            tabSim.put(cs.EtX,etX);
         }
         if(!tabSim.containsKey(cs.EtY)&&cs.EtY!=""){
            //Resuelve a EtY
            Simbolo etY=new Simbolo(cs.EtY,'N',"<Ninguno>","");
            etY.d1=codGen.contador+1;
            tabSim.put(cs.EtY,etY);
         }
         if(!tabSim.containsKey(cs.EtA)&&cs.EtA!=""){
            //Resuelve a EtA
            Simbolo etA=new Simbolo(cs.EtA,'N',"<Ninguno>","");
            etA.d1=codGen.contador+1;
            tabSim.put(cs.EtA,etA);
         }
      }
   }
   public void poneError(int tipoAnalisis,String error){
      editor.errCount++;
      editor.errores(estado,tipoAnalisis,error,lineCounter,i,lexema,win,win.nombre,win.ruta);
   }
   public void poneError2(int tipoAnalisis,String error){
      editor.errCount++;
      editor.errores(estado,tipoAnalisis,error,lineCounter,i-lexema.length(),"",win,win.nombre,win.ruta);
   }
   
   
   //*************************************************************************************
   //*******************************    Analisis Lexico    *******************************
   //*************************************************************************************
   public String dameToken(){
      lexema="";
      estado=0;
      while(i<fuente.length()){
         i++;
         if(i>=fuente.length())
            c='\0';
         else
            c=fuente.charAt(i);
         
         switch(estado){
            case 0:{
                  tipoChar=dameTipoChar(c);
                  switch(tipoChar){
                     case 1:{//Es digito
                           estado=1;
                           lexema=lexema+c;
                     }break;
                     case 2:{//Es letra
                           estado=8;
                           lexema=lexema+c;
                     }break;
                     case 3:{//Es guion bajo
                           estado=9;
                           lexema=lexema+c;
                     }break;
                     case 4:{//Es comilla
                           estado=10;
                           lexema=lexema+c;
                     }break;
                     case 5:{//Es <
                           estado=12;
                           lexema=lexema+c;
                     }break;
                     case 6:{//Es >
                           estado=17;
                           lexema=lexema+c;
                     }break;
                     case 7:{//Es =
                           estado=18;
                           lexema=lexema+c;
                     }break;
                     case 8:{//Es +,-,=,*
                           estado=21;
                           lexema=lexema+c;
                     }break;
                     case 9:{//Es |,&,!
                           estado=22;
                           lexema=lexema+c;
                     }break;
                     case 10:{//Es delimitador
                           estado=23;
                           lexema=lexema+c;
                     }break;
                     case -1:{//Caracter ilegal
                           String err="'"+c+"' es un caracter invalido";
                           editor.errCount++;
                           lexema=lexema+c;
                           editor.errores(estado,1,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                           lexema="";
                     }break;
                  }
            }break;
            case 1:{
                  if(Character.isDigit(c))
                     lexema=lexema+c;
                  else
                     if(c=='.'){
                        estado=2;
                        lexema=lexema+c;
                     }else
                        if(c=='e'||c=='E'){
                           estado=4;
                           lexema=lexema+c;
                        }else{
                           i--;
                           return "<Entero>";
                        }
            }break;
            case 2:{
                  if(Character.isDigit(c)){
                     estado=3;
                     lexema=lexema+c;
                  }else{
                     i--;
                     String err="se esperaba un digito y se encontr?: '"+c+"' en el lexema: "+lexema;
                     editor.errCount++;
                     editor.errores(estado,1,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                     return "<Real>";
                  }
            }break;
            case 3:{
                  if(Character.isDigit(c))
                     lexema=lexema+c;
                  else
                     if(c=='e'||c=='E'){
                        estado=4;
                        lexema=lexema+c;
                     }else{
                        i--;
                        return "<Real>";
                     }
            }break;
            case 4:{
                  if(Character.isDigit(c)){
                     estado=6;
                     lexema=lexema+c;
                  }else
                     if(c=='+'||c=='-'){
                        estado=5;
                        lexema=lexema+c;
                     }else{
                        i--;
                        String err="se esperaba un digito o un +|- y se encontr?: '"+c+"' en el lexema: "+lexema;
                        editor.errCount++;
                        editor.errores(estado,1,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                        return "<Real>";
                     }
            }break;
            case 5:{
                  if(Character.isDigit(c)){
                     estado=6;
                     lexema=lexema+c;
                  }else{
                     i--;
                     String err="se esperaba un digito y se encontr?: '"+c+"' en el lexema: "+lexema;
                     editor.errCount++;
                     editor.errores(estado,1,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                     
                     return "<Real>";
                  }
            }break;
            case 6:{
                  if(Character.isDigit(c)){
                     estado=7;
                     lexema=lexema+c;
                  }else{
                     i--;
                     return "<Real>";
                  }
            }break;
            case 7:{
                  i--;
                  return "<Real>";
            }//break;
            case 8:{
                  tipoChar=dameTipoChar(c);
                  if(tipoChar>=1&&tipoChar<=3){
                     lexema=lexema+c;
                  }else{
                     i--;
                     if(lexema.equals("DIV"))
                        return "<Op_Aritm>";
                     else
                        if(lexema.equals("MOD"))
                           return "<Op_Aritm>";
                        else
                           if(lexema.equals("POT"))
                              return "<Op_Aritm>";
                           else
                              if(lexema.equals("VERDADERO"))
                                 return "<Logico>";
                              else
                                 if(lexema.equals("FALSO"))
                                    return "<Logico>";
                                 else
                                    if(findReserved(lexema)>-1)
                                       return "<Pal_Res>";
                                    else
                                       return "<Identificador>";
                  }
            }break;
            case 9:{
                  tipoChar=dameTipoChar(c);
                  if(tipoChar==3){
                     lexema=lexema+c;
                  }else
                     if(tipoChar==1||tipoChar==2){
                        estado=8;
                        lexema=lexema+c;
                     }else{
                        i--;
                        String err="los identificadores no pueden ser de solo guiones bajos: "+lexema;
                        editor.errCount++;
                        editor.errores(estado,1,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                        
                        return "<Identificador>";
                     }
            }break;
            case 10:{
                  if(c=='"'){
                     estado=11;
                     lexema=lexema+c;
                  }else
                     if(c!='\n'&&c!='\0'){
                        lexema=lexema+c;
                     }else{
                        i--;
                        String err="Las cadenas deben ser de una sola linea: "+lexema;
                        editor.errCount++;
                        editor.errores(estado,1,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                        
                        return "<Cadena>";
                     }
            }break;
            case 11:{
                  i--;
                  return "<Cadena>";
            }//break;
            case 12:{
                  if(c=='!'){
                     estado=13;
                     lexema=lexema+c;
                  }else
                     if(c=='='){
                        estado=18;
                        lexema=lexema+c;
                     }else
                        if(c=='>'){
                           estado=19;
                           lexema=lexema+c;
                        }else
                           if(c=='-'){
                              estado=20;
                              lexema=lexema+c;
                           }else{
                              i--;
                              return "<Op_Rel>";
                           }
            }break;
            case 13:{
                  if(c=='-'){
                     estado=14;
                     lexema=lexema+c;
                  }else{
                     i--;
                     String err="Los comentarios deben iniciar con la cadena '<!-' (sin las comillas): "+lexema;
                     editor.errCount++;
                     editor.errores(estado,1,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                     
                     lexema="";
                     estado=0;
                  }
            }break;
            case 14:{
                  if(c=='-'){
                     estado=15;
                     lexema=lexema+c;
                  }else
                     if(c!='\0'){
                        lexema=lexema+c;
                        if(c=='\n')
                           lineCounter++;
                     }else{
                        i--;
                        String err="Debes finalizar el comentario antes de que sea el fin del documento";
                        editor.errCount++;
                        editor.errores(estado,1,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                        
                        lexema="";
                        estado=0;
                     }
            }break;
            case 15:{
                  if(c=='-'){
                     lexema=lexema+c;
                  }else
                     if(c=='>'){
                        estado=16;
                        lexema=lexema+c;
                     }else
                        if(c!='\0'){
                           estado=14;
                           lexema=lexema+c;
                           if(c=='\n')
                              lineCounter++;
                        }else{
                           i--;
                           String err="Debes finalizar el comentario antes de que sea el fin del documento";
                           editor.errCount++;
                           editor.errores(estado,1,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
                           
                           lexema="";
                           estado=0;
                        }
            }break;
            case 16:{
                  i--;
                  lexema="";
                  estado=0;
            }break;
            case 17:{
                  if(c=='='){
                     estado=18;
                     lexema=lexema+c;
                  }else{
                     i--;
                     return "<Op_Rel>";
                  }
            }break;
            case 18:{
                  i--;
                  return "<Op_Rel>";
            }//break;
            case 19:{
                  i--;
                  return "<Op_Rel>";
            }//break;
            case 20:{
                  i--;
                  return "<Op_Asign>";
            }//break;
            case 21:{
                  i--;
                  return "<Op_Aritm>";
            }//break;
            case 22:{
                  i--;
                  return "<Op_Log>";
            }//break;
            case 23:{
                  i--;
                  return "<Delimitador>";
            }//break;
         }
      }
      return "\0";
   }
   
   public int dameTipoChar(char car){
      if(Character.isDigit(car)){
         return 1;
      }else if(car=='_'){
         return 3;
      }else if(car=='"'){
         return 4;
      }else if(car=='<'){
         return 5;
      }else if(car=='>'){
         return 6;
      }else if(car=='='){
         return 7;
      }else if(car=='+'||car=='-'||car=='*'||car=='/'){
         return 8;
      }else if(car=='|'||car=='&'||car=='!'){
         return 9;
      }else if(car==','||car==';'||car=='('||car==')'||car=='['||car==']'||car=='{'||car=='}'){
         return 10;
      }else if(Character.isLetter(car)&&car!='?'&&car!='?'&&car!='?'&&car!='?'){
         return 2;
      }else if(car=='\n'){
         if(estado==0)
            lineCounter++;
         /*System.out.print("\nEmpieza linea "+lineCounter);
         System.out.println(", Lexema: "+lexema);*/
         return 0;
      }else
         if(car=='\t'||car=='\0'||Character.isWhitespace(car)){
            return 0;
         }else{
            return -1;
         }
   }
   private void existeLibreria(){
      if(!lexema.endsWith(".h\"")&&!lexema.endsWith(".H\"")){
         String err="El archivo referenciado debe tener la extension \".h\": "+lexema;
         editor.errCount++;
         editor.errores(estado,0,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
      }else{
         //Abre el archivo para ver si existe:
         boolean existe;
         String ruta=lexema.substring(1,lexema.length()-1);
         
         //Checa si la ruta es absoluta o relativa y le agrega la ruta completa si esta es relativa
         if(ruta.indexOf(':')==-1){
            //La ruta es relativa
            String rutaTmp=win.ruta.substring(0,win.ruta.lastIndexOf("\\"));
            rutaTmp=rutaTmp+'\\'+ruta;
            ruta=rutaTmp.toString();
         }
         //System.out.println("Ruta: "+ruta);
         //Checa si ya se habia declarado antes la libreria:
         ruta=ruta.toLowerCase();
         if(librerias.contains(ruta)){
            String err="La libreria \""+ruta+"\" ya se incluy? antes.";
            editor.errCount++;
            editor.errores(estado,0,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
         }else{
            librerias.add(ruta);
            try{
               FileReader fr=new FileReader(ruta);
               existe=true;
               fr.close();
            }catch(IOException ex){
               existe=false;
            }
            if(!existe){
               String err="No se encontr? la libreria en la ruta especificada: "+ruta;
               editor.errCount++;
               editor.errores(estado,0,err,lineCounter,i,lexema,win,win.nombre,win.ruta);
            }else{
               //Analiza el archivo
               String ghostName="@"+(ruta.substring(ruta.lastIndexOf("\\")+1,ruta.length()));
               
               Ventana ghostWin=new Ventana(null,null,null,ghostName,null,ruta);
               editor.ventanas.add(ghostWin);
               
               Sintactico sint=new Sintactico(ghostWin,editor,tabSim,tabProtos,tempCode,codGen);
               sint.estate=250;
               sint.Programa();
            }
         }
      }
   }
   
   private void revisaTipo(){
      if(tipoElem==14)
         tipo="<Entero>";
      else if(tipoElem==15)
         tipo="<Real>";
      else if(tipoElem==16)
         tipo="<Cadena>";
      else if(tipoElem==17)
         tipo="<Logico>";
   }
   private String letraTipo(){
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
   private String tipoLetra(char letra){
      if(letra=='E')
         return "<Entero>";
      else if(letra=='R')
         return "<Real>";
      else if(letra=='C')
         return "<Cadena>";
      else if(letra=='L')
         return "<Logico>";
      else 
         return "<Ninguno>";
   }
   private void ListaBurbuja(ArrayList lista){
      boolean b;
      do{
         b=false;
         for(int i=0;i<lista.size()-1;++i){
            Simbolo s1=(Simbolo)lista.get(i);
            Simbolo s2=(Simbolo)lista.get(i+1);
            if(s1.nombre.compareTo(s2.nombre)>0){//s1>s2
               //z=a[i];
               lista.set(i,s2);//a[i]=a[i+1];
               lista.set(i+1,s1);//a[i+1]=z;
               b=true;
            }
         }
      }while(b);
   }
   private void dimRegistro(String llave,int dim,boolean bidim){
      for(Enumeration e=tabSim.keys();e.hasMoreElements();){
         String keyTmp=(String)e.nextElement();
         String prefijo=llave+".";
         if(keyTmp.startsWith(prefijo)&&keyTmp.indexOf('@')==-1){
            Simbolo stmp=(Simbolo)tabSim.get(keyTmp);
            if(!bidim)
               stmp.d1=dim;
            else
               stmp.d2=dim;
         }
      }
   }
   private void dimRegistroL(String llave,int dim,boolean bidim){
      for(Enumeration e=tabSim.keys();e.hasMoreElements();){
         String keyTmp=(String)e.nextElement();
         String posfijo="@"+modulo;
         if(keyTmp.endsWith(posfijo)){
            String prefijo=llave+".";
            if(keyTmp.startsWith(prefijo)){
               Simbolo stmp=(Simbolo)tabSim.get(keyTmp);
               if(!bidim)
                  stmp.d1=dim;
               else
                  stmp.d2=dim;
            }
         }
      }
   }
   //****************************************************************************************
   //**************     Todos los metodos para la recuperacion de errores:     **************
   private void llenaReglas(){
      ArrayList reglaTmp;
      //Reglas.put(KEY,VALUE);
      
      reglaTmp=new ArrayList(8);
      reglaTmp.add("ARCHIVOS");
      reglaTmp.add("CONSTANTES");
      reglaTmp.add("VARIABLES");
      reglaTmp.add("ARREGLOS");
      reglaTmp.add("REGISTROS");
      reglaTmp.add("PROTOTIPOS");
      reglaTmp.add("MODULOS");
      reglaTmp.add("PRINCIPAL");
      Reglas.put("Programa",reglaTmp);
      //int array1[]={1,5,11,19,40,53,57,62};
      int array1[]={0,0,0,0,0,0,0,0};
      ReglasE.put("Programa",array1);
      
      reglaTmp=new ArrayList(5);
      reglaTmp.add("ARCHIVOS");
      reglaTmp.add("[");
      reglaTmp.add("<Cadena>");//reuse
      reglaTmp.add(",");
      reglaTmp.add("]");
      Reglas.put("Archivos",reglaTmp);
      //int array2[]={1,2,2,2,4};
      int array2[]={0,1,2,3,3};
      ReglasE.put("Archivos",array2);
      
      reglaTmp=new ArrayList(10);
      reglaTmp.add("CONSTANTES");
      reglaTmp.add("[");
      reglaTmp.add("<Identificador>");
      reglaTmp.add("<Op_Asign>");
      reglaTmp.add("<Cadena>");
      reglaTmp.add("<Entero>");
      reglaTmp.add("<Real>");
      reglaTmp.add("<Logico>");
      reglaTmp.add(",");
      reglaTmp.add("]");
      Reglas.put("Constantes",reglaTmp);
      //int array3[]={5,6,7,8,9,9,9,9,6,10};
      int array3[]={0,5,6,7,8,8,8,8,9,9};
      ReglasE.put("Constantes",array3);
      
      reglaTmp=new ArrayList(16);
      reglaTmp.add("<Identificador>");
      reglaTmp.add("<Identificador>");// 1
      reglaTmp.add("VARIABLES");
      reglaTmp.add("[");
      reglaTmp.add("<Op_Asign>");
      reglaTmp.add(",");
      reglaTmp.add(";");
      reglaTmp.add("<Cadena>");
      reglaTmp.add("<Entero>");
      reglaTmp.add("<Real>");
      reglaTmp.add("<Logico>");
      reglaTmp.add("ENTERO");
      reglaTmp.add("REAL");
      reglaTmp.add("CADENA");
      reglaTmp.add("LOGICO");
      reglaTmp.add("]");
      Reglas.put("Variables",reglaTmp);
      //int array4[]={14,18,11,12,16,13,12,17,17,17,17,13,13,13,13,15};
      //int array4[]={13,16,0
      int array4[]={13,13,0,11,14,14,14,16,16,16,16,12,12,12,12,14};
      ReglasE.put("Variables",array4);
      
      reglaTmp=new ArrayList(44);
      reglaTmp.add("[");//                  0-20
      reglaTmp.add("<Entero>");//           1-24
      //Desviacion para arreglo simple
      reglaTmp.add("}");//                  2-25
      reglaTmp.add(",");//                  3-21
      reglaTmp.add(";");//                  4-20
      reglaTmp.add("<-");//                 5-28
      reglaTmp.add("]");//                  6-31
      reglaTmp.add("[");//                  7-29
      reglaTmp.add("<Entero>");//           8-30
      reglaTmp.add("<Real>");//             9-30
      reglaTmp.add("<Cadena>");//          10-30
      reglaTmp.add("<Logico>");//          11-30
      reglaTmp.add(",");//                 12-29
      reglaTmp.add("]");//                 13-31
      reglaTmp.add(",");//                 14-21
      reglaTmp.add(";");//                 15-20
      reglaTmp.add("]");//                 16-27
      //Desviacion para arreglo bidimensional
      reglaTmp.add(",");//                 17-26
      reglaTmp.add("<Entero>");//          18-32
      reglaTmp.add("}");//                 19-33
      reglaTmp.add(",");//                 20-21
      reglaTmp.add(";");//                 21-20
      reglaTmp.add("<-");//                22-34
      reglaTmp.add("]");//                 23-38
      reglaTmp.add("[");//                 24-35
      reglaTmp.add("[");//                 25-36
      reglaTmp.add("<Entero>");//          26-37
      reglaTmp.add("<Real>");//            27-37
      reglaTmp.add("<Cadena>");//          28-37
      reglaTmp.add("<Logico>");//          29-37
      reglaTmp.add(",");//                 30-36
      reglaTmp.add("]");//                 31-38
      reglaTmp.add(",");//                 32-35
      reglaTmp.add("]");//                 33-39
      reglaTmp.add(",");//                 34-21
      reglaTmp.add(";");//                 35-20
      reglaTmp.add("]");//                 36-27
      //Elementos generales:
      reglaTmp.add("ARREGLOS");//          37-19
      reglaTmp.add("ENTERO");//            38-21
      reglaTmp.add("REAL");//              39-21
      reglaTmp.add("CADENA");//            40-21
      reglaTmp.add("LOGICO");//            41-21
      reglaTmp.add("<Identificador>");//   42-22
      reglaTmp.add("{");//                 43-23
      Reglas.put("Arreglos",reglaTmp);
      /*int array5[]={20,24,
                    25,21,20,28,31,29,30,30,30,30,29,31,21,20,27,
                    26,32,33,21,20,34,38,35,36,37,37,37,37,36,38,35,39,21,20,27,
                    19,21,21,21,21,22,23};*/
      int array5[]={19,23,
                    24,25,25,25,30,28,29,29,29,29,30,30,31,31,31,
                    24,26,32,33,33,33,37,34,35,36,36,36,36,37,37,38,38,39,39,39,
                    0,20,20,20,20,21,22};
      ReglasE.put("Arreglos",array5);
      
      reglaTmp=new ArrayList(25);
      reglaTmp.add("[");//                  0-41
      reglaTmp.add("<Identificador>");//    1-42
      reglaTmp.add("[");//                  2-43
      reglaTmp.add("<Identificador>");//    3-45
      reglaTmp.add(",");//                  4-44
      reglaTmp.add(";");//                  5-43
      reglaTmp.add("]");//                  6-46
      reglaTmp.add(";");//                  7-41
      reglaTmp.add("]");//                  8-52
      reglaTmp.add("{");//                  9-47
      reglaTmp.add("<Entero>");//          10-48
      reglaTmp.add("}");//                 11-51
      reglaTmp.add(",");//                 12-49
      reglaTmp.add("<Entero>");//          13-50
      reglaTmp.add("}");//                 14-51
      reglaTmp.add(";");//                 15-41
      reglaTmp.add("]");//                 16-52
      reglaTmp.add("REGISTROS");//         17-40
      reglaTmp.add("ENTERO");//            18-44
      reglaTmp.add("REAL");//              19-44
      reglaTmp.add("CADENA");//            20-44
      reglaTmp.add("LOGICO");//            21-44
      reglaTmp.add("<Identificador>");//   22-42
      reglaTmp.add("[");//                 23-43
      reglaTmp.add("{");//                 24-47
      Reglas.put("Registros",reglaTmp);
      //int array6[]={41,42,43,45,44,43,46,41,52,47,48,51,49,50,51,41,52,40,44,44,44,44,42,43,47};
      int array6[]={40,41,42,44,45,45,45,46,46,46,47,48,48,49,50,51,51,0,43,43,43,43,41,42,46};
      ReglasE.put("Registros",array6);
      
      reglaTmp=new ArrayList(6);
      reglaTmp.add("PROTOTIPOS");//        0
      reglaTmp.add("[");//                 1
      reglaTmp.add("]");//                 2
      reglaTmp.add("PROCEDIMIENTO");//     3 reuse
      reglaTmp.add("FUNCION");//           4 reuse
      reglaTmp.add(";");//                 5
      Reglas.put("Prototipos",reglaTmp);
      //int array7[]={53,54,56,54,54,54};
      int array7[]={0,53,55,54,54,55};
      ReglasE.put("Prototipos",array7);
      
      reglaTmp=new ArrayList(6);
      reglaTmp.add("MODULOS");//           0
      reglaTmp.add("[");//                 1
      reglaTmp.add("]");//                 2
      reglaTmp.add("PROCEDIMIENTO");//     3 reuse
      reglaTmp.add("FUNCION");//           4 reuse
      reglaTmp.add(";");//                 5
      Reglas.put("Modulos",reglaTmp);
      //int array8[]={57,58,61,58,58,58};
      int array8[]={0,57,60,58,58,60};
      ReglasE.put("Modulos",array8);
      
      reglaTmp=new ArrayList(8);
      reglaTmp.add("PROCEDIMIENTO");//     0
      reglaTmp.add("<Identificador>");//   1
      reglaTmp.add("{");//                 2
      reglaTmp.add("}");//                 3 reuse
      reglaTmp.add("ENTERO");//            4 reuse
      reglaTmp.add("REAL");//              5 reuse
      reglaTmp.add("CADENA");//            6 reuse
      reglaTmp.add("LOGICO");//            7 reuse
      Reglas.put("EncabezadoProc",reglaTmp);
      //int array9[]={63,64,65,66,65,65,65,65};
      int array9[]={211,63,64,65,65,65,65,65};
      ReglasE.put("EncabezadoProc",array9);
      
      reglaTmp=new ArrayList(12);
      reglaTmp.add("ENTERO");//             0-69 reuse
      reglaTmp.add("REAL");//               1-69 reuse
      reglaTmp.add("CADENA");//             2-69 reuse
      reglaTmp.add("LOGICO");//             3-69 reuse
      reglaTmp.add("ENTERO");//             4-71 reuse
      reglaTmp.add("REAL");//               5-71 reuse
      reglaTmp.add("CADENA");//             6-71 reuse
      reglaTmp.add("LOGICO");//             7-71 reuse
      reglaTmp.add("FUNCION");//            8-67
      reglaTmp.add("<Identificador>");//    9-68
      reglaTmp.add("{");//                 10-69
      reglaTmp.add("}");//                 11-71
      Reglas.put("EncabezadoFunc",reglaTmp);
      //int array10[]={69,69,69,69,71,71,71,71,67,68,69,71};
      int array10[]={69,69,69,69,71,71,71,71,212,67,68,70};
      ReglasE.put("EncabezadoFunc",array10);
      
      reglaTmp=new ArrayList(11);
      reglaTmp.add("<Entero>");//           0-75
      reglaTmp.add("<Entero>");//           1-78
      reglaTmp.add("<Identificador>");//    2-73
      reglaTmp.add("{");//                  3-74
      reglaTmp.add("}");//                  4-76
      reglaTmp.add(",");//                  5-72
      reglaTmp.add(";");//                  6-79
      reglaTmp.add("ENTERO");//             7-72
      reglaTmp.add("REAL");//               8-72
      reglaTmp.add("CADENA");//             9-72
      reglaTmp.add("LOGICO");//            10-72
      Reglas.put("Parametros",reglaTmp);
      //Revisar cuando se espera una coma al estar dentro de dimensiones (al caso 77)
      //int array11[]={75,78,73,74,76,72,79,72,72,72,72};
      int array11[]={74,77,72,73,78,73,76,79,79,79,79};
      ReglasE.put("Parametros",array11);
      
      reglaTmp=new ArrayList(4);
      reglaTmp.add("VARIABLES");//Reuse
      reglaTmp.add("ARREGLOS");//Reuse
      reglaTmp.add("REGISTROS");//Reuse
      reglaTmp.add("INICIO");//Reuse
      Reglas.put("Cuerpo",reglaTmp);
      int array12[]={80,80,80,83};
      ReglasE.put("Cuerpo",array12);
      
      reglaTmp=new ArrayList(5);
      reglaTmp.add("INICIO");
      reglaTmp.add("[");
      reglaTmp.add(";");
      reglaTmp.add("]");
      reglaTmp.add("FIN");
      //IMPORTANTE: Si no es ninguno de estos elementos, entrar en Instruccion, 
      //            a menos que ya se haya encontrado el corchete de cierre
      Reglas.put("Bloque",reglaTmp);
      //int array13[]={86,87,89,91,92};
      int array13[]={85,86,88,90,91};
      ReglasE.put("Bloque",array13);
      
      //------------------- Fragmentos para analizar instruccion -------------------
      reglaTmp=new ArrayList(6);
      reglaTmp.add("SI");//                0-102
      reglaTmp.add("MIENTRAS");//          1-103
      reglaTmp.add("HACER");//             2-104
      reglaTmp.add("DESDE");//             3-105
      reglaTmp.add("DEPENDIENDO");//       4-106
      reglaTmp.add("LEER");//              5-111
      Reglas.put("Instruccion",reglaTmp);
      //int array14[]={102,103,104,105,106,111};
      int array14[]={93,93,93,93,93,93};
      ReglasE.put("Instruccion",array14);
      
      reglaTmp=new ArrayList(6);
      reglaTmp.add("<Identificador>");//   0-94
      reglaTmp.add("(");//                 1-95
      reglaTmp.add(")");//                 2-101
      reglaTmp.add("<-");//                3-98
      reglaTmp.add("[");//                 4-99
      reglaTmp.add("{");//                 5-100
      Reglas.put("InstruccionIdent",reglaTmp);
      //int array15[]={94,95,101,98,99,100};
      int array15[]={93,94,96,94,94,94};
      ReglasE.put("InstruccionIdent",array15);
      
      reglaTmp=new ArrayList(3);
      reglaTmp.add("REGRESAR");//          0-107
      reglaTmp.add("(");//                 1-108
      reglaTmp.add(")");//                 2-101
      Reglas.put("InstruccionReturn",reglaTmp);
      //int array16[]={107,108,101};
      int array16[]={93,107,109};
      ReglasE.put("InstruccionReturn",array16);
      
      reglaTmp=new ArrayList(3);
      reglaTmp.add("ESCRIBIR");//          0-112
      reglaTmp.add("(");//                 1-113
      reglaTmp.add(")");//                 2-101
      Reglas.put("InstruccionWrite",reglaTmp);
      //int array17[]={112,113,101};
      int array17[]={93,112,114};
      ReglasE.put("InstruccionWrite",array17);
      
      reglaTmp=new ArrayList(3);
      reglaTmp.add("ESCRIBIRCSL");//       0-116
      reglaTmp.add("(");//                 1-117
      reglaTmp.add(")");//                 2-101
      Reglas.put("InstruccionWriteLN",reglaTmp);
      //int array18[]={116,117,101};
      int array18[]={93,116,118};
      ReglasE.put("InstruccionWriteLN",array18);
      
      reglaTmp=new ArrayList(4);
      reglaTmp.add("DTPT");//              0-120
      reglaTmp.add("LPPT");//              1-120
      reglaTmp.add("(");//                 2-121
      reglaTmp.add(")");//                 3-101
      Reglas.put("InstruccionPT",reglaTmp);
      //int array19[]={120,120,121,101};
      int array19[]={93,93,120,121};
      ReglasE.put("InstruccionPT",array19);
      //--------------------- Fin de fragmentos de instruccion ---------------------
      
      reglaTmp=new ArrayList(5);
      reglaTmp.add("SI");
      reglaTmp.add("SINO");
      reglaTmp.add("SINOSI");
      Reglas.put("Si",reglaTmp);
      //int array20[]={122,125,122};
      int array20[]={213,124,124};
      ReglasE.put("Si",array20);
      
      reglaTmp=new ArrayList(2);
      reglaTmp.add("MIENTRAS");
      reglaTmp.add("INICIO");// reuse
      Reglas.put("Mientras",reglaTmp);
      //int array21[]={127,128};
      int array21[]={214,128};
      ReglasE.put("Mientras",array21);
      
      reglaTmp=new ArrayList(3);
      reglaTmp.add("HACER");
      reglaTmp.add("INICIO");// reuse
      reglaTmp.add("HASTA");
      Reglas.put("Hacer",reglaTmp);
      //int array22[]={130,130,132};
      int array22[]={215,130,131};
      ReglasE.put("Hacer",array22);
      
      reglaTmp=new ArrayList(26);
      reglaTmp.add("DESDE");//             0-134
      reglaTmp.add("<Identificador>");//   1-135
      reglaTmp.add("{");//                 2-136
      //Para entrar a expresion:
      reglaTmp.add("(");//                 3-136 reuse
      reglaTmp.add("+");//                 4-136 reuse
      reglaTmp.add("-");//                 5-136 reuse
      reglaTmp.add("<Identificador>");//   6-136 reuse
      reglaTmp.add("<Entero>");//          7-136 reuse
      reglaTmp.add("<Real>");//            8-136 reuse
      reglaTmp.add("<Cadena>");//          9-136 reuse
      reglaTmp.add("<Logico>");//         10-136 reuse
      //Fin de expresion
      reglaTmp.add(",");//                11-138
      //Para entrar a expresion:
      reglaTmp.add("(");//                12-138 reuse
      reglaTmp.add("+");//                13-138 reuse
      reglaTmp.add("-");//                14-138 reuse
      reglaTmp.add("<Identificador>");//  15-138 reuse
      reglaTmp.add("<Entero>");//         16-138 reuse
      reglaTmp.add("<Real>");//           17-138 reuse
      reglaTmp.add("<Cadena>");//         18-138 reuse
      reglaTmp.add("<Logico>");//         19-138 reuse
      //Fin de expresion
      reglaTmp.add("}");//                20-140
      reglaTmp.add("INC");//              21-141
      reglaTmp.add("DEC");//              22-141
      reglaTmp.add("INICIO");//           23-140 reuse
      reglaTmp.add("<Entero>");//         24-142
      reglaTmp.add("INICIO");//           25-142 reuse
      Reglas.put("Desde",reglaTmp);
      //int array23[]={134,135,136,136,136,136,136,136,136,136,136,138,138,138,138,138,138,138,138,138,140,141,141,140,142,142};
      int array23[]={216,134,135,136,136,136,136,136,136,136,136,137,138,138,138,138,138,138,138,138,139,140,140,140,141,142};
      ReglasE.put("Desde",array23);
      
      reglaTmp=new ArrayList(16);
      reglaTmp.add("DEPENDIENDO");//       0-144
      reglaTmp.add("<Identificador>");//   1-145
      reglaTmp.add("[");//                 2-146
      reglaTmp.add("CASO");//              3-147
      reglaTmp.add("<Identificador>");//   4-148
      reglaTmp.add("<Entero>");//          5-148
      reglaTmp.add("<Real>");//            6-148
      reglaTmp.add("<Cadena>");//          7-148
      reglaTmp.add("<Logico>");//          8-148
      reglaTmp.add("|");//                 9-147
      reglaTmp.add("INICIO");//           10-148 reuse
      reglaTmp.add(";");//                11-150
      reglaTmp.add("CUALQUIER");//        12-151
      reglaTmp.add("OTRO");//             13-152
      reglaTmp.add("INICIO");//           14-152 reuse
      reglaTmp.add("]");//                15-154
      Reglas.put("Dependiendo",reglaTmp);
      //int array24[]={144,145,146,147,148,148,148,148,148,147,148,150,151,152,152,154};
      int array24[]={217,144,145,146,147,147,147,147,147,148,148,149,150,151,152,153};
      ReglasE.put("Dependiendo",array24);
      
      reglaTmp=new ArrayList(1);
      reglaTmp.add("<-");
      Reglas.put("Asignacion",reglaTmp);
      int arrayX[]={230};//155
      ReglasE.put("Asignacion",arrayX);
      
      reglaTmp=new ArrayList(23);
      //Para entrar a expresion:
      reglaTmp.add("(");//                 0-160 reuse
      reglaTmp.add("+");//                 1-160 reuse
      reglaTmp.add("-");//                 2-160 reuse
      reglaTmp.add("<Identificador>");//   3-160 reuse
      reglaTmp.add("<Entero>");//          4-160 reuse
      reglaTmp.add("<Real>");//            5-160 reuse
      reglaTmp.add("<Cadena>");//          6-160 reuse
      reglaTmp.add("<Logico>");//          7-160 reuse
      //Fin de expresion
      reglaTmp.add(",");//                 8-162
      //Para entrar a expresion:
      reglaTmp.add("(");//                 9-162 reuse
      reglaTmp.add("+");//                10-162 reuse
      reglaTmp.add("-");//                11-162 reuse
      reglaTmp.add("<Identificador>");//  12-162 reuse
      reglaTmp.add("<Entero>");//         13-162 reuse
      reglaTmp.add("<Real>");//           14-162 reuse
      reglaTmp.add("<Cadena>");//         15-162 reuse
      reglaTmp.add("<Logico>");//         16-162 reuse
      //Fin de expresion
      reglaTmp.add("}");//                17-164
      reglaTmp.add("<-");//               18-165
      reglaTmp.add("[");//                19-157
      reglaTmp.add("<Identificador>");//  20-158
      reglaTmp.add("]");//                21-159
      //Desvio hacia dimensiones de arreglo
      reglaTmp.add("{");//                22-160
      Reglas.put("AsignacionReg",reglaTmp);
      /*int array25[]={160,160,160,160,160,160,160,160,
                     162,162,162,162,162,162,162,162,162,164,165,157,158,159,160};*/
      int array25[]={160,160,160,160,160,160,160,160,
                     161,162,162,162,162,162,162,162,162,163,159,231,157,158,159};
      ReglasE.put("AsignacionReg",array25);
      
      reglaTmp=new ArrayList(46);
      //Desviacion hacia asignacion normal
      //Para entrar a expresion:
      reglaTmp.add("(");//                 0-167
      reglaTmp.add("+");//                 1-167
      reglaTmp.add("-");//                 2-167
      reglaTmp.add("<Identificador>");//   3-167
      reglaTmp.add("<Entero>");//          4-167
      reglaTmp.add("<Real>");//            5-167
      reglaTmp.add("<Cadena>");//          6-167
      reglaTmp.add("<Logico>");//          7-167
      //Fin de expresion
      reglaTmp.add(",");//                 8-168
      //Para entrar a expresion:
      reglaTmp.add("(");//                 9-169
      reglaTmp.add("+");//                10-169
      reglaTmp.add("-");//                11-169
      reglaTmp.add("<Identificador>");//  12-169
      reglaTmp.add("<Entero>");//         13-169
      reglaTmp.add("<Real>");//           14-169
      reglaTmp.add("<Cadena>");//         15-169
      reglaTmp.add("<Logico>");//         16-169
      //Fin de expresion
      reglaTmp.add("}");//                17-168
      reglaTmp.add("<-");//               18-171
      //Desviacion a inicializacion unidimensional
      //Para entrar a expresion:
      reglaTmp.add("(");//                19-176
      reglaTmp.add("+");//                20-176
      reglaTmp.add("-");//                21-176
      reglaTmp.add("<Identificador>");//  22-176
      reglaTmp.add("<Entero>");//         23-176
      reglaTmp.add("<Real>");//           24-176
      reglaTmp.add("<Cadena>");//         25-176
      reglaTmp.add("<Logico>");//         26-176
      //Fin de expresion
      reglaTmp.add(",");//                27-177
      reglaTmp.add("]");//                28-177
      //Desviacion a inicializacion bidimensional
      reglaTmp.add("]");//                29-179
      reglaTmp.add("]");//                30-181
      reglaTmp.add("[");//                31-180
      //Para entrar a expresion:
      reglaTmp.add("(");//                32-178
      reglaTmp.add("+");//                33-178
      reglaTmp.add("-");//                34-178
      reglaTmp.add("<Identificador>");//  35-178
      reglaTmp.add("<Entero>");//         36-178
      reglaTmp.add("<Real>");//           37-178
      reglaTmp.add("<Cadena>");//         38-178
      reglaTmp.add("<Logico>");//         39-178
      //Fin de expresion
      reglaTmp.add(",");//                40-181
      reglaTmp.add(",");//                41-179
      reglaTmp.add("{");//                42-232
      reglaTmp.add("}");//                43-167
      reglaTmp.add("<-");//               44-174
      reglaTmp.add("[");//                45-175
      Reglas.put("AsignacionArr",reglaTmp);
      /*t array26[]={167,167,167,167,167,167,167,167,168,169,169,169,169,169,169,169,169,168,171,
                     176,176,176,176,177,177,179,181,176,178,178,178,178,181,179,232,167,174,175};*/
      int array26[]={167,167,167,167,167,167,167,167,168,169,169,169,169,169,169,169,169,168,171,
                     176,176,176,176,176,176,176,176,177,177,
                     179,181,180,178,178,178,178,178,178,178,178,181,179,232,167,174,175};
      ReglasE.put("AsignacionArr",array26);
      
      reglaTmp=new ArrayList(8);
      reglaTmp.add("<Identificador>");//   0-185
      reglaTmp.add("<Identificador>");//   1-187
      reglaTmp.add("(");//                 2-184
      reglaTmp.add("[");//                 3-186
      reglaTmp.add("]");//                 4-188
      reglaTmp.add("{");//                 5-185 reuse
      reglaTmp.add(",");//                 6-184
      reglaTmp.add(")");//                 7-190
      Reglas.put("Leer",reglaTmp);
      //int array27[]={185,187,184,186,188,185,184,190};
      int array27[]={184,186,183,185,187,185,185,185};
      ReglasE.put("Leer",array27);
      
      reglaTmp=new ArrayList(19);
      reglaTmp.add("{");//                 0-192
      //Para entrar a expresion:
      reglaTmp.add("(");//                 1-192 reuse
      reglaTmp.add("+");//                 2-192 reuse
      reglaTmp.add("-");//                 3-192 reuse
      reglaTmp.add("<Identificador>");//   4-192 reuse
      reglaTmp.add("<Entero>");//          5-192 reuse
      reglaTmp.add("<Real>");//            6-192 reuse
      reglaTmp.add("<Cadena>");//          7-192 reuse
      reglaTmp.add("<Logico>");//          8-192 reuse
      //Fin de expresion
      reglaTmp.add(",");//                 9-194
      //Para entrar a expresion:
      reglaTmp.add("(");//                10-194 reuse
      reglaTmp.add("+");//                11-194 reuse
      reglaTmp.add("-");//                12-194 reuse
      reglaTmp.add("<Identificador>");//  13-194 reuse
      reglaTmp.add("<Entero>");//         14-194 reuse
      reglaTmp.add("<Real>");//           15-194 reuse
      reglaTmp.add("<Cadena>");//         16-194 reuse
      reglaTmp.add("<Logico>");//         17-194 reuse
      //Fin de expresion
      reglaTmp.add("}");//                18-196
      Reglas.put("Dimensiones_Ident",reglaTmp);
      //int array28[]={192,192,192,192,192,192,192,192,192,194,194,194,194,194,194,194,194,194,196};
      int array28[]={191,192,192,192,192,192,192,192,192,193,194,194,194,194,194,194,194,194,195};
      ReglasE.put("Dimensiones_Ident",array28);
      
      reglaTmp=new ArrayList(22);
      reglaTmp.add("(");//                 0-200
      reglaTmp.add("+");//                 1-198
      reglaTmp.add("-");//                 2-198
      reglaTmp.add("<Identificador>");//   3-202
      reglaTmp.add("<Entero>");//          4-199
      reglaTmp.add("<Real>");//            5-199
      reglaTmp.add("<Cadena>");//          6-199
      reglaTmp.add("<Logico>");//          7-199
      reglaTmp.add("{");//                 8-202 reuse
      //Desvio a parentesis
      reglaTmp.add("(");//                 9-203
      //Desvio a corchetes
      reglaTmp.add("[");//                10-206
      reglaTmp.add("<Identificador>");//  11-207
      reglaTmp.add("]");//                12-208
      reglaTmp.add("{");//                13-208 reuse
      reglaTmp.add(")");//                14-204
      reglaTmp.add("+");//                15-197
      reglaTmp.add("-");//                16-197
      reglaTmp.add("*");//                17-197
      reglaTmp.add("/");//                18-197
      reglaTmp.add("POT");//              19-197
      reglaTmp.add("DIV");//              20-197
      reglaTmp.add("MOD");//              21-197
      Reglas.put("Expresion",reglaTmp);
      int array29[]={197,197,197,197,197,197,197,197,202,202,202,206,207,208,205,234,234,236,236,238,236,236};
      ReglasE.put("Expresion",array29);
      
      reglaTmp=new ArrayList(1);
      reglaTmp.add(",");
      Reglas.put("Parametros_Ident",reglaTmp);
      int arrayY[]={221};//220
      ReglasE.put("Parametros_Ident",arrayY);
      
      reglaTmp=new ArrayList(12);
      reglaTmp.add("!");//                 0-223
      reglaTmp.add("<Op_Rel>");//          1-223
      reglaTmp.add("&");//                 2-222
      reglaTmp.add("|");//                 3-222
      //Para entrar a expresion:
      reglaTmp.add("(");//                 4-223 reuse
      reglaTmp.add("+");//                 5-223 reuse
      reglaTmp.add("-");//                 6-223 reuse
      reglaTmp.add("<Identificador>");//   7-223 reuse
      reglaTmp.add("<Entero>");//          8-223 reuse
      reglaTmp.add("<Real>");//            9-223 reuse
      reglaTmp.add("<Cadena>");//         10-223 reuse
      reglaTmp.add("<Logico>");//         11-223 reuse
      //Fin de expresion
      Reglas.put("Regla_OR",reglaTmp);
      int array30[]={222,224,243,241,223,223,223,223,223,223,223,223};
      ReglasE.put("Regla_OR",array30);
      
      reglaTmp=new ArrayList(6);
      reglaTmp.add("CONSTANTES");
      reglaTmp.add("VARIABLES");
      reglaTmp.add("ARREGLOS");
      reglaTmp.add("REGISTROS");
      reglaTmp.add("PROTOTIPOS");
      reglaTmp.add("MODULOS");
      Reglas.put("ArchivosH",reglaTmp);
      //int array31[]={5,11,19,40,53,57};
      int array31[]={250,250,250,250,250,250};
      ReglasE.put("ArchivosH",array31);
   }
}