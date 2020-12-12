package edu.ucsb.cs.cs184.ashleyswang.schoolplanner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import java.io.*


class UserPortal : AppCompatActivity() {
    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var id: TextView
    private lateinit var readText: TextView
    private lateinit var signOutButton: Button
    private lateinit var readFileButton: Button
    private lateinit var writeFileButton: Button

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_portal)

        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        name = findViewById(R.id.name)
        email = findViewById(R.id.email)
        id = findViewById(R.id.id)
        readText = findViewById(R.id.text_parsed_from_file)
        signOutButton = findViewById(R.id.sign_out_button)
        readFileButton = findViewById(R.id.read_id_from_file)
        writeFileButton = findViewById(R.id.write_id_to_file)

        signOutButton.setOnClickListener {
            when (it.getId()) {
                R.id.sign_out_button -> signOut()
            }
        }

        readFileButton.setOnClickListener {
            when (it.getId()) {
                R.id.read_id_from_file -> readFile()
            }
        }

        writeFileButton.setOnClickListener {
            when (it.getId()) {
                R.id.write_id_to_file -> writeFile()
            }
        }

        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            val personName = acct.displayName
            val personGivenName = acct.givenName
            val personFamilyName = acct.familyName
            val personEmail = acct.email
            val personId = acct.id
            val personPhoto: Uri? = acct.photoUrl

            name.text = personName
            email.text = personEmail
            id.text = personId
            //Glide.with(this).load(personPhoto.toString()).into(image)
        }
    }

    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this, OnCompleteListener<Void?> {
                // ...
                Toast.makeText(this, "Signed out successfully!", Toast.LENGTH_LONG).show()
                //finish()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            })
    }

    private fun writeFile() {
        var textToSave = name.text.toString()
        try {
            var fileOutputStream: FileOutputStream = openFileOutput("TestFile.txt", Context.MODE_PRIVATE)
            //val fileOutputStream: FileOutputStream = FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/TestFile.txt")
            fileOutputStream.write(textToSave.toByteArray())
            fileOutputStream.close()
            Toast.makeText(this, "Wrote to file: " + textToSave, Toast.LENGTH_LONG).show()
        }
        catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun readFile() {
        Toast.makeText(this, "in ReadFile", Toast.LENGTH_LONG).show()
        try {
            //val reader = BufferedReader(someStream)
            //for (line in reader.lines()) {
            //    println(line)
            //}
            /*
            var fileInputStream: FileInputStream = openFileInput("TestFile.txt")
            //var inputStreamReader: InputStreamReader = InputStreamReader(fileInputStream)
            val dataInputStream = DataInputStream(fileInputStream)
            val bufferedReader = BufferedReader(InputStreamReader(dataInputStream))
            //var bufferedReader: BufferedReader = BufferedReader(inputStreamReader)
            //var stringBuffer: StringBuffer = StringBuffer()
            var data: String = ""

            var lines: String
            lines = bufferedReader.readLine()
            while (lines  != null) {
                data = data + lines + "\n"
                lines = bufferedReader.readLine()
            }*/
            var data: String = ""
            val path = this.filesDir.absolutePath + "/TestFile.txt"
            data = File(path).bufferedReader().readLines().toString()
            readText.text = data
            Toast.makeText(this, "App has read the following data: " + data, Toast.LENGTH_LONG).show()
            //bufferedReader.close()
            //dataInputStream.close()
            //fileInputStream.close()
        }
        catch (e: FileNotFoundException) {
            e.printStackTrace()
            //Log.d(UserPortal::class.qualifiedName, e.printStackTrace())
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //https://stackoverflow.com/questions/43710317/is-storing-data-using-file-input-output-stream-method-secure
    //https://www.youtube.com/watch?v=CRrDJkkzY2A
}