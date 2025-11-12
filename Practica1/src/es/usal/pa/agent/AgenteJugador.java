package es.usal.pa.agent;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import es.usal.pa.cifras.controlador.AuxSolucion;
import es.usal.pa.cifras.controlador.CallableSolucionTeclado;
import es.usal.pa.cifras.modelo.Solucion;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

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
                addBehaviour(new RecibirCifras());
                System.out.print("Cifras: ");
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
                        System.out.print(numero + " ");
                    } catch (NumberFormatException e) {
                        // Ignorar mensajes corruptos
                    }
                }
            } else block();
        }

		@Override
		public boolean done() {return numeros.size() == 6;}
		
        @Override
		public int onEnd() {
            System.out.println();
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
            
            myAgent.addBehaviour(new RondaCifras());
            
            return 0;
        }
    }

    private class RondaCifras extends Behaviour {

        private static final long serialVersionUID = 1L;
        private boolean done = false;

        @Override
        public void action() {
            // Crear el callable que gestionará la entrada de operaciones del jugador
            CallableSolucionTeclado callableSolucionTeclado = new CallableSolucionTeclado(numeros, objetivo);
            FutureTask<Solucion> task = new FutureTask<>(callableSolucionTeclado);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(task);

            Solucion solucion = null;

            try {
                // Esperar hasta 45 segundos a que el jugador introduzca operaciones
                solucion = task.get(45, TimeUnit.SECONDS);
                System.out.println(getLocalName() + " → Solución registrada dentro del tiempo.");
                System.out.println(AuxSolucion.cadenaOperaciones(solucion));

            } catch (TimeoutException e) {
                // Si se acaba el tiempo, cancela la tarea y usa el mejor resultado parcial
                System.out.println(getLocalName() + " → Tiempo agotado. Enviando mejor resultado parcial...");
                task.cancel(true);
                solucion = callableSolucionTeclado.getMejorResultadoCalculado();

            } catch (InterruptedException | ExecutionException e) {
                System.err.println(getLocalName() + " → Error al obtener solución: " + e.getMessage());
                e.printStackTrace();

            } finally {
                // Cerrar el executor correctamente
                executorService.shutdownNow();
                done = true;
            }

            // Enviar el mensaje al agente experto con la solución obtenida
            if (solucion != null) {
                ACLMessage solMsg = new ACLMessage(ACLMessage.INFORM);
                solMsg.addReceiver(new AID("expertoDavid", AID.ISLOCALNAME));
                solMsg.setConversationId("SOLUCION_CIFRAS");
                solMsg.setContent("JUGADOR_SOLUCION_DAVID_" + AuxSolucion.cadenaOperaciones(solucion));
                send(solMsg);

                System.out.println(getLocalName() + " → Solución enviada a expertoDavid.");
            } else {
                System.out.println(getLocalName() + " → No se generó ninguna solución válida.");
            }
            myAgent.addBehaviour(new RecibirGanadoresBehaviour());
        }

        @Override
        public boolean done() {
            return done;
        }
    }

    private class RecibirGanadoresBehaviour extends Behaviour {

        private boolean done = false;

        @Override
        public void action() {
            // Solo mensajes INFORM
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                String contenido = msg.getContent();

                if (contenido == null) {
                    return;
                }

                // Caso: no hay ganadores
                if (contenido.equals("DAVID_SIN_GANADORES")) {
                    System.out.println(getLocalName() + " → No ha habido ganadores en esta partida.");
                    done = true; // finaliza comportamiento
                } 
                // Caso: hay ganador
                else if (contenido.startsWith("DAVID_GANADOR_JUGADORES_AITOR:")) {
                    // Ejemplo: DAVID_GANADOR_JUGADORES_AITOR:Juan(347)
                    String ganador = contenido.substring(contenido.indexOf(":") + 1);
                    System.out.println(getLocalName() + " → Ganador de la partida: " + ganador);
                    // No finalizamos, porque puede venir otro mensaje con otro ganador
                } 
                else if (contenido.equals("DAVID_FINALIZAR_CIFRAS_JUGADORES")) {
                    // Señal de David de que no habrá más mensajes de ganadores
                    done = true;
                } 
                else {
                    // Mensaje no relevante, ignorar
                }
            } else {
                block(); // esperar próximos mensajes
            }
        }

        @Override
        public boolean done() {
            return done;
        }

        @Override
        public int onEnd() {
            System.out.println(getLocalName() + " → Fin recepción de ganadores.");
            return 0;
            }
    	}
    }
