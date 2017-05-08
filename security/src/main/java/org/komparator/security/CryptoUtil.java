package org.komparator.security;

import java.security.Key;
import java.security.Security;
import java.security.Signature;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.SignatureException;
import java.security.InvalidKeyException;
import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class CryptoUtil {

	//DEFINE
   private static final String PUBLIC_KEY_PATH = "/home/gui/SD/proj2/mediator-ws/src/main/resources/A31/A31_Mediator.cer";
   private static final String PRIVATE_KEY_PATH =  "/home/gui/SD/proj2/mediator-ws-cli/src/main/A31/A31_Mediator.jks";
   private static final String PUBLIC_KEY_PATH_SUPPLIER = "/home/gui/SD/proj2/mediator-ws/src/main/resources/A31/A31_Supplier1.cer";
   private static final String PRIVATE_KEY_PATH_SUPPLIER =  "/home/gui/SD/proj2/mediator-ws/src/main/resources/A31/A31_Supplier1.jks";
   private static final String GROUP_PASS =  "y77CL7xb";
	private static final String ALIAS = "a31_mediator";
	private static final String ALIAS_SUPPLIER = "a31_supplier";
	private static final String SIGN_METHOD = "SHA256withRSA";
	
	public static boolean outputFlag = true;

	public static Certificate getX509CertificateFromStream(InputStream in) throws CertificateException {
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			Certificate cert = certFactory.generateCertificate(in);
			return cert;
		} finally {
			closeStream(in);
		}
	}

	public static Certificate getX509CertificateFromFile(File certificateFile)
			throws FileNotFoundException, CertificateException {
		FileInputStream fis = new FileInputStream(certificateFile);
		return getX509CertificateFromStream(fis);
	}

	public static Certificate getX509CertificateFromFilePath(String certificateFilePath)
			throws FileNotFoundException, CertificateException {
		File certificateFile = new File(certificateFilePath);
		return getX509CertificateFromFile(certificateFile);
	}
	
	private static KeyStore readKeystoreFromStream(InputStream keyStoreInputStream, char[] keyStorePassword)
			throws KeyStoreException {
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		try {
			keystore.load(keyStoreInputStream, keyStorePassword);
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new KeyStoreException("Could not load key store", e);
		} finally {
			closeStream(keyStoreInputStream);
		}
		return keystore;
	}

	private static KeyStore readKeystoreFromFile(File keyStoreFile, char[] keyStorePassword)
			throws FileNotFoundException, KeyStoreException {
		FileInputStream fis = new FileInputStream(keyStoreFile);
		return readKeystoreFromStream(fis, keyStorePassword);
	}

	public static KeyStore readKeystoreFromFilePath(String keyStoreFilePath, char[] keyStorePassword)
			throws FileNotFoundException, KeyStoreException {
		return readKeystoreFromFile(new File(keyStoreFilePath), keyStorePassword);
	}
	
	public static PrivateKey getPrivateKeyFromKeyStore(String keyAlias, char[] keyPassword, KeyStore keystore)
			throws KeyStoreException, UnrecoverableKeyException {
		PrivateKey key;
		try {
			key = (PrivateKey) keystore.getKey(keyAlias, keyPassword);
		} catch (NoSuchAlgorithmException e) {
			throw new KeyStoreException(e);
		}
		return key;
	}
	
	private static void closeStream(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) { System.out.println(e.toString());}
	}

   public static PublicKey getPublicKey() {
   
      PublicKey publicKey = null;
      try{
		   publicKey = getX509CertificateFromFilePath(PUBLIC_KEY_PATH).getPublicKey();
      } catch (Exception e) { System.out.println(e.toString());}
        
		return publicKey;      
	}
	
	public static PublicKey getPublicKeySupplier() {
   
      PublicKey publicKey = null;
      try{
		   publicKey = getX509CertificateFromFilePath(PUBLIC_KEY_PATH_SUPPLIER).getPublicKey();
      } catch (Exception e) { System.out.println(e.toString());}
        
		return publicKey;      
	}
      
   public static PrivateKey getPrivateKey() {
		
      PrivateKey privateKey = null;
      KeyStore keystore = null;
		try{
			keystore = readKeystoreFromFilePath(PRIVATE_KEY_PATH, GROUP_PASS.toCharArray());
			privateKey = getPrivateKeyFromKeyStore(ALIAS, GROUP_PASS.toCharArray(), keystore);
		} catch(Exception e) { System.out.println(e.toString());}
   
		return privateKey;   
	}
	
	public static PrivateKey getPrivateKeySupplier(){
		PrivateKey privateKey = null;
      KeyStore keystore = null;
		try{
			keystore = readKeystoreFromFilePath(PRIVATE_KEY_PATH_SUPPLIER, GROUP_PASS.toCharArray());
			privateKey = getPrivateKeyFromKeyStore(ALIAS_SUPPLIER, GROUP_PASS.toCharArray(), keystore);
		} catch(Exception e) { System.out.println(e.toString());}
   
		return privateKey;  
	}    
      

    // TODO add security helper methods
    public static String asymCipher(String StringCleanBytes, Key publicKey){
      
      byte[] encryptedBytes = null;
      byte[] cleanBytes = StringCleanBytes.getBytes();
      String ret = null;
      try{
         Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); //Criar Cipher
         cipher.init(Cipher.ENCRYPT_MODE, publicKey); //inicializar parametros de encripcao
         encryptedBytes = cipher.doFinal(cleanBytes);
         ret = printBase64Binary(encryptedBytes);
      }
      catch(Exception e) { System.out.println(e.toString());}
      return ret;
    }

   public static String asymDecipher(String StringEncryptedBytes, Key privateKey){
   
      byte[] cleanBytes = null;
      byte[] encryptedBytes = parseBase64Binary(StringEncryptedBytes);
      String ret = null;
      try{
         Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); //Criar Cipher
         cipher.init(Cipher.DECRYPT_MODE, privateKey); //inicializar parametros de encripcao
         cleanBytes = cipher.doFinal(encryptedBytes);
         ret = new String(cleanBytes, "UTF-8");
      }
      catch(Exception e) { System.out.println(e.toString());}
      return ret;
      
    }
    
    	public static byte[] makeDigitalSignature(final String signatureMethod, final PrivateKey privateKey,
			final byte[] bytesToSign) {
		try {
			Signature sig = Signature.getInstance(signatureMethod);
			sig.initSign(privateKey);
			sig.update(bytesToSign);
			byte[] signatureResult = sig.sign();
			return signatureResult;
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			if (outputFlag) {
				System.err.println("Caught exception while making signature: " + e);
				System.err.println("Returning null.");
			}
			return null;
		}
	}

	/**
	 * Verify signature of bytes with the public key. If anything goes wrong,
	 * returns false (swallows exceptions).
	 * 
	 * @param signatureMethod
	 *            e.g. "SHA1WithRSA"
	 * @param publicKey
	 * @param bytesToVerify	
	 * @param signature
	 * @return
	 */
	public static boolean verifyDigitalSignature(final String signatureMethod, PublicKey publicKey,
			byte[] bytesToVerify, byte[] signature) {
		try {
			Signature sig = Signature.getInstance(signatureMethod);
			sig.initVerify(publicKey);
			sig.update(bytesToVerify);
			return sig.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			if (outputFlag) {
				System.err.println("Caught exception while verifying signature " + e);
				System.err.println("Returning false.");
			}
			return false;
		}
	}

	/**
	 * Verify signature of bytes with the public key contained in the
	 * certificate. If anything goes wrong, returns false (swallows exceptions).
	 * 
	 * @param signatureMethod
	 * @param publicKeycertificate
	 * @param bytesToVerify
	 * @param signature
	 * @return
	 */
	public static boolean verifyDigitalSignature(final String signatureMethod, Certificate publicKeyCertificate,
			byte[] bytesToVerify, byte[] signature) {
		return verifyDigitalSignature(signatureMethod, publicKeyCertificate.getPublicKey(), bytesToVerify, signature);
	}

	/**
	 * Checks if the certificate was properly signed by the CA with the provided
	 * public key.
	 * 
	 * @param certificate
	 *            Certificate to be verified
	 * @param caPublicKey
	 *            CA public key certificate
	 * @return true if properly signed
	 */
	public static boolean verifySignedCertificate(Certificate certificate, PublicKey caPublicKey) {
		try {
			certificate.verify(caPublicKey);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			if (outputFlag) {
				System.err.println("Caught exception while verifying certificate with CA public key : " + e);
				System.err.println("Returning false.");
			}
			return false;
		}
		return true;
	}

	/**
	 * Checks if the certificate was properly signed by the CA with the provided
	 * certificate.
	 * 
	 * @param certificate
	 *            Certificate to be verified
	 * @param caCertificate
	 *            Certificate containing the CA public key
	 * @return true if properly signed
	 */
	public static boolean verifySignedCertificate(Certificate certificate, Certificate caCertificate) {
		return verifySignedCertificate(certificate, caCertificate.getPublicKey());
	}

	public static String Sign(String toSign, PrivateKey signature){
		byte[] bytestoSign = null;
		byte[] retBytes = null;
		try{
			bytestoSign = toSign.getBytes();
			retBytes = makeDigitalSignature(SIGN_METHOD, signature, bytestoSign);
			return new String(retBytes, "UTF-8");
		} catch (Exception e) { System.out.println(e.toString());}
		return null;
	}
	
	public static boolean CheckSign(String message, String signature){
		byte[] signatureBytes = signature.getBytes();
		byte[] bytesToVerify = message.getBytes();
		return verifyDigitalSignature(SIGN_METHOD, getPublicKeySupplier(), bytesToVerify, signatureBytes);
	}
}
