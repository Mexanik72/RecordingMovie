package rec;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.*;

import javax.swing.JFileChooser;
import javax.media.*;
import javax.media.Controls;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.media.format.AudioFormat;
import javax.media.format.H263Format;
import javax.media.format.JPEGFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
  
import javax.media.format.YUVFormat;
import javax.swing.JOptionPane;
  
  
 public class mainFrame extends javax.swing.JFrame {
      
      /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private camDataSource dataSource;
      
      private DataSource camSource;
      private DataSource recordCamSource;
      private DataSink dataSink;
      private Processor processor;
      private Processor recordProcessor;
      private camStateHelper playhelper;
      File file = null;
      private JFileChooser movieChooser;
      
      public mainFrame(camDataSource dataSource) {
          this.dataSource = dataSource;
          this.dataSource.setParent(this);
          camSource = dataSource.cloneCamSource();
          
          initComponents();
         try{
              processor = Manager.createProcessor(camSource);
          }catch (IOException e) {
              JOptionPane.showMessageDialog(this, "Exception creating processor: " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
              return;
         }catch (NoProcessorException e) {
             JOptionPane.showMessageDialog(this, "Exception creating processor: " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
              return;
          }
         
          playhelper = new camStateHelper(processor);
          if(!playhelper.configure(10000)){
              JOptionPane.showMessageDialog(this, "cannot configure processor", "Error", JOptionPane.WARNING_MESSAGE);
              return;
          }
          
          processor.setContentDescriptor(null);
          if(!playhelper.realize(10000)){
              JOptionPane.showMessageDialog(this, "cannot realize processor", "Error", JOptionPane.WARNING_MESSAGE);
              return;
          }
          
          checkIncoding(processor.getTrackControls());
          setJPEGQuality(processor, 1.0f);
         // setAudQuality(processor, 1.0f);
         //Control control = processor.getControl("javax.media.control.FrameRateControl");
         // if ( control != null && control instanceof javax.media.control.FrameRateControl ) ((javax.media.control.FrameRateControl)control).setFrameRate(5f);
          processor.start();
          try{
              Thread.sleep(10000);
          }catch(java.lang.InterruptedException e){}
          
           Control control = processor.getControl("javax.media.control.FrameRateControl");
          if ( control != null && control instanceof javax.media.control.FrameRateControl ){
              ((javax.media.control.FrameRateControl)control).setFrameRate(15.0f);
          } else{
              System.out.println("no frame control");
          }
          
          processor.getVisualComponent().setBackground(Color.gray);
          centerPanel.add(processor.getVisualComponent(), BorderLayout.CENTER);
          centerPanel.add(processor.getControlPanelComponent(), BorderLayout.SOUTH);
      }
      
      
      private void initComponents() {                          
          northPanel = new javax.swing.JPanel();
          messageLabel = new javax.swing.JLabel();
          southPanel = new javax.swing.JPanel();
          mainToolBar = new javax.swing.JToolBar();
          recordButton = new javax.swing.JButton();
          fileLabel = new javax.swing.JLabel();
         centerPanel = new javax.swing.JPanel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("My Webcam");
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         northPanel.setLayout(new java.awt.BorderLayout());

         messageLabel.setText("Status");
         northPanel.add(messageLabel, java.awt.BorderLayout.CENTER);
 
         getContentPane().add(northPanel, java.awt.BorderLayout.NORTH);
 
         southPanel.setLayout(new java.awt.BorderLayout());
 
         recordButton.setText("Record");
         recordButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 recordButtonActionPerformed(evt);
             }
         });
 
         mainToolBar.add(recordButton);
 
         fileLabel.setText("File:");
         mainToolBar.add(fileLabel);
 
         southPanel.add(mainToolBar, java.awt.BorderLayout.CENTER);
 
         getContentPane().add(southPanel, java.awt.BorderLayout.SOUTH);

         centerPanel.setLayout(new java.awt.BorderLayout());
 
         getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);
 
         pack();
     }                        
     
     private void formWindowClosing(java.awt.event.WindowEvent evt) {                                   
         processor.close();
     }                                  
     
     private void recordButtonActionPerformed(java.awt.event.ActionEvent evt) {   
    	 
         if(recordButton.getText().equals("Record")){
             fileLabel.setText("File:");
             
             if (movieChooser == null) movieChooser = new JFileChooser();
             movieChooser.setDialogType(JFileChooser.SAVE_DIALOG);
             //Add a custom file filter and disable the default
             //(Accept All) file filter.
             movieChooser.addChoosableFileFilter(new MOVFilter());
             movieChooser.setAcceptAllFileFilterUsed(false);
             movieChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
             int returnVal = movieChooser.showDialog(this, "Record");
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 file = movieChooser.getSelectedFile();
                 if(!file.getName().endsWith(".mov")&&!file.getName().endsWith(".MOV")) file = new File(file.toString() + ".mov");
                 recordToFile(file);
                 fileLabel.setText("File:" + file.toString());
                 recordButton.setText("Stop");
             }
         }else{ 
             stopRecording();
             JOptionPane.showMessageDialog(this, file.length(), "Error", JOptionPane.WARNING_MESSAGE);
             recordButton.setText("Record");
             ClientPart cl = new ClientPart();
             cl.sendFile(file);
         }
     }                                            
     
     void setJPEGQuality(Player p, float val) {
         Control cs[] = p.getControls();
         QualityControl qc = null;
         VideoFormat jpegFmt = new VideoFormat(VideoFormat.MPEG);
         
         // Loop through the controls to find the Quality control for
         // the JPEG encoder.
         for (int i = 0; i < cs.length; i++) {
             if (cs[i] instanceof QualityControl && cs[i] instanceof Owned) {
                 Object owner = ((Owned)cs[i]).getOwner();
                 // Check to see if the owner is a Codec.
                 // Then check for the output format.
                 if (owner instanceof Codec) {
                    Format fmts[] = ((Codec)owner).getSupportedOutputFormats(null);
                     for (int j = 0; j < fmts.length; j++) {
                        if (fmts[j].matches(jpegFmt)) {
                             qc = (QualityControl)cs[i];
                             qc.setQuality(val);
                             break;
                         }
                     }
                 }
                 if (qc != null) break;
            }
         }
     }
     void setAudQuality(Player p, float val) {
         Control cs[] = p.getControls();
         QualityControl qc = null;
         AudioFormat imaFmt = new AudioFormat(AudioFormat.IMA4);
         
         // Loop through the controls to find the Quality control for
         // the JPEG encoder.
         for (int i = 0; i < cs.length; i++) {
             if (cs[i] instanceof QualityControl && cs[i] instanceof Owned) {
                 Object owner = ((Owned)cs[i]).getOwner();
                 // Check to see if the owner is a Codec.
                 // Then check for the output format.
                 if (owner instanceof Codec) {
                    Format fmts[] = ((Codec)owner).getSupportedOutputFormats(null);
                     for (int j = 0; j < fmts.length; j++) {
                        if (fmts[j].matches(imaFmt)) {
                             qc = (QualityControl)cs[i];
                             qc.setQuality(val);
                             break;
                         }
                     }
                 }
                 if (qc != null) break;
            }
         }
     }
     public void checkIncoding(TrackControl track[]){
    	 VideoFormat jpegFormat = null;
    	 
         for (int i = 0; i < track.length; i++) {
        	
             Format format = track[i].getFormat();
             
             if (track[i].isEnabled() && format instanceof VideoFormat) {
                 Dimension size = ((VideoFormat)format).getSize();
                 float frameRate = ((VideoFormat)format).getFrameRate();
                 int w = (size.width % 8 == 0 ? size.width :(int)(size.width / 8) * 8);
                 int h = (size.height % 8 == 0 ? size.height :(int)(size.height / 8) * 8);
                 jpegFormat = new VideoFormat(VideoFormat.JPEG_RTP, new Dimension(w, h), Format.NOT_SPECIFIED, Format.byteArray, frameRate);
                 
                 //VideoFormat jpegFormat = new VideoFormat(((VideoFormat)format).getEncoding(), new Dimension(w, h), ((VideoFormat)format).getMaxDataLength(), ((VideoFormat)format).getDataType(), 15.0f);
                 
                 track[i].setFormat(jpegFormat);

                 messageLabel.setText("Status: Video transmitted as: " + jpegFormat.toString());
                 
             }
             if (track[i].isEnabled() && format instanceof AudioFormat) {
                 double frameRate = ((AudioFormat)format).getFrameRate();
                 AudioFormat audiFormat = new AudioFormat(AudioFormat.LINEAR, -1.0, -1, -1, -1, -1, -1, -1.0, Format.byteArray);
                 track[i].setFormat(null);
                 track[i].setEnabled(false);
                 JOptionPane.showMessageDialog(this, track[i].isEnabled(), "Error", JOptionPane.WARNING_MESSAGE);
                 
                 messageLabel.setText("Status: Video transmitted as: "  + "Aud " + audiFormat.toString());
             }
         }
     }
     
     @SuppressWarnings("deprecation")
	public void recordToFile(File file){
         URL movieUrl = null;
         MediaLocator dest = null;
         try{
             movieUrl = file.toURL();
             dest = new MediaLocator(movieUrl);
         }catch(MalformedURLException e){
            
         }
         
         recordCamSource = dataSource.cloneCamSource();
         try{
            recordProcessor = Manager.createProcessor(recordCamSource);
         }catch (IOException e) {
             JOptionPane.showMessageDialog(this, "Exception creating record processor: " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
             return;
         }catch (NoProcessorException e) {
             JOptionPane.showMessageDialog(this, "Exception creating record processor: " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
             return;
         }
         playhelper = new camStateHelper(recordProcessor);
         if(!playhelper.configure(10000)){
             JOptionPane.showMessageDialog(this, "cannot configure record processor", "Error", JOptionPane.WARNING_MESSAGE);
             return;
         }
         //Format[] formats = new Format[2];
      // VideoFormat vmft = new VideoFormat(VideoFormat.CINEPAK);
         //formats[1] = new AudioFormat(AudioFormat.IMA4);
       Format formats[] = new Format[2];
       formats[0] = new AudioFormat(AudioFormat.LINEAR);
       formats[1] = new VideoFormat(VideoFormat.CINEPAK);
         (recordProcessor.getTrackControls())[0].setFormat(formats[0]);
         (recordProcessor.getTrackControls())[0].setEnabled(true);
         (recordProcessor.getTrackControls())[1].setFormat(formats[1]);
         (recordProcessor.getTrackControls())[1].setEnabled(true);
         recordProcessor.setContentDescriptor(new FileTypeDescriptor(FileTypeDescriptor.QUICKTIME));
      // FileTypeDescriptor outputType =
        //       new FileTypeDescriptor(FileTypeDescriptor.QUICKTIME);
     
     //    recordProcessor = Manager.createRealizedProcessor(new ProcessorModel(formats,
       //          outputType));
     
         if(!playhelper.realize(10000)){
             JOptionPane.showMessageDialog(this, "cannot realize processor", "Error", JOptionPane.WARNING_MESSAGE);
             return;
         }
        // Control control = recordProcessor.getControl("javax.media.control.FrameRateControl");
         //if ( control != null && control instanceof javax.media.control.FrameRateControl ) ((javax.media.control.FrameRateControl)control).setFrameRate(0.7f);
         try {
             if(recordProcessor.getDataOutput()==null){
                 JOptionPane.showMessageDialog(this, "No Data Output", "Error", JOptionPane.WARNING_MESSAGE);
                 return;
             }
             dataSink = Manager.createDataSink(recordProcessor.getDataOutput(), dest);
            recordProcessor.start();
            dataSink.open();
            dataSink.start();
         } catch (NoDataSinkException ex) {
             JOptionPane.showMessageDialog(this, "No DataSink " + ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(this, "IOException " + ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
         }
     }
     
     public void stopRecording(){
         try {
        	 
             recordProcessor.close();
             dataSink.stop();
             dataSink.close();
         } catch (IOException e) {
             JOptionPane.showMessageDialog(this, "cannot stop recording " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
         }
         
     }
     
     // Variables declaration - do not modify                     
     private javax.swing.JPanel centerPanel;
     private javax.swing.JLabel fileLabel;
     private javax.swing.JToolBar mainToolBar;
     private javax.swing.JLabel messageLabel;
     private javax.swing.JPanel northPanel;
     private javax.swing.JButton recordButton;
     private javax.swing.JPanel southPanel;
     // End of variables declaration                   
     
     CaptureDeviceInfo  audioDevice = null;
     CaptureDeviceInfo  videoDevice = null;
 
}