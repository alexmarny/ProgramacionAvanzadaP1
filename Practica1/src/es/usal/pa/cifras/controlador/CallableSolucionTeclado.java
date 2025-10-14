package es.usal.pa.cifras.controlador;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import es.usal.pa.cifras.modelo.Operacion;
import es.usal.pa.cifras.modelo.Solucion;

/**
 * Permite crear una lectura con teclado con timeout. En el método main tenéis un ejemplo
 * de cómo usarlo
 * @author Fran
 *
 */
public class CallableSolucionTeclado implements Callable<Solucion>
{
	/**
	 * Números que se pueden usar para el cálculo del número
	 */
	protected List<Integer> listNumeros;
	/**
	 * resultado a buscar
	 */
	protected Integer resultadoBuscado;
	/**
	 * solución calculada
	 */
	protected Solucion solucion;

	public CallableSolucionTeclado(List<Integer> listNumeros, Integer resultadoBuscado)
	{
		this.listNumeros=listNumeros;
		this.resultadoBuscado=resultadoBuscado;
		this.solucion=new Solucion();
	}
	
	@Override
	public Solucion call() throws Exception
	{
		// TODO Auto-generated method stub
		Scanner scanner=new Scanner(System.in);
		Scanner scanerParser;
		Integer operando1, operando2;
		Character operador;
		Operacion operacionTemp;
		String cadena;
		List<Integer> listNumerosTemp=new ArrayList<Integer>();
		Integer tempOpereacion;
		
		listNumerosTemp.addAll(listNumeros);
		
		System.out.println("opciones "
				+ "\nOperando1 operador Operando 2 "
				+ "\nE) para enviar y finalizar"
				+ "\nR) reiniciar");
		
		while(true)
		{
			if(Thread.currentThread().isInterrupted()) 
				return null;
			
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader in = new BufferedReader(isr);

			//no hay opción para poner un timeout en la clase scanner con system.in
			//lo intentamos arreglar así para evitar que el hilo se pueda quedar
			//ejecutando de por vida si no pulsamos una tecla
			while(!in.ready())
			{
				if(Thread.currentThread().isInterrupted()) 
					return null;
				
				Thread.sleep(100);
			}
			
			cadena=scanner.nextLine();
			
			
			if(cadena.equals(""))
				continue;
			else if(cadena.equalsIgnoreCase("E"))
				break;
			else if(cadena.equalsIgnoreCase("R"))
			{
				listNumerosTemp.clear();
				listNumerosTemp.addAll(listNumeros);
				this.solucion=new Solucion();
				continue;
			}
			
			
			try {
				scanerParser=new Scanner(cadena);
				scanerParser.useDelimiter("[\\+|\\-|\\*|\\/]");
				operando1=scanerParser.nextInt();
				operador=scanerParser.findInLine("[\\+|\\-|\\*|\\/]").charAt(0);
				operando2=scanerParser.nextInt();
				
				if(!listNumerosTemp.contains(operando1))
				{
					System.out.println("Operando "+operando1+" no existente");
					continue;
				}
				else if(!listNumerosTemp.contains(operando2))
				{
					System.out.println("Operando "+operando2+" no existente");
					continue;				
				}
				
				operacionTemp=new Operacion(operando1, operando2, operador);
				solucion.addOpereacion(operacionTemp);
				tempOpereacion=AuxOperacion.calcularOperacion(operacionTemp);
				
				if(tempOpereacion!=null)
				{
					System.out.println(tempOpereacion);
					listNumerosTemp.remove(operando1);
					listNumerosTemp.remove(operando2);
					listNumerosTemp.add(tempOpereacion);
				}
					
				scanerParser.close();
			}
			catch(Exception e)
			{
				System.out.println("Error, línea ignorada "+e.getMessage());
				//e.printStackTrace();
				continue;
			}
		}
    	
		return solucion;
	}
	
	/**
	 * En caso de no superar el tiempo y por tanto no se haya 
	 * devuelto un valor se puede llamar a este método para recuperar la última solución calculada 
	 * @return
	 */
	public Solucion getMejorResultadoCalculado()
	{
		return solucion;
	}
	
	public static void main(String args[]) throws Exception
	{
		//https://unpocodejava.com/2010/08/02/threads-devolver-valores-desde-un-hilo/
		//CallableSolucionTeclado callableSolucionTeclado=new CallableSolucionTeclado(Arrays.asList(1, 2, 3, 4, 5, 5), 20);
		CallableSolucionTeclado callableSolucionTeclado=new CallableSolucionTeclado(Arrays.asList(2, 20, 2, 1, 9, 10), 589);
		FutureTask<Solucion> task = new FutureTask<Solucion> (callableSolucionTeclado);
		ExecutorService executorService = Executors.newSingleThreadExecutor ();
		executorService.submit(task);
		
		Solucion solucion=null;
		try
		{
			//dejo como máximo 45 segundos para introducir operaciones
    		solucion = task.get(45, TimeUnit.SECONDS);
    		
    		//cuando no salta el timeout
        	System.out.println("Solución registrada");
        	System.out.println(AuxSolucion.cadenaOperaciones(solucion));
    		
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e)
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
			//https://www.geekyhacker.com/callable-with-the-timeout-in-java-executorservice/
			//añadir comprobación en el hilo para salirse
			//if(Thread.currentThread().isInterrupted()) return;
			task.cancel(true);
		}
		
		
		executorService.shutdown();
		try
		{
			executorService.awaitTermination(500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(!executorService.isTerminated())
			executorService.shutdownNow();		
		
		if(!executorService.isShutdown())
			executorService.shutdownNow();
		
		
		//cuando salta el timeout me quedo por donde iba
		if(task.isCancelled())
		{
    		solucion=callableSolucionTeclado.getMejorResultadoCalculado();
    		
        	System.out.println("Solución timeout");
        	System.out.println(AuxSolucion.cadenaOperaciones(solucion));
		}
	}

}
