package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.userAuth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.LoginPage
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.MainActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import java.io.File

class UserAuthFragment : Fragment() {
    private lateinit var notificationsViewModel: UserAuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProviders.of(this).get(UserAuthViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_schedule, container, false)
        val signOutbutton: Button = root.findViewById(R.id.sign_out_button)
        signOutbutton.setOnClickListener {
            var file: File = File((activity?.filesDir?.absolutePath ?: "") + "/GuestId.txt")
            if (file.exists()) {
                Log.d("UserAuthFragment", "deleted")
                file.delete()
                //Log.d("key: ", (activity as MainActivity).controller.db.key.toString())
                (activity as MainActivity).controller.db.key?.let { it1 ->
                    (activity as MainActivity).controller.db.child(
                        it1
                    ).removeValue()
                }
                (activity as MainActivity).controller.db.removeValue()
                var intent: Intent = Intent(context, LoginPage::class.java)
                startActivity(intent)
            }
            else {
                val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
                var mGoogleSignInClient = activity?.let { GoogleSignIn.getClient(it, gso) }
                if (mGoogleSignInClient != null) {
                    activity?.let { it1 ->
                        mGoogleSignInClient.signOut()
                            .addOnCompleteListener(it1, OnCompleteListener<Void?> {
                                // ...
                                Log.d("UserAuthFragment", "Signed out successfully!")
                                //finish()
                                val intent = Intent(context, LoginPage::class.java)
                                startActivity(intent)
                            })
                    }
                }
            }
        }
        return root
    }
}