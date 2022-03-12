import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class ReplaceDialog extends JDialog implements ActionListener{
   private JLabel textLabel,replaceLabel;
   private JTextField texto,replace;
   protected JButton next,bReplace,cancel;
   private JPanel pTexto,pFields,pBoton1,pBoton2,pBoton3,pBotones;
   Ventana win;
   Editor editor;
   
   public ReplaceDialog(Editor e,Ventana w){
      super(e,"Reemplazar...",false);
      editor=e;
      win=w;
      
      textLabel=new JLabel("Texto:  ",SwingConstants.RIGHT);
      replaceLabel=new JLabel("Reemplazar por:  ",SwingConstants.RIGHT);
      texto=new JTextField(12);
      texto.setText(editor.lastSearch);
      replace=new JTextField(12);
      
      JPanel pT=new JPanel();
      pT.add(texto);
      JPanel pR=new JPanel();
      pR.add(replace);
      
      pTexto=new JPanel(new GridLayout(1,2));
      pTexto.add(textLabel);
      pTexto.add(pT);
      pFields=new JPanel(new GridLayout(1,2));
      pFields.add(replaceLabel);
      pFields.add(pR);
      
      next=new JButton("Siguiente");
      next.addActionListener(this);
      bReplace=new JButton("Reemplazar");
      bReplace.addActionListener(this);
      bReplace.setEnabled(false);
      cancel=new JButton("Cancelar");
      cancel.addActionListener(this);
      
      pBoton1=new JPanel();
      pBoton1.add(next);
      pBoton2=new JPanel();
      pBoton2.add(bReplace);
      pBoton3=new JPanel();
      pBoton3.add(cancel);
      pBotones=new JPanel(new GridLayout(1,3));
      pBotones.add(pBoton1);
      pBotones.add(pBoton2);
      pBotones.add(pBoton3);
      
      getContentPane().setLayout(new GridLayout(3,1));
      getContentPane().add(pTexto);
      getContentPane().add(pFields);
      getContentPane().add(pBotones);
      
      setSize(312,138);
      setLocation(300,200);
      setResizable(false);
      
      show();
   }
   
   public void actionPerformed(ActionEvent e){
      String ac=e.getActionCommand();
      JInternalFrame frameTmp=editor.areaVentanas.getSelectedFrame();
      if(frameTmp==null)
         System.out.println("Error, no se pudo obtener la ventana activa");
      else
         win=editor.dameVentana(frameTmp.getTitle());
      
      if(ac.equals("Siguiente")){
         String linea=texto.getText();
         if(!linea.equals("")){
            editor.metodoBuscar(win,linea,1,false);
            if(editor.lastDownSearch!=-1)
               bReplace.setEnabled(true);
            //dispose();
         }else
            texto.setText("Escribe algo!!");
      }
      if(ac.equals("Reemplazar")){
         String linea=texto.getText();
         String linea2=replace.getText();
         if(!linea.equals("")&&!linea2.equals("")){
            editor.metodoReemplazar(win,linea,linea2);
            //dispose();
         }else{
            if(linea.equals(""))
               texto.setText("Escribe algo!!");
            if(linea2.equals(""))
               replace.setText("Escribe algo!!");
         }
         
         /*Dimension dim=getSize();
         System.out.println("Tamaño: "+dim.height+","+dim.width);*/
      }
      if(ac.equals("Cancelar")){
         dispose();
      }
   }
}