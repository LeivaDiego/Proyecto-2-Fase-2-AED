import java.util.Scanner;

/**
 * Programa de recomendaciones de Anime
 * @author diego leiva, pablo orellana
 */
public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        View vista = new View();
        Security seguridad = new Security();
        EmbeddedNeo4j db = new EmbeddedNeo4j( "bolt://44.204.86.134:7687", "neo4j", "task-default-soap" );
        int option = 0;
        vista.printWelcome();
        vista.printLogIn();
        option = input.nextInt();
        switch (option){
            case 1:
                System.out.println("Ingrese su username: ");
                String username = input.next();
                System.out.println("Ingrese su contraseña: ");
                String password = input.next();
                db.login(username,password);
                break;
            case 2:
                System.out.println("Porfavor complete los siguientes campos");
                System.out.println("Ingrese el nombre de usuario que desea: ");
                String registerUser = input.next();
                System.out.println("Ingrese su nombre: ");
                String firstname = input.next();
                System.out.println("Ingrese su apellido: ");
                String lastname = input.next();
                System.out.println("Ingrese su contraseña: ");
                String contra = input.next();
                if (db.createUser(registerUser,contra,firstname,lastname))
                {
                    System.out.println("Usuario Creado Existosamente");
                }else System.out.println("El usuario ya existe, intente nuevamente");
                break;
            case 3:
                vista.printMessage("Gracias por utilizar LaGuiaOtaku");
                break;
            default:
                //Por si elije otra opcion
                break;
        }

    }


}