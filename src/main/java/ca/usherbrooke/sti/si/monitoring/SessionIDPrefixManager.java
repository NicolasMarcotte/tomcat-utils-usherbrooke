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

import org.apache.catalina.session.StandardManager;
/**
 * @author marn2402
 */
public class SessionIDPrefixManager
  extends StandardManager
{
  protected String prefix = "";
  
  public String getPrefix()
  {
    return this.prefix;
  }
  
  public void setPrefix(String prefix)
  {
    prefix = prefix == null ? "" : prefix;
    this.prefix = prefix;
  }
  
  protected synchronized String generateSessionId()
  {
    return this.prefix + super.generateSessionId();
  }
}
