package user.jayessdeesea.main;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import user.jayessdeesea.query.Query;

import java.util.function.Consumer;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        try {
            SparkConf conf = new SparkConf()
                .setAppName("SparkKubernetesApp")
                .setMaster("k8s://https://kubernetes.default.svc:443")
                .set("spark.kubernetes.namespace", "spark-namespace")
                .set("spark.kubernetes.authenticate.driver.serviceAccountName", "spark-service-account")
                .set("spark.eventLog.enabled", "true")
                .set("spark.eventLog.dir", "file:///mnt/spark-logs")
                .set("spark.history.fs.logDirectory", "file:///mnt/spark-logs")
                .set("spark.kubernetes.container.image", "docker.io/apache/spark:3.5.0")
                .set("spark.kubernetes.submission.waitAppCompletion", "false")
                .set("spark.kubernetes.authenticate.submission.caCertFile", "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt")
                .set("spark.kubernetes.authenticate.submission.oauthTokenFile", "/var/run/secrets/kubernetes.io/serviceaccount/token")
                .set("spark.kubernetes.authenticate.driver.serviceAccountName", "spark-service-account")
                .set("spark.kubernetes.driver.pod.name", "spark-driver")
                .set("spark.kubernetes.context", "spark-context")
                .set("spark.executor.instances", "2")
                .set("spark.executor.memory", "1g")
                .set("spark.executor.cores", "1")
                .set("spark.driver.memory", "1g")
                .set("spark.driver.cores", "1");

            logger.info("Initializing Spark session...");
            
            try (SparkSession sparkSession = SparkSession.builder()
                    .config(conf)
                    .getOrCreate()) {
                
                logger.info("Spark session initialized successfully");
                
                final Consumer<SparkSession> query = new Query();
                query.accept(sparkSession);
                
                logger.info("Query execution completed successfully");
            }
        } catch (Exception e) {
            logger.error("Error executing Spark application", e);
            System.exit(1);
        }
    }
}
