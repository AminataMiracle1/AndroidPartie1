package com.example.objetconnect

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
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
import com.google.gson.Gson
import java.util.concurrent.TimeUnit


val channelId = "my_channel_id"

class MainActivity : AppCompatActivity() {
    val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // afficher le layout

        // Initialisation des notification
        creerCanal()
        demanderPermissionNotification()

        getDatServerGet()

        // Appeler le thread
        //exceuterAsynchrone()
        // TODO : Créer une requette pour excutter la tache une seule fois
        //Créer une requeste pour executer une seule fois
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

/*        // Gestion d'un bouton
        val btnPost = findViewById<Button>(R.id.btnEnvPost)
        btnPost.setOnClickListener {
            // Créer un nouveau thread pour exceuter la requeste POSt
            val thread = Thread{
                sendPost("http://10.4.129.42:8080" , "{\"commande\":1}")
            }
            // Demarer le thread
            thread.start()
        }*/
        getDatServerPost()



    }
    //TODO Résumé des étapes principales pour utiliser les notifications en Kotlin :
    //Crée un canal de notification pour les appareils Android 8.0+.
    //Utilise NotificationCompat.Builder pour construire une notification avec un titre, un texte, une icône et des actions.
    //Envoie la notification via le NotificationManager.
    //Pour gérer les actions de la notification, crée des PendingIntent pour les boutons et les interactions utilisateur.
    //Pour envoyer des notifications push, utilise Firebase Cloud Messaging et un service pour gérer la réception des messages.

    /*
    Créer une fonction qui créer la canal de notification
     */
    private fun creerCanal(){
        val channelName = "My Channel"
        val channelDescription = "canal de notification "
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        // Créer le canal de notification pour Android 8.0 et plus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            // Enregistrer le canal de notification
            val notificationManager: NotificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
    /**
     * Méthode pour demander la permission d'envoyer des notifiacation
     * nécessaire a partir de l'API 33
     */
    private fun demanderPermissionNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ){
            // Lanceur pour demander la perission pour les notifications
            val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
                if (!result){
                    Toast.makeText(this, "la permission n'a pas été accordée", Toast.LENGTH_SHORT).show()
                }
                }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PermissionChecker.PERMISSION_GRANTED){
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }

        }

    }

    private fun getDatServerPost(){
        // Gestion d'un bouton
        val btnPost = findViewById<Button>(R.id.btnEnvPost)
        btnPost.setOnClickListener {
            // Recupere le contenus des champs
            val ip = findViewById<EditText>(R.id.editTextIP).text.toString()
            val port = findViewById<EditText>(R.id.editTextPort).text
            // Le lien de connection
            val stUrl = "http://${ip}:${port}"
            // Recuperer les variables des champs et les mettre dans le fichier json
            val marche = findViewById<EditText>(R.id.editMarche).text.toString()
            val freq = findViewById<EditText>(R.id.editFreq).text.toString().toInt() // convertir en int
            val pour = findViewById<EditText>(R.id.editPour).text.toString().toInt() // converir en Int
            //Créer un fichier Json a envoyer : Note on doit creer un data class pour faire un json
            val myData = MyData(freq, pour, marche)
            // Utiliser Gson pour convertir cet objet en chaîne JSON
            val jsonMsg = Gson().toJson(myData)
            Log.d("FichierJson", jsonMsg)
            // Créer un nouveau thread pour exceuter la requeste POSt
            val thread = Thread{
                val reponseServer = sendPost("${stUrl}" , "${jsonMsg}")
                // un handler pour m'anipuler le layout.
                handler.post{
                    Toast.makeText(applicationContext,"Réponse du serveur: $reponseServer", Toast.LENGTH_LONG).show()
                }
            }
            // Demarer le thread
            thread.start()
        }
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
                            val capteur3 =  objet.getInt("capteur3")
                            val capteur5 = objet.getInt("capteur5")
                            Log.d("LesCapteur" , "${capteur3}, ${capteur5}")
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
    private fun sendPost(stUrl: String, jsonMsg: String): String? {
        try {
            // Établir la connexion à l'URL et envoyer la commande JSON dans une requête POST
            val url = URL(stUrl)
            val connexion = url.openConnection() as HttpURLConnection
            connexion.requestMethod = "POST"
            connexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connexion.setRequestProperty("Accept", "application/json")
            connexion.doOutput = true

            // Envoyer la requête JSON dans le corps de la requête
            DataOutputStream(connexion.outputStream).use { os ->
                os.writeBytes(jsonMsg)
                os.flush()
            }
            // Obtenir le code de réponse du serveur
            val responseCode = connexion.responseCode
            val responseMessage = connexion.responseMessage

            // Log des informations de réponse
            Log.d("Status", responseCode.toString())
            Log.d("MSG", responseMessage)

            // Lire le flux de réponse du serveur
            val inputStream = if (responseCode in 200..299) {
                // Si le code de réponse est dans la plage 2xx (succès), utiliser l'InputStream normal
                connexion.inputStream
            } else {
                // Sinon, utiliser l'InputStream d'erreur
                connexion.errorStream
            }
            // Lire la réponse et la convertir en chaîne de caractères
            val responseBody = inputStream.bufferedReader().use { it.readText() }
            // Afficher le corps de la réponse dans les logs
            Log.d("Response Body", responseBody)
            // Déconnecter la connexion
            connexion.disconnect()

            // Retourner la réponse du serveur
            return responseBody

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Erreur", e.message ?: "Erreur inconnue")
            return null
        }
    }
}
// Crééer une classe qui hérite de Worker
class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams){
    // Rédefinir la methode doWork pour faire ce qu'on veut en arrière-plan
    override fun doWork(): Result{
        //TODO : Code à exceuter en arrière plan workerManager
        Log.i("MonWorker", "Afficher un log en arrière plan ")
        // Variable pour la notification
        val titre = "La notification d'Aminata"
        val texte = "La notification se trouve dans la classe MyWorker"
        val notID = 1
        afficherNotification(notID,titre,texte)

        // indiquer si le travail c'est exceuter avec succes
        return Result.success()
    }

    private fun afficherNotification(id: Int, titre : String, texte: String){
        // Préparer la notification, choisir ce qui y sera affiché et son niveau de priorité.
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.star_on) // Icône de la notification
            .setContentTitle(titre) // Titre de la notification
            .setContentText(texte) // Texte de la notification
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Priorité
           // .setAutoCancel(true) // La notification disparaît lorsqu'on clique dessus
            .setDefaults(NotificationCompat.DEFAULT_SOUND) // Son par défaut pour la notification

        // Afficher la notification
        with(NotificationManagerCompat.from(applicationContext)){
            // vérification de la permission à ce moment la
            if(ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
                notify(id, builder.build())
            }
        }
    }
}
// Définir une classe représentant les données que tu veux envoyer
data class MyData(val mli: Int, val pourcentage: Int, val frequence: String)

