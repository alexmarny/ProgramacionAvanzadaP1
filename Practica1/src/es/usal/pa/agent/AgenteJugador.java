package es.usal.pa.agent;

import java.util.ArrayList;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Agente jugador que participa en el juego de cifras y letras.
 * 
 * <p>Este agente representa a un jugador individual que interactúa con otros agentes
 * del sistema (Aitor y David) para participar en rondas del juego de cifras.
 * El agente recibe números, un objetivo, y debe resolver el problema de cifras.</p>
 * 
 * <h3>Protocolo de mensajes:</h3>
 * <ul>
 *   <li><b>AITOR_TIEMPO_JUGADORES_X</b>: Mensaje de cuenta atrás enviado por Aitor (X = tiempo restante)</li>
 *   <li><b>AITOR_TURNO_DAVID_JUGADORES</b>: Notificación de que es el turno de David</li>
 *   <li><b>DAVID_NUMERO_JUGADORES_X</b>: Número enviado por David (X = valor del número, 6 números en total)</li>
 *   <li><b>DAVID_VALOR_BUSCADO_JUGADORES_X</b>: Objetivo numérico a alcanzar (X = valor objetivo)</li>
 *   <li><b>DAVID_EMPEZAR_CIFRAS_JUGADORES</b>: Señal de inicio de la ronda de cifras</li>
 * </ul>
 * 
 * <h3>Flujo de interacción:</h3>
 * <ol>
 *   <li>Espera la cuenta atrás de Aitor (15 a 0 segundos)</li>
 *   <li>Recibe notificación del turno de David</li>
 *   <li>Recibe 6 números de David</li>
 *   <li>Recibe el objetivo a alcanzar de David</li>
 *   <li>Espera la señal de inicio para comenzar a resolver el problema</li>
 * </ol>
 * 
 * @author Sistema Multi-Agente - Cifras y Letras
 */
@SuppressWarnings("serial")
public class AgenteJugador extends Agent {
	
	 private int objetivo;
	 private ArrayList<Integer> numeros = new ArrayList<>();

    /**
     * Inicializa el agente jugador y establece el comportamiento inicial.
     * 
     * <p>El agente comienza en estado de espera de la cuenta atrás enviada por el agente Aitor.
     * Una vez iniciado, el agente está listo para recibir mensajes y participar en el juego.</p>
     */
    @Override
    protected void setup() {
        System.out.println(getLocalName() + " conectado. Esperando cuenta atrás...");
        addBehaviour(new EsperarCuentaAtras());
    }

    /**
     * Comportamiento cíclico que espera y procesa los mensajes de cuenta atrás enviados por Aitor.
     * 
     * <p>Este comportamiento recibe mensajes con formato "AITOR_TIEMPO_JUGADORES_X" donde X es el
     * tiempo restante en la cuenta atrás. Cuando la cuenta llega a 0, el comportamiento se termina
     * y se inicia el comportamiento de espera del turno de David.</p>
     * 
     * <p>Mensajes recibidos:</p>
     * <ul>
     *   <li>AITOR_TIEMPO_JUGADORES_X: Actualización del tiempo restante</li>
     * </ul>
     * 
     * <p>Transición: Cuando el tiempo llega a 0, pasa a {@link EsperarCifrasDeDavid}</p>
     */
    private class EsperarCuentaAtras extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();

            if (msg != null) {
                String contenido = msg.getContent();

                if (contenido.startsWith("AITOR_TIEMPO_JUGADORES")) {
                    String tiempo = contenido.substring(contenido.lastIndexOf("S") + 1);
                    System.out.println(getLocalName() + " -> Tiempo restante: " + tiempo);

                    // Si llega a 0, pasamos a esperar el mensaje del turno
                    if (tiempo.equals("0")) {
                        System.out.println(getLocalName() + " -> Fin cuenta atrás. Esperando turno de David...");
                        myAgent.addBehaviour(new EsperarCifrasDeDavid());
                        myAgent.removeBehaviour(this);
                    }
                }
                // Cualquier otro mensaje se ignora
            } else {
                block();
            }
        }
    }

    /**
     * Comportamiento cíclico que espera la notificación del turno de David.
     * 
     * <p>Una vez que la cuenta atrás termina, este comportamiento espera el mensaje
     * "AITOR_TURNO_DAVID_JUGADORES" que indica que David va a comenzar a enviar las cifras
     * para la ronda actual. Al recibir este mensaje, se inicia el comportamiento de
     * recepción de cifras.</p>
     * 
     * <p>Mensajes esperados:</p>
     * <ul>
     *   <li>AITOR_TURNO_DAVID_JUGADORES: Señal de inicio del turno de David</li>
     * </ul>
     * 
     * <p>Transición: Al recibir el mensaje, pasa a {@link RecibirCifras}</p>
     */
    private class EsperarCifrasDeDavid extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
            	
            	String contenido = msg.getContent();
                if (contenido.equals("AITOR_TURNO_DAVID_JUGADORES")) {
                System.out.println(getLocalName() + " -> mensaje recibido, comienza ronda de cifras: " + msg.getContent());
                addBehaviour(new RecibirCifras());
                removeBehaviour(this);
            } else {
                block();
                }
            }
        }
    }


    /**
     * Comportamiento que recibe los 6 números enviados por David para la ronda de cifras.
     * 
     * <p>Este comportamiento procesa mensajes con formato "DAVID_NUMERO_JUGADORES_X" donde X
     * es el valor del número. Continúa recibiendo números hasta completar los 6 necesarios,
     * almacenándolos en una lista para su posterior uso en la resolución del problema.</p>
     * 
     * <p>Mensajes procesados:</p>
     * <ul>
     *   <li>DAVID_NUMERO_JUGADORES_X: Número individual (X = valor del número)</li>
     * </ul>
     * 
     * <p>Condición de finalización: Cuando se han recibido 6 números</p>
     * <p>Transición: Al completar la recepción de 6 números, pasa a {@link EsperarObjetivoDeDavid}</p>
     */
    private class RecibirCifras extends Behaviour {

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();

            if (msg != null) {
                String contenido = msg.getContent();

                if (contenido.startsWith("DAVID_NUMERO_JUGADORES_")) {
                    String valor = contenido.substring(contenido.lastIndexOf("_") + 1);
                    try {
                        int numero = Integer.parseInt(valor);
                        numeros.add(numero);
                        System.out.println(getLocalName() + " -> Recibido número: " + numero + " (" + numeros.size() + "/6)");
                    } catch (NumberFormatException e) {
                        // Ignorar mensajes corruptos
                    }
                }
            }
        }

		@Override
		public boolean done() {return numeros.size() == 6;}
		
        @Override
		public int onEnd() {
            System.out.println(getLocalName() + " -> Se recibieron los 6 números.");
            myAgent.addBehaviour(new EsperarObjetivoDeDavid());
            return 0;
        }
    }
    
    /**
     * Comportamiento que espera y recibe el número objetivo de David.
     * 
     * <p>Una vez recibidos los 6 números, este comportamiento espera el mensaje de David
     * con el valor objetivo que el jugador debe intentar alcanzar utilizando los números
     * proporcionados y las operaciones aritméticas básicas.</p>
     * 
     * <p>Mensajes esperados:</p>
     * <ul>
     *   <li>DAVID_VALOR_BUSCADO_JUGADORES_X: Objetivo numérico (X = valor a alcanzar)</li>
     * </ul>
     * 
     * <p>Condición de finalización: Cuando se recibe el mensaje con el objetivo</p>
     * <p>Transición: Al recibir el objetivo, pasa a {@link EsperarInicioDeRonda}</p>
     */
    private class EsperarObjetivoDeDavid extends Behaviour {

        private boolean done = false;

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (msg.getContent().startsWith("DAVID_VALOR_BUSCADO_JUGADORES_")) {
                    objetivo = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf("_") + 1));
                    System.out.println(getLocalName() + " -> Objetivo recibido: " + objetivo);
                    done = true;
                }
            } else block();
        }

        @Override
        public boolean done() { return done; }

        @Override
        public int onEnd() {
            System.out.println(getLocalName() + " -> Esperando inicio de ronda...");
            myAgent.addBehaviour(new EsperarInicioDeRonda());
            return 0;
        }
    }
    /**
     * Comportamiento que espera la señal de inicio de la ronda de cifras.
     * 
     * <p>Este es el último paso de la preparación del juego. Una vez que el jugador tiene
     * los 6 números y el objetivo, espera el mensaje de David que indica el inicio oficial
     * de la ronda, momento en el cual el jugador puede comenzar a resolver el problema.</p>
     * 
     * <p>Mensajes esperados:</p>
     * <ul>
     *   <li>DAVID_EMPEZAR_CIFRAS_JUGADORES: Señal de inicio de la ronda</li>
     * </ul>
     * 
     * <p>Condición de finalización: Cuando se recibe el mensaje de inicio</p>
     * <p>Estado final: El jugador está listo para resolver el problema de cifras</p>
     */
    private class EsperarInicioDeRonda extends Behaviour {

        private boolean done = false;

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (msg.getContent().equals("DAVID_EMPEZAR_CIFRAS_JUGADORES")) {
                    System.out.println(getLocalName() + " -> ¡Comienza la ronda de cifras!");
                    done = true;
                }
            } else block();
        }

        @Override
        public boolean done() { return done; }
        
        @Override
        public int onEnd() {
            System.out.println(getLocalName() + " -> Inicio de ronda completado. Comenzando cálculo...");
            
            myAgent.addBehaviour(new ResolverJuego());
            
            return 0;
        }
        private class ResolverJuego extends Behaviour {
            @Override
            public void action() {
                System.out.println(getLocalName() + " -> Aquí resolvería la cuenta con los números recibidos.");
                // TODO: implementar lógica
            }

            @Override
            public boolean done() { return true; }
        }
    }
}
