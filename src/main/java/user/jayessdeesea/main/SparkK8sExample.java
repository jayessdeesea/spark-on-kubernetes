package user.jayessdeesea.main;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import static org.apache.spark.sql.functions.*;

public class SparkK8sExample {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
            .appName("Spark K8s Example")
            .getOrCreate();

        try {
            // Read JSON from input volume
            Dataset<Row> people = spark.read()
                .option("multiline", "true")
                .json("/mnt/input/people.json");

            // Show the original data
            System.out.println("Original Data:");
            people.show();

            // Process data
            Dataset<Row> processedPeople = people
                .withColumn("ageGroup", 
                    when(col("age").lt(30), "Young")
                    .when(col("age").lt(50), "Middle")
                    .otherwise("Senior"))
                .withColumn("timestamp", current_timestamp());

            // Show the processed data
            System.out.println("Processed Data:");
            processedPeople.show();

            // Write results to output volume
            processedPeople
                .coalesce(1)
                .write()
                .mode("overwrite")
                .json("/mnt/output/processed_people");

            // Print summary
            System.out.println("Data processing complete!");
            System.out.println("Age distribution:");
            processedPeople.groupBy("ageGroup")
                .count()
                .orderBy("ageGroup")
                .show();

        } finally {
            spark.stop();
        }
    }
}
