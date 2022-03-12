PRINCIPAL
VARIABLES[
   ENTERO x
]
   DEPENDIENDO x[
      CASO 1
         ESCRIBIR("Uno");
      CASO 2
      INICIO[
         LPPT();
         ESCRIBIR("Dos");
         DTPT()
      ]FIN;
      CASO 3|4|5
      INICIO[
         ESCRIBIR("Opcion invalida");
         DTPT()
      ]FIN;
      CASO 6
         ESCRIBIR("Seis");
      CUALQUIER OTRO
         DTPT()
   ]
