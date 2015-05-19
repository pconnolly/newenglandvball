This code will read data from advancedeventsystems (aes), the website used by usavolleyball for event scheduling and results, and will reformat the data into a readable schedule for the teams specified.  The aes system doesn't provide a way to show multiple teams' schedules concurrently.  Within aes it is very cumbersome to navigate to several dozen team's pages to determine when and where matches will be taking place.  

This program will take data from the aes website and reformat it and output the upcoming schedule as an html file.  It is expected that this html file will be placed in a publicly accessible html folder.

##Prerequisites
JDK 1.6 or greater

Maven must be installed (https://maven.apache.org)

##How to build
mvn install

##How to run
After you get the build running you can run it like this for local testing:
> java -cp target/gamelist-1.0-SNAPSHOT-jar-with-dependencies.jar com.newenglandvball.App gamelist.properties

This will create a data folder with a file index.html.  You can then view this file in a browser.  

To run in production, change the properties file to 
> java -cp target/gamelist-1.0-SNAPSHOT-jar-with-dependencies.jar com.newenglandvball.App gamelist.properties.prod

This properties file has slightly different values, such as outputting to apache http server's default html folder and also showing matches for a shorter duration after they complete so it doesn't clutter the page.

Typically this command would be run on a cron job every 15 minutes such as:
> */15 * * * * java -cp ~ec2-user/gamelist-1.0-SNAPSHOT-jar-with-dependencies.jar com.newenglandvball.App ~ec2-user/gamelist.properties

##Future work
The UI could certainly use some work as its just simple html written by hand with no css or js.  
