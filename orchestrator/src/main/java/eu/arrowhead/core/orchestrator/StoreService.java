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

package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.OrchestrationStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class StoreService {


  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final HashMap<String, Object> restrictionMap = new HashMap<>();

  /**
   * This method returns the active Orchestration Store entries for a consumer.
   */
  public static List<OrchestrationStore> getDefaultStoreEntries(ArrowheadSystem consumer) {
    restrictionMap.clear();
    ArrowheadSystem savedConsumer = getConsumerSystem(consumer.getSystemName());
    if (savedConsumer == null) {
      return new ArrayList<>();
    }

    restrictionMap.put("consumer", savedConsumer);
    restrictionMap.put("defaultEntry", true);
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

  /**
   * This method returns a list of Orchestration Store entries specified by the consumer system and the requested service.
   */
  public static List<OrchestrationStore> getStoreEntries(ArrowheadSystem consumer, ArrowheadService service) {
    restrictionMap.clear();
    ArrowheadSystem savedConsumer = getConsumerSystem(consumer.getSystemName());
    ArrowheadService savedService = getRequestedService(service.getServiceDefinition());
    if (savedConsumer == null || savedService == null) {
      return new ArrayList<>();
    }

    if (!savedService.getInterfaces().isEmpty()) {
      if (!hasMatchingInterfaces(savedService, service)) {
        return new ArrayList<>();
      }
    }

    restrictionMap.put("consumer", savedConsumer);
    restrictionMap.put("service", savedService);
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

  public static List<OrchestrationStore> getStoreEntries(ArrowheadService service) {
    restrictionMap.clear();
    ArrowheadService savedService = getRequestedService(service.getServiceDefinition());

    if (!savedService.getInterfaces().isEmpty()) {
      if (!hasMatchingInterfaces(savedService, service)) {
        return new ArrayList<>();
      }
    }

    restrictionMap.put("service", savedService);
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

  /**
   * This private method returns an ArrowheadSystem from the database.
   */
  private static ArrowheadSystem getConsumerSystem(String systemName) {
    HashMap<String, Object> rm = new HashMap<>();
    rm.put("systemName", systemName);
    return dm.get(ArrowheadSystem.class, rm);
  }

  /**
   * This private method returns an ArrowheadService from the database.
   */
  private static ArrowheadService getRequestedService(String serviceDefinition) {
    HashMap<String, Object> rm = new HashMap<>();
    rm.put("serviceDefinition", serviceDefinition);
    return dm.get(ArrowheadService.class, rm);
  }

  private static boolean hasMatchingInterfaces(ArrowheadService savedService, ArrowheadService givenService) {
    if (givenService.getInterfaces().isEmpty()) {
      return true;
    }
    for (String givenInterface : givenService.getInterfaces()) {
      for (String savedInterface : savedService.getInterfaces()) {
        if (givenInterface.equals(savedInterface)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * This method returns all the entries of the Orchestration Store.
   */
  public static List<OrchestrationStore> getAllStoreEntries() {
    restrictionMap.clear();
    return dm.getAll(OrchestrationStore.class, restrictionMap);
  }

}
