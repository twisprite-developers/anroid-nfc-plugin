package com.twinsprite.demokit.androidNFC;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.graphics.PixelFormat;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class ScanNFCActivity extends Activity {

	private NfcAdapter mAdapter = null;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	private String[] URI_PREFIX_MAP = new String[] {
			"",
			"http://www.",
			"https://www.",
			"http://",
			"https://"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set full screen
		setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);		
		getWindow ().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Get layout id
		int layoutID = getResources().getIdentifier("activity_scan_nfc", "layout", getPackageName());

		// Load layout
		RelativeLayout layout = (RelativeLayout) LayoutInflater.from(this).inflate(layoutID, null);

		// Set layout as content view
		setContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));


		// Create NFC Adapter
		mAdapter = NfcAdapter.getDefaultAdapter(this);

		if (mAdapter == null) {
			finishWithResult("NO_HARDWARE");
			return;
		}

		// Create a generic PendingIntent that will be deliver to this activity. The NFC stack
		// will fill in the intent with the details of the discovered tag before delivering to
		// this activity.
		mPendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, ScanNFCActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// Setup an intent filter for all MIME based dispatches (TEXT);
		IntentFilter ndefText = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefText.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			finishWithResult("ERROR");
			return;
		}		

		// Setup an intent filter for all MIME based dispatches (URI);
		IntentFilter ndefURI = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		ndefURI.addDataScheme("http");
		ndefURI.addDataScheme("https");

		mFilters = new IntentFilter[] { ndefText, ndefURI};

		// Setup a tech list for all NfcF tags
		mTechLists = new String[][] { new String[] { NfcF.class.getName() } };    

	}


	@Override
	public void onResume() {
		if (mAdapter != null) {
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
		}	  
		super.onResume();
	}

	@Override
	public void onPause() {
		if (mAdapter != null) {
			mAdapter.disableForegroundDispatch(this);
		}
		super.onPause();	
	}

	private void finishWithResult(String result) {
		Intent resultIntent = new Intent();
		resultIntent.putExtra("SCAN_RESULT", result);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		String nfcText = "";

		Parcelable[] ndefMessages = (Parcelable[])(intent.getParcelableArrayExtra("android.nfc.extra.NDEF_MESSAGES"));
		if (ndefMessages != null)
		{			
			try
			{
				for(int i = 0; i < ndefMessages.length; i++)
				{        			
					NdefMessage ndefMessage = (NdefMessage)(ndefMessages[i]);	            
					NdefRecord[] ndefRecords = ndefMessage.getRecords();

					for (int j = 0; j < ndefRecords.length; j++) {					      		
						if (ndefRecords[j].getTnf() == NdefRecord.TNF_WELL_KNOWN && 
								Arrays.equals(ndefRecords[j].getType(), NdefRecord.RTD_TEXT)) {	

							/*
							 * payload[0] contains the "Status Byte Encodings" field, per the
							 * NFC Forum "Text Record Type Definition" section 3.2.1.
							 *
							 * bit7 is the Text Encoding Field.
							 *
							 * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
							 * The text is encoded in UTF16
							 *
							 * Bit_6 is reserved for future use and must be set to zero.
							 *
							 * Bits 5 to 0 are the length of the IANA language code.
							 */

							byte[] payLoad = ndefRecords[0].getPayload();

							//Get the Text Encoding
							String textEncoding = ((payLoad[0] & 0200) == 0) ? "UTF-8" : "UTF-16";

							//Get the Language Code
							int languageCodeLength = payLoad[0] & 0077;
							//String languageCode = new String(payLoad, 1, languageCodeLength, "US-ASCII");

							//Get the Text
							nfcText = new String(payLoad, languageCodeLength + 1, payLoad.length - languageCodeLength - 1, textEncoding);


						}
						else if (ndefRecords[j].getTnf() == NdefRecord.TNF_WELL_KNOWN && 
								Arrays.equals(ndefRecords[j].getType(), NdefRecord.RTD_URI)) {

							/*
							 * See NFC forum specification for "URI Record Type Definition" at 3.2.2
							 *
							 * http://www.nfc-forum.org/specs/
							 * 
							 * payload[0] contains the URI Identifier Code
							 * payload[1]...payload[payload.length - 1] contains the rest of the URI.
							 */
							byte[] payload = ndefRecords[j].getPayload();
							String prefix = (String) URI_PREFIX_MAP[payload[0]];

							byte prefBytes[] = prefix.getBytes(Charset.forName("UTF-8"));
							byte postBytes[] = Arrays.copyOfRange(payload, 1, payload.length);

							byte[] fullUri = new byte[prefBytes.length + postBytes.length];
							System.arraycopy(prefBytes, 0, fullUri, 0, prefBytes.length);
							System.arraycopy(postBytes, 0, fullUri, prefBytes.length, postBytes.length);

							nfcText= new String(fullUri, Charset.forName("UTF-8"));
						}
					}
				}      
			}
			catch (Exception e){
				finishWithResult("ERROR");
				return;
			}        
		}

		// Scan completed
		finishWithResult(nfcText);
		
	}	

}
