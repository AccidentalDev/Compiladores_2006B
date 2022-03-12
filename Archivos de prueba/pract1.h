CONSTANTES [
   PI <- 3.1416
]

VARIABLES [
   ENTERO _;
   REAL r <- 1.;
   CADENA c <- ";
]

MODULOS [
   FUNCION f1 {} LOGICO
   VARIABLES [logico l <- falso]
   INICIO [
      l <- (VERDADERO & l) | !l;
      r <- 1.0E-12 / 3E99 - 9e-7 POT 3.141592e99;
      l <- (((5<=r) & (3<>r)) | (r=1));
      LEER (l);
      ESCRIBIR (!l);
      REGRESAR (r>=0);
   ] FIN;

   PROCEDIMIENTO p1 {}
   INICIO [
      r <- 1.5e DIV 1.5e- MOD ___;
      c <- "hola"@ + "mundo"?;
   ] FIN;
]

PRINCIPAL
   ESCRIBIR("ERROOOR")
