# SENG302 Team 100 - Everyware
See the User Manual inside the project Wiki for how to setup the the application to run.

## Dependencies

Everyware's Odyssey requires the following dependencies to run

#### Operating system
Odyssey may be run on Windows or Linux/OSX. The build instructions for each operating system are explained below.

#### Programs
* [sbt 1.2.8](https://www.scala-sbt.org/download.html)
* [JRE/JDK 1.8](https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

## Build instructions (Windows/Linux)
1. Clone the repository by running `git clone https://eng-git.canterbury.ac.nz/seng302-2019/team-100.git`
2. Enter the cloned git repository using `cd team-100`
3. Run `sbt dist` (Note: internet must be enabled).
4. Navigate to `team-100-sprint_*/target/universal`
5. Extract the `seng302-team-100-everyware-0.0.1-SNAPSHOT.zip` into your desired install location.

## Run instructions

#### Windows
1. Navigate into the newly extracted `${INSTALL_LOCATION}/seng302-team-100-everyware-0.0.1-SNAPSHOT/bin` folder
2. Execute the `seng302-team-100-everyware.bat` file

#### Linux
1. Navigate into the newly extracted `${INSTALL_LOCATION}/seng302-team-100-everyware-0.0.1-SNAPSHOT/bin` folder
2. Open the folder in the terminal and type `chmod +x seng302-team-100-everyware`. This enables the file to be executable. 
3. Run the application by executing `./seng302-team-100-everyware` in the terminal.

Congratulations, Odyssey should now be running!
Your browser should open onto the Odyssey website, but if it does not navigate to `localhost:8080`.  
You can now Create a Profile or Login.  

To login as an admin user use username `admin@travelea.com` and password `1nimda`.  
To login as a regular user use username `guestUser@travelea.com` and password `guest123`.

### Other documentation

`Due to this repository originaly having been hosted on GitLab, the link no longer works since migration to GitHub`

Click [here](https://eng-git.canterbury.ac.nz/seng302-2019/team-100/wikis/Product/User-Manual) for a comprehensive user manual. 

### Reference
* [Play Documentation](https://playframework.com/documentation/latest/Home)
* [EBean](https://www.playframework.com/documentation/latest/JavaEbean) is a Java ORM library that uses SQL.The documentation can be found [here](https://ebean-orm.github.io/).
* For Java Forms, Please see [here](<https://playframework.com/documentation/latest/JavaForms>).

### Licencing
All external licences can be found in the `/doc` directory in the repo or on our Wiki.
