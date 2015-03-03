package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Map.Entry;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDynamoProvider extends ContentProvider {
	
	//Database Variables
	private static final String DbName="SimpleDynamoDB";
	private static final String TableName="SimpleDynamoTable";
	private static final int version=1;
	SQLiteDatabase SQLitedb;
	SQLiteQueryBuilder QueryBuilder;
	public static Context context;
	public static final String KEY="key";
	public static final String VALUE="value";
	public static final String lDumpQryType="@";
	public static final String gDumpQryType="*";
	
			//----message passing variables
			static String myPort;
			static final String REMOTE_PORT0 = "11108";
		    static final String REMOTE_PORT1 = "11112";
		    static final String REMOTE_PORT2 = "11116";
		    static final String REMOTE_PORT3 = "11120";
		    static final String REMOTE_PORT4 = "11124";
		    static final int SERVER_PORT = 10000;
		    
		    public class DbClass extends SQLiteOpenHelper
			{
				private static final String SQL_CREATE = "CREATE TABLE " +
						TableName +
			            "(" +                           // The columns in the table
			            " key String PRIMARY KEY, " +
			            " value String"+ ")";
				
				
				
				/*private static final String SQL_CREATE =
						"CREATE TABLE GrpMessengerTable (key String PRIMARY KEY,value String)";*/
				
				public DbClass(Context context) {
					super(context, DbName, null, version);
					// TODO Auto-generated constructor stub
				}

				@Override
				public void onCreate(SQLiteDatabase db) {
					// TODO Auto-generated method stub
					 db.execSQL(SQL_CREATE);
					 
				}

				@Override
				public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
					// TODO Auto-generated method stub
					
				}
			}
			
	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		//String Contentkey=values.getAsString(KEY);
 		//String Contentval=values.getAsString(VALUE);
 		
 		//Log.e("1 node inser", Contentkey+"   "+Contentval);
	    	SQLitedb.insert(TableName, null, values);
	       // Log.v("insert", values.toString());
	        return uri;
		
	}

	@Override
	public boolean onCreate() {
		
		context=getContext();
		DbClass dbHelper= new DbClass(context);
     	SQLitedb= dbHelper.getWritableDatabase();
     	
     	//get current port
    	TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        
        
      //start server...
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
           
            Log.e("Server Creation", "Can't create a ServerSocket");
        }
		
        return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		QueryBuilder = new SQLiteQueryBuilder();
    	QueryBuilder.setTables(TableName);
    	
    	if(selection.equalsIgnoreCase(lDumpQryType))
    	   {
    		   String sql= "Select * from "+TableName;
    		  
    	       Cursor cursor= SQLitedb.rawQuery(sql, null);
    			//Log.v("query", selection);
    				
    	       return cursor;
    		   
    	   }
    	if(selection.equalsIgnoreCase(gDumpQryType))
    	{
    		
    	}
    	if ((!selection.equalsIgnoreCase(gDumpQryType)) &&(!selection.equalsIgnoreCase(lDumpQryType)))
    	{
    		String[] columns={"key","value"};
        	Cursor cursor = SQLitedb.query(TableName,columns,"key = "+"'"+selection+"'", null, null, null, null);
            Log.v("query", selection);
            return cursor;
    	
    	}
    	
    	
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

    	@Override
       protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
           // int portNo[]={11108,11112,11116,11120,11124};
            ObjectInputStream inStream = null;
          //  BufferedReader br;
			try {
				while(true)
				{
				inStream = new ObjectInputStream(serverSocket.accept().getInputStream());
				MessageClass msgRecObj=(MessageClass)inStream.readObject();
				
		     			     	
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
				//Log.e(TAG, "error in displaying content");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    
        	
         	
           return null;
        }

       
		protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
        	return;
        }
    }
    
    public class ClientTask implements Runnable {

        private String[] myParam;

        public ClientTask(String... myParam){
            this.myParam = myParam;
            
        }

        public void run(){
            
        	int portToSend= Integer.parseInt(myParam[2]);
        	
        	MessageClass msgObj = null;
        	
        	ObjectOutputStream outToServer=null;
        	try{
        		
                 Socket socket=new Socket("10.0.2.2",portToSend);
                 outToServer = new ObjectOutputStream(socket.getOutputStream());
                    
                    outToServer.writeObject(msgObj);
                    
                    outToServer.flush();
                    outToServer.close();
                    socket.close();
                       
                  
               } catch (UnknownHostException e) {
                  // Log.e(TAG, "ClientTask UnknownHostException");
               } catch (IOException e) {
                  // Log.e(TAG, "ClientTask socket IOException");
               }
        	
        	
        }

    }


}
