import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

class ExitDialog extends JDialog implements ActionListener{
   protected JButton si,no,cancelar;
   private JPanel texto,botones;
   private JLabel mensaje,alerta;
   String winName;
   Editor editor;
   
   public ExitDialog(Editor ed,String vent){
      super(ed,"Guardar cambios?",true);
      editor=ed;
      
      winName=vent;
      alerta=new JLabel(new ImageIcon("alerta.gif"),SwingConstants.LEFT);
      mensaje=new JLabel("   ¿Gardar cambios a "+winName+"?");
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
         editor.procesosStack.push(new pendingProcess(2,editor.dameVentana(winName)));
         setModal(false);
         hide();
         editor.administraProcesosStack();
         dispose();
      }
      if(ac.equals("No")){
         //editor.saveBan=true;
         Ventana tmp=editor.dameVentana(winName);
         tmp.cambio=false;
         setModal(false);
         hide();
         editor.eliminaVentana(winName);
         dispose();
      }
      if(ac.equals("Cancelar")){
         editor.cancelaSalir();
         //editor.procesosStack.pop();
         setModal(false);
         hide();
         //editor.administraProcesosStack();
         dispose();
      }
   }
}