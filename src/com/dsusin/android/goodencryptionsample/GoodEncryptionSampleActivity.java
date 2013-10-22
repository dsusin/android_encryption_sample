package com.dsusin.android.goodencryptionsample;

import android.support.v4.app.Fragment;

public class GoodEncryptionSampleActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new GoodEncryptionSampleFragment();
	}
}
