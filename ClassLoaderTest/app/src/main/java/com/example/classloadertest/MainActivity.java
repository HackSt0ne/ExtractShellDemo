package com.example.classloadertest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.TextView;

import com.example.classloadertest.databinding.ActivityMainBinding;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DexClassLoader classLoader = new DexClassLoader("/data/local/tmp/1.dex",
                getCacheDir().getAbsolutePath(),
                null,
                getClassLoader());
        try {
//            Class TestClass = classLoader.loadClass("com.example.plugindex.TestClass");
//            Object obj = TestClass.newInstance();
//            Method testFunc = TestClass.getDeclaredMethod("testFunc");
//            testFunc.setAccessible(true);
//            testFunc.invoke(obj);
//            fixClassLoader(classLoader);
            replaceClassLoader(classLoader);
            Class TestClass = classLoader.loadClass("com.example.plugindex.MainActivity");
            startActivity(new Intent(this, TestClass)); // can not found in default ClassLoader,crash
        } catch (ClassNotFoundException e) {
           e.printStackTrace();
        }
    }

    public void replaceClassLoader(ClassLoader dexclassloader) {
        ClassLoader pathClassloader = MainActivity.class.getClassLoader();
        try {
            Class ActivityThreadClass = pathClassloader.loadClass("android.app.ActivityThread");
            //public static ActivityThread currentActivityThread()
            Method currentActivityThreadMethod = ActivityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);
            Field mPackagesfield = ActivityThreadClass.getDeclaredField("mPackages");
            mPackagesfield.setAccessible(true);
            ArrayMap mPackagesobj = (ArrayMap) mPackagesfield.get(currentActivityThread);
            if (this != null) {
                String packagename = getPackageName();
                //final ArrayMap<String, WeakReference<LoadedApk>> mPackages = new ArrayMap<>();
                WeakReference wr = (WeakReference) mPackagesobj.get(packagename);
                Object loadedapk = wr.get();
                Class LoadedApkClass = pathClassloader.loadClass("android.app.LoadedApk");
                Field mClassLoaderfield = LoadedApkClass.getDeclaredField("mClassLoader");
                mClassLoaderfield.setAccessible(true);
                ClassLoader pathclassloader = (ClassLoader) mClassLoaderfield.get(loadedapk);
                Log.e("mClassloader", pathClassloader.toString());
                mClassLoaderfield.set(loadedapk, dexclassloader);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void fixClassLoader(ClassLoader classLoader) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        ClassLoader currentClassLoader = getClassLoader();
        ClassLoader parentClassLoader = currentClassLoader.getParent();

        Field parent = ClassLoader.class.getDeclaredField("parent");
        parent.setAccessible(true);

        parent.set(classLoader, parentClassLoader);
        parent.set(currentClassLoader, classLoader);
    }
    /**
     * A native method that is implemented by the 'classloadertest' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    private void EnumClassLoader(){
        ClassLoader classLoader = getClassLoader();
        while(classLoader != null){
            Log.e("st0ne", classLoader.toString() );
            classLoader = classLoader.getParent();
        }
    }

    private void EnumClassInClassLoader(ClassLoader classLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        Class baseDexClassLoaderClass = classLoader.loadClass("dalvik.system.BaseDexClassLoader");
        //Class BaseDexClassLoaderClass1 = Class.forName("dalvik.system.BaseDexClassLoader");
        Field pathListField = baseDexClassLoaderClass.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object dexPathList = pathListField.get(classLoader);

        Class dexPathListClass = classLoader.loadClass("dalvik.system.DexPathList");
        Field dexElementsField =  dexPathListClass.getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        Object[] elements = (Object[]) dexElementsField.get(dexPathList);

        Class elementClass = classLoader.loadClass("dalvik.system.DexPathList$Element");
        Field dexFileField = elementClass.getDeclaredField("dexFile");
        dexFileField.setAccessible(true);
        Class dexFileClass = DexFile.class;

        for (Object element: elements){
            DexFile dexFile = (DexFile) dexFileField.get(element);
//            Method entriesMethod =  dexFileClass.getDeclaredMethod("entries");
//            entriesMethod.setAccessible(true);
//            entriesMethod.invoke()
            Enumeration<String> enumClass = dexFile.entries();
            while(enumClass.hasMoreElements()){
                Log.e("st0ne",  enumClass.nextElement().toString());
            }
        }
    }
}