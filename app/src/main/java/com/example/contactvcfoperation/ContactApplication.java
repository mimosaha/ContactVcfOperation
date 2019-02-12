package com.example.contactvcfoperation;

import android.app.Application;
import android.content.Context;


/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Created by: Mimo Saha on [12-Feb-2019 at 5:26 PM].
 * Email:
 * Project: ContactVcfOperation.
 * Code Responsibility: <Purpose of code>
 * Edited by :
 * --> <First Editor> on [12-Feb-2019 at 5:26 PM].
 * --> <Second Editor> on [12-Feb-2019 at 5:26 PM].
 * Reviewed by :
 * --> <First Reviewer> on [12-Feb-2019 at 5:26 PM].
 * --> <Second Reviewer> on [12-Feb-2019 at 5:26 PM].
 * ============================================================================
 **/
public class ContactApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getAppContext() {
        return context;
    }

}
