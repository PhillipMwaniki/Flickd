package ke.co.philsoft.flickd;

import android.os.Bundle;

public class SearchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
       activateToolbar(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
