package es.usal.pa.cifras.modelo;

import java.io.Serializable;

/**
 * Contine pares de números y un operador que representa una operacion siguiendo el ejemplo
 * de como se representan los cálculos intermedios en el juego
 * @author Fran
 *
 */
public class Operacion implements Serializable
{
	/**
	 * primer valor
	 */
	protected Integer operando1;
	
	/**
	 * segundo valor
	 */
	protected Integer operando2;
	
	/**
	 * Operando que puede ser +, -, *, /
	 */
	protected Character operador;
	
	public Operacion(Integer operando1, Integer operando2, Character operador) 
	{
		super();
		this.operando1 = operando1;
		this.operando2 = operando2;
		this.operador = operador;
	}

	public Integer getOperando1() 
	{
		return operando1;
	}

	public void setOperando1(Integer operando1) 
	{
		this.operando1 = operando1;
	}

	public Integer getOperando2() 
	{
		return operando2;
	}

	public void setOperando2(Integer operando2) 
	{
		this.operando2 = operando2;
	}

	public Character getOperador() 
	{
		return operador;
	}

	public void setOperador(Character operador) 
	{
		this.operador = operador;
	}
	
	
	@Override
	public Operacion clone()
	{
		Operacion operacionClonado=new Operacion(operando1, operando2, operador);
	
		return operacionClonado;
	}
	
}
