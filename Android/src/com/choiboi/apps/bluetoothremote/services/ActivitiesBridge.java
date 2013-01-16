package com.choiboi.apps.bluetoothremote.services;

/*
 * This class helps pass objects between Android Activities.
 * 
 * Credit for this idea goes to:
 * http://vandzi.wordpress.com/2011/12/24/android-passing-objects-between-activities/
 */
public class ActivitiesBridge {
    
    private static Object object;
    
    /*
     * Set object to static variable and retrieve it from another activity.
     */
    public static void setObject(Object obj) {
        object = obj;
    }
    
    /*
     * Get object passed from previous activity.
     */
    public static Object getObject() {
        Object obj = object;
        
        // Can get only once
        object = null;
        return obj;
    }
}
