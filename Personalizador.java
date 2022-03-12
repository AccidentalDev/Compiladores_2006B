import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;

class Personalizador extends JDialog implements ActionListener{
   private JPanel pIzq,pDer;
   private JPanel pFont,pSize,pType,pColor,pBack,pBotons;
   private JPanel pbId,pbRes,pbCom,pbCad,pbNum,pbOp,pbInv;
   protected JButton aceptar,cancelar,restaurar,bColor,bBack;
   protected JButton bId,bRes,bCom,bCad,bNum,bOp,bInv;
   private JLabel lFont,lSize,lType,lColor,lBack;
   private Choice fontSel,sizeSel;
   private JCheckBox boldCheck,cursivaCheck;
   int intFont,intSize,intType,intColor,intBack;
   int intId,intRes,intCom,intCad,intNum,intOp,intInv;
   boolean boldTrue,cursivaTrue;
   Editor editor;
   
   public Personalizador(Editor ed){
      super(ed,"Personalizar...",true);
      editor=ed;
      
      intFont=ed.atributos[0];
      intSize=ed.atributos[1];
      intColor=ed.atributos[2];
      intBack=ed.atributos[3];
      intType=ed.atributos[4];
      intId=ed.atributos[5];
      intRes=ed.atributos[6];
      intCom=ed.atributos[7];
      intCad=ed.atributos[8];
      intNum=ed.atributos[9];
      intOp=ed.atributos[10];
      intInv=ed.atributos[11];
      
      if(intType==1)
         boldTrue=cursivaTrue=false;
      else
         if(intType==2){
            boldTrue=true;
            cursivaTrue=false;
         }else
            if(intType==3){
               boldTrue=false;
               cursivaTrue=true;
            }else
               if(intType==4)
                  boldTrue=cursivaTrue=true;
      
      //Inicializa todo lo referente al panel izquierdo (opciones generales)
      bColor=new JButton("   ");
      bColor.setBackground(new Color(intColor));
      bColor.addActionListener(this);
      bBack=new JButton("   ");
      bBack.setBackground(new Color(intBack));
      bBack.addActionListener(this);
      
      lFont=new JLabel("Fuente: ");
      lSize=new JLabel("Tamaño: ");
      lType=new JLabel("Tipo: ");
      lColor=new JLabel("Color del cursor: ");
      lBack=new JLabel("Color del fondo: ");
      
      fontSel=new Choice();
      fontSel.addItem("Courier");
      fontSel.addItem("Arial");
      fontSel.addItem("Bookman Old Style");
      fontSel.addItem("Comic Sans MS");
      fontSel.addItem("Times New Roman");
      fontSel.addItemListener(new AccionChoice());
      pFont=new JPanel();
      pFont.add(lFont);
      pFont.add(fontSel);
      
      sizeSel=new Choice();
      sizeSel.addItem("11");
      sizeSel.addItem("12");
      sizeSel.addItem("14");
      sizeSel.addItem("16");
      sizeSel.addItem("18");
      sizeSel.addItemListener(new AccionChoice());
      pSize=new JPanel();
      pSize.add(lSize);
      pSize.add(sizeSel);
      
      boldCheck=new JCheckBox("Negrita",boldTrue);
      cursivaCheck=new JCheckBox("Cursiva",cursivaTrue);
      pType=new JPanel();
      pType.add(boldCheck);
      pType.add(cursivaCheck);
      
      pColor=new JPanel();
      pColor.add(lColor);
      pColor.add(bColor);
      
      pBack=new JPanel();
      pBack.add(lBack);
      pBack.add(bBack);
      
      pIzq=new JPanel();
      pIzq.setLayout(new GridLayout(5,1));
      pIzq.add(pFont);
      pIzq.add(pSize);
      pIzq.add(pType);
      pIzq.add(pColor);
      pIzq.add(pBack);
      pIzq.setBorder(new TitledBorder("General"));
      //--------------------Fin del panel izquierdo-------------------------
      
      //Inicializa todo lo referente al panel derecho (colores de los tokens)
      bId=new JButton("   ");
      bRes=new JButton("   ");
      bCom=new JButton("   ");
      bCad=new JButton("   ");
      bNum=new JButton("   ");
      bOp=new JButton("   ");
      bInv=new JButton("   ");
      bId.setBackground(new Color(intId));
      bRes.setBackground(new Color(intRes));
      bCom.setBackground(new Color(intCom));
      bCad.setBackground(new Color(intCad));
      bNum.setBackground(new Color(intNum));
      bOp.setBackground(new Color(intOp));
      bInv.setBackground(new Color(intInv));
      bId.addActionListener(this);
      bRes.addActionListener(this);
      bCom.addActionListener(this);
      bCad.addActionListener(this);
      bNum.addActionListener(this);
      bOp.addActionListener(this);
      bInv.addActionListener(this);
      
      pDer=new JPanel();
      pDer.setLayout(new GridLayout(7,2));
      pDer.add(new JLabel("Identificadores: ",SwingConstants.RIGHT));
      pDer.add(bId);
      pDer.add(new JLabel("Palabras reservadas: ",SwingConstants.RIGHT));
      pDer.add(bRes);
      pDer.add(new JLabel("Comentarios: ",SwingConstants.RIGHT));
      pDer.add(bCom);
      pDer.add(new JLabel("Cadenas: ",SwingConstants.RIGHT));
      pDer.add(bCad);
      pDer.add(new JLabel("Numeros: ",SwingConstants.RIGHT));
      pDer.add(bNum);
      pDer.add(new JLabel("Operadores: ",SwingConstants.RIGHT));
      pDer.add(bOp);
      pDer.add(new JLabel("Caracteres ilegales: ",SwingConstants.RIGHT));
      pDer.add(bInv);
      pDer.setBorder(new TitledBorder("Tokens"));
      //----------------------Fin del panel derecho--------------------------
      
      
      aceptar=new JButton("Aceptar");
      aceptar.addActionListener(this);
      restaurar=new JButton("Restaurar");
      restaurar.addActionListener(this);
      cancelar=new JButton("Cancelar");
      cancelar.addActionListener(this);
      
      pBotons=new JPanel();
      pBotons.add(aceptar);
      pBotons.add(restaurar);
      pBotons.add(cancelar);
      
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add("West",pIzq);
      getContentPane().add("East",pDer);
      getContentPane().add("South",pBotons);
      setSize(475,265);
      setLocation(300,200);
      setResizable(false);
      
      show();
   }
   
   class AccionChoice implements ItemListener{
      public void itemStateChanged(ItemEvent e){
         if(e.getItem()=="Courier"){
            intFont=1;
         }
         if(e.getItem()=="Arial"){
            intFont=2;
         }
         if(e.getItem()=="Bookman Old Style"){
            intFont=3;
         }
         if(e.getItem()=="Comic Sans MS"){
            intFont=4;
         }
         if(e.getItem()=="Times New Roman"){
            intFont=5;
         }
         if(e.getItem()=="11"){
            intSize=1;
         }
         if(e.getItem()=="12"){
            intSize=2;
         }
         if(e.getItem()=="14"){
            intSize=3;
         }
         if(e.getItem()=="16"){
            intSize=4;
         }
         if(e.getItem()=="18"){
            intSize=5;
         }
      }
   }
   
   public void actionPerformed(ActionEvent e){
      if(e.getSource().equals(bColor)){
         
      }
      if(e.getSource().equals(bBack)){
         
      }
      if(e.getSource().equals(bId)){
         
      }
      if(e.getSource().equals(bRes)){
         
      }
      if(e.getSource().equals(bCom)){
         
      }
      if(e.getSource().equals(bCad)){
         
      }
      if(e.getSource().equals(bNum)){
         
      }
      if(e.getSource().equals(bOp)){
         
      }
      if(e.getSource().equals(bInv)){
         
      }
      
      
      if(e.getSource().equals(aceptar)){
         editor.actualizaVentanas(intFont,intSize,intType,intBack,intColor);
         dispose();
      }
      if(e.getSource().equals(restaurar)){
         /*Dimension dim=getSize();
         System.out.println("Tamaño: "+dim.height+","+dim.width);*/
      }
      if(e.getSource().equals(cancelar)){
         dispose();
      }
   }
}