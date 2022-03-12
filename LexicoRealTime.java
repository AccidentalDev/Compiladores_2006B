import java.io.*;
//import java.util.Enumeration;
import java.awt.Color;
import javax.swing.text.*;

public class LexicoRealTime{
   private Editor editor;
   //private Ventana win;
   protected String lexema,token,fuente,reserved[];
   protected int estado,tipoChar;
   private char c;
   
   public LexicoRealTime(Editor e){
      editor=e;
      //win=v;
      lexema="";
      estado=0;
      reserved=new String[37];
      getReserved();
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
   public void checkPrevLine(Ventana win,int pos){
      String texto=null;
      char car,car2;
      try{
         texto=win.areaTexto.getDocument().getText(0,win.areaTexto.getDocument().getLength());
         car=texto.charAt(pos-2);
         car2=texto.charAt(pos-3);
         System.out.println("Caracter anterior: '"+car+"'");
         System.out.println("Caracter anterior: '"+car2+"'");
         
         int realPos=win.areaTexto.getCaretPosition();
         win.areaTexto.setCaretPosition(pos-2);
         AttributeSet set=win.areaTexto.getCharacterAttributes();
         win.areaTexto.setCaretPosition(realPos);
         
         if(set.containsAttributes(editor.colorComent)&&car!='>'&&car2!='-')
            System.out.println("La linea anterior termina en comentario sin finalizar");
      }catch(BadLocationException ble){
         System.out.println("Error al capturar el texto para analizarlo");
      }
   }
   public void colorea(Ventana win,String renglon,int realCurPos){
      /*if(realCurPos>3)
         checkPrevLine(win,realCurPos);
      */
      if(win.rowStates.contains(new Integer(win.teclado.lineaActual)))
         estado=14;
      else
         estado=0;
      
      lexema="";
      int i;
      for(i=0;i<renglon.length();i++){
         c=renglon.charAt(i);
         
         switch(estado){
            case 0:{
                  tipoChar=dameTipoChar(c);
                  switch(tipoChar){
                     case 1:{//Es digito
                           estado=1;
                           lexema=lexema+c;
                           editor.colorFuente(win,5,realCurPos+i,1);
                     }break;
                     case 2:{//Es letra
                           estado=8;
                           lexema=lexema+c;
                           editor.colorFuente(win,1,realCurPos+i,1);
                     }break;
                     case 3:{//Es guion bajo
                           estado=9;
                           lexema=lexema+c;
                           editor.colorFuente(win,1,realCurPos+i,1);
                     }break;
                     case 4:{//Es comilla
                           estado=10;
                           lexema=lexema+c;
                           editor.colorFuente(win,4,realCurPos+i,1);
                     }break;
                     case 5:{//Es <
                           estado=12;
                           lexema=lexema+c;
                           editor.colorFuente(win,6,realCurPos+i,1);
                     }break;
                     case 6:{//Es >
                           estado=17;
                           lexema=lexema+c;
                           editor.colorFuente(win,6,realCurPos+i,1);
                     }break;
                     case 7:{//Es =
                           estado=18;
                           lexema=lexema+c;
                           editor.colorFuente(win,6,realCurPos+i,1);
                     }break;
                     case 8:{//Es +,-,=,*
                           estado=21;
                           lexema=lexema+c;
                           editor.colorFuente(win,6,realCurPos+i,1);
                     }break;
                     case 9:{//Es |,&,!
                           estado=22;
                           lexema=lexema+c;
                           editor.colorFuente(win,6,realCurPos+i,1);
                     }break;
                     case 10:{//Es delimitador
                           estado=23;
                           lexema=lexema+c;
                           editor.colorFuente(win,8,realCurPos+i,1);
                     }break;
                     case 0:{
                           editor.colorFuente(win,8,realCurPos+i,1);
                     }break;
                     case -1:{//Caracter ilegal
                           editor.colorFuente(win,7,realCurPos+i,1);
                           lexema="";
                     }break;
                  }
            }break;
            case 1:{
                  if(Character.isDigit(c)){
                     lexema=lexema+c;
                     editor.colorFuente(win,5,realCurPos+i,1);
                  }else
                     if(c=='.'){
                        estado=2;
                        lexema=lexema+c;
                        editor.colorFuente(win,5,realCurPos+i,1);
                     }else
                        if(c=='e'||c=='E'){
                           estado=4;
                           lexema=lexema+c;
                           editor.colorFuente(win,5,realCurPos+i,1);
                        }else{
                           editor.colorFuente(win,5,realCurPos+i-lexema.length(),lexema.length());
                           
                           estado=0;
                           lexema="";
                           i--;
                        }
            }break;
            case 2:{
                  if(Character.isDigit(c)){
                     estado=3;
                     lexema=lexema+c;
                     editor.colorFuente(win,5,realCurPos+i,1);
                  }else{
                     //ERROR
                     editor.colorFuente(win,5,realCurPos+i-lexema.length(),lexema.length());
                     editor.tachar(win,realCurPos+i-lexema.length(),lexema.length());
                     estado=0;
                     lexema="";
                     i--;
                  }
            }break;
            case 3:{
                  if(Character.isDigit(c)){
                     lexema=lexema+c;
                     editor.colorFuente(win,5,realCurPos+i,1);
                  }else
                     if(c=='e'||c=='E'){
                        estado=4;
                        lexema=lexema+c;
                        editor.colorFuente(win,5,realCurPos+i,1);
                     }else{
                        editor.colorFuente(win,5,realCurPos+i-lexema.length(),lexema.length());
                        
                        estado=0;
                        lexema="";
                        i--;
                     }
            }break;
            case 4:{
                  if(Character.isDigit(c)){
                     estado=6;
                     lexema=lexema+c;
                     editor.colorFuente(win,5,realCurPos+i,1);
                  }else
                     if(c=='+'||c=='-'){
                        estado=5;
                        lexema=lexema+c;
                        editor.colorFuente(win,5,realCurPos+i,1);
                     }else{
                        //ERROR
                        editor.colorFuente(win,5,realCurPos+i-lexema.length(),lexema.length());
                        editor.tachar(win,realCurPos+i-lexema.length(),lexema.length());
                        estado=0;
                        lexema="";
                        i--;
                     }
            }break;
            case 5:{
                  if(Character.isDigit(c)){
                     estado=6;
                     lexema=lexema+c;
                     editor.colorFuente(win,5,realCurPos+i,1);
                  }else{
                     //ERROR
                     editor.colorFuente(win,5,realCurPos+i-lexema.length(),lexema.length());
                     editor.tachar(win,realCurPos+i-lexema.length(),lexema.length());
                     estado=0;
                     lexema="";
                     i--;
                  }
            }break;
            case 6:{
                  if(Character.isDigit(c)){
                     estado=7;
                     lexema=lexema+c;
                     editor.colorFuente(win,5,realCurPos+i,1);
                  }else{
                     editor.colorFuente(win,5,realCurPos+i-lexema.length(),lexema.length());
                     estado=0;
                     lexema="";
                     i--;
                  }
            }break;
            case 7:{
                  editor.colorFuente(win,5,realCurPos+i-lexema.length(),lexema.length());
                  estado=0;
                  lexema="";
                  i--;
            }break;
            case 8:{
                  tipoChar=dameTipoChar(c);
                  if(tipoChar>=1&&tipoChar<=3){
                     lexema=lexema+c;
                     editor.colorFuente(win,1,realCurPos+i,1);
                  }else{
                     if(lexema.equals("DIV"))
                        editor.colorFuente(win,6,realCurPos+i-lexema.length(),lexema.length());
                     else
                        if(lexema.equals("MOD"))
                           editor.colorFuente(win,6,realCurPos+i-lexema.length(),lexema.length());
                        else
                           if(lexema.equals("POT"))
                              editor.colorFuente(win,6,realCurPos+i-lexema.length(),lexema.length());
                           else
                              if(lexema.equals("VERDADERO"))
                                 editor.colorFuente(win,2,realCurPos+i-lexema.length(),lexema.length());
                              else
                                 if(lexema.equals("FALSO"))
                                    editor.colorFuente(win,2,realCurPos+i-lexema.length(),lexema.length());
                                 else
                                    if(findReserved(lexema)>-1)
                                       editor.colorFuente(win,2,realCurPos+i-lexema.length(),lexema.length());
                                    else
                                       editor.colorFuente(win,1,realCurPos+i-lexema.length(),lexema.length());
                     estado=0;
                     lexema="";
                     i--;
                  }
            }break;
            case 9:{
                  tipoChar=dameTipoChar(c);
                  if(tipoChar==3){
                     lexema=lexema+c;
                     editor.colorFuente(win,1,realCurPos+i,1);
                  }else
                     if(tipoChar==1||tipoChar==2){
                        estado=8;
                        lexema=lexema+c;
                        editor.colorFuente(win,1,realCurPos+i,1);
                     }else{
                        //ERROR
                        editor.colorFuente(win,1,realCurPos+i-lexema.length(),lexema.length());
                        //editor.tachar(win,realCurPos+i-lexema.length(),lexema.length());
                        estado=0;
                        lexema="";
                        i--;
                     }
            }break;
            case 10:{
                  if(c=='"'){
                     estado=11;
                     lexema=lexema+c;
                     editor.colorFuente(win,4,realCurPos+i,1);
                  }else
                     if(c!='\n'&&c!='\0'){
                        lexema=lexema+c;
                        editor.colorFuente(win,4,realCurPos+i,1);
                     }else{
                        //ERROR
                        editor.colorFuente(win,4,realCurPos+i-lexema.length(),lexema.length());
                        editor.tachar(win,realCurPos+i-lexema.length(),lexema.length());
                        /*estado=0;
                        lexema="";
                        i--;*/
                     }
            }break;
            case 11:{
                  editor.colorFuente(win,4,realCurPos+i-lexema.length(),lexema.length());
                  estado=0;
                  lexema="";
                  i--;
            }break;
            case 12:{
                  if(c=='!'){
                     estado=13;
                     lexema=lexema+c;
                     editor.colorFuente(win,3,realCurPos+i-1,2);
                  }else
                     if(c=='='){
                        estado=18;
                        lexema=lexema+c;
                        editor.colorFuente(win,6,realCurPos+i,1);
                     }else
                        if(c=='>'){
                           estado=19;
                           lexema=lexema+c;
                           editor.colorFuente(win,6,realCurPos+i,1);
                        }else
                           if(c=='-'){
                              estado=20;
                              lexema=lexema+c;
                              editor.colorFuente(win,6,realCurPos+i,1);
                           }else{
                              editor.colorFuente(win,6,realCurPos+i-lexema.length(),lexema.length());
                              estado=0;
                              lexema="";
                              i--;
                           }
            }break;
            case 13:{
                  if(c=='-'){
                     estado=14;
                     lexema=lexema+c;
                     editor.colorFuente(win,3,realCurPos+i,1);
                  }else{
                     //ERROR
                     editor.colorFuente(win,3,realCurPos+i-lexema.length(),lexema.length());
                     editor.tachar(win,realCurPos+i-lexema.length(),lexema.length());
                     estado=0;
                     lexema="";
                     i--;
                  }
            }break;
            case 14:{
                  if(c=='-'){
                     estado=15;
                     lexema=lexema+c;
                     editor.colorFuente(win,3,realCurPos+i,1);
                  }else
                     if(c!='\0'){
                        lexema=lexema+c;
                        editor.colorFuente(win,3,realCurPos+i,1);
                     }else{
                        //ERROR
                        editor.colorFuente(win,3,realCurPos+i-lexema.length(),lexema.length());
                        //editor.tachar(win,realCurPos+i-lexema.length(),lexema.length());
                        estado=0;
                        lexema="";
                        i--;
                     }
            }break;
            case 15:{
                  if(c=='-'){
                     lexema=lexema+c;
                     editor.colorFuente(win,3,realCurPos+i,1);
                  }else
                     if(c=='>'){
                        estado=16;
                        lexema=lexema+c;
                        editor.colorFuente(win,3,realCurPos+i,1);
                     }else
                        if(c!='\0'){
                           estado=14;
                           lexema=lexema+c;
                           editor.colorFuente(win,3,realCurPos+i,1);
                        }else{
                           //ERROR
                           editor.colorFuente(win,3,realCurPos+i-lexema.length(),lexema.length());
                           //editor.tachar(win,realCurPos+i-lexema.length(),lexema.length());
                           estado=0;
                           lexema="";
                           i--;
                        }
            }break;
            case 16:{
                  editor.colorFuente(win,3,realCurPos+i-lexema.length(),lexema.length());
                  estado=0;
                  lexema="";
                  i--;
            }break;
            case 17:{
                  if(c=='='){
                     estado=18;
                     lexema=lexema+c;
                     editor.colorFuente(win,6,realCurPos+i,1);
                  }else{
                     editor.colorFuente(win,6,realCurPos+i-lexema.length(),lexema.length());
                     estado=0;
                     lexema="";
                     i--;
                  }
            }break;
            case 18:{
                  editor.colorFuente(win,6,realCurPos+i-lexema.length(),lexema.length());
                  estado=0;
                  lexema="";
                  i--;
            }break;
            case 19:{
                  editor.colorFuente(win,6,realCurPos+i-lexema.length(),lexema.length());
                  estado=0;
                  lexema="";
                  i--;
            }break;
            case 20:{
                  editor.colorFuente(win,6,realCurPos+i-lexema.length(),lexema.length());
                  estado=0;
                  lexema="";
                  i--;
            }break;
            case 21:{
                  editor.colorFuente(win,6,realCurPos+i-lexema.length(),lexema.length());
                  estado=0;
                  lexema="";
                  i--;
            }break;
            case 22:{
                  editor.colorFuente(win,6,realCurPos+i-lexema.length(),lexema.length());
                  estado=0;
                  lexema="";
                  i--;
            }break;
            case 23:{
                  editor.colorFuente(win,8,realCurPos+i-lexema.length(),lexema.length());
                  estado=0;
                  lexema="";
                  i--;
            }break;
         }
      }
      if(estado==10&&c!='"')
         editor.tachar(win,realCurPos+i-lexema.length(),lexema.length()-1);
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
                                       return 0;
                                    }else
                                       if(car=='\t'||car=='\0'||Character.isWhitespace(car)){
                                          return 0;
                                       }else{
                                          return -1;
                                       }
   }
}