package es.usal.pa.agent;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Iterator;

import java.util.*;

public class expertoDavid extends Agent {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	 List<String> jugadores = new ArrayList<>();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");

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
			
			MessageTemplate mt = MessageTemplate.MatchContent("AITOR_TURNO_DAVID_JUGADORES");
			ACLMessage msg = receive(mt);
			

			if (msg != null) {

			    jugadores.clear();
			    jade.util.leap.Iterator it = msg.getAllReceiver();

			    while (it.hasNext()) {
			        AID aid = (AID) it.next();
			        if (!aid.getLocalName().equals(myAgent.getLocalName())) {
			            jugadores.add(aid.getLocalName());
			        }
			    }

	                System.out.println("Turno recibido de Aitor. Lista de jugadores:");
	                for (String j : jugadores) {
	                    System.out.println(" - " + j);
	                }
	                
	                myAgent.addBehaviour(new RondaCifras(jugadores));
	                myAgent.removeBehaviour(this);
	        } else {
	            block(); 
	        }
	    }
	}

            

    // ---------------------- RONDA DE CIFRAS ------------------------
    private class RondaCifras extends OneShotBehaviour {
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private List<String> jugadores;
        private int[] numeros = {1, 3, 5, 25, 50, 75};
        private int numeroBuscado = 347;
        private List<String> resultados = new ArrayList<>();

        public RondaCifras(List<String> jugadores) {
            this.jugadores = jugadores;
        }

        @Override
        public void action() {
        	
        	System.out.println(getLocalName() + " → INICIANDO RONDA DE CIFRAS");
            // 2.1 Enviar los números y número buscado
            sendNumeros();
            sendNumeroBuscado();
            sendInicioRonda();

            // 2.2 Agregar comportamiento para recibir resultados mientras la ronda está activa
            myAgent.addBehaviour(new RecogerResultadosBehaviour(jugadores, resultados));

            // 2.3 Agregar un WakerBehaviour que se activará después de 40 segundos para finalizar la ronda
            myAgent.addBehaviour(new WakerBehaviour(myAgent, 40000) {
                @Override
                protected void onWake() {
                    sendFinRonda();
                    enviarGanadores(resultados);
                    System.out.println("Ronda finalizada. Esperando siguiente turno de Aitor...");
                }
            });
        }

        // Métodos auxiliares
        private void sendNumeros() {
            for (int n : numeros) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("DAVID_NUMERO_JUGADORES_" + n);
                for (String j : jugadores) msg.addReceiver(new AID(j, AID.ISLOCALNAME));
                send(msg);
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
        }

        private void sendNumeroBuscado() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("DAVID_VALOR_BUSCADO_JUGADORES_" + numeroBuscado);
            for (String j : jugadores) msg.addReceiver(new AID(j, AID.ISLOCALNAME));
            send(msg);
        }

        private void sendInicioRonda() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("DAVID_EMPEZAR_CIFRAS_JUGADORES");
            for (String j : jugadores) msg.addReceiver(new AID(j, AID.ISLOCALNAME));
            send(msg);
        }

        private void sendFinRonda() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("DAVID_FINALIZAR_CIFRAS_JUGADORES");
            for (String j : jugadores) msg.addReceiver(new AID(j, AID.ISLOCALNAME));
            send(msg);
        }

        private void enviarGanadores(List<String> resultados) {
            List<String> ganadores = new ArrayList<>();
            for (String r : resultados) {
                String[] parts = r.split(":");
                String jugador = parts[0];
                int solucion = Integer.parseInt(parts[1]);
                if (solucion == numeroBuscado) {
                    ganadores.add(jugador + "(" + solucion + ")");
                }
            }

            if (!ganadores.isEmpty()) {
                for (String g : ganadores) {
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setContent("DAVID_GANADOR_JUGADORES_AITOR:" + g);
                    for (String j : jugadores) msg.addReceiver(new AID(j, AID.ISLOCALNAME));
                    msg.addReceiver(new AID("Aitor", AID.ISLOCALNAME));
                    send(msg);
                }
            } else {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("DAVID_GANADOR_JUGADORES_AITOR:NINGUNO");
                for (String j : jugadores) msg.addReceiver(new AID(j, AID.ISLOCALNAME));
                msg.addReceiver(new AID("Aitor", AID.ISLOCALNAME));
                send(msg);
            }
        }
    }

    // 3️⃣ Comportamiento para recoger resultados mientras la ronda está activa
    private class RecogerResultadosBehaviour extends CyclicBehaviour {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private List<String> jugadores;
        private List<String> resultados;

        public RecogerResultadosBehaviour(List<String> jugadores, List<String> resultados) {
            this.jugadores = jugadores;
            this.resultados = resultados;
        }

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchContent("RESULTADO:");
            ACLMessage msg = receive(mt);

            if (msg != null) {
                String content = msg.getContent();
                String jugador = msg.getSender().getLocalName();
                if (jugadores.contains(jugador)) { // <-- filtramos jugadores activos
                    String solucion = content.split(":")[1];
                    resultados.add(jugador + ":" + solucion);
                    } else {
                    	block();
                    	}
                }
            }
        }
    }
