ARCHIVOS [
   "1";
   "1.h"
]

CONSTANTES [
   IVA 1.15, MAX <- 10 T <- 
]

VARIABLES [
   ENTER x,y;
   REALl r 1.3;
   CADENA c1 <- c2 <- c3 <- 
]

ARREGLOS [
   ENTERO x1{},y1{2 4};
   REAL r1{3} <- [1,2,3;
   ENTERO x2{2,3} <- [[1,2],3,4]]
]

REGISTROS [
 y2[ENTERO y3 y4; REL y5]{4,}
]

MODULOS [
   PROCEDIMENTO p1 ENTERO a b; CADENA c}
   INICIO [
      ESCRIBIRCSL();
      SI a b LPPT() 
      SINOSI a<b DTPT(
      SINOSI a=b LEER(c a,b)
      SINO ESCRIBIR()
   ] FIN

   FUNCION f1 {}
   VARIABLES [LOGICO l FALSO]
   INICIO [
      l <- (((5 r) &  3<>r)) | (r=1);
      l <- (VERDADERO  l) | !l
      l <- f1();
      r <- 1.0E-12   3E99 - 9e-7 POT;
      LEER();
      ESCRIBIR(!l);
      DEPENDIENDO x [
         CASO 1 
         CASO 2
                   ESCRIBIRCSL();
                   DTPT()
         CASO 3 | 4 | INICIO [
                   ESCRIBIRCSL();
                   DTPT()
         CASO 6 INICIO [
                   ESCRIBIRCSL();
                   DTPT()
                ] FIN;
         CUALQUIER INICIO [
                      ESCRIBIRCSL();
                      DTPT()
                   ] FIN
      ];
      REGRESAR()
   ] FIN
]

PRINCIPAL
INICIO [
   HACER y2[]{0,} <- 10 HASTA;
   x1{ <- [9,8,7,6];
   y1{} <- [[5,4,3,2,[1 0,9,8]];
   y2[y4{x1{},y1{x+,x-y}} <-;
   p1(,a+b "X");
   DESDE j {5 10 INC 5 LPPT();
] FIN;
