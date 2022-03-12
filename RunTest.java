import java.io.*;
public class RunTest{
   public static void main(String args[]) throws Exception{
      System.out.println(" ");
      if(args.length>0){
         System.out.println("----------------------------------------------------------------");
         System.out.println("Se encontro un parametro: "+args[0]);
         System.out.println("----------------------------------------------------------------");
      }
      
      InputStreamReader isr=new InputStreamReader(System.in);
      BufferedReader br=new BufferedReader(isr);
      String tmp="";
      int num1,num2;
      System.out.println(">>> Comparacion de numeros <<<\n");
      System.out.println("Escribe un numero entero: ");
      tmp=br.readLine();
      try{
         num1=Integer.parseInt(tmp);
      }catch(NumberFormatException nfe){
         System.out.println("Lo que escribiste no era un numero entero, se va a usar el valor 0");
         Thread.currentThread().sleep(2000);
         num1=0;
      }
      
      System.out.println("Escribe otro numero entero: ");
      tmp=br.readLine();
      try{
         num2=Integer.parseInt(tmp);
      }catch(NumberFormatException nfe){
         System.out.println("Lo que escribiste no era un numero entero, se va a usar el valor 0");
         Thread.currentThread().sleep(2000);
         num2=0;
      }
      
      System.out.println(" ");
      if(num1>num2)
         System.out.println("El numero mayor es: "+num1);
      else if(num1<num2)
         System.out.println("El numero mayor es: "+num2);
      else
         System.out.println("Los numeros son iguales.");
      
      //Thread.currentThread().sleep(5000);
      System.out.println("\nPresiona <Enter> para salir.");
      br.read();
    }
}