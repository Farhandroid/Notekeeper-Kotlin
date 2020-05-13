package tanvir.notekeepersample.Email

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.widget.Toast

import com.kaopiz.kprogresshud.KProgressHUD

import java.util.Properties

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class SendMail(private val context: Context, private val email: String, private val subject: String, private val message: String, internal var hud: KProgressHUD) : AsyncTask<Void, Void, Void>() {
    private var session: Session? = null
    private val progressDialog: ProgressDialog? = null
    override fun onPreExecute() {
        super.onPreExecute()
        hud.show()
        ///progressDialog = ProgressDialog.show(context,"Sending message","Please wait...",false,false);
    }
    override fun onPostExecute(aVoid: Void) {
        super.onPostExecute(aVoid)
        hud.dismiss()
        //progressDialog.dismiss();
        Toast.makeText(context, "Message Sent", Toast.LENGTH_LONG).show()
    }

    override fun doInBackground(vararg params: Void): Void? {
        val props = Properties()
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.socketFactory.port"] = "465"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"
        session = Session.getDefaultInstance(props,
                object : javax.mail.Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(Config.EMAIL, Config.PASSWORD)
                    }
                })
        try {
            val mm = MimeMessage(session)
            mm.setFrom(InternetAddress(Config.EMAIL))
            mm.addRecipient(Message.RecipientType.TO, InternetAddress(email))
            mm.subject = subject
            mm.setText(message)
            Transport.send(mm)
        } catch (e: MessagingException) {
            e.printStackTrace()
        }

        return null
    }
}
