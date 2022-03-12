import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

class HayColorDialog extends JDialog implements ActionListener{
   protected JButton aceptar;
   private JPanel texto,botones;
   private JLabel mensaje,alerta;
   
   public HayColorDialog(JDialog padre){
      super(padre,"Colores duplicados",true);
      
      alerta=new JLabel(new ImageIcon("alerta.gif"),SwingConstants.LEFT);
      mensaje=new JLabel("   Ya existe otro elemento con ese color");
      aceptar=new JButton("Aceptar");
      aceptar.addActionListener(this);
      texto=new JPanel();
      texto.add(alerta);
      texto.add(mensaje);
      botones=new JPanel();
      botones.add(aceptar);
      
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add("Center",texto);
      getContentPane().add("South",botones);
      setSize(300,130);
      setLocation(250,250);
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