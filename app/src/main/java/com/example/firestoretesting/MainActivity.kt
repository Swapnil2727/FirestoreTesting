package com.example.firestoretesting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {

    private val personCollectionRef = Firebase.firestore.collection("persons")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val title = titleEditText.text.toString()
            val age = ageEditText.text.toString().toInt()

            val person = Person(name, title, age)
            savePerson(person)
        }

        //Live updates
    //    subscribeToRealtimeUpdates()


        retriveButton.setOnClickListener {
            retrivePerson()
        }
    }

    //Realtime Updates of Person Data
    private fun subscribeToRealtimeUpdates(){
        personCollectionRef.addSnapshotListener{querySnapshot, firebaseFirestoreExceptipon ->
            firebaseFirestoreExceptipon?.let {
                Toast.makeText(this, it.message,Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            querySnapshot?.let {

                val sb = StringBuilder()
                for (document in it){

                    val person = document.toObject<Person>()
                    sb.append("$person")
                }
                 retrivedPersonTextView.text = sb.toString()

            }
        }

    }
    private fun retrivePerson() = CoroutineScope(Dispatchers.IO).launch {

        val minAge = minAgeEditText.text.toString().toInt()
        val maxAge = maxAgeEditText.text.toString().toInt()
        try {

            //Query Data Retrieval
            val querySnapshot = personCollectionRef
                .whereGreaterThanOrEqualTo("age",minAge)
                .whereLessThan("age",maxAge)
                .orderBy("age")
                .get().await()
            val sb = StringBuilder()

            for(document in querySnapshot.documents){
                val person = document.toObject<Person>()
                sb.append("$person")

            }
            withContext(Dispatchers.Main){
                retrivedPersonTextView.text = sb.toString()
            }

        }catch (e:Exception)
        {
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {

        try {
            personCollectionRef.add(person).await()
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,"Saved to fireStore",Toast.LENGTH_LONG).show()
            }

        }catch (e:Exception)
        {
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_LONG).show()
            }
        }
    }
}