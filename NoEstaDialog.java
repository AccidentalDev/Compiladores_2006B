import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class NoEstaDialog extends JDialog implements ActionListener{
   protected JButton ok;
   private JLabel mensaje,alerta;
   private JPanel texto,boton;
   
   public NoEstaDialog(Editor ed){
      super(ed,"No se encontro",true);
      alerta=new JLabel(new ImageIcon("alerta.gif"),SwingConstants.LEFT);
      mensaje=new JLabel("   No se encontró el texto");
      
      ok=new JButton("Aceptar");
      ok.addActionListener(this);
      
      texto=new JPanel();
      texto.add(alerta);
      texto.add(mensaje);
      boton=new JPanel();
      boton.add(ok);
      
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add("Center",texto);
      getContentPane().add("South",boton);
      
      setSize(230,120);
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
   
   /*public static void main(String a[]){
      new NoEstaDialog();
   }*/
}