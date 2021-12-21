import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/*ACLARACIONES:

    1. Mediante el AtomicInteger puesto podemos solucionar el problema de que varios hilos que superen la prueba 2, queden en la misma posición.
    No obstante, sigo necesitando hacerlo estático y público en Main para manejarlo.
    Si lo creo en la clase Jugador me han surgido problemas. Había casos en que dos hilos superaban la prueba 2, y solo uno aparecía en la consola con su posición
    El otro era descalificado al rato, pero a haber superado primero la prueba 2.

    2.Pasar como parámetro el número de prueba nos permite reutilizar el código de la funcion realizarPrueba. De esta manera solo cambiamos el número que aparece por consola.

    3.Existen 3 warnings en el código. Esto se debe a que los métodos son procedimientos (void para Java) y estos tipos no deben llevar return, puesto que no
    devuelven ningún valor como las funciones. Sin embargo, los he utilizado para "matar" los hilos según corresponde, por lo que los considera necesarios.
 */

public class Main {

    public static AtomicBoolean hayGanador = new AtomicBoolean(false);
    //static int posicion = 2;
    public static AtomicInteger puesto = new AtomicInteger(2);

    public static void main(String[] args) {

        for(int i = 0; i < 20; i++){
            Jugador player = new Jugador();
            player.setName("jugador " + i);
            player.start();
        }

    }
}

class Jugador extends Thread{

    private static final int NUM_ACCESO_SIMULTANEOS = 10;
    int jugadoresEnCola = 0;

    AtomicBoolean segundaFasePasada = new AtomicBoolean(false);

    Semaphore semaphorePosiciones = new Semaphore(5,true);

    @Override
    public void run (){
        realizarPrueba(1);
    }

    private void realizarPrueba(int numPrueba) {

        try {

            Thread.sleep((new Random().nextInt(4)+1)*1000);

            Random r = new Random();

            if (r.nextInt(10) == 1) {

                System.out.println("El " + getName() + " ha sido descalificado en la prueba "+numPrueba);
                return;

            }else {

                System.out.println("El " + getName() + " ha superado la prueba " + numPrueba);

                if (segundaFasePasada.compareAndSet(false, true)) {

                    intentarSegundaFase();

                } else {

                    semaphorePosiciones.release(5);
                    Random random = new Random();
                    Integer num = random.nextInt();
                    System.out.println("Entro " + num);
                    semaphorePosiciones.acquire();
                    System.out.println("Salgo " + num);

                    if (Main.hayGanador.compareAndSet(false, true)) {

                        System.out.println("EL " + getName() + " HA GANADO");

                    } else {
                        int i = Main.puesto.getAndIncrement();
                        if (i <= 5) {
                            System.out.println("EL " + getName() + " HA QUEDADO EN LA POSICION " + (i));

                        } else {

                            System.out.println("El " + getName() + " no ha llegado a tiempo a la prueba " + numPrueba + " y ha sido descalificado");
                            return;

                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void intentarSegundaFase() {

        Semaphore semaphore = new Semaphore(10,true);

        //semaphore.release(10);

        if(semaphore.tryAcquire()){

            System.out.println("El "+getName()+" ha completado a tiempo la prueba 1");
            segundaFasePasada.set(true);
            realizarSegundaFase();

        }else{

            System.out.println("El "+getName()+" no ha completado a tiempo la prueba 1 y ha sido descalificado");
            return;

        }
    }

    private void realizarSegundaFase() {

        Semaphore semaphore2 = new Semaphore(NUM_ACCESO_SIMULTANEOS,true);
        jugadoresEnCola++;

        if ( jugadoresEnCola == NUM_ACCESO_SIMULTANEOS){

            semaphore2.release(NUM_ACCESO_SIMULTANEOS);

        }

        try {

            semaphore2.acquire();
            System.out.println("Esto deber•a salir con 10 m♣s a la vez");
            Thread.sleep(5000);
            realizarPrueba(2);

        } catch (InterruptedException e) {

            e.printStackTrace();

        }
    }
}