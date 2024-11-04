package com.example.objetconnect

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
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
        // Appller la fonction getData
         val reponse = getData("http://localhost")
        if (reponse != null){
            Log.d("Mon_get", reponse)
        }else{
            Log.d("Mon_get", "null")
        }

    }

    private fun exceuterAsynchrone(){
        val thread = Thread{
            // TODO : code a exuter  en asynchrone
            Log.i("monthread" , "Affiche d'un log asynchrone")

            // le handler permet à notre thread asynchrone de communication avec
            // le thread principale pour lui permettre de mettre à jour l'affichage
            handler.post{
                // TODO: Mettre a jour l'interface utilisteur
                val txtSortie = findViewById<TextView>(R.id.LbTextBIenvenue)
                txtSortie.text = "Nouvelle donnée obtenues"
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