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
import com.azure.resourcemanager.billing.BillingManager;
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
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.Credentials;

import com.amazonaws.services.route53domains.AmazonRoute53Domains;
import com.amazonaws.services.route53domains.AmazonRoute53DomainsClientBuilder;
import com.amazonaws.services.route53domains.model.*;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.util.Context;

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
    BillingManager manager = BillingManager
            .authenticate(credential, profile);
    invoice(manager);
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

  /*
   * x-ms-original-file: specification/billing/resource-manager/Microsoft.Billing/stable/2020-05-01/examples/Invoice.json
   */
  /**
   * Sample code: Invoice.
   *
   * @param manager Entry point to BillingManager.
   */
  public static void invoice(com.azure.resourcemanager.billing.BillingManager manager) {
    manager.invoices().getWithResponse("{billingAccountName}", "{invoiceName}", Context.NONE);
  }

}
// [END bigquery_query]

