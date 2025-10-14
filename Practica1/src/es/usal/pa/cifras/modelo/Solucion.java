package es.usal.pa.cifras.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda la lista de operaciones que realiza el usuario para el cálculo del resultado buscado
 * @author Fran
 *
 */
public class Solucion implements Serializable
{
	/**
	 * listado con las operaciones
	 */
	protected List<Operacion> listaOperacion;

	public Solucion() 
	{
		super();
		// TODO Auto-generated constructor stub
		listaOperacion=new ArrayList<Operacion>();
	}
	
	protected Solucion(List<Operacion> listaOperacion) 
	{
		super();
		// TODO Auto-generated constructor stub
		this.listaOperacion=listaOperacion;
	}
	
	public List<Operacion> getListaOperacion() 
	{
		return listaOperacion;
	}

	public void setListaOperacion(List<Operacion> listaOperacion) 
	{
		this.listaOperacion = listaOperacion;
	}
	
	public Operacion getUltimaOperacion()
	{
		if(listaOperacion==null || listaOperacion.size()==0)
			return null;
		
		return listaOperacion.get(listaOperacion.size()-1);
	}

	public void addOpereacion(Operacion operacion)
	{
		listaOperacion.add(operacion);
	}
	
	public void removeUltimaOperacion()
	{
		if(listaOperacion.size()>0)
			listaOperacion.remove(listaOperacion.size()-1);
	}
	
	@Override
	public Solucion clone()
	{
		List<Operacion> listaOperacionClonado;		
		listaOperacionClonado=new ArrayList<Operacion>();
		
		for(int i=0; i<listaOperacion.size();i++)
			listaOperacionClonado.add(listaOperacion.get(i).clone());
		
		return new Solucion(listaOperacionClonado); 
	}
}
