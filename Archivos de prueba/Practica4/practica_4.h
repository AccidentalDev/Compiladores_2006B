VARIABLES [ENTERO movs]

PROTOTIPOS [
   PROCEDIMIENTO mover_disco {ENTERO n; CADENA pinicial,pfinal};
   PROCEDIMIENTO mover_torre {ENTERO n; CADENA pinicial,pfinal,paux; ENTERO dsup}
]

MODULOS [
   PROCEDIMIENTO torres_h {}
   VARIABLES [ENTERO numero_discos; CADENA poste_inicial,poste_final,poste_aux]
   INICIO [
     ESCRIBIRCSL("Algoritmo de las Torres de Hanoi.");
     movs <- 0;
     poste_inicial <- "a";
     poste_final <- "c";
     poste_aux <- "b";
     ESCRIBIR("Introduce el numero de discos: ");
     LEER(numero_discos);
     ESCRIBIRCSL();
     mover_torre(numero_discos,poste_inicial,poste_final,poste_aux,1);
     ESCRIBIRCSL("Se necesitaron ",movs," movimientos.");
     DTPT()
   ] FIN;

   PROCEDIMIENTO mover_disco {ENTERO n; CADENA pinicial,pfinal}
   INICIO [
     ESCRIBIRCSL("Se mueve el disco ",n," desde el poste ",pinicial," hasta el poste ",pfinal);
     movs <- movs+1
   ] FIN;

   PROCEDIMIENTO mover_torre {ENTERO n; CADENA pinicial,pfinal,paux; ENTERO dsup}
      SI(n <> dsup)
      INICIO[
         mover_torre(n-1,pinicial,paux,pfinal,1);
         mover_torre(n,pinicial,pfinal,paux,n);
         mover_torre(n-1,paux,pfinal,pinicial,1)
      ]FIN 
      SINO mover_disco(dsup,pinicial,pfinal)

   ]
