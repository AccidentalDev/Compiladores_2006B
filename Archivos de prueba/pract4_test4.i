PRINCIPAL
VARIABLES[
   ENTERO a,b
]
INICIO[
   LEER(a);
   MIENTRAS(a>1 & a<=5)
   INICIO[
      b<-a;
      SI(b=2)
         MIENTRAS b<5
            b<-b+2
      SINO
         SI(b=3)
            MIENTRAS b<80
               b<-b POT 3
         SINO
            MIENTRAS b<=10
               b<-b*2
   ]FIN
]FIN
