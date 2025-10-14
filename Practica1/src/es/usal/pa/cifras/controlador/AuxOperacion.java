package es.usal.pa.cifras.controlador;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.usal.pa.cifras.modelo.Operacion;

/**
 * Calcula el resultado de la operación pasada como argmento
 * @param operacion operación a realizar que contiene los operandos y el operador
 * @return devuelve el resultado. Devolverá null en caso de error y se deberá de interprentar como una operación incorrecta
 */
public class AuxOperacion 
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	/**
	 * Calcula el resultado de la operación pasada como argmento
	 * @param operacion operación a realizar que contiene los operandos y el operador
	 * @return devuelve el resultado
	 */
	public static Integer calcularOperacion(Operacion operacion) 
	{
		switch(operacion.getOperador())
		{
			case '+': return operacion.getOperando1()+operacion.getOperando2();
			case '-': return operacion.getOperando1()-operacion.getOperando2();
			case '*': return operacion.getOperando1()*operacion.getOperando2();
			case '/': 
						if(operacion.getOperando1()%operacion.getOperando2()==0)				
							return operacion.getOperando1()/operacion.getOperando2();
						return null;
		}
		
		if(operacion.getOperador()==null)
			return operacion.getOperando1();
		
		LOGGER.warn("Operación desconocida '{}'",operacion.getOperador());
		
		return null;
	}

}
