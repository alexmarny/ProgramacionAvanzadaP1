package es.usal.pa.agent;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.*;

public class ExpertoDavid extends Agent {

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

            // 2.3 Agregar un WakerBehaviour que se activará después de 45 segundos para finalizar la ronda
            myAgent.addBehaviour(new WakerBehaviour(myAgent, 45000) {
                @Override
                protected void onWake() {
                    sendFinRonda();
                    myAgent.addBehaviour(new AnalizarGanadoresBehaviour(jugadores, resultados, numeroBuscado));
                    System.out.println("Ronda finalizada...");
                    myAgent.removeBehaviour(this);
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

    }

    private class RecogerResultadosBehaviour extends CyclicBehaviour {

        private static final long serialVersionUID = 1L;
        private List<String> jugadores;
        private List<String> resultados;

        public RecogerResultadosBehaviour(List<String> jugadores, List<String> resultados) {
            this.jugadores = jugadores;
            this.resultados = resultados;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();

            if (msg != null) {

                String contenido = msg.getContent();

                // Comprobamos si es un resultado válido
                if (contenido.startsWith("JUGADOR_SOLUCION_DAVID_")) {

                    String jugador = msg.getSender().getLocalName();

                    if (jugadores.contains(jugador)) {
                        String solucion = contenido.substring(contenido.lastIndexOf("_") + 1);

                        System.out.println(getLocalName() + 
                            " → Recibida solución de " + jugador + ": " + solucion);

                        resultados.add(jugador + ":" + solucion);
                    }
                }

                if (resultados.size() == jugadores.size()) {
                    System.out.println(getLocalName() + " → Se han recibido todas las soluciones.");
                    myAgent.removeBehaviour(this);
                }

            } else {
                block();
            }
        }
    }
    
    private class AnalizarGanadoresBehaviour extends OneShotBehaviour {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private List<String> jugadores;
        private List<String> resultados;
        private int numeroBuscado;

        public AnalizarGanadoresBehaviour(List<String> jugadores, List<String> resultados, int numeroBuscado) {
            this.jugadores = jugadores;
            this.resultados = resultados;
            this.numeroBuscado = numeroBuscado;
        }

        @Override
        public void action() {

            if (resultados.isEmpty()) {
                enviarAvisoSinGanadores();
                myAgent.addBehaviour(new EsperarRonda());
                myAgent.removeBehaviour(this);
            }

            // Cada entrada es "nombre:solucion"
            int mejorDiferencia = Integer.MAX_VALUE;
            List<String> ganadores = new ArrayList<>();

            for (String r : resultados) {
                String[] partes = r.split(":");
                int valor = Integer.parseInt(partes[1]);

                int diferencia = Math.abs(valor - numeroBuscado);

                if (diferencia < mejorDiferencia) {
                    mejorDiferencia = diferencia;
                    ganadores.clear();
                    ganadores.add(r);
                } else if (diferencia == mejorDiferencia) {
                    ganadores.add(r);
                }
            }

            // Enviar mensajes de ganadores
            for (String g : ganadores) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("DAVID_GANADOR_JUGADORES_AITOR_" + g); // ej: GANADOR_Jugador1:347

                // enviamos a todos los jugadores
                for (String j : jugadores) msg.addReceiver(new AID(j, AID.ISLOCALNAME));
                // y a Aitor
                msg.addReceiver(new AID("Aitor", AID.ISLOCALNAME));

                send(msg);
            }
            
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("DAVID_FINALIZAR_CIFRAS_JUGADORES");
            for (String j : jugadores) msg.addReceiver(new AID(j, AID.ISLOCALNAME));
            msg.addReceiver(new AID("Aitor", AID.ISLOCALNAME));
            
            myAgent.addBehaviour(new EsperarRonda());
            myAgent.removeBehaviour(this);
            
        }

        private void enviarAvisoSinGanadores() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("SIN_GANADORES");

            for (String j : jugadores) msg.addReceiver(new AID(j, AID.ISLOCALNAME));
            msg.addReceiver(new AID("Aitor", AID.ISLOCALNAME));

            send(msg);
        }
    }
}

