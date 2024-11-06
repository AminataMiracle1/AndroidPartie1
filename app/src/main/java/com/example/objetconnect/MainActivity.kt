package com.example.objetconnect

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // afficher le layout

        // Appeler le thread
        exceuterAsynchrone()
        // TODO : Créer une requette pour excutter la tache une seule fois

        // TODO:
        // Créer une requeste pour executer une seule fois
        val myWorkRequest = OneTimeWorkRequest.Builder(MyWorker::class.java).build()

        // planifier la tache avec WorkManager
        WorkManager.getInstance(this).enqueue(myWorkRequest)

        // Préparer la requete au worker
        val request = PeriodicWorkRequest.Builder(
            MyWorker::class.java,
            15, TimeUnit.MINUTES // minimun 15 minutes
        ).build()

        // Lancer la commande au worker pour qu'il l'éxecute
        WorkManager.getInstance(this).enqueue(request)
        // TODO: Get les données de l'objet


    }

    private fun exceuterAsynchrone(){
        val thread = Thread{
            // TODO : code a exuter  en asynchrone
            Log.i("monthread" , "Affiche d'un log asynchrone")
            // Appller la fonction getData

            val jsonData = getData("https://api.jsonbin.io/v3/qs/672bc3c1ad19ca34f8c5747b")
            Log.d("JsonData", jsonData ?: "Donnée nulles")
            // Poster l'opération de mise à jour sur le thread principale.
            handler.post{
                // Convertir la réponse en objet JSON
                val obj = JSONObject(jsonData)
                try {
                    // Afficher la chaine dans un log pour connaitre la structure
                    Log.d("JsonData", jsonData ?: "Donnée nulles")
                    // Extraire les données de la chaines JSON et les afficher
                    if (jsonData != null){
                        // méthode pour avoir un fichier json.
                        val record = obj.getJSONObject("record") // accéder à l'objet recorde
                        val ip = record.getString( "IP")
                        val port = record.getInt("Post")
                        Log.d("Donnes", "nulle ${port}")
                     //   val marche = obj.getBoolean("marche")
                        val freq = record.getInt("freq")
                        Log.d("Donnes", "nulle ${freq}")
                        val pourc = record.getInt("pourc")
                        Log.d("Donnes", "nulle ${pourc}")
                        // Maintenant mettre à les textEdit
                        val champIP : EditText = findViewById(R.id.editTextIP)
                        val champPort : EditText = findViewById(R.id.editTextPort)
                        val champMarch : EditText = findViewById(R.id.editMarche)
                        val champFre : EditText = findViewById(R.id.editFreq)
                        val champPourc : EditText = findViewById(R.id.editPour)
                        champIP.setText(ip)
                        champPort.setText(port.toString())
                      //  champMarch.setText(marche) marche pas à cause du booleen
                        champFre.setText(freq.toString())
                        champPourc.setText(pourc.toString())
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
        thread.start()
    }

    // Partie Htt
    private fun getData(stUrl: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(stUrl)
            .build()
        return try {
            client.newCall(request).execute().use { response : Response ->
                if(!response.isSuccessful){
                    Log.e("ERREUR", "Erreur de connexion : ${response.code}")
                    null
                }else{
                    response.body?.string() // Renvoie le corps de la reonse en tant que chaine
                }

            }
        }catch (e:Exception) {
            e.printStackTrace()
            Log.e("ERREUR", e.toString())
            null
        }
    }
}

// Crééer une classe qui hérite de Worker
class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams){
    // Rédefinir la methode doWork pour faire ce qu'on veut en arrière-plan
    override fun doWork(): Result{
        //TODO : Code à exceuter en arrière plan workerManager
        Log.i("MonWorker", "Afficher un log en arrière plan ")

        // indiquer si le travail c'est exceuter avec succes
        return Result.success()
    }
}