package it.adrian.code.trainlines

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import it.adrian.code.testingkotlin.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var stazioneEditText: EditText
    private lateinit var destinazioneEditText: EditText
    private lateinit var searchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stazioneEditText = findViewById(R.id.stazionePartenza)
        destinazioneEditText = findViewById(R.id.destinazioneEditText)
        searchButton = findViewById(R.id.search_button)

        searchButton.setOnClickListener {
            val partenza = stazioneEditText.text.toString().replace(" ", "_")
            val destinazione = destinazioneEditText.text.toString()

            val now = Date()
            val dateFormat = SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat(" HH:mm:ss", Locale.getDefault())
            val time = timeFormat.format(now).replace(" ", "%20")
            val date = dateFormat.format(now).replace(" ", "%20")
            val format = " GMT+0100 (Ora standard dell’Europa centrale)".replace(" ", "%20")
            val range = ("$date$time$format")

            val client = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }).build()

            val request = Request.Builder().url("http://www.viaggiatreno.it/infomobilita/resteasy/viaggiatreno/partenze/${STAZIONI.valueOf(partenza.uppercase(Locale.ROOT)).getCode()}/$range").build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {

                        val jsonArray = JSONArray(response.body?.string())
                        val newTrains = mutableListOf<Train>()

                        for (i in 0 until jsonArray.length()) {
                            val js = jsonArray.getJSONObject(i)
                            val dest = js.getString("destinazione")
                            if (dest.equals(destinazione.uppercase())) {
                                val train = Train(js.getString("destinazione"), js.getString("compNumeroTreno"), js.getInt("ritardo"), js.getString("compOrarioPartenza"), !js.getBoolean("nonPartito"), js.getString("binarioProgrammatoPartenzaDescrizione"))
                                newTrains.add(train)
                            }
                        }
                        runOnUiThread {
                            if (newTrains.isNotEmpty()) {

                                val container = findViewById<LinearLayout>(R.id.container)

                                newTrains.forEach { train ->

                                    val cardView = CardView(this@MainActivity)

                                    cardView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                                    cardView.cardElevation = 4f
                                    cardView.radius = 8f
                                    cardView.useCompatPadding = true

                                    cardView.setCardBackgroundColor(Color.WHITE)

                                    val siglaText = TextView(this@MainActivity)
                                    siglaText.text = buildString {
                                        append("Treno: ")
                                        append(train.siglaTreno)
                                    }
                                    siglaText.textSize = 15f
                                    siglaText.setTextColor(Color.MAGENTA)
                                    siglaText.setPadding(16, 16 * 4 - 7, 16, 16)


                                    val destinazioneText = TextView(this@MainActivity)
                                    destinazioneText.text = buildString {
                                        append("Destinazione: ")
                                        append(train.destinazione)
                                    }
                                    destinazioneText.textSize = 15f
                                    destinazioneText.setTextColor(Color.rgb(67, 160, 163))
                                    destinazioneText.setPadding(16, 16, 16, 16)


                                    val partenzaText = TextView(this@MainActivity)
                                    partenzaText.text = buildString {
                                        append("Partenza: ")
                                        append(train.partenza)
                                    }
                                    partenzaText.textSize = 15f
                                    partenzaText.setTextColor(Color.GREEN)
                                    partenzaText.setPadding(16, 16 * 4 + 32, 16, 16)

                                    val binarioText = TextView(this@MainActivity)
                                    binarioText.text = buildString {
                                        append("Binario: ")
                                        append(train.binario)
                                    }
                                    binarioText.textSize = 15f
                                    binarioText.setTextColor(Color.MAGENTA)
                                    binarioText.setPadding(16, 16 * 4 + (32 * 2), 16, 16)


                                    cardView.addView(siglaText)
                                    cardView.addView(destinazioneText)
                                    cardView.addView(partenzaText)
                                    cardView.addView(binarioText)

                                    container.addView(cardView)

                                    val space: View = Space(this@MainActivity)
                                    container.addView(space, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 12))
                                }
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    data class Train(val destinazione: String, val siglaTreno: String, val minuti_ritardo: Int, val partenza: String, val partito: Boolean, val binario: String)

    //here you can add any station you want ;)
    enum class STAZIONI(private val code: String) {
        BERGAMO("S01529"),
        MILANO_CENTRALE("S01700");

        fun getCode(): String {
            return code
        }
    }
}