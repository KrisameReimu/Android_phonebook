package hk.edu.polyu.eie3109.phonebook;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ListView myPhoneList;
    SimpleCursorAdapter myCursorAdaptor;
    Button displayNumbersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myPhoneList = findViewById(R.id.LVPhoneList);
        displayNumbersButton = findViewById(R.id.btnDisplayNumbers);
        displayNumbersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedItemPosition = myPhoneList.getCheckedItemPosition();
                if (selectedItemPosition != ListView.INVALID_POSITION) {
                    Cursor cursor = (Cursor) myCursorAdaptor.getItem(selectedItemPosition);
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    Cursor phoneCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    StringBuilder phoneNumbers = new StringBuilder();
                    while (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNumbers.append(phoneNumber).append("\n");
                    }
                    if (phoneNumbers.length() > 0) {
                        Toast.makeText(MainActivity.this, name + ":\n" + phoneNumbers.toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "No phone numbers found for " + name, Toast.LENGTH_SHORT).show();
                    }
                    phoneCursor.close();
                } else {
                    Toast.makeText(MainActivity.this, "No item selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        showContacts();
    }

    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);
            //After this point, you wait for a callback in the onRequestPermissionsResult(int, String[], int[]) overridden method
        } else {
            //Code to query the phone numbers
            final ContentResolver cr = getContentResolver();
            Cursor c = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    new String[]{ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME},
                    null, null, null);
            myCursorAdaptor = new SimpleCursorAdapter(this, R.layout.list_item, c,
                    new String[]{ContactsContract.Contacts.DISPLAY_NAME}, new int[]{R.id.TVRow},
                    0);
            myPhoneList.setAdapter(myCursorAdaptor);
            myPhoneList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Cursor cursor = (Cursor) myCursorAdaptor.getItem(position);
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    Cursor phoneCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null);
                    StringBuilder phoneNumbers = new StringBuilder();
                    while (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNumbers.append(phoneNumber).append("\n");
                        Toast.makeText(MainActivity.this, phoneNumber, Toast.LENGTH_SHORT).show();
                        Log.d("Contact Details", "Phone number: " + phoneNumber); // Add this line
                    }
                    if (phoneNumbers.length() > 0) {
                        Toast.makeText(MainActivity.this, name + ":\n" + phoneNumbers.toString(), Toast.LENGTH_LONG).show();
                        Log.d("Toast Details", "Phone number: " + phoneNumbers);
                    } else {
                        Toast.makeText(MainActivity.this, "No phone numbers found for " + name, Toast.LENGTH_SHORT).show();
                    }
                    phoneCursor.close();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
