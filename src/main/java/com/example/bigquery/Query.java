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

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.costexplorer.AWSCostExplorer;
import com.amazonaws.services.costexplorer.AWSCostExplorerClientBuilder;
import com.amazonaws.services.costexplorer.model.*;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.util.Context;
import com.azure.resourcemanager.billing.BillingManager;
import com.azure.resourcemanager.costmanagement.CostManagementManager;
import com.azure.resourcemanager.costmanagement.models.ExportType;
import com.azure.resourcemanager.costmanagement.models.FunctionType;
import com.azure.resourcemanager.costmanagement.models.GranularityType;
import com.azure.resourcemanager.costmanagement.models.OperatorType;
import com.azure.resourcemanager.costmanagement.models.QueryAggregation;
import com.azure.resourcemanager.costmanagement.models.QueryColumnType;
import com.azure.resourcemanager.costmanagement.models.QueryComparisonExpression;
import com.azure.resourcemanager.costmanagement.models.QueryDataset;
import com.azure.resourcemanager.costmanagement.models.QueryDefinition;
import com.azure.resourcemanager.costmanagement.models.QueryFilter;
import com.azure.resourcemanager.costmanagement.models.QueryGrouping;
import com.azure.resourcemanager.costmanagement.models.TimeframeType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// Sample to query in a table
public class Query {

  public static void main(String[] args) {

    //GCP

    // TODO(developer): Replace these variables before running the sample.
    String projectId = "atos-iberia-idm-cloud-demo-1";
    String datasetName = "billing_data";
    String tableName = "gcp_billing_export_v1_01FF7F_D1770F_4DEB3A";
    String query =
            "SELECT invoice.month,\n"
                    + " SUM(cost)"
                    + " + SUM(IFNULL((SELECT SUM(c.amount) FROM UNNEST(credits) c), 0))"
                    + " AS total,"
                    + " (SUM(CAST(cost * 1000000 AS int64))"
                    + " + SUM(IFNULL((SELECT SUM(CAST(c.amount * 1000000 as int64)) FROM UNNEST(credits) c), 0))) / 1000000"
                    + " AS total_exact"
                    + " FROM `"
                    + projectId
                    + "."
                    + datasetName
                    + "."
                    + tableName
                    + "`"
                    + " GROUP BY 1"
                    + " ORDER BY 1 ASC";

    
    // AWS
    CEWithDimension();
    
    //GCP
    query(query);

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

    try{
        customerQueryGroupingModern(manager);
    }
     catch (final Exception e) {
        System.out.println(e);
    }
            /*
    try {
        invoice(manager);

      } catch (final Exception e) {
        System.out.println(e);
      }
      */
  }

  
    private static void CEWithDimension() {
      Expression expression = new Expression();
      DimensionValues dimensions = new DimensionValues();
      dimensions.withKey(Dimension.SERVICE);
      dimensions.withValues("Amazon Route 53");

      expression.withDimensions(dimensions);

      final GetCostAndUsageRequest awsCERequest = new GetCostAndUsageRequest()
              .withTimePeriod(new DateInterval().withStart("2022-06-01").withEnd("2022-06-30"))
              .withGranularity(Granularity.DAILY)
              .withMetrics("BlendedCost")
              .withFilter(expression);

      try {
        AWSCostExplorer ce = AWSCostExplorerClientBuilder.standard()
                //.withCredentials(new CredentialsClient().getCredentials())
                .build();

        System.out.println(ce.getCostAndUsage(awsCERequest));

      } catch (final Exception e) {
        System.out.println(e);
      }
    }
    

  public static void query(String query) {
    try {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

      QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();

      TableResult results = bigquery.query(queryConfig);

      results
          .iterateAll()
          .forEach(row -> row.forEach(val -> System.out.printf("%s,", val.toString())));

      System.out.println("Query performed successfully.");
    } catch (BigQueryException | InterruptedException e) {
      System.out.println("Query not performed \n" + e.toString());
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

}
// [END bigquery_query]

