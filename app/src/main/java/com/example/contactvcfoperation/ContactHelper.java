package com.example.contactvcfoperation;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;

/**
 * ============================================================================
 * Copyright (C) 2019 HexaBit Soft Solution - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <p>
 * Created by: Mimo Saha on [11-Feb-2019 at 11:48 PM].
 * Email: hexa.bit.slash@gmail.com
 * <p>
 * Project: kotha_android_2018.
 * Code Responsibility: <Purpose of code>
 * <p>
 * Edited by :
 * --> <First Editor> on [11-Feb-2019 at 11:48 PM].
 * --> <Second Editor> on [11-Feb-2019 at 11:48 PM].
 * <p>
 * Reviewed by :
 * --> <First Reviewer> on [11-Feb-2019 at 11:48 PM].
 * --> <Second Reviewer> on [11-Feb-2019 at 11:48 PM].
 * ============================================================================
 **/
public class ContactHelper {

    private static ContactHelper contactHelper = new ContactHelper();
    private Activity context;

    public static ContactHelper getInstance() {
        return contactHelper;
    }

    public ContactHelper setContactContext(Activity context) {
        this.context = context;
        return this;
    }

    public File getContactFile(Intent intent) {
        Uri contactData = intent.getData();
        Cursor cursor = context.managedQuery(contactData, null, null, null, null);
        int contactsCount = cursor.getCount();

        String id = "";
        String name = "";
        String number = "";
        String typeMobile = "";
        String typeHome = "";
        String typeHomeFax = "";
        String typeMain = "";
        String typeOther = "";
        String typePager = "";
        String typeWork = "";
        String typeWorkFax = "";

        if (contactsCount > 0) {

            while (cursor.moveToNext()) {
                id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    //the below cursor will give you details for multiple contacts
                    Cursor pCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds
                                    .Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    // continue till this cursor reaches to all phone numbers which are associated with a contact in the                                                                                                                                                                                                                            contact list
                    while (pCursor.moveToNext()) {
                        int phoneType = pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                        number = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //you will get all phone numbers according to it's type as below switch case.
                        //Logs.e will print the phone number along with the name in DDMS. you can use these details where ever you want.
                        switch (phoneType) {
                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                Log.e(name + ": TYPE_MOBILE", " " + number);
                                typeMobile = number;
                                //number = typeMobile;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                Log.e(name + ": TYPE_HOME", " " + number);
                                typeHome = number;
                                // number = typeHome;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                Log.e(name + ": TYPE_WORK", " " + number);
                                typeWork = number;
                                // number = typeWork;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                                Log.e(name + ": TYPE_MAIN", " " + number);
                                typeMain = number;
                                // number = typeWorkMobile;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                                Log.e(name + ": TYPE_FAX_WORK", " " + number);
                                typeWorkFax = number;
                                // number = typeWorkMobile;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                                Log.e(name + ": TYPE_FAX_HOME", " " + number);
                                typeHomeFax = number;
                                // number = typeWorkMobile;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                                Log.e(name + ": TYPE_PAGER", " " + number);
                                typePager = number;
                                // number = typeWorkMobile;
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                                Log.e(name + ": TYPE_OTHER", " " + number);
                                typeOther = number;
                                // number = typeOther;
                                break;
                            default:
                                break;
                        }
                    }
                    pCursor.close();
                }
            }
        }

        String contactFileName = generateContactFile();

        File vcfFile = new File(contactFileName);

        try {
            FileWriter fw = new FileWriter(vcfFile);
            fw.write("BEGIN:VCARD\r\n");
            fw.write("VERSION:2.1\r\n");
            fw.write("N:" + name + "\r\n");
            fw.write("FN:" + name + "\r\n");
            fw.write("TEL;CELL:" + typeMobile + "\r\n");
            if (!typeHome.equals("") && typeHome != null) {
                fw.write("TEL;TYPE=HOME,VOICE:" + typeHome + "\r\n");
            }
            if (!typeWork.equals("") && typeWork != null) {
                fw.write("TEL;TYPE=WORK,VOICE:" + typeWork + "\r\n");
            }
            if (!typeMain.equals("") && typeMain != null) {
                fw.write("TEL;TYPE=MAIN,VOICE:" + typeMain + "\r\n");
            }
            if (!typeWorkFax.equals("") && typeWorkFax != null) {
                fw.write("TEL;TYPE=TYPE_FAX_WORK,VOICE:" + typeWorkFax + "\r\n");
            }
            if (!typeHomeFax.equals("") && typeHomeFax != null) {
                fw.write("TEL;TYPE=TYPE_FAX_HOME,VOICE:" + typeHomeFax + "\r\n");
            }
            if (!typePager.equals("") && typePager != null) {
                fw.write("TEL;TYPE=TYPE_PAGER,VOICE:" + typePager + "\r\n");
            }
            if (!typeOther.equals("") && typeOther != null) {
                fw.write("TEL;TYPE=TYPE_OTHER,VOICE:" + typeOther + "\r\n");
            }
            fw.write("END:VCARD" + "\r\n");
            fw.close();

            return vcfFile;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String generateContactFile() {

        String root = Environment.getExternalStorageDirectory().getPath();
        File myDir = new File(root + "/Contacts");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        long timestramp = System.currentTimeMillis();
        String fname = "contacts" + timestramp + ".vcf";
        String contactFileName = myDir.getAbsolutePath() + "/" + fname;

        return contactFileName;
    }

}
