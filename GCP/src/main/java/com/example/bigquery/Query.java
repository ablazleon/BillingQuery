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

// Sample to query in a table
public class Query {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "gcping-349912";
    String datasetName = "billing";
    String tableName = "gcp_billing_export_v1_010502_563011_B0AFC5";
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
    query(query);
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
// [END bigquery_query]
