/*
Code inspired by
https://gist.github.com/vatshat/f3fa2bbee59edcabf3d9cb4b04d88c72

 */

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

public class Test {

	public static void main(String[] args) {
		ViewBilling();
		//CEWithDimension();
		//CEWithGroupDefinition();

	}
		
	private static void CEWithGroupDefinition() {
		final GetCostAndUsageRequest awsCERequest = new GetCostAndUsageRequest()
        		.withTimePeriod(new DateInterval().withStart("2022-01-01").withEnd("2022-07-30"))
        		.withGranularity(Granularity.DAILY)
        		.withMetrics("BlendedCost")
                .withGroupBy(new GroupDefinition().withType("DIMENSION").withKey("INSTANCE_TYPE"));
		
		try {			
			AWSCostExplorer ce = AWSCostExplorerClientBuilder.standard()
					.withCredentials(new CredentialsClient().getCredentials())
					.build();
			
			GetCostAndUsageResult ceResult = ce.getCostAndUsage(awsCERequest);
	        ceResult.getResultsByTime().forEach(resultsByTime -> {
	            System.out.println(resultsByTime.toString());
	        });
		    
		} catch (final Exception e) {
			System.out.println(e);
		}
	}
	
	private static void CEWithDimension() {
		Expression expression = new Expression();		
		DimensionValues dimensions = new DimensionValues();
		dimensions.withKey(Dimension.SERVICE);
		dimensions.withValues("Amazon Route 53");
		
		expression.withDimensions(dimensions);
		
		final GetCostAndUsageRequest awsCERequest = new GetCostAndUsageRequest()
        		.withTimePeriod(new DateInterval().withStart("2022-01-01").withEnd("2022-07-30"))
        		.withGranularity(Granularity.DAILY)
        		.withMetrics("BlendedCost")
                .withFilter(expression);
                
		try {			
			AWSCostExplorer ce = AWSCostExplorerClientBuilder.standard()
					.withCredentials(new CredentialsClient().getCredentials())
					.build();
			
			System.out.println(ce.getCostAndUsage(awsCERequest));
		    
		} catch (final Exception e) {
			System.out.println(e);
		}
	}
	
	private static void ViewBilling() {
		final ViewBillingRequest awsVBRequest = new ViewBillingRequest();
		
		try {
			ViewBillingResult vbResult = vb.viewBilling(awsVBRequest);
            System.out.println(vbResult.getBillingRecords());
		    
		} catch (final Exception e) {
			System.out.println(e);
		}
	}
}

class CredentialsClient{
	
	CredentialsClient () {
  
	}	
	
	public AWSStaticCredentialsProvider getCredentials() throws Exception {

        BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(
				"AKIA3D3XGHY2FJR475NQ",
				"OIuupuCX1alObjAc+4/YEOYztaF0jNJnJUhBunWI");
		
        return new AWSStaticCredentialsProvider(sessionCredentials);
	}
    
}