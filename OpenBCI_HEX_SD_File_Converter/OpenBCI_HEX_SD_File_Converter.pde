/**
 * Trying to convert OBCI data from HEX to DECIMAL
 * Started with the Button example, added scratch from other examples.
 * Relies heavily on the OpenBCI Processing GUI by Chip Audette (link)
 * 
 * This will successfully prompt to open file on button press
 * also creates file folder and time-stamp new data file
 * 
 * float scale_fac_uVolts_per_count = ADS1299_Vref / (pow(2,23)-1) / ADS1299_gain  * 1000000.f; //ADS1299 datasheet Table 7, confirmed through experiment
 *
 */
 
 
//properties of the openBCI board
float fs_Hz = 250.0f;  //sample rate used by OpenBCI board
final float ADS1299_Vref = 4.5f;  //reference voltage for ADC in ADS1299
final float ADS1299_gain = 24;  //assumed gain setting for ADS1299
final float scale_fac_uVolts_per_count = ADS1299_Vref / (pow(2,23)-1) / ADS1299_gain  * 1000000.f; //ADS1299 datasheet Table 7, confirmed through experiment
 
PFont font;  

BufferedReader dataReader;
String dataLine;
PrintWriter dataWriter;
String convertedLine;
String thisLine;
String h;
float[] intData = new float[20];
String logFileName;
long thisTime;
long thatTime;

int buttonX, buttonY;      // Position of square button
float buttonHeight = 60;     // height of button
float buttonWidth = 400;
color buttonColor, buttonHighlight, strokeColor; 
color baseColor;
boolean overButton = false;
boolean reading = false;

void setup() {
  size(640, 360);
  font = createFont("Dialog",24);
  textFont(font);
  textAlign(CENTER);
  rectMode(CENTER);
  buttonColor = color(0,200,148);
  buttonHighlight = color(19,203,48);
  strokeColor =  color(0,0,0);
  baseColor = color(77,36,21);
  buttonX = width/2;
  buttonY = height/2;
  
  
}

void draw() {
  background(baseColor);
  
  drawButton();
    
  while (reading){
    try {
      dataLine = dataReader.readLine();
    }catch (IOException e) {
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
    }else{
//        println(dataLine);
      String[] hexNums = splitTokens(dataLine,",");
      
      if(hexNums[0].charAt(0) == '%'){
//          println(dataLine);
        dataWriter.println(dataLine);
        println(dataLine);
      }
      else{
        for(int i=0; i<hexNums.length; i++){
          h = hexNums[i];
          if(i > 0){
            if(h.charAt(0) > '7'){  // if the number is negative 
              h = "FF" + hexNums[i];   // keep it negative
            }else{                  // if the number is positive
              h = "00" + hexNums[i];   // keep it positive
            }
            if(i > 8){ // accelerometer data needs another byte
              if(h.charAt(0) == 'F'){
                h = "FF" + h;
              }else{
                h = "00" + h;
              }
            }
          }
          // println(h); // use for debugging
            if (h.length()%2 == 0){  // make sure this is a real number
              intData[i] = unhex(h);
            }else{
              intData[i] = 0;          
            }
          
          
          //if not first column(sample #) or columns 9-11 (accelerometer), convert to uV
          if(i>=1 && i<=8){
            intData[i] *= scale_fac_uVolts_per_count;
          }
          
//            print the current channel value
          dataWriter.print(intData[i]);
          if(i < hexNums.length-1){
//              print "," separator
            dataWriter.print(",");
          }
        }
//          println();
        dataWriter.println();
      } 
    }   
  }
}// end of draw
  






