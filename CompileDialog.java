import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

class CompileDialog extends JDialog implements ActionListener{
   protected JButton si,cancelar;
   private JPanel texto,botones;
   private JLabel mensaje,mensaje2,alerta;
   Ventana win;
   Editor editor;
   
   public CompileDialog(Editor ed,Ventana v){
      super(ed,"Continuar?",true);
      editor=ed;
      win=v;
      
      alerta=new JLabel(new ImageIcon("alerta.gif"),SwingConstants.LEFT);
      mensaje=new JLabel("   Se van a guardar los cambios");
      mensaje2=new JLabel("   hechos a "+win.nombre);
      si=new JButton("Aceptar");
      si.addActionListener(this);
      cancelar=new JButton("Cancelar");
      cancelar.addActionListener(this);
      JPanel txt=new JPanel(new BorderLayout());
      txt.add("Center",mensaje);
      txt.add("South",mensaje2);
      texto=new JPanel();
      texto.add(alerta);
      texto.add(txt);
      botones=new JPanel();
      botones.add(si);
      botones.add(cancelar);
      
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add("Center",texto);
      getContentPane().add("South",botones);
      setSize(280,130);
      setLocation(300,200);
      setResizable(false);
      show();
   }
   
   public void actionPerformed(ActionEvent e){
      String ac=e.getActionCommand();
      if(ac.equals("Aceptar")){
         editor.procesosStack.push(new pendingProcess(4,win));
         editor.procesosStack.push(new pendingProcess(2,win));
         setModal(false);
         hide();
         editor.administraProcesosStack();
         dispose();
      }
      if(ac.equals("Cancelar")){
         dispose();
      }
   }
}