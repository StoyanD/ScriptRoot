package com.bithedge.scriptroot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * A activity that executes a file as root.
 * <p>
 * @author Stoyan Dimitrov
 */
public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";

	public static final String SH_LABEL = ".sh";

	public static final String EXC_LABEL = "sh ";

	/**
	 * 	start here http://stackoverflow.com/questions/9386972/interactive-commands-using-java-runtime-getruntime-exec
     *	remember http://stackoverflow.com/questions/11822500/changing-system-file-permission
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String filename = getResources().getResourceEntryName(R.raw.single_reg_dump) + SH_LABEL;
		Log.i(TAG, "filename :  " + filename);
		File file = saveScript(filename, loadFile(R.raw.single_reg_dump));
		Log.i(TAG, "file dir : " + file.getParent());
		initLayout(file);

	}

	/**
	 * Initialize the layout in the activity to execute the file as root
	 * @param file The file to execute as root with {@link #execRoot(File)}
	 */
	private void initLayout(final File file){
		Button b = (Button) findViewById(R.id.rootButton);
		EditText et = (EditText) findViewById(R.id.terminal_textview);
		et.setSelection(et.getText().length());//move cursor to end of text

        b.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0) {
            	EditText et = (EditText) findViewById(R.id.terminal_textview);
//            	execBlazeRoot(file, toParams(et.getText().toString()));
            	execRoot(file, toParams(et.getText().toString()));
            }
        });
	}
	/**
	 * Saves a file into the package/files directory
	 * <p>
	 * @param filename The name of the file including the file extension type, ie. .txt, .sh,...
	 * @param content The content of the file
	 * @return The saved file
	 */
	private File saveScript(String filename, String content){
		FileOutputStream outputStream;
		File file = new File(this.getFilesDir(), filename);

		try {
		  outputStream = openFileOutput(file.getName(), Context.MODE_PRIVATE);
		  outputStream.write(content.getBytes());
		  outputStream.close();
		  return file;
		} catch (Exception e) {
		  e.printStackTrace();
		}
		return null;
	}

	/**
	 * Loads a file from the res/raw directory and reads its contents
	 * @param res_id The resource ID of the res/raw file
	 * @return The string contents of the file
	 */
	private String loadFile(int res_id){
		InputStream databaseInputStream = getResources().openRawResource(res_id);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(databaseInputStream));
		String line;
		final StringBuilder sb = new StringBuilder();
		try {
			while ((line = bufferedReader.readLine()) != null){
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Executes a file as root. Changes file permissions to 744 -rwxr--r--
	 * @param file The file to be executed as root
	 * @param params The bash script parameters that the file should be called with. Make sure
	 * 				that there is spacing between each parameters as well as at the start of the first parameter, use {@link #toParams(String)}
	 */
	private void execRoot(final File file, final String params){
		Process proc = null;

    	try {
    	   proc = Runtime.getRuntime().exec("su", null, file.getParentFile());
    	}catch (IOException e) {
    	   e.printStackTrace();
    	}
    	if (proc != null) {
    	   BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    	   PrintWriter out = new PrintWriter(proc.getOutputStream(), true);
    	   out.println("pwd");
    	   out.println("ls -l");
    	   out.println("chmod 744 " + file);
    	   out.println("ls -l");
    	   out.println(EXC_LABEL + file.getName() + params);
    	   out.println("exit");
    	   try {
    	       String line;
    	       while ((line = in.readLine()) != null) {
    	    	   Log.e(TAG, line);
    	       }
    	       proc.waitFor();
    	       in.close();
    	       out.close();
    	       proc.destroy();

    	       if(proc.exitValue() == 1){
    	    	   Log.w(TAG, "proc.exitValue() : 	" + proc.exitValue());
    	    	   Log.w(TAG, "execRoot fail, starting execBlazeRoot");
    	    	   execBlazeRoot(file, params);
    	       }
    	   }catch (Exception e) {
    		   e.printStackTrace();
    	   }
    	}
	}

	/**
	 * Execute a file as root on a device that has a process that cannot be writen to after creation, ie. Blaze tablet.
	 * Changes file permissions to 744 -rwxr--r--
	 * <p>
	 * @param file the file to execute as root
	 * @param params The bash script parameters that the file should be called with. Make sure
	 * 				that there is spacing between each parameters as well as at the start of the first parameter, use {@link #toParams(String)}
	 */
	private void execBlazeRoot(final File file, final String params){ //<<< check if ans is always whatever was in files, ie no write permissions>
		Process proc = null;
 	    /**
 	     * Note: Make sure basic_reg_addr and basic_reg_val are world writable in order for scripts to function accurately
 	     */
// 	    File basic_reg_addr = new File("/sys/devices/platform/omap/omap_i2c.3/i2c-3/3-0078/basic_reg_addr");
// 	    File basic_reg_val = new File("/sys/devices/platform/omap/omap_i2c.3/i2c-3/3-0078/basic_reg_val");

		try {
	    	   proc = Runtime.getRuntime().exec("chmod 744 " + file, null, file.getParentFile());
	    	   logExecProcess(proc, "chmod 744 to : " + file.getName());
	    	   proc = Runtime.getRuntime().exec("ls", null, file.getParentFile());
	    	   logExecProcess(proc);

	    	   Log.i(TAG, "executing file : " + file.getName());
	    	   proc = Runtime.getRuntime().exec(EXC_LABEL + file.getName() + params, null, file.getParentFile());
	    	   logExecProcess(proc);
	  	       proc.destroy();//TODO Blaze fails to destroy process
		 }catch (IOException e1) {
		    	e1.printStackTrace();
		 }
	}

	private void logExecProcess(Process proc, String tag){
		if(tag != null){
			Log.i(TAG, tag);
		}
 	    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
 	    BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
 	    PrintWriter out = new PrintWriter(proc.getOutputStream(), true);
	    String line;

	    try {
			while ((line = in.readLine()) != null) {
				Log.w(TAG, line);
			}
			while ((line = err.readLine()) != null) {
				Log.e(TAG, line);
			}
			proc.waitFor();
		    in.close();
		    out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void logExecProcess(Process proc){
		logExecProcess(proc, null);
	}
	/**
	 * A method that checks that the input starts with a space, adds it if it is not present
	 * @param text The text to check
	 * @return text with the proper space at the begining
	 */
	private String toParams(String text){
		if(text.indexOf(" ") != 0){
			return " " + text;
		}
		return text;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
