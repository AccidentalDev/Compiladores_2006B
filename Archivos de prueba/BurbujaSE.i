<!- Arreglos (metodo de burbuja) ->
CONSTANTES[
   tope <- 10
]
PRINCIPAL
VARIABLES[
   ENTERO tmp;<!- Para leer y luego ponerlo en los dos arreglos ->
   ENTERO Z,I,N,B
]
ARREGLOS[
   ENTERO A{ tope }, T{ tope }
]
INICIO[
   LPPT( );
   ESCRIBIRCSL( "** Ordenamiento de arreglos con el metodo de Burbuja **" );
   ESCRIBIRCSL();
   ESCRIBIRCSL("Paso 1: Introducir cada elemento en el arreglo con dimension ",tope,".");
   N<-tope;
   DESDE I { 0,N-1 }
   INICIO[
      ESCRIBIR("Escribe el elemento # ",I+1,": ");
      LEER(tmp);
      A{I}<-tmp;
      T{I}<-tmp
   ]FIN;
   
   ESCRIBIRCSL();
   ESCRIBIRCSL("presiona <Enter> para continuar");
   DTPT();
   
   ESCRIBIRCSL();
   ESCRIBIRCSL("Ordenando el arreglo...");
   HACER
   INICIO[
      B<-0;
      DESDE I {0,N-2}
         SI A{I} > A{I+1}
         INICIO[
            Z<-A{I};
            A{I}<-A{I+1};
            A{I+1}<-Z;
            B<-1<!-La bandera es para cuando ya no se hacen modificaciones en el arreglo->
         ]FIN
   ]FIN
   HASTA B=0;
   
   ESCRIBIRCSL();
   ESCRIBIRCSL("Original - Ordenado");
   DESDE I { 0, N-1 }
      ESCRIBIRCSL("   ",T{I},"        ",A{I})
   <!-
   ESCRIBIRCSL("presiona <Enter> para salir...");
   DTPT()->
]FIN
