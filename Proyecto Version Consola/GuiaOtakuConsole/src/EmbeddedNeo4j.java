import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import static org.neo4j.driver.Values.parameters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Clase que tiene los metodos necesarios para conectarse e interactuar con una base de datos en Neo4j
 * @author diego leiva, pablo orellana
 * Referencia: Malonso-UVG
 */
public class EmbeddedNeo4j implements AutoCloseable {

    private final Driver driver;


    /**
     * Constructor del driver
     *
     * @param uri      bolt url
     * @param user     user
     * @param password password
     */
    public EmbeddedNeo4j(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    /**
     * Cierra la conexion
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        driver.close();
    }

    /**
     * Obtiene todos los generos almacenados en la base de datos
     *
     * @return un listado con los generos de la base de datos
     */
    public LinkedList<String> getGenres() {
        try (Session session = driver.session()) {
            LinkedList<String> genres = session.readTransaction(new TransactionWork<LinkedList<String>>() {
                @Override
                public LinkedList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (g:Genero) RETURN g.nombre");
                    LinkedList<String> mygenres = new LinkedList<>();
                    List<Record> registros = result.list();
                    for (Record registro : registros) {
                        mygenres.add(registro.get("g.nombre").asString());
                    }
                    return mygenres;
                }
            });
            return genres;
        }
    }

    /**
     * Obtiene un listdo de todos los estudios en la base de datos
     *
     * @return el listado de todos los estudios en la base de datos
     */
    public LinkedList<String> getStudios() {
        try (Session session = driver.session()) {
            LinkedList<String> studios = session.readTransaction(new TransactionWork<LinkedList<String>>() {
                @Override
                public LinkedList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (e:Estudio) RETURN e.nombre");
                    LinkedList<String> mystudios = new LinkedList<>();
                    List<Record> registros = result.list();
                    for (Record registro : registros) {
                        mystudios.add(registro.get("e.nombre").asString());
                    }
                    return mystudios;
                }
            });
            return studios;
        }
    }

    /**
     * Revisa si las credenciales del usuario son correctas o no
     *
     * @param username el nombre de usuario
     * @param password la contrasenia
     * @return verdadero si estan correctas, falso si no lo estan
     */
    public boolean checkCredentials(String username, String password) {
        try (Session session = driver.session()) {
            String encryptedPassword = Security.encrypt(password);
            return session.readTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: $username, contrasenia: $contrasenia}) RETURN u",
                            parameters("username", username, "contrasenia", encryptedPassword));

                    return result.hasNext(); // True if the user exists, false otherwise.
                }
            });
        }
    }


    /**
     * Verifica si el nombre de usuario ya existe en la base de datos para evitar crear usuarios repetidos
     * @param username el nombre de usuario
     * @return verdadero si el usuario ya existe, falso si aun no existe
     */
    public boolean verifyUserInDB(String username) {
        try ( Session session = driver.session() ) {
            return session.readTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: $username}) RETURN u",
                            parameters("username", username));
                    return result.hasNext();
                }
            });
        }
    }

    /**
     * Crea un nuevo nodo Usuario en la base de datos con su informacion respectiva
     * @param username el nombre de usuario
     * @param password la contrasenia
     * @param firstName el nombre
     * @param lastName el apellido
     * @return mensaje de creacion exitosa
     */
    public boolean createUser(String username, String password, String firstName, String lastName) {
        if (verifyUserInDB(username)) {
            return false;
        }else {

            try (Session session = driver.session()) {
                String encryptedPassword = Security.encrypt(password);
                String encryptedFirstName = Security.encrypt(firstName);
                String encryptedLastName = Security.encrypt(lastName);
                session.writeTransaction(new TransactionWork<Void>() {
                    @Override
                    public Void execute(Transaction tx) {
                        tx.run("CREATE (u:Usuario {username: $username, contrasenia: $password, nombre: $firstName, apellido: $lastName})",
                                parameters("username", username, "password", encryptedPassword, "firstName", encryptedFirstName, "lastName", encryptedLastName));
                        return null;
                    }
                });

                return true;
            }
        }
    }

    /**
     * Metodo que obtiene el listado de animes diposnibles dependiendo del genero que seleccione el usuario
     * @param genre el genero que selecciono el usuario
     * @return el listado de animes que pertenecen a ese genero
     */
    public LinkedList<String> getAnimesByGenre(String genre) {
        try ( Session session = driver.session() ) {
            return session.readTransaction(new TransactionWork<LinkedList<String>>() {
                @Override
                public LinkedList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (a:Anime)-[:PERTENECE]->(g:Genero {nombre: $genre}) RETURN a.titulo",
                            parameters("genre", genre));

                    LinkedList<String> animes = new LinkedList<>();
                    while (result.hasNext()) {
                        animes.add(result.next().get("a.titulo").asString());
                    }
                    return animes;
                }
            });
        }
    }

    /**
     * Metodo que obtiene un listado de animes diponibles dependiendo del estudio de animacion que seleccione el usuario
     * @param studio el estudio de animacion seleccionado
     * @return el listado de animes que fuerona animados por el estudio
     */
    public LinkedList<String> getAnimesByStudio(String studio) {
        try ( Session session = driver.session() ) {
            return session.readTransaction(new TransactionWork<LinkedList<String>>() {
                @Override
                public LinkedList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (a:Anime)<-[:ANIMACION]-(s:Estudio {nombre: $studio}) RETURN a.titulo",
                            parameters("studio", studio));

                    LinkedList<String> animes = new LinkedList<>();
                    while (result.hasNext()) {
                        animes.add(result.next().get("a.titulo").asString());
                    }
                    return animes;
                }
            });
        }
    }

    /**
     * Metodo que devuelve toda la informacion de un anime en especifico
     * @param title el titulo del anime
     * @return un map con la informacion completa del anime
     */
    public Map<String, Object> getAnimeInfo(String title) {
        try ( Session session = driver.session() ) {
            Map<String, Object> animeInfo = session.readTransaction(new TransactionWork<Map<String, Object>>() {
                @Override
                public Map<String, Object> execute(Transaction tx) {
                    Result result = tx.run("MATCH (a:Anime {titulo: $title}) RETURN a",
                            parameters("title", title));
                    Record record = result.single();
                    return record.get("a").asNode().asMap();
                }
            });

            return animeInfo;
        }
    }
}
