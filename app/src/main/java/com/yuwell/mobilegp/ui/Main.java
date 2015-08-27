package com.yuwell.mobilegp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.yuwell.mobilegp.R;

/**
 * Created by Chen on 2015/8/11.
 */
public class Main extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        findViewById(R.id.btn_printer).setOnClickListener(this);
        findViewById(R.id.btn_id_reader).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_printer:
                startActivity(new Intent(Main.this, PrinterActivity.class));
                break;
            case R.id.btn_id_reader:
                startActivity(new Intent(Main.this, IDCardActivity.class));
                break;
            default:
                break;
        }
    }
}
