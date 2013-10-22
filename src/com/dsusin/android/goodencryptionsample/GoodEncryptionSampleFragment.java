package com.dsusin.android.goodencryptionsample;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.dsusin.android.goodencryption.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class GoodEncryptionSampleFragment extends Fragment {
	private static final String TAG="GoodEncryptionFragment";
	public static final String DIALOG_PASSWORD="pwd";
	private TextView mMasterKeyTextView;
	private Button mGenButton;
	private EditText mPasswordEditText;
	private TextView mEncMasterKeyTextView;
	private Button mEncryptButton;
	private TextView mDecMasterKeyTextView;
	private Button mDecryptButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_good_encryption, parent,
				false);

		mMasterKeyTextView = (TextView) v.findViewById(R.id.textViewMasterKey);

		mGenButton = (Button) v.findViewById(R.id.buttonGenerateMasterKey);
		mGenButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mMasterKeyTextView.setText(randomMasterKey());
			}
		});
		
		mPasswordEditText=(EditText) v.findViewById(R.id.editTextPassword);

		mEncMasterKeyTextView = (TextView) v.findViewById(R.id.textViewEncryptedMasterKey);

		mEncryptButton = (Button) v.findViewById(R.id.buttonEncryptMasterKey);
		mEncryptButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(mPasswordEditText.getText().toString().length()>0){
					String emkey = encryptMasterKey(mMasterKeyTextView.getText().toString(), mPasswordEditText.getText().toString());
					mEncMasterKeyTextView.setText(emkey);
				}else{
					Toast.makeText(getActivity(), "Empty password!", Toast.LENGTH_SHORT).show();
				}
			}
		});

		mDecMasterKeyTextView = (TextView) v.findViewById(R.id.textViewDecryptedMasterKey);

		mDecryptButton = (Button) v.findViewById(R.id.buttonDecryptMasterKey);
		mDecryptButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(mPasswordEditText.getText().toString().length()>0){
					String dmkey = decryptMasterKey(mEncMasterKeyTextView.getText().toString(), mPasswordEditText.getText().toString());
					mDecMasterKeyTextView.setText(dmkey);
				}else{
					Toast.makeText(getActivity(), "Empty password!", Toast.LENGTH_SHORT).show();
				}
			}
		});

		return v;
	}

	private String randomMasterKey(){
		try {
			KeyGenerator kg=KeyGenerator.getInstance("AES");
			kg.init(128);
			SecretKey key=kg.generateKey();

			byte[] raw = key.getEncoded();

			return Base64.encodeToString(raw, Base64.NO_WRAP);			
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private byte[] deriveKey(String pwd){
		//IMEI as salt
		TelephonyManager mngr = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE); 
		byte[] imei=mngr.getDeviceId().getBytes();
		
		//Derived user key
		SecretKeyFactory skf;
		try {
			skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec ks=new PBEKeySpec(pwd.toCharArray(), imei, 4096, 128);
			byte[] rawDerivedKey=skf.generateSecret(ks).getEncoded();
			return rawDerivedKey;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String encryptMasterKey(String key, String pwd){
		
		byte[] rawDerivedKey=deriveKey(pwd);

		//Encrypt master key
		SecretKeySpec skeySpec = new SecretKeySpec(rawDerivedKey, "AES");
	    Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES");
		    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		    byte[] rawEncryptedMasterKey=cipher.doFinal(Base64.decode(key, Base64.NO_WRAP));
		    return Base64.encodeToString(rawEncryptedMasterKey, Base64.NO_WRAP);
		    
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String decryptMasterKey(String encKey, String pwd){
		
		byte[] rawDerivedKey=deriveKey(pwd);

		//Decrypt master key
		SecretKeySpec skeySpec = new SecretKeySpec(rawDerivedKey, "AES");
	    Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES");
		    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		    byte[] rawDecryptedMasterKey=cipher.doFinal(Base64.decode(encKey, Base64.NO_WRAP));
		    return Base64.encodeToString(rawDecryptedMasterKey, Base64.NO_WRAP);
		    
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
		    Toast.makeText(getActivity(), "Bad password!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		return null;
	}
}
