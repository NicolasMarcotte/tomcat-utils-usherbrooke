# tomcat-utils-usherbrooke
This library instruments a tomcat 7+ with http://metrics.dropwizard.io/3.1.0/

To use it:

1. enable jmx
2. add this project jar to tomcat lib folder
3. add the following line to server.xml: &lt;Valve className="ca.usherbrooke.sti.si.monitoring.RequestValve"/&gt; before the closing Host tag


Known limitations:
Async request are gnored


