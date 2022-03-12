/*
 *Carlos Francisco Camacho Uribe
 *Taller de Compiladores
 *CUCEI, UdeG
*/

import javax.swing.plaf.metal.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.table.*;
import javax.swing.undo.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.Vector;
import java.util.Stack;
import java.util.ArrayList;
import java.beans.PropertyVetoException;


class Ventana{
   JInternalFrame win;
   JTextPane lineCounter;
   SubJTextPane areaTexto;
   JMenuItem menuWin;
   JMenuItem menuUndo,menuRedo;
   String nombre,ruta;
   boolean cambio,canUndo,canRedo;
   int lineas;
   public Vector rowStates;
   boolean sinErrores;
   
   EstadoCursor cursor;
   DocChanges docListener;
   EscuchadorTeclado teclado;
   UndoAction deshacer;
   RedoAction rehacer;
   UndoManager manejadorUndo;
   
   public Ventana(JInternalFrame w,JTextPane count,SubJTextPane areaT,String n,JMenuItem menu,String rute){
      win=w;
      lineCounter=count;
      areaTexto=areaT;
      menuWin=menu;
      nombre=n;
      ruta=rute;
      cambio=false;
      lineas=0;
      sinErrores=false;
      
      canUndo=canRedo=false;
      rowStates=new Vector(20,10);
   }
}
class pendingProcess{
   int id;
   Ventana winWork;
   
   public pendingProcess(int pr,Ventana vent){
      id=pr;
      winWork=vent;
   }
}
class Errores{
   protected int fila,pos;
   protected String lexema;
   protected Ventana win;
   protected String nombreWin,ruta;
   
   public Errores(Ventana v,int f,int p,String txt,String wn,String r){
      win=v;
      fila=f;
      pos=p;
      lexema=txt;
      nombreWin=wn;
      ruta=r;
   }
}
class Reciente{
	protected String ruta,nombre;
	protected JMenuItem menuWin;
	
	public Reciente(String r,JMenuItem menu){
		ruta=r;
		menuWin=menu;
		sacaNombre();
	}
	private void sacaNombre(){
		nombre=ruta.substring(ruta.lastIndexOf("\\")+1,ruta.length());
	}
}
class ManejadorIntWin implements InternalFrameListener{
   Editor editor;
   String winName;
   Ventana win;
   public ManejadorIntWin(Editor ed,String winN){
      editor=ed;
      winName=winN;
      win=editor.dameVentana(winName);
   }
   public void internalFrameActivated(InternalFrameEvent e){
      if(!win.cambio){
         editor.bSave.setEnabled(false);
         editor.save.setEnabled(false);
      }else{
         editor.bSave.setEnabled(true);
         editor.save.setEnabled(true);
      }
      if(win.areaTexto.getSelectedText()==null){
         editor.bCut.setEnabled(false);
         editor.bCopy.setEnabled(false);
         editor.cut.setEnabled(false);
         editor.copy.setEnabled(false);
      }else{
         editor.bCut.setEnabled(true);
         editor.bCopy.setEnabled(true);
         editor.cut.setEnabled(true);
         editor.copy.setEnabled(true);
      }
      if(win.areaTexto.getDocument().getLength()==0){
         editor.todo.setEnabled(false);
         editor.bSearch.setEnabled(false);
         editor.bReplace.setEnabled(false);
         editor.busc.setEnabled(false);
         editor.reemplazar.setEnabled(false);
         editor.irA.setEnabled(false);
         editor.compile.setEnabled(false);
         editor.bCompile.setEnabled(false);
      }else{
         editor.todo.setEnabled(true);
         editor.bSearch.setEnabled(true);
         editor.bReplace.setEnabled(true);
         editor.busc.setEnabled(true);
         editor.reemplazar.setEnabled(true);
         editor.irA.setEnabled(true);
         editor.compile.setEnabled(true);
         editor.bCompile.setEnabled(true);
      }
      if(win.sinErrores){
         editor.bExecute.setEnabled(true);
         editor.bDebug.setEnabled(true);
         editor.execute.setEnabled(true);
         editor.depurar.setEnabled(true);
      }else{
         editor.bExecute.setEnabled(false);
         editor.bDebug.setEnabled(false);
         editor.execute.setEnabled(false);
         editor.depurar.setEnabled(false);
      }
      
      editor.edicion.remove(3);
      editor.edicion.add(win.menuUndo,3);
      editor.edicion.remove(4);
      editor.edicion.add(win.menuRedo,4);
      
      editor.bUndo.setEnabled(win.canUndo);
      editor.bRedo.setEnabled(win.canRedo);
   }
   public void internalFrameDeactivated(InternalFrameEvent e){} 
   public void internalFrameDeiconified(InternalFrameEvent e){} 
   public void internalFrameIconified(InternalFrameEvent e){}
   public void internalFrameOpened(InternalFrameEvent e){}
   public void internalFrameClosing(InternalFrameEvent e){
      //editor.eliminaVentana(winName);
      Ventana ventmp=editor.dameVentana(winName);
      editor.procesosStack.push(new pendingProcess(1,ventmp));
      editor.administraProcesosStack();
   } 
   public void internalFrameClosed(InternalFrameEvent e){}
}

class Eventos implements ActionListener{
   private Editor editor;
   
   public Eventos (Editor e){
      editor=e;
   }
   
   public void actionPerformed(ActionEvent e){
      if(e.getSource().equals(editor.nuevo)||e.getSource().equals(editor.bNew)){//---- ** Nuevo **
         System.out.println("Nuevo");
         editor.creaVentana(true,null);
      }
      if(e.getSource().equals(editor.open)||e.getSource().equals(editor.bOpen)){//---- ** Abrir **
         System.out.println("Abrir");
         editor.paAbrir();
      }
      if(e.getSource().equals(editor.close)){//-------------------------------------- ** Cerrar **
         System.out.println("Cerrar");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            editor.procesosStack.push(new pendingProcess(1,ventmp));
            editor.administraProcesosStack();
            //editor.eliminaVentana(ventmp.nombre);
         }
      }
      if(e.getSource().equals(editor.save)||e.getSource().equals(editor.bSave)){//-- ** Guardar **
         System.out.println("Guardar");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            /*if(ventmp.ruta==null)
               editor.paGuardar(ventmp);
            else
               editor.trueGuardar(ventmp);*/
            editor.procesosStack.push(new pendingProcess(2,ventmp));
            editor.administraProcesosStack();
         }
      }
      if(e.getSource().equals(editor.saveAs)){//------------------------------- ** Guardar como **
         System.out.println("Guardar como");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            //editor.paGuardar(ventmp);
            editor.procesosStack.push(new pendingProcess(3,ventmp));
            editor.administraProcesosStack();
         }
      }
      if(e.getSource().equals(editor.saveAll)||e.getSource().equals(editor.bSaveAll)){//Guardar todo
         System.out.println("Guardar todo");
         //Agrega a la pila de procesos guardar cada ventana
         for(int i=0;i<editor.ventanas.size();i++){
            Ventana tmpWin=(Ventana)editor.ventanas.get(i);
            if(!tmpWin.nombre.startsWith("@")){
               if(tmpWin.ruta==null)
                  editor.procesosStack.push(new pendingProcess(3,tmpWin));
               else
                  editor.procesosStack.push(new pendingProcess(2,tmpWin));
            }else
               System.out.println("Se encontro una ventana fantasma");
         }
         editor.administraProcesosStack();
      }
      if(e.getSource().equals(editor.salir)){//--------------------------------------- ** Salir **
         editor.procesosStack.push(new pendingProcess(0,null));
         editor.cerrarEditor();
      }
      
      
      if(e.getSource().equals(editor.cut)||e.getSource().equals(editor.bCut)){//----- ** Cortar **
         System.out.println("Cortar");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            ventmp.areaTexto.cut();
            ventmp.areaTexto.requestFocus();
            editor.bPaste.setEnabled(true);
            editor.paste.setEnabled(true);
         }
      }
      if(e.getSource().equals(editor.copy)||e.getSource().equals(editor.bCopy)){//--- ** Copiar **
         System.out.println("Copiar");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            ventmp.areaTexto.copy();
            ventmp.areaTexto.requestFocus();
            editor.bPaste.setEnabled(true);
            editor.paste.setEnabled(true);
         }
      }
      if(e.getSource().equals(editor.paste)||e.getSource().equals(editor.bPaste)){//-- ** Pegar **
         System.out.println("Pegar");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            ventmp.areaTexto.paste();
            ventmp.areaTexto.requestFocus();
         }
      }
      if(e.getSource().equals(editor.bUndo)){//------------------------------------ ** Deshacer **
         System.out.println("Deshacer");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            try{
               ventmp.manejadorUndo.undo();
            }catch(CannotUndoException ex){
               System.out.println("Imposible deshacer: " + ex);
               //ex.printStackTrace();
            }
            ventmp.deshacer.updateUndoState();
            ventmp.rehacer.updateRedoState();
            
            ventmp.areaTexto.requestFocus();
            
            int posTmp=ventmp.areaTexto.getCaretPosition();
            LexicoAbrir coloreador=new LexicoAbrir(ventmp,editor);
            coloreador.analiza();
            ventmp.areaTexto.setCaretPosition(posTmp);
         }
      }
      if(e.getSource().equals(editor.bRedo)){//------------------------------------- ** Rehacer **
         System.out.println("Rehacer");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            try {
               ventmp.manejadorUndo.redo();
            } catch (CannotRedoException ex) {
               System.out.println("Imposible rehacer: " + ex);
               //ex.printStackTrace();
            }
            ventmp.rehacer.updateRedoState();
            ventmp.deshacer.updateUndoState();
            
            ventmp.areaTexto.requestFocus();
            
            int posTmp=ventmp.areaTexto.getCaretPosition();
            LexicoAbrir coloreador=new LexicoAbrir(ventmp,editor);
            coloreador.analiza();
            ventmp.areaTexto.setCaretPosition(posTmp);
         }
      }
      if(e.getSource().equals(editor.todo)){//----------------------------------- ** Select all **
         System.out.println("Seleccionar todo");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            ventmp.areaTexto.requestFocus();
            ventmp.areaTexto.selectAll();
         }
      }
      
      
      if(e.getSource().equals(editor.busc)||e.getSource().equals(editor.bSearch)){//- ** Buscar **
         System.out.println("Buscar");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            new FindWindow(editor,ventmp);
         }
      }
      if(e.getSource().equals(editor.reemplazar)||e.getSource().equals(editor.bReplace)){//Reemplazar **
         System.out.println("Reemplazar");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            new ReplaceDialog(editor,ventmp);
         }
      }
      if(e.getSource().equals(editor.irA)){//------------------------------------------ ** Ir a **
         System.out.println("Ir a");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            new GoToDialog(editor,ventmp);
         }
      }
      
      
      if(e.getSource().equals(editor.compile)||e.getSource().equals(editor.bCompile)){//*Compilar*
         System.out.println("Compilar");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            if(ventmp.ruta==null||ventmp.cambio){
               new CompileDialog(editor,ventmp);
            }else
               editor.hazCompilar(ventmp);
         }
      }
      if(e.getSource().equals(editor.execute)||e.getSource().equals(editor.bExecute)){//*Ejecutar*
         System.out.println("Ejecutar");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            RunTime run=new RunTime();
            run.runShell(ventmp.ruta,null);
         }
      }
      if(e.getSource().equals(editor.depurar)||e.getSource().equals(editor.bDebug)){// **Depurar**
         System.out.println("Depurar");
         JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
         if(frameTmp==null)
            System.out.println("Error, no se pudo obtener la ventana activa");
         else{
            Ventana ventmp=editor.dameVentana(frameTmp.getTitle());
            RunTime run=new RunTime();
            run.runShell(ventmp.ruta,"-d");
         }
      }
      
      
      if(e.getSource().equals(editor.autor)||e.getSource().equals(editor.bAbout)){//-- ** Autor **
         System.out.println("Autor");
         new AboutDialog(editor);
      }
      if(e.getSource().equals(editor.personalize)){//-------------------------- ** Personalizar **
         System.out.println("Personalizar");
         new Personalizador2(editor);
      }
      
      //Lo siguiente es para el menu ventanas, que selecciona alguna de las ventanas abiertas:
      for(int n=0;n<editor.ventanas.size();n++){
         Ventana tmpWin=(Ventana)editor.ventanas.get(n);
         if(!tmpWin.nombre.startsWith("@")){
            if(e.getSource().equals(tmpWin.menuWin)){
               //System.out.println("Ventana '"+tmpWin.nombre+"' seleccionada");
               try{
                  tmpWin.win.setSelected(true);
               }catch(PropertyVetoException pve){}
            }
         }
      }
      //Lo siguiente es para la seccion de archivos recientes del menu archivo:
      for(int n=0;n<editor.recientes.size();n++){
         Reciente recien=(Reciente)editor.recientes.get(n);
         if(e.getSource().equals(recien.menuWin)){
            System.out.println("Archivo '"+recien.nombre+"' seleccionado");
            editor.abreSimple(recien);
         }
      }
   }
}//-------Fin de la clase Eventos-------

class EscuchadorTeclado implements KeyListener{
   Editor editor;
   Ventana win;
   int winLineas=0;
   int inicioLinea=0;
   int lineaActual=0;
   
   public EscuchadorTeclado(Editor e,Ventana v){
      editor=e;
      win=v;
   }
   public void keyPressed(KeyEvent e){
      int pos=win.areaTexto.getCaretPosition();
      int tam=win.areaTexto.getDocument().getLength();
      String temp=win.areaTexto.getText();
      
      if(temp.equals("")||pos==tam){
         editor.fuente(win,editor.atributos[0],editor.atributos[1],editor.atributos[4],editor.atributos[3],editor.atributos[2]);
      }
   }
   public void keyReleased(KeyEvent e){
      //Referente a la identacion
      if(e.getKeyCode()==KeyEvent.VK_ENTER){
         int pos=win.areaTexto.getCaretPosition();
         String linea=editor.obtenLinea(win,pos-1);
         String espacios="";
         char ct;
         if(pos>0){
            //System.out.println("Linea anterior: "+linea);
            //Revisa cuantos espacios en blanco habia en la linea anterior y los inserta 
            //en la linea actual mas los espacios en blanco definidos por e.ident
            boolean fin=false;
            for(int i=0;i<linea.length()&&!fin;i++){
               ct=linea.charAt(i);
               if(!Character.isWhitespace(ct))
                  fin=true;
               else
                  espacios=espacios+ct;
            }
            if(linea.indexOf('[')!=-1&&linea.indexOf(']')==-1){
               String espacios2=espacios.toString();
               espacios=espacios+editor.ident;
               win.areaTexto.replaceSelection(espacios);
               if(linea.indexOf("ARCHIVOS")!=-1){
                  win.areaTexto.replaceSelection("\".h\"\n"+espacios2+"]");
                  int putCaret=win.areaTexto.getCaretPosition();
                  putCaret=putCaret-5-espacios2.length();
                  win.areaTexto.setCaretPosition(putCaret);
               }else if(linea.indexOf("CONSTANTES")!=-1){
                  win.areaTexto.replaceSelection("<-\n"+espacios2+"]");
                  int putCaret=win.areaTexto.getCaretPosition();
                  putCaret=putCaret-4-espacios2.length();
                  win.areaTexto.setCaretPosition(putCaret);
               }else if(linea.indexOf("VARIABLES")!=-1||linea.indexOf("ARREGLOS")!=-1||linea.indexOf("REGISTROS")!=-1||linea.indexOf("PROTOTIPOS")!=-1||linea.indexOf("MODULOS")!=-1){
                  win.areaTexto.replaceSelection("\n"+espacios2+"]");
                  int putCaret=win.areaTexto.getCaretPosition();
                  putCaret=putCaret-2-espacios2.length();
                  win.areaTexto.setCaretPosition(putCaret);
               }else if(linea.indexOf("INICIO")!=-1){
                  win.areaTexto.replaceSelection(";\n"+espacios2+"]FIN");
                  int putCaret=win.areaTexto.getCaretPosition();
                  putCaret=putCaret-6-espacios2.length();
                  win.areaTexto.setCaretPosition(putCaret);
               }
            }else
               win.areaTexto.replaceSelection(espacios);
         }
      }else if(e.getKeyChar()=='-'){
         int pos=win.areaTexto.getCaretPosition();
         String temp;
         try{
            temp=win.areaTexto.getDocument().getText(0,win.areaTexto.getDocument().getLength());
         }catch(BadLocationException ble){
            temp="";
            System.out.println("Error al capturar el texto para analizarlo");
         }
         
         if(temp.length()>1){
            char car=temp.charAt(pos-2);
            char car2=temp.charAt(pos-3);
            //System.out.println("Caracter anterior: '"+car+"'");
            //System.out.println("Caracter anterior: '"+car2+"'");
            if(car=='!'&&car2=='<'){
               win.areaTexto.replaceSelection("  ->");
               int putCaret=win.areaTexto.getCaretPosition();
               win.areaTexto.setCaretPosition(putCaret-3);
            }
         }
      }
      //Fin de identacion
      
      int c=win.areaTexto.getDocument().getDefaultRootElement().getElementCount()-1;
      if(!e.isActionKey()){
         if(c!=winLineas){
            winLineas=c;
            int posTmp=win.areaTexto.getCaretPosition();
            LexicoAbrir coloreador=new LexicoAbrir(win,editor);
            coloreador.analiza();
            win.areaTexto.setCaretPosition(posTmp);
            
            /*if(win.rowStates.contains(new Integer(c-1))){
               win.areaTexto.setCharacterAttributes(editor.colorComent,false);
            }*/
         }else{
            int posCur=win.areaTexto.getCaretPosition()+1;
            String tmp=editor.obtenLinea(win,posCur);
            String prevString;
            boolean c1;
            do{
               //System.out.println("\nLinea: "+tmp);
               //System.out.println("Inicio linea: "+editor.teclado.inicioLinea); 
               tmp+=' ';
               editor.lexicolor.colorea(win,tmp,inicioLinea);
               int lengTemp=tmp.length();
               int prevLine=lineaActual;
               prevString=tmp.toString();
               tmp=editor.obtenLinea(win,inicioLinea+lengTemp+2);
               //System.out.println("Linea siguiente: '"+tmp+"'");
               //System.out.println("Linea siguiente: "+lineaActual);
               c1=condicion1(tmp,prevString);
               
               if(((editor.lexicolor.estado==14||editor.lexicolor.estado==15)||(prevString==null&&win.rowStates.contains(new Integer(prevLine))))&&lineaActual!=prevLine){
                  if(!win.rowStates.contains(new Integer(lineaActual)))
                     win.rowStates.add(new Integer(lineaActual));
               }
               
               /*
               System.out.print("Filas que inician en estado 14: ");
               for(int n=0;n<win.rowStates.size();n++){
                  int row=((Integer)win.rowStates.get(n)).intValue();
                  System.out.print(row+" ");
               }
               System.out.println(" ");*/
               //if(prevString==null&&win.rowStates.contains(new Integer(lineaActual-1)))
            }while(c1||condicion2(tmp,prevString));
         }
      }
   }
   private boolean condicion1(String cad,String prev){
      if(((editor.lexicolor.estado==14||editor.lexicolor.estado==15)||(prev==null&&win.rowStates.contains(new Integer(lineaActual-1))))&&cad!=null&&!win.rowStates.contains(new Integer(lineaActual)))
         return true;
      else
         return false;
   }
   private boolean condicion2(String temp,String prev){
      if((editor.lexicolor.estado!=14&&editor.lexicolor.estado!=15&&(prev!=null||!win.rowStates.contains(new Integer(lineaActual-1))))&&temp!=null&&win.rowStates.contains(new Integer(lineaActual))){
         win.rowStates.remove(new Integer(lineaActual));
         return true;
      }else
         return false;
   } 
   public void keyTyped(KeyEvent e){
      //System.out.println("La tecla presionada es: "+e.getKeyChar());
      //System.out.println("Su codigo es: "+e.getKeyCode());
      //System.out.println("Tecla: "+KeyEvent.getKeyText(e.getKeyCode()));
      
      if(e.getKeyChar()==']'){
         int pos=win.areaTexto.getCaretPosition();
         String linea=editor.obtenLinea(win,pos);
         //System.out.println("Linea: \""+linea+"\"");
         if(linea.indexOf('[')==-1/*&&linea.indexOf(']')!=-1*/){
            String espacios="";
            char ct;
            boolean fin=false;
            for(int i=0;i<linea.length()&&!fin;i++){
               ct=linea.charAt(i);
               if(!Character.isWhitespace(ct))
                  fin=true;
               else
                  espacios=espacios+ct;
            }
            if(espacios.length()>editor.ident.length()){
               //espacios=espacios.substring(editor.ident.length());
               win.areaTexto.setCaretPosition(inicioLinea);
               win.areaTexto.moveCaretPosition(inicioLinea+editor.ident.length());
               win.areaTexto.replaceSelection("");
               win.areaTexto.setCaretPosition(pos-editor.ident.length());
            }else{
               win.areaTexto.setCaretPosition(inicioLinea);
               win.areaTexto.moveCaretPosition(inicioLinea+espacios.length());
               win.areaTexto.replaceSelection("");
               win.areaTexto.setCaretPosition(pos-espacios.length());
            }
            
         }
      }else if(e.getKeyChar()=='"'){
         win.areaTexto.replaceSelection("\"");
         int putCaret=win.areaTexto.getCaretPosition();
         win.areaTexto.setCaretPosition(putCaret-1);
      }else if(e.getKeyChar()=='('){
         win.areaTexto.replaceSelection(")");
         int putCaret=win.areaTexto.getCaretPosition();
         win.areaTexto.setCaretPosition(putCaret-1);
      }else if(e.getKeyChar()=='{'){
         win.areaTexto.replaceSelection("}");
         int putCaret=win.areaTexto.getCaretPosition();
         win.areaTexto.setCaretPosition(putCaret-1);
      }else if(e.getKeyChar()=='['){
         int pos=win.areaTexto.getCaretPosition();
         String linea=editor.obtenLinea(win,pos);
         if(linea.indexOf("ARCHIVOS")==-1&&linea.indexOf("CONSTANTES")==-1&&linea.indexOf("VARIABLES")==-1&&linea.indexOf("ARREGLOS")==-1&&linea.indexOf("REGISTROS")==-1&&linea.indexOf("PROTOTIPOS")==-1&&linea.indexOf("MODULOS")==-1&&linea.indexOf("INICIO")==-1){
            win.areaTexto.replaceSelection("]");
            win.areaTexto.setCaretPosition(pos);
         }
      }
   }
}

class EstadoCursor implements CaretListener{
   Editor ed;
   Ventana win;
   
   public EstadoCursor(Editor e,Ventana ventana){
      ed=e;
      win=ventana;
   }
   public void caretUpdate(CaretEvent e){
      int c=win.areaTexto.getDocument().getDefaultRootElement().getElementCount();
      //Para actualizar el contador de lineas
      if(c!=win.lineas){
         win.lineCounter.setEditable(true);
         win.lineCounter.setText("");
         win.lineCounter.replaceSelection("0");
         for(int j=1;j<c; j++){
            win.lineCounter.replaceSelection("\n"+j);
         }
         win.lineas=c;
         ed.actualizaLineCounter(win);
         win.lineCounter.setEditable(false);
      }
      //Para actualizar los botones de cortar y copiar
      if(win.areaTexto.getSelectedText()==null){
         ed.bCut.setEnabled(false);
         ed.bCopy.setEnabled(false);
         ed.cut.setEnabled(false);
         ed.copy.setEnabled(false);
      }else{
         ed.bCut.setEnabled(true);
         ed.bCopy.setEnabled(true);
         ed.cut.setEnabled(true);
         ed.copy.setEnabled(true);
      }
      
   }
}
class DocChanges implements DocumentListener{
   Editor editor;
   Ventana win;
   
   public DocChanges(Editor ed,Ventana vent){
      editor=ed;
      win=vent;
   }
   
   public void insertUpdate(DocumentEvent e){
      editor.todo.setEnabled(true);
      editor.bSearch.setEnabled(true);
      editor.bReplace.setEnabled(true);
      editor.busc.setEnabled(true);
      editor.reemplazar.setEnabled(true);
      editor.irA.setEnabled(true);
      editor.compile.setEnabled(true);
      editor.bCompile.setEnabled(true);
      
      if(!win.cambio){
         win.cambio=true;
         editor.bSave.setEnabled(true);
         editor.save.setEnabled(true);
      }
   }
   public void removeUpdate(DocumentEvent e){
      //editor.lastUpSearch=-1;
      //editor.lastDownSearch=-1;
      
      if(win.areaTexto.getDocument().getLength()==0){
         editor.todo.setEnabled(false);
         editor.bSearch.setEnabled(false);
         editor.bReplace.setEnabled(false);
         editor.busc.setEnabled(false);
         editor.reemplazar.setEnabled(false);
         editor.irA.setEnabled(false);
         editor.compile.setEnabled(false);
         editor.bCompile.setEnabled(false);
      }
      if(!win.cambio){
         if(win.areaTexto.getDocument().getLength()!=0){
            win.cambio=true;
            editor.bSave.setEnabled(true);
            editor.save.setEnabled(true);
         }
      }else{
         if(win.ruta==null&&win.areaTexto.getDocument().getLength()==0){
            win.cambio=false;
            editor.bSave.setEnabled(false);
            editor.save.setEnabled(false);
         }
      }
   }
   public void changedUpdate(DocumentEvent e){
      if(win.sinErrores){
         win.sinErrores=false;
         editor.bExecute.setEnabled(false);
         editor.bDebug.setEnabled(false);
         editor.execute.setEnabled(false);
         editor.depurar.setEnabled(false);
      }
   }
}

class MyUndoableEditListener implements UndoableEditListener{
   Ventana win;
   
   public MyUndoableEditListener(Ventana ven){
      win=ven;
   }
   public void undoableEditHappened(UndoableEditEvent e){
      //Remember the edit and update the menus.
      //System.out.println(e.getEdit().getPresentationName());
      if(!e.getEdit().getPresentationName().equals("cambio de estilo")){
         win.manejadorUndo.addEdit(e.getEdit());
         win.deshacer.updateUndoState();
         win.rehacer.updateRedoState();
      }
   }
}
class UndoAction extends AbstractAction{
   Editor editor;
   Ventana win;
   
   public UndoAction(Editor ed,Ventana v){
      super("Deshacer");
      setEnabled(false);
      v.canUndo=false;
      editor=ed;
      win=v;
   }
   
   public void actionPerformed(ActionEvent e) {
      try{
         win.manejadorUndo.undo();
      }catch(CannotUndoException ex){
         System.out.println("Imposible deshacer: " + ex);
         //ex.printStackTrace();
      }
      updateUndoState();
      win.rehacer.updateRedoState();
      
      int posTmp=win.areaTexto.getCaretPosition();
      LexicoAbrir coloreador=new LexicoAbrir(win,editor);
      coloreador.analiza();
      win.areaTexto.setCaretPosition(posTmp);
   }
   
   protected void updateUndoState(){
      if(win.manejadorUndo.canUndo()&&deniedAccions()){
         setEnabled(true);
         win.canUndo=true;
         editor.bUndo.setEnabled(true);
         //putValue(Action.NAME, editor.manejadorUndo.getUndoPresentationName());
      }else{
         setEnabled(false);
         win.canUndo=false;
         editor.bUndo.setEnabled(false);
         
         win.cambio=false;
         editor.bSave.setEnabled(false);
         editor.save.setEnabled(false);
         //putValue(Action.NAME, "Deshacer");
      }
   }
   private boolean deniedAccions(){
      boolean acerto=true;
      if(win.manejadorUndo.getUndoPresentationName().equals("Deshacer cambio de estilo"))
         acerto=false;
      
      return acerto;
   }
}
   
class RedoAction extends AbstractAction{
   Editor editor;
   Ventana win;
   
   public RedoAction(Editor e,Ventana v) {
      super("Rehacer");
      setEnabled(false);
      v.canRedo=false;
      editor=e;
      win=v;
   }
   
   public void actionPerformed(ActionEvent e) {
      try {
         win.manejadorUndo.redo();
      } catch (CannotRedoException ex) {
         System.out.println("Imposible rehacer: " + ex);
         //ex.printStackTrace();
      }
      updateRedoState();
      win.deshacer.updateUndoState();
      
      int posTmp=win.areaTexto.getCaretPosition();
      LexicoAbrir coloreador=new LexicoAbrir(win,editor);
      coloreador.analiza();
      win.areaTexto.setCaretPosition(posTmp);
   }
   
   protected void updateRedoState() {
      if(win.manejadorUndo.canRedo()){
         setEnabled(true);
         win.canRedo=true;
         editor.bRedo.setEnabled(true);
         //putValue(Action.NAME, editor.manejadorUndo.getRedoPresentationName());
      }else{
         setEnabled(false);
         win.canRedo=false;
         editor.bRedo.setEnabled(false);
         //putValue(Action.NAME, "Rehacer");
      }
   }
}
class SubJTextPane extends JTextPane{
   public SubJTextPane(){
      super();
   }
   public SubJTextPane(StyledDocument doc){
      super(doc);
   }
   
   public void setBounds(int x, int y,int width, int height){
      Dimension size = this.getPreferredSize();
      super.setBounds(x,y,Math.max(size.width, width),height);
   }
}
class MyDefaultTableModel extends DefaultTableModel{
   //private String[] columnNames = {"First Name","Last Name",};
   public MyDefaultTableModel(Object[] columnas,int filas){
      super(columnas,filas);
   }
   public boolean isCellEditable(int row, int col){
      return false;
   }
}

//*************************************************************************************************
//***************************************  CLASE PRINCIPAL  ***************************************
//*************************************************************************************************

public class Editor extends JFrame{
   private JToolBar barraHerramientas;
   private JMenuBar barraMenu;
   public JMenu archivo,edicion,buscar,compilar,windows,ayuda;
   protected JMenuItem nuevo,open,close,save,saveAs,saveAll,salir;
   protected JMenuItem cut,copy,paste,undo,redo,todo;
   protected JMenuItem busc,reemplazar,irA;
   protected JMenuItem compile,execute,depurar;
   protected JMenuItem autor,personalize;
   protected JButton bNew,bOpen,bSave,bSaveAll,bCut,bCopy,bPaste,bSearch,bReplace;
   protected JButton bUndo,bRedo,bCompile,bExecute,bDebug,bAbout;
   //private JScrollPane scrollMain;
   protected JDesktopPane areaVentanas;
   public Vector ventanas;
   
   protected Style colorId,colorReserved,colorComent,colorString;
   protected Style colorNum,colorOp,colorInvalid,colorFondo,colorCursor;
   protected Style tamanyo11,tamanyo12,tamanyo14,tamanyo16,tamanyo18;
   protected Style fuenteCourier,fuenteArial,fuenteBookman,fuenteComic,fuenteTimes;
   protected Style fuenteCenturyGoth,fuenteLucidaSans,fuenteMonotype,fuenteSystem;
   protected Style normal,negrita,cursiva,tachado,tachadoNo;
   protected Style alineacionCounter,espaciado;
   public int atributos[];
   
   protected MyDefaultTableModel modeloTabla;
   private JScrollPane scrollTabla;
   protected JTable tabla;
   private JSplitPane splitter;
   public int errCount=-1;
   public Vector errorsList;
   
   protected Eventos events;
   Stack procesosStack;
   String rutaTemp;
   
   public int winCounter=0;
   public int winPos=0,offset=1;
   
   public int lastUpSearch=-1,lastDownSearch=-1;
   public String lastSearch="";
   public boolean contexto=true;
   public boolean abajo=true;
   
   protected LexicoRealTime lexicolor;
   
   protected String ident="   ";//3 default:3
   public ArrayList recientes;
   
   public Editor(){
      super("Editor");
      
      setSize(800,600);
      setLocation(30,30);
      
      atributos=new int[12];
      atributos[0]=1;//Fuente
      atributos[1]=2;//Tamaño
      atributos[2]=-16777216;//Color del cursor
      atributos[3]=-1;//Color de fondo
      atributos[4]=1;//Define si es normal(1), negrita(2), cursiva(3) o negrita y cursiva(4)
      atributos[5]=-7654681;//Color identificadores
      atributos[6]=-16776961;//Color palabras reservadas
      atributos[7]=-16731648;//Color comentarios
      atributos[8]=-5111808;//Color cadenas
      atributos[9]=-8355712;//Color numeros
      atributos[10]=-16681983;//Color operadores
      atributos[11]=-65536;//Color caracteres invalidos
      abreINI();
      
      lexicolor=new LexicoRealTime(this);
      
      startComponents();
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add("North",barraHerramientas);
      //getContentPane().add("Center",areaVentanas);
      getContentPane().add("Center",splitter);
      
      splitter.setSize(780,550);
      splitter.setDividerSize(5);
      splitter.setDividerLocation(0.77);//0.80
      splitter.setResizeWeight(1);
      
      procesosStack=new Stack();
      errorsList=new Vector(10,10);
      
      recientes=new ArrayList(4);
      abreRecientes();
   }
   private void startComponents(){
      events=new Eventos(this);
      
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter(){
         public void windowClosing(WindowEvent e){
            procesosStack.push(new pendingProcess(0,null));
            cerrarEditor();
         }
      });
      //--------------------Inicializa el menu-------------------
      barraMenu=new JMenuBar();
      archivo=new JMenu("Archivo");
      archivo.setMnemonic('A');
      edicion=new JMenu("Edición");
      edicion.setMnemonic('E');
      buscar=new JMenu("Buscar");
      buscar.setMnemonic('B');
      compilar=new JMenu("Compilar");
      compilar.setMnemonic('C');
      windows=new JMenu("Ventanas");
      windows.setMnemonic('V');
      ayuda=new JMenu("?");
      //ayuda.setMnemonic('?');
      
      nuevo=new JMenuItem("Nuevo");
      nuevo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_MASK));
      nuevo.addActionListener(events);
      open=new JMenuItem("Abrir");
      open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.CTRL_MASK));
      open.addActionListener(events);
      close=new JMenuItem("Cerrar");
      close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,InputEvent.CTRL_MASK));
      close.addActionListener(events);
      save=new JMenuItem("Guardar");
      save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,InputEvent.CTRL_MASK));
      save.addActionListener(events);
      save.setEnabled(false);
      saveAs=new JMenuItem("Guardar como");
      saveAs.addActionListener(events);
      saveAll=new JMenuItem("Guardar todo");
      saveAll.addActionListener(events);
      saveAll.setEnabled(false);
      salir=new JMenuItem("Salir");
      salir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,InputEvent.ALT_MASK));
      salir.addActionListener(events);
      archivo.add(nuevo);
      archivo.add(open);
      archivo.add(close);
      archivo.add(save);
      archivo.add(saveAs);
      archivo.add(saveAll);
      archivo.addSeparator();
      archivo.addSeparator();
      archivo.add(salir);
      
      cut=new JMenuItem("Cortar");
      cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,InputEvent.CTRL_MASK));
      cut.addActionListener(events);
      cut.setEnabled(false);
      copy=new JMenuItem("Copiar");
      copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_MASK));
      copy.addActionListener(events);
      copy.setEnabled(false);
      paste=new JMenuItem("Pegar");
      paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,InputEvent.CTRL_MASK));
      paste.addActionListener(events);
      paste.setEnabled(false);
      undo=new JMenuItem("Deshacer");//undo=new JMenuItem(deshacer);
      undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,InputEvent.CTRL_MASK));
      undo.setEnabled(false);
      redo=new JMenuItem("Rehacer");//redo=new JMenuItem(rehacer);
      redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_MASK));
      redo.setEnabled(false);
      todo=new JMenuItem("Seleccionar todo");
      todo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,InputEvent.CTRL_MASK));
      todo.addActionListener(events);
      todo.setEnabled(false);
      edicion.add(cut);
      edicion.add(copy);
      edicion.add(paste);
      edicion.add(undo);
      edicion.add(redo);
      edicion.add(todo);
      
      busc=new JMenuItem("Buscar...");
      busc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,InputEvent.CTRL_MASK));
      busc.addActionListener(events);
      busc.setEnabled(false);
      reemplazar=new JMenuItem("Reemplazar...");
      reemplazar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_MASK));
      reemplazar.addActionListener(events);
      reemplazar.setEnabled(false);
      irA=new JMenuItem("Ir a...");
      irA.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,InputEvent.CTRL_MASK));
      irA.addActionListener(events);
      irA.setEnabled(false);
      buscar.add(busc);
      buscar.add(reemplazar);
      buscar.add(irA);
      
      compile=new JMenuItem("Compilar");
      compile.setAccelerator(KeyStroke.getKeyStroke("F9"));
      compile.addActionListener(events);
      execute=new JMenuItem("Ejecutar");
      execute.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9,InputEvent.CTRL_MASK));
      execute.addActionListener(events);
      depurar=new JMenuItem("Depurar");
      depurar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9,InputEvent.ALT_MASK));
      depurar.addActionListener(events);
      compile.setEnabled(false);
      execute.setEnabled(false);
      depurar.setEnabled(false);
      compilar.add(compile);
      compilar.add(execute);
      compilar.add(depurar);
      
      autor=new JMenuItem("Acerca de...");
      autor.setAccelerator(KeyStroke.getKeyStroke("F2"));
      autor.addActionListener(events);
      personalize=new JMenuItem("Personalizar");
      personalize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,InputEvent.CTRL_MASK));
      personalize.addActionListener(events);
      ayuda.add(autor);
      ayuda.add(personalize);
      
      barraMenu.add(archivo);
      barraMenu.add(edicion);
      barraMenu.add(buscar);
      barraMenu.add(compilar);
      barraMenu.add(windows);
      barraMenu.add(ayuda);
      
      setJMenuBar(barraMenu);
      //--------------------Fin del menu-------------------
      
      //--------------Inicializa barra de Herramientas-------------
      barraHerramientas=new JToolBar();
      bNew=new JButton(new ImageIcon("nuevo.gif"));
      bNew.addActionListener(events);
      bOpen=new JButton(new ImageIcon("open.gif"));
      bOpen.addActionListener(events);
      bSave=new JButton(new ImageIcon("diskett2s.GIF"));
      bSave.addActionListener(events);
      bSave.setEnabled(false);
      bSaveAll=new JButton(new ImageIcon("saveAll.gif"));
      bSaveAll.addActionListener(events);
      bSaveAll.setEnabled(false);
      bCut=new JButton(new ImageIcon("cut.GIF"));
      bCut.addActionListener(events);
      bCut.setEnabled(false);
      bCopy=new JButton(new ImageIcon("copiar.gif"));
      bCopy.addActionListener(events);
      bCopy.setEnabled(false);
      bPaste=new JButton(new ImageIcon("paste.GIF"));
      bPaste.addActionListener(events);
      bPaste.setEnabled(false);
      bUndo=new JButton(new ImageIcon("undo2.gif"));
      bUndo.addActionListener(events);
      bUndo.setEnabled(false);
      bRedo=new JButton(new ImageIcon("redo2.gif"));
      bRedo.addActionListener(events);
      bRedo.setEnabled(false);
      bSearch=new JButton(new ImageIcon("buscar.gif"));
      bSearch.addActionListener(events);
      bSearch.setEnabled(false);
      bReplace=new JButton(new ImageIcon("reemplazar.gif"));
      bReplace.addActionListener(events);
      bReplace.setEnabled(false);
      bCompile=new JButton(new ImageIcon("compilar.gif"));
      bCompile.addActionListener(events);
      bCompile.setEnabled(false);
      bExecute=new JButton(new ImageIcon("play.gif"));
      bExecute.addActionListener(events);
      bExecute.setEnabled(false);
      bDebug=new JButton(new ImageIcon("debug.gif"));
      bDebug.addActionListener(events);
      bDebug.setEnabled(false);
      bAbout=new JButton(new ImageIcon("help.gif"));
      bAbout.addActionListener(events);
      
      barraHerramientas.add(bNew);
      barraHerramientas.add(bOpen);
      barraHerramientas.add(bSave);
      barraHerramientas.add(bSaveAll);
      barraHerramientas.addSeparator();
      barraHerramientas.addSeparator();
      barraHerramientas.add(bCut);
      barraHerramientas.add(bCopy);
      barraHerramientas.add(bPaste);
      barraHerramientas.add(bUndo);
      barraHerramientas.add(bRedo);
      barraHerramientas.addSeparator();
      barraHerramientas.addSeparator();
      barraHerramientas.add(bSearch);
      barraHerramientas.add(bReplace);
      barraHerramientas.addSeparator();
      barraHerramientas.addSeparator();
      barraHerramientas.add(bCompile);
      barraHerramientas.add(bExecute);
      barraHerramientas.add(bDebug);
      barraHerramientas.addSeparator();
      barraHerramientas.addSeparator();
      barraHerramientas.add(bAbout);
      
      bNew.setToolTipText("Nuevo");
      bOpen.setToolTipText("Abrir");
      bSave.setToolTipText("Guardar");
      bSaveAll.setToolTipText("Guardar todo");
      bCut.setToolTipText("Cortar");
      bCopy.setToolTipText("Copiar");
      bPaste.setToolTipText("Pegar");
      bSearch.setToolTipText("Buscar");
      bReplace.setToolTipText("Reemplazar");
      bUndo.setToolTipText("Deshacer");
      bRedo.setToolTipText("Rehacer");
      bCompile.setToolTipText("Compilar");
      bExecute.setToolTipText("Ejecutar");
      bDebug.setToolTipText("Depurar");
      bAbout.setToolTipText("Acerca de...");
      //-----------------Fin barra de Herramientas-----------------
      
      //-----Inicia area de ventanas y una ventana por default-----
      areaVentanas=new JDesktopPane();
      areaVentanas.setBackground(Color.gray);
      //areaVentanas.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
      //scrollMain=new JScrollPane();
      //scrollMain.getViewport().add(areaVentanas);
      
      ventanas=new Vector(5,5);
      JInternalFrame tmp=creaVentana(true,null);
      try{
         tmp.setMaximum(true);
         tmp.setSelected(true);
      }catch(PropertyVetoException pve){
         System.out.println("Error al seleccionar la ventana:");
         pve.printStackTrace();
      }
      areaVentanas.setSelectedFrame(tmp);
      //--------------------Fin area de ventanas-------------------
      
      //--------------------Inicio tabla inferior---------------------
      Object colums[]={"Linea","Tipo","Error","Archivo"};
      modeloTabla=new MyDefaultTableModel(colums,0);
      tabla=new JTable(modeloTabla);
      tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      tabla.getColumn("Linea").setMaxWidth(tabla.getColumn("Linea").getWidth());
      tabla.getColumn("Tipo").setMaxWidth(tabla.getColumn("Tipo").getWidth());
      tabla.getColumn("Error").setMaxWidth(1000);
      tabla.getColumn("Archivo").setMaxWidth(1000);
      
      tabla.getColumn("Linea").setMinWidth(40);
      tabla.getColumn("Linea").setPreferredWidth(50);
      
      tabla.getColumn("Tipo").setMinWidth(60);
      tabla.getColumn("Tipo").setPreferredWidth(100);
      
      tabla.getColumn("Error").setMinWidth(100);
      tabla.getColumn("Error").setPreferredWidth(525);
      
      tabla.getColumn("Archivo").setMinWidth(60);
      tabla.getColumn("Archivo").setPreferredWidth(120);
      
      tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
         public void valueChanged(ListSelectionEvent e){
            //Ignore extra messages.
            if (e.getValueIsAdjusting()) 
               return;
            
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            if (lsm.isSelectionEmpty()){
               //System.out.println("No hay filas seleccionadas.");
            }else{
               int selectedRow=lsm.getMinSelectionIndex();
               //System.out.println("Fila "+(selectedRow+1)+" seleccionada");
               String temp=(String)tabla.getModel().getValueAt(selectedRow,0);
               if(temp!=null&&temp!=""){
                  seleccionaToken(selectedRow);
                  //seleccionarLinea((Integer.parseInt(temp))-1);
               }
            }
         }
      });
      scrollTabla=new JScrollPane(tabla,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      scrollTabla.getViewport().setBackground(Color.white);
      tabla.setShowGrid(false);
      //tabla.setDragEnabled(false);
      //Dimension dim=tabla.getIntercellSpacing();
      //System.out.println("Espacio entre celdas: "+dim.height+","+dim.width);
      tabla.setIntercellSpacing(new Dimension(0,0));
      
      splitter=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      splitter.setLeftComponent(areaVentanas);
      splitter.setRightComponent(scrollTabla);
      //--------------------Fin de tabla inferior---------------------
   }
   
   
   //-------------------------------------------------------------------------------------------
   /*------------------------Todo lo referente a los estilos de letra-------------------------*/
   //-------------------------------------------------------------------------------------------
   private StyleContext creaEstilos(){
      StyleContext sc=new StyleContext();
      
      colorFondo=sc.addStyle("Fondo",null);
      StyleConstants.setBackground(colorFondo,new Color(atributos[3]));
      
      colorId=sc.addStyle(null,null);
      colorReserved=sc.addStyle(null,null);
      colorComent=sc.addStyle(null,null);
      colorString=sc.addStyle(null,null);
      colorNum=sc.addStyle(null,null);
      colorOp=sc.addStyle(null,null);
      colorInvalid=sc.addStyle(null,null);
      colorCursor=sc.addStyle(null,null);
      StyleConstants.setForeground(colorId,new Color(atributos[5]));
      StyleConstants.setForeground(colorReserved,new Color(atributos[6]));
      StyleConstants.setForeground(colorComent,new Color(atributos[7]));
      StyleConstants.setForeground(colorString,new Color(atributos[8]));
      StyleConstants.setForeground(colorNum,new Color(atributos[9]));
      StyleConstants.setForeground(colorOp,new Color(atributos[10]));
      StyleConstants.setForeground(colorInvalid,new Color(atributos[11]));
      StyleConstants.setForeground(colorCursor,new Color(atributos[2]));
      
      tamanyo11=sc.addStyle(null,null);
      tamanyo12=sc.addStyle(null,null);
      tamanyo14=sc.addStyle(null,null);
      tamanyo16=sc.addStyle(null,null);
      tamanyo18=sc.addStyle(null,null);
      StyleConstants.setFontSize(tamanyo11,11);
      StyleConstants.setFontSize(tamanyo12,12);
      StyleConstants.setFontSize(tamanyo14,14);
      StyleConstants.setFontSize(tamanyo16,16);
      StyleConstants.setFontSize(tamanyo18,18);
      
      fuenteCourier=sc.addStyle(null,null);
      fuenteArial=sc.addStyle(null,null);
      fuenteBookman=sc.addStyle(null,null);
      fuenteComic=sc.addStyle(null,null);
      fuenteTimes=sc.addStyle(null,null);
      fuenteCenturyGoth=sc.addStyle(null,null);
      fuenteLucidaSans=sc.addStyle(null,null);
      fuenteMonotype=sc.addStyle(null,null);
      fuenteSystem=sc.addStyle(null,null);
      StyleConstants.setFontFamily(fuenteCourier,"Courier");
      StyleConstants.setFontFamily(fuenteArial,"Arial");
      StyleConstants.setFontFamily(fuenteBookman,"Bookman Old Style");
      StyleConstants.setFontFamily(fuenteComic,"Comic Sans MS");
      StyleConstants.setFontFamily(fuenteTimes,"Times New Roman");
      StyleConstants.setFontFamily(fuenteCenturyGoth,"Century Gothic");
      StyleConstants.setFontFamily(fuenteLucidaSans,"Lucida Sans");
      StyleConstants.setFontFamily(fuenteMonotype,"Monotype Corsiva");
      StyleConstants.setFontFamily(fuenteSystem,"System");
      
      normal=sc.addStyle(null,null);
      negrita=sc.addStyle(null,null);
      cursiva=sc.addStyle(null,null);
      tachado=sc.addStyle(null,null);
      tachadoNo=sc.addStyle(null,null);
      
      StyleConstants.setBold(normal,false);
      StyleConstants.setItalic(normal,false);
      StyleConstants.setBold(negrita,true);
      StyleConstants.setItalic(cursiva,true);
      StyleConstants.setStrikeThrough(tachado,true);
      StyleConstants.setStrikeThrough(tachadoNo,false);
      
      alineacionCounter=sc.addStyle(null,null);
      StyleConstants.setAlignment(alineacionCounter,StyleConstants.ALIGN_RIGHT);
      espaciado=sc.addStyle(null,null);
      StyleConstants.setLineSpacing(espaciado,0.1f);
      
      return(sc);
   }
   public void actualizaVentanas(int letra,int tamanyo,int tipo,int fondo,int colCur){
      for(int n=0;n<ventanas.size();n++){
         Ventana tmpWin=(Ventana)ventanas.get(n);
         if(!tmpWin.nombre.startsWith("@")){
            tmpWin.areaTexto.selectAll();
            tmpWin.lineCounter.selectAll();
            fuente(tmpWin,letra,tamanyo,tipo,fondo,colCur);
            tmpWin.lineCounter.setCaretPosition(tmpWin.lineCounter.getDocument().getLength());
            coloreaTodo(tmpWin);
         }
      }
      JInternalFrame frameTmp=areaVentanas.getSelectedFrame();
      if(frameTmp==null)
         System.out.println("Error, no se pudo obtener la ventana activa");
      else{
         Ventana ventmp=dameVentana(frameTmp.getTitle());
         ventmp.lineCounter.setCaretPosition(ventmp.lineCounter.getDocument().getLength());
         ventmp.areaTexto.requestFocus();
      }
   }
   public void fuente(Ventana ven,int selFont,int size,int tipo,int bground,int colorCur){
      atributos[0]=selFont;
      atributos[1]=size;
      atributos[4]=tipo;
      
      StyleConstants.setBackground(colorFondo,new Color(bground));
      ven.areaTexto.setBackground(new Color(bground));
      atributos[3]=bground;
      ven.areaTexto.setCharacterAttributes(colorFondo,false);
      
      StyleConstants.setForeground(colorCursor,new Color(colorCur));
      atributos[2]=colorCur;
      ven.areaTexto.setCharacterAttributes(colorCursor,false);
      ven.areaTexto.setCaretColor(new Color(atributos[2]));
      ven.areaTexto.setCharacterAttributes(tachadoNo,false);
      
      if(selFont==1){
         ven.areaTexto.setCharacterAttributes(fuenteCourier,false);
         ven.lineCounter.setCharacterAttributes(fuenteCourier,false);
         ident="   ";//3 default:3
      }else
         if(selFont==2){
            ven.areaTexto.setCharacterAttributes(fuenteArial,false);
            ven.lineCounter.setCharacterAttributes(fuenteArial,false);
            ident="   ";//3 default:5
         }else
            if(selFont==3){
               ven.areaTexto.setCharacterAttributes(fuenteBookman,false);
               ven.lineCounter.setCharacterAttributes(fuenteBookman,false);
               ident="   ";//3 default:6
            }else
               if(selFont==4){
                  ven.areaTexto.setCharacterAttributes(fuenteComic,false);
                  ven.lineCounter.setCharacterAttributes(fuenteComic,false);
                  ident="   ";//3 default:6
               }else
                  if(selFont==5){
                     ven.areaTexto.setCharacterAttributes(fuenteTimes,false);
                     ven.lineCounter.setCharacterAttributes(fuenteTimes,false);
                     ident="   ";//3 default:5
                  }else
                     if(selFont==6){
                        ven.areaTexto.setCharacterAttributes(fuenteCenturyGoth,false);
                        ven.lineCounter.setCharacterAttributes(fuenteCenturyGoth,false);
                        ident="   ";//3 default:5
                     }else
                        if(selFont==7){
                           ven.areaTexto.setCharacterAttributes(fuenteLucidaSans,false);
                           ven.lineCounter.setCharacterAttributes(fuenteLucidaSans,false);
                           ident="   ";//3 default:5
                        }else
                           if(selFont==8){
                              ven.areaTexto.setCharacterAttributes(fuenteMonotype,false);
                              ven.lineCounter.setCharacterAttributes(fuenteMonotype,false);
                              ident="   ";//3 default:7
                           }else{
                              ven.areaTexto.setCharacterAttributes(fuenteSystem,false);
                              ven.lineCounter.setCharacterAttributes(fuenteSystem,false);
                              ident="   ";//3 default:6
                           }
      
      if(size==1){
         ven.areaTexto.setCharacterAttributes(tamanyo11,false);
         ven.lineCounter.setCharacterAttributes(tamanyo11,false);
      }else
         if(size==2){
            ven.areaTexto.setCharacterAttributes(tamanyo12,false);
            ven.lineCounter.setCharacterAttributes(tamanyo12,false);
         }else
            if(size==3){
               ven.areaTexto.setCharacterAttributes(tamanyo14,false);
               ven.lineCounter.setCharacterAttributes(tamanyo14,false);
            }else
               if(size==4){
                  ven.areaTexto.setCharacterAttributes(tamanyo16,false);
                  ven.lineCounter.setCharacterAttributes(tamanyo16,false);
               }else{
                  ven.areaTexto.setCharacterAttributes(tamanyo18,false);
                  ven.lineCounter.setCharacterAttributes(tamanyo18,false);
               }
      if(tipo==1){
         ven.areaTexto.setCharacterAttributes(normal,false);
         ven.lineCounter.setCharacterAttributes(normal,false);
      }else
         if(tipo==2){
            ven.areaTexto.setCharacterAttributes(normal,false);
            ven.lineCounter.setCharacterAttributes(normal,false);
            ven.areaTexto.setCharacterAttributes(negrita,false);
            ven.lineCounter.setCharacterAttributes(negrita,false);
         }else
            if(tipo==3){
               ven.areaTexto.setCharacterAttributes(normal,false);
               ven.lineCounter.setCharacterAttributes(normal,false);
               ven.areaTexto.setCharacterAttributes(cursiva,false);
               ven.lineCounter.setCharacterAttributes(cursiva,false);
            }else
               if(tipo==4){
                  ven.areaTexto.setCharacterAttributes(negrita,false);
                  ven.lineCounter.setCharacterAttributes(negrita,false);
                  ven.areaTexto.setCharacterAttributes(cursiva,false);
                  ven.lineCounter.setCharacterAttributes(cursiva,false);
               }
   }
   public void actualizaLineCounter(Ventana w){
      w.lineCounter.selectAll();
      int selFont=atributos[0];
      
      if(selFont==1){
         w.lineCounter.setCharacterAttributes(fuenteCourier,false);
      }else
         if(selFont==2){
            w.lineCounter.setCharacterAttributes(fuenteArial,false);
         }else
            if(selFont==3){
               w.lineCounter.setCharacterAttributes(fuenteBookman,false);
            }else
               if(selFont==4){
                  w.lineCounter.setCharacterAttributes(fuenteComic,false);
               }else
                  if(selFont==5){
                     w.lineCounter.setCharacterAttributes(fuenteTimes,false);
                  }else
                     if(selFont==6){
                        w.lineCounter.setCharacterAttributes(fuenteCenturyGoth,false);
                     }else
                        if(selFont==7){
                           w.lineCounter.setCharacterAttributes(fuenteLucidaSans,false);
                        }else
                           if(selFont==8){
                              w.lineCounter.setCharacterAttributes(fuenteMonotype,false);
                           }else{
                              w.lineCounter.setCharacterAttributes(fuenteSystem,false);
                           }
               
      int size=atributos[1];
      if(size==1){
         w.lineCounter.setCharacterAttributes(tamanyo11,false);
      }else
         if(size==2){
            w.lineCounter.setCharacterAttributes(tamanyo12,false);
         }else
            if(size==3){
               w.lineCounter.setCharacterAttributes(tamanyo14,false);
            }else
               if(size==4){
                  w.lineCounter.setCharacterAttributes(tamanyo16,false);
               }else{
                  w.lineCounter.setCharacterAttributes(tamanyo18,false);
               }
      
      int tipo=atributos[4];
      if(tipo==1){
         w.lineCounter.setCharacterAttributes(normal,false);
      }else
         if(tipo==2){
            w.lineCounter.setCharacterAttributes(normal,false);
            w.lineCounter.setCharacterAttributes(negrita,false);
         }else
            if(tipo==3){
               w.lineCounter.setCharacterAttributes(normal,false);
               w.lineCounter.setCharacterAttributes(cursiva,false);
            }else
               if(tipo==4){
                  w.lineCounter.setCharacterAttributes(negrita,false);
                  w.lineCounter.setCharacterAttributes(cursiva,false);
               }
      
      w.lineCounter.setParagraphAttributes(alineacionCounter,false);
      w.lineCounter.setCaretPosition(w.lineCounter.getDocument().getLength());
   }
   
   public void colorFuente(Ventana win,int tipo,int docPos,int longitud){
      DefaultStyledDocument dsDoc=(DefaultStyledDocument)win.areaTexto.getStyledDocument();
      
      switch(tipo){
         case 1:{ dsDoc.setCharacterAttributes(docPos,longitud,colorId,false);
                  dsDoc.setCharacterAttributes(docPos,longitud,tachadoNo,false);
                }break;
         case 2:{ dsDoc.setCharacterAttributes(docPos,longitud,colorReserved,false);
                  dsDoc.setCharacterAttributes(docPos,longitud,tachadoNo,false);
                }break;
         case 3:{ dsDoc.setCharacterAttributes(docPos,longitud,colorComent,false);
                  dsDoc.setCharacterAttributes(docPos,longitud,tachadoNo,false);
                }break;
         case 4:{ dsDoc.setCharacterAttributes(docPos,longitud,colorString,false);
                  dsDoc.setCharacterAttributes(docPos,longitud,tachadoNo,false);
                }break;
         case 5:{ dsDoc.setCharacterAttributes(docPos,longitud,colorNum,false);
                  dsDoc.setCharacterAttributes(docPos,longitud,tachadoNo,false);
                }break;
         case 6:{ dsDoc.setCharacterAttributes(docPos,longitud,colorOp,false);
                  dsDoc.setCharacterAttributes(docPos,longitud,tachadoNo,false);
                }break;
         case 7:{ dsDoc.setCharacterAttributes(docPos,longitud,colorInvalid,false);
                  dsDoc.setCharacterAttributes(docPos,longitud,tachadoNo,false);
                }break;
         case 8:{ dsDoc.setCharacterAttributes(docPos,longitud,colorCursor,false);
                  dsDoc.setCharacterAttributes(docPos,longitud,tachadoNo,false);
                }
                  
      }
   }
   public void cambiaTokenColors(int colores[]){
      if(atributos[5]!=colores[0]){
         StyleConstants.setForeground(colorId,new Color(colores[0]));
         atributos[5]=colores[0];
      }
      if(atributos[6]!=colores[1]){
         StyleConstants.setForeground(colorReserved,new Color(colores[1]));
         atributos[6]=colores[1];
      }
      if(atributos[7]!=colores[2]){
         StyleConstants.setForeground(colorComent,new Color(colores[2]));
         atributos[7]=colores[2];
      }
      if(atributos[8]!=colores[3]){
         StyleConstants.setForeground(colorString,new Color(colores[3]));
         atributos[8]=colores[3];
      }
      if(atributos[9]!=colores[4]){
         StyleConstants.setForeground(colorNum,new Color(colores[4]));
         atributos[9]=colores[4];
      }
      if(atributos[10]!=colores[5]){
         StyleConstants.setForeground(colorOp,new Color(colores[5]));
         atributos[10]=colores[5];
      }
      if(atributos[11]!=colores[6]){
         StyleConstants.setForeground(colorInvalid,new Color(colores[6]));
         atributos[11]=colores[6];
      }
   }
   public void tachar(Ventana win,int docPos,int longitud){
      DefaultStyledDocument dsDoc=(DefaultStyledDocument)win.areaTexto.getStyledDocument();
      dsDoc.setCharacterAttributes(docPos,longitud,tachado,false);
   }
   public void coloreaTodo(Ventana v){
      LexicoAbrir coloreador=new LexicoAbrir(v,this);
      coloreador.analiza();
   }
   
   private void abreINI(){
      String s;
      try{
         FileReader fr=new FileReader("Editor.ini");
         BufferedReader entrada=new BufferedReader(fr);
         for(int i=0;i<12;i++){
            s=entrada.readLine();
            if(s!=null)
               atributos[i]=Integer.parseInt(s);
            else
               System.out.println("Error al cargar la informacion del archivo .ini");
         }
         entrada.close();
      }catch(java.io.IOException ioex){
         System.out.println("Error al abrir el archivo ini, usando las opciones por default.");
      }
   }
   private void guardaINI(){
      try{
         FileWriter fw=new FileWriter("Editor.ini");
         BufferedWriter bw=new BufferedWriter(fw);
         PrintWriter salida=new PrintWriter(bw);
         for(int i=0;i<12;i++)
            salida.println(atributos[i]);
         salida.close();
      }catch(IOException ioex){
         System.out.println("Error al guardar el archivo ini"); 
      }
   }
   //-------------------------------------------------------------------------------------------
   /*--------------------------------Fin de los estilos de letra------------------------------*/
   //-------------------------------------------------------------------------------------------
   
   
   public void hazCompilar(Ventana win){
      //Primero revisa si el archivo es .i:
      if(win.nombre.endsWith(".i")||win.nombre.endsWith(".I")){
         vaciaTabla();
         Sintactico sintactico=new Sintactico(win,this,null,null,null,null);
         sintactico.Programa();
         
         if(errCount==-1){
            win.sinErrores=true;
            bExecute.setEnabled(true);
            bDebug.setEnabled(true);
            execute.setEnabled(true);
            depurar.setEnabled(true);
            modeloTabla.addRow(new Vector());
            tabla.setValueAt("     >>>>>>>>>>>>>>>>>>>>>>>>     SIN ERRORES     <<<<<<<<<<<<<<<<<<<<<<<<",0,2);
         }
      }else{
         new CompErrorDialog(this);
      }
   }
   public void administraProcesosStack(){
      if(!procesosStack.empty()){
         System.out.println("Procesos pendientes: "+procesosStack.size());
         pendingProcess pendiente=(pendingProcess)procesosStack.peek();
         
         switch(pendiente.id){
            //0 = salir
            //1 = cerrar
            //2 = guardar
            //3 = guardar como
            //4 = compilar
            case 0:{ trueCerrarEditor();
                   }break;
            case 1:{ eliminaVentana(pendiente.winWork.nombre);
                   }break;
            case 2:{ if(pendiente.winWork.ruta==null)
                        paGuardar(pendiente.winWork);
                     else
                        trueGuardar(pendiente.winWork);
                   }break;
            case 3:{ paGuardar(pendiente.winWork);
                   }break;
            case 4:{ procesosStack.pop();
                     hazCompilar(pendiente.winWork);
                   }break;
         }
      }else
         System.out.println("Pila vacia");
   }
   private void abreRecientes(){
      String s;
      try{
         FileReader fr=new FileReader("Editor.rec");
         BufferedReader entrada=new BufferedReader(fr);
         int c=0;
         System.out.println("Cargando la lista de archivos recientes");
         while((s=entrada.readLine())!=null&&c<4){
            String nombre=s.substring(s.lastIndexOf("\\")+1,s.length());
            JMenuItem winMenu=new JMenuItem(nombre);
            winMenu.addActionListener(events);
            recientes.add(new Reciente(s,winMenu));
            this.archivo.add(winMenu,c+7);
            c++;
         }
         entrada.close();
      }catch(java.io.IOException ioex){}
   }
   private void guardaRecientes(){
      try{
         FileWriter fw=new FileWriter("Editor.rec");
         BufferedWriter bw=new BufferedWriter(fw);
         PrintWriter salida=new PrintWriter(bw);
         System.out.println("Guardando la lista de archivos recientes");
         for(int i=0;i<recientes.size();i++){
            Reciente r=(Reciente)recientes.get(i);
            salida.println(r.ruta);
         }
         salida.close();
      }catch(IOException ioex){
         System.out.println("Error al guardar el archivo ini"); 
      }
   }
   public void abreSimple(Reciente recien){
      Ventana vt=dameVentana(recien.nombre);
      if(vt!=null){
         if(!vt.ruta.equalsIgnoreCase(recien.ruta)){
            new NombreDuplicadoDialog(this,vt);
         }else{
            //Quita el elemento del menu y lo vuelve a poner al principio
            if(recientes.indexOf(recien)>0){
               //System.out.println("Moviendo elemento al principio del menu de recientes.");
               recientes.remove(recien);
               this.archivo.remove(recien.menuWin);
               recientes.add(0,recien);
               this.archivo.add(recien.menuWin,7);
            }
            try{
               vt.win.setSelected(true);
               vt.areaTexto.requestFocus();
            }catch(PropertyVetoException pve){}
         }
      }else{
         String s;
         int i=1;
         try{
            FileReader fr=new FileReader(recien.ruta);
            BufferedReader entrada=new BufferedReader(fr);
            
            JInternalFrame winTmp=creaVentana(false,recien.nombre);
            Ventana vTmp=dameVentana(recien.nombre);
            vTmp.ruta=recien.ruta;
            vTmp.lineCounter.setEditable(true);
            vTmp.lineCounter.selectAll();
            vTmp.lineCounter.replaceSelection("");
            
            vTmp.areaTexto.removeCaretListener(vTmp.cursor);
            vTmp.areaTexto.getDocument().removeDocumentListener(vTmp.docListener);
            
            vTmp.lineCounter.replaceSelection("0");
            s=entrada.readLine();
            if(s!=null){
               vTmp.areaTexto.replaceSelection(s);
               while((s=entrada.readLine())!=null){
                  vTmp.areaTexto.replaceSelection("\n"+s);
                  vTmp.lineCounter.replaceSelection("\n"+i);
                  i++;
               }
            }
            entrada.close();
            
            vTmp.lineCounter.setEditable(false);
            vTmp.lineas=i;
            vTmp.areaTexto.addCaretListener(vTmp.cursor);
            vTmp.areaTexto.getDocument().addDocumentListener(vTmp.docListener);
            vTmp.manejadorUndo.discardAllEdits();
            vTmp.deshacer.setEnabled(false);
            bUndo.setEnabled(false);
            
            if(vTmp.areaTexto.getDocument().getLength()!=0){
               todo.setEnabled(true);
               bSearch.setEnabled(true);
               bReplace.setEnabled(true);
               busc.setEnabled(true);
               reemplazar.setEnabled(true);
               irA.setEnabled(true);
               compile.setEnabled(true);
               bCompile.setEnabled(true);
            }
            
            actualizaLineCounter(vTmp);
            
            vTmp.teclado.winLineas=i;
            vTmp.areaTexto.selectAll();
            fuente(vTmp,atributos[0],atributos[1],atributos[4],atributos[3],atributos[2]);
            
            coloreaTodo(vTmp);
            vTmp.areaTexto.setCaretPosition(0);
            vTmp.areaTexto.requestFocus();
            
            //Quita el elemento del menu y lo vuelve a poner al principio
            if(recientes.indexOf(recien)>0){
               //System.out.println("Moviendo elemento al principio del menu de recientes.");
               recientes.remove(recien);
               this.archivo.remove(recien.menuWin);
               recientes.add(0,recien);
               this.archivo.add(recien.menuWin,7);
            }
         }catch(IOException ioex){
            System.out.println("No se pudo abrir el archivo");
         }
      }
   }
   public void paAbrir(){
      JFileChooser chooser=new JFileChooser();
      ExampleFileFilter filtro3 = new ExampleFileFilter("pl0","Codigo intermedio PL0");
      chooser.setFileFilter(filtro3);
      ExampleFileFilter filtro2 = new ExampleFileFilter("h","Archivos de Encabezado H");
      chooser.setFileFilter(filtro2);
      ExampleFileFilter filtro = new ExampleFileFilter("i","Archivos Fuente I");
      chooser.setFileFilter(filtro);
      chooser.setCurrentDirectory(new File("."));
      int result=chooser.showOpenDialog(null);
      if(result==JFileChooser.APPROVE_OPTION){
         File archivo=chooser.getSelectedFile();
         String ruta=archivo.getAbsolutePath();
         
         System.out.println("Archivo abierto: "+ruta);
         String ventName=ruta.substring(ruta.lastIndexOf("\\")+1,ruta.length());
         Ventana vt=dameVentana(ventName);
         if(vt!=null){
            if(!vt.ruta.equalsIgnoreCase(ruta))
               new NombreDuplicadoDialog(this,vt);
            else{
               //Quita el elemento del menu y lo vuelve a poner al principio
               int c;
               Reciente r=null;
               boolean isHere=false;
               for(c=0;c<recientes.size()&&!isHere;c++){
                  r=(Reciente)recientes.get(c);
                  if(r.ruta.equalsIgnoreCase(ruta))
                     isHere=true;
               }
               if(isHere){
                  System.out.println("Ya esta el archivo en la lista de recientes.");
                  if(c>0){
                     //System.out.println("Moviendo elemento al principio del menu de recientes.");
                     recientes.remove(r);
                     this.archivo.remove(r.menuWin);
                     recientes.add(0,r);
                     this.archivo.add(r.menuWin,7);
                  }
               }else{
                  if(recientes.size()==4){
                     this.archivo.remove(10);
                     recientes.remove(3);
                  }
                  JMenuItem winMenu=new JMenuItem(vt.nombre);
                  winMenu.addActionListener(events);
                  recientes.add(0,new Reciente(vt.ruta,winMenu));
                  this.archivo.add(winMenu,7);
               }
            }
            try{
               vt.win.setSelected(true);
               vt.areaTexto.requestFocus();
            }catch(PropertyVetoException pve){}
         }else{
            String s;
            int i=1;
            try{
               FileReader fr=new FileReader(ruta);
               BufferedReader entrada=new BufferedReader(fr);
               
               int pos=ruta.lastIndexOf('\\');
               String tmpName=ruta.substring(pos+1);
               JInternalFrame winTmp=creaVentana(false,tmpName);
               Ventana vTmp=dameVentana(tmpName);
               vTmp.ruta=ruta;
               vTmp.lineCounter.setEditable(true);
               vTmp.lineCounter.selectAll();
               vTmp.lineCounter.replaceSelection("");
               
               vTmp.areaTexto.removeCaretListener(vTmp.cursor);
               vTmp.areaTexto.getDocument().removeDocumentListener(vTmp.docListener);
               
               vTmp.lineCounter.replaceSelection("0");
               s=entrada.readLine();
               if(s!=null){
                  vTmp.areaTexto.replaceSelection(s);
                  while((s=entrada.readLine())!=null){
                     vTmp.areaTexto.replaceSelection("\n"+s);
                     vTmp.lineCounter.replaceSelection("\n"+i);
                     i++;
                  }
               }
               entrada.close();
               
               vTmp.lineCounter.setEditable(false);
               vTmp.lineas=i;
               vTmp.areaTexto.addCaretListener(vTmp.cursor);
               vTmp.areaTexto.getDocument().addDocumentListener(vTmp.docListener);
               vTmp.manejadorUndo.discardAllEdits();
               vTmp.deshacer.setEnabled(false);
               bUndo.setEnabled(false);
               
               if(vTmp.areaTexto.getDocument().getLength()!=0){
                  todo.setEnabled(true);
                  bSearch.setEnabled(true);
                  bReplace.setEnabled(true);
                  busc.setEnabled(true);
                  reemplazar.setEnabled(true);
                  irA.setEnabled(true);
                  compile.setEnabled(true);
                  bCompile.setEnabled(true);
               }
               
               actualizaLineCounter(vTmp);
               
               vTmp.teclado.winLineas=i;
               vTmp.areaTexto.selectAll();
               fuente(vTmp,atributos[0],atributos[1],atributos[4],atributos[3],atributos[2]);
               
               coloreaTodo(vTmp);
               vTmp.areaTexto.setCaretPosition(0);
               vTmp.areaTexto.requestFocus();
               
               //Actualiza la seccion de recientes en el menu archivo:
               int c;
               Reciente r=null;
               boolean isHere=false;
               for(c=0;c<recientes.size()&&!isHere;c++){
                  r=(Reciente)recientes.get(c);
                  if(r.ruta.equalsIgnoreCase(vTmp.ruta))
                     isHere=true;
               }
               if(isHere){
                  System.out.println("Ya esta el archivo en la lista de recientes.");
                  if(c>0){
                     recientes.remove(r);
                     this.archivo.remove(r.menuWin);
                     recientes.add(0,r);
                     this.archivo.add(r.menuWin,7);
                  }
               }else{
                  if(recientes.size()==4){
                     this.archivo.remove(10);
                     recientes.remove(3);
                  }
                  JMenuItem winMenu=new JMenuItem(vTmp.nombre);
                  winMenu.addActionListener(events);
                  recientes.add(0,new Reciente(vTmp.ruta,winMenu));
                  this.archivo.add(winMenu,7);
               }
            }catch(IOException ioex){
               System.out.println("No se pudo abrir el archivo");
            }
         }
      }
   }
   public void paGuardar(Ventana winObj){
      JFileChooser chooser=new JFileChooser();
      ExampleFileFilter filtro = new ExampleFileFilter("i","Archivos Fuente I");
      chooser.setFileFilter(filtro);
      chooser.setCurrentDirectory(new File("."));
      chooser.setDialogTitle("Guardar "+winObj.nombre);
      int result=chooser.showSaveDialog(null);
      if(result==chooser.APPROVE_OPTION){
         File archivo=chooser.getSelectedFile();
         String ruta=archivo.getAbsolutePath();
         
         if (!ruta.endsWith(".i")&&!ruta.endsWith(".I")){
            ruta=ruta+".i";
         }
         
         //--Esta seccion es para revisar si ya existe un archivo con ese nombre
         boolean existe;
         try{
            FileReader fr=new FileReader(ruta);
            existe=true;
            fr.close();
         }catch(IOException ex){
            existe=false;
         }
         if(existe){
            //preguntar si se quiere sobreescribir el archivo
            System.out.println("Atencion, el archivo ya existe");
            rutaTemp=ruta;
            new ReplaceFileDialog(this,winObj);
            //replace=false;
         }else{
            winObj.ruta=ruta;
            
            int pos=ruta.lastIndexOf('\\');
            String tmpName=ruta.substring(pos+1);
            winObj.win.setTitle(tmpName);
            winObj.nombre=tmpName;
            winObj.menuWin.setText(tmpName);
            winObj.win.removeInternalFrameListener(winObj.win.getInternalFrameListeners()[0]);
            winObj.win.addInternalFrameListener(new ManejadorIntWin(this,tmpName));
            
            trueGuardar(winObj);
         }
         //---------------------------Fin---------------------------------------
      }else{
         procesosStack.pop();
         if(!procesosStack.empty()){
            pendingProcess pendiente=(pendingProcess)procesosStack.peek();
            if(pendiente.id==4)
               procesosStack.pop();
            administraProcesosStack();
         }
      }
   }
   public void trueGuardar(Ventana win){
      try{
         FileWriter fw=new FileWriter(win.ruta);
         BufferedWriter bw=new BufferedWriter(fw);
         PrintWriter salida=new PrintWriter(bw);
         salida.println(win.areaTexto.getText());
         salida.close();
         System.out.println("Archivo guardado: "+win.ruta);
         win.cambio=false;
         
         bSave.setEnabled(false);
         save.setEnabled(false);
         
         win.deshacer.setEnabled(false);
         win.rehacer.setEnabled(false);
         bUndo.setEnabled(false);
         bRedo.setEnabled(false);
         //bCompile.setEnabled(true);
         win.manejadorUndo.discardAllEdits();
         
         //Actualiza la seccion de recientes en el menu archivo:
         int c;
         Reciente r=null;
         boolean isHere=false;
         for(c=0;c<recientes.size()&&!isHere;c++){
            r=(Reciente)recientes.get(c);
            if(r.ruta.equalsIgnoreCase(win.ruta))
               isHere=true;
         }
         if(isHere){
            /*System.out.println("Ya esta el archivo en la lista de recientes.");
            System.out.println("Posicion en la lista de recientes: "+c);
            System.out.println("Nombre: "+r.nombre);*/
            if(c>0){
               recientes.remove(r);
               this.archivo.remove(r.menuWin);
               recientes.add(0,r);
               this.archivo.add(r.menuWin,7);
            }
         }else{
            if(recientes.size()==4){
               this.archivo.remove(10);
               recientes.remove(3);
            }
            JMenuItem winMenu=new JMenuItem(win.nombre);
            winMenu.addActionListener(events);
            recientes.add(0,new Reciente(win.ruta,winMenu));
            this.archivo.add(winMenu,7);
         }
         
         procesosStack.pop();
         administraProcesosStack();
      }catch(IOException ioex){
         System.out.println("Error al guardar el archivo "+win.ruta);
      }
   }
   public JInternalFrame creaVentana(boolean nuevo,String nombre){
      String winName;
      if(nuevo){
         winCounter++;
         winName="Sin titulo "+winCounter;
      }else
         winName=nombre;
         
      JTextPane lineCounter;
      SubJTextPane areaTexto;
      StyleContext sc=creaEstilos();
      
      areaTexto=new SubJTextPane(new DefaultStyledDocument(sc));
      lineCounter=new JTextPane(new DefaultStyledDocument(sc));
      lineCounter.setBackground(new Color(231,234,241));//(Color.lightGray);
      lineCounter.setText("0");
      lineCounter.setEditable(false);
      
      JScrollPane scroller;
      scroller=new JScrollPane();
      scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      scroller.getViewport().add(areaTexto);
      scroller.setRowHeaderView(lineCounter);
      
      JInternalFrame temp=new JInternalFrame(winName,true,true,true,true);
      temp.getContentPane().add(scroller);
      temp.setSize(500,350);
      
      JMenuItem winMenu=new JMenuItem(winName);
      winMenu.addActionListener(events);
      ventanas.add(new Ventana(temp,lineCounter,areaTexto,winName,winMenu,null));
      windows.add(winMenu);
      areaVentanas.add(temp);
      temp.show();
      temp.setLocation((winPos*25)+offset,winPos*25);
      //System.out.println("Posicion: "+(winPos*25));//475
      winPos++;
      if(winPos==17){//20
         offset+=50;
         winPos=0;
      }
      
      //*******************************************************************
      //********** Esta seccion es para agregar los listeners a la ventana:
      Ventana ventTmp=dameVentana(winName);
      temp.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      temp.addInternalFrameListener(new ManejadorIntWin(this,winName));
      EstadoCursor eCur=new EstadoCursor(this,ventTmp);
      DocChanges cambiosDoc=new DocChanges(this,ventTmp);
      EscuchadorTeclado teclado=new EscuchadorTeclado(this,ventTmp);
      ventTmp.cursor=eCur;
      ventTmp.docListener=cambiosDoc;
      ventTmp.teclado=teclado;
      areaTexto.addCaretListener(ventTmp.cursor);
      areaTexto.getDocument().addDocumentListener(cambiosDoc);
      areaTexto.addKeyListener(teclado);
      
      
      // Manejadores de deshacer y rehacer
      UndoAction deshacer=new UndoAction(this,ventTmp);
      RedoAction rehacer=new RedoAction(this,ventTmp);
      UndoManager manejadorUndo = new UndoManager();
      JMenuItem winUndo=new JMenuItem(deshacer);//3
      JMenuItem winRedo=new JMenuItem(rehacer);//4
      winUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,InputEvent.CTRL_MASK));
      winRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_MASK));
      ventTmp.deshacer=deshacer;
      ventTmp.rehacer=rehacer;
      ventTmp.manejadorUndo=manejadorUndo;
      ventTmp.menuUndo=winUndo;
      ventTmp.menuRedo=winRedo;
      
      edicion.remove(3);
      edicion.add(winUndo,3);
      edicion.remove(4);
      edicion.add(winRedo,4);
      
      areaTexto.getDocument().addUndoableEditListener(new MyUndoableEditListener(ventTmp));
      //********************************************* Fin de los listeners
      //*******************************************************************
      
      //Esta seccion es para poner el texto con la fuente actual:
      actualizaLineCounter(ventTmp);
      fuente(ventTmp,atributos[0],atributos[1],atributos[4],atributos[3],atributos[2]);
      //Fin de la fuente actual---------------------------------
      
      //Esta seccion es para activar las opciones de menu que sean necesarias:
      int numWin=areaVentanas.getComponentCountInLayer(0);
      if(numWin==2){
         bSaveAll.setEnabled(true);
         saveAll.setEnabled(true);
      }
      if(numWin==1){
         close.setEnabled(true);
         saveAs.setEnabled(true);
      }
      save.setEnabled(false);
      bSave.setEnabled(false);
      bCut.setEnabled(false);
      bCopy.setEnabled(false);
      cut.setEnabled(false);
      copy.setEnabled(false);
      bUndo.setEnabled(false);
      bRedo.setEnabled(false);
      todo.setEnabled(false);
      bSearch.setEnabled(false);
      bReplace.setEnabled(false);
      busc.setEnabled(false);
      reemplazar.setEnabled(false);
      irA.setEnabled(false);
      compile.setEnabled(false);
      bCompile.setEnabled(false);
      bExecute.setEnabled(false);
      bDebug.setEnabled(false);
      execute.setEnabled(false);
      depurar.setEnabled(false);
      //fin de opciones de menu
      
      return temp;
   }
   public void eliminaVentana(String nombre){
      //Indica al editor que elimine todos los registros de esta ventana, incluyendo el del menu:
      for(int n=0;n<ventanas.size();n++){
         Ventana tmpWin=(Ventana)ventanas.get(n);
         if(tmpWin.nombre.equals(nombre)){
            if(tmpWin.cambio){
               //Aqui se agrega a la pila de procesos el de cerrar
               new ExitDialog(this,tmpWin.nombre);
            }else{
               System.out.println("Cerrando ventana "+nombre);
               tmpWin.win.dispose();
               
               //Esta seccion es para activar la siguente ventana abierta
               JInternalFrame wins[]=areaVentanas.getAllFrames();
               if(wins.length!=0){
                  areaVentanas.setSelectedFrame(wins[0]);
                  try{
                     wins[0].setSelected(true);
                  }catch(PropertyVetoException pve){
                     System.out.println("Error al seleccionar la ventana:");
                     pve.printStackTrace();
                  }
               }
               // fin de activacion
               
               //Lo siguiente es para revisar si existen errores asociados a esta ventana:
               for(int j=0;j<errorsList.size();j++){
                  Errores errTmp=(Errores)errorsList.elementAt(j);
                  if(tmpWin.nombre.equals(errTmp.win.nombre)){
                     vaciaTabla();
                  }
               }
               //-----------------------------Fin de errores-----------------------------
               windows.remove(tmpWin.menuWin);
               ventanas.remove(n);
               
               //Esta seccion es para desactivar las opciones de menu que sean necesarias:
               int numWin=areaVentanas.getComponentCountInLayer(0);
               if(numWin<2){
                  bSaveAll.setEnabled(false);
                  saveAll.setEnabled(false);
               }
               if(numWin==0){
                  close.setEnabled(false);
                  save.setEnabled(false);
                  saveAs.setEnabled(false);
                  bSave.setEnabled(false);
                  bCut.setEnabled(false);
                  bCopy.setEnabled(false);
                  bPaste.setEnabled(false);
                  cut.setEnabled(false);
                  copy.setEnabled(false);
                  paste.setEnabled(false);
                  bUndo.setEnabled(false);
                  bRedo.setEnabled(false);
                  todo.setEnabled(false);
                  bSearch.setEnabled(false);
                  bReplace.setEnabled(false);
                  busc.setEnabled(false);
                  reemplazar.setEnabled(false);
                  irA.setEnabled(false);
                  compile.setEnabled(false);
                  bCompile.setEnabled(false);
                  bExecute.setEnabled(false);
                  bDebug.setEnabled(false);
                  execute.setEnabled(false);
                  depurar.setEnabled(false);
                  
                  edicion.remove(3);
                  edicion.add(undo,3);
                  edicion.remove(4);
                  edicion.add(redo,4);
               }else{
                  //Actualiza las opciones de deshacer y rehacer 
                  //de acuerdo a la ventana activa:
                  Ventana vtmp=dameVentana(wins[0].getTitle());
                  edicion.remove(3);
                  edicion.add(vtmp.menuUndo,3);
                  edicion.remove(4);
                  edicion.add(vtmp.menuRedo,4);
                  
                  bUndo.setEnabled(vtmp.canUndo);
                  bRedo.setEnabled(vtmp.canRedo);
               }
               //fin de opciones de menu
               
               procesosStack.pop();
               administraProcesosStack();
            }
         }
      }
   }
   public Ventana dameVentana(String nombre){
      for(int n=0;n<ventanas.size();n++){
         Ventana tmpWin=(Ventana)ventanas.get(n);
         if(tmpWin.nombre.equals(nombre)){
            return tmpWin;
         }
      }
      return null;
   }
   public void cerrarEditor(){
      //Agrega a la pila de procesos cerrar para cada ventana
      for(int i=0;i<ventanas.size();i++){
         Ventana tmpWin=(Ventana)ventanas.get(i);
         if(!tmpWin.nombre.startsWith("@"))
            procesosStack.push(new pendingProcess(1,tmpWin));
      }
      administraProcesosStack();
   }
   public void trueCerrarEditor(){
      System.out.println("Terminando la aplicacion");
      System.out.println("Guardando archivo INI");
      guardaINI();
      if(recientes.size()>0)
         guardaRecientes();
      System.exit(0);
   }
   public void cancelaSalir(){
      boolean esta=false;
      for(int i=0;i<procesosStack.size()&&!esta;i++){
         pendingProcess tmp=(pendingProcess)procesosStack.get(i);
         if(tmp.id==0){
            //System.out.println("Se encontro proceso de salir");
            esta=true;
         }
      }
      if(esta){
         esta=false;
         for(int i=0;!esta;i++){
            pendingProcess tmp=(pendingProcess)procesosStack.pop();
            //System.out.println("Eliminando proceso con ID = "+tmp.id);
            if(tmp.id==0)
               esta=true;
         }
      }else
         procesosStack.pop();
      administraProcesosStack();
   }
   
   public void metodoBuscar(Ventana vent,String cadena,int sentido,boolean context){
      if(!lastSearch.equals(cadena)){
         lastDownSearch=-1;
         lastUpSearch=-1;
      }
      lastSearch=cadena;
      String temp="";
      int pos;
      try{
         temp=vent.areaTexto.getDocument().getText(0,vent.areaTexto.getDocument().getLength());
      }catch(BadLocationException ble){
         System.out.println("Error al capturar el texto para realizar la busqueda");
      }
      
      if(!context){
         String temp2=temp.toLowerCase();
         temp=temp2;
         String cadena2=cadena.toLowerCase();
         cadena=cadena2;
      }
      
      if(sentido==1){
         //la busqueda se hara hacia abajo
         lastUpSearch=-1;
         //System.out.println("Ultima posicion del cursor: "+lastDownSearch);
         if(lastDownSearch==-1)
            lastDownSearch=0;
         else
            lastDownSearch=vent.areaTexto.getCaretPosition();
         
         pos=temp.indexOf(cadena,lastDownSearch);
         if(pos==-1){
            vent.areaTexto.setCaretPosition(lastDownSearch);
            new NoEstaDialog(this);
            lastDownSearch=-1;
         }else{
            vent.areaTexto.requestFocus();
            vent.areaTexto.setCaretPosition(pos);
            vent.areaTexto.moveCaretPosition(pos+cadena.length());
         }
            
      }else{
         //la busqueda se hara hacia arriba
         lastDownSearch=-1;
         if(lastUpSearch==-1){
            lastUpSearch=temp.length();
         }
         pos=temp.lastIndexOf(cadena,lastUpSearch);
         if(pos==-1){
            vent.areaTexto.setCaretPosition(lastUpSearch);
            new NoEstaDialog(this);
            lastUpSearch=-1;
         }else{
            lastUpSearch=pos-1;
            vent.areaTexto.requestFocus();
            vent.areaTexto.setCaretPosition(pos);
            vent.areaTexto.moveCaretPosition(pos+cadena.length());
         }
      }
      
   }
   public void metodoReemplazar(Ventana vent,String cadena,String cadena2){
      String selText=vent.areaTexto.getSelectedText();
      if(cadena.equalsIgnoreCase(selText)){
         vent.areaTexto.replaceSelection(cadena2);
         metodoBuscar(vent,cadena,1,false);
      }else{
         new NoEstaDialog(this);
      }
   }
   public void seleccionarLinea(Ventana v,int pos){
      Element documento=v.areaTexto.getDocument().getDefaultRootElement();
      int startLine=documento.getElement(pos).getStartOffset();
      int endLine=documento.getElement(pos).getEndOffset();
      v.areaTexto.requestFocus();
      v.areaTexto.setCaretPosition(startLine);
      v.areaTexto.moveCaretPosition(endLine-1);
   }
   
   public void seleccionaToken(int pos){
      Errores temp=(Errores)errorsList.elementAt(pos);
      if(temp!=null){
         if(temp.win.nombre.startsWith("@")){
            temp.win=abreArchivoH(temp.win);
         }
         if(temp.win.win!=null){
            try{
               temp.win.win.setSelected(true);
            }catch(PropertyVetoException pve){}
            temp.win.areaTexto.requestFocus();
            if(!temp.win.cambio){
               temp.win.areaTexto.setCaretPosition(temp.pos-temp.lexema.length());
               temp.win.areaTexto.moveCaretPosition(temp.pos);
            }else{
               seleccionarLinea(temp.win,temp.fila);
            }
         }
      }else
         System.out.println("Error: no se encontro nada en la fila "+pos+" de la lista de errores");
   }
   private Ventana abreArchivoH(Ventana win){
      String ventName=win.nombre.substring(win.nombre.lastIndexOf("@")+1,win.nombre.length());
      Ventana vt=dameVentana(ventName);
      if(vt!=null){
         if(!vt.ruta.equalsIgnoreCase(win.ruta)){
            new NombreDuplicadoDialog(this,vt);
            return win;
         }else{
            try{
               vt.win.setSelected(true);
               vt.areaTexto.requestFocus();
            }catch(PropertyVetoException pve){}
            ventanas.remove(win);
            return vt;
         }
      }else{
         String s;
         int i=1;
         try{
            FileReader fr=new FileReader(win.ruta);
            BufferedReader entrada=new BufferedReader(fr);
            
            JInternalFrame winTmp=creaVentana(false,ventName);
            Ventana vTmp=dameVentana(ventName);
            vTmp.ruta=win.ruta;
            vTmp.lineCounter.setEditable(true);
            vTmp.lineCounter.selectAll();
            vTmp.lineCounter.replaceSelection("");
            
            vTmp.areaTexto.removeCaretListener(vTmp.cursor);
            vTmp.areaTexto.getDocument().removeDocumentListener(vTmp.docListener);
            
            vTmp.lineCounter.replaceSelection("0");
            s=entrada.readLine();
            if(s!=null){
               vTmp.areaTexto.replaceSelection(s);
               while((s=entrada.readLine())!=null){
                  vTmp.areaTexto.replaceSelection("\n"+s);
                  vTmp.lineCounter.replaceSelection("\n"+i);
                  i++;
               }
            }
            entrada.close();
            
            vTmp.lineCounter.setEditable(false);
            vTmp.lineas=i;
            vTmp.areaTexto.addCaretListener(vTmp.cursor);
            vTmp.areaTexto.getDocument().addDocumentListener(vTmp.docListener);
            vTmp.manejadorUndo.discardAllEdits();
            vTmp.deshacer.setEnabled(false);
            bUndo.setEnabled(false);
            
            if(vTmp.areaTexto.getDocument().getLength()!=0){
               todo.setEnabled(true);
               bSearch.setEnabled(true);
               bReplace.setEnabled(true);
               busc.setEnabled(true);
               reemplazar.setEnabled(true);
               irA.setEnabled(true);
               compile.setEnabled(true);
               bCompile.setEnabled(true);
            }
            
            actualizaLineCounter(vTmp);
            
            vTmp.teclado.winLineas=i;
            vTmp.areaTexto.selectAll();
            fuente(vTmp,atributos[0],atributos[1],atributos[4],atributos[3],atributos[2]);
            
            coloreaTodo(vTmp);
            
            ventanas.remove(win);
            return vTmp;
         }catch(IOException ioex){
            System.out.println("No se pudo abrir el archivo");
            return win;
         }
      }
   }
   public String obtenLinea(Ventana v,int pos){
      Element documento=v.areaTexto.getDocument().getDefaultRootElement();
      int c=v.areaTexto.getDocument().getDefaultRootElement().getElementCount();
      String temp="";
      try{
         temp=v.areaTexto.getDocument().getText(0,v.areaTexto.getDocument().getLength());
      }catch(BadLocationException ble){
         System.out.println("Error al capturar el texto");
      }
      
      int startLine=0,endLine=0;
      
      boolean esta=false;
      
      // Busca la linea en la que se encuentra el cursor:
      for(int i=0;i<documento.getElementCount()&&!esta;i++){
         startLine=documento.getElement(i).getStartOffset();
         endLine=documento.getElement(i).getEndOffset();
         if(pos>=startLine&&pos<=endLine){
            v.teclado.inicioLinea=startLine;
            v.teclado.lineaActual=i;
            esta=true;
         }
      }
      if(esta){
         return(temp.substring(startLine,endLine-1));
      }else{
         //System.out.println("ERROR al capturar el renglon en el documento");
         return null;
      }
   }
   public void errores(int estado,int analisis,String error,int linea,int posicion,String lexema,Ventana ventana,String winName,String rute){
      String tipo;
      switch(analisis){
         case 1: tipo="Lexico"; 
                 break;
         case 2: tipo="Sintactico";
                 break;
         case 3: tipo="Semantico";
                 break;
         default:tipo="General";
      }
      
      String wName=winName.toString();
      if(wName.startsWith("@")){
         wName=wName.substring(wName.lastIndexOf("@")+1,wName.length());
      }
      poneError(tipo,error,linea,errCount,wName/*rute*/);
      posicion++;
      errorsList.add(errCount,new Errores(ventana,linea,posicion,lexema,winName,rute));
   }
   public void poneError(String analisis,String error,int linea,int filaTabla,String rutaFile){
      int numRows=tabla.getRowCount();
      if(filaTabla>=numRows)
         modeloTabla.addRow(new Vector());
      
      int lineColumn=0,typeColumn=1,errorColumn=2,fileColumn=3;
      /*String col=tabla.getColumnName(0);
      System.out.println("Nombre de la columna 0: "+col);
      if(col.equals("Tipo"))
         typeColumn=0;
      else
         if(col.equals("Error"))
            errorColumn=0;
      
      col=tabla.getColumnName(1);
      System.out.println("Nombre de la columna 1: "+col);
      if(col.equals("Linea"))
         lineColumn=1;
      else
         if(col.equals("Error"))
            errorColumn=1;
      
      col=tabla.getColumnName(2);
      System.out.println("Nombre de la columna 2: "+col);
      if(col.equals("Tipo"))
         typeColumn=2;
      else
         if(col.equals("Linea"))
            errorColumn=2;
      */
      
      tabla.setValueAt(Integer.toString(linea),filaTabla,lineColumn);
      tabla.setValueAt(analisis,filaTabla,typeColumn);
      tabla.setValueAt(error,filaTabla,errorColumn);
      tabla.setValueAt(rutaFile,filaTabla,fileColumn);
   }
   public void vaciaTabla(){
      int numRows=tabla.getRowCount();
      errorsList.clear();
      
      if(numRows>0){
         tabla.clearSelection();
         for(int i=0;i<numRows;i++){
            //remueve las filas de mas
            modeloTabla.removeRow(0);
         }
      }
      errCount=-1;
   }
   
   public static void main(String a[]){
      /*try{
         //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
         MetalLookAndFeel.setCurrentTheme(new CharcoalTheme());
      }catch(Exception e){
         System.out.println("ERROR: No se pudo aplicar el Look & Feel que se queria.");
      }*/
      new Editor().show();
   }
}