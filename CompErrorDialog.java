import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

class CompErrorDialog extends JDialog implements ActionListener{
   protected JButton acept;
   private JPanel texto,botones;
   private JLabel alerta,mensaje,mensaje2;
   
   public CompErrorDialog(Editor e){
      super(e,"Error",true);
      
      alerta=new JLabel(new ImageIcon("error.gif"),SwingConstants.LEFT);
      mensaje=new JLabel("   ERROR: Solo se pueden compilar archivos");
      mensaje2=new JLabel("   fuente del lenguaje I (con extension .i)");
      acept=new JButton("Aceptar");
      acept.addActionListener(this);
      JPanel txt=new JPanel(new BorderLayout());
      txt.add("Center",mensaje);
      txt.add("South",mensaje2);
      texto=new JPanel();
      texto.add(alerta);
      texto.add(txt);
      botones=new JPanel();
      botones.add(acept);
      
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add("Center",texto);
      getContentPane().add("South",botones);
      setSize(280,160);
      setLocation(300,200);
      setResizable(false);
      show();
   }
   
   public void actionPerformed(ActionEvent e){
      String ac=e.getActionCommand();
      if(ac.equals("Aceptar")){
         dispose();
      }
   }
}