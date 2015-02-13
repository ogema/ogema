*****************************************************************
*								*
*	      OGEMA 2.0.1 Installation Instruction		*
*								*
*****************************************************************

-------------
Prerequisites
-------------

- Git (optional)
- Maven 3 or higher
- Java 7 or higher

------------
Quick Start 
------------

1) To compile OGEMA from the sources go to the src directory and execute:
	- mvn clean install -DskipTests
	- mvn test

2) Get the run directory from our demokit (unfortunately not included in our GitHub yet) -> http://www.ogema.org/

3) Go to the demokit rundirectory and simply execute the start.sh (for bash compatible shells) script or the start.cmd (Windows shell)

4) Access the system from within your webbrowser (e.g. Internet Explorer, Firefox, Chrome, etc.): https://<IP>:8443/ogema/index.html

Further information can be found on our Wiki: https://www.ogema-source.net/wiki/display/OGEMA/OGEMA+Home

The API docs can be found here: https://www.ogema-source.net/apidocs/
