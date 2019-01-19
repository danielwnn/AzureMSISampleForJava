package hello;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AppServiceMSICredentials;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

@RestController
public class HelloController {
    
    @RequestMapping("/")
    public String index() {
    	
    	// MSI Credentials
        AppServiceMSICredentials credentials = new AppServiceMSICredentials(AzureEnvironment.AZURE);

    	// App Settings
        String slotName = System.getenv("SLOT_NAME");

        // Key Vault
        KeyVaultClient keyVaultClient = new KeyVaultClient(credentials);
        SecretBundle secretBundle = keyVaultClient.getSecret("https://nyc-dof-keyvault01.vault.azure.net/", "MySecret");
        String secret = secretBundle.value();
        
        // SQL DB
        try { 
            SQLServerDataSource ds = new SQLServerDataSource();
            ds.setServerName("aad-managed-demo.database.windows.net"); // Replace with your server name
            ds.setDatabaseName("demo"); // Replace with your database name
            // ds.setAuthentication("ActiveDirectoryIntegrated");
            ds.setAccessToken(credentials.getToken("https://database.windows.net/"));
            ds.setHostNameInCertificate("*.database.windows.net");             
            
        	Connection connection = ds.getConnection(); 
            Statement stmt = connection.createStatement();           
            ResultSet rs = stmt.executeQuery("SELECT SUSER_SNAME()");
            
            if (rs.next()) {
                System.out.println("You have successfully logged on as: " + rs.getString(1));
            }
        } catch (Exception e) {
        	//
        }      

        return "Slot: " + slotName + " - Key Vault: MySecret = " + secret;
    }    
}
