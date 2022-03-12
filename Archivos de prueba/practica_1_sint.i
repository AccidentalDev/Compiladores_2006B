ARCHIVOS [
   "C:\install\a1.h",
   "a2.h"
]

CONSTANTES [
   PI <- 3.1416,
   MAX <- 20,
   SALUDO <- "Hola",
   V <- VERDADERO
]

VARIABLES [ 
   ENTERO _E;
   ENTERO b<-10;
   REAL x,y,z;
   CADENA c <- "Hello";
   LOGICO m<-n<-o<-FALSO;
   
   ENTERO e,f<-20,g<-h<-255,i
]

ARREGLOS [
   ENTERO x{3}, y{4,2};
   REAL z{2}<-[1.4, 0.6], a{3,2} <- [[1.0,2.4],[6.1,10],[5,7.1]]; 
   CADENA w{3} <- ["Hola", "Adios", "Mundo" ]
]

REGISTROS [
   X [REAL a; ENTERO b,c; LOGICO d ];
   Y [CADENA e; ENTERO f ] {3};
   Z [LOGICO g,h,i; REAL j,k ] {4,2}
]

PRINCIPAL
   DTPT( )
