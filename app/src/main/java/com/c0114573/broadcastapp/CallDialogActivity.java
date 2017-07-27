package com.c0114573.broadcastapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by member on 2017/07/26.
 */

public class CallDialogActivity extends Activity {

    Intent it;
    String label = "no title";
    String permission = "-1,-1,-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        label = intent.getStringExtra("LABEL");
        permission = intent.getStringExtra("PERMISSION");

        it = getIntent();

        String[] permissions = permission.split(",", 0);
        String resultPermission = "";

        if (permissions[0].equals("0")) {
            resultPermission += "    カメラ";
        }
        if (permissions[1].equals("0")) {
            resultPermission += "    SMS";
        }
        if (permissions[2].equals("0")) {
            resultPermission += "   位置情報";
        }

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(label);
        alert.setMessage("以下の権限を持つアプリを使用します\n位置情報の漏洩に注意してください\n" + resultPermission);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                try {
                    //FileInputStream inFile = new FileInputStream(FILE_NAME);
                    FileInputStream inFile = openFileInput("appData.file");
                    ObjectInputStream inObject = new ObjectInputStream(inFile);
                    List<AppData> dataList = (ArrayList<AppData>) inObject.readObject();
                    inObject.close();
                    inFile.close();

                    for (AppData appData : dataList) {
                        if (appData.getpackageLabel().equals(label)) {
                            appData.setLock(false);
                        }
                    }

                    // シリアライズしてファイルに保存
                    FileOutputStream outFile = openFileOutput("appData.file", 0);
                    ObjectOutputStream outObject = new ObjectOutputStream(outFile);
                    outObject.writeObject(dataList);
                    outObject.close();
                    outFile.close();

                } catch (StreamCorruptedException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                finish();

            }
        });
        alert.setNegativeButton("制限を許可", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Noボタンが押された時の処理
//                Toast.makeText(MainActivity.this, "No Clicked!", Toast.LENGTH_LONG).show();

                try {
                    //FileInputStream inFile = new FileInputStream(FILE_NAME);
                    FileInputStream inFile = openFileInput("appData.file");
                    ObjectInputStream inObject = new ObjectInputStream(inFile);
                    List<AppData> dataList = (ArrayList<AppData>) inObject.readObject();
                    inObject.close();
                    inFile.close();

                    for (AppData appData : dataList) {
                        if (appData.getpackageLabel().equals(label)) {
                            appData.setLock(false);
                        }
                    }

                    // シリアライズしてファイルに保存
                    FileOutputStream outFile = openFileOutput("appData.file", 0);
                    ObjectOutputStream outObject = new ObjectOutputStream(outFile);
                    outObject.writeObject(dataList);
                    outObject.close();
                    outFile.close();

                } catch (StreamCorruptedException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                finish();

            }
        });


        alert.show();

    }

}