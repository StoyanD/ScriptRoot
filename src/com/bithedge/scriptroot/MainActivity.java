package com.bithedge.scriptroot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";

	public static final String SH_LABEL = ".sh";

	public static final String EXC_LABEL = "./";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String filename = getResources().getResourceEntryName(R.raw.dump_reg) + SH_LABEL;
		Log.i(TAG, "filename :  " + filename);
		saveScript(filename, loadFile(R.raw.dump_reg));
		setRoot2(filename);
//		setBlazeRoot(filename);
//		setRoot(saveScript(loadFile()));
//		setRoot(loadFile());

	}
	private String saveScript(String filename, String content){
		FileOutputStream outputStream;
		File file = new File(this.getFilesDir(), filename);


		try {
//		  outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
		  outputStream = openFileOutput(file.getName(), Context.MODE_WORLD_READABLE);
		  outputStream.write(content.getBytes());
		  outputStream.close();
		  return file.getAbsolutePath();
		} catch (Exception e) {
		  e.printStackTrace();
		}
		return null;
	}

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
//			Log.i(TAG, "file : " + sb.toString());
			return sb.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void setRoot2(final String filename){
		Button b = (Button)findViewById(R.id.rootButton);


        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	Process proc = null;
            	try {
            	   proc = Runtime.getRuntime().exec("su", null, new File("/data/data/com.bithedge.scriptroot/files/"));
            	}catch (IOException e) {
            	   e.printStackTrace();
            	}
            	if (proc != null) {
            	   BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            	   PrintWriter out = new PrintWriter(proc.getOutputStream(), true);
            	   out.println("pwd");
            	   out.println("ls -l");
            	   out.println("chmod 744 " + filename);
            	   out.println("ls -l");
            	   out.println(EXC_LABEL + filename);
            	   out.println("exit");
            	   try {
//            		   Log.e(TAG, "try read");
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
            	    	   Log.w(TAG, "setRoot2 fail, starting setBlazeRoot");
            	    	   setBlazeRoot(filename);
            	       }
            	   }catch (Exception e) {
            		   e.printStackTrace();

            	   }
            	}
            }

        });
	}

	private void setBlazeRoot(final String filename){
		Process proc = null;
		BufferedReader in = null;
 	    PrintWriter out = null;
		try {
//	    	   Log.e(TAG, "trying execution again");
	    	   proc = Runtime.getRuntime().exec("chmod 744 " + filename, null, new File("/data/data/com.bithedge.scriptroot/files/"));
	    	   proc = Runtime.getRuntime().exec("ls", null, new File("/data/data/com.bithedge.scriptroot/files/"));
	    	   Log.i(TAG, "executing file  :" + filename);
//		   	   proc = Runtime.getRuntime().exec(EXC_LABEL + filename, null, new File("/data/data/com.bithedge.scriptroot/files/"));
	    	   proc = Runtime.getRuntime().exec("sh " + filename, null, new File("/data/data/com.bithedge.scriptroot/files/"));
		       in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		       out = new PrintWriter(proc.getOutputStream(), true);
		       String line;
		       while ((line = in.readLine()) != null) {
		    	   Log.e(TAG, line);
		       }
	  	       proc.waitFor();
	  	       in.close();
	  	       out.close();
//	  	       proc.destroy();
		 }catch (IOException e1) {
				// TODO Auto-generated catch block
		    	e1.printStackTrace();
		 } catch (InterruptedException e1) {
				// TODO Auto-generated catch block
		  		e1.printStackTrace();
		 }
	}
	/**
	 * 	start here http://stackoverflow.com/questions/9386972/interactive-commands-using-java-runtime-getruntime-exec
     *	remember http://stackoverflow.com/questions/11822500/changing-system-file-permission
	 */

	private void setRoot(final String script){
		Button b = (Button)findViewById(R.id.rootButton);


        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Process process = null;
                BufferedReader bufferedReader = null;
                try {
//                	Log.e(TAG, "path : " + script);
//                	process = Runtime.getRuntime().exec("su -c " + script);
//                	process = Runtime.getRuntime().exec("su -c /data/data/com.bithedge.scriptroot/files/my.sh");
//                	process = Runtime.getRuntime().exec("su -c /data/data/com.bithedge.scriptroot/files/my.sh");
//                    process = new ProcessBuilder()
//                    .command("/data/scripts/simple.sh")
//                    .command(script)
//                    .start();
//                	 process = Runtime.getRuntime().exec("su", null, new File("/system/bin/"));
                	 process = Runtime.getRuntime().exec(new String[] {"chmod 777 /data/data/com.bithedge.scriptroot/files/my.sh;","./data/data/com.bithedge.scriptroot/files/my.sh"});
                     OutputStream os = process.getOutputStream();


//                     os.write(("chmod 777 /data/data/com.bithedge.scriptroot/files/my.sh;").getBytes("ASCII"));
//                     os.flush();
//                     os.close();
                     try {
                    	 process.waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//                     os = process.getOutputStream();
//                     os.write(("sh /data/data/com.bithedge.scriptroot/files/my.sh").getBytes("ASCII"));
//                     os.flush();
//                     os.close();
//                     try {
//                    	 process.waitFor();
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
//                    if(p!=null) p.destroy();
                	if(process !=  null){
                		final StringBuilder log = new StringBuilder();
                		bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                		String line;
                        try {
                        	if(bufferedReader != null){
                        		while ((line = bufferedReader.readLine()) != null){
    							    log.append(line);
    							    log.append("\n");
    							}
                        	}

							Log.e(TAG,"output : " + log.toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                	}
                }
            }
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
