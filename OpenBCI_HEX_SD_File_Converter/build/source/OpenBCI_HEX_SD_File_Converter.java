import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class OpenBCI_HEX_SD_File_Converter extends PApplet {

/**
 * Trying to convert OBCI data from HEX to DECIMAL
 * Started with the Button example, added scratch from other examples.
 * Relies heavily on the OpenBCI Processing GUI by Chip Audette (link)
 *
 * This will successfully prompt to open file on button press
 * also creates file folder and time-stamp new data file
 *
 *
 */


//properties of the openBCI board
float fs_Hz = 250.0f;  //sample rate used by OpenBCI board
final float ADS1299_Vref = 4.5f;  //reference voltage for ADC in ADS1299
final float ADS1299_gain = 24;  //assumed gain setting for ADS1299
final float scale_fac_uVolts_per_count = ADS1299_Vref / (pow(2, 23)-1) / ADS1299_gain  * 1000000.f; //ADS1299 datasheet Table 7, confirmed through experiment
final float scale_fac_accel_G_per_count = 0.002f / ((float)pow(2,4));  //assume set to +/4G, so 2 mG per digit (datasheet). Account for 4 bits unused

PFont font;

BufferedReader dataReader;
String dataLine;
PrintWriter dataWriter;
String convertedLine;
String thisLine;
String h;
float[] floatData = new float[20];
int[] intData = new int[20];
String[] hexNums;
String logFileName;
long thisTime;
long thatTime;
boolean printNextLine = false;

int buttonX, buttonY;      // Position of square button
float buttonHeight = 60;     // height of button
float buttonWidth = 400;
int buttonColor, buttonHighlight, strokeColor;
int baseColor;
boolean overButton = false;
boolean reading = false;

public void setup() {
  
  font = createFont("Dialog", 24);
  textFont(font);
  textAlign(CENTER);
  rectMode(CENTER);
  buttonColor = color(0, 200, 148);
  buttonHighlight = color(19, 203, 48);
  strokeColor =  color(0, 0, 0);
  baseColor = color(77, 36, 21);
  buttonX = width/2;
  buttonY = height/2;
}

public void draw() {
  background(baseColor);

  drawButton();

  while (reading) {
    try {
      dataLine = dataReader.readLine();
    }
    catch (IOException e) {
      e.printStackTrace();
      dataLine = null;
    }

    if (dataLine == null) {
      // Stop reading because of an error or file is empty
      thisTime = millis() - thatTime;
      reading = false;
      println("nothing left in file");
      println("write took "+thisTime+" mS");
      dataWriter.flush();
      dataWriter.close();
    } else {
      //        println(dataLine);
      hexNums = splitTokens(dataLine, ",");

      if (hexNums[0].charAt(0) == '%') {
        //          println(dataLine);
        dataWriter.println(dataLine);
        println(dataLine);
        printNextLine = true;
      } else {
        if (hexNums.length < 13){
          convert8channelLine();
        }else{
          convert16channelLine();
        }
        if(printNextLine){
          printNextLine = false;
        }
      }
    }
  }
}// end of draw



public void convert16channelLine() {
  if(printNextLine){
    for(int i=0; i<hexNums.length; i++){
      h = hexNums[i];
      if (h.length()%2 == 0) {  // make sure this is a real number
        intData[i] = unhex(h);
      } else {
        intData[i] = 0;
      }
      dataWriter.print(intData[i]);
      print(intData[i]);
      if(hexNums.length > 1){
        dataWriter.print(", ");
        print(", ");
      }
    }
    dataWriter.println();
    println();
    return;
  }
  for (int i=0; i<hexNums.length; i++) {
    h = hexNums[i];
    if (i > 0) {
      if (h.charAt(0) > '7') {  // if the number is negative
        h = "FF" + hexNums[i];   // keep it negative
      } else {                  // if the number is positive
        h = "00" + hexNums[i];   // keep it positive
      }
      if (i > 16) { // accelerometer data needs another byte
        if (h.charAt(0) == 'F') {
          h = "FF" + h;
        } else {
          h = "00" + h;
        }
      }
    }
    // println(h); // use for debugging
    if (h.length()%2 == 0) {  // make sure this is a real number
      floatData[i] = unhex(h);
    } else {
      floatData[i] = 0;
    }

    if (i>=1 && i<=16) {
      floatData[i] *= scale_fac_uVolts_per_count;
    }else if(i != 0){
      floatData[i] *= scale_fac_accel_G_per_count;
    }

    if(i == 0){
      dataWriter.print(PApplet.parseInt(floatData[i]));  // print the sample counter
    }else{
      dataWriter.print(floatData[i]);  // print the current channel value
    }
    if (i < hexNums.length-1) {  // print the current channel value
      dataWriter.print(",");  // print "," separator
    }
  }
  dataWriter.println();
}

public void convert8channelLine() {
  if(printNextLine){
    for(int i=0; i<hexNums.length; i++){
      h = hexNums[i];
      if (h.length()%2 == 0) {  // make sure this is a real number
        intData[i] = unhex(h);
      } else {
        intData[i] = 0;
      }
      print(intData[i]);
      dataWriter.print(intData[i]);
      if(hexNums.length > 1){
        dataWriter.print(", ");
        print(", ");
      }
    }
    dataWriter.println();
    println();
    return;
  }
  for (int i=0; i<hexNums.length; i++) {
    h = hexNums[i];
    if (i > 0) {
      if (h.charAt(0) > '7') {  // if the number is negative
        h = "FF" + hexNums[i];   // keep it negative
      } else {                  // if the number is positive
        h = "00" + hexNums[i];   // keep it positive
      }
      if (i > 8) { // accelerometer data needs another byte
        if (h.charAt(0) == 'F') {
          h = "FF" + h;
        } else {
          h = "00" + h;
        }
      }
    }
    // println(h); // use for debugging
    if (h.length()%2 == 0) {  // make sure this is a real number
      floatData[i] = unhex(h);
    } else {
      floatData[i] = 0;
    }

    if (i>=1 && i<=8) {
      floatData[i] *= scale_fac_uVolts_per_count;
    }else if(i != 0){
      floatData[i] *= scale_fac_accel_G_per_count;
    }

    if(i == 0){
      dataWriter.print(PApplet.parseInt(floatData[i]));  // print the sample counter
    }else{
      dataWriter.print(floatData[i]);  // print the current channel value
    }
    if (i < hexNums.length-1) {
      dataWriter.print(",");  // print "," separator
    }
  }
  dataWriter.println();
}




public void folderSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());
    dataReader = createReader(selection.getAbsolutePath()); // ("positions.txt");
    reading = true;
    println("timing file write");
    thatTime = millis();
  }
}

public void createFile(){
   logFileName = "OBCI_data/"+month()+"_"+day()+"_"+hour()+"_"+minute()+".csv";
   dataWriter = createWriter(logFileName);
   dataWriter.println("%OBCI Data Log " + month()+"/"+day()+" "+hour()+":"+minute());
}





public void drawButton(){
  updateButton();
  if (overButton) {
      fill(buttonHighlight);
    } else {
      fill(buttonColor);
    }
    stroke(strokeColor);
    rect(buttonX, buttonY, buttonWidth, buttonHeight);
    fill(0);
    text("select file to convert", width/2,height/2 + 10);
}


public void updateButton() {
  if (buttonOver(buttonX, buttonY, buttonWidth, buttonHeight) ) {
    overButton = true;
  } else {
    overButton = false;
  }
}

public boolean buttonOver(int x, int y, float w, float h)  {
  if (mouseX >= x-w/2 && mouseX <= x+w/2 && 
      mouseY >= y-h/2 && mouseY <= y+h/2) {
    return true;
  } else {
    return false;
  }
}

public void mousePressed() {
  if (overButton) {
    println("mouse pressed");
    createFile();
    selectInput("Select a folder to process:", "folderSelected");
  }
}
  public void settings() {  size(640, 360); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "OpenBCI_HEX_SD_File_Converter" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
