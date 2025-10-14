package es.usal.pa.agent.modelo;

/**
 * Contine los indenticiadores que se pueden usar para mandar los mensajes entre los agentes
 * y así poder determinar el tipo de mensaje que se está mandando
 * La parte de Elena no está implementada todavía por lo que podéis ignorarlo
 * @author Fran
 *
 */
public enum TipoMensaje
{
	AITOR_TIEMPO_JUGADORES, AITOR_TURNO_DAVID_JUGADORES, 
	
	DAVID_NUMERO_JUGADORES, DAVID_VALOR_BUSCADO_JUGADORES, DAVID_EMPEZAR_CIFRAS_JUGADORES, DAVID_FINALIZAR_CIFRAS_JUGADORES,
	DAVID_GANADOR_JUGADORES_AITOR,
	
	JUGADOR_SOLUCION_DAVID,
	
	AITOR_TURNO_ELENA, ELENA_lETRA_JUGADORES, ELENA_EMPEZAR_LETRAS_JUGADORES
}
