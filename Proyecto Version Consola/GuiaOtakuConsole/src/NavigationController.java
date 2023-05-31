import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class NavigationController {
    private Scanner input;
    private View vista;
    private Security security;
    private EmbeddedNeo4j db;
    private Usuario currentUser;
    private LinkedList<String> genres; //almacena el listado de generos en la base de datos
    private LinkedList<String> studios; //almacena el listado de estudio en la base de datos
    private String genre;//almacena el nombre del genero seleccionado por el usuario
    private String anime;//almacena el nombre del anime seleccionado por el usuario
    private String studio; // almacena el estudio seleccionado por el usuario


    /**
     * Constructor del navegador
     */
    public NavigationController() {
        input = new Scanner(System.in);
        vista = new View();
        security = new Security();
        db = new EmbeddedNeo4j("bolt://44.204.86.134:7687", "neo4j", "task-default-soap");
        currentUser = new Usuario();
        studios = db.getStudios();
        genres = db.getGenres();
    }

    /**
     * Inicializa la aplicacion con la bienvenida y el menu de ingreso
     */
    public void startApplication() {
        int option = 0;
        boolean flag = true;
        vista.printWelcome();
        while(flag) {
            vista.printEntryMenu();
            option = security.validOption();
            switch (option) {
                case 1:
                    // iniciar sesion
                    login();
                    break;
                case 2:
                    // registrarse
                    register();
                    break;
                case 3:
                    // salir
                    vista.Message("Gracias por utilizar LaGuiaOtaku");
                    break;
                default:
                    vista.printInvalidOption();
                    break;
            }
        }
    }

    /**
     * Realiza el inicio de sesion a un usuario existente
     */
    protected void login() {
        vista.Message("=== INICIO DE SESIÓN ===");
        vista.Message("Ingrese su nombre de usuario: ");
        String username = input.next();
        vista.Message("Ingrese su contraseña: ");
        String password = input.next();
        if (db.checkCredentials(username, password)) {
            currentUser.setUsername(username);
            HomePage();
        } else {
            vista.printError("El usuario o contraseña son incorrectos");
            vista.Message("Intentelo nuevamente");
        }
    }

    /**
     * realiza la creacion de un usuario
     */
    protected void register() {
        vista.Message("=== REGISTRO ===");
        vista.Message("Por favor complete los siguientes campos");
        vista.Message("Ingrese el nombre de usuario que desea: ");
        String username = input.next();
        vista.Message("Ingrese su nombre: ");
        String firstName = input.next();
        vista.Message("Ingrese su apellido: ");
        String lastName = input.next();
        vista.Message("Ingrese su contraseña: ");
        String password = input.next();

        if (db.createUser(username, password, firstName, lastName)) {
            vista.Message("Usuario creado exitosamente");
            currentUser.setUsername(username);
            HomePage();
        } else {
            vista.printError("El usuario ya existe, intente nuevamente");
        }
    }

    /**
     * Realiza el menu principal del programa
     */
    private void HomePage(){
        int option = 0;
        boolean flag = true;
        while (flag){
            vista.userSession(currentUser);
            vista.printHomePage();
            option = security.validOption();
            switch (option){
                case 1:
                    //Explorar
                    exploreAnime();
                    break;
                case 2:
                    //preferencias
                    configurePreferences(currentUser);
                    break;
                case 3:
                    //recomendaciones
                    showRecommendations(currentUser);
                    break;
                case 4:
                    // mi usuario
                    myUser();
                    break;
                case 5:
                    //cerrar sesion
                    flag = false;
                    vista.Message("Has cerrado Sesión");
                    startApplication();
                    break;
                case 6:
                    vista.printInvalidOption();
                    break;

            }
        }
    }


    /**
     * Realiza la seccion de exploracion
     */
    private void exploreAnime() {
        int option = 0;
        boolean flag = true;
        boolean innerFlag= true;
        String temp;
        while (flag){
            vista.Separator();
            vista.printExplore();
            option = security.validOption();
            switch (option) {
                case 1:
                    System.out.println("Los géneros disponibles son: ");
                    vista.printList(genres);
                    vista.printSelect();
                    option = security.validOption();
                    genre = vista.getByIndex(option,genres);
                    vista.Message("Animes del genero: "+genre);
                    vista.printList(db.getAnimesByGenre(genre));
                    vista.Message("Selecciona el anime que quieres ver: ");
                    option = security.validOption();
                    anime = vista.getByIndex(option,db.getAnimesByGenre(genre));
                    vista.Message("Esta es la informacion de: "+anime);
                    vista.showAnimeInfo(db.getAnimeInfo(anime));
                    vista.printReturn("EXPLORAR");
                    temp = input.next();
                    break;
                case 2:
                    System.out.println("Los estudios disponibles son: ");
                    vista.printList(studios);
                    vista.printSelect();
                    option = input.nextInt();
                    studio = vista.getByIndex(option,studios);
                    vista.Message("Los animes que el estudio "+studio+" ha animado son:");
                    vista.printList(db.getAnimesByStudio(studio));
                    vista.Message("Selecciona el anime que quieres ver: ");
                    option = input.nextInt();
                    anime = vista.getByIndex(option,db.getAnimesByStudio(studio));
                    vista.Message("Esta es la informacion de: "+anime);
                    vista.showAnimeInfo(db.getAnimeInfo(anime));
                    vista.printReturn("EXPLORAR");
                    temp = input.next();
                    break;
                case 3:
                    flag = false;
                    HomePage();
            }
        }
    }

    /**
     * realiza la seccion de preferencias
     * @param currentUser el username del usuario de sesion actual
     */
    private void configurePreferences(Usuario currentUser) {
        vista.Separator();
        vista.Message("=== PREFERENCIAS ===");
        String temp;
        if (db.userHasInterests(currentUser.getUsername())){
            int option = 0;
            vista.Message("Actualmente ya tienes estas preferencias: ");
            vista.Message("Géneros: ");
            vista.printList(db.getUserInterestsGenres(currentUser.getUsername()));
            vista.Message("Estudios: ");
            vista.printList(db.getUserInterestsStudios(currentUser.getUsername()));
            vista.Message("¿Deseas resetear tus preferencias?");
            vista.Message("1. Sí");
            vista.Message("2. No");
            vista.printSelect();
            option = security.validOption();
            if (option == 1){
                vista.Message("Estas seguro de borrar tus preferencias?");
                vista.Message("Esto borrara tus preferencias acutales");
                vista.Message("1. Sí");
                vista.Message("2. No");
                option = security.validOption();
                if (option == 1){
                    db.resetInterests(currentUser.getUsername());
                    vista.Message("Tus preferencias se han borrado");
                }
            }
            vista.printReturn("MENU PRINCIPAL");
            temp = input.next();
            HomePage();
        } else {
            System.out.println("¡Te invitamos a que selecciones tus categorías de interés!");
            System.out.println("GÉNEROS");
            System.out.println("Selecciona los que más te gusten");
            vista.printList(genres);
            List<String> genreInterest = security.validFormatPref(genres);
            System.out.println("Excelente ahora la siguiente categoría");
            System.out.println("ESTUDIOS");
            System.out.println("Selecciona los que más te gusten");
            vista.printList(studios);
            List<String> studioInterest = security.validFormatPref(studios);
            System.out.println("Tus preferencias se han guardado exitosamente");
            db.createInterests(currentUser.getUsername(), genreInterest, studioInterest);
            vista.printReturn("MENU PRINCIPAL");
            temp = input.next();
            HomePage();
        }
    }

    /**
     * realiza la seccion de recomendaciones
     * @param currentUser el username del usuario de sesion actual
     */
    private void showRecommendations(Usuario currentUser) {
        vista.Separator();
        vista.Message("=== RECOMENDACIONES ===");
        if (db.userHasInterests(currentUser.getUsername())){
            vista.Message("Este es tu TOP 10 animes que te recomendamos");
            vista.Message("Basados en tus preferencias actuales");
            ArrayList<String> recommended = db.createMegaList();
            LinkedList<String> top = (LinkedList<String>) recommended.subList(0, recommended.size()-1);
            vista.printList(top);
        } else {
            System.out.println("Vaya, parece que aún no has configurado tus preferencias");
            HomePage();
        }
    }


    /**
     * Realiza la seccion de mi usuario
     */
    private void myUser(){
        vista.Separator();
        String temp;
        vista.Message("=== MI PERFIL ===");
        vista.showUserInfo(db.getUserInfo(currentUser.getUsername()));
        vista.printReturn("MENU PRINCIPAL");
        temp=input.next();
    }

}
