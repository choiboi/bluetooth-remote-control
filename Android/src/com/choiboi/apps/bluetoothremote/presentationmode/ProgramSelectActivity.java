package com.choiboi.apps.bluetoothremote.presentationmode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.choiboi.apps.bluetoothremote.R;

public class ProgramSelectActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.presentation_program_list);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);
        
        // Setup array with programs
        String[] programs = new String[] {
                getResources().getString(R.string.micro_ppt),
                getResources().getString(R.string.adobe_pdf),
                getResources().getString(R.string.browser)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.device_name, programs);
        
        // Setup dialog to display list of programs.
        ListView listView = (ListView) findViewById(R.id.program_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String programSelected = ((TextView) view).getText().toString();

                // Create result Intent and include the name of program
                Intent intent = new Intent();
                intent.putExtra(PresentationMode.PROGRAM, programSelected);

                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
}
