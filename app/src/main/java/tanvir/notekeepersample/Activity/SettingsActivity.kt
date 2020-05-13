package tanvir.notekeepersample.Activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Switch

import com.kaopiz.kprogresshud.KProgressHUD
import com.sdsmdg.tastytoast.TastyToast

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList
import tanvir.notekeepersample.Database.DatabaseHelper
import tanvir.notekeepersample.R

class SettingsActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private lateinit var aSwitch: Switch
    private lateinit var dialogView: View
    private lateinit var alertDialog: AlertDialog
    private lateinit var databaseHelper: DatabaseHelper
    private var permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        databaseHelper = DatabaseHelper(this)
        aSwitch = findViewById<View>(R.id.LockSwitch) as Switch
        val result = checkLock()
        setStickySwitch(result)
        ////aSwitch.setChecked(true);
        supportActionBar!!.show()
        supportActionBar!!.title = "Setting..."

        aSwitch.setOnCheckedChangeListener(this as CompoundButton.OnCheckedChangeListener)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val sharedPreferences = getSharedPreferences("lock", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("came_from_which_activity?", "setting")
        editor.commit()
        val myIntent = Intent(this@SettingsActivity, MainActivity::class.java)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        this@SettingsActivity.startActivity(myIntent)
        overridePendingTransition(R.anim.right_in, R.anim.right_out)
        finish()

    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        if (aSwitch.isChecked) {
            showSetPasswordAlertDialog()
        } else {
            val sharedPreferences = getSharedPreferences("lock", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("is_it_lock_in_settings?", "no")
            editor.putString("password", "")
            editor.commit()
        }

    }

    fun checkLock(): Boolean {
        val sharedPreferences = getSharedPreferences("lock", Context.MODE_PRIVATE)
        val checkPassword = sharedPreferences.getString("is_it_lock_in_settings?", "")
        return checkPassword!!.contains("yes")
    }

    override fun onResume() {
        super.onResume()
        val result = checkLock()
        setStickySwitch(result)
    }

    fun setStickySwitch(result: Boolean) {
        aSwitch.isChecked = result
    }


    fun SavePasswordInformation(view: View) {
        val passwordET = dialogView.findViewById<EditText>(R.id.password)
        val confirmPasswordET = dialogView.findViewById<EditText>(R.id.confirm_password)
        val gmailET = dialogView.findViewById<EditText>(R.id.gmail)
        val password = passwordET.text.toString()
        val confirmPassword = confirmPasswordET.text.toString()
        val gmail = gmailET.text.toString()
        if (password.length > 0 && confirmPassword.length > 0 && gmail.length > 0) {
            if (password != confirmPassword) {
                TastyToast.makeText(applicationContext, "Password doesn't match", TastyToast.LENGTH_SHORT, TastyToast.ERROR)
                showSetPasswordAlertDialog()
            } else if (!gmail.contains("@")) {
                TastyToast.makeText(applicationContext, "Please enter valid gmail address", TastyToast.LENGTH_SHORT, TastyToast.ERROR)

                if (!gmail.contains("gmail")) {
                    TastyToast.makeText(applicationContext, "Please enter valid gmail address", TastyToast.LENGTH_SHORT, TastyToast.ERROR)
                }
                showSetPasswordAlertDialog()
            } else {
                val sharedPreferences = getSharedPreferences("lock", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                editor.putString("is_it_lock_in_settings?", "yes")
                editor.putString("password", password)
                editor.putString("gmail", gmail)
                editor.commit()
                alertDialog.cancel()
                TastyToast.makeText(applicationContext, "Security is on ", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS)
                alertDialog.cancel()

            }

        } else {
            TastyToast.makeText(applicationContext, "Please fill up all field !", TastyToast.LENGTH_SHORT, TastyToast.WARNING)
        }
    }

    fun showSetPasswordAlertDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        dialogView = inflater.inflate(R.layout.set_password, null)
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create()
        alertDialog.show()
    }


    @Throws(IOException::class)
    fun backupData(view: View) {

        checkPermissions()
        var success = true
        var file: File? = null
        file = File(Environment.getExternalStorageDirectory().toString() + "/NOteKeeperBackup")

        if (file.exists()) {
            success = true
        } else {
            success = file.mkdir()
        }

        if (success) {
            val hud: KProgressHUD

            hud = returnProgressDialog()
            val backupAS = Backup(this@SettingsActivity, hud)
            backupAS.execute()
        }
    }

    @Throws(IOException::class)
    fun importData(view: View) {
        checkPermissions()
        var success = true
        var file: File? = null
        file = File(Environment.getExternalStorageDirectory().toString() + "/NOteKeeperBackup")

        if (file.exists()) {
            success = true
        } else {
            success = file.mkdir()
        }

        if (success) {
            val hud: KProgressHUD

            hud = returnProgressDialog()
            val backupAS = Restore(this@SettingsActivity, hud)
            backupAS.execute()
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
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 100)
            return false
        }
        return true
    }


    private inner class Backup(private val context: Context, private var hud: KProgressHUD) : AsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            hud.show()
            ///progressDialog = ProgressDialog.show(context,"Sending message","Please wait...",false,false);
        }

        override fun doInBackground(vararg voids: Void): Void? {

         /*   var myExternalFile:File = File(getExternalFilesDir(filepath),fileName)
            try {
                val fileOutPutStream = FileOutputStream(myExternalFile)
                fileOutPutStream.write(fileContent.toByteArray())
                fileOutPutStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }*/

            /*    val inFileName = "/data/data/tanvir.notekeepersample/databases/DatabaseForNote"
                val dbFile = File(inFileName)
                var fis: FileInputStream? = null
                try {
                    fis = FileInputStream(dbFile)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

                val outFileName = Environment.getExternalStorageDirectory().toString() + "/NOteKeeperBackup/NOteKeeperBackup.s3db"

                // Open the empty db as the output stream
                var output: OutputStream? = null
                try {
                    output = FileOutputStream(outFileName)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

                // Transfer bytes from the inputfile to the outputfile
                val buffer = ByteArray(1024)
                var length: Int
                try {
                    while ((length = fis!!.read(buffer)) > 0) {
                        output!!.write(buffer, 0, length)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }


                try {
                    output!!.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                try {
                    output!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                try {
                    fis!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }*/

            return null
        }


        override fun onPostExecute(aVoid: Void) {
            hud.dismiss()
            this@SettingsActivity.runOnUiThread { TastyToast.makeText(applicationContext, "Backup Success", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS) }
        }
    }

    fun returnProgressDialog(): KProgressHUD {
        return KProgressHUD.create(this@SettingsActivity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setDimAmount(0.6f)
                .setLabel("Please Wait")
                .setCancellable(false)
    }


    private inner class Restore(private val context: Context, private var hud: KProgressHUD) : AsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            hud.show()
            ///progressDialog = ProgressDialog.show(context,"Sending message","Please wait...",false,false);
        }

        override fun doInBackground(vararg voids: Void): Void? {

 /*           databaseHelper.deleteAllData()
            val inFileName = Environment.getExternalStorageDirectory().toString() + "/NOteKeeperBackup/NOteKeeperBackup.s3db"
            val dbFile = File(inFileName)
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(dbFile)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            val outFileName = "/data/data/tanvir.notekeepersample/databases/DatabaseForNote"

            // Open the empty db as the output stream
            var output: OutputStream? = null
            try {
                output = FileOutputStream(outFileName)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            // Transfer bytes from the inputfile to the outputfile
            val buffer = ByteArray(1024)
            var length: Int
            try {
                while ((length = fis!!.read(buffer)) > 0) {
                    output!!.write(buffer, 0, length)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                output!!.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                output!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                fis!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
*/
            return null
        }


        override fun onPostExecute(aVoid: Void) {
            hud.dismiss()
            this@SettingsActivity.runOnUiThread { TastyToast.makeText(applicationContext, "Import Success", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS) }
        }
    }

}
