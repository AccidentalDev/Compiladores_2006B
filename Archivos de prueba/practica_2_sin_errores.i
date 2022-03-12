<!-ARCHIVOS [
   "1.h",
   "2.h"
]->

CONSTANTES [
   IVA <- 1.15, MAX <- 10, T <- VERDADERO
]

VARIABLES [
   ENTERO x,y;
   REAL r <- 1.3;
   CADENA c1 <- c2 <- c3 <- ""
]

ARREGLOS [
   ENTERO x1{4},y1{2,4};
   REAL r1{3} <- [1,2,3];
   ENTERO x2{2,3} <- [[1,2,3],[4,5,6]]
]

REGISTROS [
 y2[ENTERO y3,y4; REAL y5]{4,5}
]

MODULOS [
   PROCEDIMIENTO p1 {ENTERO a,b; CADENA c}
   INICIO [
      ESCRIBIRCSL();
      SI a>=b LPPT() 
      SINOSI a<b DTPT()
      SINOSI a=b LEER(c,a,b)
      SINO ESCRIBIR("Valores incorrectos")
   ] FIN;

   FUNCION f1 {} LOGICO
   VARIABLES [LOGICO l <- FALSO]
   INICIO [
      l <- (((5<=r) & (3<>r)) | (r=1));
      l <- (VERDADERO & l) | !l;
      l <- f1();
      r <- 1.0E-12 / 3E99 - 9e-7 POT 3.141592e99;
      LEER(l);
      ESCRIBIR(!l);
      DEPENDIENDO x [
         CASO 1 INICIO [
                   ESCRIBIRCSL();
                   ESCRIBIR(8,FALSO,3.5,VERDADERO,"Ya");
                   DTPT()
                ] FIN;
         CASO 2 INICIO [
                   ESCRIBIRCSL();
                   DTPT()
                ] FIN;
         CASO 3 | 4 | 5 INICIO [
                   ESCRIBIRCSL();
                   DTPT()
                ] FIN;
         CASO 6 INICIO [
                   ESCRIBIRCSL();
                   DTPT()
                ] FIN;
         CUALQUIER OTRO INICIO [
                           ESCRIBIRCSL();
                           DTPT()
                        ] FIN
      ];
      REGRESAR(r>=0)
   ] FIN
]

PRINCIPAL
INICIO [
   HACER y2[y3]{0,0} <- 10 HASTA x=y;
   x1{} <- [9,8,7,6];
   y1{} <- [[5,4,3,2],[1,0,9,8]];
   y2[y4]{x1{1},y1{x+y,x-y}} <- 0;
   p1(5,r+y,"X");
   DESDE x {5,10} INC 5 LPPT()
] FIN
