package es.usal.pa.cifras.controlador;

import java.util.ArrayList;
import java.util.List;

import es.usal.pa.cifras.modelo.Solucion;

/**
 * Permite operarar con soluciones propuestas.
 * @author Fran
 *
 */
public class AuxSolucion
{
	
	/**
	 * Devuelve una cadena para imprimir la solución propuesta
	 * @param solucion a imprimir
	 * @return cadena con la solución para imprimir
	 */
	public static String cadenaOperaciones(Solucion solucion)
	{
		String temp="";
		for(int i=0;i<solucion.getListaOperacion().size();i++)
			temp+=""+solucion.getListaOperacion().get(i).getOperando1()+solucion.getListaOperacion().get(i).getOperador()+solucion.getListaOperacion().get(i).getOperando2()+"="+AuxOperacion.calcularOperacion(solucion.getListaOperacion().get(i))+"\n";
		
		return temp;
	}
	
	/**
	 * Permite determinar el resultado que viene indciado en la solución pasada como argumento. Comprueba
	 * además que los los operandos propuestos en la solución son correctos y están en la lista de números
	 * propuestos o son cálculos intermedios
	 * @param solucion solución propuesta por el jugaodr
	 * @param listNumeros núemros que se pueden usar para el cálculo de la solución
	 * @param resultadoBuscado número a calcular
	 * @return resultado que se ha conseguido en la solucíon propuesta
	 */
	public static Integer calcularSolucion(Solucion solucion, List<Integer> listNumeros, Integer resultadoBuscado)
	{
		Boolean valida=true;
		Integer resultado=null, resultadoTemp=null;
		List<Integer> listaEnteros=new ArrayList<Integer>();
		
		listaEnteros.addAll(listNumeros);
		
		for(int i=0;i<solucion.getListaOperacion().size();i++)
		{
			resultadoTemp=AuxOperacion.calcularOperacion(solucion.getListaOperacion().get(i));
			
			if(resultadoTemp==null)
				return null;
			
			listaEnteros.add(resultadoTemp);
			if(listaEnteros.contains(solucion.getListaOperacion().get(i).getOperando1()))
				listaEnteros.remove(solucion.getListaOperacion().get(i).getOperando1());
			else
				return null;
			
			if(listaEnteros.contains(solucion.getListaOperacion().get(i).getOperando2()))
				listaEnteros.remove(solucion.getListaOperacion().get(i).getOperando2());
			else
				return null;
		}
		
		resultado=valorMasCercanoBuscado(listaEnteros, resultadoBuscado);
		
		return resultado;
	}
	
	/**
	 * Devuelve el resultado más cercano a la solución dentro de los cálculos realizados por el usuario
	 * @param lista listado con los números calculados al operar con la solución
	 * @param resultadoBuscado valor buscado
	 * @return devuelve el valor más cercano al resuultado buscado dentro de la lista
	 */
	protected static Integer valorMasCercanoBuscado(List<Integer> lista, Integer resultadoBuscado)
	{
		Integer temp=null;
		for(int i=0;i<lista.size();i++)
			if(temp==null || Math.abs(resultadoBuscado-lista.get(i))<Math.abs(resultadoBuscado-temp))
				temp=lista.get(i);
		
		return temp;
	}
}
