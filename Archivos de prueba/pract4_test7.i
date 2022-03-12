MODULOS[
   FUNCION F1{ENTERO x,y} LOGICO
   INICIO[
      SI x>y
         REGRESAR(VERDADERO);
      x<-y;
      REGRESAR(FALSO);
      x<-0
   ]FIN;
   
   FUNCION F2{ENTERO a,b} ENTERO
      SI a>b
         REGRESAR(a)
      SINO
         REGRESAR(b)
]

PRINCIPAL
   SI F1(3,6)
      ESCRIBIR("El mayor es ",F2(9,7))
