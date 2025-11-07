package com.day.mate

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class GoogleSignInManager(private val context: Context, private val webClientId: String) {

    // دالة تعطي Intent جاهز للـ Sign-In
    fun getSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val client: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

        // ده اللي هيخليه يختار Account كل مرة
        return client.signInIntent
    }

    fun signOut(onComplete: () -> Unit = {}) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val client: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
        client.signOut().addOnCompleteListener { onComplete() }
    }
}
