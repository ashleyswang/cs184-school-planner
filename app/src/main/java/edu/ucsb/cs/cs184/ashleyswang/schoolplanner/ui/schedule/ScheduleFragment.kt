package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.LoginPage
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.MainActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications.AppNotificationChannel
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications.NotificationsBroadcastReceiver
import java.io.File

class ScheduleFragment : Fragment() {
    private lateinit var notificationsViewModel: ScheduleViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProviders.of(this).get(ScheduleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_schedule, container, false)
        val signOutbutton: Button = root.findViewById(R.id.sign_out_button)
        signOutbutton.setOnClickListener {
            var file: File = File((activity?.filesDir?.absolutePath ?: "") + "/GuestId.txt")
            if (file.exists()) {
                Log.d("ScheduleFragment", "deleted")
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
                                Log.d("ScheduleFragment", "Signed out successfully!")
                                //finish()
                                val intent = Intent(context, LoginPage::class.java)
                                startActivity(intent)
                            })
                    }
                }
            }
        }
        val fab: FloatingActionButton = root.findViewById(R.id.add_course)
        fab.setImageResource(R.drawable.ic_add_24px)
        fab.setOnClickListener {
            //define a new Intent for the second Activity
            val intent = Intent(context, CourseFormActivity::class.java)
            intent.putExtra("userId", "test")

            //start the second Activity
            this.startActivity(intent)
            //startActivityForResult(intent, STATIC_RESULT);

            Log.d("hello", "in terms fragment, in fab")
        }
        return root
    }
}

/*
package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.schedule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.LoginPage
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.MainActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import java.io.File

class ScheduleFragment : Fragment() {

    private lateinit var notificationsViewModel: ScheduleViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProviders.of(this).get(ScheduleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_schedule, container, false)
        val textView: TextView = root.findViewById(R.id.text_notifications)
        val fab: FloatingActionButton = root.findViewById(R.id.add_course)
        fab.setImageResource(R.drawable.ic_add_24px)
        fab.setOnClickListener {
            //define a new Intent for the second Activity
            val intent = Intent(context, CourseFormActivity::class.java)
            intent.putExtra("userId", "test")

            //start the second Activity
            this.startActivity(intent)
            //startActivityForResult(intent, STATIC_RESULT);

            Log.d("hello", "in terms fragment, in fab")
        }

        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        var mGoogleSignInClient = activity?.let { GoogleSignIn.getClient(it, gso) }

        val signOutButton = root.findViewById<Button>(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            if (mGoogleSignInClient != null) {
                activity?.let { it1 ->
                    mGoogleSignInClient.signOut()
                        .addOnCompleteListener(it1, OnCompleteListener<Void?> {
                            // ...
                            //finish()
                            val intent = Intent(context, LoginPage::class.java)
                            startActivity(intent)
                        })
                }
            }
            else {
                var file: File = File((activity?.filesDir?.absolutePath ?: "") + "/GuestId.txt")
                if (file.exists()) {
                    file.delete()
                }
            }
        }


        return root
    }
}*/