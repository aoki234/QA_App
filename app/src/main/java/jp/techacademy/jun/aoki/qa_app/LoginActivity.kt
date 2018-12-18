package jp.techacademy.jun.aoki.qa_app

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth:FirebaseAuth
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDataBaseReference: DatabaseReference


    private var mIsCreateAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        mDataBaseReference = FirebaseDatabase.getInstance().reference


        mAuth = FirebaseAuth.getInstance()

        //アカウント作成
        mCreateAccountListener = OnCompleteListener { task ->
            if(task.isSuccessful){

                val email = emailText.text.toString()
                val password = passwordText.text.toString()
                login(email,password)

            }else{
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view,"アカウントの作成に失敗しました",Snackbar.LENGTH_LONG).show()

                progressBar.visibility = View.GONE

            }
        }

        //ログイン処理
        mLoginListener = OnCompleteListener { task ->
            if (task.isSuccessful){

                val user = mAuth.currentUser
                val userRef = mDataBaseReference.child(UsersPATH).child(user!!.uid)

                if(mIsCreateAccount){

                    val name = nameText.text.toString()
                    val data = HashMap<String,String>()

                    data["name"] = name
                    userRef.setValue(data)
                    //名前の保存
                    saveName(name)
                }else{
                    //データベースから名前を取り出し、preferenceに保存する
                    userRef.addListenerForSingleValueEvent(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as  Map<*,*>?
                            saveName(data!!["name"] as String)
                        }

                        override fun onCancelled(p0: DatabaseError) {
                           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }
                    })
                }

                progressBar.visibility = View.GONE
                //activityの終了
                 finish()

            }else{
                //ログイン処理の失敗
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view,"ログインに失敗しました",Snackbar.LENGTH_LONG).show()

                progressBar.visibility =View.GONE
            }
        }


        title = "ログイン"
        //button1
        createButton.setOnClickListener { v ->

            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val name = nameText.text.toString()

            if(email.length != 0 && password.length >= 6 && name.length != 0){
                mIsCreateAccount = true
                createAccount(email,password)
            }else{

                Snackbar.make(v,"正しく入力してください",Snackbar.LENGTH_LONG).show()
            }
        }

        //button2
        loginButton.setOnClickListener { v ->

            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if (email.length != 0 && password.length >= 6){
                mIsCreateAccount = false
                login(email,password)
            }else{
            Snackbar.make(v,"正しく入力してください",Snackbar.LENGTH_LONG).show()
        }
        }

    }

    private fun createAccount(email:String,password:String){
        progressBar.visibility = View.VISIBLE
        //アカウント作成のリスナーを呼び出し、メールとパスワードでログイン
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(mCreateAccountListener)

    }


    private fun login(email: String,password: String){

        progressBar.visibility = View.VISIBLE
        //
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(mLoginListener)
        print("deeeeeeeeeeeeeeeeeeeeeeeeee")
    }


    private fun saveName(name: String){

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY,name)
        editor.commit()
    }


}
