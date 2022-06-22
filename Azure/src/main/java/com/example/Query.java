import com.azure.core.util.Context;
public class Query {

    public static void main (String[] args ) {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
        .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
        .build();
         BillingManager manager = BillingManager
        .authenticate(credential, profile);

        
    }
}


