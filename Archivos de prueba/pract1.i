CONSTANTES [
   PI <- 3.1416
]

VARIABLES [
   ENTERO _e;
   REAL r <- 1.0;
   CADENA c <- ""
]

MODULOS [
   FUNCION f1 {} LOGICO
   VARIABLES [LOGICO l <- FALSO]
   INICIO [
      l <- (VERDADERO & l) | !l;
      r <- 1.0E-12 / 3E99 - 9e-7 POT 3.141592e99;
      l <- (((5<=r) & (3<>r)) | (r=1));
      LEER (l);
      ESCRIBIR (l);
      REGRESAR (r>=0)
   ] FIN;

   PROCEDIMIENTO p1 {}
   INICIO [
      r <- 1.5e2 DIV 1.5e-2 MOD _e;
      c <- "hola" + "mundo"
   ] FIN
]

PRINCIPAL
   DTPT()
