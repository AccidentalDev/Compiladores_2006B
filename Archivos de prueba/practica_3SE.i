CONSTANTES [
   IVA <- 1.15, MAX <- 10, T <- VERDADERO
]

VARIABLES [
   ENTERO x,y;
   REAL r<- 1.3;
   CADENA c1 <- ""
]

ARREGLOS [
   REAL r1{2};
   ENTERO x2{2,400}
]

REGISTROS [
   y2[ENTERO y2,MAX;REAL x]
]

PROTOTIPOS [
   PROCEDIMIENTO p1 {ENTERO a,b; REAL c}
]

MODULOS [
   PROCEDIMIENTO p1 {ENTERO a,b; CADENA c}
      SI a>b LPPT() 
      SINOSI a=b DTPT()
      SINO ESCRIBIR("Valores incorrectos");

   FUNCION f1 {} LOGICO
   VARIABLES [LOGICO l <- FALSO ]
   INICIO [
      l <- (((5<=r) & (3<>r)) | (r=1));
      l <- (VERDADERO & l) | !l;
      LEER(x);
      ESCRIBIR(y);
      DEPENDIENDO x [
         CASO 1 INICIO [
                   ESCRIBIRCSL();
                   DTPT()
                ] FIN;
         CASO 2 INICIO [
                   ESCRIBIRCSL();
                   DTPT()
                ] FIN;
         CASO 3 | 4 INICIO [
                   ESCRIBIRCSL();
                   DTPT()
                ] FIN;
         CUALQUIER OTRO INICIO [
                           ESCRIBIRCSL();
                           DTPT()
                        ] FIN
      ];
      REGRESAR(l)
   ] FIN
]

PRINCIPAL
INICIO [
   HACER y2[y2] <- 10 HASTA x<10;
   r1{} <- [9,8];
   y2[MAX] <- 0;
   DESDE y {5,50} INC 5 LPPT()
] FIN
