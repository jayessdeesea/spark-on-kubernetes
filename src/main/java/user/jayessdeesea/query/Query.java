package user.jayessdeesea.query;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Sample Query implementation to test Spark on Kubernetes setup.
 * This class creates a simple dataset and performs basic transformations
 * to verify the Spark cluster is working correctly.
 */
public class Query implements Consumer<SparkSession> {
    private static final Logger logger = LoggerFactory.getLogger(Query.class);

    @Override
    public void accept(final SparkSession sparkSession) {
        try {
            logger.info("Starting sample query execution");

            // Create sample data
            List<String> data = Arrays.asList("Spark", "Kubernetes", "Java", "Scala", "Python");
            
            // Convert to Dataset
            Dataset<String> dataset = sparkSession.createDataset(data, sparkSession.encoder(String.class));
            
            // Perform some transformations
            Dataset<Row> result = dataset
                .map(str -> str.toLowerCase(), sparkSession.encoder(String.class))
                .toDF("word")
                .filter(row -> row.getString(0).length() > 4);

            // Show results
            logger.info("Query results:");
            result.show();

            // Get execution plan
            logger.info("Execution plan:");
            result.explain();

            logger.info("Query execution completed successfully");
        } catch (Exception e) {
            logger.error("Error executing query", e);
            throw new RuntimeException("Query execution failed", e);
        }
    }
}
