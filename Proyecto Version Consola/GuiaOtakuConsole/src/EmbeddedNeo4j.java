/**
 * 
 */

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
 * @author Administrator
 *
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

}
