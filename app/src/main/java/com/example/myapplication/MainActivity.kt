package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import java.io.File
import java.lang.Exception
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class MainActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var filePath : String
    private lateinit var excelFile : File
    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val path : String? = result.data?.dataString
                if(path != null)
                    filePath = path
                else
                    Log.d("Error","Go to issue")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),1001)
        }
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        val textView = findViewById<TextView>(R.id.textView)
        codeScanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not
        textView.text = getString(R.string.no_file_placeholder)
        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            if(this::filePath.isInitialized && filePath != "null"){
                runOnUiThread {
                    Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
                }

            }
            else{
                runOnUiThread {
                    Toast.makeText(this, "Select File First!!",Toast.LENGTH_LONG).show()
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }
    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
    fun openDirChooseFile(mimeTypes: Array<String>) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        if (mimeTypes != null) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        // intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);//多选
        resultLauncher.launch(intent)
    }

    fun chooseFile() {
        val mimeTypes = arrayOf<String>(
//            MimeType.DOC,
//            MimeType.DOCX,
//            MimeType.PDF,
//            MimeType.PPT,
//            MimeType.PPTX,
            MimeType.XLS,
            MimeType.XLSX
        )
        openDirChooseFile(mimeTypes)
    }
    fun openExcel(view:View) {
//        val intent = Intent()
//            .setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//            .setAction(Intent.ACTION_GET_CONTENT)
//        resultLauncher.launch(intent)
        chooseFile()
        try {
            excelFile = File(filePath)
        }
        catch(e : Exception){
            Log.d("Open File Error, try another one",e.toString())
            filePath = "null"
        }
    }

}