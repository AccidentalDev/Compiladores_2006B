import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

class NombreDuplicadoDialog extends JDialog implements ActionListener{
   protected JButton acept;
   private JPanel texto,botones;
   private JLabel alerta,mensaje,mensaje2;
   
   public NombreDuplicadoDialog(Editor e,Ventana win){
      super(e,"Error",true);
      
      alerta=new JLabel(new ImageIcon("error.gif"),SwingConstants.LEFT);
      //mensaje=new JLabel("   ERROR: No pueden existir dos documentos");
      //mensaje2=new JLabel("   diferentes con el mismo nombre.");
      mensaje=new JLabel("ERROR: No pueden existir dos documentos diferentes con el mismo nombre.",SwingConstants.CENTER);
      mensaje2=new JLabel("Cierra la ventana con el nombre \""+win.nombre+"\" y vuelve a intentarlo.",SwingConstants.CENTER);
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
      setSize(460,160);
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