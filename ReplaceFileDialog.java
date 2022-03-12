import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

class ReplaceFileDialog extends JDialog implements ActionListener{
   protected JButton si,no,cancelar;
   private JPanel texto,botones;
   private JLabel mensaje,alerta;
   Editor editor;
   Ventana win;
   
   public ReplaceFileDialog(Editor e,Ventana v){
      super(e,"Reemplazar?",true);
      editor=e;
      win=v;
      
      alerta=new JLabel(new ImageIcon("alerta.gif"),SwingConstants.LEFT);
      mensaje=new JLabel("   ¿Reemplazar el archivo existente?");
      si=new JButton("Si");
      si.addActionListener(this);
      no=new JButton("No");
      no.addActionListener(this);
      cancelar=new JButton("Cancelar");
      cancelar.addActionListener(this);
      texto=new JPanel();
      texto.add(alerta);
      texto.add(mensaje);
      botones=new JPanel();
      botones.add(si);
      botones.add(no);
      botones.add(cancelar);
      
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add("Center",texto);
      getContentPane().add("South",botones);
      setSize(300,130);
      setLocation(300,200);
      setResizable(false);
      
      show();
   }
   
   public void actionPerformed(ActionEvent e){
      String ac=e.getActionCommand();
      if(ac.equals("Si")){
         win.ruta=editor.rutaTemp;
         String ventName=win.ruta.substring(win.ruta.lastIndexOf("\\")+1,win.ruta.length());
         win.win.setTitle(ventName);
         win.nombre=ventName;
         editor.rutaTemp=null;
         setModal(false);
         hide();
         editor.trueGuardar(win);
         dispose();
      }
      if(ac.equals("No")){
         /*editor.replace=false;
         editor.ruta=null;*/
         setModal(false);
         hide();
         editor.paGuardar(win);
         dispose();
      }
      if(ac.equals("Cancelar")){
      	 editor.procesosStack.pop();
         dispose();
      }
   }
}