import java.io.*;

public class RunTime{
   public void runShell(String ruta,String arg){
      String osName = System.getProperty("os.name"); 
      Runtime run;
      Process proc;
      try{
         if(osName.equalsIgnoreCase("WINDOWS NT") || osName.equals("Windows 2000")|| osName.equals("Windows XP")){
            run = Runtime.getRuntime();
            //run.traceInstructions(true);
            if(arg==null)
               arg="";
            //Define los comandos suponiendo que el classpath de java ya esta definido en el sistema:
            //String comandos[]={"cmd.exe","/c","start","java","RunTest",arg};
            //System.out.println("Ejecutando el proceso: RunTest en: "+osName+"\n");
            ruta=ruta.substring(0,ruta.lastIndexOf("."));
            ruta=ruta+".mvi";
            String comandos[]={"cmd.exe","/c","start","java","-jar","mvi.jar",ruta,arg};
            
            proc=run.exec(comandos);
            
            System.out.println("\nEsperando a que el proceso termine...");
            proc.waitFor();
            System.out.println("Proceso finalizado.");
         }else{
            System.out.println("ERROR: el sistema operativo no esta soportado por el codigo: "+osName+"\n");
         }
         //Thread.currentThread().sleep(1000);
      }catch(Exception e){
         System.out.println("ERROR: ");
         System.out.println(e.getMessage());
      }
   }
   
   public static void main(String[] args){
      RunTime test = new RunTime();
      test.runShell(null,null);
   }
}