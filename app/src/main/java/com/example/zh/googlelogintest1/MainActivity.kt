package com.example.zh.googlelogintest1

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.drive.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAG="GoogleLogin"
    private val RC_GOOGLE_LOGIN = 901
    private var REQUEST_OPEN=1029

    private lateinit var loginClient: GoogleSignInClient
    private lateinit var account:GoogleSignInAccount
    private lateinit var driverClient: DriveClient
    private lateinit var resourceClient: DriveResourceClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var loginConfig = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(Scopes.DRIVE_FILE))
                .requestEmail()
                .build()

        loginClient = GoogleSignIn.getClient(this, loginConfig)


        val accountTemp= GoogleSignIn.getLastSignedInAccount(this)
        if(accountTemp!=null){
            account=accountTemp
            driverClient=Drive.getDriveClient(this,accountTemp)
            resourceClient=Drive.getDriveResourceClient(this,accountTemp)
            var clientRes=Drive.getDriveResourceClient(this,accountTemp)
            Toast.makeText(this, "已登录", Toast.LENGTH_SHORT).show()
        }

        button2.setOnClickListener{
            var option=OpenFileActivityOptions.Builder()
                    .setActivityTitle("Select File").build()
            pickItem(option).addOnSuccessListener {
                Toast.makeText(this, "in success", Toast.LENGTH_SHORT).show()
            }

        }

        sign_in_button.setOnClickListener {
            signIn()
        }
    }

    fun pickItem(option: OpenFileActivityOptions):Task<DriveId>{

        var a=TaskCompletionSource<DriveId>()


        driverClient.newOpenFileActivityIntentSender(option)
                .continueWith{
                    startIntentSenderForResult(it.result,REQUEST_OPEN,null,
                            0,0,0)
                }.addOnSuccessListener {
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                }

        return a.task
    }

    fun signIn() {
        val intent = loginClient.signInIntent
        Toast.makeText(this,"",Toast.LENGTH_SHORT).show()
        startActivityForResult(intent, RC_GOOGLE_LOGIN)

    }



    fun handleSignInTask(task: Task<GoogleSignInAccount>) {
        try {
            account = task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            Log.w("googlelogin", e.message + e.statusCode)
        }
        Toast.makeText(this,"${account.displayName}:${account.email}",Toast.LENGTH_LONG).show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            RC_GOOGLE_LOGIN->{
                var task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInTask(task)
            }
            REQUEST_OPEN->{
                if(resultCode== RESULT_OK){
                    val driveId=data?.getParcelableExtra<DriveId>(OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID)
                    Toast.makeText(this, "driveId=${driveId?.encodeToString()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}

