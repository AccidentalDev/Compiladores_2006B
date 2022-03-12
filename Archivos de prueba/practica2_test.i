ARCHIVOS [
   "C:\install\a1.h",
   "pract1.h",
   "a2.h"
]

CONSTANTES [
   PI <- 3.1416,
   MAX <- 20,
   SALUDO <- "Hola",
   V <- VERDADERO
]
VARIABLES [ 
   ENTERO _A;
   ENTERO b<-10;
   REAL x,y,z;
   CADENA c <- "Hello";
   LOGICO m<-n<-o<-FALSO;
   
   ENTERO e,f<-20,g<-h<-255,i
]

ARREGLOS [
   ENTERO x{3}, y{4,2};
   REAL z{2}<-[1.4,0.6], a{3,2} <- [[1.0,2.4,[6.1,10],[5,7.1]]; 
   CADENA w{3} <- ["Hola", "Adios", "Mundo" ]
]

REGISTROS [
   X [REAL a; ENTERO b,c; LOGICO d ];
   Y [CADENA e; ENTERO f ] {3};
   Z [LOGICO g,h,i; REAL j,k ] {4,2}
]

PROTOTIPOS [
   FUNCION f1{} LOGICO;
   FUNCION f2{ENTERO x{5}; REAL y,z } ENTERO;
   PROCEDIMIENTO p1{ };
   PROCEDIMIENTO p2{ CADENA c1 }
]

MODULOS [
   FUNCION F3{ } LOGICO
      INICIO[
         m{}<-[34,65,2,87,12];
         DEPENDIENDO N [
            CASO 1| 3 | 5
               LEER(a);
            CASO 2 | 4 | 6
               LEER( n[e] )
         ];
         DTPT( )
      ]FIN
]

PRINCIPAL
   INICIO[
      LPPT( );
      SI X<Y
      INICIO[
         X<-X+10;
         ESCRIBIR(X)
      ]FIN
      SINO
      INICIO[
         Y<-Y+10;
         ESCRIBIRCSL(Y)
      ]FIN;

      DTPT( )
   ]FIN
