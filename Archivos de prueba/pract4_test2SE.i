PRINCIPAL
VARIABLES[
   ENTERO pants,shirts,np,ns;
   REAL total,desc;
   LOGICO tarjeta
]
INICIO[
   ESCRIBIRCSL("Cuantos pantalones?");
   LEER(pants);
   ESCRIBIRCSL("Cuantas playeras?");
   LEER(shirts);
   np<-pants*300;
   ns<-shirts*180;
   total<-np+ns;
   SI(total>1000)
   INICIO[
      ESCRIBIR("Tiene tarjeta? ");
      LEER(tarjeta);
      SI tarjeta=VERDADERO
         desc<-(total*15)/100
      SINO
         desc<-(total*5)/100;
      total<-total-desc
   ]FIN;
   ESCRIBIRCSL();
   ESCRIBIR("El total es: ");
   ESCRIBIR(total)
]FIN
