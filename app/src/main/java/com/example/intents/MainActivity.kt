package com.example.intents

import android.Manifest.permission.CALL_PHONE
import android.content.Intent
import android.content.Intent.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.example.intents.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val amb: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    companion object Constantes { //singleton de constantes
        const val PARAMETRO_EXTRA: String = "PARAMETRO_EXTRA"
    }

    private lateinit var parl : ActivityResultLauncher<Intent> //parametro activity result launcher
    private lateinit var permissaoChamadaArl: ActivityResultLauncher<String> //as permissoes sao do tipo string
    private lateinit var pegarImagemArl: ActivityResultLauncher<Intent> //pegar imagem de uma intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(amb.root)

        parl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result?.resultCode == RESULT_OK)
                amb.parametroTv.text = result.data?.getStringExtra(PARAMETRO_EXTRA)
        }

        amb.entrarParametroBt.setOnClickListener {
            parl.launch(Intent("WINDOW_PARAMETRO").putExtra(PARAMETRO_EXTRA, amb.parametroTv.text.toString()))
        }

        permissaoChamadaArl = registerForActivityResult(ActivityResultContracts.RequestPermission()){permissaoConcedida ->
            if (permissaoConcedida)
                chamarNumero(true)
            else {
                Toast.makeText(this, "Permissão necessária para continuar", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        pegarImagemArl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val imagemUri = it.data?.data
                imagemUri?.let {
                    startActivity(Intent(Intent.ACTION_VIEW, it))
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.viewMi -> { //nao funciona com www, precisa ter o http
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(amb.parametroTv.text.toString())))
                true
            }
            R.id.callMi -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    //checar a permissao
                    if (checkSelfPermission(CALL_PHONE) == PERMISSION_GRANTED)
                        chamarNumero(true)
                    else
                        permissaoChamadaArl.launch(CALL_PHONE)
                }else
                    chamarNumero(true)
                true
            } //call faz a chamada sem interacao com o usuario
            R.id.dialMi -> {
                chamarNumero(false)
                true
            }  //dial abre o discador e o usuario que permite a chamada
            R.id.pickMi ->  {
                startActivity(Intent(Intent.ACTION_PICK).setDataAndType(
                    Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path), "image/*")
                )
                pegarImagemArl.launch(Intent(Intent.ACTION_PICK).setDataAndType(
                    Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path), "image/*"))
                true
            }
            R.id.chooserMi -> { // seleciona o app se tiver mais de um que se encaixe no desejado
                startActivity(Intent(ACTION_CHOOSER).putExtra(EXTRA_TITLE, "Escolha seu navegadro preferido").putExtra(
                    EXTRA_INTENT, Intent(ACTION_VIEW, Uri.parse(amb.parametroTv.text.toString()))))
                true
            }
            else -> false
        }
    }

    private fun chamarNumero (chamar: Boolean) {
        startActivity(Intent(if (chamar) Intent.ACTION_CALL else Intent.ACTION_DIAL, Uri.parse("tel: ${amb.parametroTv.text}")))
    }


}




















