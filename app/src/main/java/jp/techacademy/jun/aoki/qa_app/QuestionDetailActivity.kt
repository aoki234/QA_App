package jp.techacademy.jun.aoki.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*



class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion :Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference

    private lateinit var fav_btn:Button
    private var favorite:String? = null

    private lateinit var mDataBaseReference: DatabaseReference

   // private lateinit var fav_text:TextView
    private var fav_flag:Boolean = true

    /*private val mFavoriteListener = object : ValueEventListener {



        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val map = dataSnapshot.value as Map<String, String>

            val favorirteUid = dataSnapshot.key ?: ""

            Log.d("デバック2","called this ")

            Log.d("デバック2",favorirteUid.toString())

        }
        override fun onCancelled(firebaseError: DatabaseError) {
            print("エラーが発生")
        }
    }*/


    private val mEventListener = object : ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String,String>

            val answerUid = dataSnapshot.key ?:""

            for(answer in mQuestion.answers){
                if(answerUid == answer.anwwerUid){
                    return
                }
            }


            val body = map["body"] ?:""
            val name = map["name"] ?:""
            val uid = map["uid"] ?:""


            val answer = Answer(body,name,uid,answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }


        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        val user = FirebaseAuth.getInstance().currentUser

        fav_btn = findViewById(R.id.favorite_button)
        if (user == null) {
            //ログインしていない場合、非表示
            fav_btn.visibility = View.GONE
        }
        //fav_btn = findViewById(R.id.favorite_button)
        //fav_text = findViewById(R.id.fav_flag)

        fav_btn.setOnClickListener { v ->
            if (fav_flag === false) {
                fav_btn.text = "お気に入りに登録"
                //fav_text.text = "お気に入り済"
                fav_flag = true

            } else {
                fav_btn.text = "お気に入りを解除"
                //fav_text.text = "お気に入り未登録"
                fav_flag = false


                mDataBaseReference = FirebaseDatabase.getInstance().reference

                val user = FirebaseAuth.getInstance().currentUser

                if (user == null) {
                    Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show()

                } else {

                    val favRef = mDataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString())
                    val userRef = mDataBaseReference.child(UsersPATH).child(user.uid)

                    //val fav_id = favRef.toString()
                    //Log.d("デバック",fav_id)
                    favRef.addChildEventListener(object : ChildEventListener {
                        override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                            val favorite_id = dataSnapshot.key

                            if(mQuestion.questionUid == favorite_id.toString()){
                                favorite = favorite_id.toString()
                                Log.d("デバック3",favorite)
                            }

                        }

                        override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {}

                        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

                        override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {}

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })

                    val data = HashMap<String, String>()
                    data["favorite"] = favorite.toString()

                    Log.d("デバック3","before data set")
                    userRef.setValue(data)
                    //Snackbar.make(v, "表示名を変更しました", Snackbar.LENGTH_LONG).show()

                }
            }
        }


                val extras = intent.extras
                mQuestion = extras.get("question") as Question

                title = mQuestion.title

                mAdapter = QuestionDetailListAdapter(this, mQuestion)
                listView.adapter = mAdapter
                mAdapter.notifyDataSetChanged()

                fab.setOnClickListener {
                    val user = FirebaseAuth.getInstance().currentUser

                    if (user == null) {
                        val intent = Intent(applicationContext, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        //todo
                        val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                        intent.putExtra("question", mQuestion)

                        startActivity(intent)
                    }
                }

                val dataBaseReference = FirebaseDatabase.getInstance().reference
                mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString())
                    .child(mQuestion.questionUid).child(
                        AnswersPATH
                    )

                mAnswerRef.addChildEventListener(mEventListener)


            }

        }
