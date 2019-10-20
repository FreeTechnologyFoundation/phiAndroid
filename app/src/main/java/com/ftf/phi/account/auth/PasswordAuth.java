package com.ftf.phi.account.auth;


import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordAuth implements Auth {
	private String algorithm;
	private byte[] salt;
	private int iterations;

	public PasswordAuth(){
		this.algorithm = "pbkdf2";
		this.iterations = 999999;
		this.salt = new byte[64];
		SecureRandom random = new SecureRandom();
		random.nextBytes(this.salt);
	}

	public PasswordAuth(String algorithm, int iterations, int saltBytes){
		this.algorithm = algorithm;
		this.iterations = iterations;
		this.salt = new byte[saltBytes];
		SecureRandom random = new SecureRandom();
		random.nextBytes(this.salt);
	}

	public PasswordAuth(JSONObject auth) throws JSONException {
		this.algorithm = auth.getString("algorithm");
		this.salt = auth.getString("salt").getBytes();
		this.iterations = auth.getInt("iterations");
	}

	public byte[] getKey(byte[] lastKey) throws NoSuchAlgorithmException {
		switch(this.algorithm){
			case "pbkdf2":
				byte[] hash = new byte[512];

				char[] password = new char[lastKey.length];
				for(int i = lastKey.length - 1; i > -1; i--){
					password[i] = (char) lastKey[i];
				}

				PBEKeySpec spec = new PBEKeySpec(password, this.salt, iterations, 512);
				SecretKeyFactory skf;

				try {
					skf = SecretKeyFactory.getInstance(this.algorithm);
					hash = skf.generateSecret(spec).getEncoded();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (InvalidKeySpecException e) {
					e.printStackTrace();
				}
				return hash;
			default:
				throw new NoSuchAlgorithmException();
		}
	}

	public JSONObject asJSON() throws JSONException {
		JSONObject data = new JSONObject();
		data.put("type", "password");
		data.put("algorithm", this.algorithm);
		data.put("iterations", this.iterations);
		data.put("salt", this.salt.toString());
		return data;
	}
}