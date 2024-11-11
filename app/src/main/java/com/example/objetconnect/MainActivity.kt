package com.example.objetconnect

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
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
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // afficher le layout

        getDatServerGet()

        // Appeler le thread
        //exceuterAsynchrone()
        // TODO : Créer une requette pour excutter la tache une seule fois

        // TODO:
        /*
        // Créer une requeste pour executer une seule fois
        val myWorkRequest = OneTimeWorkRequest.Builder(MyWorker::class.java).build()

         //planifier la tache avec WorkManager
       WorkManager.getInstance(this).enqueue(myWorkRequest)

        // Préparer la requete au worker
        val request = PeriodicWorkRequest.Builder(
            MyWorker::class.java,
            15, TimeUnit.MINUTES // minimun 15 minutes
        ).build()

        // Lancer la commande au worker pour qu'il l'éxecute
        WorkManager.getInstance(this).enqueue(request)
        // TODO: Get les données de l'objet
         */


      /*  // Gestion d'un bouton
        val btnPost = findViewById<Button>(R.id.btnEnvPost)
        btnPost.setOnClickListener {
            // Créer un nouveau thread pour exceuter la requeste POSt
            val thread = Thread{
                sendPost("http://10.4.129.42:8080" , "{\"commande\":1}")
            }
            // Demarer le thread
            thread.start()
        }
        // methode qui nous permet de recevoir le fichier Json de mon raspberry by
        getDatServerGet()
        */



    }
    // Cette méthode est pour recevoir un fichier JSon
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
    /*
    Méthode qui va demande le Ip et port de getDatServerGet
     */
    private fun getDatServerGet(){

            val btnGet = findViewById<Button>(R.id.btnEnvoyerGet)
            try {
                // Appeler la fonction getData qui nous retourne la reponse du server
                btnGet.setOnClickListener {
                    // Recupere le contenus des champs
                    val ip = findViewById<EditText>(R.id.editTextIP).text.toString()
                    val port = findViewById<EditText>(R.id.editTextPort).text

                    // Le lien de connection
                    val stUrl = "http://${ip}:${port}"
                    Log.d("SetUrl", stUrl)
                    val thread = Thread{
                        val reponseServer = getData(stUrl)
                        // afficher la valeurs des capteurs
                        Log.d("ServeurGet", reponseServer.toString())

                        handler.post {
                            // Convertir le fichier recu en json
                            val objet = JSONObject(reponseServer)

                            val capteur3 =  objet.getJSONObject("capteur3")
                            val capteur5 = objet.getJSONObject("capteur5")
                            // Recupere les widget pour afficher :
                            findViewById<EditText>(R.id.editMarche).setText(capteur3.toString())
                            findViewById<EditText>(R.id.editFreq).setText(capteur5.toString())
                        }


                    }
                    thread.start()


                    //TODO : recupere le fichier Json et Afficher sur les edit text
                }
            }catch (e:Exception) {
                e.printStackTrace()
                Log.e("ERREUR", e.toString())
            }


    }

    /*
    Méthode pour obtemir des données d'un server ou d'un objet connecté avec Get
    */
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
                    response.body?.string() // Renvoie le corps de la reponse en tant que chaine
                }

            }
        }catch (e:Exception) {
            e.printStackTrace()
            Log.e("ERREUR", e.toString())
            null
        }
    }

    /**
     * Méthode pour envoyer une commande json a un objet connect. à l'aide d'une requete POST
     * @param stUrl Address Url du serveur ou de notre objet connecté
     * @param jsonMsg Le message Json qui sera envoyé à l'objet connecté Password1
     */

    private fun sendPost(stUrl: String, jsonMsg: String){
        try{
            // Etablir la connexion a l'url et envoyer notre commande json dans une requete post
            val url = URL(stUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8" )
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.doOutput = true
            DataOutputStream(conn.outputStream).use { os ->
                os.writeBytes(jsonMsg)
                os.flush()
            }
            // On peut afficher la réponse du server dans un log pour s'assurer du fonctionnement
            Log.d("Status", conn.responseCode.toString())
            Log.d("MSG", conn.responseMessage)
            conn.disconnect()
        }catch (e: Exception){
            e.printStackTrace()
            Log.e("Erreur", e.message ?: "Erreur inconnue")
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