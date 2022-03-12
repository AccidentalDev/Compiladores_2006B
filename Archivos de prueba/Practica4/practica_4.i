ARCHIVOS ["practica_4.h"]

MODULOS [
	PROCEDIMIENTO arreglo {}
	ARREGLOS [ENTERO a{7},b{2,6}]
	INICIO [
		ESCRIBIRCSL("Operaciones con arreglos:");
		ESCRIBIRCSL();
		a{} <- [1,1,1,2,1,2,1];
		b{} <- [[2,1,3,1,2,1],[2,1,3,1,2,1]];
		b{a{0},a{2}} <- a{a{2}-1}+a{b{1,1}}*a{a{0}+a{1}-1};
		ESCRIBIRCSL("El resultado de la operacion sobre variables dimensionadas es: ",b{1,1})
	] FIN
]

PRINCIPAL
VARIABLES [ENTERO i <- 1]
ARREGLOS [ENTERO arr{10}]
INICIO [
	arreglo();
	ESCRIBIRCSL();
	DESDE i {0,9}
        INICIO[
		ESCRIBIRCSL("Escribe el arreglo en...");
		ESCRIBIR("la posicion ", i, ": ");
		LEER(arr{i})
	]FIN;
	ESCRIBIRCSL();
	torres_h()
] FIN
