PRINCIPAL
VARIABLES[
   ENTERO pants,shirts,np,ns;
   REAL total,desc;
   LOGICO tarjeta
]
INICIO[
   LEER(pants);
   LEER(shirts);
   np<-pants*300;
   ns<-shirts*180;
   total<-np+ns;
   SI(total>1000)
   INICIO[
      ESCRIBIR("Tarjeta?");
      LEER(tarjeta);
      SI tarjeta=VERDADERO
         desc<-(total*15)/100
      SINO
         desc<-(total*5)/100;
      total<-total-desc
   ]FIN;
   ESCRIBIR(total)
]FIN
