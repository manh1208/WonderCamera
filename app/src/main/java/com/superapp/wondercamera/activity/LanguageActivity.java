package com.superapp.wondercamera.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.superapp.wondercamera.R;
import com.superapp.wondercamera.util.DataUtils;

import java.util.List;

public class LanguageActivity extends AppCompatActivity {
    private List<String> mLanguages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        createList();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.listview_language_item,R.id.lv_language_item,mLanguages);
        ListView listView = (ListView) findViewById(R.id.lv_language);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataUtils.getINSTANCE(LanguageActivity.this).setmCurrentLanguage(position+1);
                onBackPressed();
            }
        });
    }

    private void createList(){
        mLanguages = DataUtils.getINSTANCE(LanguageActivity.this).getListLanguageName();
    }
}
