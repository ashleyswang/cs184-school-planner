package edu.ucsb.cs.cs184.ashleyswang.schoolplanner

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications.AppNotificationChannel
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications.NotificationsBroadcastReceiver
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class LoginPage : AppCompatActivity() {
    private lateinit var button: SignInButton
    private lateinit var guestButton: Button
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        var file: File = File(this.filesDir.absolutePath + "/GuestId.txt")
        if (file.exists()) {
            //continue using this
            var username: String = readFile()
            var signInIntent: Intent = Intent(this, MainActivity::class.java)
            signInIntent.putExtra("isGoogleSignIn", false)
            signInIntent.putExtra("user", username)
            startActivity(signInIntent)
        }

        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        button = this.findViewById(R.id.sign_in_button)
        button.setOnClickListener{
            when (it.getId()) {
                R.id.sign_in_button -> signIn()
            }
        }
        guestButton = this.findViewById(R.id.continue_as_guest)
        guestButton.setOnClickListener {
            when (it.getId()) {
                R.id.continue_as_guest -> guestSignIn()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
// the GoogleSignInAccount will be non-null.
        // Check for existing Google Sign In account, if the user is already signed in
// the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        //updateUI(account)
    }

    private fun signIn() {
        Toast.makeText(this, "opening Sign in!", Toast.LENGTH_LONG).show()
        var signInIntent: Intent = mGoogleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun guestSignIn() {
        Toast.makeText(this, "opening Guest Sign in!", Toast.LENGTH_LONG).show()
        var file: File = File(this.filesDir.absolutePath + "/GuestId.txt")
        //writeToFile and pass along our guestId
        var username: String = writeFile()
        var signInIntent: Intent = Intent(this, MainActivity::class.java)
        signInIntent.putExtra("isGoogleSignIn", false)
        signInIntent.putExtra("user", username)
        startActivity(signInIntent)
    }

    private fun writeFile(): String {
        var textToSave = Scope.randomString()
        try {
            var fileOutputStream: FileOutputStream = openFileOutput("GuestId.txt", Context.MODE_PRIVATE)
            //val fileOutputStream: FileOutputStream = FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/TestFile.txt")
            fileOutputStream.write(textToSave.toByteArray())
            fileOutputStream.close()
            Toast.makeText(this, "Wrote to file: " + textToSave, Toast.LENGTH_LONG).show()
            return textToSave
        }
        catch (e: FileNotFoundException) {
            Log.d("WriteFile", "FileNotFoundException")
            e.printStackTrace()
        }
        catch (e: IOException) {
            Log.d("WriteFile", "IOException")
            e.printStackTrace()
        }
        return "error"
    }

    private fun readFile(): String {
        try {
            var data: String = ""
            val path = this.filesDir.absolutePath + "/GuestId.txt"
            data = File(path).bufferedReader().readLine().toString()
            val re = Regex("[^A-Za-z0-9 ]") //only alphanumeric
            data = re.replace(data, "")
            Toast.makeText(this, "App has read the following data: " + data, Toast.LENGTH_LONG).show()
            return data
        }
        catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
        return "error" //this shouldn't be the case
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("here", "right here")
        Log.d("requestCode", requestCode.toString())

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.d("beepBoop","requestCode accepted")
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account =
                completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            //updateUI(account)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("isGoogleSignIn", true)
            Log.d("LoginPage", "starting intent")
            startActivity(intent)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(MainActivity::class.qualifiedName, "signInResult:failed code=" + e.statusCode)
            //updateUI(null)
        }
    }

}