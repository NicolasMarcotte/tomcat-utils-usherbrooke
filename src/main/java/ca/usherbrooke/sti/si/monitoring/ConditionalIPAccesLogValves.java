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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.AccessLogValve;
/**
 * 
 * @author marn2402
 */
public class ConditionalIPAccesLogValves
  extends AccessLogValve
{
  private Set<String> ignoredIPs = Collections.emptySet();
  private Set<String> ignoredStatusCode = Collections.emptySet();
  
  public void log(Request request, Response response, long time)
  {
    String ip = request.getRemoteAddr();
    String statusCode = Integer.toString(response.getStatus());
    if (((this.ignoredIPs.contains(ip)) || ((this.ignoredStatusCode.isEmpty()) && (!this.ignoredIPs.isEmpty()))) && ((this.ignoredStatusCode.contains(statusCode)) || ((!this.ignoredStatusCode.isEmpty()) && (this.ignoredIPs.isEmpty())))) {
      return;
    }
    super.log(request, response, time);
  }
  
  public void setIgnoredIPs(String ipsCommaDelimited)
  {
    String[] ips = ipsCommaDelimited.split(",");
    this.ignoredIPs = new HashSet(Arrays.asList(ips));
  }
  
  public Set<String> getIgnoredIPs()
  {
    return this.ignoredIPs;
  }
  
  public void setIgnoredStatusCodes(String ignoredStatusCodesCommaDelimited)
  {
    String[] status = ignoredStatusCodesCommaDelimited.split(",");
    this.ignoredStatusCode = new HashSet(Arrays.asList(status));
  }
  
  public Set<String> getIgnoredStatusCode()
  {
    return this.ignoredStatusCode;
  }
}