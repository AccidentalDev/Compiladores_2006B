import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

class AboutDialog extends JDialog implements ActionListener{
   protected JButton ok;
   private JLabel foto,name,code,mail,espacio,carrera,escuela,uni,logoUdeG;
   private JPanel texto,boton;
   
   public AboutDialog(Editor editor){
      super(editor,"Acerca de:",true);
      foto=new JLabel(new ImageIcon("carlossama.JPG"),SwingConstants.CENTER);
      name=new JLabel("Autor: Carlos F. Camacho Uribe",SwingConstants.CENTER);
      code=new JLabel("Codigo: 398251049",SwingConstants.CENTER);
      mail=new JLabel("E-mail: serge_cfcu@hotmail.com",SwingConstants.CENTER);
      espacio=new JLabel("                              ");
      carrera=new JLabel("Ingenieria en Computacion",SwingConstants.CENTER);
      escuela=new JLabel("                    CUCEI                    ",SwingConstants.CENTER);
      uni=new JLabel("Universidad   de   Guadalajara",SwingConstants.CENTER);
      logoUdeG=new JLabel(new ImageIcon("logo.GIF"),SwingConstants.CENTER);
      ok=new JButton("OK");
      ok.addActionListener(this);
      
      texto=new JPanel();
      boton=new JPanel();
      boton.add(ok);
      
      texto.add(foto);
      texto.add(name);
      texto.add(code);
      texto.add(mail);
      texto.add(espacio);
      texto.add(carrera);
      texto.add(escuela);
      texto.add(uni);
      texto.add(logoUdeG);
      
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add("Center",texto);
      getContentPane().add("South",boton);
      
      setSize(210,380);
      setLocation(300,200);
      setResizable(false);
      
      addWindowListener(new WindowAdapter(){
         public void windowClosing(WindowEvent e){
            dispose();
         }
      });
      
      show();
   }
   public void actionPerformed(ActionEvent e){
      String ac=e.getActionCommand();
      if(ac.equals("OK")){
         dispose();
      }
   }
}