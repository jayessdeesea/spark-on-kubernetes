package user.jayessdeesea.main;

import org.apache.spark.sql.SparkSession;
import user.jayessdeesea.query.Query;

import java.util.function.Consumer;

public class Main {

    public static void main(String[] args) {

        final SparkSession.Builder builder = SparkSession.builder() //
                .appName("Main"); //

        try (final SparkSession sparkSession = builder.getOrCreate()) {

            final Consumer<SparkSession> query = new Query();

            query.accept(sparkSession);
        }

        System.out.println("Hello World!");
    }
}
