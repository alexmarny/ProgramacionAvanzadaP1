package es.usal.pa.agent;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;



public class agenteAitor extends Agent {

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
            for (int i = 0; i < args.length; i++) {
                jugadores[i] = (String) args[i];
            }
        } else {
            System.out.println("No se han indicado jugadores. Ejemplo ejecución:");
            System.out.println("jade.Boot -gui Aitor:AitorAgent(j1,j2,j3)");
            doDelete();
            return;
        }

        addBehaviour(new CuentaAtrasBehaviour());
    }

    private class CuentaAtrasBehaviour extends Behaviour {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private boolean finished = false;

        @Override
		public void action() {

		    // Se envía mensaje a los jugadores con el valor actual
		    for (String jugador : jugadores) {
		        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		        msg.setContent("AITOR_CUENTA_" + counter);
		        msg.addReceiver(new AID(jugador, AID.ISLOCALNAME));
		        send(msg);
		    }

		    System.out.println("Cuenta atrás: " + counter);

		    if (counter == 0) {
		        // Cuando llega a 0, avisamos al experto y jugadores
		        ACLMessage inicio = new ACLMessage(ACLMessage.INFORM);
		        inicio.setContent("AITOR_INICIO_RONDA");
		        inicio.addReceiver(new AID("David", AID.ISLOCALNAME));
		        for (String jugador : jugadores) {
		            inicio.addReceiver(new AID(jugador, AID.ISLOCALNAME));
		        }

		        send(inicio);

		        // Esperamos respuesta del experto
		        myAgent.addBehaviour(new EsperarGanadoresBehaviour());

		        // Terminamos este comportamiento
		        finished = true;
		    } else {
		        // esperamos 1 segundo antes del siguiente tick
		        block(1000);
		        counter--;
		    }
		}


        @Override
        public boolean done() {
            return finished;
        }
    }

    private class EsperarGanadoresBehaviour extends CyclicBehaviour {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchConversationId("GANADORES");
            ACLMessage mensaje = receive(mt);

            if (mensaje != null) {
                System.out.println("Ganadores recibidos desde David: " + mensaje.getContent());

                // Reiniciar cuenta atrás
                counter = 15;
                myAgent.addBehaviour(new CuentaAtrasBehaviour());
                myAgent.removeBehaviour(this);
            } else {
                block();
            }
        }
    }
}
