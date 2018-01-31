/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import java.util.ArrayList;
import java.util.List;

public class ServiceQueryResult {

  private List<ServiceRegistryEntry> serviceQueryData = new ArrayList<>();

  public ServiceQueryResult() {
  }

  public ServiceQueryResult(List<ServiceRegistryEntry> serviceQueryData) {
    this.serviceQueryData = serviceQueryData;
  }

  public List<ServiceRegistryEntry> getServiceQueryData() {
    return serviceQueryData;
  }

  public void setServiceQueryData(List<ServiceRegistryEntry> serviceQueryData) {
    this.serviceQueryData = serviceQueryData;
  }

  @JsonIgnore
  public boolean isValid() {
    return serviceQueryData != null && !serviceQueryData.isEmpty();
  }

}
