import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;

class Personalizador2 extends JDialog implements ActionListener{
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
   
   public Personalizador2(Editor ed){
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
      lSize=new JLabel("Tama?o: ");
      lType=new JLabel("Tipo: ");
      lColor=new JLabel("Color del cursor: ");
      lBack=new JLabel("  Color del fondo: ");
      
      fontSel=new Choice();
      fontSel.addItem("Courier");
      fontSel.addItem("Arial");
      fontSel.addItem("Bookman Old Style");
      fontSel.addItem("Comic Sans MS");
      fontSel.addItem("Times New Roman");
      fontSel.addItem("Century Gothic");
      fontSel.addItem("Lucida Sans");
      fontSel.addItem("Monotype Corsiva");
      fontSel.addItem("System");
      fontSel.select(intFont-1);
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
      sizeSel.select(intSize-1);
      sizeSel.addItemListener(new AccionChoice());
      pSize=new JPanel();
      pSize.add(lSize);
      pSize.add(sizeSel);
      
      boldCheck=new JCheckBox("Negrita",boldTrue);
      boldCheck.addActionListener(this);
      cursivaCheck=new JCheckBox("Cursiva",cursivaTrue);
      cursivaCheck.addActionListener(this);
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
      pIzq.setBorder(new TitledBorder("GENERAL: "));
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
      
      pbId=new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pbRes=new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pbCom=new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pbCad=new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pbNum=new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pbOp=new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pbInv=new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pbId.add(new JLabel("Identificadores: "));
      pbRes.add(new JLabel("Palabras reservadas: "));
      pbCom.add(new JLabel("Comentarios: "));
      pbCad.add(new JLabel("Cadenas: "));
      pbNum.add(new JLabel("Numeros: "));
      pbOp.add(new JLabel("Operadores: "));
      pbInv.add(new JLabel("Caracteres ilegales: "));
      pbId.add(bId);
      pbRes.add(bRes);
      pbCom.add(bCom);
      pbCad.add(bCad);
      pbNum.add(bNum);
      pbOp.add(bOp);
      pbInv.add(bInv);
      
      pDer=new JPanel();
      pDer.setLayout(new GridLayout(7,1));
      pDer.add(pbId);
      pDer.add(pbRes);
      pDer.add(pbCom);
      pDer.add(pbCad);
      pDer.add(pbNum);
      pDer.add(pbOp);
      pDer.add(pbInv);
      pDer.setBorder(new TitledBorder("TOKENS: "));
      //----------------------Fin del panel derecho--------------------------
      
      
      aceptar=new JButton("Aceptar");
      aceptar.addActionListener(this);
      restaurar=new JButton("Restaurar");
      restaurar.addActionListener(this);
      restaurar.setToolTipText("Establece las opciones por default");
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
      setSize(405,280);
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
         if(e.getItem()=="Century Gothic"){
            intFont=6;
         }
         if(e.getItem()=="Lucida Sans"){
            intFont=7;
         }
         if(e.getItem()=="Monotype Corsiva"){
            intFont=8;
         }
         if(e.getItem()=="System"){
            intFont=9;
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
   
   public boolean estaColor(int cual,Color esteColor){
      if(cual!=1)
         if(esteColor.equals(new Color(intColor)))
            return true;
      if(cual!=2)
         if(esteColor.equals(new Color(intBack)))
            return true;
      if(cual!=3)
         if(esteColor.equals(new Color(intId)))
            return true;
      if(cual!=4)
         if(esteColor.equals(new Color(intRes)))
            return true;
      if(cual!=5)
         if(esteColor.equals(new Color(intCom)))
            return true;
      if(cual!=6)
         if(esteColor.equals(new Color(intCad)))
            return true;
      if(cual!=7)
         if(esteColor.equals(new Color(intNum)))
            return true;
      if(cual!=8)
         if(esteColor.equals(new Color(intOp)))
            return true;
      if(cual!=9)
         if(esteColor.equals(new Color(intInv)))
            return true;
      
      return false;
   }
   public void actionPerformed(ActionEvent e){
      if(e.getSource().equals(bColor)){
         Color newColor=JColorChooser.showDialog(this,"Escoge un color",new Color(intColor));
         if(newColor!=null){
            if(!estaColor(1,newColor)){
               bColor.setBackground(newColor);
               intColor=newColor.getRGB();
            }else
               new HayColorDialog(this);
         }
      }
      if(e.getSource().equals(bBack)){
         Color newColor=JColorChooser.showDialog(this,"Escoge un color",new Color(intBack));
         if(newColor!=null){
            if(!estaColor(2,newColor)){
               bBack.setBackground(newColor);
               intBack=newColor.getRGB();
            }else
               new HayColorDialog(this);
         }
      }
      if(e.getSource().equals(bId)){
         Color newColor=JColorChooser.showDialog(this,"Escoge un color",new Color(intId));
         if(newColor!=null){
            if(!estaColor(3,newColor)){
               bId.setBackground(newColor);
               intId=newColor.getRGB();
            }else
               new HayColorDialog(this);
         }
      }
      if(e.getSource().equals(bRes)){
         Color newColor=JColorChooser.showDialog(this,"Escoge un color",new Color(intRes));
         if(newColor!=null){
            if(!estaColor(4,newColor)){
               bRes.setBackground(newColor);
               intRes=newColor.getRGB();
            }else
               new HayColorDialog(this);
         }
      }
      if(e.getSource().equals(bCom)){
         Color newColor=JColorChooser.showDialog(this,"Escoge un color",new Color(intCom));
         if(newColor!=null){
            if(!estaColor(5,newColor)){
               bCom.setBackground(newColor);
               intCom=newColor.getRGB();
            }else
               new HayColorDialog(this);
         }
      }
      if(e.getSource().equals(bCad)){
         Color newColor=JColorChooser.showDialog(this,"Escoge un color",new Color(intCad));
         if(newColor!=null){
            if(!estaColor(6,newColor)){
               bCad.setBackground(newColor);
               intCad=newColor.getRGB();
            }else
               new HayColorDialog(this);
         }
      }
      if(e.getSource().equals(bNum)){
         Color newColor=JColorChooser.showDialog(this,"Escoge un color",new Color(intNum));
         if(newColor!=null){
            if(!estaColor(7,newColor)){
               bNum.setBackground(newColor);
               intNum=newColor.getRGB();
            }else
               new HayColorDialog(this);
         }
      }
      if(e.getSource().equals(bOp)){
         Color newColor=JColorChooser.showDialog(this,"Escoge un color",new Color(intOp));
         if(newColor!=null){
            if(!estaColor(8,newColor)){
               bOp.setBackground(newColor);
               intOp=newColor.getRGB();
            }else
               new HayColorDialog(this);
         }
      }
      if(e.getSource().equals(bInv)){
         Color newColor=JColorChooser.showDialog(this,"Escoge un color",new Color(intInv));
         if(newColor!=null){
            if(!estaColor(9,newColor)){
               bInv.setBackground(newColor);
               intInv=newColor.getRGB();
            }else
               new HayColorDialog(this);
         }
      }
      
      
      
      if(e.getSource().equals(boldCheck)){
         boldTrue=!boldTrue;
      }
      if(e.getSource().equals(cursivaCheck)){
         cursivaTrue=!cursivaTrue;
      }
      
      
      
      if(e.getSource().equals(aceptar)){
         if(boldTrue&&cursivaTrue)
            intType=4;
         else
            if(boldTrue)
               intType=2;
            else
               if(cursivaTrue)
                  intType=3;
               else
                  intType=1;
         //System.out.println("Tipo: "+intType);
         
         int[] colors={intId,intRes,intCom,intCad,intNum,intOp,intInv};
         editor.cambiaTokenColors(colors);
         editor.actualizaVentanas(intFont,intSize,intType,intBack,intColor);
         dispose();
      }
      if(e.getSource().equals(restaurar)){
         intFont=1;
         intSize=2;
         intType=1;
         intColor=-16777216;
         intBack=-1;
         intId=-7654681;
         intRes=-16776961;
         intCom=-16731648;
         intCad=-5111808;
         intNum=-8355712;
         intOp=-16681983;
         intInv=-65536;
         
         fontSel.select(intFont-1);
         sizeSel.select(intSize-1);
         boldTrue=cursivaTrue=false;
         boldCheck.setSelected(false);
         cursivaCheck.setSelected(false);
         bColor.setBackground(new Color(intColor));
         bBack.setBackground(new Color(intBack));
         bId.setBackground(new Color(intId));
         bRes.setBackground(new Color(intRes));
         bCom.setBackground(new Color(intCom));
         bCad.setBackground(new Color(intCad));
         bNum.setBackground(new Color(intNum));
         bOp.setBackground(new Color(intOp));
         bInv.setBackground(new Color(intInv));
      }
      if(e.getSource().equals(cancelar)){
         dispose();
      }
   }
}