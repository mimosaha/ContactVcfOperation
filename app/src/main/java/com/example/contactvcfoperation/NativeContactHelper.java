package com.example.contactvcfoperation;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;

import ezvcard.VCard;
import ezvcard.android.AndroidCustomFieldScribe;
import ezvcard.android.ContactOperations;
import ezvcard.io.text.VCardReader;

import static ezvcard.util.IOUtils.closeQuietly;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Created by: Mimo Saha on [12-Feb-2019 at 5:22 PM].
 * Email:
 * Project: ContactVcfOperation.
 * Code Responsibility: <Purpose of code>
 * Edited by :
 * --> <First Editor> on [12-Feb-2019 at 5:22 PM].
 * --> <Second Editor> on [12-Feb-2019 at 5:22 PM].
 * Reviewed by :
 * --> <First Reviewer> on [12-Feb-2019 at 5:22 PM].
 * --> <Second Reviewer> on [12-Feb-2019 at 5:22 PM].
 * ============================================================================
 **/
public class NativeContactHelper {

    private static NativeContactHelper nativeContactHelper = new NativeContactHelper();
    private final String TAG = "NativeContacts";

    public static NativeContactHelper getInstance() {
        return nativeContactHelper;
    }

    public File prepareContactVcf(Uri contactUri) {

        Cursor cursor = null;

        try {
            Context context = ContactApplication.getAppContext();

            cursor = context.getContentResolver().query(contactUri,
                    null, null, null, null);

            if (cursor == null || cursor.getCount() == 0)
                return null;

            String id = "", name = "", number = "", typeMobile = "", typeHome = "", typeHomeFax = "",
                    typeMain = "", typeOther = "", typePager = "", typeWork = "", typeWorkFax = "", imageEncoded = "";

            while (cursor.moveToNext()) {

                id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                name = "AbcAbcAbc";

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    //the below cursor will give you details for multiple contacts
                    Cursor pCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    // continue till this cursor reaches to all phone numbers which are associated with a contact in the                                                                                                                                                                                                                            contact list
                    while (pCursor.moveToNext()) {

                        int phoneType = pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        number = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        //you will get all phone numbers according to it's type as below switch case.
                        //Logs.e will print the phone number along with the name in DDMS.
                        // you can use these details where ever you want.

                        switch (phoneType) {
                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                Log.v(TAG, "TYPE_MOBILE: " + number);
                                typeMobile = number;
                                break;

                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                Log.v(TAG, "TYPE_HOME: " + number);
                                typeHome = number;
                                break;

                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                Log.v(TAG, "TYPE_WORK: " + number);
                                typeWork = number;
                                break;

                            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                                Log.v(TAG, "TYPE_MAIN: " + number);
                                typeMain = number;
                                break;

                            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                                Log.v(TAG, "TYPE_FAX_WORK: " + number);
                                typeWorkFax = number;
                                break;

                            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                                Log.v(TAG, "TYPE_FAX_HOME: " + number);
                                typeHomeFax = number;
                                break;

                            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                                Log.v(TAG, "TYPE_PAGER: " + number);
                                typePager = number;
                                break;

                            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                                Log.v(TAG, "TYPE_OTHER: " + number);
                                typeOther = number;
                                break;

                            default:
                                break;
                        }
                    }
                    pCursor.close();
                }
            }

            String filePath = generateVcfAndGetFilePath();

            File vcfFile = new File(filePath);

            /*String content = "BEGIN:VCARD\n" +
                    "VERSION:3.0\n" +
                    "CLASS:PUBLIC\nPRODID:-" +
                    "//class_vcard from  TroyWolf.com//NONSGML Version 1//EN\n" +
                    "FN:"+contactName+"\n" +
                    "TEL;TYPE=cell,voice:"+number+"\n" +
                    "PHOTO;TYPE=JPEG;ENCODING=BASE64:"+imageEncoded+"\nTZ:+0000\nEND:VCARD";*/

            FileWriter fw = new FileWriter(vcfFile);
            fw.write("BEGIN:VCARD\r\n");
            fw.write("VERSION:2.1\r\n");
            fw.write("N:" + name + "\r\n");
            fw.write("FN:" + name + "\r\n");
            fw.write("TEL;CELL:" + typeMobile + "\r\n");
            if (!typeHome.equals("") && typeHome != null) {
                fw.write("TEL;TYPE=HOME,VOICE:" + typeHome + "\r\n");
            }
            if (!TextUtils.isEmpty(imageEncoded)) {
                fw.write("PHOTO;TYPE=JPEG;ENCODING=BASE64:" + imageEncoded + "\r\n");
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
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String generateVcfAndGetFilePath() {

        String root = Environment.getExternalStorageDirectory().getPath();
        File myDir = new File(root + "/Contacts");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        long timestramp = System.currentTimeMillis();
        String fname = "contacts" + timestramp + ".vcf";

        return myDir.getAbsolutePath() + "/" + fname;
    }

    public void saveVcfToContact(String filePath) {
        File vcardFile = new File(filePath);
        VCardReader reader = null;

        Context context = ContactApplication.getAppContext();

        try {
            reader = new VCardReader(vcardFile);
            reader.registerScribe(new AndroidCustomFieldScribe());

            ContactOperations operations = new ContactOperations(context);

            VCard vcard;
            while ((vcard = reader.readNext()) != null) {
                operations.insertContact(vcard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(reader);
        }
    }

    /*private String getImageString(String number) {
        try {
            Context context = ContactApplication.getAppContext();
            Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Uri.encode(number));
            Bitmap photoBitmap;
            ContentResolver cr = context.getContentResolver();
            InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(cr, photoUri);
            photoBitmap = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();
            return Base64.encodeToString(bitmapdata, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String convertUriToBase64(Context context, String photoUri) {
        InputStream imageStream = null;
        try {
            imageStream = context.getContentResolver().openInputStream(Uri.parse(photoUri));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();
        // line break has to be removed, so it is on the same line as PHOTO
        return Base64.encodeToString(bitmapData, Base64.DEFAULT).replaceAll("\n", "");
    }*/
}
