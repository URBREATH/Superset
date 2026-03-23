package eu.urbreathdsjobs.util;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.junit.jupiter.api.Test;

public class TestEncrypt {
	
	@Test
	void encryptTest() {



		System.out.println(buildEncryptor().encrypt("Municipia"));
		//System.out.println(buildEncryptor().decrypt("ljzGnSiPhDa53RLIDqr9/svz8s9jHvF9B+ZDTj722s3m59kRN8vUY4rjo8dl+UuU"));
	}
	
	
	
    private static StandardPBEStringEncryptor buildEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

        encryptor.setPassword("NgCM1UCg9gf37fWt");
        encryptor.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        encryptor.setKeyObtentionIterations(1000);
        encryptor.setStringOutputType("base64");

        // fondamentale per AES
        encryptor.setIvGenerator(new RandomIvGenerator());

        return encryptor;
    }

}
