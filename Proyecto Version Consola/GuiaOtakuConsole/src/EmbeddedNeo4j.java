import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import static org.neo4j.driver.Values.parameters;

import java.util.*;

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

    /**
     * Metodo que verifica si el usuario ya tiene intereses por generos o estudios
     * @param username el nombre del usuario actual
     * @return verdadero si ya hay intereses, falso si no
     */
    public boolean userHasInterests(String username) {
        try ( Session session = driver.session() ) {
            boolean hasInterests = session.readTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: $username})-[:INTERESADO]->() RETURN COUNT(*) AS relCount",
                            parameters("username", username));
                    return result.single().get("relCount").asInt() > 0;
                }
            });
            return hasInterests;
        }
    }

    /**
     * Metodo para recopilar los generos de anime en los que el usuario actual esta interesado
     * @param username el usuario actual
     * @return listado de los generos de anime en los que el usuario esta interesado
     */
    public LinkedList<String> getUserInterestsGenres(String username) {
        try ( Session session = driver.session() ) {
            LinkedList<String> interestsGenres = session.readTransaction(new TransactionWork<LinkedList<String>>() {
                @Override
                public LinkedList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: $username})-[r:INTERESADO]->(g:Genero) RETURN g.nombre AS genreName",
                            parameters("username", username));

                    LinkedList<String> genres = new LinkedList<>();
                    while (result.hasNext()) {
                        Record record = result.next();
                        genres.add(record.get("genreName").asString());
                    }

                    return genres;
                }
            });

            return interestsGenres;
        }
    }

    /**
     * Metodo para recopilar los estudios de animacion en los que el usuario actual esta interesado
     * @param username el usuario actual
     * @return listado de los estudios de animacion en los que el usuario esta interesado
     */
    public LinkedList<String> getUserInterestsStudios(String username) {
        try ( Session session = driver.session() ) {
            LinkedList<String> interestsStudios = session.readTransaction(new TransactionWork<LinkedList<String>>() {
                @Override
                public LinkedList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: $username})-[r:INTERESADO]->(s:Estudio) RETURN s.nombre AS studioName",
                            parameters("username", username));

                    LinkedList<String> studios = new LinkedList<>();
                    while (result.hasNext()) {
                        Record record = result.next();
                        studios.add(record.get("studioName").asString());
                    }

                    return studios;
                }
            });

            return interestsStudios;
        }
    }

    /**
     * Metodo que crea las relaciones de interes del usuario
     * @param username el usuario actual
     * @param genres los generos seleccionados
     * @param studios los estudios seleccionados
     */
    public void createInterests(String username, List<String> genres, List<String> studios) {
        try (Session session = driver.session()) {
            session.writeTransaction(new TransactionWork<Void>() {
                @Override
                public Void execute(Transaction tx) {
                    for (String genre : genres) {
                        tx.run("MATCH (u:Usuario {username: $username}), (g:Genero {nombre: $genre}) " +
                                        "MERGE (u)-[:INTERESADO]->(g)",
                                parameters("username", username, "genre", genre));
                    }
                    for (String studio : studios) {
                        tx.run("MATCH (u:Usuario {username: $username}), (s:Estudio {nombre: $studio}) " +
                                        "MERGE (u)-[:INTERESADO]->(s)",
                                parameters("username", username, "studio", studio));
                    }
                    return null;
                }
            });
        }
    }

    /**
     * Metodo que remueve todos los intereses actuales del usuario para que pueda ver otras recomendaciones
     * @param username el usuario actual
     */
    public void resetInterests(String username) {
        try (Session session = driver.session()) {
            session.writeTransaction(new TransactionWork<Void>() {
                @Override
                public Void execute(Transaction tx) {
                    tx.run("MATCH (u:Usuario {username: $username})-[r]-() DELETE r",
                            parameters("username", username));
                    return null;
                }
            });
        }
    }

    /**
     * Obtiene todos los animes que pertenecen a los 3 generos que el usuario esta interesado,
     * y que pertenecen a alguno de los estudios que el usuario esta interesado
     * @return una lista con los nombres de los animes de la base de datos
     */
    public ArrayList<String> getAnimesBy3Genres1Studio() {
        try (Session session = driver.session()) {
            ArrayList<String> animes = session.readTransaction(new TransactionWork<ArrayList<String>>() {
                @Override
                public ArrayList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: \"puxter\"})-[:INTERESADO]->(g:Genero), " +
                            "(u)-[:INTERESADO]->(e:Estudio), " +
                            "(a:Anime)-[:PERTENECE]->(g), " +
                            "(e)-[:ANIMACION]->(a) " +
                            "WITH a, COUNT(DISTINCT g) AS generosCount " +
                            "WHERE generosCount = 3 " +
                            "RETURN a.titulo");
                    ArrayList<String> myAnimes = new ArrayList<>();
                    List<Record> registros = result.list();
                    for (Record registro : registros) {
                        myAnimes.add(registro.get("a.titulo").asString());
                    }
                    return myAnimes;
                }
            });
            return animes;
        }
    }

    /**
     * Obtiene todos los animes que pertenecen a los 3 generos que el usuario esta interesado,
     * @return una lista con los nombres de los animes de la base de datos
     */
    public ArrayList<String> getAnimesBy3Genres() {
        try (Session session = driver.session()) {
            ArrayList<String> animes = session.readTransaction(new TransactionWork<ArrayList<String>>() {
                @Override
                public ArrayList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: \"puxter\"})-[:INTERESADO]->(g:Genero), " +
                            "(a:Anime)-[:PERTENECE]->(g) " +
                            "WITH a, COUNT(DISTINCT g) AS generosCount " +
                            "WHERE generosCount = 3 " +
                            "RETURN a.titulo");
                    ArrayList<String> myAnimes = new ArrayList<>();
                    List<Record> registros = result.list();
                    for (Record registro : registros) {
                        myAnimes.add(registro.get("a.titulo").asString());
                    }
                    return myAnimes;
                }
            });
            return animes;
        }
    }

    /**
     * Obtiene todos los animes que pertenecen a 2 de los 3 generos que el usuario esta interesado,
     * y que pertenecen a alguno de los estudios que el usuario esta interesado
     * @return una lista con los nombres de los animes de la base de datos
     */
    public ArrayList<String> getAnimesBy2Genre1Studio() {
        try (Session session = driver.session()) {
            ArrayList<String> animes = session.readTransaction(new TransactionWork<ArrayList<String>>() {
                @Override
                public ArrayList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: \"puxter\"})-[:INTERESADO]->(g:Genero), " +
                            "(u)-[:INTERESADO]->(e:Estudio), " +
                            "(a:Anime)-[:PERTENECE]->(g), " +
                            "(e)-[:ANIMACION]->(a) " +
                            "WITH a, COUNT(DISTINCT g) AS generosCount " +
                            "WHERE generosCount = 2 " +
                            "RETURN a.titulo");
                    ArrayList<String> myAnimes = new ArrayList<>();
                    List<Record> registros = result.list();
                    for (Record registro : registros) {
                        myAnimes.add(registro.get("a.titulo").asString());
                    }
                    return myAnimes;
                }
            });
            return animes;
        }
    }

    /**
     * Obtiene todos los animes que pertenecen a 2 los 3 generos que el usuario esta interesado,
     * @return una lista con los nombres de los animes de la base de datos
     */
    public ArrayList<String> getAnimesBy2Genres() {
        try (Session session = driver.session()) {
            ArrayList<String> animes = session.readTransaction(new TransactionWork<ArrayList<String>>() {
                @Override
                public ArrayList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: \"puxter\"})-[:INTERESADO]->(g:Genero), " +
                            "(a:Anime)-[:PERTENECE]->(g) " +
                            "WITH a, COUNT(DISTINCT g) AS generosCount " +
                            "WHERE generosCount = 2 " +
                            "RETURN a.titulo");
                    ArrayList<String> myAnimes = new ArrayList<>();
                    List<Record> registros = result.list();
                    for (Record registro : registros) {
                        myAnimes.add(registro.get("a.titulo").asString());
                    }
                    return myAnimes;
                }
            });
            return animes;
        }
    }

    /**
     * Obtiene todos los animes que pertenecen a 1 de los 3 generos que el usuario esta interesado,
     * y que pertenecen a alguno de los estudios que el usuario esta interesado
     *
     * @return una lista con los nombres de los animes de la base de datos
     */
    public ArrayList<String> getAnimesBy1Genre1Studio() {
        try (Session session = driver.session()) {
            ArrayList<String> animes = session.readTransaction(new TransactionWork<ArrayList<String>>() {
                @Override
                public ArrayList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: \"puxter\"})-[:INTERESADO]->(g:Genero), " +
                            "(u)-[:INTERESADO]->(e:Estudio), " +
                            "(a:Anime)-[:PERTENECE]->(g), " +
                            "(e)-[:ANIMACION]->(a) " +
                            "WITH a, COUNT(DISTINCT g) AS generosCount " +
                            "WHERE generosCount = 1 " +
                            "RETURN a.titulo");
                    ArrayList<String> myAnimes = new ArrayList<>();
                    List<Record> registros = result.list();
                    for (Record registro : registros) {
                        myAnimes.add(registro.get("a.titulo").asString());
                    }
                    return myAnimes;
                }
            });
            return animes;
        }
    }

    /**
     * Obtiene todos los animes que pertenecen a 1 de los 3 generos que el usuario esta interesado,
     * @return una lista con los nombres de los animes de la base de datos
     */
    public ArrayList<String> getAnimesBy1Genre() {
        try (Session session = driver.session()) {
            ArrayList<String> animes = session.readTransaction(new TransactionWork<ArrayList<String>>() {
                @Override
                public ArrayList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: \"puxter\"})-[:INTERESADO]->(g:Genero), " +
                            "(a:Anime)-[:PERTENECE]->(g) " +
                            "WITH a, COUNT(DISTINCT g) AS generosCount " +
                            "WHERE generosCount = 1 " +
                            "RETURN a.titulo");
                    ArrayList<String> myAnimes = new ArrayList<>();
                    List<Record> registros = result.list();
                    for (Record registro : registros) {
                        myAnimes.add(registro.get("a.titulo").asString());
                    }
                    return myAnimes;
                }
            });
            return animes;
        }
    }


    /**
     * Obtiene todos los animes que pertenecen a alguno de los estudios que el usuario esta interesado
     * @return una lista con los nombres de los animes de la base de datos
     */
    public ArrayList<String> getAnimesBy1Studio() {
        try (Session session = driver.session()) {
            ArrayList<String> animes = session.readTransaction(new TransactionWork<ArrayList<String>>() {
                @Override
                public ArrayList<String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: \"puxter\"})-[:INTERESADO]->(e:Estudio), " +
                            "(e)-[:ANIMACION]->(a:Anime) " +
                            "RETURN a.titulo");
                    ArrayList<String> myAnimes = new ArrayList<>();
                    List<Record> registros = result.list();
                    for (Record registro : registros) {
                        myAnimes.add(registro.get("a.titulo").asString());
                    }
                    return myAnimes;
                }
            });
            return animes;
        }
    }

    /**
     * Crea una "mega lista" de animes que cumplan con ciertos criterios de interés para el usuario.
     * @return una lista con los nombres de los animes de la base de datos
     */
    public ArrayList<String> createMegaList() {
        // Inicializar la "mega lista"
        ArrayList<String> megaList = new ArrayList<>();

        // Ejecutar cada método y agregar sus resultados a la "mega lista"
        ArrayList<String> listA = getAnimesBy3Genres1Studio(); // reemplaza esto con el método correcto
        if (!listA.isEmpty()) megaList.addAll(listA);

        ArrayList<String> listD = getAnimesBy3Genres(); // reemplaza esto con el método correcto
        if (!listD.isEmpty()) megaList.addAll(listD);

        ArrayList<String> listB = getAnimesBy2Genre1Studio(); // reemplaza esto con el método correcto
        if (!listB.isEmpty()) megaList.addAll(listB);

        ArrayList<String> listE = getAnimesBy2Genres(); // reemplaza esto con el método correcto
        if (!listE.isEmpty()) megaList.addAll(listE);

        ArrayList<String> listC = getAnimesBy1Genre1Studio(); // reemplaza esto con el método correcto
        if (!listC.isEmpty()) megaList.addAll(listC);

        ArrayList<String> listF = getAnimesBy1Genre(); // reemplaza esto con el método correcto
        if (!listF.isEmpty()) megaList.addAll(listF);

        ArrayList<String> listG = getAnimesBy1Studio(); // reemplaza esto con el método correcto
        if (!listG.isEmpty()) megaList.addAll(listG);

        // Eliminar duplicados de la "mega lista" manteniendo el orden
        LinkedHashSet<String> hashSet = new LinkedHashSet<>(megaList);
        ArrayList<String> finalList = new ArrayList<>(hashSet);

        return finalList;
    }

    /**
     * Metodo que busca la información del usuario en la base de datos
     * @param username el usuario actual
     * @return la informacion del usuario
     */
    public Map<String, String> getUserInfo(String username) {
        try (Session session = driver.session()) {
            return session.readTransaction(new TransactionWork<Map<String, String>>() {
                @Override
                public Map<String, String> execute(Transaction tx) {
                    Result result = tx.run("MATCH (u:Usuario {username: $username}) RETURN u.username AS username, u.contrasenia AS password, u.nombre AS firstName, u.apellido AS lastName",
                            parameters("username", username));

                    if (result.hasNext()) {
                        Record record = result.next();
                        Map<String, String> userInfo = new HashMap<>();
                        userInfo.put("username", record.get("username").asString());
                        userInfo.put("password", Security.decrypt(record.get("password").asString()));
                        userInfo.put("firstName", Security.decrypt(record.get("firstName").asString()));
                        userInfo.put("lastName", Security.decrypt(record.get("lastName").asString()));
                        return userInfo;
                    } else {
                        return null;
                    }
                }
            });
        }
    }

}
