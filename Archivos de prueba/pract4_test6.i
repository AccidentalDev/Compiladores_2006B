VARIABLES [ 
   ENTERO b<-10;
   REAL x,y,z;
   CADENA c
]
 
ARREGLOS [
   ENTERO xa{5}, ya{10,2}
]

REGISTROS [
   X [REAL a; ENTERO b,c; LOGICO d ];
   Y [CADENA a; ENTERO b] {3};
   Z [LOGICO g,h,i; REAL j,k ] {4,2}
]

PRINCIPAL
   INICIO[
      LEER(x,xa{b},ya{b,1},X[a]);
      LEER(Y[b]{xa{0}},Z[h]{2,X[b]});
      LEER(Z[k]{2,0})
   ]FIN
