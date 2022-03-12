<!- Arreglos (metodo de burbuja) ->
ARCHIVOS [
   "c:\TC\lib\time.h",
   "c:\TC\lib\stdlib.h"
]
CONSTANTES[
   tope <- 10
]
MODULOS[
   PROCEDIMIENTO randomize{}
      LPPT();
   FUNCION RANDOM{ENTERO x} ENTERO
      REGRESAR(360 MOD x)
]
PRINCIPAL
VARIABLES[
   ENTERO _0; <!- Solo se usa para detener el codigo con leer ->
   ENTERO Z,I,N,B,TMP <!- TMP reemplaza lo que en el codigo original seria " I+1 "->
]
ARREGLOS[
   ENTERO A{ 10 }
]
INICIO[
   LPPT( );
   ESCRIBIRCSL( "¿Cuantos elementos contiene el vector? (maximo 10)" );
   LEER( N );
   randomize( );
   
   DESDE I { 0,N }
   INICIO[
      A{I} <- RANDOM( 49 ) + 1;
      ESCRIBIRCSL( I+A{I} )
   ]FIN;
   
   ESCRIBIRCSL("presiona cualquier tecla para continuar");
   LEER( _0 );
   TMP<-I+1;
   HACER
   INICIO[
      B<-0;
      DESDE I {0,N-1}
         SI A{I} > A{TMP}
         INICIO[
            Z<-A{I};
            A{I}<-A{TMP};
            A{TMP}<-Z;
            B<-1
         ]FIN
   ]FIN
   HASTA B=1;
   
   DESDE I { 0, N }
      ESCRIBIRCSL( A{I} );
   LEER( _0 )
]FIN
