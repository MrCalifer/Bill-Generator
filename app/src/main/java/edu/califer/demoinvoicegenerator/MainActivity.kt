package edu.califer.demoinvoicegenerator

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import edu.califer.demoinvoicegenerator.models.Item
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var dateObj: Date
    private lateinit var dateFormat: DateFormat
    private var items: ArrayList<Item> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //When user click to generate_invoice button
        generate_invoice.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), PERMISSION_REQUEST_CODE
                )
            } else {

                //Checking if the edit text fields are not empty
                if (item_name.text.toString().isNotEmpty()
                    && item_quantity.text.toString().isNotEmpty()
                    && !item_price.text.isNullOrEmpty()
                ) {
                    items.clear()
                    //Adding item to the item list.
                    items.add(
                        Item(
                            item_name.text.toString(),
                            item_quantity.text.toString().toInt(),
                            item_price.text.toString().toDouble()
                        )
                    )
                    Log.d(TAG, "Items size : ${items.size}")
                    generateInvoice()
                } else {
                    Toast.makeText(applicationContext, "Fields cannot be empty", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun generateInvoice() {

        dateObj = Date()


        val myPdfDocument = PdfDocument()
        val myPaint = Paint()

        val companyTitle = Paint()

        val myPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val myPage1 = myPdfDocument.startPage(myPageInfo)
        val canvas = myPage1.canvas

        //Setting Company Title
        companyTitle.textAlign = Paint.Align.CENTER
        companyTitle.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        companyTitle.textSize = 70f
        canvas.drawText("Company Title", (pageWidth / 2).toFloat(), 270F, companyTitle)

        //Setting Company Contact details
        myPaint.color = Color.BLACK
        myPaint.textSize = 30f
        myPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Call : +91- 1234567890", 1160f, 40f, myPaint)

        //Setting Invoice Text in Center
        companyTitle.textAlign = Paint.Align.CENTER
        companyTitle.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        companyTitle.textSize = 70f
        canvas.drawText("Invoice", (pageWidth / 2).toFloat(), 500f, companyTitle)

        //Setting Customer Name
        myPaint.textAlign = Paint.Align.LEFT
        myPaint.textSize = 35f
        myPaint.color = Color.DKGRAY
        canvas.drawText("Customer Name  : Customer", 20f, 590f, myPaint)
        canvas.drawText("Contact Number : 0123456789", 20f, 640f, myPaint)

        //Setting Invoice details
        myPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Invoice No. : 12358394", (pageWidth - 20).toFloat(), 590f, myPaint)

        dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        canvas.drawText(
            "Date : ${dateFormat.format(dateObj)}",
            (pageWidth - 20).toFloat(), 640F, myPaint
        )

        dateFormat = SimpleDateFormat("hh.mm aa", Locale.getDefault())
        canvas.drawText(
            "Time : ${dateFormat.format(dateObj)}",
            (pageWidth - 20).toFloat(), 690F, myPaint
        )

        //Setting up the Table
        myPaint.style = Paint.Style.STROKE
        myPaint.strokeWidth = 2f
        canvas.drawRect(20F, 780F, (pageWidth - 20).toFloat(), 860F, myPaint)

        myPaint.textAlign = Paint.Align.LEFT
        myPaint.style = Paint.Style.FILL

        canvas.drawText("Sl. No.", 40F, 830F, myPaint)
        canvas.drawText("Item Description", 200F, 830F, myPaint)
        canvas.drawText("Price", 700F, 830F, myPaint)
        canvas.drawText("Quantity", 880F, 830F, myPaint)
        canvas.drawText("Total ", 1050F, 830F, myPaint)

        canvas.drawLine(180F, 790F, 180F, 840F, myPaint)
        canvas.drawLine(680F, 790F, 680F, 840F, myPaint)
        canvas.drawLine(850F, 790F, 850F, 840F, myPaint)
        canvas.drawLine(1030F, 790F, 1030F, 840F, myPaint)


        //For Items
        canvas.drawText("1.", 40F, 950F, myPaint)
        canvas.drawText(item_name.text.toString(), 200F, 950F, myPaint)
        canvas.drawText(item_price.text.toString().toFloat().toString(), 700F, 950F, myPaint)
        canvas.drawText(item_quantity.text.toString(), 950F, 950F, myPaint)

        val total = item_price.text.toString().toFloat()
        myPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(total.toString(), (pageWidth - 40).toFloat(), 950F, myPaint)


        //For Final Amount
        canvas.drawLine(680F, 1200F, (pageWidth - 20).toFloat(), 1200F, myPaint)
        myPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Sub Total ", 700F, 1250F, myPaint)
        canvas.drawText(":", 900F, 1250F, myPaint)
        myPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$total", (pageWidth - 40).toFloat(), 1250F, myPaint)

        myPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Tax (12%) ", 700F, 1300F, myPaint)
        canvas.drawText(":", 900F, 1300F, myPaint)
        myPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${total * 0.12}", (pageWidth - 40).toFloat(), 1300F, myPaint)

        myPaint.textAlign = Paint.Align.LEFT
        myPaint.color = Color.rgb(247, 147, 30)
        canvas.drawRect(680F, 1350F, (pageWidth - 20).toFloat(), 1450F, myPaint)

        myPaint.color = Color.BLACK
        myPaint.textSize = 50f
        myPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Total", 700F, 1415F, myPaint)
        myPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${(total * 0.12)+total}", (pageWidth - 40).toFloat(), 1415F, myPaint)

        myPdfDocument.finishPage(myPage1)

        val root = this.getExternalFilesDir("/Invoices")

        /** Creating my own Directory in Storage.*/
        val myDirectory = File("$root")
        myDirectory.mkdirs()

        /**Initializing a new file.*/
        val file = File(myDirectory, "Invoice.pdf")

        Log.d(TAG, "File Location : ${file.absolutePath}")

        try {
            myPdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(
                applicationContext, "Pdf Created.", Toast
                    .LENGTH_SHORT
            ).show()

            openPDF(file)

        } catch (exception: IOException) {
            exception.printStackTrace()
        }

        myPdfDocument.close()

    }

    private fun openPDF(file : File){

        val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file);

        val target = Intent(Intent.ACTION_VIEW)
        target.setDataAndType(uri, "application/pdf")
        target.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY

        val intent = Intent.createChooser(target, "Open File")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Instruct the user to install a PDF reader here, or something
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val pageWidth = 1200
        private const val pageHeight = 2010
        private const val PERMISSION_REQUEST_CODE = 101
    }
}