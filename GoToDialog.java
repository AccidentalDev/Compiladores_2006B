import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class GoToDialog extends JDialog implements ActionListener{
   private JLabel mensaje;
   private JTextField dato;
   protected JButton acept,cancel;
   private JPanel texto,botones;
   Ventana window;
   Editor editor;
   
   public GoToDialog(Editor ed,Ventana win){
      super(ed,"Ir a...",true);
      editor=ed;
      window=win;
      mensaje=new JLabel("Numero de la linea: ");
      dato=new JTextField(5);
      acept=new JButton("Aceptar");
      acept.addActionListener(this);
      cancel=new JButton("Cancelar");
      cancel.addActionListener(this);
      
      texto=new JPanel();
      botones=new JPanel();
      texto.add(mensaje);
      texto.add(dato);
      botones.add(acept);
      botones.add(cancel);
      
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add("Center",texto);
      getContentPane().add("South",botones);
      
      setSize(230,110);
      setLocation(300,200);
      setResizable(false);
      
      show();
   }
   
   public void actionPerformed(ActionEvent e){
      String ac=e.getActionCommand();
      if(ac.equals("Aceptar")){
         String linea=dato.getText();
         try{
            int pos=Integer.parseInt(linea);
            if(pos<=window.lineas-1&&pos>=0){
               editor.seleccionarLinea(window,pos);
               dispose();
            }else
               dato.setText("NO");
         }catch(NumberFormatException nfe){
            dato.setText("NO");
         }
      }
      if(ac.equals("Cancelar")){
         dispose();
      }
   }
}