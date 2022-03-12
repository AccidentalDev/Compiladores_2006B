import java.io.*;
import javax.swing.text.*;

public class LexicoAbrir{
   private Editor editor;
   private Ventana win;
   protected String lexema,token,fuente,reserved[];
   protected int i,estado,lineCounter,tipoChar;
   private char c;
   
   public LexicoAbrir(Ventana v,Editor e){
      editor=e;
      win=v;
      lineCounter=0;
      i=-1;
      lexema="";
      estado=0;
      reserved=new String[37];
      getReserved();
      
      try{
         fuente=win.areaTexto.getDocument().getText(0,win.areaTexto.getDocument().getLength());
      }catch(BadLocationException ble){
         System.out.println("Error al capturar el texto para analizarlo");
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
   
   public void analiza(){
      win.rowStates.clear();
      token=dameToken();
      while(token!="\0"){
         token=dameToken();
      }
      win.areaTexto.setCaretPosition(fuente.length());
      //editor.colorFuente(editor.atributos[2]);
      
      /*if(estado==14||estado==15){
         System.out.println("Se termino es estado 14|15");
         System.out.println("Agregando la linea: "+(lineCounter+1));
         if(!win.rowStates.contains(new Integer(lineCounter+1)))
            win.rowStates.add(new Integer(lineCounter+1));
      }*/
   }
   public String dameToken(){
      lexema="";
      estado=0;
      while(i<fuente.length()){
         i++;
         if(i==fuente.length())
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
                           editor.colorFuente(win,7,i,1);
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
                           editor.colorFuente(win,5,i-lexema.length(),lexema.length());
                           i--;
                           return "<Entero>";
                        }
            }break;
            case 2:{
                  if(Character.isDigit(c)){
                     estado=3;
                     lexema=lexema+c;
                  }else{
                     editor.colorFuente(win,5,i-lexema.length(),lexema.length());
                     editor.tachar(win,i-lexema.length(),lexema.length());
                     i--;
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
                        editor.colorFuente(win,5,i-lexema.length(),lexema.length());
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
                        //ERROR
                        editor.colorFuente(win,5,i-lexema.length(),lexema.length());
                        editor.tachar(win,i-lexema.length(),lexema.length());
                        i--;
                        return "<Real>";
                     }
            }break;
            case 5:{
                  if(Character.isDigit(c)){
                     estado=6;
                     lexema=lexema+c;
                  }else{
                     //ERROR
                     editor.colorFuente(win,5,i-lexema.length(),lexema.length());
                     editor.tachar(win,i-lexema.length(),lexema.length());
                     i--;
                     return "<Real>";
                  }
            }break;
            case 6:{
                  if(Character.isDigit(c)){
                     estado=7;
                     lexema=lexema+c;
                  }else{
                     editor.colorFuente(win,5,i-lexema.length(),lexema.length());
                     i--;
                     return "<Real>";
                  }
            }break;
            case 7:{
                  editor.colorFuente(win,5,i-lexema.length(),lexema.length());
                  i--;
                  return "<Real>";
            }//break;
            case 8:{
                  tipoChar=dameTipoChar(c);
                  if(tipoChar>=1&&tipoChar<=3){
                     lexema=lexema+c;
                  }else{
                     if(lexema.equals("DIV"))
                        editor.colorFuente(win,6,i-lexema.length(),lexema.length());
                     else
                        if(lexema.equals("MOD"))
                           editor.colorFuente(win,6,i-lexema.length(),lexema.length());
                        else
                           if(lexema.equals("POT"))
                              editor.colorFuente(win,6,i-lexema.length(),lexema.length());
                           else
                              if(lexema.equals("VERDADERO"))
                                 editor.colorFuente(win,2,i-lexema.length(),lexema.length());
                              else
                                 if(lexema.equals("FALSO"))
                                    editor.colorFuente(win,2,i-lexema.length(),lexema.length());
                                 else
                                    if(findReserved(lexema)>-1)
                                       editor.colorFuente(win,2,i-lexema.length(),lexema.length());
                                    else
                                       editor.colorFuente(win,1,i-lexema.length(),lexema.length());
                     i--;
                     return "<Palabra>";
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
                        //ERROR
                        editor.colorFuente(win,1,i-lexema.length(),lexema.length());
                        //editor.tachar(win,i-lexema.length(),lexema.length());
                        i--;
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
                        //ERROR
                        editor.colorFuente(win,4,i-lexema.length(),lexema.length());
                        editor.tachar(win,i-lexema.length(),lexema.length());
                        i--;
                        return "<Cadena>";
                     }
            }break;
            case 11:{
                  editor.colorFuente(win,4,i-lexema.length(),lexema.length());
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
                              editor.colorFuente(win,6,i-lexema.length(),lexema.length());
                              i--;
                              return "<Op_Rel>";
                           }
            }break;
            case 13:{
                  if(c=='-'){
                     estado=14;
                     lexema=lexema+c;
                  }else{
                     //ERROR
                     editor.colorFuente(win,3,i-lexema.length(),lexema.length());
                     editor.tachar(win,i-lexema.length(),lexema.length());
                     i--;
                     return "<Comentario>";
                  }
            }break;
            case 14:{
                  if(c=='-'){
                     estado=15;
                     lexema=lexema+c;
                  }else
                     if(c!='\0'){
                        lexema=lexema+c;
                        if(fuente.charAt(i-1)=='\n'){
                           if(!win.rowStates.contains(new Integer(lineCounter)))
                              win.rowStates.add(new Integer(lineCounter));
                        }
                        if(c=='\n')
                           lineCounter++;
                     }else{
                        if(fuente.charAt(i-1)=='\n'){
                           if(!win.rowStates.contains(new Integer(lineCounter)))
                              win.rowStates.add(new Integer(lineCounter));
                        }
                        //ERROR
                        editor.colorFuente(win,3,i-lexema.length(),lexema.length());
                        //editor.tachar(win,i-lexema.length(),lexema.length());
                        i--;
                        return "<Comentario>";
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
                           if(fuente.charAt(i-1)=='\n'){
                              if(!win.rowStates.contains(new Integer(lineCounter)))
                                 win.rowStates.add(new Integer(lineCounter));
                           }
                           if(c=='\n')
                              lineCounter++;
                        }else{
                           if(fuente.charAt(i-1)=='\n'){
                              if(!win.rowStates.contains(new Integer(lineCounter)))
                                 win.rowStates.add(new Integer(lineCounter));
                           }
                           //ERROR
                           editor.colorFuente(win,3,i-lexema.length(),lexema.length());
                           //editor.tachar(win,i-lexema.length(),lexema.length());
                           i--;
                           return "<Comentario>";
                        }
            }break;
            case 16:{
                  editor.colorFuente(win,3,i-lexema.length(),lexema.length());
                  i--;
                  return "<Comentario>";
            }//break;
            case 17:{
                  if(c=='='){
                     estado=18;
                     lexema=lexema+c;
                  }else{
                     editor.colorFuente(win,6,i-lexema.length(),lexema.length());
                     i--;
                     return "<Op_Rel>";
                  }
            }break;
            case 18:{
                  editor.colorFuente(win,6,i-lexema.length(),lexema.length());
                  i--;
                  return "<Op_Rel>";
            }//break;
            case 19:{
                  editor.colorFuente(win,6,i-lexema.length(),lexema.length());
                  i--;
                  return "<Op_Rel>";
            }//break;
            case 20:{
                  editor.colorFuente(win,6,i-lexema.length(),lexema.length());
                  i--;
                  return "<Op_Asign>";
            }//break;
            case 21:{
                  editor.colorFuente(win,6,i-lexema.length(),lexema.length());
                  i--;
                  return "<Op_Aritm>";
            }//break;
            case 22:{
                  editor.colorFuente(win,6,i-lexema.length(),lexema.length());
                  i--;
                  return "<Op_Log>";
            }//break;
            case 23:{
                  editor.colorFuente(win,8,i-lexema.length(),lexema.length());
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
      }else
         if(car=='_'){
            return 3;
         }else
            if(car=='"'){
               return 4;
            }else
               if(car=='<'){
                  return 5;
               }else
                  if(car=='>'){
                     return 6;
                  }else
                     if(car=='='){
                        return 7;
                     }else
                        if(car=='+'||car=='-'||car=='*'||car=='/'){
                           return 8;
                        }else
                           if(car=='|'||car=='&'||car=='!'){
                              return 9;
                           }else
                              if(car==','||car==';'||car=='('||car==')'||car=='['||car==']'||car=='{'||car=='}'){
                                 return 10;
                              }else
                                 if(Character.isLetter(car)&&car!='º'&&car!='ª'&&car!='ç'&&car!='Ç'){
                                    return 2;
                                 }else
                                    if(car=='\n'){
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
   
}