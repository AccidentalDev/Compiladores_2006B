import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class FindWindow extends JDialog implements ActionListener{
   private JLabel findLabel/*,contextLabel*/;
   protected JRadioButton[] rSentido;
   protected JCheckBox contextCheck;
   private JTextField texto;
   protected JButton buscar,cancel;
   private JPanel pTexto,pContexto,pBoton1,pBoton2;
   private int haciaAbajo;
   Ventana win;
   Editor editor;
   
   public FindWindow(Editor e,Ventana w){
      super(e,"Buscar...",true);
      editor=e;
      win=w;
      
      findLabel=new JLabel("                 Texto:");
      texto=new JTextField(12);
      texto.setText(editor.lastSearch);
      pTexto=new JPanel();
      pTexto.add(texto);
      
      buscar=new JButton("Buscar");
      buscar.addActionListener(this);
      cancel=new JButton("Cancelar");
      cancel.addActionListener(this);
      
      pBoton1=new JPanel();
      pBoton1.add(buscar);
      pBoton2=new JPanel();
      pBoton2.add(cancel);
      
      rSentido=new JRadioButton[2];
      rSentido[0]=new JRadioButton("Hacia arriba");
      rSentido[0].addActionListener(this);
      rSentido[1]=new JRadioButton("Hacia abajo");
      rSentido[1].addActionListener(this);
      if(editor.abajo)
         haciaAbajo=1;
      else
         haciaAbajo=0;
      rSentido[haciaAbajo].setSelected(true);
      
      contextCheck=new JCheckBox("Sensible al contexto",editor.contexto);
      contextCheck.addActionListener(this);
      pContexto=new JPanel();
      pContexto.add(contextCheck);
      
      JPanel pIzq=new JPanel(new GridLayout(3,1));
      pIzq.add(findLabel);
      pIzq.add(rSentido[0]);
      pIzq.add(rSentido[1]);
      JPanel pDer=new JPanel(new GridLayout(3,1));
      pDer.add(pTexto);
      pDer.add(pContexto);
      pDer.add(new JLabel(" "));
      JPanel pSur=new JPanel(new GridLayout(1,2));
      pSur.add(pBoton1);
      pSur.add(pBoton2);
      
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add("West",pIzq);
      getContentPane().add("East",pDer);
      getContentPane().add("South",pSur);
      
      setSize(250,170);
      setLocation(300,200);
      setResizable(false);
      
      show();
   }
   
   public void actionPerformed(ActionEvent e){
      String ac=e.getActionCommand();
      if(ac.equals("Buscar")){
         String linea=texto.getText();
         if(!linea.equals("")){
            if(haciaAbajo==1)
               editor.abajo=true;
            else
               editor.abajo=false;
            editor.metodoBuscar(win,linea,haciaAbajo,editor.contexto);
            dispose();
         }else
            texto.setText("Escribe algo!!");
      }else 
         if(ac.equals("Cancelar")){
            dispose();
         }else
            if(e.getSource().equals(contextCheck)){
               editor.contexto=!editor.contexto;
               System.out.println("Sensible al contexto: "+editor.contexto);
            }else{
               for(int i=0;i<2;i++){
                  if(e.getSource().equals(rSentido[i])){
                     System.out.println("Sentido seleccionado: "+ac);
                     rSentido[i].setSelected(true);
                     haciaAbajo=i;
                  }
                  else
                     rSentido[i].setSelected(false);
               }
            }
   }
   
   /*public static void main(String a[]){
      new FindWindow(null);
   }*/
}