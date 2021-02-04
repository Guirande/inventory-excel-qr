package tk.pdmo.inventoryqr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class Settings extends Activity {
    private static final int DIALOG_LOAD_FILE = 1000;
    private static final String TAG = "F_PATH";
    EditText Data1;
    EditText Data2;
    EditText Data3;
    EditText Data4;
    EditText FileName;
    EditText InvColumn;
    ListAdapter adapter;
    Button btnParams;
    private String chosenFile;
    String[] colors = {"RED", "GREEN", "BLUE", "GRAY", "YELLOW", "BROWN"};
    private Item[] fileList;
    private Boolean firstLvl = true;
    private File path = new File(new StringBuilder().append(Environment.getExternalStorageDirectory()).toString());
    SharedPreferences sPref;
    Spinner spinner;
    ArrayList<String> str = new ArrayList<>();

    /* access modifiers changed from: protected */
    @SuppressLint("ResourceType")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        View btnScan = findViewById(R.id.BrowseButton);
        View btnSave = findViewById(R.id.SaveButton);
        this.FileName = (EditText) findViewById(R.id.PathColumn);
        this.InvColumn = (EditText) findViewById(R.id.InventoryColumn);
        this.Data1 = (EditText) findViewById(R.id.editText3);
        this.Data2 = (EditText) findViewById(R.id.editText4);
        this.Data3 = (EditText) findViewById(R.id.editText5);
        this.Data4 = (EditText) findViewById(R.id.editText6);
        this.spinner = (Spinner) findViewById(R.id.spinner1);
        btnScan.setOnClickListener(new View.OnClickListener() {
            /* class com.mobileinventory.Settings.AnonymousClass1 */

            public void onClick(View v) {
                Settings.this.loadFileList();
                Settings.this.showDialog(Settings.DIALOG_LOAD_FILE);
                Log.d(Settings.TAG, Settings.this.path.getAbsolutePath());
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            /* class com.mobileinventory.Settings.AnonymousClass2 */

            public void onClick(View v) {
                Settings.this.saveData("InvColName", Settings.this.InvColumn.getText().toString());
                Settings.this.saveData("Data1", Settings.this.Data1.getText().toString());
                Settings.this.saveData("Data2", Settings.this.Data2.getText().toString());
                Settings.this.saveData("Data3", Settings.this.Data3.getText().toString());
                Settings.this.saveData("Data4", Settings.this.Data4.getText().toString());
            }
        });
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, 17367048, this.colors);
        adapter2.setDropDownViewResource(17367049);
        this.spinner.setAdapter((SpinnerAdapter) adapter2);
        this.spinner.setPrompt(getString(R.string.colors));
        this.sPref = getSharedPreferences("DATA", 0);
        String color = this.sPref.getString("Color", "");
        int col = 1;
        if (color == "RED") {
            col = 0;
        } else if (color == "GREEN") {
            col = 1;
        } else if (color == "BLUE") {
            col = 2;
        } else if (color.equals("GRAY")) {
            col = 3;
        } else if (color == "YELLOW") {
            col = 4;
        } else if (color == "BROWN") {
            col = 5;
        }
        this.spinner.setSelection(col);
        this.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /* class com.mobileinventory.Settings.AnonymousClass3 */

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Settings.this.saveData("Color", Settings.this.spinner.getSelectedItem().toString());
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        loadData();
    }

    /* access modifiers changed from: package-private */
    public void saveData(String name, String source) {
        this.sPref = getSharedPreferences("DATA", 0);
        SharedPreferences.Editor ed = this.sPref.edit();
        ed.putString(name, source);
        ed.commit();
    }

    /* access modifiers changed from: package-private */
    public void loadData() {
        this.sPref = getSharedPreferences("DATA", 0);
        this.FileName.setText(this.sPref.getString("xlsPath", ""));
        this.InvColumn.setText(this.sPref.getString("InvColName", ""));
        this.Data1.setText(this.sPref.getString("Data1", ""));
        this.Data2.setText(this.sPref.getString("Data2", ""));
        this.Data3.setText(this.sPref.getString("Data3", ""));
        this.Data4.setText(this.sPref.getString("Data4", ""));
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        saveData("xlsPath", this.FileName.getText().toString());
        saveData("InvColName", this.InvColumn.getText().toString());
        saveData("Data1", this.Data1.getText().toString());
        saveData("Data2", this.Data2.getText().toString());
        saveData("Data3", this.Data3.getText().toString());
        saveData("Data4", this.Data4.getText().toString());
        super.onDestroy();
    }

    public void loadFileList() {
        try {
            this.path.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card ");
        }
        if (this.path.exists()) {
            String[] fList = this.path.list(new FilenameFilter() {

                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return (sel.isFile() || sel.isDirectory()) && !sel.isHidden();
                }
            });
            this.fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                this.fileList[i] = new Item(fList[i], Integer.valueOf((int) R.drawable.file_icon));
                if (new File(this.path, fList[i]).isDirectory()) {
                    this.fileList[i].icon = R.drawable.directory_up;
                    Log.d("DIRECTORY", this.fileList[i].file);
                } else {
                    Log.d("FILE", this.fileList[i].file);
                }
            }
            if (!this.firstLvl.booleanValue()) {
                Item[] temp = new Item[(this.fileList.length + 1)];
                for (int i2 = 0; i2 < this.fileList.length; i2++) {
                    temp[i2 + 1] = this.fileList[i2];
                }
                temp[0] = new Item("Up", Integer.valueOf((int) R.drawable.directory_up));
                this.fileList = temp;
            }
        } else {
            Log.e(TAG, "path does not exist");
        }
        this.adapter = new ArrayAdapter<Item>(this, 17367057, 16908308, this.fileList) {
            /* class com.mobileinventory.Settings.AnonymousClass5 */

            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                @SuppressLint("ResourceType") TextView textView = (TextView) view.findViewById(16908308);
                textView.setCompoundDrawablesWithIntrinsicBounds(Settings.this.fileList[position].icon, 0, 0, 0);
                textView.setCompoundDrawablePadding((int) ((5.0f * Settings.this.getResources().getDisplayMetrics().density) + 0.5f));
                return view;
            }
        };
    }

    /* access modifiers changed from: private */
    public class Item {
        public String file;
        public int icon;

        public Item(String file2, Integer icon2) {
            this.file = file2;
            this.icon = icon2.intValue();
        }

        public String toString() {
            return this.file;
        }
    }

    /* access modifiers changed from: protected */
    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (this.fileList == null) {
            Log.e(TAG, "No files loaded");
            return builder.create();
        }
        switch (id) {
            case DIALOG_LOAD_FILE /*{ENCODED_INT: 1000}*/:
                builder.setTitle(R.string.choose);
                builder.setAdapter(this.adapter, new DialogInterface.OnClickListener() {
                    /* class com.mobileinventory.Settings.AnonymousClass6 */

                    public void onClick(DialogInterface dialog, int which) {
                        Settings.this.chosenFile = Settings.this.fileList[which].file;
                        File sel = new File(Settings.this.path + "/" + Settings.this.chosenFile);
                        if (sel.isDirectory()) {
                            Settings.this.firstLvl = false;
                            Settings.this.str.add(Settings.this.chosenFile);
                            Settings.this.fileList = null;
                            Settings.this.path = new File(new StringBuilder().append(sel).toString());
                            Settings.this.loadFileList();
                            Settings.this.removeDialog(Settings.DIALOG_LOAD_FILE);
                            Settings.this.showDialog(Settings.DIALOG_LOAD_FILE);
                            Log.d(Settings.TAG, Settings.this.path.getAbsolutePath());
                        } else if (!Settings.this.chosenFile.equalsIgnoreCase("up") || sel.exists()) {
                            Settings.this.FileName.setText(sel.toString().replace("mnt/sdcard/", ""));
                            Settings.this.saveData("xlsPath", Settings.this.FileName.getText().toString());
                        } else {
                            Settings.this.path = new File(Settings.this.path.toString().substring(0, Settings.this.path.toString().lastIndexOf(Settings.this.str.remove(Settings.this.str.size() - 1))));
                            Settings.this.fileList = null;
                            if (Settings.this.str.isEmpty()) {
                                Settings.this.firstLvl = true;
                            }
                            Settings.this.loadFileList();
                            Settings.this.removeDialog(Settings.DIALOG_LOAD_FILE);
                            Settings.this.showDialog(Settings.DIALOG_LOAD_FILE);
                            Log.d(Settings.TAG, Settings.this.path.getAbsolutePath());
                        }
                    }
                });
                break;
        }
        return builder.show();
    }
}
