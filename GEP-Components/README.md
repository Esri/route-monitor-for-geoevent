# Route Monitor GeoEvent Extension server components

Route Monitor ArcGIS 10.2.1 GeoEvent Extension server components

![App](gep-components.png?raw=true)

## Building the source code:
1. Download the source code.
2. Make sure Maven and Java SDK are installed on your machine.
3. Create a new distribution folder that will contain all compiled jar files.  For example, create a folder c:\routemonitor\jars
4. Run 'mvn clean install -Ddistribution.directory=c:\routemonitor\jars' in the GEP-components folder
5. When maven finishes compiling the jar files, all jar files and the parent pom files are copied to the distribution folder.  If you don't specify a distribution folder when running maven, you will have to find the jar files in the target folder of each indivicual project.

## Installing the built jar files:
1. Copy the *.jar files under the distribution folder into the [ArcGIS-GeoEvent-Extension-Install-Directory]/deploy folder.
2. Follow the Route Monitor Installation Guide (downloadable from Gallery) to set up the server components of the Route Monitor.

## Requirements
* ArcGIS Server version 10.2.1
* ArcGIS GeoEvent Extension for Server version 10.2.1.
* ArcGIS GeoEvent Extension SDK version 10.2.1.
* Java JDK 1.7 or greater.
* Apache Maven 3.0.3 and above.
