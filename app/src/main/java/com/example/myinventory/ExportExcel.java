package com.example.myinventory;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.myinventory.ui.notifications.Transaction;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExportExcel {

    // Global Variables
    private static Cell cell;

    public void simpleAdd(Context context, List<Transaction> dataList, String fileName){
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users"); //Creating a sheet

        Row headerRow = sheet.createRow(0);

        cell = headerRow.createCell(0);
        cell.setCellValue("Item");
//        cell.setCellStyle(headerCellStyle);

        cell = headerRow.createCell(1);
        cell.setCellValue("Description");
//        cell.setCellStyle(headerCellStyle);

        cell = headerRow.createCell(2);
        cell.setCellValue("Bought for");
//        cell.setCellStyle(headerCellStyle);

        cell = headerRow.createCell(3);
        cell.setCellValue("Sold for");

        cell = headerRow.createCell(4);
        cell.setCellValue("Quantity");

        cell = headerRow.createCell(5);
        cell.setCellValue("Total price");

        cell = headerRow.createCell(6);
        cell.setCellValue("Sold by");

        cell = headerRow.createCell(7);
        cell.setCellValue("Paid");

        cell = headerRow.createCell(8);
        cell.setCellValue("Unpaid");

        cell = headerRow.createCell(9);
        cell.setCellValue("Date");

        for(int  i=0; i<dataList.size(); i++){

            Row rowData = sheet.createRow(i+1);
//            row.createCell(CELL_INDEX_0).setCellValue(VALUE_YOU_WANT_TO_KEEP_ON_1ST_COLUMN);
//            row.createCell(CELL_INDEX_1).setCellValue(VALUE_YOU_WANT_TO_KEEP_ON_2ND_COLUMN);
            cell = rowData.createCell(0);
            cell.setCellValue(dataList.get(i).getItem());

            cell = rowData.createCell(1);
            cell.setCellValue(dataList.get(i).getDescription());

            cell = rowData.createCell(2);
            cell.setCellValue(dataList.get(i).getBought());

            cell = rowData.createCell(3);
            cell.setCellValue(dataList.get(i).getSold());

            cell = rowData.createCell(4);
            cell.setCellValue(dataList.get(i).getQuantity());

            cell = rowData.createCell(5);
            cell.setCellValue(dataList.get(i).getTotal());

            cell = rowData.createCell(6);
            cell.setCellValue(dataList.get(i).getSoldBy());

            cell = rowData.createCell(7);
            cell.setCellValue(dataList.get(i).getPaid());

            cell = rowData.createCell(8);
            cell.setCellValue(dataList.get(i).getUnpaid());

            cell = rowData.createCell(9);
            cell.setCellValue(dataList.get(i).getDate().toDate().toString());
        }

//        String fileName = "FileName.xlsx"; //Name of the file

        String extStorageDirectory = Environment.getExternalStorageDirectory()
                .toString();
        File folder = new File(extStorageDirectory, "TheInventory");// Name of the folder you want to keep your file in the local storage.
        folder.mkdir(); //creating the folder
        File file = new File(folder, fileName+".xls");
        if(file.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            // Set the message show for the Alert time
            builder.setMessage("Are you sure u want to override it?");

            // Set Alert Title
            builder.setTitle("File already exist!");

            // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
            builder.setCancelable(false);

            // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
            builder.setPositiveButton("Yes", (dialog, which) -> {
                // When the user click yes button then app will close
                try {
                    file.createNewFile(); // creating the file inside the folder
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    FileOutputStream fileOut = new FileOutputStream(file); //Opening the file
                    workbook.write(fileOut); //Writing all your row column inside the file
                    fileOut.close(); //closing the file and done
                    Toast.makeText(context, fileName+".xls saved successfully", Toast.LENGTH_SHORT).show();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
            builder.setNegativeButton("No", (dialog, which) -> {
                // If user click no then dialog box is canceled.
                dialog.cancel();
            });

            // Create the Alert dialog
            AlertDialog alertDialog = builder.create();
            // Show the Alert Dialog box
            alertDialog.show();
        }
        else{
            try {
                file.createNewFile(); // creating the file inside the folder
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            try {
                FileOutputStream fileOut = new FileOutputStream(file); //Opening the file
                workbook.write(fileOut); //Writing all your row column inside the file
                fileOut.close(); //closing the file and done
                Toast.makeText(context, fileName+".xls saved successfully", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
