package es.usal.pa.agent;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;



public class AgenteAitor extends Agent {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String[] jugadores; // lista de agentes jugadores
    private int counter = 15;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");

        // Se reciben los nombres de los jugadores como argumentos
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            jugadores = new String[args.length];
            System.out.print("Jugadores registrados: ");
            for (int i = 0; i < args.length; i++) {
                jugadores[i] = (String) args[i];
                System.out.print(jugadores[i] + " ");
            } 
        } else {
            System.out.println("No se han indicado jugadores. Ejemplo ejecución:");
            System.out.println("jade.Boot -gui Aitor:AitorAgent(j1,j2,j3,...)");
            doDelete();
            return;
        }

        System.out.println("");
        addBehaviour(new CuentaAtrasBehaviour(this, 1000)); // cada 1000 ms (1 s)
    }

    private class CuentaAtrasBehaviour extends TickerBehaviour {

        private static final long serialVersionUID = 1L;

        public CuentaAtrasBehaviour(Agent a, long period) {
            super(a, period);
        }
        @Override
        protected void onTick() {
            // Enviar mensaje de cuenta atrás a los jugadores
            for (String jugador : jugadores) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("AITOR_TIEMPO_JUGADORES:" + counter);
                msg.addReceiver(new AID(jugador, AID.ISLOCALNAME));
                send(msg);
            }

            System.out.println("Cuenta atrás: " + counter);

            if (counter == 0) {
                // Enviar mensaje a David y jugadores para iniciar ronda
                ACLMessage inicio = new ACLMessage(ACLMessage.INFORM);
                inicio.setContent("AITOR_TURNO_DAVID_JUGADORES");
                inicio.addReceiver(new AID("expertoDavid", AID.ISLOCALNAME)); // nombre exacto
                for (String jugador : jugadores) {
                    inicio.addReceiver(new AID(jugador, AID.ISLOCALNAME));
                }
                send(inicio);

                System.out.println("Mensaje de turno enviado a David y jugadores.");
                
                myAgent.addBehaviour(new EsperarGanadoresBehaviour());
                // Detener ticker
                stop();
            } else {
                counter--;
            }
        }
    }

    private class EsperarGanadoresBehaviour extends CyclicBehaviour {

        private static final long serialVersionUID = 1L;
        private List<String> ganadores = new ArrayList<>(); // Para acumular los ganadores de esta ronda

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage mensaje = receive(mt);

            if (mensaje != null) {
                String contenido = mensaje.getContent();

                if (contenido != null) {

                    // Mensaje de un ganador enviado por David
                    if (contenido.startsWith("DAVID_GANADOR_JUGADORES_AITOR")) {
                        String ganador = contenido.substring("DAVID_GANADOR_JUGADORES_AITOR".length() + 1);
                        ganadores.add(ganador);
                        System.out.println(getLocalName() + " → Ganador recibido desde David: " + ganador);
                    }

                    // Mensaje de finalización de ronda enviado por David
                    else if (contenido.equals("DAVID_FINALIZAR_CIFRAS_JUGADORES")) {
                        System.out.println(getLocalName() + " → Ronda finalizada por David. Ganadores de esta ronda: " + ganadores);

                        // Reiniciamos la cuenta atrás para la siguiente ronda
                        counter = 15;
                        System.out.println(getLocalName() + " → Iniciando nueva cuenta atrás...");
                        myAgent.addBehaviour(new CuentaAtrasBehaviour(myAgent, 1000));

                        ganadores.clear();

                        myAgent.removeBehaviour(this);
                    }

                    // Mensaje de sin ganadores
                    else if (contenido.equals("DAVID_SIN_GANADORES")) {
                        System.out.println(getLocalName() + " → No ha habido ganadores.");

                        // Reiniciamos la cuenta atrás igualmente
                        counter = 15;
                        myAgent.addBehaviour(new CuentaAtrasBehaviour(myAgent, 1000));
                        myAgent.removeBehaviour(this);
                    }

                    // Mensaje no relevante
                    else {
                        block();
                    }

                } else {
                    block();
                }

            } else {
                block();
            }
        }
    }

}
