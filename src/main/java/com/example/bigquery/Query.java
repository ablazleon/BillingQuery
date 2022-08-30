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
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.util.Context;
import com.azure.resourcemanager.costmanagement.CostManagementManager;
import com.azure.resourcemanager.costmanagement.implementation.QueryResultImpl;
import com.azure.resourcemanager.costmanagement.models.ExportType;
import com.azure.resourcemanager.costmanagement.models.FunctionType;
import com.azure.resourcemanager.costmanagement.models.GranularityType;
import com.azure.resourcemanager.costmanagement.models.QueryAggregation;
import com.azure.resourcemanager.costmanagement.models.QueryColumn;
import com.azure.resourcemanager.costmanagement.models.QueryColumnType;
import com.azure.resourcemanager.costmanagement.models.QueryDataset;
import com.azure.resourcemanager.costmanagement.models.QueryDefinition;
import com.azure.resourcemanager.costmanagement.models.QueryGrouping;
import com.azure.resourcemanager.costmanagement.models.QueryResult;
import com.azure.resourcemanager.costmanagement.models.TimeframeType;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.BasicConfigurator;

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
    
    BasicConfigurator.configure();

    try{
         subscriptionQueryTraza(manager) ;
        //customerQueryGroupingModern(manager);
    }
     catch (final Exception e) {
        System.out.println(e);
    }

  }

  //https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/costmanagement/azure-resourcemanager-costmanagement/src/samples/java/com/azure/resourcemanager/costmanagement/QueryUsageSamples.java


    /**
     * Este código funciona en la función de prueba de azure
     * https://docs.microsoft.com/en-us/rest/api/cost-management/query/usage?tabs=HTTP#queryresult
     * 
     * Sample code: SubscriptionQuery-Legacy.
     *
     * @param costManagementManager Entry point to CostManagementManager.
     * @return 
     */
    public static void subscriptionQueryTraza(
        com.azure.resourcemanager.costmanagement.CostManagementManager costManagementManager) {
        Response<QueryResult> rqr = costManagementManager
            .queries()
            .usageWithResponse(
                "subscriptions/0b6b4c37-f1bf-4ce2-a367-85ec50c803ea",
                new QueryDefinition()
                    .withType(ExportType.ACTUAL_COST)
                    .withTimeframe(TimeframeType.MONTH_TO_DATE)
                    .withDataset(
                        new QueryDataset()
                            .withGranularity(GranularityType.DAILY)
                           ),
                Context.NONE);
        
        System.out.println("!!!!!!! Azure query" + rqr.getRequest().getBodyAsBinaryData()); // {"type":"Usage","timeframe":"MonthToDate","dataset":{"granularity":"Daily"}}
        System.out.println(rqr.getValue().innerModel().nextLink()); 
        
        for (QueryColumn i : rqr.getValue().innerModel().columns()) System.out.println(i.name());
        Integer n = 0;
        Integer m = 0;
        for (List<Object> i : rqr.getValue().innerModel().rows()) {
            n++;
            for (Object j : i) {
                m++;
                System.out.println(j.toString()+" "+n+" "+ m);
            }
        }

        for (QueryColumn i : rqr.getValue().columns()) System.out.println(i.name());
        Integer l = 0;
        Integer q = 0;
        for (List<Object> i : rqr.getValue().rows()) {
            l++;
            for (Object j : i) {
                q++;
                System.out.println(j.toString()+" "+l+" "+ q);
            }
        }
        
    }

    // Inspired by this comment https://gist.github.com/vatshat/f3fa2bbee59edcabf3d9cb4b04d88c72?permalink_comment_id=4184585
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

}


