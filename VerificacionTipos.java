public class VerificacionTipos{
   Sintactico sint;
   String tipo1,tipo2;
   String op;
   
   public VerificacionTipos(Sintactico s){
      sint=s;
   }
   
   public void verExp(){
      op=((Operador)sint.pilaOp.pop()).op;
      
      if(!sint.pilaTipos.isEmpty())
         tipo2=(String)sint.pilaTipos.pop();
      else
         tipo2="<Ninguno>";
      
      if(!sint.pilaTipos.isEmpty())
         tipo1=(String)sint.pilaTipos.pop();
      else
         tipo1="<Ninguno>";
      
      if(tipo1.equals("<Entero>")&&tipo2.equals("<Entero>")){
         sint.pilaTipos.push("<Entero>");
      }else if(tipo1.equals("<Entero>")&&tipo2.equals("<Real>")){
         sint.pilaTipos.push("<Real>");
      }else if(tipo2.equals("<Entero>")&&tipo1.equals("<Real>")){
         sint.pilaTipos.push("<Real>");
      }else if(tipo1.equals("<Real>")&&tipo2.equals("<Real>")){
         sint.pilaTipos.push("<Real>");
      }else if(tipo1.equals("<Cadena>")&&tipo2.equals("<Cadena>")&&op.equals("+")){
         sint.pilaTipos.push("<Cadena>");
      }else{
         //Error
         sint.pilaTipos.push("<Ninguno>");
         if(!tipo1.equals("<Ninguno>")&&!tipo2.equals("<Ninguno>")){
            String err="No se pueden operar elementos de tipo "+tipo1+" con elementos de tipo: "+tipo2+": \""+op+"\"";
            sint.poneError2(3,err);
         }
      }
   }
   
   public void verTerm(){
      op=((Operador)sint.pilaOp.pop()).op;
      
      if(!sint.pilaTipos.isEmpty())
         tipo2=(String)sint.pilaTipos.pop();
      else
         tipo2="<Ninguno>";
      
      if(!sint.pilaTipos.isEmpty())
         tipo1=(String)sint.pilaTipos.pop();
      else
         tipo1="<Ninguno>";
      
      if(op.equals("*")){
         if(tipo1.equals("<Entero>")&&tipo2.equals("<Entero>")){
            sint.pilaTipos.push("<Entero>");
         }else if(tipo1.equals("<Entero>")&&tipo2.equals("<Real>")){
            sint.pilaTipos.push("<Real>");
         }else if(tipo2.equals("<Entero>")&&tipo1.equals("<Real>")){
            sint.pilaTipos.push("<Real>");
         }else if(tipo1.equals("<Real>")&&tipo2.equals("<Real>")){
            sint.pilaTipos.push("<Real>");
         }else{
            //Error
            sint.pilaTipos.push("<Ninguno>");
            if(!tipo1.equals("<Ninguno>")&&!tipo2.equals("<Ninguno>")){
               String err="No se pueden operar elementos de tipo "+tipo1+" con elementos de tipo: "+tipo2+": \""+op+"\"";
               sint.poneError2(3,err);
            }
         }
      }else if(op.equals("/")){
         if((tipo1.equals("<Entero>")||tipo1.equals("<Real>"))&&(tipo2.equals("<Entero>")||tipo2.equals("<Real>"))){
            sint.pilaTipos.push("<Real>");
         }else{
            //Error
            sint.pilaTipos.push("<Ninguno>");
            if(!tipo1.equals("<Ninguno>")&&!tipo2.equals("<Ninguno>")){
               String err="No se pueden operar elementos de tipo "+tipo1+" con elementos de tipo: "+tipo2+": \""+op+"\"";
               sint.poneError2(3,err);
            }
         }
      }else if(op.equals("DIV")){
         if((tipo1.equals("<Entero>")||tipo1.equals("<Real>"))&&(tipo2.equals("<Entero>")||tipo2.equals("<Real>"))){
            sint.pilaTipos.push("<Entero>");
         }else{
            //Error
            sint.pilaTipos.push("<Ninguno>");
            if(!tipo1.equals("<Ninguno>")&&!tipo2.equals("<Ninguno>")){
               String err="No se pueden operar elementos de tipo "+tipo1+" con elementos de tipo: "+tipo2+": \""+op+"\"";
               sint.poneError2(3,err);
            }
         }
      }else if(op.equals("MOD")){
         if(tipo1.equals("<Entero>")&&tipo2.equals("<Entero>")){
            sint.pilaTipos.push("<Entero>");
         }else{
            //Error
            sint.pilaTipos.push("<Ninguno>");
            if(!tipo1.equals("<Ninguno>")&&!tipo2.equals("<Ninguno>")){
               String err="No se pueden operar elementos de tipo "+tipo1+" con elementos de tipo: "+tipo2+": \""+op+"\"";
               sint.poneError2(3,err);
            }
         }
      }
   }
   
   public void verPot(){
      op=((Operador)sint.pilaOp.pop()).op;
      
      if(!sint.pilaTipos.isEmpty())
         tipo2=(String)sint.pilaTipos.pop();
      else
         tipo2="<Ninguno>";
      
      if(!sint.pilaTipos.isEmpty())
         tipo1=(String)sint.pilaTipos.pop();
      else
         tipo1="<Ninguno>";
      
      if(tipo1.equals("<Entero>")&&tipo2.equals("<Entero>")){
         sint.pilaTipos.push("<Entero>");
      }else if(tipo1.equals("<Entero>")&&tipo2.equals("<Real>")){
         sint.pilaTipos.push("<Real>");
      }else if(tipo2.equals("<Entero>")&&tipo1.equals("<Real>")){
         sint.pilaTipos.push("<Real>");
      }else if(tipo1.equals("<Real>")&&tipo2.equals("<Real>")){
         sint.pilaTipos.push("<Real>");
      }else{
         //Error
         sint.pilaTipos.push("<Ninguno>");
         if(!tipo1.equals("<Ninguno>")&&!tipo2.equals("<Ninguno>")){
            String err="No se pueden operar elementos de tipo "+tipo1+" con elementos de tipo: "+tipo2+": \""+op+"\"";
            sint.poneError2(3,err);
         }
      }
   }
   
   public void verFact(){
      op=((Operador)sint.pilaOp.pop()).op;
      
      if(!sint.pilaTipos.isEmpty())
         tipo1=(String)sint.pilaTipos.pop();
      else
         tipo1="<Ninguno>";
      
      if(tipo1.equals("<Entero>")||tipo1.equals("<Real>")){
         sint.pilaTipos.push(tipo1);
      }else{
         //Error
         sint.pilaTipos.push("<Ninguno>");
         if(!tipo1.equals("<Ninguno>")){
            String err="No se le puede aplicar signo a elementos de tipo "+tipo1+": \""+op+"\"";
            sint.poneError2(3,err);
         }
      }
   }
   
   public void verOR(){
      op=((Operador)sint.pilaOp.pop()).op;
      
      if(!sint.pilaTipos.isEmpty())
         tipo2=(String)sint.pilaTipos.pop();
      else
         tipo2="<Ninguno>";
      
      if(!sint.pilaTipos.isEmpty())
         tipo1=(String)sint.pilaTipos.pop();
      else
         tipo1="<Ninguno>";
      
      if(tipo1.equals("<Logico>")&&tipo2.equals("<Logico>")){
         sint.pilaTipos.push(tipo1);
      }else{
         //Error
         sint.pilaTipos.push("<Ninguno>");
         if(!tipo1.equals("<Ninguno>")&&!tipo2.equals("<Ninguno>")){
            String err="No se pueden operar logicamente elementos de tipo "+tipo1+" con elementos de tipo: "+tipo2+": \""+op+"\"";
            sint.poneError2(3,err);
         }
      }
   }
   public void verREL(){
      op=((Operador)sint.pilaOp.pop()).op;
      
      if(!sint.pilaTipos.isEmpty())
         tipo2=(String)sint.pilaTipos.pop();
      else
         tipo2="<Ninguno>";
      
      if(!sint.pilaTipos.isEmpty())
         tipo1=(String)sint.pilaTipos.pop();
      else
         tipo1="<Ninguno>";
      
      
      if((tipo1.equals("<Entero>")||tipo1.equals("<Real>"))&&(tipo2.equals("<Entero>")||tipo2.equals("<Real>"))){
         sint.pilaTipos.push("<Logico>");
      }else if(tipo1.equals("<Cadena>")&&tipo2.equals("<Cadena>")&&(op.equals("=")||op.equals("<>"))){
         sint.pilaTipos.push("<Logico>");
      }else if(tipo1.equals("<Logico>")&&tipo2.equals("<Logico>")&&(op.equals("=")||op.equals("<>"))){
         sint.pilaTipos.push("<Logico>");
      }else{
         //Error
         sint.pilaTipos.push("<Ninguno>");
         if(!tipo1.equals("<Ninguno>")&&!tipo2.equals("<Ninguno>")){
            String err="No se pueden operar logicamente elementos de tipo "+tipo1+" con elementos de tipo: "+tipo2+": \""+op+"\"";
            sint.poneError2(3,err);
         }
      }
   }
   public void verNOT(){
      op=((Operador)sint.pilaOp.pop()).op;
      
      if(!sint.pilaTipos.isEmpty())
         tipo1=(String)sint.pilaTipos.pop();
      else
         tipo1="<Ninguno>";
      
      if(tipo1.equals("<Logico>")){
         sint.pilaTipos.push(tipo1);
      }else{
         //Error
         sint.pilaTipos.push("<Ninguno>");
         if(!tipo1.equals("<Ninguno>")){
            String err="No se pueden negar elementos de tipo "+tipo1+".";
            sint.poneError2(3,err);
         }
      }
   }
   
   public void verAsign(){
      op=((Operador)sint.pilaOp.pop()).op;
      
      if(!sint.pilaTipos.isEmpty())
         tipo2=(String)sint.pilaTipos.pop();
      else
         tipo2="<Ninguno>";
      
      if(!sint.pilaTipos.isEmpty())
         tipo1=(String)sint.pilaTipos.pop();
      else
         tipo1="<Ninguno>";
      
      if(!(tipo1.equals("<Entero>")&&tipo2.equals("<Entero>")))
         if(!(tipo1.equals("<Real>")&&tipo2.equals("<Entero>")))
            if(!(tipo1.equals("<Real>")&&tipo2.equals("<Real>")))
               if(!(tipo1.equals("<Cadena>")&&tipo2.equals("<Cadena>")))
                  if(!(tipo1.equals("<Logico>")&&tipo2.equals("<Logico>")))
                     if(!tipo1.equals("<Ninguno>")&&!tipo2.equals("<Ninguno>")){
                        String err="No se puede asignar un valor de tipo "+tipo2+" a una variable de tipo: "+tipo1+".";
                        sint.poneError2(3,err);
                     }
   }
}