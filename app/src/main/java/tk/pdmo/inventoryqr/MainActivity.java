package tk.pdmo.inventoryqr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private  static  final  int PERMISSION_REQUEST_STORAGE = 1000;
    private  static  final  int READ_REQUEST_CODE = 1001;
    public static String SAMPLE_XLSX_FILE_PATH = "";
    static TextView qrCode;
    static TextView qrFile;

    String color;
    private Handler handler = new Handler();
    SharedPreferences sPref;
    String savedCriteria;
    String savedText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrFile = (TextView) findViewById(R.id.qrFile);
        qrCode = (TextView) findViewById(R.id.qrCode);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.initiateScan();
            }
        });

        Button fileReader = (Button) findViewById(R.id.fileReader);
        fileReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getApplicationContex
                //startActivity(intent);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
                } else{
                    performFileSearch();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = (IntentResult) IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            if(data != null){
                Uri uri = data.getData();
                String path = uri.getPath();
                path = path.substring(path.indexOf(":") + 1);
                SAMPLE_XLSX_FILE_PATH = path;
                qrFile.setText(path);
            } else {
                Toast.makeText(this, "Data has null" , Toast.LENGTH_SHORT).show();
            }
        }
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                this.handler.post(new Runnable() {

                    @SuppressLint({"SdCardPath"})
                    public void run() {
                        MainActivity.qrCode.setTextSize(22.0f);
                        MainActivity.qrCode.setText(result.getContents());
                        if (result != null) {
                            MainActivity.this.readExcelFile(MainActivity.this, "/storage/emulated/0/"+SAMPLE_XLSX_FILE_PATH, result.getContents());
                        }
                    }
                });
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint({"SdCardPath"})
    private void readExcelFile(Context context, String filename, String var12) {
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            return;
        }
        qrCode.setTextColor(-65536);
        qrFile.setTextColor(-65536);
        qrFile.setTextSize(20.0f);
        qrFile.setText(R.string.nosearch);
        ((ListView) findViewById(R.id.listView1)).setAdapter(new ArrayAdapter<>(this, (int) R.layout.list_items, new String[0]));
        try {
            HSSFSheet mySheet = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(new File(filename)))).getSheetAt(0);

            DataFormatter dataFormatter = new DataFormatter();

            String cellValue;
            Row values = null;
            for(Row row: mySheet){
                values = row;
                for(Cell cell: row){
                    cellValue = dataFormatter.formatCellValue(cell);
                    if(cellValue.equalsIgnoreCase(qrCode.getText().toString())){
                        qrFile.setText(R.string.finded);
                        final Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.dialog);
                        dialog.setTitle("Data founded...");

                        int i = 0;
                        for (Cell c: values){
                            if(i == 1){
                                TextView text = (TextView) dialog.findViewById(R.id.id);
                                text.setText(dataFormatter.formatCellValue(c));
                            }
                            if(i == 2){
                                TextView text1 = (TextView) dialog.findViewById(R.id.marca);
                                text1.setText(dataFormatter.formatCellValue(c));
                            }
                            if(i == 3){
                                TextView text2 = (TextView) dialog.findViewById(R.id.empresa);
                                text2.setText(dataFormatter.formatCellValue(c));
                            }

                            i++;
                        }

                        Button dialogButton = (Button) dialog.findViewById(R.id.btnOk);
                        // if button is clicked, close the custom dialog
                        dialogButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public static boolean isExternalStorageReadOnly() {
        if ("mounted_ro".equals(Environment.getExternalStorageState())) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        if ("mounted".equals(Environment.getExternalStorageState())) {
            return true;
        }
        return false;
    }

    private  String readText(String input){
        File file = new File(input);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null){
                text.append(line);
                text.append("\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_STORAGE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                performFileSearch();
            } else{
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}