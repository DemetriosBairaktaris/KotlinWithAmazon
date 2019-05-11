package com.kotlin20.kotlinwithamazon

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.amazon.identity.auth.device.api.workflow.RequestContext
import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.*


class MainActivity : AppCompatActivity() {

    private var requestContext: RequestContext? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestContext = RequestContext.create(this.applicationContext)
        requestContext?.registerListener(object : AuthorizeListener() {

            /* Authorization was completed successfully. */
            override fun onSuccess(result: AuthorizeResult) {
                println("Success")
                runOnUiThread {
                    setContentView(R.layout.logged_in)
                    getUserProfileInfo()

                }

            }

            /* There was an error during the attempt to authorize the
        application. */
            override fun onError(ae: AuthError) {
                println("Failed with an error")
            }

            /* Authorization was cancelled before it could be completed. */
            override fun onCancel(cancellation: AuthCancellation) {
                println("User cancelled login request")
            }
        })

    }


    override fun onDestroy() {
        super.onDestroy()
        AuthorizationManager.signOut(this.applicationContext, object : Listener<Void, AuthError> {
            override fun onSuccess(response: Void?) {
                runOnUiThread({
                    setContentView(R.layout.activity_main)
                })
            }

            override fun onError(authError: AuthError?) {
                println("Error when logging out")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        requestContext?.onResume()
    }

    fun onLoginWithAmazon(view: View) {
        AuthorizationManager.authorize(
            AuthorizeRequest.Builder(this.requestContext)
                .addScopes(ProfileScope.profile(), ProfileScope.postalCode())
                .build()
        )
    }

    fun onLogoutWithAmazon(view: View) {
        AuthorizationManager.signOut(this.applicationContext, object : Listener<Void, AuthError> {
            override fun onSuccess(response: Void?) {
                runOnUiThread({
                    setContentView(R.layout.activity_main)
                })
            }

            override fun onError(authError: AuthError?) {
                println("Error when logging out")
            }
        })
    }


    override fun onResumeFragments() {
        super.onStart()

    }


    fun getUserProfileInfo(){

        val scopes = arrayOf<Scope>(ProfileScope.profile(), ProfileScope.postalCode())
        AuthorizationManager.getToken(this, scopes, object : Listener<AuthorizeResult, AuthError> {

            override fun onSuccess(result: AuthorizeResult) {
                if (result.accessToken != null) {
                    User.fetch(applicationContext, object : Listener<User, AuthError> {
                        override fun onSuccess(user: User) {
                            runOnUiThread({
                                val textView: TextView = findViewById(R.id.apptitle) as TextView
                                textView.text = "${user.userName}, you have logged in to Amazon."
                            })
                        }

                        override fun onError(p0: AuthError?) {
                            println("Error in getting profile info")
                        }
                    })
                }
            }

            override fun onError(ae: AuthError) {
                /* The user is not signed in */
            }
        })

    }
}
