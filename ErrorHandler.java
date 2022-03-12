import java.util.ArrayList;

public class ErrorHandler{
   Sintactico sint;
   public ArrayList tokenBuffer,lexemaBuffer,tipoBuffer,posBuffer;
   
   public ErrorHandler(Sintactico s){
      sint=s;
      tokenBuffer=new ArrayList(3);
      lexemaBuffer=new ArrayList(3);
      tipoBuffer=new ArrayList(3);
      posBuffer=new ArrayList(3);
   }
   public String damePadre(int estado){
      if(estado==55)
         return "Prototipos";
      else if(estado==59)
         return "Modulos";
      else if(estado==0)
         return "Programa";
      else if(estado==66)
         return "EncabezadoProc";
      else if(estado==70)
         return "EncabezadoFunc";
      else if(estado==84)
         return "Cuerpo";
      else if(estado==92)
         return "Bloque";
      else if(estado==95)
         return "InstruccionIdent";
      else if(estado==111)
         return "Instruccion";
      else if(estado==109)
         return "InstruccionReturn";
      else if(estado==114)
         return "InstruccionWrite";
      else if(estado==118)
         return "InstruccionWriteLN";
      else if(estado==123)
         return "Si";
      else if(estado==128)
         return "Mientras";
      else if(estado==131)
         return "Hacer";
      else if(estado==137)
         return "Desde";
      else if(estado==149)
         return "Dependiendo";
      else if(estado==156)
         return "Asignacion";
      else if(estado==166)
         return "AsignacionReg";
      else if(estado==173)
         return "AsignacionArr";
      else if(estado==189)
         return "Leer";
      else if(estado==193)
         return "Dimensiones_Ident";
      else if(estado==201)
         return "Expresion";
      else if(estado==221)
         return "Parametros_Ident";
      else if(estado==224)
         return "Regla_OR";
      else if(estado==250)
         return "ArchivosH";
      else
         return " ";
      
   }
   public String[] damePadreStrs(int estado){
      if(estado==55){
         String sincStrings[]={";","]"};
         return sincStrings;
      }else if(estado==59){
         String sincStrings[]={";","]"};
         return sincStrings;
      }else if(estado==0){
         String sincStrings[]={"MODULOS","PRINCIPAL"};
         return sincStrings;
      }else if(estado==66){
         String sincStrings[]={"{","}"};
         return sincStrings;
      }else if(estado==70){
         String sincStrings[]={"{","}"};
         return sincStrings;
      }else if(estado==84){
         String sincStrings[]={"ARREGLOS","REGISTROS"};
         return sincStrings;
      }else if(estado==92){
         String sincStrings[]={";","]","FIN"};
         return sincStrings;
      }else if(estado==95){
         String sincStrings[]={"(",")","<-","{","["};
         return sincStrings;
      }else if(estado==111){
         String sincStrings[]={"SI","MIENTRAS","HACER","DESDE","DEPENDIENDO","LEER"};
         return sincStrings;
      }else if(estado==109){
         String sincStrings[]={"(",")"};
         return sincStrings;
      }else if(estado==114){
         String sincStrings[]={"(",")"};
         return sincStrings;
      }else if(estado==118){
         String sincStrings[]={"(",")"};
         return sincStrings;
      }else if(estado==123){
         String sincStrings[]={"SINO","SINOSI"};
         return sincStrings;
      }else if(estado==128){
         String sincStrings[]={"INICIO"};
         return sincStrings;
      }else if(estado==131){
         String sincStrings[]={"HASTA"};
         return sincStrings;
      }else if(estado==137){
         String sincStrings[]={"}","INC","DEC","<Entero>"};
         return sincStrings;
      }else if(estado==149){
         String sincStrings[]={"CASO","CUALQUIER","OTRO","]"};
         return sincStrings;
      }else if(estado==156){
         String sincStrings[]={"<-"};
         return sincStrings;
      }else if(estado==166){
         String sincStrings[]={"<-","}"};
         return sincStrings;
      }else if(estado==173){
         String sincStrings[]={"<-","}","]"};
         return sincStrings;
      }else if(estado==189){
         String sincStrings[]={",",")"};
         return sincStrings;
      }else if(estado==193){
         String sincStrings[]={",","}"};
         return sincStrings;
      }else if(estado==201){
         String sincStrings[]={")","]","+","-","*","/","DIV","MOD","POT"};
         return sincStrings;
      }else if(estado==221){
         String sincStrings[]={","};
         return sincStrings;
      }else if(estado==224){
         String sincStrings[]={"!","&","|"};
         return sincStrings;
      }else if(estado==250){
         String sincStrings[]={"CONSTANTES","VARIABLES","ARREGLOS","REGISTROS","PROTOTIPOS","MODULOS"};
         return sincStrings;
      }else{
         String sincStrings[]={"]"};
         return sincStrings;
      }
   }
   public int Panico(String reglaID,String sincro[]){
      ArrayList regla;
      int reglaE[],pos;
      System.out.print("    <Regla en panico: "+reglaID+">    ");
      
      regla=(ArrayList)sint.Reglas.get(reglaID);
      reglaE=(int[])sint.ReglasE.get(reglaID);
      
      boolean recupero=false;
      while(!recupero&&(!sint.lexema.equals("\0")&&!sint.lexema.equals("")&&!sint.lexema.equals(null))){
         sint.token=sint.dameToken();
         sint.tipoElem=sint.tipoLexema();
         System.out.println("Lexema en panico: \""+sint.lexema+"\"");
         
         for(int i=0;i<sincro.length;i++)
            if(sint.token.equals(sincro[i])){
               pos=regla.indexOf(sint.token);
               return reglaE[pos];
            }else
               if(sint.lexema.equals(sincro[i])){
                  pos=regla.indexOf(sint.lexema);
                  return reglaE[pos];
               }
      }
      return 0;
   }
   
   public int PanicoMayor(String reglaID,String padreID,String sincro[],String sincroPadre[]){
      ArrayList regla,reglaP;
      int reglaE[],reglaEP[],pos;
      System.out.print("    <Regla en panico mayor: "+reglaID+">    ");
      
      regla=(ArrayList)sint.Reglas.get(reglaID);
      reglaP=(ArrayList)sint.Reglas.get(padreID);
      reglaE=(int[])sint.ReglasE.get(reglaID);
      reglaEP=(int[])sint.ReglasE.get(padreID);
      
      boolean recupero=false;
      while(!recupero&&(!sint.lexema.equals("\0")&&!sint.lexema.equals("")&&!sint.lexema.equals(null))){
         for(int i=0;i<sincro.length;i++)
            if(sint.token.equals(sincro[i])){
               pos=regla.indexOf(sint.token);
               return reglaE[pos];
            }else
               if(sint.lexema.equals(sincro[i])){
                  pos=regla.indexOf(sint.lexema);
                  return reglaE[pos];
               }
               
         for(int i=0;i<sincroPadre.length;i++)
            if(sint.token.equals(sincroPadre[i])){
               pos=reglaP.indexOf(sint.token);
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               return reglaEP[pos];
            }else
               if(sint.lexema.equals(sincroPadre[i])){
                  pos=reglaP.indexOf(sint.lexema);
                  sint.infoStates.pop();
                  sint.padresState.pop();
                  sint.nivel--;
                  return reglaEP[pos];
               }
               
         sint.token=sint.dameToken();
         sint.tipoElem=sint.tipoLexema();
      }
      return 0;
   }
   
   public int recuperaPrograma(){
      ArrayList regla;
      int reglaE[],pos=0;
      
      //Aqui tiene que buscar en todas las reglas posibles en este nivel
      String reglas[]={"Archivos","Constantes","Variables","Arreglos","Registros","Prototipos","Modulos"};
      boolean isHere=false;
      int reglasPos=0;
      for(int i=0;i<reglas.length&&!isHere;i++){
         regla=(ArrayList)sint.Reglas.get(reglas[i]);
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos!=-1){
            isHere=true;
            reglasPos=i;
            
            if(reglas[i].equals("Constantes")||reglas[i].equals("Variables")||reglas[i].equals("Arreglos")||reglas[i].equals("Registros")||reglas[i].equals("Prototipos")||reglas[i].equals("Modulos")){
               sint.infoStates.push(new Integer(0));
               sint.padresState.push(new Integer(0));
               sint.nivel++;
            }
               
            if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
               if(pos==3||pos==4)
                  sint.reuse=true;
         }
      }
      
      if(!isHere){
         //Repite este procedimiento pero con el siguiente token
         sint.token=sint.dameToken();
         sint.tipoElem=sint.tipoLexema();
         
         isHere=false;
         reglasPos=0;
         for(int i=0;i<reglas.length&&!isHere;i++){
            regla=(ArrayList)sint.Reglas.get(reglas[i]);
            pos=regla.indexOf(sint.lexema);
            if(pos==-1)
               pos=regla.indexOf(sint.token);
            
            if(pos!=-1){
               isHere=true;
               reglasPos=i;
               
               if(reglas[i].equals("Constantes")||reglas[i].equals("Variables")||reglas[i].equals("Arreglos")||reglas[i].equals("Registros")||reglas[i].equals("Prototipos")||reglas[i].equals("Modulos")){
                  sint.infoStates.push(new Integer(0));
                  sint.padresState.push(new Integer(0));
                  sint.nivel++;
               }
                  
               if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
                  if(pos==3||pos==4)
                     sint.reuse=true;
            }
         }
         if(!isHere){
            sint.reuse=true;
            String sincStrings[]={"ARCHIVOS","CONSTANTES","VARIABLES","ARREGLOS","REGISTROS","PROTOTIPOS","MODULOS","PRINCIPAL"};
            return Panico("Programa",sincStrings);
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }else{
         sint.reuse=true;
         reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
         return reglaE[pos];
      }
   }
   
   public int recuperaArchivos(){
      ArrayList regla;
      int reglaE[],pos=-1;
      
      regla=(ArrayList)sint.Reglas.get("Archivos");
      reglaE=(int[])sint.ReglasE.get("Archivos");
      
      for(int i=0;i<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));i++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(i<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={"Archivos","Constantes","Variables","Arreglos","Registros","Prototipos","Modulos"};
         boolean isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
            }
            if(sint.lexema.equals("PRINCIPAL")){
               sint.reuse=true;
               return 0;//62;
            }
            
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(!reglas[i].equals("Archivos")){
                     sint.infoStates.push(new Integer(0));
                     sint.padresState.push(new Integer(0));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            if(sint.lexema.equals("PRINCIPAL")){
               sint.reuse=true;
               return 0;//62;
            }else{
               //Repite este procedimiento pero con el siguiente token
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
               
               isHere=false;
               reglasPos=0;
               for(int i=0;i<reglas.length&&!isHere;i++){
                  regla=(ArrayList)sint.Reglas.get(reglas[i]);
                  pos=regla.indexOf(sint.lexema);
                  if(pos==-1)
                     pos=regla.indexOf(sint.token);
                  
                  if(pos!=-1){
                     isHere=true;
                     reglasPos=i;
                     
                     if(!reglas[i].equals("Archivos")){
                        sint.infoStates.push(new Integer(0));
                        sint.padresState.push(new Integer(0));
                        sint.nivel++;
                     }
                  
                     if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
                        if(pos==3||pos==4)
                           sint.reuse=true;
                  }
               }
               if(!isHere){
                  if(sint.lexema.equals("PRINCIPAL")){
                     sint.reuse=true;
                     return 0;//62;
                  }else{
                     sint.reuse=true;
                     String sincStrings[]={",","]"};
                     return Panico("Archivos",sincStrings);
                  }
               }else{
                  sint.reuse=true;
                  reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
                  return reglaE[pos];
               }
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
            
      }
      
      //return 0;
   }
   
   public int recuperaConstantes(int padreID){
      ArrayList regla;
      int reglaE[],pos=-1;
      
      regla=(ArrayList)sint.Reglas.get("Constantes");
      reglaE=(int[])sint.ReglasE.get("Constantes");
      
      for(int i=0;i<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));i++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(i<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         sint.reuse=true;
         limpiaBuffers();
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         //String reglas[]={"Constantes","Archivos","Variables","Arreglos","Registros","Prototipos","Modulos"};
         String reglas[];
         if(padreID==0){
            reglas=new String[7];
            reglas[0]="Constantes";
            reglas[1]="Archivos";
            reglas[2]="Variables";
            reglas[3]="Arreglos";
            reglas[4]="Registros";
            reglas[5]="Prototipos";
            reglas[6]="Modulos";
         }else{
            reglas=new String[6];
            reglas[0]="Constantes";
            reglas[1]="Variables";
            reglas[2]="Arreglos";
            reglas[3]="Registros";
            reglas[4]="Prototipos";
            reglas[5]="Modulos";
         }
         boolean isHere=false;
         int reglasPos=0;
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               sint.reuse=true;
               return 0;//62;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(reglas[i].equals("Archivos")&&padreID==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }
                  if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
                     if(pos==3||pos==4)
                        sint.reuse=true;
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               sint.reuse=true;
               return 0;//62;
            }else{
               //Repite este procedimiento pero con el siguiente token
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
               
               isHere=false;
               reglasPos=0;
               for(int i=0;i<reglas.length&&!isHere;i++){
                  regla=(ArrayList)sint.Reglas.get(reglas[i]);
                  pos=regla.indexOf(sint.lexema);
                  if(pos==-1)
                     pos=regla.indexOf(sint.token);
                  
                  if(pos!=-1){
                     isHere=true;
                     reglasPos=i;
                     
                     if(reglas[i].equals("Archivos")&&padreID==0){
                        sint.infoStates.pop();
                        sint.padresState.pop();
                        sint.nivel--;
                     }
                     if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
                        if(pos==3||pos==4)
                           sint.reuse=true;
                  }
               }
               if(!isHere){
                  if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                     sint.reuse=true;
                     return 0;//62;
                  }else{
                     sint.reuse=true;
                     String sincStrings[]={",","]"};
                     return Panico("Constantes",sincStrings);
                  }
               }else{
                  sint.reuse=true;
                  reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
                  return reglaE[pos];
               }
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaVariables(int currentPos,int padreID){
      ArrayList regla;
      int reglaE[],pos=-1;
      boolean isHere=false;
      
      regla=(ArrayList)sint.Reglas.get("Variables");
      reglaE=(int[])sint.ReglasE.get("Variables");
      
      for(int i=0;i<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));i++){
         for(int c=currentPos;c<regla.size()&&!isHere;c++){
            String strTmp=(String)regla.get(c);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=c;
               isHere=true;
            }
         }
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(i<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         sint.reuse=true;
         limpiaBuffers();
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         //String reglas[]={"Variables","Archivos","Constantes","Arreglos","Registros","Prototipos","Modulos"};
         String reglas[];
         if(padreID==0){
            reglas=new String[7];
            reglas[0]="Variables";
            reglas[1]="Archivos";
            reglas[2]="Constantes";
            reglas[3]="Arreglos";
            reglas[4]="Registros";
            reglas[5]="Prototipos";
            reglas[6]="Modulos";
         }else{
            reglas=new String[6];
            reglas[0]="Variables";
            reglas[1]="Constantes";
            reglas[2]="Arreglos";
            reglas[3]="Registros";
            reglas[4]="Prototipos";
            reglas[5]="Modulos";
         }
         isHere=false;
         int reglasPos=0;
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               sint.reuse=true;
               return 0;//62;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(reglas[i].equals("Archivos")&&padreID==0/*||reglas[i].equals("Constantes")||reglas[i].equals("Prototipos")||reglas[i].equals("Modulos")*/){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }
               
                  if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
                     if(pos==3||pos==4)
                        sint.reuse=true;
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               sint.reuse=true;
               return 0;//62;
            }else{
               //Repite este procedimiento pero con el siguiente token
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
               
               isHere=false;
               reglasPos=0;
               for(int i=0;i<reglas.length&&!isHere;i++){
                  regla=(ArrayList)sint.Reglas.get(reglas[i]);
                  pos=regla.indexOf(sint.lexema);
                  if(pos==-1)
                     pos=regla.indexOf(sint.token);
                  
                  if(pos!=-1){
                     isHere=true;
                     reglasPos=i;
                     
                     if(reglas[i].equals("Archivos")&&padreID==0/*||reglas[i].equals("Constantes")||reglas[i].equals("Prototipos")||reglas[i].equals("Modulos")*/){
                        sint.infoStates.pop();
                        sint.padresState.pop();
                        sint.nivel--;
                     }
                  
                     if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
                        if(pos==3||pos==4)
                           sint.reuse=true;
                  }
               }
               if(!isHere){
                  if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                     sint.reuse=true;
                     return 0;//62;
                  }else{
                     sint.reuse=true;
                     String sincStrings[]={",","]"};
                     return Panico("Variables",sincStrings);
                  }
               }else{
                  sint.reuse=true;
                  reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
                  return reglaE[pos];
               }
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaArreglos(int currentPos,int padreID){
      ArrayList regla;
      int reglaE[],pos=-1,lim=44;
      boolean isHere=false;
      
      regla=(ArrayList)sint.Reglas.get("Arreglos");
      reglaE=(int[])sint.ReglasE.get("Arreglos");
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         if(currentPos<17)
            lim=17;
         
         for(int i=currentPos;i<lim&&!isHere;i++){
            String strTmp=(String)regla.get(i);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=i;
               isHere=true;
            }
         }
         if(lim==17&&pos==-1){
            for(int i=37;i<44&&!isHere;i++){
               String strTmp=(String)regla.get(i);
               if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
                  pos=i;
                  isHere=true;
               }
            }
         }
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         sint.reuse=true;
         limpiaBuffers();
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         //String reglas[]={"Arreglos","Archivos","Constantes","Variables","Registros","Prototipos","Modulos"};
         String reglas[];
         if(padreID==0){
            reglas=new String[7];
            reglas[0]="Arreglos";
            reglas[1]="Archivos";
            reglas[2]="Constantes";
            reglas[3]="Variables";
            reglas[4]="Registros";
            reglas[5]="Prototipos";
            reglas[6]="Modulos";
         }else{
            reglas=new String[6];
            reglas[0]="Arreglos";
            reglas[1]="Constantes";
            reglas[2]="Variables";
            reglas[3]="Registros";
            reglas[4]="Prototipos";
            reglas[5]="Modulos";
         }
         isHere=false;
         int reglasPos=0;
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               sint.reuse=true;
               return 0;//62;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(reglas[i].equals("Archivos")&&padreID==0/*||reglas[i].equals("Constantes")||reglas[i].equals("Prototipos")||reglas[i].equals("Modulos")*/){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }
               
                  if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
                     if(pos==3||pos==4)
                        sint.reuse=true;
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               sint.reuse=true;
               return 0;//62;
            }else{
               //Repite este procedimiento pero con el siguiente token
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
               
               isHere=false;
               reglasPos=0;
               for(int i=0;i<reglas.length&&!isHere;i++){
                  regla=(ArrayList)sint.Reglas.get(reglas[i]);
                  pos=regla.indexOf(sint.lexema);
                  if(pos==-1)
                     pos=regla.indexOf(sint.token);
                  
                  if(pos!=-1){
                     isHere=true;
                     reglasPos=i;
                     
                     if(reglas[i].equals("Archivos")&&padreID==0/*||reglas[i].equals("Constantes")||reglas[i].equals("Prototipos")||reglas[i].equals("Modulos")*/){
                        sint.infoStates.pop();
                        sint.padresState.pop();
                        sint.nivel--;
                     }
                  
                     if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
                        if(pos==3||pos==4)
                           sint.reuse=true;
                  }
               }
               if(!isHere){
                  if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                     sint.reuse=true;
                     return 0;//62;
                  }else{
                     sint.reuse=true;
                     String sincStrings[]={",",";","[","]"};
                     return Panico("Arreglos",sincStrings);
                  }
               }else{
                  sint.reuse=true;
                  reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
                  return reglaE[pos];
               }
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaRegistros(int currentPos,int padreID){
      ArrayList regla;
      int reglaE[],pos=-1;
      boolean isHere=false;
      
      regla=(ArrayList)sint.Reglas.get("Registros");
      reglaE=(int[])sint.ReglasE.get("Registros");
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         for(int i=currentPos;i<regla.size()&&!isHere;i++){
            String strTmp=(String)regla.get(i);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=i;
               isHere=true;
            }
         }
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         sint.reuse=true;
         limpiaBuffers();
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         //String reglas[]={"Registros","Archivos","Constantes","Variables","Arreglos","Prototipos","Modulos"};
         String reglas[];
         if(padreID==0){
            reglas=new String[7];
            reglas[0]="Registros";
            reglas[1]="Archivos";
            reglas[2]="Constantes";
            reglas[3]="Variables";
            reglas[4]="Arreglos";
            reglas[5]="Prototipos";
            reglas[6]="Modulos";
         }else{
            reglas=new String[6];
            reglas[0]="Registros";
            reglas[1]="Constantes";
            reglas[2]="Variables";
            reglas[3]="Arreglos";
            reglas[4]="Prototipos";
            reglas[5]="Modulos";
         }
         isHere=false;
         int reglasPos=0;
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               sint.reuse=true;
               return 0;//62;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(reglas[i].equals("Archivos")&&padreID==0/*||reglas[i].equals("Constantes")||reglas[i].equals("Prototipos")||reglas[i].equals("Modulos")*/){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }
               
                  if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
                     if(pos==3||pos==4)
                        sint.reuse=true;
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               sint.reuse=true;
               return 0;//62;
            }else{
               //Repite este procedimiento pero con el siguiente token
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
               
               isHere=false;
               reglasPos=0;
               for(int i=0;i<reglas.length&&!isHere;i++){
                  regla=(ArrayList)sint.Reglas.get(reglas[i]);
                  pos=regla.indexOf(sint.lexema);
                  if(pos==-1)
                     pos=regla.indexOf(sint.token);
                  
                  if(pos!=-1){
                     isHere=true;
                     reglasPos=i;
                     
                     if(reglas[i].equals("Archivos")&&padreID==0/*||reglas[i].equals("Constantes")||reglas[i].equals("Prototipos")||reglas[i].equals("Modulos")*/){
                        sint.infoStates.pop();
                        sint.padresState.pop();
                        sint.nivel--;
                     }
                  
                     if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
                        if(pos==3||pos==4)
                           sint.reuse=true;
                  }
               }
               if(!isHere){
                  if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                     sint.reuse=true;
                     return 0;//62;
                  }else{
                     sint.reuse=true;
                     String sincStrings[]={",",";","]"};
                     return Panico("Registros",sincStrings);
                  }
               }else{
                  sint.reuse=true;
                  reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
                  return reglaE[pos];
               }
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaPrototipos(int padreID){
      ArrayList regla;
      int reglaE[],pos=-1;
      
      regla=(ArrayList)sint.Reglas.get("Prototipos");
      reglaE=(int[])sint.ReglasE.get("Prototipos");
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         //if(pos==3||pos==4)
            sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={"Prototipos","Modulos","EncabezadoProc","EncabezadoFunc"};
         boolean isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               sint.reuse=true;
               return 0;//62;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(reglas[i].equals("EncabezadoProc")||reglas[i].equals("EncabezadoFunc")){
                     sint.infoStates.push(new Integer(55));
                     sint.padresState.push(new Integer(55));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               sint.reuse=true;
               return 0;//62;
            }else{
               //Repite este procedimiento pero con el siguiente token
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
               
               isHere=false;
               reglasPos=0;
               for(int i=0;i<reglas.length&&!isHere;i++){
                  regla=(ArrayList)sint.Reglas.get(reglas[i]);
                  pos=regla.indexOf(sint.lexema);
                  if(pos==-1)
                     pos=regla.indexOf(sint.token);
                  
                  if(pos!=-1){
                     isHere=true;
                     reglasPos=i;
                     
                     if(reglas[i].equals("EncabezadoProc")||reglas[i].equals("EncabezadoFunc")){
                        sint.infoStates.push(new Integer(55));
                        sint.padresState.push(new Integer(55));
                        sint.nivel++;
                     }
                  }
               }
               if(!isHere){
                  if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                     sint.reuse=true;
                     return 0;//62;
                  }else{
                     sint.reuse=true;
                     String sincStrings[]={";","]"};
                     return Panico("Prototipos",sincStrings);
                  }
               }else{
                  sint.reuse=true;
                  reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
                  return reglaE[pos];
               }
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaModulos(int padreID){
      ArrayList regla;
      int reglaE[],pos=-1;
      
      regla=(ArrayList)sint.Reglas.get("Modulos");
      reglaE=(int[])sint.ReglasE.get("Modulos");
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      
      if(pos!=-1){
         //if(pos==3||pos==4)
            sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={"Modulos","EncabezadoProc","EncabezadoFunc","Cuerpo"};
         boolean isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               sint.reuse=true;
               return 0;//62;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(reglas[i].equals("EncabezadoProc")||reglas[i].equals("EncabezadoFunc")||reglas[i].equals("Cuerpo")){
                     sint.padresState.push(new Integer(59));
                     sint.nivel++;
                     if(reglas[i].equals("Cuerpo")){
                        sint.infoStates.push(new Integer(60));
                        sint.reuse=true;
                     }else
                        sint.infoStates.push(new Integer(59));
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               sint.reuse=true;
               return 0;//62;
            }else{
               //Repite este procedimiento pero con el siguiente token
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
               
               isHere=false;
               reglasPos=0;
               for(int i=0;i<reglas.length&&!isHere;i++){
                  regla=(ArrayList)sint.Reglas.get(reglas[i]);
                  pos=regla.indexOf(sint.lexema);
                  if(pos==-1)
                     pos=regla.indexOf(sint.token);
                  
                  if(pos!=-1){
                     isHere=true;
                     reglasPos=i;
                     
                     //Checa a que etapa se va a brincar:
                     /*if(i==0){
                        if(sint.etapa<=0)
                           sint.etapa=1;
                     }else if(i==1){
                        if(sint.etapa<=1)
                           sint.etapa=2;
                     }else if(i==2){
                        if(sint.etapa<=2)
                           sint.etapa=3;
                     }else if(i==3){
                        if(sint.etapa<=3)
                           sint.etapa=4;
                     }else if(i==4){
                        if(sint.etapa<=4)
                           sint.etapa=5;
                     }*/
                     if(reglas[i].equals("EncabezadoProc")||reglas[i].equals("EncabezadoFunc")||reglas[i].equals("Cuerpo")){
                        sint.padresState.push(new Integer(59));
                        sint.nivel++;
                        if(reglas[i].equals("Cuerpo")){
                           sint.infoStates.push(new Integer(60));
                           sint.reuse=true;
                        }else
                           sint.infoStates.push(new Integer(59));
                     }
                  }
               }
               if(!isHere){
                  if(sint.lexema.equals("PRINCIPAL")&&padreID==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                     sint.reuse=true;
                     return 0;//62;
                  }else{
                     sint.reuse=true;
                     String sincStrings[]={";","]"};
                     return Panico("Modulos",sincStrings);
                  }
               }else{
                  /*if(reglasPos==0){
                     if(pos==3||pos==4)
                        sint.reuse=true;
                  }else
                     if(reglasPos==1){
                        if(pos>=3&&pos<8)
                           sint.reuse=true;
                     }else
                        if(reglasPos==2)
                           if(pos>=0&&pos<8)
                              sint.reuse=true;
                  */
                  sint.reuse=true;
                  reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
                  return reglaE[pos];
               }
            }
         }else{
            /*if((pos==3||pos==4)&&reglasPos!=0)
               sint.reuse=true;*/
            /*if(reglasPos==0){
               if(pos==3||pos==4)
                  sint.reuse=true;
            }else
               if(reglasPos==1){
                  if(pos>=3&&pos<8)
                     sint.reuse=true;
               }else
                  if(reglasPos==2)
                     if(pos>=0&&pos<8)
                        sint.reuse=true;
            */
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaEncabezadoP(int padreState){
      //System.out.print(" <<Entrando a recuperacion de EncabezadoP>> ");
      ArrayList regla;
      int reglaE[],pos=-1;
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      regla=(ArrayList)sint.Reglas.get("EncabezadoProc");
      reglaE=(int[])sint.ReglasE.get("EncabezadoProc");
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(""));c++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      
      if(pos!=-1){
         //if(pos==3||pos==4||pos==5||pos==6||pos==7)
            sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={padre,"Parametros","EncabezadoProc","Cuerpo"};
         boolean isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else if(i==1){
                     sint.infoStates.push(new Integer(66));
                     sint.padresState.push(new Integer(66));
                     sint.nivel++;
                  }else if(i==3){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                     sint.infoStates.push(new Integer(60));
                     sint.padresState.push(new Integer(59));
                     sint.reuse=true;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else if(i==1){
                     sint.infoStates.push(new Integer(66));
                     sint.padresState.push(new Integer(66));
                     sint.nivel++;
                  }else if(i==3){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                     sint.infoStates.push(new Integer(60));
                     sint.padresState.push(new Integer(59));
                     sint.reuse=true;
                  }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"{","}"};
               return PanicoMayor("EncabezadoProc",padre,sincStrings,padreStrings);
            }else{
               /*if(reglasPos==0){
                  if(pos==3||pos==4)
                     sint.reuse=true;
               }else{
                  if(reglasPos==2)
                     if(pos>=0&&pos<8)
                        sint.reuse=true;
               }*/
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            /*if(reglasPos==0){
               if(pos==3||pos==4)
                  sint.reuse=true;
            }else{
               if(reglasPos==2)
                  if(pos>=0&&pos<8)
                     sint.reuse=true;
            }*/
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaEncabezadoF(int padreState,int currentPos){
      ArrayList regla;
      int reglaE[],pos=-1;
      boolean isHere=false;
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      regla=(ArrayList)sint.Reglas.get("EncabezadoFunc");
      reglaE=(int[])sint.ReglasE.get("EncabezadoFunc");
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(""));c++){
         /*int pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);*/
         for(int i=currentPos;i<regla.size()&&!isHere;i++){
            String strTmp=(String)regla.get(i);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=i;
               isHere=true;
            }
         }
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         //if(pos>=0&&pos<8)
            sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={padre,"Parametros","EncabezadoFunc","Cuerpo"};
         isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else if(i==1){
                     sint.infoStates.push(new Integer(70));
                     sint.padresState.push(new Integer(70));
                     sint.nivel++;
                  }else if(i==3){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                     sint.infoStates.push(new Integer(60));
                     sint.padresState.push(new Integer(59));
                     sint.reuse=true;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else if(i==1){
                     sint.infoStates.push(new Integer(70));
                     sint.padresState.push(new Integer(70));
                     sint.nivel++;
                  }else if(i==3){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                     sint.infoStates.push(new Integer(60));
                     sint.padresState.push(new Integer(59));
                     sint.reuse=true;
                  }
               }
            }
            
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"{","}"};
               return PanicoMayor("EncabezadoFunc",padre,sincStrings,padreStrings);
            }else{
               /*if(reglasPos==0){
                  if(pos==3||pos==4)
                     sint.reuse=true;
               }else{
                  if(reglasPos==2)
                     if(pos>=3&&pos<8)
                        sint.reuse=true;
               }*/
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            /*if(reglasPos==0){
               if(pos==3||pos==4)
                  sint.reuse=true;
            }else{
               if(reglasPos==2)
                  if(pos>=3&&pos<8)
                     sint.reuse=true;
            }*/
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaParametros(int padreState,int currentPos){
      ArrayList regla;
      int reglaE[],pos=-1;
      boolean isHere=false;
      
      regla=(ArrayList)sint.Reglas.get("Parametros");
      reglaE=(int[])sint.ReglasE.get("Parametros");
      
      String padre=damePadre(padreState);
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         /*int pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);*/
         for(int i=currentPos;i<regla.size()&&!isHere;i++){
            String strTmp=(String)regla.get(i);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=i;
               isHere=true;
            }
         }
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={padre};
         isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  sint.infoStates.pop();
                  sint.padresState.pop();
                  sint.nivel--;
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  sint.infoStates.pop();
                  sint.padresState.pop();
                  sint.nivel--;
               }
            }
            if(!isHere){
               String sincStrings[]={"}","<Identificador>","ENTERO","REAL","CADENA","LOGICO"};
               //String padreStrings[]=damePadreStrs(padreState);
               //Investiga quien es el padre del padre:
               sint.infoStates.pop();
               sint.padresState.pop();
               sint.nivel--;
               int tmp=((Integer)sint.infoStates.peek()).intValue();
               padre=damePadre(tmp);
               String padreS[]=damePadreStrs(tmp);
               sint.reuse=true;
               return PanicoMayor("Parametros",padre,sincStrings,padreS);
            }else{
               /*if(padre.equals("EncabezadoFunc")){
                  if(pos>=0&&pos<8)
                     sint.reuse=true;
               }else{
                  if(padre.equals("EncabezadoProc"))
                     if(pos>=3&&pos<8)
                        sint.reuse=true;
               }*/
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            /*if(padre.equals("EncabezadoFunc")){
               if(pos>=0&&pos<8)
                  sint.reuse=true;
            }else{
               if(padre.equals("EncabezadoProc"))
                  if(pos>=3&&pos<8)
                     sint.reuse=true;
            }*/
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaBloque(int padreState){
      ArrayList regla;
      int reglaE[],pos=-1;
      
      regla=(ArrayList)sint.Reglas.get("Bloque");
      reglaE=(int[])sint.ReglasE.get("Bloque");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         sint.reuse=true;
         limpiaBuffers();
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={padre,"Instruccion","InstruccionIdent","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         boolean isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i==0){
                     /*if(padre.equals("Si")){
                        if(pos==1||pos==3)
                           sint.reuse=true;
                     }else if(padre.equals("Mientras")||padre.equals("Hacer")){
                        if(pos==1)
                           sint.reuse=true;
                     }else if(padre.equals("Desde")){
                        if((pos>2&&pos<11)||(pos>11&&pos<20)||pos==23||pos==25)
                           sint.reuse=true;
                     }else if(padre.equals("Dependiendo")){
                        if(pos==10||pos==14)
                           sint.reuse=true;
                     }*/
                     
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else if(i>=1){
                     sint.infoStates.push(new Integer(90));
                     sint.padresState.push(new Integer(92));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i==0){
                     if(padre.equals("Si")){
                        if(pos==1||pos==3)
                           sint.reuse=true;
                     }else if(padre.equals("Mientras")||padre.equals("Hacer")){
                        if(pos==1)
                           sint.reuse=true;
                     }else if(padre.equals("Desde")){
                        if((pos>2&&pos<11)||(pos>11&&pos<20)||pos==23||pos==25)
                           sint.reuse=true;
                     }else if(padre.equals("Dependiendo")){
                        if(pos==10||pos==14)
                           sint.reuse=true;
                     }
                     
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else if(i>=1){
                     sint.infoStates.push(new Integer(90));
                     sint.padresState.push(new Integer(92));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={";","]","FIN"};
               return Panico("Bloque",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaInstruccion(int padreState){
      ArrayList regla;
      int reglaE[],pos=-1;
      
      regla=(ArrayList)sint.Reglas.get("Instruccion");
      reglaE=(int[])sint.ReglasE.get("Instruccion");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         sint.reuse=true;
         limpiaBuffers();
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={padre,"Instruccion","InstruccionIdent","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT","Si","Mientras","Hacer","Desde","Dependiendo","Leer"};
         boolean isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else
                     if(i>6){
                        sint.infoStates.push(new Integer(101));
                        sint.padresState.push(new Integer(111));
                        sint.nivel++;
                        /*
                        if(reglas[i].equals("Mientras")||reglas[i].equals("Hacer")){
                           if(pos==1)
                              sint.reuse=true;
                        }else if(reglas[i].equals("Desde")){
                           if((pos>2&&pos<11)||(pos>11&&pos<20)||pos==23||pos==25)
                              sint.reuse=true;
                        }else if(reglas[i].equals("Dependiendo")){
                           if(pos==10||pos==14)
                              sint.reuse=true;
                        }else if(reglas[i].equals("Leer")){
                           if(pos==5)
                              sint.reuse=true;
                        }*/
                     }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else
                     if(i>6){
                        sint.infoStates.push(new Integer(101));
                        sint.padresState.push(new Integer(111));
                        sint.nivel++;
                        if(reglas[i].equals("Mientras")||reglas[i].equals("Hacer")){
                           if(pos==1)
                              sint.reuse=true;
                        }else if(reglas[i].equals("Desde")){
                           if((pos>2&&pos<11)||(pos>11&&pos<20)||pos==23||pos==25)
                              sint.reuse=true;
                        }else if(reglas[i].equals("Dependiendo")){
                           if(pos==10||pos==14)
                              sint.reuse=true;
                        }else if(reglas[i].equals("Leer")){
                           if(pos==5)
                              sint.reuse=true;
                        }
                     }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"SI","MIENTRAS","HACER","DESDE","DEPENDIENDO","LEER"};
               return Panico("Instruccion",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaInstruccionIdent(int padreState){
      ArrayList regla;
      int reglaE[],pos=-1;
      
      regla=(ArrayList)sint.Reglas.get("InstruccionIdent");
      reglaE=(int[])sint.ReglasE.get("InstruccionIdent");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         sint.reuse=true;
         limpiaBuffers();
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={padre,"Asignacion","AsignacionReg","AsignacionArr","Parametros_Ident","Instruccion","InstruccionIdent","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         boolean isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else
                     if(i<5){
                        if(i<4)
                           sint.infoStates.push(new Integer(101));
                        else
                           sint.infoStates.push(new Integer(96));
                        sint.padresState.push(new Integer(95));
                        sint.nivel++;
                        if(i==2||i==3){
                           if((pos>=0&&pos<=7)||(pos>=9&&pos<=16))
                              sint.reuse=true;
                        }
                     }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else
                     if(i<5){
                        if(i<4)
                           sint.infoStates.push(new Integer(101));
                        else
                           sint.infoStates.push(new Integer(96));
                        sint.padresState.push(new Integer(95));
                        sint.nivel++;
                        if(i==2||i==3){
                           if((pos>=0&&pos<=7)||(pos>=9&&pos<=16))
                              sint.reuse=true;
                        }
                     }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"(",")","<-","{","["};
               return Panico("InstruccionIdent",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaInstruccionReturn(int padreState){
      ArrayList regla;
      int reglaE[],pos=-1;
      
      regla=(ArrayList)sint.Reglas.get("InstruccionReturn");
      reglaE=(int[])sint.ReglasE.get("InstruccionReturn");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         sint.reuse=true;
         limpiaBuffers();
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={padre,"Regla_OR","Instruccion","InstruccionIdent","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         boolean isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else
                     if(i<2){
                        sint.infoStates.push(new Integer(109));
                        sint.padresState.push(new Integer(109));
                        sint.nivel++;
                        if(pos>=4&&pos<=11)
                           sint.reuse=true;
                     }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else
                     if(i<2){
                        sint.infoStates.push(new Integer(109));
                        sint.padresState.push(new Integer(109));
                        sint.nivel++;
                        if(pos>=4&&pos<=11)
                           sint.reuse=true;
                     }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"(",")"};
               return Panico("InstruccionReturn",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaInstruccionWrite(int padreState){
      ArrayList regla;
      int reglaE[],pos=-1;
      
      regla=(ArrayList)sint.Reglas.get("InstruccionWrite");
      reglaE=(int[])sint.ReglasE.get("InstruccionWrite");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      //for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         /*
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }*/
      if(pos!=-1){
         sint.reuse=true;
         limpiaBuffers();
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={padre,"Parametros_Ident","Instruccion","InstruccionIdent","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         boolean isHere=false;
         int reglasPos=0;
         /*
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }*/
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  /*
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  */
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else
                     if(i<2){
                        sint.infoStates.push(new Integer(114));
                        sint.padresState.push(new Integer(114));
                        sint.nivel++;
                     }
               }
            }/*
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }*/
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else
                     if(i<2){
                        sint.infoStates.push(new Integer(114));
                        sint.padresState.push(new Integer(114));
                        sint.nivel++;
                     }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"(",")"};
               return Panico("InstruccionWrite",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaInstruccionWriteLN(int padreState){
      ArrayList regla;
      int reglaE[],pos=-1;
      
      regla=(ArrayList)sint.Reglas.get("InstruccionWriteLN");
      reglaE=(int[])sint.ReglasE.get("InstruccionWriteLN");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      //for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         /*
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }*/
      if(pos!=-1){
         sint.reuse=true;
         limpiaBuffers();
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={padre,"Parametros_Ident","Instruccion","InstruccionIdent","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         boolean isHere=false;
         int reglasPos=0;
         /*
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }*/
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else
                     if(i<2){
                        sint.infoStates.push(new Integer(118));
                        sint.padresState.push(new Integer(118));
                        sint.nivel++;
                     }
               }
            }/*
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }*/
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else
                     if(i<2){
                        sint.infoStates.push(new Integer(118));
                        sint.padresState.push(new Integer(118));
                        sint.nivel++;
                     }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"(",")"};
               return Panico("InstruccionWriteLN",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaInstruccionPT(int padreState){
      ArrayList regla;
      int reglaE[],pos=-1;
      
      regla=(ArrayList)sint.Reglas.get("InstruccionPT");
      reglaE=(int[])sint.ReglasE.get("InstruccionPT");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      //System.out.print(" <<Padre de InstruccionPT: "+padre+">> ");
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         sint.reuse=true;
         limpiaBuffers();
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={padre,"Instruccion","InstruccionIdent","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         boolean isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i==0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"(",")"};
               return Panico("InstruccionPT",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaHacer(int padreState){
      ArrayList regla;
      int reglaE[],pos=-1;
      
      regla=(ArrayList)sint.Reglas.get("Hacer");
      reglaE=(int[])sint.ReglasE.get("Hacer");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         //if(pos==1)
            sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={"Bloque","Regla_OR",padre,"InstruccionIdent","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         boolean isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i>1){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     if(i==0)
                        sint.infoStates.push(new Integer(131));
                     else{
                        sint.infoStates.push(new Integer(133));
                        if(pos>3&&pos<12)
                           sint.reuse=true;
                     }
                     sint.padresState.push(new Integer(131));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i>1){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     if(i==0)
                        sint.infoStates.push(new Integer(131));
                     else{
                        sint.infoStates.push(new Integer(133));
                        if(pos>3&&pos<12)
                           sint.reuse=true;
                     }
                     sint.padresState.push(new Integer(131));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"HASTA"};
               return Panico("Hacer",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaDesde(int padreState,int currentPos){
      ArrayList regla;
      int reglaE[],pos=-1;
      boolean isHere=false;
      
      regla=(ArrayList)sint.Reglas.get("Desde");
      reglaE=(int[])sint.ReglasE.get("Desde");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         /*int pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);*/
         for(int i=currentPos;i<regla.size()&&!isHere;i++){
            String strTmp=(String)regla.get(i);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=i;
               isHere=true;
            }
         }
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         //if((pos>=3&&pos<=10)||(pos>=12&&pos<=19)||pos==23||pos==25)
            sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={"Expresion","Bloque",padre,"InstruccionIdent","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i>1){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     if(i==0){
                        if(currentPos<11)
                           sint.infoStates.push(new Integer(137));
                        else
                           sint.infoStates.push(new Integer(139));
                        
                        if(pos==8||pos==13)
                           sint.reuse=true;
                     }else{
                        sint.infoStates.push(new Integer(143));
                     }
                     sint.padresState.push(new Integer(137));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i>1){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     if(i==0){
                        if(currentPos<11)
                           sint.infoStates.push(new Integer(137));
                        else
                           sint.infoStates.push(new Integer(139));
                        
                        if(pos==8||pos==13)
                           sint.reuse=true;
                     }else{
                        sint.infoStates.push(new Integer(143));
                     }
                     sint.padresState.push(new Integer(137));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"}","INC","DEC","<Entero>"};
               return Panico("Desde",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaDependiendo(int padreState,int currentPos){
      ArrayList regla;
      int reglaE[],pos=-1;
      boolean isHere=false;
      
      regla=(ArrayList)sint.Reglas.get("Dependiendo");
      reglaE=(int[])sint.ReglasE.get("Dependiendo");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         for(int i=currentPos;i<regla.size()&&!isHere;i++){
            String strTmp=(String)regla.get(i);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=i;
               isHere=true;
            }
         }
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         //if(pos==10||pos==14)
            sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={"Bloque",padre,"InstruccionIdent","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i>0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     if(sint.estate<151)
                        sint.infoStates.push(new Integer(149));
                     else
                        sint.infoStates.push(new Integer(153));
                     
                     sint.padresState.push(new Integer(149));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i>0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     if(sint.estate<151)
                        sint.infoStates.push(new Integer(149));
                     else
                        sint.infoStates.push(new Integer(153));
                     
                     sint.padresState.push(new Integer(149));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"CASO","CUALQUIER","OTRO","]"};
               return Panico("Dependiendo",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaAsignacionReg(int padreState,int currentPos){
      ArrayList regla;
      int reglaE[],pos=-1;
      boolean isHere=false;
      
      regla=(ArrayList)sint.Reglas.get("AsignacionReg");
      reglaE=(int[])sint.ReglasE.get("AsignacionReg");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         for(int i=currentPos;i<regla.size()&&!isHere;i++){
            String strTmp=(String)regla.get(i);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=i;
               isHere=true;
            }
         }
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         //if((pos>=0&&pos<=7)||(pos>=9&&pos<=16))
            sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={"Expresion","Regla_OR",padre,"Instruccion","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i>1){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     if(i==0){
                        if(sint.estate<161)
                           sint.infoStates.push(new Integer(161));
                        else
                           sint.infoStates.push(new Integer(163));
                        
                        if(pos==8||pos==13)
                           sint.reuse=true;
                     }else{
                        sint.infoStates.push(new Integer(166));
                        if(pos>3&&pos<12)
                           sint.reuse=true;
                     }
                     sint.padresState.push(new Integer(166));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i>1){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     if(i==0){
                        if(sint.estate<161)
                           sint.infoStates.push(new Integer(161));
                        else
                           sint.infoStates.push(new Integer(163));
                        
                        if(pos==8||pos==13)
                           sint.reuse=true;
                     }else{
                        sint.infoStates.push(new Integer(166));
                        if(pos>3&&pos<12)
                           sint.reuse=true;
                     }
                     sint.padresState.push(new Integer(166));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"<-","}"};
               return Panico("AsignacionReg",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaAsignacionArr(int padreState,int currentPos){
      ArrayList regla;
      int reglaE[],pos=-1,lim=46;//38;
      boolean isHere=false;
      
      regla=(ArrayList)sint.Reglas.get("AsignacionArr");
      reglaE=(int[])sint.ReglasE.get("AsignacionArr");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         for(int i=currentPos;i<regla.size()&&!isHere;i++){
            String strTmp=(String)regla.get(i);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=i;
               isHere=true;
            }
         }
         if(currentPos<=28)//24
            lim=29;//25
         for(int i=currentPos;i<lim&&!isHere;i++){
            String strTmp=(String)regla.get(i);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=i;
               isHere=true;
            }
         }
         if(lim==29&&pos==-1){//lim==25
            for(int i=42;i<46&&!isHere;i++){//i=34;i<38
               String strTmp=(String)regla.get(i);
               if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
                  pos=i;
                  isHere=true;
               }
            }
         }
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         //if((pos>=0&&pos<=7)||(pos>=9&&pos<=16))
            sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={"Expresion","Regla_OR",padre,"Instruccion","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i>1){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     if(i==0){
                        if(sint.estate<168)
                           sint.infoStates.push(new Integer(168));
                        else
                           sint.infoStates.push(new Integer(170));
                        
                        if(pos==8||pos==13)
                           sint.reuse=true;
                     }else{
                        sint.infoStates.push(new Integer(173));
                        if(pos>3&&pos<12)
                           sint.reuse=true;
                     }
                     sint.padresState.push(new Integer(173));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i>1){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     if(i==0){
                        if(sint.estate<168)
                           sint.infoStates.push(new Integer(168));
                        else
                           sint.infoStates.push(new Integer(170));
                        
                        if(pos==8||pos==13)
                           sint.reuse=true;
                     }else{
                        sint.infoStates.push(new Integer(173));
                        if(pos>3&&pos<12)
                           sint.reuse=true;
                     }
                     sint.padresState.push(new Integer(173));
                     sint.nivel++;
                  }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={"<-","}","]"};
               return Panico("AsignacionArr",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaLeer(int padreState,int currentPos){
      ArrayList regla;
      int reglaE[],pos=-1;
      boolean isHere=false;
      
      regla=(ArrayList)sint.Reglas.get("Leer");
      reglaE=(int[])sint.ReglasE.get("Leer");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         for(int i=currentPos;i<regla.size()&&!isHere;i++){
            String strTmp=(String)regla.get(i);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=i;
               isHere=true;
            }
         }
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         //if(pos==5)
            sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={"Dimensiones_Ident",padre,"Instruccion","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i>0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     sint.infoStates.push(new Integer(189));
                     sint.padresState.push(new Integer(189));
                     sint.nivel++;
                     if((pos>=1&&pos<=8)||(pos>=10&&pos<=17))
                        sint.reuse=true;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i>0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     sint.infoStates.push(new Integer(189));
                     sint.padresState.push(new Integer(189));
                     sint.nivel++;
                     if((pos>=1&&pos<=8)||(pos>=10&&pos<=17))
                        sint.reuse=true;
                  }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={",",")"};
               return Panico("Leer",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaDimensiones_Ident(int padreState,int currentPos){
      ArrayList regla;
      int reglaE[],pos=-1;
      boolean isHere=false;
      
      regla=(ArrayList)sint.Reglas.get("Dimensiones_Ident");
      reglaE=(int[])sint.ReglasE.get("Dimensiones_Ident");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         for(int i=currentPos;i<regla.size()&&!isHere;i++){
            String strTmp=(String)regla.get(i);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=i;
               isHere=true;
            }
         }
         
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }
      if(pos!=-1){
         //if((pos>=1&&pos<=8)||(pos>=10&&pos<=17))
            sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         String reglas[]={"Expresion",padre,"Instruccion","InstruccionReturn","InstruccionWrite","InstruccionWriteLN","InstruccionPT"};
         isHere=false;
         int reglasPos=0;
         
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  
                  if(i>0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     if(sint.estate<193)
                        sint.infoStates.push(new Integer(193));
                     else
                        sint.infoStates.push(new Integer(195));
                     
                     sint.padresState.push(new Integer(193));
                     sint.nivel++;
                     if(pos==8||pos==13)
                        sint.reuse=true;
                  }
               }
            }
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  if(i>0){
                     sint.infoStates.pop();
                     sint.padresState.pop();
                     sint.nivel--;
                  }else{
                     if(sint.estate<193)
                        sint.infoStates.push(new Integer(193));
                     else
                        sint.infoStates.push(new Integer(195));
                     
                     sint.padresState.push(new Integer(193));
                     sint.nivel++;
                     if(pos==8||pos==13)
                        sint.reuse=true;
                  }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={",","}"};
               return Panico("Dimensiones_Ident",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaExpresion(int padreState,int currentPos){
      ArrayList regla;
      int reglaE[],pos=-1;
      boolean isHere=false;
      
      regla=(ArrayList)sint.Reglas.get("Expresion");
      reglaE=(int[])sint.ReglasE.get("Expresion");
      
      String padre=damePadre(padreState);
      String padreStrings[]=damePadreStrs(padreState);
      
      //for(int c=0;c<3&&pos==-1&&(!sint.lexema.equals("\0")&&!sint.lexema.equals(null));c++){
         for(int i=currentPos;i<regla.size()&&!isHere;i++){
            String strTmp=(String)regla.get(i);
            if(strTmp.equals(sint.lexema)||strTmp.equals(sint.token)){
               pos=i;
               isHere=true;
            }
         }
         /*
         if(pos==-1){
            tokenBuffer.add(sint.token);
            lexemaBuffer.add(sint.lexema);
            tipoBuffer.add(new Integer(sint.tipoElem));
            posBuffer.add(new Integer(sint.i));
            //System.out.println(" Agregando \""+sint.lexema+"\" al buffer");
            if(c<2){
               sint.token=sint.dameToken();
               sint.tipoElem=sint.tipoLexema();
            }
         }
      }*/
      if(pos!=-1){
         //if(pos==8||pos==13)
            sint.reuse=true;
         
         limpiaBuffers();
         
         return reglaE[pos];
      }else{
         //Aqui tiene que buscar en las otras reglas posibles en este nivel
         //Padres posibles: Dim_Ident, Regla_OR, Desde, AsignacionReg, AsignacionArr
         String reglas[]={padre};
         isHere=false;
         int reglasPos=0;
         /*
         for(int c=0;c<3&&pos==-1;c++){
            if(!tokenBuffer.isEmpty()){
               sint.token=(String)tokenBuffer.get(0);
               sint.lexema=(String)lexemaBuffer.get(0);
               sint.tipoElem=((Integer)tipoBuffer.get(0)).intValue();
               //sint.reuse=true;
            }*/
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  sint.reuse=true;
                  /*
                  String trozo=sint.fuente.substring(((Integer)posBuffer.get(0)).intValue(),sint.i);
                  //System.out.print(" -- Trozo: \""+trozo+"\" -- ");
                  cuentaLineas(trozo);
                  sint.i=((Integer)posBuffer.get(0)).intValue();
                  limpiaBuffers();
                  */
                  sint.infoStates.pop();
                  sint.padresState.pop();
                  sint.nivel--;
                  if(padre.equals("Dimensiones_Ident")){
                     if((pos>=1&&pos<=8)||(pos>=10&&pos<=17))
                        sint.reuse=true;
                  }else if(padre.equals("Regla_OR")){
                     if(pos>=4&&pos<=11)
                        sint.reuse=true;
                  }else if(padre.equals("Desde")){
                     if((pos>=3&&pos<=10)||(pos>=12&&pos<=19)||pos==23||pos==25)
                        sint.reuse=true;
                  }else if(padre.equals("AsignacionReg")||padre.equals("AsignacionArr")){
                     if((pos>=0&&pos<=7)||(pos>=9&&pos<=16))
                        sint.reuse=true;
                  }
               }
            }/*
            if(!isHere){
               tokenBuffer.remove(0);
               lexemaBuffer.remove(0);
               tipoBuffer.remove(0);
               posBuffer.remove(0);
            }
         }*/
         if(!isHere){
            //Repite este procedimiento pero con el siguiente token
            sint.token=sint.dameToken();
            sint.tipoElem=sint.tipoLexema();
            
            isHere=false;
            reglasPos=0;
            for(int i=0;i<reglas.length&&!isHere;i++){
               regla=(ArrayList)sint.Reglas.get(reglas[i]);
               pos=regla.indexOf(sint.lexema);
               if(pos==-1)
                  pos=regla.indexOf(sint.token);
               
               if(pos!=-1){
                  isHere=true;
                  reglasPos=i;
                  
                  sint.infoStates.pop();
                  sint.padresState.pop();
                  sint.nivel--;
                  if(padre.equals("Dimensiones_Ident")){
                     if((pos>=1&&pos<=8)||(pos>=10&&pos<=17))
                        sint.reuse=true;
                  }else if(padre.equals("Regla_OR")){
                     if(pos>=4&&pos<=11)
                        sint.reuse=true;
                  }else if(padre.equals("Desde")){
                     if((pos>=3&&pos<=10)||(pos>=12&&pos<=19)||pos==23||pos==25)
                        sint.reuse=true;
                  }else if(padre.equals("AsignacionReg")||padre.equals("AsignacionArr")){
                     if((pos>=0&&pos<=7)||(pos>=9&&pos<=16))
                        sint.reuse=true;
                  }
               }
            }
            if(!isHere){
               sint.reuse=true;
               String sincStrings[]={")","]","+","-","*","/","DIV","MOD","POT"};
               if(padre.equals("Desde")||padre.equals("AsignacionReg")||padre.equals("AsignacionArr"))
                  return PanicoMayor("Expresion",padre,sincStrings,padreStrings);
               else
                  return Panico("Expresion",sincStrings);
            }else{
               sint.reuse=true;
               reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
               return reglaE[pos];
            }
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }
      //return 0;
   }
   
   public int recuperaArchivosH(){
      ArrayList regla;
      int reglaE[],pos=0;
      
      //Aqui tiene que buscar en todas las reglas posibles en este nivel
      String reglas[]={"Constantes","Variables","Arreglos","Registros","Prototipos","Modulos"};
      boolean isHere=false;
      int reglasPos=0;
      for(int i=0;i<reglas.length&&!isHere;i++){
         regla=(ArrayList)sint.Reglas.get(reglas[i]);
         pos=regla.indexOf(sint.lexema);
         if(pos==-1)
            pos=regla.indexOf(sint.token);
         
         if(pos!=-1){
            isHere=true;
            reglasPos=i;
            
            sint.infoStates.push(new Integer(250));
            sint.padresState.push(new Integer(250));
            sint.nivel++;
            if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
               if(pos==3||pos==4)
                  sint.reuse=true;
         }
      }
      
      if(!isHere){
         //Repite el procedimiento pero con el siguiente token
         sint.token=sint.dameToken();
         sint.tipoElem=sint.tipoLexema();
         
         isHere=false;
         reglasPos=0;
         for(int i=0;i<reglas.length&&!isHere;i++){
            regla=(ArrayList)sint.Reglas.get(reglas[i]);
            pos=regla.indexOf(sint.lexema);
            if(pos==-1)
               pos=regla.indexOf(sint.token);
            
            if(pos!=-1){
               isHere=true;
               reglasPos=i;
               
               sint.infoStates.push(new Integer(250));
               sint.padresState.push(new Integer(250));
               sint.nivel++;
               if(reglas[i].equals("Prototipos")||reglas[i].equals("Modulos"))
                  if(pos==3||pos==4)
                     sint.reuse=true;
            }
         }
         if(!isHere){
            sint.reuse=true;
            String sincStrings[]={"CONSTANTES","VARIABLES","ARREGLOS","REGISTROS","PROTOTIPOS","MODULOS"};
            return Panico("ArchivosH",sincStrings);
         }else{
            sint.reuse=true;
            reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
            return reglaE[pos];
         }
      }else{
         sint.reuse=true;
         reglaE=(int[])sint.ReglasE.get(reglas[reglasPos]);
         return reglaE[pos];
      }
   }
   
   public void limpiaBuffers(){
      tokenBuffer.clear();
      lexemaBuffer.clear();
      tipoBuffer.clear();
      posBuffer.clear();
   }
   private void cuentaLineas(String s){
      int lineas=0;
      char letras[]=s.toCharArray();
      for(int c=0;c<letras.length;c++)
         if(c=='\n')
            lineas++;
      
      sint.lineCounter-=lineas;
   }
}