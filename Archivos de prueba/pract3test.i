ARCHIVOS[
   "a1.h"
]
CONSTANTES[
   INI<-0
]
VARIABLES[
   ENTERO x<-640,y<-480
]
ARREGLOS[
   LOGICO l{6,3};
   CADENA c{3}<-["hola","mundo","extraño"]
]
REGISTROS[
   r[ENTERO a;REAL b];
   s[ENTERO a,b,c]{5}
]
PRINCIPAL
INICIO[
   ESCRIBIR(c{INI}," ",c{1});
   LEER(s[a]{INI})
]FIN
