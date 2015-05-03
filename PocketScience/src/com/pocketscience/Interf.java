/**
 * C:\Program Files\Java\jdk1.7.0_45\bin
 * C:\New\jar\jar cvf drun.jar classes.dex
 * 
 * D:\ANDROID\adt-bundle-windows-x86-20130219\sdk\build-tools\17.0.0>dx.bat --no-strict --dex --output=D:\ANDROID\adt-bundle-windows-x86-20130219\sdk\build-tools\17.0.0\classes.dex D:\ANDROID\adt-bundle-windows-x86-20130219\sdk\build-tools\17.0.0\Implemented.class
 * 
*/

package com.pocketscience;

import android.content.Context;

public interface Interf 
{
    public void show(Context context, String message);
    
    public void process(Context context);
}
