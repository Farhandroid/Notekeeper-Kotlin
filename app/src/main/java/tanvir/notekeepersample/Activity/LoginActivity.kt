package tanvir.notekeepersample.Activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import com.kaopiz.kprogresshud.KProgressHUD
import com.sdsmdg.tastytoast.TastyToast
import java.util.ArrayList
import tanvir.notekeepersample.Email.SendMail
import tanvir.notekeepersample.R

class LoginActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var dialogView: View
    private lateinit var alertDialog: AlertDialog

    internal var permissions = arrayOf(Manifest.permission.INTERNET, Manifest.permission.READ_PHONE_STATE)


    private val isOnline: Boolean
        get() {

            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            return if (netInfo != null && netInfo.isConnectedOrConnecting) {
                true
            } else {
                false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }


    fun checkLolgin(view: View) {
        val isPasswordRight = getSharedPreferences("lock", Context.MODE_PRIVATE)
        val password = isPasswordRight.getString("password", "")
        editText = findViewById<View>(R.id.paswordET) as EditText
        val passwordFromLoginPage = editText.text.toString()
        if (passwordFromLoginPage.contains(password!!)) {
            val sharedPreferences = getSharedPreferences("lock", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("came_from_which_activity?", "log_in")
            editor.commit()
            val myIntent = Intent(this@LoginActivity, MainActivity::class.java)
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            this@LoginActivity.startActivity(myIntent)
            overridePendingTransition(R.anim.right_in, R.anim.right_out)
            finish()
        } else {
            editText.text.clear()
            val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
            editText.startAnimation(shake)
        }
    }


    fun clickOnforgotPassword(view: View) {

        val isPasswordRight = getSharedPreferences("lock", Context.MODE_PRIVATE)
        val gmail = isPasswordRight.getString("gmail", "")
        if (gmail!!.length == 0)
            TastyToast.makeText(applicationContext, "Gmail id missing ", TastyToast.LENGTH_SHORT, TastyToast.WARNING)
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.forgot_password, null)
        val textView = dialogView.findViewById<TextView>(R.id.hint)
        textView.text = "Hint : " + customizeGmailForHint(gmail)
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    fun customizeGmailForHint(gmail: String): String {
        val position = gmail.indexOf("@")
        var copyGmail = ""

        for (i in 0..position - 3) {
            copyGmail += "*"
        }
        copyGmail += gmail.substring(position - 3, gmail.length)
        return copyGmail
    }

    fun SendMail(view: View) {
        checkPermissions()

        if (isOnline) {
            val hud = KProgressHUD.create(this@LoginActivity)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setDimAmount(0.6f)
                    .setLabel("Please Wait")
                    .setCancellable(false)

            val sharedPreferences = getSharedPreferences("lock", Context.MODE_PRIVATE)
            val gmail = sharedPreferences.getString("gmail", "")
            val password = sharedPreferences.getString("password", "")
            val message = "Hello user , Your password for Notekeeper is $password \nHave a good day "
            val gmailET = dialogView.findViewById<EditText>(R.id.gmailInForgotPassword)
            val gmailFromForgotPassword = gmailET.text.toString()
            if (gmailFromForgotPassword == gmail) {
                val sm = SendMail(this@LoginActivity, gmail, "password recovery ", message, hud)
                sm.execute()
                alertDialog.cancel()
            } else {
                TastyToast.makeText(applicationContext, "Gmail doesn't macth ", TastyToast.LENGTH_SHORT, TastyToast.ERROR)
            }

        } else {
            TastyToast.makeText(applicationContext, "Please turn on Wifi or mobile data ", TastyToast.LENGTH_LONG, TastyToast.ERROR)
        }
    }

    private fun checkPermissions(): Boolean {
        var result: Int
        val listPermissionsNeeded = ArrayList<String>()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray<String>(), 100)
            return false
        }
        return true
    }


}
