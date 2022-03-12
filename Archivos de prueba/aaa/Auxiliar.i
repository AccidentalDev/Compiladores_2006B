ARCHIVOS [
   "p3.h"
]

MODULOS [
   FUNCION f3 {} ENTERO REGRESAR(1.0);

   FUNCION f1 {ENTERO x,y,z} ENTERO
      VARIABLES [REAL x,f1,MAX,m]
      ARREGLOS [ENTERO m{4,2},n{3}]
      INICIO [
         LEER(MAX,h,e,f,f1,p1,a{1},b{1,1},c{-1},c{2},c{1,1},e{-1,-1},e{10,10},e{1});
         ESCRIBIR(h,e,f,p1,a{1},b{1,1},c{-1},c{2},c{1,1},e{-1,-1},e{10,10},e{1});
         f1();
         p5(1);
         MAX<-0;
         f1<-0;
         p5<-0;
         z<-1.0;
         z<-1+"";
         z<-5 MOD 1.0;
         z<--F;
         z<-p5();
         z<-f1;
         z<-""<5 & F;
         z<-!"";
         z<-z/0;
         z<-z DIV CERO;
         z<-z MOD CERO;
         z{}<-[1];
         n{}<-[1,2];
         n{}<-[1,2,3,4];
         n<-1;
         SI (m) REGRESAR(1)
         SINOSI ("") DTPT()
      ] FIN;

   PROCEDIMIENTO p3 {}
      INICIO [
         MIENTRAS MAX LPPT();
         HACER DTPT() HASTA 3<=""
      ] FIN;

   PROCEDIMIENTO p4 {REAL d,e}
      INICIO [
         DESDE i {1.0,""} LPPT();
         DEPENDIENDO d [
            CASO "" ESCRIBIRCSL();
            CASO F ESCRIBIRCSL()
         ]
      ] FIN
]

PRINCIPAL
INICIO [
   p4("",F);
   k<-f1(1,2,3);
   a<-f1();
   a<-f1(1,2,3,4);
   REGRESAR(1)
] FIN    
