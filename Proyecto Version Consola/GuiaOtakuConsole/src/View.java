import java.util.LinkedList;
import java.util.Map;

/**
 * Clase encargada de mostrarle mensajes a los usuarios en consola
 * @author diego leiva, pablo orellana
 */
public class View {

    /**
     * Metodo que extrae la informacion del usuario y la muestra en pantalla
     * @param userInfo
     */
    public void showUserInfo(Map<String, String> userInfo) {
        if (userInfo != null) {
            System.out.println("Información del Usuario:");
            System.out.println("Nombre de usuario: " + userInfo.get("username"));
            System.out.println("Nombre: " + userInfo.get("firstName"));
            System.out.println("Apellido: " + userInfo.get("lastName"));
            System.out.println("Contraseña: " + userInfo.get("password"));
        } else {
            System.out.println("No se encontró información del usuario.");
        }
    }

    /**
     * Metodo que muestra la informacion del anime
     * @param animeInfo
     */
    public void showAnimeInfo(Map<String, Object> animeInfo) {
        for (Map.Entry<String, Object> entry : animeInfo.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    /**
     * Metodo que obtiene el nombre del genero dependiendo de su indice
     * @param index el indice elegido
     * @param list el listado de generos
     * @return
     */
    public String getByIndex(int index, LinkedList<String> list) {
        return list.get(index);  // Los índices de la lista comienzan en 0
    }

    /**
     * Metodo para la impresion de listados
     * @param list la lista a mostrar
     */
    public void printList(LinkedList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            String item = list.get(i);
            System.out.println(i + ". " + item);
        }
    }

    /**
     * Metodo que muestra el mensaje de bienvenida al usuario
     */
    public void printWelcome() {
        Separator();
        System.out.println("************************************************");
        System.out.println("*                                              *");
        System.out.println("*               LA GUIA OTAKU                  *");
        System.out.println("*                                              *");
        System.out.println("************************************************\n");
        System.out.println("           Bienvenido a La Guia Otaku");
        System.out.println("        tu guia para el mundo del anime\n");
    }

    /**
     * Metodo que muestra la "ventana" de inicio de sesion o registro al usuario
     */
    public void printEntryMenu(){
        Separator();
        System.out.println("Para ingresar a nuestro sistema porfavor elija una opción");
        System.out.println("1. Iniciar sesión");
        System.out.println("2. Registrarse");
        System.out.println("3. Salir");
        printSelect();
    }

    /**
     * Metodo que muestra el menu principal de opciones al usuario
     */
    public void printHomePage(){
        Separator();
        System.out.println("==== MENU PRINCIPAL ====");
        System.out.println("1. Explorar");
        System.out.println("2. Preferencias");
        System.out.println("3. Recomendaciones");
        System.out.println("4. Mi Usuario");
        System.out.println("5. Cerrar Sesión");
        printSelect();
    }

    /**
     * Mensaje de seleccion de opcion
     */
    public void printSelect(){
        System.out.println("Seleccione una opción: ");
    }

    /**
     * Metodo que muestra cualquiera un mensaje en pantalla
     * @param msg el mensaje a mostrar
     */
    public void Message(String msg){
        System.out.println(msg);
    }

    /**
     * Muestra un mensaje de error en pantalla y su razon
     * @param msg la razon del error
     */
    public void printError(String msg){
        System.out.println("ERROR " + msg);
    }

    /**
     * Muestra en pantalla el menu de exploracion
     */
    public void printExplore(){
        Separator();
        System.out.println("=== EXPLORAR ===");
        System.out.println("1. Ver animes por Género");
        System.out.println("2. Ver animes por Estudio");
        System.out.println("3. Regresar al menu principal");
        printSelect();
    }

    /**
     * Muestra en pantalla el mensaje de bienvenida al usuario actual
     * @param user el usuario actual
     */
    public void printWelcomeUser(Usuario user){
        System.out.println("\nBienvenido :"+user.getUsername());
    }

    /**
     * Muestra un mensaje de que la opcion es no valida
     */
    public void printInvalidOption(){
        printError("Opción inválida");
        Message("Intenta de nuevo");
    }

    /**
     * Muestra un mensaje que indica que se regresara a la pantalla anterior
     * @param window la pantalla a la que se regresara
     */
    public void printReturn(String window){
        Message("Presiona cualquier letra para regresar a "+window);
    }

    /**
     * Muestra el separador en pantalla
     */
    public void Separator(){
        System.out.println("-----------------------------------------------------------------------------------------");
    }
}

