import java.util.LinkedList;

/**
 * Clase encargada de mostrarle mensajes a los usuarios en consola
 * @author diego leiva, pablo orellana
 */
public class View {

    /**
     * Metodo para la impresion de listados
     * @param list la lista a mostrar
     */
    public static void printList(LinkedList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            String item = list.get(i);
            System.out.println(i + ". " + item);
        }
    }

    /**
     * Metodo que muestra el mensaje de bienvenida al usuario
     */
    public static void printWelcome() {
        System.out.println("************************************************");
        System.out.println("*                                              *");
        System.out.println("*               La Guia Otaku                  *");
        System.out.println("*                                              *");
        System.out.println("************************************************\n");
        System.out.println("           Bienvenido a La Guia Otaku");
        System.out.println("        tu guia para el mundo del anime\n");
    }

    /**
     * Metodo que muestra la "ventana" de inicio de sesion o registro al usuario
     */
    public static void printLogIn(){
        System.out.println("Para ingresar a nuestro sistema porfavor elija una opción");
        System.out.println("1. Iniciar sesión");
        System.out.println("2. Registrarse");
        System.out.println("3. Salir");
        printSelect();
    }

    /**
     * Metodo que muestra el menu principal de opciones al usuario
     */
    public static void printHomePage(){
        System.out.println("==== Menú Principal ====");
        System.out.println("1. Explorar");
        System.out.println("2. Preferencias");
        System.out.println("3. Recomendaciones");
        System.out.println("4. Mi Usuario");
        System.out.println("5. Salir");
        printSelect();
    }

    /**
     * Mensaje de seleccion de opcion
     */
    public static void printSelect(){
        System.out.println("Seleccione una opción: \n");
    }

    /**
     * Metodo que muestra un mensaje en pantalla
     * @param msg el mensaje a mostrar
     */
    public static void printMessage(String msg){
        System.out.println(msg+"\n");
    }


}

