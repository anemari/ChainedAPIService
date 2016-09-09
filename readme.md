Prerequisites:
Java8
Maven3

Go to project root directory

In order to build the project execute:
mvn clean install

In order to run the app execute:
- For Windows: java -cp target\chained.api.service-1.0-SNAPSHOT.jar com.importio.Runner [fileName].csv
- For OSX/Unix java -cp target/chained.api.service-1.0-SNAPSHOT.jar com.importio.Runner [fileName].csv

The output will be available in the output file that will be created in the root of the project if only a fileName was specified, or in the provided full path.

