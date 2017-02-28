/**
 * 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package ca.usherbrooke.sti.si.monitoring;

import com.codahale.metrics.Counter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import static com.codahale.metrics.MetricRegistry.name;
import com.codahale.metrics.Timer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.ServletException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

/**
 * Adapted from com.codahale.metrics.servlet.AbstractInstrumentedFilter
 *  public the metric to jmx under metrics.*
 * <pre>
 * &lt;Host appBase="webapps" autoDeploy="true" name="localhost" unpackWARs="true"&gt;
 *  
 *    
 *    &lt;Valve
 *        className="org.apache.catalina.valves.AccessLogValve"
 *        directory="logs"
 *        pattern="%h %l %u %t &quot;%r&quot; %s %b"
 *        prefix="localhost_access_log" suffix=".txt"/>
 *     &lt;!-- the following line is the important one -->
 *     &lt;Valve className="ca.usherbrooke.sti.si.monitoring.RequestValve"&gt;
 * &lt;/Host&gt;
 * <pre>
 * @author marn2402
 */
public class RequestValve extends ValveBase {

    private static final String METRIC_NAME = "RequestsStats";
    private final Counter activeRequests;
    private static final String NAME_PREFIX = "responseCodes.";
    private static final int OK = 200;
    private static final int CREATED = 201;
    private static final int NO_CONTENT = 204;
    private static final int BAD_REQUEST = 400;
    private static final int NOT_FOUND = 404;
    private static final int SERVER_ERROR = 500;
    private final ConcurrentMap<Integer, Meter> metersByStatusCode;
    private final MetricRegistry metricsRegistry;
    private final Meter otherMeter;
    private final Timer requestTimer;

    private String suffix="";
    
    private String ignoredPathSuffix;
    
    public RequestValve() {
        this.metricsRegistry = new MetricRegistry();

        Map<Integer, String> meterNamesByStatusCode = createMeterNamesByStatusCode();

        this.metersByStatusCode = new ConcurrentHashMap<>(meterNamesByStatusCode.size());
        for (Map.Entry<Integer, String> entry : meterNamesByStatusCode.entrySet()) {
            metersByStatusCode.put(entry.getKey(), metricsRegistry.meter(name(METRIC_NAME, entry.getValue())));
        }
        this.otherMeter = metricsRegistry.meter(name(METRIC_NAME, "otherRequest"));
        this.activeRequests = metricsRegistry.counter(name(METRIC_NAME, "activeRequests"));
        this.requestTimer = metricsRegistry.timer(name(METRIC_NAME, "requests"));
        
        
    }

    public String getIgnoredPathSuffix() {
        return ignoredPathSuffix;
    }
/**
 * 
 * @param ignoredPathSuffix if you have a monitoring probe like /myapps/isAlive configure it there with 
 */
    public void setIgnoredPathSuffix(String ignoredPathSuffix) {
        this.ignoredPathSuffix = ignoredPathSuffix;
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * 
     * @param suffix if set this string will be appended to the jmx registry name
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
   
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
     if(ignoredPathSuffix!=null&&!ignoredPathSuffix.isEmpty()&&request.getPathInfo()!=null&&request.getPathInfo().endsWith(ignoredPathSuffix)){
         getNext().invoke(request, response);
         return;
     }
      
     if(request.isAsyncStarted()&&request.getAsyncContext()!=null){
          otherMeter.mark();
          getNext().invoke(request, response);
          return;
      }
         
        final Timer.Context context = requestTimer.time();
        try {          
            getNext().invoke(request, response);
        } finally {
            context.stop();
            activeRequests.dec();
            markMeterForStatusCode(response.getStatus());
        }
    }

    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal(); 
        final JmxReporter reporter = JmxReporter.forRegistry(metricsRegistry).inDomain("metrics"+suffix).build();       
        reporter.start();
    }

    private static Map<Integer, String> createMeterNamesByStatusCode() {
        final Map<Integer, String> meterNamesByStatusCode = new HashMap<>(6);
        meterNamesByStatusCode.put(OK, NAME_PREFIX + "ok");
        meterNamesByStatusCode.put(CREATED, NAME_PREFIX + "created");
        meterNamesByStatusCode.put(NO_CONTENT, NAME_PREFIX + "noContent");
        meterNamesByStatusCode.put(BAD_REQUEST, NAME_PREFIX + "badRequest");
        meterNamesByStatusCode.put(NOT_FOUND, NAME_PREFIX + "notFound");
        meterNamesByStatusCode.put(SERVER_ERROR, NAME_PREFIX + "serverError");
        return meterNamesByStatusCode;
    }

    private void markMeterForStatusCode(int status) {
        final Meter metric = metersByStatusCode.get(status);
        if (metric != null) {
            metric.mark();
        } else {
            otherMeter.mark();
        }
    }

}
