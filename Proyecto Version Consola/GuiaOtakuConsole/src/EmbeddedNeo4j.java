import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.summary.ResultSummary;

import static org.neo4j.driver.Values.parameters;

import java.util.LinkedList;
import java.util.List;
/**
 * Clase que tiene los metodos necesarios para conectarse e interactuar con una base de datos en Neo4j
 * @author diego leiva, pablo orellana
 * Referencia: Malonso-UVG
 */
public class EmbeddedNeo4j implements AutoCloseable{

    private final Driver driver;
    

    public EmbeddedNeo4j( String uri, String user, String password )
    {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @Override
    public void close() throws Exception
    {
        driver.close();
    }

    public LinkedList<String> getGenres() {
        try ( Session session = driver.session() ) {
            LinkedList<String> genres = session.readTransaction( new TransactionWork<LinkedList<String>>() {
                @Override
                public LinkedList<String> execute( Transaction tx ) {
                    Result result = tx.run( "MATCH (g:Genero) RETURN g.nombre");
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

    public LinkedList<String> getStudios() {
        try ( Session session = driver.session() ) {
            LinkedList<String> studios = session.readTransaction( new TransactionWork<LinkedList<String>>() {
                @Override
                public LinkedList<String> execute( Transaction tx ) {
                    Result result = tx.run( "MATCH (e:Estudio) RETURN e.nombre");
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
    public void login(String username, String password) {
        if (checkCredentials(username, password)) {
            Usuario currentUser = new Usuario();
            currentUser.setUsername(username);
        } else {
            System.out.println("Usuario o contraseña no validos");
        }
    }


}
