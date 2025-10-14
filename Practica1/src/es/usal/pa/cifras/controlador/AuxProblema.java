package es.usal.pa.cifras.controlador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Permite generar el listado de números y el resultado a buscar
 * Si se ve el programa se puede ver que normalmente salen números en el ranto 1-10
 * y salen con menos frecuencia números altos. Los núemros altos suelen ser
 * núemros redondos como 50, 75, 25 aquí se ha complicado un poco más
 * @author Fran
 *
 */
public class AuxProblema
{
	protected static List<Integer> posiblesValores=null;
	
	static
	{
		posiblesValores=new ArrayList<Integer>();
		
		posiblesValores.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		posiblesValores.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		posiblesValores.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		posiblesValores.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		posiblesValores.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		posiblesValores.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		posiblesValores.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		posiblesValores.addAll(Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19, 20));
		posiblesValores.addAll(Arrays.asList(10, 20, 30, 40, 50, 60, 70, 80, 90, 100));
		posiblesValores.addAll(Arrays.asList(15, 25, 35, 45, 55, 65, 75, 85, 95, 100));
	}
	
	/**
	 * Devuelve la lista de números
	 * @param numeros números a calcular
	 * @return listado con los números
	 */
	public static List<Integer> calcularListaNumeros(int numeros)
	{
		List<Integer> listaNumeros=new ArrayList<Integer>();
		Random random=new Random();
		
		for(int i=0;i<numeros;i++)
			listaNumeros.add(posiblesValores.get(random.nextInt(0, 99)));
		
		return listaNumeros;
	}
	
	/**
	 * Devuelve un número en el rango indicado
	 * @param min rango mínimo
	 * @param maximo rango máximo
	 * @return número calculado
	 */
	public static Integer calcularResultado(int min, int maximo)
	{
		Random random=new Random();
		
		return random.nextInt(min, maximo);
	}
}
