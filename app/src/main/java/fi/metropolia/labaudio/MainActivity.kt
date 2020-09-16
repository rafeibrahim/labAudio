package fi.metropolia.labaudio

import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.*
import java.time.LocalTime

class MainActivity : AppCompatActivity() {
    lateinit var inputStream1: InputStream
    lateinit var inputStream2: InputStream
    lateinit var recFile: File
    var recRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputStream1 = resources.openRawResource(R.raw.ts)
        inputStream2 = resources.openRawResource(R.raw.ts)





/*
        val recFileName = "testkjs.raw"
        val storageDir= getExternalFilesDir(Environment.DIRECTORY_MUSIC)


        try{
            recFile = File(storageDir.toString() + "/"+ recFileName)
        } catch (ex: IOException) {
            // Error occured
        }

        val testInputStream = FileInputStream(recFile)
*/



        btnStart.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO){
                recRunning = true
                async(Dispatchers.Default){ recordAudio()}
            }
        }


        btnStop.setOnClickListener {
/*            GlobalScope.launch(Dispatchers.IO){
                async(Dispatchers.Default){ recordAudio(false)}
            }*/
            recRunning = false
        }

        btnPlay.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val recFileName = "testkjs.raw"
                val storageDir= getExternalFilesDir(Environment.DIRECTORY_MUSIC)


                try{
                    recFile = File(storageDir.toString() + "/"+ recFileName)
                } catch (ex: IOException) {
                    // Error occured
                }

                val testInputStream = FileInputStream(recFile)
                val ft = async(Dispatchers.Default) { playAudio(testInputStream) }
                //val st = async(Dispatchers.Default) { playAudio(inputStream2) }
                //showTimes(ft.await(), st.await())
                showTimes(ft.await())
            }
        }

    }



   fun showTimes(f: String) {
        txt.text = "first: $f"
    }

    suspend fun recordAudio() {
        try{
            val recFileName = "testkjs.raw"
            val storageDir= getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            recFile = File(storageDir.toString() + "/"+ recFileName)
        } catch (ex: IOException) {
            // Error occured
        }
        try {

            val outputStream= FileOutputStream(recFile)
            val bufferedOutputStream= BufferedOutputStream(outputStream)
            val dataOutputStream= DataOutputStream(bufferedOutputStream)
            val minBufferSize= AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT)
            val aFormat= AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()
            val recorder= AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(aFormat)
                .setBufferSizeInBytes(minBufferSize)
                .build()
            val audioData= ByteArray(minBufferSize)
            recorder.startRecording()

            while(recRunning) {
                val numofBytes= recorder.read(audioData, 0, minBufferSize)
                if(numofBytes>0) {
                    dataOutputStream.write(audioData)
                }
            }
            recorder.stop()
            dataOutputStream.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    suspend fun playAudio(istream: InputStream): String {
        val minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT)
        val aBuilder = AudioTrack.Builder()
        val aAttr: AudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        val aFormat: AudioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()
        val track = aBuilder.setAudioAttributes(aAttr)
                .setAudioFormat(aFormat)
                .setBufferSizeInBytes(minBufferSize)
                .build()
        track!!.setVolume(0.2f)
        val startTime = LocalTime.now().toString()
        track!!.play()
        var i = 0
        val buffer = ByteArray(minBufferSize)
        try {
            i = istream.read(buffer, 0, minBufferSize)
            while (i != -1) {
                track!!.write(buffer, 0, i)
                i = istream.read(buffer, 0, minBufferSize)
            }
        } catch (e: IOException) {

        }
        try {
            istream.close()
        } catch (e: IOException) {

        }
        track!!.stop()
        track!!.release()
        return startTime
    }
}