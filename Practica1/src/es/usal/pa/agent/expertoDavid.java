package es.usal.pa.agent;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class expertoDavid extends Agent {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String[] jugadores;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");

        // Se reciben nombres de jugadores como argumentos
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            jugadores = new String[args.length];
            for (int i = 0; i < args.length; i++)
                jugadores[i] = (String) args[i];
        } else {
            System.out.println("ERROR: No se han indicado jugadores.");
            doDelete();
            return;
        }

        addBehaviour(new EsperarRonda());
    }

    // ------------------ ESPERAR MENSAJE DE AITOR ------------------
    private class EsperarRonda extends CyclicBehaviour {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchContent("AITOR_INICIO_RONDA");
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                System.out.println("David: Recibido turno de Aitor. Iniciando ronda...");

                myAgent.addBehaviour(new RondaCifras());
            } else {
                block();
            }
        }
    }

    // ---------------------- RONDA DE CIFRAS ------------------------
    private class RondaCifras extends CyclicBehaviour {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public void action() {

            // 1. Números disponibles (puedes cambiarlos)
            int[] numeros = {1, 3, 5, 25, 50, 75};

            for (int n : numeros) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("DAVID_NUMERO_" + n);
                for (String j : jugadores) msg.addReceiver(new AID(j, AID.ISLOCALNAME));
                send(msg);

                try { Thread.sleep(100); } catch (Exception ignored) {}
            }

            // 2. Enviar número objetivo
            int objetivo = 347; // puedes randomizar si quieres
            ACLMessage obj = new ACLMessage(ACLMessage.INFORM);
            obj.setContent("DAVID_VALOR_BUSCADO_" + objetivo);
            for (String j : jugadores) obj.addReceiver(new AID(j, AID.ISLOCALNAME));
            send(obj);

            // 3. Enviar mensaje de inicio
            ACLMessage start = new ACLMessage(ACLMessage.INFORM);
            start.setContent("DAVID_EMPEZAR_CIFRAS");
            for (String j : jugadores) start.addReceiver(new AID(j, AID.ISLOCALNAME));
            send(start);

            // 4. Esperar 40 segundos
            try { Thread.sleep(40000); } catch (Exception ignored) {}

            // 5. Avisar final de ronda
            ACLMessage fin = new ACLMessage(ACLMessage.INFORM);
            fin.setContent("DAVID_FINALIZAR_CIFRAS");
            for (String j : jugadores) fin.addReceiver(new AID(j, AID.ISLOCALNAME));
            send(fin);

            // 6. Leer mensajes de resultados
            List<String> resultados = new ArrayList<>();
            ACLMessage m;
            do {
                m = myAgent.receive();
                if (m != null && "RESULTADO".equals(m.getConversationId()))
                    resultados.add(m.getSender().getLocalName() + ":" + m.getContent());
            } while (m != null);

            // 7. Buscar ganador(es)
            List<String> ganadores = new ArrayList<>();

            for (String r : resultados)
                ganadores.add(r);

            // 8. Enviar ganadores a todos y Aitor
            if (ganadores.isEmpty()) {
                ACLMessage noWinner = new ACLMessage(ACLMessage.INFORM);
                noWinner.setContent("DAVID_SIN_GANADORES");
                noWinner.addReceiver(new AID("Aitor", AID.ISLOCALNAME));
                for (String j : jugadores) noWinner.addReceiver(new AID(j, AID.ISLOCALNAME));
                send(noWinner);

            } else {
                for (String g : ganadores) {
                    ACLMessage winner = new ACLMessage(ACLMessage.INFORM);
                    winner.setContent("DAVID_GANADOR_" + g);
                    winner.addReceiver(new AID("Aitor", AID.ISLOCALNAME));
                    for (String j : jugadores) winner.addReceiver(new AID(j, AID.ISLOCALNAME));
                    send(winner);
                }
            }

            // 9. Volver a esperar la siguiente ronda
            myAgent.addBehaviour(new EsperarRonda());
            myAgent.removeBehaviour(this);
        }
    }
}
