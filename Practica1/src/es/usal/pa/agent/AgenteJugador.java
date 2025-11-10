package es.usal.pa.agent;

import java.util.ArrayList;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class AgenteJugador extends Agent {
	
	 private int objetivo;
	 private ArrayList<Integer> numeros = new ArrayList<>();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " conectado. Esperando cuenta atrás...");
        addBehaviour(new EsperarCuentaAtras());
    }

    private class EsperarCuentaAtras extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();

            if (msg != null) {
                String contenido = msg.getContent();

                if (contenido.startsWith("AITOR_TIEMPO_JUGADORES")) {
                    String tiempo = contenido.substring(contenido.lastIndexOf("_") + 1);
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
    }

        @Override
        public int onEnd() {
            // TODO: Replace with the actual next behavior for the game logic
            // myAgent.addBehaviour(new SiguienteComportamiento());
            System.out.println(getLocalName() + " -> EsperarInicioDeRonda finalizado. (Falta añadir el siguiente comportamiento)");
            return 0;
        }

        //hilar los comportamientos
    

    private class MostrarNumeros extends CyclicBehaviour {

        @Override
        public void action(){
            ACLMessage msg = myAgent.receive();
                if(msg != null){
                    String contenido = msg.getContent();
                    if (contenido.startsWith("DAVID_NUMERO_")){
                        String numero = contenido.substring(contenido.lastIndexOf("_") + 1);
                        System.out.println(getLocalName() + numero); //Convendría añadir un mensaje de "estos son los numeros" pero no sé cómo
                    }else if (contenido.startsWith("DAVID_VALOR_BUSCADO_JUGADORES_")){
                        String numero = contenido.substring(contenido.lastIndexOf("_") + 1);
                        System.out.println(getLocalName() + "Número objetivo: " + numero);
                        // myAgent.addBehaviour(new SiguienteComportamiento());

                    }else{
                        block();
                    }
                    
                }
        }
    }



    }
}
