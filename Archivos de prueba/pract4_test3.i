PRINCIPAL
VARIABLES[
   ENTERO p,mult,total
]
INICIO[
   ESCRIBIR("Cuantas peliculas?");
   LEER(p);
   SI p<4
      mult<-20
   SINOSI p<6
      mult<-18
   SINOSI p=6
      mult<-16
   SINO
      mult<-10;
   total<-p*mult;
   ESCRIBIR(total)
]FIN
