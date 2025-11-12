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
                    String tiempo = contenido.substring(contenido.lastIndexOf(":") + 1);
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

    private class RondaCifras extends Behaviour {

        @Override
        public void action(){
            CallableSolucionTeclado callableSolucionTeclado=new CallableSolucionTeclado(numeros, objetivo);
            FutureTask<Solucion> task = new FutureTask<Solucion> (callableSolucionTeclado);
            ExecutorService executorService = Executors.newSingleThreadExecutor ();
            executorService.submit(task);
            ACLMessage solMsg = new ACLMessage(ACLMessage.INFORM);
		    
            
            Solucion solucion=null;
            try
            {
                //dejo como máximo 45 segundos para introducir operaciones
                solucion = task.get(45, TimeUnit.SECONDS);
                
                //cuando no salta el timeout
                System.out.println("Solución registrada");
                System.out.println(AuxSolucion.cadenaOperaciones(solucion));
                solMsg.setContent("JUGADOR_SOLUCION_DAVID" + solucion);
		        solMsg.addReceiver(new AID("David", AID.ISLOCALNAME));
                send(solMsg);

                
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
            
            //implementar que cierre cuando se reciba el mensaje de "DAVID_FINALIZAR_CIFRAS"
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
                solMsg.setContent("JUGADOR_SOLUCION_DAVID" + solucion);
		        solMsg.addReceiver(new AID("David", AID.ISLOCALNAME));
                send(solMsg);
            }
            // myAgent.addBehaviour(new SiguienteComportamiento());
	    }
    }

    private class EsperarGanadoresDavid extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
            	String contenido = msg.getContent();
                if (contenido.equals("DAVID_SIN_GANADORES")) {
                    System.out.println("No ha habido ganadores en esta partida.");
                } else if (contenido.startsWith()){
                    String ganador = contenido.substring(contenido.lastIndexOf("_") + 1);
                    System.out.println("Ganador de la partida:" + ganador);
                } else { block ();}
                // myAgent.addBehaviour(new SiguienteComportamiento());
            }
        }
    }

    private class RecibirGanadores extends Behaviour {

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            System.out.println("Ganador/es de la partida:");

            do {
                msg = myAgent.receive();
                String contenido = msg.getContent();
                if(contenido.startsWith("DAVID_GANADOR_")){
                    String ganador = contenido.substring(contenido.lastIndexOf("_") + 1);
                    System.out.println(ganador);
                }

            }while (msg != null)
        }

		@Override
		public boolean done() {return done;} //no entiendo muy bien qué hay que poner aquí
		
        @Override
		public int onEnd() {
            System.out.println(getLocalName() + " -> Se recibieron los ganadores.");
            // myAgent.addBehaviour(new SiguienteComportamiento());
            return 0;
        }
    }
}
