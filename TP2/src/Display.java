import java.io.*;

class Display {
  FileInputStream fis;
  int frame;

  public Display(String filename) throws Exception{
    fis = new FileInputStream(filename);
    frame = 0;
  }

  public int getnextframe(byte[] frame) throws Exception{
    int length = 0;
    String length_string;
    byte[] frame_length = new byte[5];

    fis.read(frame_length,0,5);

    length_string = new String(frame_length);
    length = Integer.parseInt(length_string);

    return(fis.read(frame,0,length));
  }  
}
