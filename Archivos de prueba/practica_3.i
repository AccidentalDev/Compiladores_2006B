CONSTANTES [
   IVA <- 1.15, MAX <- 10, T <- VERDADERO, MAX <- 3
]

VARIABLES [
   ENTERO x,y;
   REAL r<- 1.3,x,MAX;
   CADENA c1 <- c1 <- ""
]

ARREGLOS [
   ENTERO x{2},y{2,4};
   REAL r1{1};
   ENTERO x2{0,40000}
]

REGISTROS [
 y2[ENTERO y2,MAX;REAL x,y2]
]


PROTOTIPOS [
   FUNCION p1 {ENTERO b,a; REAL c};
   PROCEDIMIENTO p3 {}
]

MODULOS [
   PROCEDIMIENTO p1 {ENTERO a,b; CADENA c}
   INICIO [
      SI a+b LPPT() 
      SINOSI a-b DTPT()
      SINO ESCRIBIR("Valores incorrectos")
      REGRESAR(0)
   ] FIN;

   FUNCION f1 {} LOGICO
   VARIABLES [LOGICO l <- FALSO; REAL l,MAX]
   INICIO [
      l <- (((5<=r) & (3<>r)) | (r=1));
      l <- (VERDADERO & l) | !l;
      l <- p1(1,2,"");
      f1();
      LEER(m,f1);
      ESCRIBIR(m,p1);
      DEPENDIENDO a [
         CASO 1.2 INICIO [
                   ESCRIBIRCSL();
                   DTPT()
                ] FIN;
         CASO "" INICIO [
                   ESCRIBIRCSL();
                   DTPT()
                ] FIN;
         CASO 3 | FALSO INICIO [
                   ESCRIBIRCSL();
                   DTPT()
                ] FIN;
         CUALQUIER OTRO INICIO [
                           ESCRIBIRCSL();
                           DTPT()
                        ] FIN
      ];
      REGRESAR(10)
   ] FIN
]

PRINCIPAL
INICIO [
   HACER y2[y3]{0,0} <- 10 HASTA x;
   x1{} <- [9,8,""];
   y1{} <- [[5,4,3,2,1],[1,0,9]];
   y2[y4]{x1{""},y1{"",1.2}} <- 0;
   DESDE j {5.0,""} INC 5 LPPT()
] FIN
