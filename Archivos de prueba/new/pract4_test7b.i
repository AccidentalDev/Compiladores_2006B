CONSTANTES [
   PI <- 3.1416,
   MAX <- 20,
   SALUDO <- "Hola",
   V <- VERDADERO
]
VARIABLES [ 
   ENTERO f<-20,g<-h<-255;
   REAL x,y,z;
   CADENA c <- SALUDO;
   LOGICO m<-n<-o<-V
]
MODULOS[
   FUNCION F1{ENTERO x,y{2,3}} LOGICO
   INICIO[
      SI x>y{0,0}
         REGRESAR(VERDADERO);
      x<-y{1,1};
      REGRESAR(FALSO);
      x<-0
   ]FIN;
   
   FUNCION F2{ENTERO a{4},b} ENTERO
   INICIO[
      SI a{0}>b
         REGRESAR(a{0})
      SINO
         REGRESAR(b);
      REGRESAR(a{3})
   ]FIN
]

PRINCIPAL
ARREGLOS[ ENTERO a1{2,3},a2{4} ]
INICIO[
   SI F1(a1{1,1},a1)
      ESCRIBIR("El mayor es ",F2(a2,7));
   
   a1{}<-[[1,f,3],[f+y-h,5,MAX]];
   a2{}<-[h*-3,8,g+2,10]
]FIN
