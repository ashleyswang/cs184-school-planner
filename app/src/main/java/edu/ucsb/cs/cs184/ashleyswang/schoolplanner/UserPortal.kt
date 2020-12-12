package edu.ucsb.cs.cs184.ashleyswang.schoolplanner

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener


class UserPortal : AppCompatActivity() {
    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var id: TextView
    private lateinit var image: ImageView
    private lateinit var button: Button

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
        image = findViewById(R.id.image)
        button = findViewById(R.id.button)


        button.setOnClickListener {
            when (it.getId()) {
                R.id.button -> signOut()
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

            name.setText(personName)
            email.setText(personEmail)
            id.setText(personId)
            Glide.with(this).load(personPhoto.toString()).into(image)
        }
    }

    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this, OnCompleteListener<Void?> {
                // ...
                Toast.makeText(this, "Signed out successfully!", Toast.LENGTH_LONG).show()
                finish()
            })
    }


}