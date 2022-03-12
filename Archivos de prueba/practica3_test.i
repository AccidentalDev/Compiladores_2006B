ARCHIVOS [
   "C:\install\a1.h"
]
CONSTANTES [
   PI2 <- 3.14159283,
   MAX <- 20,
   SALUDO <- "Hola",
   V <- VERDADERO
]
VARIABLES [ 
   ENTERO _A;
   ENTERO b<-10;
   REAL x,y,z;
   CADENA c <- NOMBRE;
   LOGICO m<-n2<-o<-F;
   
   ENTERO e,f<-20,g<-h<-255,i
]
 
ARREGLOS [
   ENTERO xa{5}, ya{MAX,MIN};
   REAL za{MIN}<-[1.4,PI], a{3,2} <- [[1.0,PI2],[6.1,MAX],[5,7.1]]; 
   CADENA w{3} <- [SALUDO,"Adios","Mundo"]
]

REGISTROS [
   X [REAL a; ENTERO b,c; LOGICO d ];
   Y [CADENA a; ENTERO b] {5};
   Z [LOGICO g,h,i; REAL j,k ] {4,MIN}
]

PROTOTIPOS [
   <!-FUNCION f1{} LOGICO;->
   FUNCION f2{ENTERO x{5}; REAL y,z } ENTERO;
   <!-PROCEDIMIENTO p1{ };->
   PROCEDIMIENTO p2{ CADENA c1; LOGICO l }
]

MODULOS [
   PROCEDIMIENTO p2{CADENA c1; LOGICO l}
      INICIO[
         i<-f2(Y[b],x,y);
         SI l=VERDADERO
            ESCRIBIRCSL(SALUDO,c1)
      ]FIN;
   FUNCION f2{ENTERO x{5}; REAL y,z } ENTERO
      VARIABLES[
         LOGICO n<-o<-F;
         REAL r1,r2<-PI
      ]
      ARREGLOS[
         ENTERO ar1{5,MIN}
      ]
      REGISTROS [
         X [REAL a; ENTERO b,c; LOGICO d ];
         Y [LOGICO g,h,i; REAL j,k ] {9,3}
      ]
      INICIO[
         ar1{}<-[[34,65],[MIN,86],[12,24],[64,16],[256,1024]];
         DEPENDIENDO e [
            CASO 1| 3 | 5
               LEER(a{xa{f},1});
            CASO 2 | 4 | 6
               LEER( Y[h]{b,1},x{4} )
         ];
         DTPT( );
         REGRESAR(ar1{3,0})
      ]FIN
   <!-
   FUNCION F3{} LOGICO
      DTPT() ->
]

PRINCIPAL
   INICIO[
      LPPT( );
      SI X[b]<Y[b]{2}
      INICIO[
         X[c]<-X[b]+10+-ya{0,1};
         xa{3}<-f2(xa,za{1},Z[j]{b,1});
         a{2,1}<-ya{xa{e},f2(xa,y,z)};
         ESCRIBIR(X[c])
      ]FIN
      SINO
      INICIO[
         Z[j]{2,1}<-y+10;
         <!-p2(f2(xa,x,y),V);->
         p2(Y[a]{1},o);
         ESCRIBIRCSL(f2(xa,y,PI),xa{e})
      ]FIN;
      DTPT( )
   ]FIN
