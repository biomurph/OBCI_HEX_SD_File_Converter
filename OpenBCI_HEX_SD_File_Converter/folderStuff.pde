



void folderSelected(File selection) {
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

void createFile(){
   logFileName = "OBCI_data/"+month()+"_"+day()+"_"+hour()+"_"+minute()+".csv";
   dataWriter = createWriter(logFileName);
   dataWriter.println("*OBCI Data Log " + month()+"/"+day()+" "+hour()+":"+minute());
}


