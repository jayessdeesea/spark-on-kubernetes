# Spark on Kubernetes Helm Chart

This Helm chart deploys Apache Spark on Kubernetes with the following components:
- Spark on Kubernetes (using native Kubernetes scheduler)
- Spark History Server
- Native Kubernetes debugging support
- Persistent storage for logs and work directories

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- PV provisioner support in the underlying infrastructure

## Installation

1. Create required storage directories:
```bash
mkdir -p C:\kubernetes\storage\spark-work
mkdir -p C:\kubernetes\storage\spark-input
mkdir -p C:\kubernetes\storage\spark-output
```

2. Add sample data to input directory:
```bash
echo "1,John,Doe,30" > C:\kubernetes\storage\spark-input\users.csv
echo "2,Jane,Smith,25" >> C:\kubernetes\storage\spark-input\users.csv
echo "3,Bob,Johnson,35" >> C:\kubernetes\storage\spark-input\users.csv
```

3. Install the chart:
```bash
helm install spark .
```

To install with custom values:
```bash
helm install spark . -f custom-values.yaml
```

## Configuration

The following table lists the configurable parameters and their default values.

### Global Settings

| Parameter | Description | Default |
|-----------|-------------|---------|
| `namespace` | Kubernetes namespace | `spark-namespace` |
| `spark.version` | Spark version | `3.5.0` |
| `spark.image.repository` | Spark image repository | `docker.io/apache/spark` |
| `spark.image.tag` | Spark image tag | `3.5.0` |
| `spark.image.pullPolicy` | Image pull policy | `IfNotPresent` |

### Storage Settings

| Parameter | Description | Default |
|-----------|-------------|---------|
| `storage.className` | Storage class name | `local-storage` |
| `storage.size` | Storage size | `2Gi` |
| `storage.path` | Local path for storage | `\\run\\desktop\\mnt\\host\\c\\kubernetes\\storage\\spark-work` |

### Spark on Kubernetes Settings

| Parameter | Description | Default |
|-----------|-------------|---------|
| `sparkOnK8s.enabled` | Enable Spark on K8s | `true` |
| `sparkOnK8s.driver.cores` | Driver cores | `1` |
| `sparkOnK8s.driver.memory` | Driver memory | `1G` |
| `sparkOnK8s.executor.instances` | Number of executors | `2` |
| `sparkOnK8s.executor.cores` | Cores per executor | `1` |
| `sparkOnK8s.executor.memory` | Memory per executor | `1G` |

### History Server Settings

| Parameter | Description | Default |
|-----------|-------------|---------|
| `historyServer.enabled` | Enable history server | `true` |
| `historyServer.service.type` | Service type | `LoadBalancer` |
| `historyServer.service.port` | Service port | `18080` |

## Usage

### Accessing the Spark UI

The Spark UI is available at:
- History Server: `http://localhost:18080`
- Driver UI: Available through Kubernetes port-forward
- Executor UIs: Available through Kubernetes port-forward

### Using with IntelliJ IDEA

1. **Project Setup**:
   - Create a new Java project in IntelliJ IDEA
   - Add the following dependencies to your `pom.xml`:
     ```xml
     <dependencies>
         <dependency>
             <groupId>org.apache.spark</groupId>
             <artifactId>spark-core_2.12</artifactId>
             <version>3.5.0</version>
         </dependency>
         <dependency>
             <groupId>org.apache.spark</groupId>
             <artifactId>spark-sql_2.12</artifactId>
             <version>3.5.0</version>
         </dependency>
         <!-- For Kubernetes support -->
         <dependency>
             <groupId>org.apache.spark</groupId>
             <artifactId>spark-kubernetes_2.12</artifactId>
             <version>3.5.0</version>
         </dependency>
     </dependencies>
     ```

2. **Create a Spark Application**:
   ```java
   import org.apache.spark.sql.SparkSession;
   import org.apache.spark.sql.Dataset;
   import org.apache.spark.sql.Row;
   import static org.apache.spark.sql.functions.*;
   
   public class SparkK8sApp {
       public static void main(String[] args) {
           SparkSession spark = SparkSession.builder()
               .appName("Spark on K8s App")
               .master("k8s://https://kubernetes.docker.internal:6443")
               .config("spark.kubernetes.container.image", "docker.io/apache/spark:3.5.0")
               .config("spark.kubernetes.namespace", "spark-namespace")
               .config("spark.kubernetes.authenticate.driver.serviceAccountName", "spark-service-account")
               // Event logging configuration
               .config("spark.eventLog.enabled", "true")
               .config("spark.eventLog.dir", "/mnt/spark-logs")
               .config("spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-logs.options.claimName", "spark-work-dir-claim")
               .config("spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-logs.mount.path", "/mnt/spark-logs")
               .config("spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-logs.options.claimName", "spark-work-dir-claim")
               .config("spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-logs.mount.path", "/mnt/spark-logs")
               // Input volume configuration
               .config("spark.kubernetes.driver.volumes.persistentVolumeClaim.input.options.claimName", "spark-input-claim")
               .config("spark.kubernetes.driver.volumes.persistentVolumeClaim.input.mount.path", "/mnt/input")
               .config("spark.kubernetes.driver.volumes.persistentVolumeClaim.input.mount.readOnly", "true")
               .config("spark.kubernetes.executor.volumes.persistentVolumeClaim.input.options.claimName", "spark-input-claim")
               .config("spark.kubernetes.executor.volumes.persistentVolumeClaim.input.mount.path", "/mnt/input")
               .config("spark.kubernetes.executor.volumes.persistentVolumeClaim.input.mount.readOnly", "true")
               // Output volume configuration
               .config("spark.kubernetes.driver.volumes.persistentVolumeClaim.output.options.claimName", "spark-output-claim")
               .config("spark.kubernetes.driver.volumes.persistentVolumeClaim.output.mount.path", "/mnt/output")
               .config("spark.kubernetes.executor.volumes.persistentVolumeClaim.output.options.claimName", "spark-output-claim")
               .config("spark.kubernetes.executor.volumes.persistentVolumeClaim.output.mount.path", "/mnt/output")
               .getOrCreate();

           try {
               // Read input data
               Dataset<Row> users = spark.read()
                   .option("header", "false")
                   .option("inferSchema", "true")
                   .csv("/mnt/input/users.csv")
                   .toDF("id", "firstName", "lastName", "age");

               // Process data
               Dataset<Row> processedUsers = users
                   .withColumn("fullName", concat(col("firstName"), lit(" "), col("lastName")))
                   .withColumn("ageGroup", 
                       when(col("age").lt(30), "Young")
                       .when(col("age").lt(50), "Middle")
                       .otherwise("Senior"))
                   .select("id", "fullName", "age", "ageGroup");

               // Write results
               processedUsers
                   .coalesce(1) // Write to a single file
                   .write()
                   .mode("overwrite")
                   .option("header", "true")
                   .csv("/mnt/output/processed_users");

               // Print summary
               System.out.println("Data processing complete!");
               System.out.println("Age distribution:");
               processedUsers.groupBy("ageGroup")
                   .count()
                   .show();
           } finally {
               spark.stop();
           }
       }
   }
   ```

3. **Run Configuration**:
   - Right-click your main class and select "Create Run Configuration"
   - Set the following environment variables:
     ```
     SPARK_MASTER=k8s://https://kubernetes.docker.internal:6443
     SPARK_DRIVER_MEMORY=1g
     SPARK_EXECUTOR_MEMORY=1g
     ```
   - In VM options, add:
     ```
     -Dspark.driver.host=host.docker.internal
     ```

4. **Run the Application**:
   - Click the Run button or use Shift+F10
   - The application will be submitted to Kubernetes
   - Monitor the application:
     - View logs in IntelliJ's Run window
     - Check pod status: `kubectl get pods -n spark-namespace`
     - Access the History Server UI: http://localhost:18080

5. **View Results**:
   - Check the output directory:
     ```bash
     cat C:\kubernetes\storage\spark-output\processed_users\*.csv
     ```

6. **Debugging**:
   - Set breakpoints in your code
   - Use Debug instead of Run (Shift+F9)
   - IntelliJ will automatically attach to the driver pod
   - Use the Debug window to step through code

### Testing the Setup

The chart includes a test Spark Pi application that you can use to verify the setup:

1. Apply the test job:
```bash
kubectl apply -f test-spark-pi.yaml
```

2. Monitor the job progress:
```bash
kubectl get pods -n spark-namespace
```

You should see:
- A driver pod (spark-pi-xxxxx-driver)
- Two executor pods (spark-pi-xxxxx-exec-1 and spark-pi-xxxxx-exec-2)

3. Check the job results:
```bash
kubectl logs spark-pi-xxxxx-driver -n spark-namespace
```

4. View the job in History Server:
Open `http://localhost:18080` in your browser to see the completed Spark application.

### Debugging

Access Spark applications using Kubernetes native tools:
```bash
# View application logs
kubectl logs <pod-name> -n spark-namespace

# Execute commands in pods
kubectl exec -it <pod-name> -n spark-namespace -- /bin/bash

# Port forward to access UIs
kubectl port-forward <pod-name> 4040:4040 -n spark-namespace
```

## Uninstallation

To uninstall the chart:
```bash
helm uninstall spark
```

## Development

To modify the chart:
1. Update values in `values.yaml`
2. Update templates in `templates/`
3. Upgrade the deployment:
```bash
helm upgrade spark .
