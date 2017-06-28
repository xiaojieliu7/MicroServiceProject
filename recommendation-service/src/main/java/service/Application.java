package service;

import librec.data.DataDAO;
import librec.data.Movie;
import librec.data.SimiMovie;
import librec.data.SparseMatrix;
import librec.main.LibRec;
import librec.util.FileIO;
import librec.util.Lists;
import librec.util.Sims;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.config.GraphDatabaseConfiguration;
import service.data.domain.entity.Product;
import service.data.domain.entity.User;

import javax.annotation.PostConstruct;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static librec.intf.Recommender.rateDao;

@SpringBootApplication
@ComponentScan({ "service.data", "service.config", "librec"})
@EnableZuulProxy
@Slf4j
@RestController
public class Application {

    private static SparseMatrix trainMatrix;
    private static DataDAO dataDAO;
    final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    RepositoryRestMvcConfiguration restConfiguration;

    // Used to bootstrap the Neo4j database with demo data
    @Value("${aws.s3.url}")
    String datasetUrl;

    @Value("${neo4j.bootstrap}")
    Boolean bootstrap;

    public static void main(String[] args) throws Exception {

        // run algorithm
        LibRec librec = new LibRec();
        librec.setConfigFiles("static/demo/config/WRMF.conf");
        trainMatrix = librec.execute(args);
        dataDAO = librec.getRateDao();
//        librec.run();

        System.setProperty("org.neo4j.rest.read_timeout", "250");
        SpringApplication.run(Application.class, args);

    }
    @RequestMapping(value = "/getSimilarMovies", method = RequestMethod.GET)
    public String getSimilarMovies(@RequestParam(value = "id") String id)throws Exception{
        int numTopNRanks = 10;
        int itemIdx = rateDao.getItemId(id);
        String str = "";
        List<Map.Entry<Integer, Double>> itemScores = new ArrayList<>();
        List<Integer> rankedItems = new ArrayList<>(itemScores.size());
        for (int u = 0,um = trainMatrix.numColumns();u<um;u++){
            if(u!=itemIdx){
                double score = Sims.jaccard(trainMatrix.getColumns(u),trainMatrix.getColumns(itemIdx));
                itemScores.add(new AbstractMap.SimpleImmutableEntry<Integer,Double>(u,score));
            }
        }
        itemScores = Lists.sortListTopK(itemScores, true, numTopNRanks);
        for (Map.Entry<Integer, Double> kv : itemScores) {
            Integer item = kv.getKey();
            rankedItems.add(item);
        }
        int i;
        for (i = 0; i < rankedItems.size()-1 && i<9; i++) {

            str+=rateDao.getItemId(rankedItems.get(i))+",";
        }
        str+=rateDao.getItemId(rankedItems.get(i));
        return str;
    }

    @PostConstruct
    public void postConstructConfiguration() {
        // Expose ids for the domain entities having repositories
        logger.info("Exposing IDs on repositories...");
        restConfiguration.config().exposeIdsFor(User.class);
        restConfiguration.config().exposeIdsFor(Product.class);

        // Register the ObjectMapper module for properly rendering HATEOAS REST repositories
        logger.info("Registering Jackson2HalModule...");
        restConfiguration.objectMapper().registerModule(new Jackson2HalModule());
    }

    /**
     * Bootstrap the Neo4j database with demo dataset. This can run multiple times without
     * duplicating data.
     *
     * @param graphDatabaseConfiguration is the graph database configuration to communicate with the Neo4j server
     * @return a {@link CommandLineRunner} instance with the method delegate to execute
     */
    @Bean
    public CommandLineRunner commandLineRunner(GraphDatabaseConfiguration graphDatabaseConfiguration) {
        //write the topk.csv to here.
        return strings -> {
            if(bootstrap) {
                logger.info("Creating index on User(id) and Product(id)...");
                graphDatabaseConfiguration.neo4jTemplate().query("CREATE INDEX ON :User(id)", null).finish();
                graphDatabaseConfiguration.neo4jTemplate().query("CREATE INDEX ON :Product(id)", null).finish();
                logger.info("Importing ratings data...");

                // Import graph data for movie ratings
                String userImport = String.format("USING PERIODIC COMMIT 20000\n" +
                        "LOAD CSV WITH HEADERS FROM \"%s/result.csv\" AS csvLine\n" +
                        "MERGE (user:User:_User { id: toInt(csvLine.userId) })\n" +
                        "ON CREATE SET user.__type__=\"User\", user.className=\"data.domain.nodes.User\", user.knownId = csvLine.userId\n" +
                        "MERGE (product:Product:_Product { id: toInt(csvLine.movieId) })\n" +
                        "ON CREATE SET product.__type__=\"Product\", product.className=\"data.domain.nodes.Product\", product.knownId = csvLine.movieId\n" +
                        "MERGE (user)-[r:Rating]->(product)\n" +
                        "ON CREATE SET r.rating = toInt(csvLine.rating), r.knownId = csvLine.userId + \"_\" + csvLine.movieId, r.__type__ = \"Rating\", r.className = \"data.domain.rels.Rating\"",
                        datasetUrl);

                graphDatabaseConfiguration.neo4jTemplate().query(userImport, null).finish();
                logger.info("Import complete");
            }
        };
    }
}
