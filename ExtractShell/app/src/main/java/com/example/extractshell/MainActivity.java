package com.example.extractshell;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.MediaController;
import android.widget.TextView;

import com.example.extractshell.databinding.ActivityMainBinding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'extractshell' library on application startup.
    static {
        System.loadLibrary("extractshell");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hook();

        try {
            loadDexAndCallFunc();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDexAndCallFunc() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        ClassLoader classLoader = new DexClassLoader("/data/local/tmp/1.dex",
                getCacheDir().getAbsolutePath(),
                null,
                getClassLoader());
        Class clazz = classLoader.loadClass("com.example.plugindex.TestClass");
        Method testFunc = clazz.getDeclaredMethod("testFunc");
        testFunc.setAccessible(true);
        testFunc.invoke(clazz.newInstance());
    }

    /**
     * A native method that is implemented by the 'extractshell' native library,
     * which is packaged with this application.
     */
    public native void hook();
}