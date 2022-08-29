/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bigquery;

// [START bigquery_query]


import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.util.Context;
import com.azure.resourcemanager.costmanagement.CostManagementManager;
import com.azure.resourcemanager.costmanagement.models.ExportType;
import com.azure.resourcemanager.costmanagement.models.FunctionType;
import com.azure.resourcemanager.costmanagement.models.GranularityType;
import com.azure.resourcemanager.costmanagement.models.QueryAggregation;
import com.azure.resourcemanager.costmanagement.models.QueryColumnType;
import com.azure.resourcemanager.costmanagement.models.QueryDataset;
import com.azure.resourcemanager.costmanagement.models.QueryDefinition;
import com.azure.resourcemanager.costmanagement.models.QueryGrouping;
import com.azure.resourcemanager.costmanagement.models.TimeframeType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.BasicConfigurator;

// Sample to query in a table
public class Query {

  public static void main(String[] args) {

    // Azure
    AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
    TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
    // System.out.println(profile.getTenantId());
    CostManagementManager manager = CostManagementManager
            .authenticate(credential, profile);
    System.out.println(System.getenv("AZURE_TENANT_ID"));
    System.out.println(System.getenv("AZURE_CLIENT_ID"));
    System.out.println(System.getenv("AZURE_CLIENT_SECRET"));
    
    BasicConfigurator.configure();

    try{
        subscriptionQueryLegacy(manager);
        //customerQueryGroupingModern(manager);
    }
     catch (final Exception e) {
        System.out.println(e);
    }

  }

  //https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/costmanagement/azure-resourcemanager-costmanagement/src/samples/java/com/azure/resourcemanager/costmanagement/QueryUsageSamples.java
      /**
     * Sample code: CustomerQueryGrouping-Modern.
     *
     * @param costManagementManager Entry point to CostManagementManager.
     */
    public static void customerQueryGroupingModern(
        com.azure.resourcemanager.costmanagement.CostManagementManager costManagementManager) {
        costManagementManager
            .queries()
            .usageWithResponse(
                "providers/Microsoft.Billing/billingAccounts/0b6b4c37-f1bf-4ce2-a367-85ec50c803ea/customers/fbb4764e-16a0-454a-8adc-54b94dac24f6",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.THE_LAST_MONTH)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.fromString("None"))
                            .withAggregation(
                                mapOf(
                                    "totalCost",
                                    new QueryAggregation().withName("PreTaxCost").withFunction(FunctionType.SUM)))
                            .withGrouping(
                                Arrays
                                    .asList(
                                        new QueryGrouping()
                                            .withType(QueryColumnType.DIMENSION)
                                            .withName("ResourceGroup")))),
                Context.NONE);
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }

    /**
     * Este código funciona en la función de prueba de azure
     * 
     * Sample code: SubscriptionQuery-Legacy.
     *
     * @param costManagementManager Entry point to CostManagementManager.
     */
    public static void subscriptionQueryLegacy(
        com.azure.resourcemanager.costmanagement.CostManagementManager costManagementManager) {
        costManagementManager
            .queries()
            .usageWithResponse(
                "subscriptions/0b6b4c37-f1bf-4ce2-a367-85ec50c803ea",
                new QueryDefinition()
                    .withType(ExportType.USAGE)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                           ),
                Context.NONE);
    }


}


